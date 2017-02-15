package com.waves_rsp.ikb4stream.producer.score;

import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.producer.score.sample.NLP;
import com.waves_rsp.ikb4stream.producer.score.sample.RulesReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;


public class FacebookScoreProcessor implements IScoreProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreProcessorManager.class);
    private final String ruleFilename;

    public FacebookScoreProcessor() {
        PropertiesManager propertiesManager = PropertiesManager.getInstance(FacebookScoreProcessor.class, "resources/config.properties");
        try {
            ruleFilename = propertiesManager.getProperty("facebook.rules.file");
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * Process score of an event from Facebook
     * @param event an event without score
     * @return Event with a score after NLP processing
     * @throws NullPointerException if {@param event} is null
     */
    @Override
    public Event processScore(Event event) {
        Objects.requireNonNull(event);
        String content = event.getDescription();
        Map<String, String> fbMap = NLP.applyNLPtoFacebook(content);
        Map<String, Integer> rulesMap = RulesReader.parseJSONRules(ruleFilename);
        byte score = 0;

        Iterator fbWords = fbMap.entrySet().iterator();
        while (fbWords.hasNext()) {
            Map.Entry fbWord = (Map.Entry) fbWords.next();
            if (rulesMap.containsKey(fbWord.getKey())) {
                score += rulesMap.get(fbWord.getKey());
            }
        }
        if (score > 100) {
            score = 100;
        }
        return new Event(event.getLocation(), event.getStart(), event.getEnd(), content, score, event.getSource());
    }
}
