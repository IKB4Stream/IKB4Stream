package com.waves_rsp.ikb4stream.scoring.owm;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OWMScoreProcessorTest {
    private final OWMScoreProcessor tsp = new OWMScoreProcessor();
    private final String source = "OpenWeatherMap";

    @Test
    public void testCreateOWMScoreProcessor() {
        new OWMScoreProcessor();
    }

    @Test
    public void testNotNullSources() {
        assertNotNull(tsp.getSources());
    }

    @Test
    public void testSourceExist() {
        assertEquals(true, tsp.getSources().contains(source));
    }

    @Test(expected = NullPointerException.class)
    public void nullProcessScore() {
        tsp.processScore(null);
    }

    @Test
    public void testMinValues(){
        assertEquals(0, tsp.verifyMaxScore((byte) -50));
    }

    @Test
    public void testMaxValues(){
        assertEquals(100, tsp.verifyMaxScore((byte) 102));
    }

    @Test
    public void testRightValues(){
        assertEquals(50, tsp.verifyMaxScore((byte) 50));
    }
}
