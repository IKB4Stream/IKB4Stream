package com.waves_rsp.ikb4stream.core.util;

import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.lemmatizer.SimpleLemmatizer;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class OpenNLP {
    private static final String PATH_DICTIONARIES = "resources/opennlp-models/dictionaries/lemma_dict_lefff";
    private static final String PATH_BINARIES = "resources/opennlp-models/binaries/";
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenNLP.class);
    private static final NameFinderME NAME_FINDER_PERS;
    private static final NameFinderME NAME_FINDER_ORG;
    private static final NameFinderME NAME_FINDER_LOC;
    private static final SentenceDetectorME DETECTOR;
    private static final Tokenizer TOKENIZER;
    private static final POSTaggerME TAGGER;


    static {
        try {
            InputStream fileFrSentBin = new FileInputStream(PATH_BINARIES + "fr-sent.bin");
            DETECTOR = new SentenceDetectorME(new SentenceModel(fileFrSentBin));
            fileFrSentBin.close();

            InputStream fileFrTokenBin = new FileInputStream(PATH_BINARIES + "fr-token.bin");
            TOKENIZER = new TokenizerME(new TokenizerModel(fileFrTokenBin));
            fileFrTokenBin.close();

            InputStream fileFrPosMaxent2Bin = new FileInputStream(PATH_BINARIES + "fr-pos-maxent-2.bin");
            TAGGER = new POSTaggerME(new POSModel(fileFrPosMaxent2Bin));
            fileFrPosMaxent2Bin.close();

            InputStream frNerOrganizationBin = new FileInputStream(PATH_BINARIES + "fr-ner-organization.bin");
            NAME_FINDER_ORG = new NameFinderME(new TokenNameFinderModel(frNerOrganizationBin));
            frNerOrganizationBin.close();

            InputStream fileFrNerLocationBin = new FileInputStream(PATH_BINARIES + "fr-ner-location.bin");
            NAME_FINDER_LOC = new NameFinderME(new TokenNameFinderModel(fileFrNerLocationBin));
            fileFrNerLocationBin.close();

            InputStream fileNerPersonBin = new FileInputStream(PATH_BINARIES + "fr-ner-person.bin");
            NAME_FINDER_PERS = new NameFinderME(new TokenNameFinderModel(fileNerPersonBin));
            fileNerPersonBin.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException(e);
        }
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
    private static String[] detectSentences(String text) {
        Objects.requireNonNull(text);
        return DETECTOR.sentDetect(text);
    }

    /**
     * OpenNLP : learnableTokenize. The function tokenize a text
     *
     * @param text to tokenize
     * @return an array of words
     */
    private static String[] learnableTokenize(String text) {
        Objects.requireNonNull(text);
        return TOKENIZER.tokenize(text);
    }

    /**
     * OpenNLP : posTagging affect a tag to each word (V, NC, NP, ADJ...)
     *
     * @param tokens is a tokenize text
     * @return an array of posTag
     */
    private static String[] posTagging(String[] tokens) {
        Objects.requireNonNull(tokens);
        return TAGGER.tag(tokens);
    }

    /**
     * OpenNLP : name entity recognizer function. Detect organizations names.
     *
     * @param tokens are an array of string to analyze
     * @return an array of entity detected as an organization
     */
    private static Span[] findOrganizationName(String[] tokens) {
        Objects.requireNonNull(tokens);
        return NAME_FINDER_ORG.find(tokens);
    }

    /**
     * OpenNLP : name entity recognizer function. Detect locations names.
     *
     * @param tokens are an array of string to analyze
     * @return an array of entity detected as a location
     */
    private static Span[] findLocationName(String[] tokens) {
        Objects.requireNonNull(tokens);
        return NAME_FINDER_LOC.find(tokens);
    }

    /**
     * OpenNLP : name entity recognizer function. Detect persons names.
     *
     * @param tokens are an array of string to analyze
     * @return an array of entity detected as a personnality
     */
    private static Span[] findPersonName(String[] tokens) {
        Objects.requireNonNull(tokens);
        return NAME_FINDER_PERS.find(tokens);
    }

    /**
     * OpenNLP : lemmatization. The function simplify the step of POStagging for the verbs category.
     *
     * @param text to lemmatize
     * @return Map of each lemmatize word with the POStag associate
     */
    private static Map<String, String> lemmatize(String text) throws IOException {
        Objects.requireNonNull(text);
        InputStream inputStream = new FileInputStream(PATH_DICTIONARIES);
        DictionaryLemmatizer lemmatizer = new SimpleLemmatizer(inputStream);
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
        inputStream.close();
        return lemmatizedTokens;
    }

    /**
     * Apply the OpenNLP Lemmatization with a dictionnary. Keep only words with the verbs and nouns.
     *
     * @param post is the text to lemmatize
     * @return list of selected words.
     */
    public static List<String> applyNLPlemma(String post) {
        Objects.requireNonNull(post);
        Map<String, String> input;
        List<String> output = new ArrayList<>();
        try {
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
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return output;
    }

    /**
     * Apply the ÖpenNLP ner (name entity recognizer) algorithm on a text. Keep only distinct words from a text.
     *
     * @param post to analyze
     * @param ner  ENUM : LOCATION, ORGANIZATION or PERSON : type of NER analyse
     * @return List of selected words by NER
     */
    public static List<String> applyNLPner(String post, nerOptions ner) {
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