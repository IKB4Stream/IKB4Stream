package com.waves_rsp.ikb4stream.core.util;

import com.waves_rsp.ikb4stream.core.model.LatLong;
import org.junit.Assert;
import org.junit.Test;

public class GeocoderTest {



    @Test(expected = NullPointerException.class)
    public void testNullAddress() {
        Geocoder.geocode(null);
    }


    @Test
    public void testResultFromFullName(){
        Geocoder gc = Geocoder.geocode("Noisy le grand");
        LatLong ll = gc.getLatLong();
        LatLong[] bbox = gc.getBbox();
        Assert.assertTrue(ll.getLatitude() >=48 && ll.getLatitude() <=49);
        Assert.assertTrue(bbox[0].getLatitude() >=48 && bbox[0].getLatitude() <=49);
        Assert.assertTrue(ll.getLongitude()>=2 && ll.getLongitude()<3);
        Assert.assertTrue(bbox[0].getLongitude()>=2 && bbox[0].getLongitude()<3);
    }
}
