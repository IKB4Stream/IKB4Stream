package com.waves_rsp.ikb4stream.datasource.dbpedia;

import org.junit.Ignore;
import org.junit.Test;

public class DBpediaProducerTest {

    @Test (expected = NullPointerException.class)
    public void checkNullProducer() {
        DBpediaProducerConnector dBpediaProducerConnector = DBpediaProducerConnector.getInstance();
        dBpediaProducerConnector.load(null);
    }

    @Ignore
    @Test
    public void checkProducerResults() {
        DBpediaProducerConnector dBpediaProducerConnector = DBpediaProducerConnector.getInstance();
        dBpediaProducerConnector.load(dataProducer -> {
            //Do nothing
        });
    }
}
