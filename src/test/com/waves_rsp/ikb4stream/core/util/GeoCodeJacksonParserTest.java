package com.waves_rsp.ikb4stream.core.util;

import com.waves_rsp.ikb4stream.core.model.LatLong;
import org.junit.Assert;
import org.junit.Test;

public class GeoCodeJacksonParserTest {
    @Test
    public void testCreate() {
        new GeoCoderJacksonParser();
    }


    @Test(expected = NullPointerException.class)
    public void testNullAddress() {
        new GeoCoderJacksonParser().parse(null);
    }


    @Test
    public void testResultFromFullName(){
        GeoCoderJacksonParser gc = new GeoCoderJacksonParser();
        LatLong ll = gc.parse("noisy le grand");
        Assert.assertTrue(ll.getLatitude() >=48 && ll.getLatitude() <=49);
        Assert.assertTrue(ll.getLongitude()>=2 && ll.getLongitude()<3);
    }
}
