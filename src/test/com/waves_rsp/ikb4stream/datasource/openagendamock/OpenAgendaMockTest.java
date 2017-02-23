package com.waves_rsp.ikb4stream.datasource.openagendamock;

import org.junit.Before;
import org.junit.Test;

public class OpenAgendaMockTest {

    @Before
    @Test
    public void createInstanceTest() {
        new OpenAgendaMock();
    }

    @Test(expected = NullPointerException.class)
    public void dataProducerNull() {
        OpenAgendaMock oa = new OpenAgendaMock();
        oa.load(null);
    }

    @Test (expected = NullPointerException.class)
    public void checkNullInstanceOfOpenAgendaMock() {
        OpenAgendaMock oa = null;
        oa.load(dataProducer -> {
            //Do nothing
        });

    }
}
