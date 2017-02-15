package com.waves_rsp.ikb4stream.datasource.twitter;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

public class TwitterProducerConnectorTest {

    @Ignore
    @Test
    public void checkTweetsFromTwitter() {
        TwitterProducerConnector producerConnector = TwitterProducerConnector.getInstance();
        long start = System.currentTimeMillis();
        producerConnector.load(dataProducer -> {
            //Do nothing
        });
        long end = System.currentTimeMillis();
        long result = end - start;
      //  Assert.assertTrue(result < 1000);
    }

    @Test (expected = NullPointerException.class)
    public void checkNullDataProducer() {
        TwitterProducerConnector producerConnector = TwitterProducerConnector.getInstance();
        producerConnector.load(null);
    }

    @Ignore
    @Test
    public void checkIllegalProperties() {
        try {
            TwitterProducerConnector producerConnector = TwitterProducerConnector.getInstance();
            producerConnector.load(dataProducer -> {
                //Do nothing
            });
        }catch (IllegalArgumentException err) {
            //Do nothing
        }
    }

    @Ignore
    @Test
    public void runTwitterConnectorWithPoolThreads() {
        Thread[] threads = new Thread[10];
        IntStream.range(0, threads.length).forEach(i -> {
            threads[i] = new Thread(() -> {
                TwitterProducerConnector producerConnector = TwitterProducerConnector.getInstance();
                producerConnector.load(dataProducer -> {
                    //Do nothing
                });
            });
        });

        Arrays.stream(threads).forEach(Thread::start);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            //Do nothing
        }finally {
            Arrays.stream(threads).forEach(Thread::interrupt);
        }
    }

    @Ignore
    @Test
    public void checkIllegalArgumentFromLoader() {
        TwitterProducerConnector producerConnector = TwitterProducerConnector.getInstance();
        try {
            producerConnector.load(dataProducer -> {

            });
        }catch (IllegalArgumentException err) {
            //Do nothing
        }
    }
}
