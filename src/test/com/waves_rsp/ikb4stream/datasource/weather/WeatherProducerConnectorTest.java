package com.waves_rsp.ikb4stream.datasource.weather;

import org.junit.Before;
import org.junit.Test;


public class WeatherProducerConnectorTest {
    private WeatherProducerConnector weather;

    @Before
    @Test
    public void testWeatherProducerConnector() {
        weather = new WeatherProducerConnector();
    }

    @Test(expected = NullPointerException.class)
    public void testLoadNullDataProducer() {
        weather.load(null);
    }

    @Test
    public void testLoad() {
        Thread t = new Thread(() -> weather.load(event -> {
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
