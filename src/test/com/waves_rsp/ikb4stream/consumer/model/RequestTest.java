package com.waves_rsp.ikb4stream.consumer.model;

import com.waves_rsp.ikb4stream.consumer.model.Request;
import org.junit.Test;

public class RequestTest {

    @Test(expected = NullPointerException.class)
    public void nullRequest() {
        new Request(null, null, null, null);
    }
}
