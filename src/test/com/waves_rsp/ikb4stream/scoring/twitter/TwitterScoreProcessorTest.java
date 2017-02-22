package com.waves_rsp.ikb4stream.scoring.twitter;

import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import org.junit.Ignore;
import org.junit.Test;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TwitterScoreProcessorTest {
    private final TwitterScoreProcessor tsp = new TwitterScoreProcessor();
    private final Date date = Calendar.getInstance().getTime();
    private final String source = "Twitter";
    private final LatLong latlong = new LatLong(2, 3);

    @Ignore
    @Test(expected = NullPointerException.class)
    public void nullProcessScore() {
        tsp.processScore(null);
    }

    @Ignore
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

    @Ignore
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

    @Ignore
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

    @Ignore
    @Test
    public void calculatedScoreGreaterThanOneHundred() throws JSONException {
        String description = "J'ai dû fuir la maison car il y a eu une fuite. A cause de la canicule, il fait super chaud, "
                + "j'ai l'impression d'être jeté dans le feu. Pour m'aider le bombardier a récupéré de l'eau depuis le "
                + " le bassin. On ne peut plus faire de baignade. D'ailleurs mes fleurs sont mortes car on a dû se "
                + " se passer de l'arrosage du jardin. De plus en plus de personnes commencent à fuir vers les pays chauds"
                + " même si les canalisations ne sont pas encore en place.";
        boolean isVerified = true;
        JSONObject jsonObject = new JSONObject();
        jsonObject.append("description", description);
        jsonObject.append("user_certified", isVerified);
        Event event = new Event(latlong, date, date, jsonObject.toString(), source);
        assertEquals(100, tsp.processScore(event).getScore());
    }

}
