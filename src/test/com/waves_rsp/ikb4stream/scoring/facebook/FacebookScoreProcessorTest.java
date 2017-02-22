package com.waves_rsp.ikb4stream.scoring.facebook;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import org.junit.Test;
import twitter4j.JSONException;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class FacebookScoreProcessorTest {
    private final FacebookScoreProcessor tsp = new FacebookScoreProcessor();
    private final Date date = Calendar.getInstance().getTime();
    private final String source = "Facebook";
    private final LatLong latlong = new LatLong(2, 3);


    @Test(expected = NullPointerException.class)
    public void nullProcessScore() {
        tsp.processScore(null);
    }

    @Test
    public void calculatedScoreForASentence() throws JSONException {
        String description = "Roger, il y a une fuite d'eau à Paris #eau";
        Event event = new Event(latlong, date, date, description, source);
        assertEquals(12, tsp.processScore(event).getScore());
    }

    @Test
    public void calculatedScoreWithoutKeyWord() throws JSONException {
        String description = "Roger, n'a rien vu";
        Event event = new Event(latlong, date, date, description, source);
        assertEquals(0, tsp.processScore(event).getScore());
    }

    @Test
    public void calculatedScoreGreaterThanOneHundred() throws JSONException {
        String description = "J'ai dû fuir la maison car il y a eu une fuite. A cause de la canicule, il fait super chaud, "
                + "j'ai l'impression d'être jeté dans le feu. Pour m'aider le bombardier a récupéré de l'eau depuis le "
                + " le bassin. On ne peut plus faire de baignade. D'ailleurs mes fleurs sont mortes car on a dû se "
                + " se passer de l'arrosage du jardin. De plus en plus de personnes commencent à fuir vers les pays chauds"
                + " même si les canalisations ne sont pas encore en place.";
        Event event = new Event(latlong, date, date, description, source);
        assertEquals(56, tsp.processScore(event).getScore());
    }

}
