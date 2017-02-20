package com.waves_rsp.ikb4stream.datasource.openagenda;

import org.junit.Test;

/**
 * Created by ikb4stream on 20/02/17.
 */
public class OpenAgendaProducerConnectorTest {

    @Test(expected = NullPointerException.class)
    public void testLoadNullDataProducer() {
        OpenAgendaProducerConnector open = new OpenAgendaProducerConnector();
        open.load(null);
    }
}
