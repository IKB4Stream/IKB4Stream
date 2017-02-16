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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class OpenNLP {

    private static final String PATH_BINARIES = "resources/opennlp-models/binaries/";
    private static final String PATH_DICTIONARIES = "resources/opennlp-models/dictionaries/";

    /**
     * Enum the ner options
     */
    public enum nerOptions {
        LOCATION, PERSON, ORGANIZATION
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenNLP.class);

    /**
     * OpenNLP : split a text in sentences
     * @param text to analyze
     * @return an array of sentences
     * @throws IOException if the binaries doesn't exits
     */
    private static String[] detectSentences(String text) throws IOException {
        Objects.requireNonNull(text);
        InputStream inputStream = new FileInputStream(PATH_BINARIES + "fr-sent.bin");
        SentenceModel model = new SentenceModel(inputStream);
        SentenceDetectorME detector = new SentenceDetectorME(model);
        inputStream.close();
        return detector.sentDetect(text);
    }

    /**
     * OpenNLP : learnableTokenize. The function tokenize a text
     *
     * @param text to tokenize
     * @return an array of words
     * @throws IOException if the binaries doesn't exits
     */
    private static String[] learnableTokenize(String text) throws IOException {
        Objects.requireNonNull(text);
        InputStream inputStream = new FileInputStream(PATH_BINARIES + "fr-token.bin");
        TokenizerModel model = new TokenizerModel(inputStream);
        Tokenizer tokenizer = new TokenizerME(model);
        inputStream.close();
        return tokenizer.tokenize(text);
    }

    /**
     * OpenNLP : posTagging affect a tag to each word (V, NC, NP, ADJ...)
     *
     * @param tokens is a tokenize text
     * @return an array of posTag
     * @throws IOException if the binaries doesn't exits
     */
    private static String[] posTagging(String[] tokens) throws IOException {
        Objects.requireNonNull(tokens);
        InputStream inputStream = new FileInputStream(PATH_BINARIES + "fr-pos-maxent-2.bin");
        POSModel model = new POSModel(inputStream);
        POSTaggerME tagger = new POSTaggerME(model);
        inputStream.close();
        return tagger.tag(tokens);
    }

    /**
     * OpenNLP : name entity recognizer function. Detect organizations names.
     *
     * @param tokens are an array of string to analyze
     * @return an array of entity detected as an organization
     * @throws IOException if the binaries doesn't exits
     */
    private static Span[] findOrganizationName(String[] tokens) throws IOException {
        Objects.requireNonNull(tokens);
        InputStream inputStream = new FileInputStream(PATH_BINARIES + "fr-ner-organization.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(inputStream);
        NameFinderME nameFinder = new NameFinderME(model);
        inputStream.close();
        return nameFinder.find(tokens);
    }

    /**
     * OpenNLP : name entity recognizer function. Detect locations names.
     *
     * @param tokens are an array of string to analyze
     * @return an array of entity detected as a location
     * @throws IOException if the binaries doesn't exits
     */
    private static Span[] findLocationName(String[] tokens) throws IOException {
        Objects.requireNonNull(tokens);
        InputStream inputStream = new FileInputStream(PATH_BINARIES + "fr-ner-location.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(inputStream);
        NameFinderME nameFinder = new NameFinderME(model);
        inputStream.close();
        return nameFinder.find(tokens);
    }

    /**
     * OpenNLP : name entity recognizer function. Detect persons names.
     *
     * @param tokens are an array of string to analyze
     * @return an array of entity detected as a personnality
     * @throws IOException if the binaries doesn't exits
     */
    private static Span[] findPersonName(String[] tokens) throws IOException {
        Objects.requireNonNull(tokens);
        InputStream inputStream = new FileInputStream(PATH_BINARIES + "fr-ner-person.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(inputStream);
        NameFinderME nameFinder = new NameFinderME(model);
        inputStream.close();
        return nameFinder.find(tokens);
    }

    /**
     * OpenNLP : lemmatization. The function simplify the step of POStagging for the verbs category.
     *
     * @param text to lemmatize
     * @return Map of each lemmatize word with the POStag associate
     * @throws IOException if the dictionnary doesn't exits
     */
    private static Map<String, String> lemmatize(String text) throws IOException {
        Objects.requireNonNull(text);
        InputStream inputStream = new FileInputStream(PATH_DICTIONARIES + "lemma_dict.txt");
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
                if (tags[i].startsWith("V") & tags[i].length() > 1){
                    //if the POStag start with V, we just keep the tag V for simplify the lemmatization with the dictionnary
                    System.out.println("transform : " + lemmatizer.lemmatize(learnableTokens[i], tags[i]) + " " + tags[i] + " --> V"); //TODO
                    tags[i] = "V";
                }
                System.out.println("lemma : " + lemmatizer.lemmatize(learnableTokens[i], tags[i]) + " " + tags[i]); //TODO

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
                    if ((pos.contains("N") || pos.contains("V")) && !pos.equals("PONCT")) {
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
        try {
            String sentences[] = detectSentences(post);
            for (String sentence : sentences) {
                System.out.println(sentence);
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
                Arrays.asList(Span.spansToStrings(spans, learnableTokens)).forEach(sp -> words.add(sp));
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return words;
    }

    public static void main(String[] args) throws IOException {
        String paragraphFR = "Il y a une fuite d'eau à Marseille et dans le quartier du château de Versailles dans la ville de Paris. " +
                "C'est EDF Suez qui transmet l'information au ministère de la ville de Londres. " +
                "Parce qu'il n'y pas l'ONU. L'adresse de Charles Aznavour est 10 Rue d'Uzès, 75002 Paris, France.";

        String test = "Les orages, la grêle et les intempéries qui touchent le pays sont parfois si violents que  Météo" +
                " France  émet des alertes  vigilance. Ainsi lors au mois de mai 2016, les fortes pluies ont donné lieu " +
                "à des records de  pluviométrie, suivie de crues et inondations dans le Centre et le Loiret sur le Loing," +
                " puis la Seine à Paris. Ces événements peuvent être reconnus par l'Etat comme une catastrophe naturelle " +
                ", par arrêté interministériel. En Ile-de-France, toute montée des eaux rapide fait craindre une nouvelle " +
                "crue centennale comme en 1910.";

        String testLoc = "Nous étions présents à l'école tous les jours. Je m'était aperçu qu'il était absent. Cependant j'ai eu un aperçu de son travail.";

        List<String> lemmatizedTokens = applyNLPlemma(testLoc);
        for (String s : lemmatizedTokens)
            System.out.println(s);
    }
}