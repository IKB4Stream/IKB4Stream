package com.waves_rsp.ikb4stream.core.util;

import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.scoring.twitter.TwitterScoreProcessor;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import java.io.*;
import java.util.Calendar;
import java.util.List;


public class Main {



    public static String readFile(String file) {
        String chaine = "";
        try {
            InputStream ips = new FileInputStream(file);
            InputStreamReader ipsr = new InputStreamReader(ips);
            BufferedReader br = new BufferedReader(ipsr);
            String ligne;
            while ((ligne = br.readLine()) != null) {
                chaine += ligne + "\n";
            }
            br.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return chaine;
    }

    public static void writeFile(String file, String chaine) {
        try {
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter fichierSortie = new PrintWriter(bw);
            fichierSortie.print(chaine);
            fichierSortie.close();
            System.out.println("Le fichier " + file + " a été créé!");
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

    public static void main(String[] args) throws IOException, JSONException {
        String text = readFile("article.txt");
        StringBuilder sb = new StringBuilder();

        List<String> lemmatizedTokens = OpenNLP.applyNLPlemma(text);
        List<String> location = OpenNLP.applyNLPner(text, OpenNLP.nerOptions.LOCATION);
        List<String> org = OpenNLP.applyNLPner(text, OpenNLP.nerOptions.ORGANIZATION);
        List<String> person = OpenNLP.applyNLPner(text, OpenNLP.nerOptions.PERSON);

        sb.append("Keep ").append(lemmatizedTokens.size()).append(" words").append("\n");
        lemmatizedTokens.forEach(t-> System.out.println(t));
        sb.append("Find ").append(location.size()).append(" locations").append("\n");
        location.forEach(l -> sb.append(l).append(" - "));
        sb.append("\nFind ").append(org.size()).append(" organizations").append("\n");
        org.forEach(l->sb.append(l).append(" - "));
        sb.append("\nFind ").append(person.size()).append(" persons").append("\n");
        person.forEach(l->sb.append(l).append(" - "));

        writeFile("stat_dico_v2", sb.toString());

        JSONObject json = new JSONObject();
        json.append("description", text);
        json.append("user_certified", false);
        Event event = new Event(new LatLong(2, 2), Calendar.getInstance().getTime(), Calendar.getInstance().getTime(), json.toString(), "Twitter");
        TwitterScoreProcessor tsp = new TwitterScoreProcessor();
        byte score = tsp.processScore(event).getScore();
        System.out.println("score : " + score);

        GeoCoderJacksonParser geocoder = new GeoCoderJacksonParser();
        location.forEach(l -> {
            LatLong ll = geocoder.parse(l);
            System.out.println(l + " : " + ll);
        });


    }
}
