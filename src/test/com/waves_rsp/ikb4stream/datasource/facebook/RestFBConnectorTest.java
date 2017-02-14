package com.waves_rsp.ikb4stream.datasource.facebook;

import facebook.RestFBConnector;
import org.junit.Test;

/**
 * Created by ikb4stream on 14/02/17.
 */
public class RestFBConnectorTest {

    @Test(expected = NullPointerException.class)
    public void testLoadNullDataProducer() {
        RestFBConnector fb = new RestFBConnector();
        fb.searchWordFromGeolocation(null, 100,48.8781161065379, 2.22459235221894);
    }


}
