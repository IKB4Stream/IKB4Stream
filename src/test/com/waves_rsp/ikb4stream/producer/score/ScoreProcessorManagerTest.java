package com.waves_rsp.ikb4stream.producer.score;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class ScoreProcessorManagerTest {
    @Test
    public void testCreateScoreProcessorManager() {
        new ScoreProcessorManager();
    }

    @Ignore // FIXME: GitLab-CI has some problems with Resource File
    @Test
    public void testInstanciate() throws IOException {
        ScoreProcessorManager scoreProcessorManager = new ScoreProcessorManager();
        scoreProcessorManager.instanciate();
    }

    @Test(expected = NullPointerException.class)
    public void testProcessScore() {
        ScoreProcessorManager scoreProcessorManager = new ScoreProcessorManager();
        scoreProcessorManager.processScore(null);
    }
}
