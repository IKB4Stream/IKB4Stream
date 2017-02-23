package com.waves_rsp.ikb4stream.scoring.owm;

import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import org.junit.Test;
import twitter4j.JSONException;

import java.util.Calendar;
import java.util.Date;

public class OWMScoreProcessorTest {
    private final OWMScoreProcessor tsp = new OWMScoreProcessor();
    private final LatLong latlong = new LatLong(2, 3);
    private final Date date = Calendar.getInstance().getTime();
    private final String source = "OpenWeatherMap";

    @Test
    public void testCreateOWMScoreProcessor() {
        new OWMScoreProcessor();
    }

    @Test
    public void testNotNullSources() {
        assert(tsp.getSources() != null);
    }

    @Test
    public void testSourceExist() {
        assert (tsp.getSources().contains(source));
    }

    @Test(expected = NullPointerException.class)
    public void nullProcessScore() {
        tsp.processScore(null);
    }

    @Test
    public void testMinValues(){
        assert (Event.getScoreMin() == tsp.verifyMaxScore((byte) -50));
    }

    @Test
    public void testMaxValues(){
        assert (Event.getScoreMax() == tsp.verifyMaxScore((byte) 102));
    }

    @Test
    public void testRightValues(){
        assert (50 == tsp.verifyMaxScore((byte) 50));
    }

    @Test
    public void checkScore() throws JSONException {
        String description = "{\"main\": {\"temp\": 48}, \"weather\": [{\"main\": \"rain\", \"description\": \"Il pleut\"}]}";
        Event event = new Event(latlong, date, date, description, source);
        Event clone = tsp.processScore(event);
        assert (clone.getScore() != -1);
    }
}
