package com.waves_rsp.ikb4stream.consumer.model;

import com.waves_rsp.ikb4stream.consumer.model.BoundingBox;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import org.junit.Test;

public class BoundingBoxTest {
    @Test(expected = IllegalArgumentException.class)
    public void lessThan1PointBoundingBox() {
        LatLong[] latLongs = new LatLong[0];
        new BoundingBox(latLongs);
    }

    @Test(expected = NullPointerException.class)
    public void nullBoundingBox() {
        new BoundingBox(null);
    }
}
