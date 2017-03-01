/*
 * Copyright (C) 2017 ikb4stream team
 * ikb4stream is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * ikb4stream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

package com.waves_rsp.ikb4stream.scoring.twitter;

import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.core.util.RulesReader;
import com.waves_rsp.ikb4stream.core.util.nlp.OpenNLP;
import org.slf4j.LoggerFactory;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import java.util.*;

/**
 * {@link IScoreProcessor} will be applied to Twitter sources
 * @author ikb4stream
 * @version 1.0
 * @see IScoreProcessor
 */
public class TwitterScoreProcessor implements IScoreProcessor {
    /**
     * Properties of this module
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class, String)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(TwitterScoreProcessor.class, "resources/scoreprocessor/twitter/config.properties");
    /**
     * Logger used to log all information in this module
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TwitterScoreProcessor.class);
    /**
     * Object to add metrics from this class
     * @see MetricsLogger#log(String, long)
     * @see MetricsLogger#getMetricsLogger()
     */
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    /**
     * Single instance per thread of {@link OpenNLP}
     * @see TwitterScoreProcessor#processScore(Event)
     */
    private final OpenNLP openNLP = OpenNLP.getOpenNLP(Thread.currentThread());
    /**
     * Max score to an {@link Event}
     * @see TwitterScoreProcessor#processScore(Event)
     */
    private static final byte MAX_SCORE = Event.getScoreMax();
    /**
     *
     */
    private static final int COEFF_VERIFY_ACCOUNT = 2;
    /**
     *
     */
    private final Map<String, Integer> rulesMap;
    /**
     *
     */
    private static final int COEFF_HASHTAG = 2;

    /**
     * Default constructor to initialize {@link TwitterScoreProcessor#rulesMap} with a {@link PropertiesManager}
     * @see TwitterScoreProcessor#rulesMap
     * @see TwitterScoreProcessor#PROPERTIES_MANAGER
     */
    public TwitterScoreProcessor() {
        try {
            String filename = PROPERTIES_MANAGER.getProperty("twitter.rules.file");
            rulesMap = RulesReader.parseJSONRules(filename);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * Check that the score can't overtake {@link TwitterScoreProcessor#MAX_SCORE}
     * @param score calculated by OpenNLP processing
     * @return score (max = {@link TwitterScoreProcessor#MAX_SCORE})
     * @see TwitterScoreProcessor#MAX_SCORE
     */
    private byte verifyMaxScore(byte score) {
        if (score > MAX_SCORE) {
            return MAX_SCORE;
        }
        return score;
    }

    /**
     * Check if the word is a hashtag
     * @param word to analyze
     * @return true if the word begin with '#'
     */
    private boolean isHashtag(String word) {
        return word.startsWith("#");
    }

    /**
     * Parse a JSONObject from a tweet and check if the twitter account is certified
     * @param json JSONObject to parse
     * @return true if the twitter account is certified
     * @throws JSONException if json is an invalid tweet
     * @throws NullPointerException if json is null
     */
    private boolean isCertified(JSONObject json) throws JSONException {
        Objects.requireNonNull(json);
        String isCertified = json.getString("user_certified");
        isCertified = isCertified.substring(1, isCertified.length() - 1);
        return "true".equalsIgnoreCase(isCertified);
    }

    /**
     * Parse a JSONObject from a tweet and extract the tweet description
     * @param json JSONObject to parse
     * @return the description of a tweet
     * @throws JSONException if json is an invalid json
     * @throws NullPointerException if json is null
     */
    private String getParseDescription(JSONObject json) throws JSONException {
        Objects.requireNonNull(json);
        return json.getString("description");
    }


    /**
     * Process score of an event from {@link com.waves_rsp.ikb4stream.datasource.twitter.TwitterProducerConnector TwitterProducerConnector}
     * @param event an event without score
     * @return Event with a score after OpenNLP processing
     * @throws NullPointerException if event is null
     * @throws IllegalArgumentException if event is invalid
     * @see TwitterScoreProcessor#openNLP
     * @see TwitterScoreProcessor#COEFF_HASHTAG
     */
    @Override
    public Event processScore(Event event) {
        Objects.requireNonNull(event);
        long start = System.currentTimeMillis();
        String tweet;
        byte score = 0;
        try {
            JSONObject jsonTweet = new JSONObject(event.getDescription());
            tweet = getParseDescription(jsonTweet);
            List<String> tweetMap = openNLP.applyNLPlemma(tweet);
            score = scoreWords(score, tweetMap);
            //Score x COEFF_VERIFY_ACCOUNT if the twitter is certified
            if (isCertified(jsonTweet)) {
                score *= COEFF_VERIFY_ACCOUNT;
            }
        } catch (JSONException e) {
            LOGGER.error("Wrong JsonObject from Twitter Connector\n" + e.getMessage());
            throw new IllegalArgumentException("Wrong description of event");
        }
        long time = System.currentTimeMillis() - start;
        METRICS_LOGGER.log("time_scoring_" + event.getSource(), time);
        return new Event(event.getLocation(), event.getStart(), event.getEnd(), tweet, verifyMaxScore(score), event.getSource());
    }

    /**
     * Get all sources that {@link IScoreProcessor} will be applied
     * @return List of sources accepted
     * @see TwitterScoreProcessor#PROPERTIES_MANAGER
     */
    @Override
    public List<String> getSources() {
        List<String> sources = new ArrayList<>();
        try {
            String allSources = PROPERTIES_MANAGER.getProperty("twitter.scoring.sources");
            sources.addAll(Arrays.asList(allSources.split(",")));
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
        }
        return sources;
    }

    /**
     * Score a tweet depending {@link TwitterScoreProcessor#rulesMap}
     * @param score Actual score of {@link Event}
     * @param tweetMap List of word of tweet
     * @return New score of {@link Event}
     * @throws NullPointerException if tweetMap is null
     * @see TwitterScoreProcessor#rulesMap
     */
    private byte scoreWords(byte score, List<String> tweetMap) {
        Objects.requireNonNull(tweetMap);
        byte scoreTmp = score;
        for (String word : tweetMap) {
            boolean isHashtag = isHashtag(word);
            if (isHashtag(word)) {
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

