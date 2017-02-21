package com.waves_rsp.ikb4stream.datasource.owm;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class OWMProducerConnectorTest {

    private OWMProducerConnector owm = new OWMProducerConnector();

    @Before
    @Test
    public void testWeatherProducerConnector() {
        owm = new OWMProducerConnector();
    }

    @Test(expected = NullPointerException.class)
    public void checkNullDataProducer() {
        owm.load(null);
    }

    @Test
    public void testLoad() {
        Thread t = new Thread(() -> owm.load(event -> {
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

    @Test
    public void testDescriptionIsNotNull() {
        Thread t = new Thread(() -> owm.load(event -> {
            //System.out.println("Description : " + event.getDescription());
            assertNotNull(event.getDescription());
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

    @Test
    public void testLocationIsNotNull() {
        Thread t = new Thread(() -> owm.load(event -> {
            //System.out.println("Location : " + event.getLocation());
            assertNotNull(event.getLocation());
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

    @Test
    public void testDateStartIsNotNull() {
        Thread t = new Thread(() -> owm.load(event -> {
            //System.out.println("Date Start : " + event.getStart());
            assertNotNull(event.getStart());
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
    @Test
    public void testDateEndIsNotNull() {
        Thread t = new Thread(() -> owm.load(event -> {
            //System.out.println("Date End : " + event.getEnd());
            assertNotNull(event.getEnd());
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

    @Test
    public void testSourceIsEqualOWM() {
        Thread t = new Thread(() -> owm.load(event -> {
            //System.out.println("Location : " + event.getLocation());
            assertEquals("OpenWeatherMap", event.getSource());
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
