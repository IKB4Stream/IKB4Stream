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
 *
 * @author ikb4stream
 * @version 1.0
 */
public class Response {
    /**
     * List of all events that match {@link Request}
     *
     * @see Event
     * @see Response#getEvents()
     */
    private final List<Event> events;
    /**
     * Request to get List of {@link Event}
     *
     * @see Request
     * @see Response#getRequest()
     */
    private final Request request;

    /**
     * The response constructor
     *
     * @param events  list of events
     * @param request an anomaly
     * @throws NullPointerException if events or request is null
     */
    public Response(List<Event> events, Request request) {
        Objects.requireNonNull(events);
        Objects.requireNonNull(request);
        this.events = events;
        this.request = request;
    }

    /**
     * Get list of {@link Event} which {@link Request} found
     *
     * @return a list of {@link Event}
     */
    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    /**
     * Get {@link Request}
     *
     * @return {@link Request} to find {@link Event}
     */
    public Request getRequest() {
        return request;
    }
}
