package com.waves_rsp.ikb4stream.scoring.openagenda;

import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import org.junit.Test;
import twitter4j.JSONException;

import java.util.Calendar;
import java.util.Date;

public class OpenAgendaScoreProcessorTest {
    private final OpenAgendaScoreProcessor sp = new OpenAgendaScoreProcessor();
    private final LatLong latlong = new LatLong(2, 3);
    private final Date date = Calendar.getInstance().getTime();

    @Test(expected = NullPointerException.class)
    public void nullProcessScore() {
        sp.processScore(null);
    }

    @Test
    public void testGetSources(){
        assert (sp.getSources() != null);
    }

    @Test
    public void testSourceExist() {
        assert (sp.getSources().contains("OpenAgenda"));
    }

    @Test
    public void checkScore() throws JSONException {
        String description = "{\"title\": \"Titre\", \"description\": \"Roger:, il y a une fuite d'eau à Paris #eau\"}";
        Event event = new Event(latlong, date, date, description, "OpenAgenda");
        Event clone = sp.processScore(event);
        assert (clone.getScore() != -1);
    }
}
