package com.waves_rsp.ikb4stream.datasource.twitter;

import org.junit.Test;

public class TwitterProducerConnectorTest {

    @Test
    public void checkTweetsFromTwitter() {
        TwitterProducerConnector producerConnector = TwitterProducerConnector.create();
        producerConnector.load(System.out::println);
    }

    @Test (expected = NullPointerException.class)
    public void checkNullDataProducer() {
        TwitterProducerConnector producerConnector = TwitterProducerConnector.create();
        producerConnector.load(null);
    }

}
