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

package dk.dma.ais.filter;

import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.packet.AisPacketFilters;
import dk.dma.enav.model.geometry.BoundingBox;
import dk.dma.enav.util.function.Predicate;

import java.util.List;

/**
 * The GeoMaskFilter is instantiated with a number of BoundingBoxes. If an AisPacket passed to the filter
 * origins from inside one of these bounding boxes, then it is rejected.
 */
public class GeoMaskFilter implements IPacketFilter {

    final Predicate<AisPacket> blocked;
    final List<BoundingBox> suppressedBoundingBoxes;

    public GeoMaskFilter(List<BoundingBox> suppressedBoundingBoxes) {
        this.suppressedBoundingBoxes = suppressedBoundingBoxes;

        Predicate oredPredicates = null;

        for (BoundingBox bbox : suppressedBoundingBoxes) {
            if (oredPredicates == null) {
                oredPredicates = AisPacketFilters.filterOnMessagePositionWithin(bbox);
            } else {
                oredPredicates = oredPredicates.or(AisPacketFilters.filterOnMessagePositionWithin(bbox));
            }
        }

        this.blocked = oredPredicates;
    }

    @SuppressWarnings("Unused")
    public List<BoundingBox> getSuppressedBoundingBoxes() {
        return suppressedBoundingBoxes;
    }

    @Override
    public boolean rejectedByFilter(AisPacket aisPacket) {
        return blocked.test(aisPacket);
    }
}
