package com.waves_rsp.ikb4stream.core.util;

import com.waves_rsp.ikb4stream.core.model.LatLong;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ikb4stream on 13/02/17.
 */
public class GeoCodeJacksonParserTest {
    @Test(expected = NullPointerException.class)
    public void nullAddress() {
        new GeoCoderJacksonParser().parse(null);
    }


    @Test
    public void checkResultFromFullName(){
        GeoCoderJacksonParser gc = new GeoCoderJacksonParser();
        LatLong ll = gc.parse("noisy le grand");
        Assert.assertTrue(ll.getLatitude() >=48 && ll.getLatitude() <=49);
        Assert.assertTrue(ll.getLongitude()>=2 && ll.getLongitude()<3);
    }
}
