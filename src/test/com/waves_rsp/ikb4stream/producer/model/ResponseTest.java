package com.waves_rsp.ikb4stream.producer.model;

import org.junit.Test;

public class ResponseTest {
    @Test(expected = NullPointerException.class)
    public void nullResponse() {
        new Response(null, null);
    }
}
