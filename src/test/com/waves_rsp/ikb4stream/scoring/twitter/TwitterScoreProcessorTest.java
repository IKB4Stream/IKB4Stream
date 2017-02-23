package com.waves_rsp.ikb4stream.scoring.twitter;

import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import org.junit.Test;
import twitter4j.JSONException;

import java.util.Calendar;
import java.util.Date;

public class TwitterScoreProcessorTest {
    private final TwitterScoreProcessor tsp = new TwitterScoreProcessor();
    private final Date date = Calendar.getInstance().getTime();
    private final String source = "Twitter";
    private final LatLong latlong = new LatLong(2, 3);

    @Test(expected = NullPointerException.class)
    public void nullProcessScore() {
        tsp.processScore(null);
    }

    @Test
    public void testGetSources(){
        assert (tsp.getSources() != null);
    }

    @Test
    public void testSourceExist() {
        assert (tsp.getSources().contains(source));
    }

    @Test
    public void calculScore() throws JSONException {
        String description = "{\"description\": \"Roger, il y a une fuite d'eau à Paris #eau\", \"user_certified\": true}";
        Event event = new Event(latlong, date, date, description, source);
        assert (tsp.processScore(event).getScore() != -1);
    }
}
