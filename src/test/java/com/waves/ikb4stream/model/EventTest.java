package com.waves.ikb4stream.model;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

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

        Event event = new Event(latLong, start, end, "WaterPony", (byte) -1);
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

        Event event = new Event(latLong, start, end, "Pool party", (byte) 101);
    }

    @Test(expected = IllegalStateException.class)
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

        Event event = new Event(latLong, start, end, "Pool party");
        event.getScore();
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

        Event event = new Event(latLong, start, end, "WaterPony", (byte) 10);
        assert (event.getScore() == (byte) 10);
    }

}