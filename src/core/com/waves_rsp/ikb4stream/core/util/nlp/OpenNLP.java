package com.waves_rsp.ikb4stream.core.util.nlp;

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

public class OpenNLP {
    private static final String PATH_DICTIONARIES = "resources/opennlp-models/dictionaries/lemma_dict_lefff";
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenNLP.class);
    private static final Map<Thread, OpenNLP> INSTANCES = new HashMap<>();
    private final DictionaryLemmatizer lemmatizer;
    private final SentenceDetectorME detector;
    private final NameFinderME nameFinderPers;
    private final NameFinderME nameFinderOrg;
    private final NameFinderME nameFinderLoc;
    private final Tokenizer tokenizer;
    private final POSTaggerME tagger;

    /**
     *
     */
    private OpenNLP() {
        try {
            detector = new SentenceDetectorME(LoaderNLP.getSentenceModel());
            tokenizer = new TokenizerME(LoaderNLP.getTokenizerModel());
            tagger = new POSTaggerME(LoaderNLP.getPosModel());
            nameFinderOrg = new NameFinderME(LoaderNLP.getTokenNameFinderModelOrg());
            nameFinderLoc = new NameFinderME(LoaderNLP.getTokenNameFinderModelLoc());
            nameFinderPers = new NameFinderME(LoaderNLP.getTokenNameFinderModelPers());
            InputStream inputStream = new FileInputStream(PATH_DICTIONARIES);
            lemmatizer = new SimpleLemmatizer(inputStream);
            inputStream.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    /**
     * Get instance of OpenNLP for each thread because Apache OpenNLP is not thread safe
     * @param thread Thread needs OpenNLP
     * @return Instance of OpenNLP
     */
    public static OpenNLP getOpenNLP(Thread thread) {
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
     * @param post is the text to lemmatize
     * @param limit is the limit to have the n first characters
     * @return list of selected words.
     */
    public List<String> applyNLPlemma(String post, int limit) {
        Objects.requireNonNull(post);
        if (post.length() > limit) {
            post = post.substring(0, limit);
        }
        Map<String, String> input;
        List<String> output = new ArrayList<>();
        input = lemmatize(post);
        input.forEach((w, pos) -> {
            if (w.startsWith("#")) {
                //if it's a hashtag
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
     */
    public List<String> applyNLPlemma(String post) {
        return applyNLPlemma(post, 1250);
    }

    /**
     * Apply the ÖpenNLP ner (name entity recognizer) algorithm on a text. Keep only distinct words from a text.
     *
     * @param post to analyze
     * @param ner  ENUM : LOCATION, ORGANIZATION or PERSON : type of NER analyse
     * @return List of selected words by NER
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
            //Add each entity in the list 'words'
            Arrays.asList(Span.spansToStrings(spans, learnableTokens)).forEach(words::add);
        }
        return words;
    }
}