package com.waves_rsp.ikb4stream.scoring.owm;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


public class OWMScoreProcessorTest {
    private final OWMScoreProcessor tsp = new OWMScoreProcessor();
    private final String source = "OpenWeatherMap";


    /*@Test(expected = NullPointerException.class)
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
    }*/
}
