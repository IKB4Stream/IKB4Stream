package com.waves_rsp.ikb4stream.producer.score.sample;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class NLP {


    public static void main(String[] args) {
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit,  pos");
        props.setProperty("tokenize.language", "fr");
        props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/french/french.tagger");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // read some text from the file..
        String text = "Hier, lors de la visite de François Hollande, il y a eu un incendie à Versailles qui a tout ravagé!";

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        List<String> interestWord = new ArrayList<>();
        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                System.out.println("word: " + word + " pos: " + pos );

                if ((pos.contains("N") || pos.equals("V") || (pos.equals("VPP"))) && !pos.contains("PUNC")){

                    if (!interestWord.contains(word)) {interestWord.add(word);};
                }
            }
        }

        interestWord.forEach(l-> System.out.println(l));

    }
}
