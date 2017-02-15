package com.waves_rsp.ikb4stream.scoring.twitter;

import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.util.OpenNLP;
import com.waves_rsp.ikb4stream.core.util.RulesReader;
import org.slf4j.LoggerFactory;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import java.io.IOException;
import java.util.*;


public class TwitterScoreProcessor implements IScoreProcessor {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TwitterScoreProcessor.class);
    private static final String FILENAME = "rules.json";
    private static byte MAX_SCORE = 100;
    private static int COEFF_HASHTAG = 2;
    private static int COEFF_VERIFY_ACCOUNT = 2;


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
        return isCertified.equals("true");
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
     */
    @Override
    public Event processScore(Event event) {
        Objects.requireNonNull(event);
        String tweet = "";
        byte score = 0;
        boolean isHashtag;

        try {
            JSONObject jsonTweet = new JSONObject(event.getDescription());
            tweet = getParseDescription(jsonTweet);
            System.out.println("tweet : " + tweet); //TODO
            List<String> tweetMap = OpenNLP.applyNLPlemma(tweet);
            Map<String, Integer> rulesMap = RulesReader.parseJSONRules(FILENAME);

            for(String word : tweetMap){
                System.out.print("Word : " + word); //TODO
                if(isHashtag =isHashtag(word)){
                    word = word.substring(1);
                }
                if (rulesMap.containsKey(word)) {
                    System.out.print(" - score : ");//TODO
                    if (isHashtag) {
                        //if tweetWord is a hashtag
                        score += rulesMap.get(word) * COEFF_HASHTAG;
                        System.out.print(" # " + rulesMap.get(word)*COEFF_HASHTAG);//TODO
                    } else {
                        score += rulesMap.get(word);
                        System.out.print(rulesMap.get(word));//TODO
                    }
                }
                System.out.println("\n");
            }

            /*
            Iterator tweetWords = tweetMap.entrySet().iterator();
            while (tweetWords.hasNext()) {
                Map.Entry tweetWord = (Map.Entry) tweetWords.next();
                System.out.print("Word : " + tweetWord.getKey()); //TODO
                String word = tweetWord.getKey().toString();
                if(isHashtag =isHashtag(word)){
                    word = word.substring(1);
                }
                if (rulesMap.containsKey(word)) {
                    System.out.print(" - score : ");//TODO
                    if (isHashtag) {
                        //if tweetWord is a hashtag
                        score += rulesMap.get(word) * COEFF_HASHTAG;
                        System.out.print(" # " + rulesMap.get(word)*COEFF_HASHTAG);//TODO
                    } else {
                        score += rulesMap.get(word);
                        System.out.print(rulesMap.get(word));//TODO
                    }
                }
                System.out.println("\n");
            }*/

            //Score x COEFF_VERIFY_ACCOUNT if the twitter is certified
            if (isCertified(jsonTweet)) {
                System.out.println("CERTIFIED");
                score *= COEFF_VERIFY_ACCOUNT;
            }
        } catch (JSONException e) {
            LOGGER.error("Wrong JsonObject from Twitter Connector\n" + e.getMessage());
        }
        return new Event(event.getLocation(), event.getStart(), event.getEnd(), tweet, verifyMaxScore(score), event.getSource());
    }

    public static void main(String[] args) throws IOException {
        String description = "Roger, il y a une fuite d'eau à Paris #eau";
        boolean isVerified = true;

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.append("description", description);

            jsonObject.append("user_certified", isVerified);
            Date date = Calendar.getInstance().getTime();
            Event event = new Event(new LatLong(2, 3), date, date, jsonObject.toString(), "Twitter");

            System.out.println(jsonObject.toString());

            TwitterScoreProcessor tsp = new TwitterScoreProcessor();
            Event e = tsp.processScore(event);
            System.out.println(e.getDescription());
            System.out.println("SCORE : " + e.getScore());
        } catch (JSONException e) {

        }


    }
}

