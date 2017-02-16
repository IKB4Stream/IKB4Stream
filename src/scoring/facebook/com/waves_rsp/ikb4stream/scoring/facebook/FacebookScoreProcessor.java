package com.waves_rsp.ikb4stream.scoring.facebook;

import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.core.util.OpenNLP;
import com.waves_rsp.ikb4stream.core.util.RulesReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class FacebookScoreProcessor implements IScoreProcessor {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(FacebookScoreProcessor.class, "resources/scoreprocessor/facebook/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookScoreProcessor.class);
    private final String ruleFilename;

    public FacebookScoreProcessor() {
        try {
            ruleFilename = PROPERTIES_MANAGER.getProperty("facebook.rules.file");
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
        String content = event.getDescription();
        List<String> fbList = OpenNLP.applyNLPlemma(content);
        Map<String, Integer> rulesMap = RulesReader.parseJSONRules(ruleFilename);
        byte score = 0;

        for (String word : fbList) {
            if (rulesMap.containsKey(word)) {
                score += rulesMap.get(word);
            }
        }

        if (score > 100) {
            score = 100;
        }
        return new Event(event.getLocation(), event.getStart(), event.getEnd(), content, score, event.getSource());
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