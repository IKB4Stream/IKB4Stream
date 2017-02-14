package com.waves_rsp.ikb4stream.datasource.twitter;

import org.junit.Ignore;
import org.junit.Test;

public class TwitterProducerConnectorTest {

    @Ignore
    @Test
    public void checkTweetsFromTwitter() {
        TwitterProducerConnector producerConnector = TwitterProducerConnector.getInstance();
        long start = System.currentTimeMillis();
        producerConnector.load(System.out::println);
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



}
