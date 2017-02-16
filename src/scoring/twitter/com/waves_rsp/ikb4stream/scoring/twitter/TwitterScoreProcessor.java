package com.waves_rsp.ikb4stream.scoring.twitter;

import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.core.util.OpenNLP;
import com.waves_rsp.ikb4stream.core.util.RulesReader;
import org.slf4j.LoggerFactory;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import java.util.*;


public class TwitterScoreProcessor implements IScoreProcessor {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(TwitterScoreProcessor.class, "resources/scoreprocessor/twitter/config.properties");
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TwitterScoreProcessor.class);
    private final String filename;
    private static final byte MAX_SCORE = 100;
    private static final int COEFF_HASHTAG = 2;
    private static final int COEFF_VERIFY_ACCOUNT = 2;


    /**
     * Override default constructor
     */
    public TwitterScoreProcessor() {

        try {
            filename = PROPERTIES_MANAGER.getProperty("twitter.rules.file");
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * Check that the score can't overtake @MAX_SCORE
     *
     * @param score calculated by OpenNLP processing
     * @return score (max = @MAX_SCORE)
     */
    private byte verifyMaxScore(byte score) {
        if (score > MAX_SCORE) {
            return MAX_SCORE;
        }
        return score;
    }

    /**
     * Check if the word is a hashtag
     *
     * @param word to analyze
     * @return true if the word begin with '#'
     */
    private boolean isHashtag(String word) {
        return word.startsWith("#");
    }

    /**
     * Parse a JSONObject from a tweet and check if the twitter account is certified
     *
     * @param json JSONObject to parse
     * @return true if the twitter account is certified
     * @throws JSONException
     */
    private boolean isCertified(JSONObject json) throws JSONException {
        String isCertified = json.getString("user_certified");
        isCertified = isCertified.substring(1, isCertified.length() - 1);
        return "true".equalsIgnoreCase(isCertified);
    }

    /**
     * Parse a JSONObject from a tweet and extract the tweet description
     *
     * @param json JSONObject to parse
     * @return the description of a tweet
     * @throws JSONException
     */
    private String getParseDescription(JSONObject json) throws JSONException {
        return json.getString("description");
    }


    /**
     * Process score of an event from Twitter
     *
     * @param event an event without score
     * @return Event with a score after OpenNLP processing
     * @throws NullPointerException if {@param event} is null
     * @throws IllegalArgumentException if {@param event} is invalid
     */
    @Override
    public Event processScore(Event event) {
        Objects.requireNonNull(event);
        String tweet = "";
        byte score = 0;

        try {
            JSONObject jsonTweet = new JSONObject(event.getDescription());
            tweet = getParseDescription(jsonTweet);
            List<String> tweetMap = OpenNLP.applyNLPlemma(tweet);
            Map<String, Integer> rulesMap = RulesReader.parseJSONRules(filename);

            score = scoreWords(score, tweetMap, rulesMap);

            //Score x COEFF_VERIFY_ACCOUNT if the twitter is certified
            if (isCertified(jsonTweet)) {
                score *= COEFF_VERIFY_ACCOUNT;
            }
        } catch (JSONException e) {
            LOGGER.error("Wrong JsonObject from Twitter Connector\n" + e.getMessage());
            throw new IllegalArgumentException("Wrong description of event");
        }
        return new Event(event.getLocation(), event.getStart(), event.getEnd(), tweet, verifyMaxScore(score), event.getSource());
    }

    @Override
    public List<String> getSources() {
        List<String> sources = new ArrayList<>();
        try {
            String allSources = PROPERTIES_MANAGER.getProperty("twitter.scoring.sources");
            sources.addAll(Arrays.asList(allSources.split(",")));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(e.getMessage());
        }
        return sources;
    }

    private byte scoreWords(byte score, List<String> tweetMap, Map<String, Integer> rulesMap) {
        byte scoreTmp = score;
        for(String word : tweetMap){
            boolean isHashtag = isHashtag(word);
            if(isHashtag(word)){
                word = word.substring(1);
            }
            if (rulesMap.containsKey(word)) {
                if (isHashtag) {
                    //if tweetWord is a hashtag
                    scoreTmp += rulesMap.get(word) * COEFF_HASHTAG;
                } else {
                    scoreTmp += rulesMap.get(word);
                }
            }
        }
        return scoreTmp;
    }
}

