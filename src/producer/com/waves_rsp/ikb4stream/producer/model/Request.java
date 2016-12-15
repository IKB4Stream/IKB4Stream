package com.waves_rsp.ikb4stream.producer.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Request {
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final BoundingBox boundingBox;
    private final LocalDateTime requestReception;

    public Request(LocalDateTime start, LocalDateTime end, BoundingBox boundingBox, LocalDateTime requestReception) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        Objects.requireNonNull(boundingBox);
        Objects.requireNonNull(requestReception);

        this.start = start;
        this.end = end;
        this.boundingBox = boundingBox;
        this.requestReception = requestReception;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public LocalDateTime getRequestReceptionDate() {
        return requestReception;
    }
}
