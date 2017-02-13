package com.waves_rsp.ikb4stream.producer.score;

import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.producer.score.sample.NLP;
import com.waves_rsp.ikb4stream.producer.score.sample.RulesReader;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;


public class TwitterScoreProcessor implements IScoreProcessor {
    private static final String FILENAME = "rules.json";

    /**
     * Process score of an event from Twitter
     * @param event an event without score
     * @return Event with a score after NLP processing
     * @throws NullPointerException if {@param event} is null
     */
    @Override
    public Event processScore(Event event) {
        Objects.requireNonNull(event);
        String tweet = event.getDescription();
        Map<String, String> tweetMap = NLP.applyNLPtoTweet(tweet);
        Map<String, Integer> rulesMap = RulesReader.parseJSONRules(FILENAME);
        byte score = 0;

        Iterator tweetWords = tweetMap.entrySet().iterator();
        while (tweetWords.hasNext()) {
            Map.Entry tweetWord = (Map.Entry) tweetWords.next();
            if (rulesMap.containsKey(tweetWord.getKey())) {
                score += rulesMap.get(tweetWord.getKey());
            }
        }
        return new Event(event.getLocation(), event.getStart(), event.getEnd(), tweet, score, event.getSource());
    }
}
