package com.waves_rsp.ikb4stream.datasource.facebook;


import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.IntStream;

public class FacebookProducerConnectorTest {

    @Test
    public void testInstanceFacebookConnector() {
        FacebookProducerConnector fb = new FacebookProducerConnector();
    }

    @Test
    public void checkValidFacebookEvents() {
        FacebookProducerConnector fb = new FacebookProducerConnector();
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
        }catch (InterruptedException err) {

        }
    }

    @Test
    public void checkFacebookEventsWithPoolThreads() {
        Thread[] threads = new Thread[10];
        IntStream.range(0, threads.length).forEach(i -> {
            threads[i] = new Thread(() -> {
                FacebookProducerConnector fb = new FacebookProducerConnector();
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
}
