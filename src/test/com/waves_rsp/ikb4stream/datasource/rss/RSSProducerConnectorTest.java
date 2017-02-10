package com.waves_rsp.ikb4stream.datasource.rss;

import org.junit.Before;
import org.junit.Test;

public class RSSProducerConnectorTest {
    private RSSProducerConnector rss;

    @Before
    @Test
    public void testCreateRSSProducer() {
        rss = new RSSProducerConnector();
    }

    @Test(expected = NullPointerException.class)
    public void testLoadNullDataProducer() {
        rss.load(null);
    }

    @Test
    public void testLoad() {
        Thread t = new Thread(() -> rss.load(event -> {
            // Do nothing
        }));
        t.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Do nothing
        } finally {
            t.interrupt();
        }
    }
}
