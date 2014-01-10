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

package dk.dma.ais.abnormal.tracker.events;

import com.google.common.base.Objects;
import dk.dma.ais.abnormal.tracker.Track;
import dk.dma.enav.model.geometry.Position;

public class PositionChangedEvent extends TrackingEvent {
    private final Position oldPosition;

    public PositionChangedEvent(Track track, Position oldPosition) {
        super(track);
        this.oldPosition = oldPosition;
    }

    public final Position getOldPosition() {
        return oldPosition;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("mmsi", getTrack().getMmsi())
                .add("oldPosition", oldPosition)
                .add("newPosition", getTrack().getProperty(Track.POSITION))
                .toString();
    }
}
