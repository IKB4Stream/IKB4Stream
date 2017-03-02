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

package com.waves_rsp.ikb4stream.scoring.event;

import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.core.util.RulesReader;
import com.waves_rsp.ikb4stream.core.util.nlp.OpenNLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * {@link IScoreProcessor} will be applied to different sources
 *
 * @author ikb4stream
 * @version 1.0
 * @see com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor
 */
public class EventScoreProcessor implements IScoreProcessor {
    /**
     * Properties of this module
     *
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class, String)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(EventScoreProcessor.class, "resources/scoreprocessor/event/config.properties");
    /**
     * Logger used to log all information in this module
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EventScoreProcessor.class);
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
     * @see EventScoreProcessor#processScore(Event)
     */
    private final OpenNLP openNLP = OpenNLP.getOpenNLP(Thread.currentThread());
    /**
     * Max score to an {@link Event}
     *
     * @see EventScoreProcessor#processScore(Event)
     */
    private static final byte MAX = Event.getScoreMax();
    /**
     * Map word, score
     *
     * @see EventScoreProcessor#processScore(Event)
     */
    private final Map<String, Integer> rulesMap;

    /**
     * Default constructor to initialize {@link EventScoreProcessor#rulesMap} with a {@link PropertiesManager}
     *
     * @see EventScoreProcessor#rulesMap
     * @see EventScoreProcessor#PROPERTIES_MANAGER
     */
    public EventScoreProcessor() {
        try {
            String ruleFilename = PROPERTIES_MANAGER.getProperty("event.rules.file");
            rulesMap = RulesReader.parseJSONRules(ruleFilename);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * Process score of an event from an {@link Event}
     *
     * @param event an {@link Event} without {@link Event#score}
     * @return Event with a score after {@link OpenNLP} processing
     * @throws NullPointerException if event is null
     * @see EventScoreProcessor#rulesMap
     * @see EventScoreProcessor#openNLP
     * @see EventScoreProcessor#MAX
     * @see Event
     */
    @Override
    public Event processScore(Event event) {
        Objects.requireNonNull(event);
        long start = System.currentTimeMillis();
        String content = event.getDescription();
        List<String> eventList = openNLP.applyNLPlemma(content);
        byte score = 0;
        for (String word : eventList) {
            if (rulesMap.containsKey(word)) {
                score += rulesMap.get(word);
            }
        }
        if (score > MAX) {
            score = MAX;
        }
        long time = System.currentTimeMillis() - start;
        METRICS_LOGGER.log("time_scoring_" + event.getSource(), time);
        return new Event(event.getLocation(), event.getStart(), event.getEnd(), event.getDescription(), score, event.getSource());
    }

    /**
     * Get all sources that {@link IScoreProcessor} will be applied
     *
     * @return List of sources accepted
     * @see EventScoreProcessor#PROPERTIES_MANAGER
     */
    @Override
    public List<String> getSources() {
        List<String> sources = new ArrayList<>();
        try {
            String allSources = PROPERTIES_MANAGER.getProperty("event.scoring.sources");
            sources.addAll(Arrays.asList(allSources.split(",")));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(e.getMessage());
        }
        return sources;
    }
}
