package com.waves_rsp.ikb4stream.consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class CommunicationManagerTest {
    private CommunicationManager communicationManager;

    @Test
    @Before
    public void testCreateCommunicationManager() {
        communicationManager = CommunicationManager.getInstance();
    }

    @Test
    public void testStart() throws IOException {
        communicationManager.start();
    }

    @Test
    @After
    public void testStop() {
        communicationManager.stop();
    }
}
