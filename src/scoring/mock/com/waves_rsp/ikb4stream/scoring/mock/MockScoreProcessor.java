package com.waves_rsp.ikb4stream.scoring.mock;

import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class MockScoreProcessor implements IScoreProcessor {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(MockScoreProcessor.class, "resources/scoreprocessor/mock/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(MockScoreProcessor.class);

    public MockScoreProcessor() {
        // Do nothing else
    }

    @Override
    public Event processScore(Event event) {
        Random rand = new Random();
        byte nombreAleatoire = (byte)rand.nextInt(100 + 1);
        LOGGER.info("Score aléatoire: " + nombreAleatoire);
        return new Event(event.getLocation(), event.getStart(), event.getEnd(), event.getDescription(), nombreAleatoire, event.getSource());
    }

    @Override
    public List<String> getSources() {
        List<String> sources = new ArrayList<>();
        try {
            String allSources = PROPERTIES_MANAGER.getProperty("mock.scoring.sources");
            sources.addAll(Arrays.asList(allSources.split(",")));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(e.getMessage());
        }
        return sources;
    }
}

