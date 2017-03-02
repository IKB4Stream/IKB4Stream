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

package com.waves_rsp.ikb4stream.scoring.openagenda;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.core.util.RulesReader;
import com.waves_rsp.ikb4stream.core.util.nlp.OpenNLP;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * {@link IScoreProcessor} will be applied to OpenAgenda {@link Event}
 *
 * @author ikb4stream
 * @version 1.0
 * @see com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor
 */
public class OpenAgendaScoreProcessor implements IScoreProcessor {
    /**
     * Properties of this module
     *
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class, String)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(OpenAgendaScoreProcessor.class, "resources/scoreprocessor/openagenda/config.properties");
    /**
     * Logger used to log all information in this module
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OpenAgendaScoreProcessor.class);
    /**
     * Object to add metrics from this class
     *
     * @see MetricsLogger#log(String, long)
     * @see MetricsLogger#getMetricsLogger()
     */
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    /**
     * Single instance per thread of {@link OpenNLP}
     *
     * @see OpenAgendaScoreProcessor#processScore(Event)
     */
    private final OpenNLP openNLP = OpenNLP.getOpenNLP(Thread.currentThread());
    /**
     * Max score to an {@link Event}
     *
     * @see OpenAgendaScoreProcessor#verifyMaxScore(byte)
     */
    private static final byte MAX_SCORE = Event.getScoreMax();
    /**
     * Map word, score
     *
     * @see OpenAgendaScoreProcessor#processScore(Event)
     */
    private final Map<String, Integer> rulesMap;

    /**
     * Default constructor to initialize {@link OpenAgendaScoreProcessor#rulesMap} with a {@link PropertiesManager}
     *
     * @see OpenAgendaScoreProcessor#rulesMap
     * @see OpenAgendaScoreProcessor#PROPERTIES_MANAGER
     */
    public OpenAgendaScoreProcessor() {
        try {
            String filename = PROPERTIES_MANAGER.getProperty("openagenda.rules.file");
            rulesMap = RulesReader.parseJSONRules(filename);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid property {} ", e);
            throw new IllegalStateException("Invalid property\n" + e.getMessage());
        }
    }

    /**
     * Process score of an event from an {@link Event}
     *
     * @param event an {@link Event} without {@link Event#score}
     * @return Event with a score after {@link OpenNLP} processing
     * @throws NullPointerException if event is null
     * @see OpenAgendaScoreProcessor#rulesMap
     * @see OpenAgendaScoreProcessor#openNLP
     * @see Event
     */
    @Override
    public Event processScore(Event event) {
        Objects.requireNonNull(event);
        long start = System.currentTimeMillis();
        ObjectMapper mapper = new ObjectMapper();
        String eventDesc;
        byte score = 0;
        try {
            JsonNode jsonDescription = mapper.readTree(event.getDescription());
            String title = jsonDescription.get("title").asText();
            String description = jsonDescription.get("description").asText();
            eventDesc = title + " " + description;
            List<String> fbList = openNLP.applyNLPlemma(eventDesc);
            for (String word : fbList) {
                if (rulesMap.containsKey(word)) {
                    score += rulesMap.get(word);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Bad json format or tree cannot be read {} ", e);
            throw new IllegalArgumentException("Bad json format or tree cannot be read");
        }

        long time = System.currentTimeMillis() - start;
        METRICS_LOGGER.log("time_scoring_" + event.getSource(), time);
        return new Event(event.getLocation(), event.getStart(), event.getEnd(), eventDesc, verifyMaxScore(score), event.getSource());
    }

    /**
     * Get all sources that {@link IScoreProcessor} will be applied
     *
     * @return List of sources accepted
     * @see OpenAgendaScoreProcessor#PROPERTIES_MANAGER
     */
    @Override
    public List<String> getSources() {
        List<String> sources = new ArrayList<>();
        try {
            String allSources = PROPERTIES_MANAGER.getProperty("openagenda.scoring.sources");
            sources.addAll(Arrays.asList(allSources.split(",")));
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
        }
        return sources;
    }

    /**
     * Check that the score can't overtake {@link OpenAgendaScoreProcessor#MAX_SCORE}
     *
     * @param score calculated by {@link OpenNLP} processing
     * @return score {@link OpenAgendaScoreProcessor#MAX_SCORE}
     */
    private byte verifyMaxScore(byte score) {
        if (score > MAX_SCORE) {
            return MAX_SCORE;
        }
        return score;
    }
}
