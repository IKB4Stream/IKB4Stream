package com.waves_rsp.ikb4stream.datasource.owm;

import com.aves_rsp.ikb4stream.datasource.owm.OWMProducerConnector;
import org.junit.Test;

public class OWMProducerConnectorTest {
    @Test(expected = NullPointerException.class)
    public void checkNullDataProducer() {
        OWMProducerConnector owm = new OWMProducerConnector();
        owm.load(null);
    }

}
