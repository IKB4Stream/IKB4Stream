package com.waves_rsp.ikb4stream.scoring.owm;

import org.junit.Test;


public class OWMScoreProcessorTest {
    private final OWMScoreProcessor tsp = new OWMScoreProcessor();
    private final String source = "OpenWeatherMap";


    @Test(expected = IllegalArgumentException.class)
    public void nullProcessScore() {
        tsp.processScore(null);
    }

}
