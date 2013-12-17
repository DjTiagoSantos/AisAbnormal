/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.ais.abnormal.stat.rest;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import dk.dma.ais.abnormal.stat.db.FeatureDataRepository;
import dk.dma.ais.abnormal.stat.db.data.FeatureData;
import dk.dma.ais.abnormal.stat.db.data.FeatureData2Key;
import dk.dma.enav.model.geometry.Area;
import dk.dma.enav.model.geometry.BoundingBox;
import dk.dma.enav.model.geometry.CoordinateSystem;
import dk.dma.enav.model.geometry.Position;
import dk.dma.enav.model.geometry.grid.Cell;
import dk.dma.enav.model.geometry.grid.Grid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@RequestScoped
@Path("/cell")
public class CellResource {

    private static final Logger LOG = LoggerFactory.getLogger(CellResource.class);
    static {
        LOG.debug("CellResource loaded.");
    }
    {
        LOG.debug(this.getClass().getSimpleName() + " created (" + this + " ).");
    }

    private FeatureDataRepository featureDataRepository;

    @Inject
    public CellResource(FeatureDataRepository featureDataRepository) {
        this.featureDataRepository = featureDataRepository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Set<CellWrapper> getCellIdsWithinBoundaries(@QueryParam("north") Double north, @QueryParam("east") Double east, @QueryParam("south") Double south, @QueryParam("west") Double west) {
        // http://localhost:8080/abnormal/featuredata/cell?north=55&east=11&south=54.91&west=10.91
        LOG.debug("Attempting get id's of cells inside boundary");

        if (north == null) {
            throw new IllegalArgumentException("Missing 'north' parameter");
        }
        if (east == null) {
            throw new IllegalArgumentException("Missing 'east' parameter");
        }
        if (west == null) {
            throw new IllegalArgumentException("Missing 'west' parameter");
        }
        if (south == null) {
            throw new IllegalArgumentException("Missing 'south' parameter");
        }
        if (north <= south) {
            throw new IllegalArgumentException("'north' parameter must be > 'south' parameter");
        }
        if (east <= west) {
            throw new IllegalArgumentException("'east' parameter must be > 'west' parameter");
        }
        if (north - south > 0.1000000001) {
      //      throw new IllegalArgumentException("'north' and 'south' parameters must be within 0.1. The current difference is " + (north - south));
        }
        if (east - west > 0.1000000001) {
      //      throw new IllegalArgumentException("'east' and 'west' parameters must be within 0.1. The current difference is " + (east - west));
        }
        LOG.debug("parameters are ok");

        Double gridResolution = featureDataRepository.getMetaData().getGridResolution();

        LOG.debug("Feature set uses grid resolution of " + gridResolution);
        Grid grid = Grid.create(gridResolution);
        LOG.debug("Created grid with resolution " + gridResolution);

        LOG.debug("Looking for cells touching area bounded by " + north + " north, " + east + " east, " + south + " south, and " + west + " west.");
        Position northWest = Position.create(north, west);
        Position southEast = Position.create(south, east);
        Area area = BoundingBox.create(northWest, southEast, CoordinateSystem.GEODETIC);

        Set<CellWrapper> cells = loadCellsInArea(grid, area);
        //Set<CellWrapper> cells = loadCellsWithData(grid, area);
        //Set<CellWrapper> cells = loadDummyCells(grid, area);

        return cells;
    }

    private Set<CellWrapper> loadCellsInArea(Grid grid, Area area) {
        // These are the features stored in the data set
        Set<String> featureNames = featureDataRepository.getFeatureNames();

        // Compute the cells contained inside the area
        LOG.debug("Computing which cells are in the area.");
        Set<Cell> cells = grid.getCells(area);
        LOG.debug("There are " + cells.size() + " cells in the area.");

        // Container to collect output data
        Set<CellWrapper> wrappedCells = new LinkedHashSet<>();

        // Load feature data for cells inside the area
        for (Cell cell : cells) {
            ArrayList<FeatureData> featureDataArray = new ArrayList<>();
            for (String featureName : featureNames) {
                FeatureData featureData = featureDataRepository.getFeatureData(featureName, cell.getCellId());
                if (featureData != null) {
                    featureDataArray.add(featureData);
                }
            }

            // Add the cell to the output only if its has statistical data
            if (featureDataArray.size() > 0) {
                BoundingBox boundingBoxOfCell = grid.getBoundingBoxOfCell(cell);
                CellWrapper wrappedCell = new CellWrapper(cell, boundingBoxOfCell, featureDataArray.toArray(new FeatureData[featureDataArray.size()]));
                // Add cell to output
                wrappedCells.add(wrappedCell);
            }
        }

        LOG.debug("There are " + wrappedCells.size() + " cells with feature data in the area");
        return wrappedCells;
    }

    /**
     * Load all cells with have statistical data from the repository. This method is not for production use, but intended
     * for test and development only.
     *
     * @param grid the grid system to use
     * @param area the area - not used; included for signature compliance.
     * @return
     */
    private Set<CellWrapper> loadCellsWithData(Grid grid, Area area) {
        // CellId , BoundingBox , Set<FeatureData>

        // Container to collect output data
        Set<CellWrapper> wrappedCells = new LinkedHashSet<>();

        Set<String> featureNames = featureDataRepository.getFeatureNames();
        Set<Long> cellIds = featureDataRepository.getAllCellsWithData();
        for (Long cellId : cellIds) {
            ArrayList<FeatureData> featureDataArray = new ArrayList<>();

            for (String featureName : featureNames) {
                FeatureData featureData = featureDataRepository.getFeatureData(featureName, cellId);
                if (featureData != null) {
                    featureDataArray.add(featureData);
                }
            }

            Cell cell = grid.getCell(cellId);
            BoundingBox boundingBoxOfCell = grid.getBoundingBoxOfCell(cell);

            CellWrapper wrappedCell = new CellWrapper(cell, boundingBoxOfCell, featureDataArray.toArray(new FeatureData[featureDataArray.size()]));
            wrappedCells.add(wrappedCell);
        }

        LOG.debug("There are " + wrappedCells.size() + " cells with feature data in the area");
        return wrappedCells;
    }

    /**
     * Simulate loading of cells from the repository, but actually generate an artificial pattern of cells with data.
     * This method is not for production use, but intended for test and development only.
     *
     * @param grid the grid system to use
     * @param area the area - not used; included for signature compliance.
     * @return
     */
    private Set<CellWrapper> loadDummyCells(Grid grid, Area area) {
        Set<CellWrapper> cells = new LinkedHashSet<>();

        for (double lon = 12.0; lon < 12.50; lon += 0.05) {
            for (double lat = 56.0; lat < 56.50; lat += 0.05) {
                FeatureData2Key feature1Data = new FeatureData2Key(this.getClass().getCanonicalName(), "shipType", "shipSize");
                feature1Data.setStatistic((short) 1, (short) 1, "stat1", (Integer) 7);

                FeatureData2Key feature2Data = new FeatureData2Key(Integer.class.getCanonicalName(), "prime", "square");
                feature2Data.setStatistic((short) 1, (short) 1, "statA", (Integer) 9);
                feature2Data.setStatistic((short) 1, (short) 2, "statA", (Integer) 8);
                feature2Data.setStatistic((short) 2, (short) 1, "statA", (Integer) 7);

                Position nw = Position.create(lat+0.02, lon-0.02);
                Position se = Position.create(lat-0.02, lon+0.02);
                BoundingBox boundingBoxOfCell = BoundingBox.create(nw, se, CoordinateSystem.GEODETIC);

                Cell cell = grid.getCell(lat, lon);
                CellWrapper cellWrapper = new CellWrapper(cell, boundingBoxOfCell, feature1Data, feature2Data);
                cells.add(cellWrapper);
            }
        }

        return cells;
    }

    public class CellWrapper {
        private final Cell cell;
        private final double north;
        private final double east;
        private final double south;
        private final double west;
        private final Set<FeatureData> featureData;

        public long getCellId() {
            return cell.getCellId();
        }

        public double getNorth() {
            return north;
        }

        public double getEast() {
            return east;
        }

        public double getSouth() {
            return south;
        }

        public double getWest() {
            return west;
        }

        public Set<FeatureData> getFeatureData() {
            return featureData;
        }

        public CellWrapper(Cell cell, BoundingBox boundingBox, FeatureData... featureData) {
            this.cell = cell;
            this.north = boundingBox.getMaxLat();
            this.east = boundingBox.getMaxLon();
            this.south = boundingBox.getMinLat();
            this.west = boundingBox.getMinLon();
            this.featureData = new HashSet<>(Arrays.asList(featureData));
        }
    }
}
