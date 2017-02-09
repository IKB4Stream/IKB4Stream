package com.waves_rsp.ikb4stream.producer.score;

import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.producer.score.sample.NLP;
import com.waves_rsp.ikb4stream.producer.score.sample.RulesReader;

import java.util.Iterator;
import java.util.Map;


public class TwitterScoreProcessor implements IScoreProcessor {
    private static final String FILENAME = "rules.json";

    @Override
    public Event processScore(Event event) {
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
        return new Event(event.getLocation(), event.getStart(), event.getEnd(), tweet, score, "twitter");
    }
}
