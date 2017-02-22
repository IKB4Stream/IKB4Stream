package com.waves_rsp.ikb4stream.scoring.facebook;

import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.core.util.RulesReader;
import com.waves_rsp.ikb4stream.core.util.nlp.OpenNLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class FacebookScoreProcessor implements IScoreProcessor {
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(FacebookScoreProcessor.class, "resources/scoreprocessor/facebook/config.properties");
    private final Map<String, Integer> rulesMap;
    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookScoreProcessor.class);

    public FacebookScoreProcessor() {
        try {
            String filename = PROPERTIES_MANAGER.getProperty("facebook.rules.file");
            rulesMap = RulesReader.parseJSONRules(filename);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * Process score of an event from Facebook
     *
     * @param event an event without score
     * @return Event with a score after OpenNLP processing
     * @throws NullPointerException if {@param event} is null
     */
    @Override
    public Event processScore(Event event) {
        Objects.requireNonNull(event);
        long start = System.currentTimeMillis();
        String content = event.getDescription();
        OpenNLP openNLP = OpenNLP.getOpenNLP(Thread.currentThread());
        List<String> fbList = openNLP.applyNLPlemma(content);
        byte score = 0;
        for (String word : fbList) {
            if (rulesMap.containsKey(word)) {
                score += rulesMap.get(word);
            }
        }
        if (score > 100) {
            score = 100;
        }
        long end = System.currentTimeMillis();
        METRICS_LOGGER.log("time_scoring_" + event.getSource(), String.valueOf(end-start));
        return new Event(event.getLocation(), event.getStart(), event.getEnd(), event.getDescription(), score, event.getSource());
    }

    @Override
    public List<String> getSources() {
        List<String> sources = new ArrayList<>();
        try {
            String allSources = PROPERTIES_MANAGER.getProperty("facebook.scoring.sources");
            sources.addAll(Arrays.asList(allSources.split(",")));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(e.getMessage());
        }
        return sources;
    }
}
