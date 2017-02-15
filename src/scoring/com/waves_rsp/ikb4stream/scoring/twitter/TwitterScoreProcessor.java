package com.waves_rsp.ikb4stream.scoring.twitter;

import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.util.NLP;
import com.waves_rsp.ikb4stream.core.util.RulesReader;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;


public class TwitterScoreProcessor implements IScoreProcessor {
    private static final String FILENAME = "rules.json";
    private static byte MAX_SCORE = 100;
    private static int COEFF_HASHTAG = 2;
    private static int COEFF_VERIFY_ACCOUNT = 2;


    /**
     * Check that the score can't overtake @MAX_SCORE
     *
     * @param score calculated by NLP processing
     * @return score (max = @MAX_SCORE)
     */
    private byte verifyMaxScore(byte score) {
        if (score > MAX_SCORE) {
            return MAX_SCORE;
        }
        return score;
    }

    private boolean isHashtag(String word) {
        return word.startsWith("#");
    }


    /**
     * Process score of an event from Twitter
     *
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
                //if tweetWord is a hashtag
                score += rulesMap.get(tweetWord.getKey()) * COEFF_HASHTAG;
            } else {
                score += rulesMap.get(tweetWord.getKey());
            }
        }


        //Score x COEFF_VERIFY_ACCOUNT if the twitter is certified
        //TODO
        return new Event(event.getLocation(), event.getStart(), event.getEnd(), tweet, verifyMaxScore(score), event.getSource());
    }
}

