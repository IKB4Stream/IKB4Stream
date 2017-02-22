package com.waves_rsp.ikb4stream.datasource.owm;

import org.junit.Before;
import org.junit.Test;

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
            assert(event.getDescription() != null);
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
            assert(event.getLocation() != null);
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
            assert(event.getStart ()!= null);
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
            assert(event.getEnd() != null);
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
            assert("OpenWeatherMap".equals(event.getSource()));
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
