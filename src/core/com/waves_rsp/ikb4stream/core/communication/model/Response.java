/*
 * Copyright (C) 2017 ikb4stream team
 * ikb4stream is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * ikb4stream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

package com.waves_rsp.ikb4stream.core.communication.model;

import com.waves_rsp.ikb4stream.core.model.Event;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Response class which represents the response of an anomaly
 * @see Request which is an anomaly
 */
public class Response {
    private final List<Event> events;
    private final Request request;

    /**
     * The response constructor
     * @param events list of events
     * @param request an anomaly
     * @throws NullPointerException if {@param events} or {@param request} is null
     */
    public Response(List<Event> events, Request request) {
        Objects.requireNonNull(events);
        Objects.requireNonNull(request);

        this.events = events;
        this.request = request;
    }

    /**
     *
     * @return a list of events
     */
    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    /**
     *
     * @return an anomaly
     */
    public Request getRequest() {
        return request;
    }
}
