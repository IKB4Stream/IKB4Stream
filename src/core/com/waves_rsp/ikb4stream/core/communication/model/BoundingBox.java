package com.waves_rsp.ikb4stream.core.communication.model;

import com.waves_rsp.ikb4stream.core.model.LatLong;

import java.util.Objects;

public class BoundingBox {
    private final LatLong[] latLongs;

    public BoundingBox(LatLong[] points) {
        Objects.requireNonNull(points);
        if(points.length < 1) {
            throw new IllegalArgumentException("We need at least 1 point in bounding box ");
        }
        this.latLongs = points;
    }

    public LatLong[] getLatLongs() {
        return latLongs;
    }
}
