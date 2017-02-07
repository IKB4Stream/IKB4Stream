package com.waves_rsp.ikb4stream.datasource;

import com.waves_rsp.ikb4stream.producer.datasource.ProducerManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ProducerManagerTest {
    private ProducerManager producerManager;

    @Test
    @Before
    public void testCreateProducerManager() {
        producerManager = ProducerManager.getInstance();
    }

    @Test
    public void testInstantiate() throws IOException {
        producerManager.instantiate();
    }

    @Test
    @After
    public void testStop() throws InterruptedException {
        Thread.sleep(1000);
        producerManager.stop();
    }
}
