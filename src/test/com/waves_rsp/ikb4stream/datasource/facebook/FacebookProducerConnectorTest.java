package com.waves_rsp.ikb4stream.datasource.facebook;


import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.IntStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class FacebookProducerConnectorTest {

    private final FacebookProducerConnector fb = new FacebookProducerConnector();

    @Test
    public void testInstanceFacebookConnector() {
        FacebookProducerConnector fb = new FacebookProducerConnector();
    }

    @Test
    public void checkValidFacebookEvents() {
        fb.load(dataProducer -> {
            //Do nothing
        });
    }

    @Test
    public void checkDate() throws ParseException {
        String form = "yyyy-MM-dd'T'HH:mm:ss.SSSS";
        DateFormat dateFormat = new SimpleDateFormat(form);
        Date date = dateFormat.getCalendar().getTime();
        System.out.println(date);
    }

    @Test
    public void checkThreadInterrupted() {
        FacebookProducerConnector fb = new FacebookProducerConnector();
        try {
            fb.load(dataProducer -> {

            });
            Thread.currentThread().join();
        } catch (InterruptedException err) {

        }
    }

    @Test
    public void checkFacebookEventsWithPoolThreads() {
        Thread[] threads = new Thread[10];
        IntStream.range(0, threads.length).forEach(i -> {
            threads[i] = new Thread(() -> {
                fb.load(dataProducer -> {
                    //Do nothing
                });
            });
        });

        Arrays.stream(threads).forEach(Thread::start);

        Arrays.stream(threads).forEach(thread -> {
            try {
                thread.join();
                thread.interrupt();
            } catch (InterruptedException e) {
                //Do nothing
            }
        });

        Thread.currentThread().interrupt();
    }

    @Test
    public void testDescriptionFBEventIsNotNull() {
        Thread t = new Thread(() -> fb.load(event -> {
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
    public void testSourceFBEventIsNotNull() {
        Thread t = new Thread(() -> fb.load(event -> {
            assertNotNull(event.getSource());
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
    public void testDateFBEventIsNotNull() {
        Thread t = new Thread(() -> fb.load(event -> {
            assertNotNull(event.getStart());
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
    public void testLatLongFBEventIsNotNull() {
        Thread t = new Thread(() -> fb.load(event -> {
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
    public void testScoreFBEventIsNotNull() {
        Thread t = new Thread(() -> fb.load(event -> {
            assertTrue(event.getScore()<= 100);
            assertTrue(event.getScore()>= -1);
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
