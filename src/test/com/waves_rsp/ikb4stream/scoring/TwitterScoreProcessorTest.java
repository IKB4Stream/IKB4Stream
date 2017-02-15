package com.waves_rsp.ikb4stream.scoring;

import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import org.junit.Test;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import java.util.Calendar;
import java.util.Date;

import static junit.framework.Assert.assertEquals;

public class TwitterScoreProcessorTest {
    private final TwitterScoreProcessor tsp = new TwitterScoreProcessor();
    private final Date date = Calendar.getInstance().getTime();
    private final String source = "Twitter";
    private final LatLong latlong = new LatLong(2, 3);

    /*
        @Test(expected = JSONException.class)
        public void wrongEventToTwitterScoreProcessor() {
            String wrongDesc = "not good enough";
            Event event = new Event(latlong, date, date, wrongDesc, source);
            tsp.processScore(event);
        }
    */
    @Test(expected = NullPointerException.class)
    public void nullProcessScore() {
        tsp.processScore(null);
    }

    @Test
    public void calculatedScoreWithCertifiedTweet() throws JSONException {
        String description = "Roger, il y a une fuite d'eau à Paris #eau";
        boolean isVerified = true;
        JSONObject jsonObject = new JSONObject();
        jsonObject.append("description", description);
        jsonObject.append("user_certified", isVerified);
        Event event = new Event(latlong, date, date, jsonObject.toString(), source);
        assertEquals(32, tsp.processScore(event).getScore());
    }

    @Test
    public void calculatedScoreWithUncertifiedTweet() throws JSONException {
        String description = "Roger, il y a une fuite d'eau à Paris #eau";
        boolean isVerified = false;
        JSONObject jsonObject = new JSONObject();
        jsonObject.append("description", description);
        jsonObject.append("user_certified", isVerified);
        Event event = new Event(latlong, date, date, jsonObject.toString(), source);
        assertEquals(16, tsp.processScore(event).getScore());
    }

    @Test
    public void calculatedScoreWithoutKeyWord() throws JSONException {
        String description = "Roger, n'a rien vu";
        boolean isVerified = false;
        JSONObject jsonObject = new JSONObject();
        jsonObject.append("description", description);
        jsonObject.append("user_certified", isVerified);
        Event event = new Event(latlong, date, date, jsonObject.toString(), source);
        assertEquals(0, tsp.processScore(event).getScore());
    }

}
