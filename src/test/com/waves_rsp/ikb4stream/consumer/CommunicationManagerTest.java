package com.waves_rsp.ikb4stream.consumer;

import com.waves_rsp.ikb4stream.consumer.manager.CommunicationManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class CommunicationManagerTest {
    private CommunicationManager communicationManager;

    @Ignore
    @Test
    @Before
    public void testCreateCommunicationManager() {
        communicationManager = CommunicationManager.getInstance();
    }

    @Ignore
    @Test
    public void testStart() throws IOException {
        communicationManager.start();
    }

    @Ignore
    @Test
    @After
    public void testStop() {
        communicationManager.stop();
    }
}
