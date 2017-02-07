package com.waves_rsp.ikb4stream.consumer.model;

import com.waves_rsp.ikb4stream.consumer.AnomalyRequest;
import org.junit.Test;

import java.util.Date;

/**
 * Created by ikb4stream on 07/02/17.
 */
public class AnomalyRequestTest {
    @Test(expected = NullPointerException.class)
    public void nullDate() { new AnomalyRequest(null, -80.0, +80.0, -160.0, +160.0); }

    @Test(expected = IllegalArgumentException.class)
    public void minLatGreaterThanMaxLat() { new AnomalyRequest(new Date(), 0.0, -5.0, -160.0, +160.0); }

    @Test(expected = IllegalArgumentException.class)
    public void minLongGreaterThanMaxLong() { new AnomalyRequest(new Date(), -80.0, +80.0, 0.0, -5.0); }


}