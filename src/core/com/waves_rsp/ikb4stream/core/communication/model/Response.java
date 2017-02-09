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
