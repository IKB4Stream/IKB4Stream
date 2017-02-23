package com.waves_rsp.ikb4stream.scoring.event;

import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import org.junit.Test;
import twitter4j.JSONException;

import java.util.Calendar;
import java.util.Date;


public class EventScoreProcessorTest {
    private final LatLong latlong = new LatLong(2, 3);
    private final EventScoreProcessor tsp = new EventScoreProcessor();
    private final Date date = Calendar.getInstance().getTime();

    @Test(expected = NullPointerException.class)
    public void nullProcessScore() {
        tsp.processScore(null);
    }

    @Test
    public void checkScore() throws JSONException {
        String description = "Roger, il y a une fuite d'eau à Paris #eau";
        Event event = new Event(latlong, date, date, description, "Facebook");
        Event clone = tsp.processScore(event);
        assert (clone.getScore() != -1);
    }

    @Test
    public void testSources() {
        assert (tsp.getSources() != null);
    }

    @Test
    public void testSourceExist() {
        assert (tsp.getSources().contains("Facebook"));
    }
}
