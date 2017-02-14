package com.waves_rsp.ikb4stream.communication.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WebCommunicationTest {
    private WebCommunication webCommunication;

    @Test
    @Before
    public void testCreateWebCommunication() {
        webCommunication = new WebCommunication();
    }

    @Test
    public void testStart() {
        webCommunication.start((request, callback) -> callback.onResult(null, "{}"));
    }

    @Test(expected = NullPointerException.class)
    public void testStartNull() {
        webCommunication.start(null);
    }

    @Test
    @After
    public void testClose() {
        webCommunication.close();
    }
}
