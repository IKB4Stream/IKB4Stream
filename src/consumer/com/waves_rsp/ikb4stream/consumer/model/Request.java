package com.waves_rsp.ikb4stream.consumer.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

public class Request {
    private final Date start;
    private final Date end;
    private final BoundingBox boundingBox;
    private final Date requestReception;

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

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public Date getRequestReceptionDate() {
        return requestReception;
    }
}
