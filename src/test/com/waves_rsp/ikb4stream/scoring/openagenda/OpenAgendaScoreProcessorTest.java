package com.waves_rsp.ikb4stream.scoring.openagenda;

import org.junit.Assert;
import org.junit.Test;

public class OpenAgendaScoreProcessorTest {
    private final OpenAgendaScoreProcessor sp = new OpenAgendaScoreProcessor();

    @Test(expected = NullPointerException.class)
    public void nullProcessScore() {
        sp.processScore(null);
    }

    @Test
    public void testGetSources(){
        Assert.assertTrue(sp.getSources().size() >= 1);
    }

}
