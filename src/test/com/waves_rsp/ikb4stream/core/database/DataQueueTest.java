package com.waves_rsp.ikb4stream.core.database;


import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class DataQueueTest {

    @Test(expected = NullPointerException.class)
    public void nullEvent() throws Exception {
        DataQueue queue = new DataQueue();
        queue.push(null);
    }

    @Test
    public void normalEvent() throws Exception {
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

        DataQueue queue = new DataQueue();
        queue.push(event);
        assert (queue.pop() == event);
    }
}
