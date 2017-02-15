package com.waves_rsp.ikb4stream.core.util;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class OpenNLP {

    public enum nerOptions{
        LOCATION, PERSON, ORGANIZATION
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenNLP.class);

    private static String[] detectSentences(String text) throws IOException {
        Objects.requireNonNull(text);
        InputStream inputStream = new FileInputStream("resources/opennlp-models/fr/fr-sent.bin");
        SentenceModel model = new SentenceModel(inputStream);
        SentenceDetectorME detector = new SentenceDetectorME(model);
        inputStream.close();
        return detector.sentDetect(text);
    }

    private static String[] learnableTokenize(String text) throws IOException {
        Objects.requireNonNull(text);
        InputStream inputStream = new FileInputStream("resources/opennlp-models/fr/fr-token.bin");
        TokenizerModel model = new TokenizerModel(inputStream);
        Tokenizer tokenizer = new TokenizerME(model);
        inputStream.close();
        return tokenizer.tokenize(text);
    }


    private static Span[] findOrganizationName(String[] tokens) throws IOException {
        Objects.requireNonNull(tokens);
        InputStream inputStream = new FileInputStream("resources/opennlp-models/fr/fr-ner-organization.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(inputStream);
        NameFinderME nameFinder = new NameFinderME(model);
        inputStream.close();
        return nameFinder.find(tokens);
    }

    private static Span[] findLocationName(String[] tokens) throws IOException {
        Objects.requireNonNull(tokens);
        InputStream inputStream = new FileInputStream("resources/opennlp-models/fr/fr-ner-location.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(inputStream);
        NameFinderME nameFinder = new NameFinderME(model);
        inputStream.close();
        return nameFinder.find(tokens);
    }

    private static Span[] findPersonName(String[] tokens) throws IOException {
        Objects.requireNonNull(tokens);
        InputStream inputStream = new FileInputStream("resources/opennlp-models/fr/fr-ner-person.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(inputStream);
        NameFinderME nameFinder = new NameFinderME(model);
        inputStream.close();
        return nameFinder.find(tokens);
    }


    /**
     * Apply the NLP ner (name entity recognizer) algorithm on a text. Keep only distinct words from the tweet.
     * @param post  to analyze
     * @param ner ENUM : LOCATION, ORGANIZATION or PERSON : type of NER analyse
     * @return List of selected words by NER
     */
    public static List<String> applyNLPner(String post, nerOptions ner) {
        Objects.requireNonNull(post);
        Objects.requireNonNull(ner);
        List<String> words = new ArrayList<>();
        Span[] spans ;
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

        String testLoc = "Il y a une fuite d'eau à Paris, il y a urgence. ";

        List<String> entity = applyNLPner(testLoc, nerOptions.LOCATION);
        //List<String> entityOrg = applyNLPner(testLoc, nerOptions.ORGANIZATION);

        System.out.println("\n*** ENTITY ***");
        for (String s : entity) {
            System.out.print(s + " - ");
        }
        //List<String> person = applyNLPtoTweet(paragraphFR, "person");
        //List<String> org = applyNLPtoTweet(paragraphFR, "organization");

    }
}