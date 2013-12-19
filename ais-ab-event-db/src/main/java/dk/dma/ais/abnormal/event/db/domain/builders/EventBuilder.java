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

package dk.dma.ais.abnormal.event.db.domain.builders;

import dk.dma.ais.abnormal.event.db.domain.Event;

import java.util.Date;

public abstract class EventBuilder {

    protected abstract Event getEvent();

    public EventBuilder startTime(Date startTime){
        getEvent().setStartTime(startTime);
        return this;
    }

    public EventBuilder state(Event.State state) {
        getEvent().setState(state);
        return this;
    }

    public EventBuilder description(String description){
        getEvent().setDescription(description);
        return this;
    }

    public BehaviourBuilder behaviour() {
        BehaviourBuilder builder = new BehaviourBuilder(this);
        getEvent().setBehaviour(builder.getBehaviour());
        return builder;
    }

}
