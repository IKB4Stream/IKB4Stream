package com.waves_rsp.ikb4stream.producer.score;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class ScoreProcessorManagerTest {

    @Ignore
    @Test
    public void testCreateScoreProcessorManager() {
        new ScoreProcessorManager();
    }

    @Ignore
    @Test
    public void testInstanciate() throws IOException {
        ScoreProcessorManager scoreProcessorManager = new ScoreProcessorManager();
        scoreProcessorManager.instanciate();
    }

    @Ignore
    @Test(expected = NullPointerException.class)
    public void testProcessScore() {
        ScoreProcessorManager scoreProcessorManager = new ScoreProcessorManager();
        scoreProcessorManager.processScore(null);
    }
}
