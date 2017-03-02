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

import com.waves_rsp.ikb4stream.core.model.LatLong;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * BoundingBox class represents an area by coordinates
 *
 * @author ikb4stream
 * @version 1.0
 */
public class BoundingBox {
    /**
     * Array of {@link LatLong} to represent a {@link BoundingBox}
     *
     * @see LatLong#getLongitude()
     * @see LatLong#getLatitude()
     */
    private final LatLong[] latLongs;

    /**
     * The constructor of BoundingBox class
     *
     * @param points an array of LatLong
     * @throws NullPointerException     if points is null
     * @throws IllegalArgumentException if points has invalid size
     * @see BoundingBox#latLongs
     */
    public BoundingBox(LatLong[] points) {
        Objects.requireNonNull(points);
        Arrays.stream(points).forEach(Objects::requireNonNull);
        if (points.length < 1) {
            throw new IllegalArgumentException("We need at least 1 point in bounding box ");
        }
        this.latLongs = points;
    }

    /**
     * Get {@link BoundingBox} as array of {@link LatLong}
     *
     * @return Array of {@link LatLong}
     * @see BoundingBox#latLongs
     */
    public LatLong[] getLatLongs() {
        return latLongs;
    }

    /**
     * Represent that object in string
     *
     * @return String that represents this {@link BoundingBox}
     * @see BoundingBox#latLongs
     */
    @Override
    public String toString() {
        return '[' + Arrays.stream(latLongs).map(LatLong::toString).collect(Collectors.joining(",", "{", "}")) + "]";
    }
}
