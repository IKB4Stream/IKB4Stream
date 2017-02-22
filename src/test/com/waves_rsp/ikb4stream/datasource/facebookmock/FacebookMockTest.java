package com.waves_rsp.ikb4stream.datasource.facebookmock;

import org.junit.Test;

public class FacebookMockTest {

    @Test(expected = NullPointerException.class)
    public void loadNullTest(){
        FacebookMock fm = new FacebookMock();
        fm.load(null);
    }

    @Test(expected = NullPointerException.class)
    public void getDateFromJsonNullTest() {
        FacebookMock fm = new FacebookMock();
        fm.getDateFromJson(null);
    }
}
