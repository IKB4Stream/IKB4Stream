package com.waves_rsp.ikb4stream.core.model;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class EventTest {

    @Test(expected = IllegalArgumentException.class)
    public void negativeScore() throws Exception {
        LatLong latLong = new LatLong(1, 1);
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, 2015);
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.DATE, 15);
        Date start = calendar.getTime();

        calendar.set(Calendar.YEAR, 2015);
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.DATE, 17);
        Date end = calendar.getTime();

        Event event = new Event(latLong, start, end, "WaterPony", (byte) -1, "twitter");
    }

    @Test(expected = IllegalArgumentException.class)
    public void moreThan100Score() throws Exception {
        LatLong latLong = new LatLong(1, 1);
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, 2015);
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.DATE, 15);
        Date start = calendar.getTime();

        calendar.set(Calendar.YEAR, 2015);
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.DATE, 17);
        Date end = calendar.getTime();

        new Event(latLong, start, end, "Pool party", (byte) 101, "facebook");
    }

    @Test
    public void noScore() throws Exception {
        LatLong latLong = new LatLong(1, 1);
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, 2015);
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.DATE, 15);
        Date start = calendar.getTime();

        calendar.set(Calendar.YEAR, 2015);
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.DATE, 17);
        Date end = calendar.getTime();

        Event event = new Event(latLong, start, end, "Pool party", "eventful");
        assert (event.getScore() == -1);
    }

    @Test
    public void normalScore() throws Exception {
        LatLong latLong = new LatLong(1, 1);
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, 2015);
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.DATE, 15);
        Date start = calendar.getTime();

        calendar.set(Calendar.YEAR, 2015);
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.DATE, 17);
        Date end = calendar.getTime();

        Event event = new Event(latLong, start, end, "WaterPony", (byte) 10, "twitter");
        assert (event.getScore() == (byte) 10);
    }

}