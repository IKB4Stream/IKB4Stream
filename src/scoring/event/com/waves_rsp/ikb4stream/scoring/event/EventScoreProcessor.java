package com.waves_rsp.ikb4stream.scoring.event;

import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.core.util.RulesReader;
import com.waves_rsp.ikb4stream.core.util.nlp.OpenNLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class EventScoreProcessor implements IScoreProcessor {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(EventScoreProcessor.class, "resources/scoreprocessor/event/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(EventScoreProcessor.class);
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    private final OpenNLP openNLP = OpenNLP.getOpenNLP(Thread.currentThread());
    private final static byte MAX = Event.getScoreMax();
    private final Map<String, Integer> rulesMap;

    public EventScoreProcessor() {
        try {
            String ruleFilename = PROPERTIES_MANAGER.getProperty("event.rules.file");
            rulesMap = RulesReader.parseJSONRules(ruleFilename);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * Process score of an event from an Event
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
        List<String> eventList = openNLP.applyNLPlemma(content);
        byte score = 0;
        for (String word : eventList) {
            if (rulesMap.containsKey(word)) {
                score += rulesMap.get(word);
            }
        }
        if (score > MAX) {
            score = MAX;
        }
        long end = System.currentTimeMillis();
        METRICS_LOGGER.log("time_scoring_" + event.getSource(), (end-start));
        return new Event(event.getLocation(), event.getStart(), event.getEnd(), event.getDescription(), score, event.getSource());
    }

    @Override
    public List<String> getSources() {
        List<String> sources = new ArrayList<>();
        try {
            String allSources = PROPERTIES_MANAGER.getProperty("event.scoring.sources");
            sources.addAll(Arrays.asList(allSources.split(",")));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(e.getMessage());
        }
        return sources;
    }
}
