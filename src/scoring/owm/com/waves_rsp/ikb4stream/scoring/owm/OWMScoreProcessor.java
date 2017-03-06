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

package com.waves_rsp.ikb4stream.scoring.owm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * {@link OWMScoreProcessor} class set up score for events
 *
 * @author ikb4stream
 * @version 1.0
 * @see IScoreProcessor
 */
public class OWMScoreProcessor implements IScoreProcessor {
    /**
     * Properties of this module
     *
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class, String)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(OWMScoreProcessor.class, "resources/scoreprocessor/owm/config.properties");
    /**
     * Logger used to log all information in this module
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OWMScoreProcessor.class);
    /**
     * Score depending on weather type
     *
     * @see OWMScoreProcessor#processScore(Event)
     */
    private final Map<String, Integer> weatherValues = new HashMap<>();
    /**
     * Weather object receive
     *
     * @see OWMScoreProcessor#processScore(Event)
     */
    private final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Max score to an {@link Event}
     *
     * @see OWMScoreProcessor#processScore(Event)
     */
    private static final byte MAX_SCORE = Event.getScoreMax();
    /**
     * Min score to an {@link Event}
     *
     * @see OWMScoreProcessor#processScore(Event)
     */
    private static final byte MIN_SCORE = Event.getScoreMin();
    /**
     * Threshold in temperature to reach
     *
     * @see OWMScoreProcessor#processScore(Event)
     */
    private final int threshold;
    /**
     * Factor to multiply of temperature
     *
     * @see OWMScoreProcessor#processScore(Event)
     */
    private final int factor;

    /**
     * Default constructor to initialize {@link OWMScoreProcessor} with custom rules
     *
     * @see OWMScoreProcessor#threshold
     * @see OWMScoreProcessor#factor
     * @see OWMScoreProcessor#weatherValues
     * @see OWMScoreProcessor#PROPERTIES_MANAGER
     */
    public OWMScoreProcessor() {
        try {
            String filename = PROPERTIES_MANAGER.getProperty("openweathermap.rules.file");
            JsonNode jsonNode = objectMapper.readTree(new File(filename));
            threshold = Byte.parseByte(jsonNode.path("threshold").asText());
            factor = Byte.parseByte(jsonNode.path("factor").asText());
            JsonNode weatherType = jsonNode.path("weatherType");
            weatherType.fieldNames().forEachRemaining(field -> weatherValues.put(field, weatherType.path(field).asInt()));
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException(e);
        } catch (IOException e) {
            LOGGER.warn("objectMapper failed: {}", e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    /**
     * Get new score for a weather type
     *
     * @param score Actual score of the event
     * @param type  Weather type
     * @return Score updated
     * @throws NullPointerException if type is null
     */
    private byte weatherType(byte score, String type) {
        Objects.requireNonNull(type);
        Integer sc = weatherValues.get(type);
        if (sc == null) return score;
        return (byte) (score + sc);
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
        } else if (score < MIN_SCORE) {
            return MIN_SCORE;
        }
        return score;
    }

    /**
     * Process score of an event from an {@link Event}
     *
     * @param event an {@link Event} without {@link Event#score}
     * @return Event with a score after temperature analysis
     * @throws NullPointerException if event is null
     * @see OWMScoreProcessor#objectMapper
     * @see Event
     */
    @Override
    public Event processScore(Event event) {
        Objects.requireNonNull(event);
        String jsonString = event.getDescription();
        try {
            JsonNode jn = objectMapper.readTree(jsonString);
            double temperature = jn.path("main").path("temp").asDouble();
            //We suppose that if the T° < threshold, no one turn on fill up his pool
            byte score1 = (byte) ((temperature - threshold) * factor);
            //About the weather, if it rain or snow
            byte score2 = weatherType(score1, jn.path("weather").get(0).path("main").asText());
            String description = createDescription(jn);
            return new Event(event.getLocation(), event.getStart(), event.getEnd(), description, verifyMaxScore(score2), event.getSource());
        } catch (IOException e) {
            LOGGER.warn("objectMapper failed: {}", e);
            return event;
        }
    }

    /**
     * Describe an {@link Event}
     *
     * @param node JSON Object to read
     * @return Future string of {@link Event#description}
     */
    private static String createDescription(JsonNode node) {
        String weather = "Weather: " + node.path("weather").get(0).path("description").asText();
        String temperature = "Temperature: " + node.path("main").path("temp").asDouble() + "°C";
        String pressure = "Pressure: " + node.path("main").path("pressure").asText() + "hPa";
        String humidity = "Humidity: " + node.path("main").path("humidity").asText() + "%";
        String wind = "Wind: " + node.path("wind").path("speed").asText() + "m/s";
        return weather + "\\n" + temperature + "\\n" + pressure + "\\n" + humidity + "\\n" + wind;
    }

    /**
     * Get all sources that {@link IScoreProcessor} will be applied
     *
     * @return List of sources accepted
     * @see OWMScoreProcessor#PROPERTIES_MANAGER
     */
    @Override
    public List<String> getSources() {
        List<String> sources = new ArrayList<>();
        try {
            String allSources = PROPERTIES_MANAGER.getProperty("openweathermap.scoring.sources");
            sources.addAll(Arrays.asList(allSources.split(",")));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(e.getMessage());
        }
        return sources;
    }
}
