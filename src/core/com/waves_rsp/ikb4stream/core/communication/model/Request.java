package com.waves_rsp.ikb4stream.core.communication.model;

import java.util.Date;
import java.util.Objects;

/**
 * Request class which represents an anomaly
 */
public class Request {
    private final Date start;
    private final Date end;
    private final BoundingBox boundingBox;
    private final Date requestReception;

    /**
     * The Request class constructor
     * @param start is the starting date of an anomaly
     * @param end is the end date of an anomaly
     * @param boundingBox coordinates
     * @param requestReception is the reception date of the request
     */
    public Request(Date start, Date end, BoundingBox boundingBox, Date requestReception) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        Objects.requireNonNull(boundingBox);
        Objects.requireNonNull(requestReception);

        this.start = start;
        this.end = end;
        this.boundingBox = boundingBox;
        this.requestReception = requestReception;
    }

    /**
     *
     * @return the starting date of an anomaly
     */
    public Date getStart() {
        return start;
    }

    /**
     *
     * @return the end date of an anomaly
     */
    public Date getEnd() {
        return end;
    }

    /**
     *
     * @return the coordinates of the boundingbox
     */
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     *
     * @return the reception date of the request
     */
    public Date getRequestReceptionDate() {
        return requestReception;
    }

    @Override
    public String toString() {
        return "Request{" +
                "start=" + start +
                ", end=" + end +
                ", boundingBox=" + boundingBox +
                ", requestReception=" + requestReception +
                '}';
    }
}
