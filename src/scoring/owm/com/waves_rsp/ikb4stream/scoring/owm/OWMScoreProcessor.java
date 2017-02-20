package com.waves_rsp.ikb4stream.scoring.owm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * OWMScoreProcessor class set up score for events
 */
public class OWMScoreProcessor implements IScoreProcessor{

    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(OWMScoreProcessor.class, "resources/scoreprocessor/owm/config.properties");
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OWMScoreProcessor.class);
    private final String filename;
    private static final byte MAX_SCORE = 100;
    private static final byte MIN_SCORE = 0;
    private final int threshold;
    private final int factor;
    private final int rain;
    private final int snow;
    private final int defaultType;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OWMScoreProcessor() {
        try {
            filename = PROPERTIES_MANAGER.getProperty("openweathermap.rules.file");
            JsonNode jsonNode = objectMapper.readTree(new File(filename));
            threshold = Byte.parseByte(jsonNode.path("threshold").asText());
            factor = Byte.parseByte(jsonNode.path("factor").asText());
            JsonNode weatherType = jsonNode.path("weatherType");
            rain = Byte.parseByte(weatherType.path("rain").asText());
            snow = Byte.parseByte(weatherType.path("snow").asText());
            defaultType = Byte.parseByte(weatherType.path("default").asText());
        } catch (IOException e) {
            LOGGER.warn("objectMapper failed: {}", e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     *
     * @param score
     * @param type
     * @return
     */
    private byte weatherType(byte score, String type) {
        if (("rain").equalsIgnoreCase(type)) {
            return (byte) (score - rain);
        }else if (("snow").equalsIgnoreCase(type)) {
            return (byte) (score - snow);
        }
        return (byte) (score + defaultType);
    }

    /**
     * Check that the score can't overtake @MAX_SCORE
     *
     * @param score calculated by OpenNLP processing
     * @return score (max = @MAX_SCORE)
     */
    byte verifyMaxScore(byte score) {
        if (score > MAX_SCORE) {
            return MAX_SCORE;
        }else if(score < MIN_SCORE){
            return MIN_SCORE;
        }
        return score;
    }

    /**
     *
     * @param event an event without score
     * @return an event with score
     * @throws NullPointerException if the event is null
     */
    @Override
    public Event processScore(Event event) {
        Objects.requireNonNull(event);
        String jsonString = event.getDescription();
        try {
            JsonNode jn = objectMapper.readTree(jsonString);
            //Temperature convert from Fahrein Celsius
            double temperature = (jn.path("main").path("temp").asDouble()-32)/1.8;
            //We suppose that if the T° < threshold, no one turn on fill up his pool
            byte score1 = (byte)((temperature - threshold)* factor);
            //About the weather, if it rain or snow
            byte score2 = weatherType(score1, jn.path("weather").get(0).path("main").asText());
            String description = jn.path("weather").get(0).path("description").asText();
            return new Event(event.getLocation(), event.getStart(), event.getEnd(), description, verifyMaxScore(score2), event.getSource());
        } catch (IOException e) {
            LOGGER.warn("objectMapper failed: {}", e);
            return event;
        }

    }

    @Override
    public List<String> getSources() {
        List<String> sources = new ArrayList<>();
        try {
            String allSources = PROPERTIES_MANAGER.getProperty("owm.scoring.sources");
            sources.addAll(Arrays.asList(allSources.split(",")));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(e.getMessage());
        }
        return sources;
    }

}
