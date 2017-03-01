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

package com.waves_rsp.ikb4stream.core.util.nlp;

import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.lemmatizer.SimpleLemmatizer;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.util.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author ikb4stream
 * @version 1.0
 */
public class OpenNLP {
    /**
     * Properties of this class
     *
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(OpenNLP.class);
    /**
     * Logger used to log all information in this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenNLP.class);
    /**
     * Store unique instance per Thread of {@link OpenNLP}
     *
     * @see OpenNLP#getOpenNLP(Thread)
     */
    private static final Map<Thread, OpenNLP> INSTANCES = new HashMap<>();
    /**
     * Load lemmatizer model
     *
     * @see OpenNLP#lemmatize(String)
     */
    private final DictionaryLemmatizer lemmatizer;
    /**
     * Use to do sentence detection
     *
     * @see OpenNLP#detectSentences(String)
     */
    private final SentenceDetectorME detector;
    /**
     * Use to apply person name finder
     *
     * @see OpenNLP#findPersonName(String[])
     */
    private final NameFinderME nameFinderPers;
    /**
     * Use to apply organization name finder
     *
     * @see OpenNLP#findOrganizationName(String[])
     */
    private final NameFinderME nameFinderOrg;
    /**
     * Use to apply location name finder
     *
     * @see OpenNLP#findLocationName(String[])
     */
    private final NameFinderME nameFinderLoc;
    /**
     * Use to apply tokenization
     *
     * @see OpenNLP#learnableTokenize(String)
     */
    private final Tokenizer tokenizer;
    /**
     * Use to apply part-of-speech tagger
     *
     * @see OpenNLP#posTagging(String[])
     */
    private final POSTaggerME tagger;

    /**
     * Private constructor to allow only one {@link OpenNLP} for each Thread
     *
     * @throws IllegalStateException if an error occurred from {@link LoaderNLP} or {@link PropertiesManager}
     */
    private OpenNLP() {
        try {
            detector = new SentenceDetectorME(LoaderNLP.getSentenceModel());
            tokenizer = new TokenizerME(LoaderNLP.getTokenizerModel());
            tagger = new POSTaggerME(LoaderNLP.getPosModel());
            nameFinderOrg = new NameFinderME(LoaderNLP.getTokenNameFinderModelOrg());
            nameFinderLoc = new NameFinderME(LoaderNLP.getTokenNameFinderModelLoc());
            nameFinderPers = new NameFinderME(LoaderNLP.getTokenNameFinderModelPers());
            InputStream inputStream = new FileInputStream(PROPERTIES_MANAGER.getProperty("nlp.dictionaries.path"));
            lemmatizer = new SimpleLemmatizer(inputStream);
            inputStream.close();
        } catch (IllegalArgumentException | IOException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    /**
     * Get instance of {@link OpenNLP} for each thread because Apache OpenNLP is not thread safe
     *
     * @param thread Thread needs {@link OpenNLP}
     * @return Instance of {@link OpenNLP}
     * @throws NullPointerException if thread is null
     * @see OpenNLP#INSTANCES
     */
    public static OpenNLP getOpenNLP(Thread thread) {
        Objects.requireNonNull(thread);
        return INSTANCES.computeIfAbsent(thread, t -> new OpenNLP());
    }

    /**
     * Enum the ner options
     */
    public enum nerOptions {
        LOCATION, PERSON, ORGANIZATION
    }

    /**
     * OpenNLP : split a text in sentences
     *
     * @param text to analyze
     * @return an array of sentences
     * @throws NullPointerException if text is null
     * @see OpenNLP#detector
     */
    private String[] detectSentences(String text) {
        Objects.requireNonNull(text);
        return detector.sentDetect(text);
    }

    /**
     * OpenNLP : learnableTokenize. The function tokenize a text
     *
     * @param text to tokenize
     * @return an array of words
     * @throws NullPointerException if text is null
     * @see OpenNLP#tokenizer
     */
    private String[] learnableTokenize(String text) {
        Objects.requireNonNull(text);
        return tokenizer.tokenize(text);
    }

    /**
     * OpenNLP : posTagging affect a tag to each word (V, NC, NP, ADJ...)
     *
     * @param tokens is a tokenize text
     * @return an array of posTag
     * @throws NullPointerException if tokens is null
     * @see OpenNLP#tagger
     */
    private String[] posTagging(String[] tokens) {
        Objects.requireNonNull(tokens);
        return tagger.tag(tokens);
    }

    /**
     * OpenNLP : name entity recognizer function. Detect organizations names.
     *
     * @param tokens are an array of string to analyze
     * @return an array of entity detected as an organization
     * @throws NullPointerException if tokens is null
     * @see OpenNLP#nameFinderOrg
     */
    private Span[] findOrganizationName(String[] tokens) {
        Objects.requireNonNull(tokens);
        return nameFinderOrg.find(tokens);
    }

    /**
     * OpenNLP : name entity recognizer function. Detect locations names.
     *
     * @param tokens are an array of string to analyze
     * @return an array of entity detected as a location
     * @throws NullPointerException if tokens is null
     * @see OpenNLP#nameFinderLoc
     */
    private Span[] findLocationName(String[] tokens) {
        Objects.requireNonNull(tokens);
        return nameFinderLoc.find(tokens);
    }

    /**
     * OpenNLP : name entity recognizer function. Detect persons names.
     *
     * @param tokens are an array of string to analyze
     * @return an array of entity detected as a personnality
     * @throws NullPointerException if tokens is null
     * @see OpenNLP#nameFinderPers
     */
    private Span[] findPersonName(String[] tokens) {
        Objects.requireNonNull(tokens);
        return nameFinderPers.find(tokens);
    }

    /**
     * OpenNLP : lemmatization. The function simplify the step of POStagging for the verbs category.
     *
     * @param text to lemmatize
     * @return Map of each lemmatize word with the POStag associate
     * @throws NullPointerException if text is null
     * @see OpenNLP#lemmatizer
     */
    private Map<String, String> lemmatize(String text) {
        Objects.requireNonNull(text);
        Map<String, String> lemmatizedTokens = new HashMap<>();
        // Split tweet text content in sentences
        String[] sentences = detectSentences(text);
        // For each sentence, tokenize and tag before lemmatizing
        for (String sentence : sentences) {
            // Split each sentence in tokens
            String[] learnableTokens = learnableTokenize(sentence);
            // Get tag for each token
            String[] tags = posTagging(learnableTokens);
            // Get lemmatize form of each token
            for (int i = 0; i < learnableTokens.length; i++) {
                if (tags[i].startsWith("V") && tags[i].length() > 1) {
                    //if the POStag start with V, we just keep the tag V for simplify the lemmatization with the dictionnary
                    tags[i] = "V";
                }
                lemmatizedTokens.put(lemmatizer.lemmatize(learnableTokens[i], tags[i]), tags[i]);
            }
        }
        return lemmatizedTokens;
    }

    /**
     * Apply the OpenNLP Lemmatization with a dictionnary. Keep only words with the verbs and nouns.
     *
     * @param post  is the text to lemmatize
     * @param limit is the limit to have the n first characters
     * @return list of selected words.
     * @throws NullPointerException if post is null
     */
    public List<String> applyNLPlemma(String post, int limit) {
        Objects.requireNonNull(post);
        String tmpPost = post;
        if (tmpPost.length() > limit) {
            tmpPost = post.substring(0, limit);
        }
        Map<String, String> input;
        List<String> output = new ArrayList<>();
        input = lemmatize(tmpPost);
        input.forEach((w, pos) -> {
            if (w.startsWith("#")) {
                output.add(w);
            } else {
                if (pos.startsWith("N") || pos.startsWith("V")) {
                    output.add(w);
                }
            }
        });
        return output;
    }

    /**
     * Apply the OpenNLP Lemmatization with a dictionnary. Keep only words with the verbs and nouns.
     *
     * @param post is the text to lemmatize. We only use the 1250 first characters
     * @return list of selected words.
     * @throws NullPointerException if post is null
     */
    public List<String> applyNLPlemma(String post) {
        Objects.requireNonNull(post);
        return applyNLPlemma(post, 1250);
    }

    /**
     * Apply the ÖpenNLP ner (name entity recognizer) algorithm on a text. Keep only distinct words from a text.
     *
     * @param post to analyze
     * @param ner  ENUM : LOCATION, ORGANIZATION or PERSON : type of NER analyse
     * @return List of selected words by NER
     * @throws NullPointerException if post or ner is null
     */
    public List<String> applyNLPner(String post, nerOptions ner) {
        Objects.requireNonNull(post);
        Objects.requireNonNull(ner);
        List<String> words = new ArrayList<>();
        Span[] spans;
        String[] sentences = detectSentences(post);
        for (String sentence : sentences) {
            String[] learnableTokens = learnableTokenize(sentence);
            switch (ner.toString()) {
                case "LOCATION":
                    spans = findLocationName(learnableTokens);
                    break;
                case "ORGANIZATION":
                    spans = findOrganizationName(learnableTokens);
                    break;
                case "PERSON":
                    spans = findPersonName(learnableTokens);
                    break;
                default:
                    LOGGER.warn("Bad NER option.\n use : 'LOCATION', 'PERSON' or 'ORGANIZATION'");
                    return words; //return empty list
            }
            Arrays.asList(Span.spansToStrings(spans, learnableTokens)).forEach(words::add);
        }
        return words;
    }
}