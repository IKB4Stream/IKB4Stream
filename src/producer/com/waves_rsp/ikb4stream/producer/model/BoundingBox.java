package com.waves_rsp.ikb4stream.producer.model;

import com.waves_rsp.ikb4stream.core.model.LatLong;

import java.util.Objects;

/**
 * BoundingBox class which represents an area
 */
public class BoundingBox {
    private final LatLong[] latLongs;

    /**
     * The BoundingBox Constructor
     * @param points an array of LatLong
     * @throws NullPointerException if {@param points} is null
     * @throws IllegalArgumentException {@param points} size is lower than 1
     */
    public BoundingBox(LatLong[] points) {
        Objects.requireNonNull(points);
        if(points.length < 1) { throw new IllegalArgumentException("We need at least 1 point in bounding box "); }
        this.latLongs = points;
    }

    /**
     * @return an array of LatLong
     */
    public LatLong[] getLatLongs() {
        return latLongs;
    }
}
