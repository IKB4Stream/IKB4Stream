package com.waves_rsp.ikb4stream.consumer.model;

import com.waves_rsp.ikb4stream.consumer.AnomalyRequest;
import org.junit.Test;

/**
 * Created by ikb4stream on 07/02/17.
 */
public class AnomalyRequestTest {
    @Test(expected = NullPointerException.class)
    public void nullDate() { new AnomalyRequest(null, -80.0, +80.0, -160.0, +160.0); }
}