package com.waves_rsp.ikb4stream.core.communication.model;

import org.junit.Test;

public class ResponseTest {
    @Test(expected = NullPointerException.class)
    public void nullResponse() {
        new Response(null, null);
    }
}
