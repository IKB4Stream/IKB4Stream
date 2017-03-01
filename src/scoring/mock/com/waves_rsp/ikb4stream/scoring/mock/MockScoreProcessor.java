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

package com.waves_rsp.ikb4stream.scoring.mock;

import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * {@link IScoreProcessor} will be applied to different mock sources
 * @author ikb4stream
 * @version 1.0
 * @see IScoreProcessor
 */
public class MockScoreProcessor implements IScoreProcessor {
    /**
     * Properties of this module
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class, String)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(MockScoreProcessor.class, "resources/scoreprocessor/mock/config.properties");
    /**
     * Logger used to log all information in this module
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MockScoreProcessor.class);
    /**
     * Object to add metrics from this class
     * @see MetricsLogger#log(String, long)
     * @see MetricsLogger#getMetricsLogger()
     */
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();

    /**
     * Override default constructor
     */
    public MockScoreProcessor() {
        // Do nothing else
    }

    /**
     * Process score of an event from an {@link Event}
     * @param event an {@link Event} without {@link Event#score}
     * @return Event with a random score
     * @throws NullPointerException if event is null
     * @see Event
     * @see Event#getScoreMax()
     */
    @Override
    public Event processScore(Event event) {
        Objects.requireNonNull(event);
        long start = System.currentTimeMillis();
        Random rand = new Random();
        byte nombreAleatoire = (byte)rand.nextInt(Event.getScoreMax() + 1);
        LOGGER.info("Score aléatoire: " + nombreAleatoire);
        long time = System.currentTimeMillis() - start;
        METRICS_LOGGER.log("time_scoring_" + event.getSource(), time);
        return new Event(event.getLocation(), event.getStart(), event.getEnd(), event.getDescription(), nombreAleatoire, event.getSource());
    }

    /**
     * Get all sources that {@link IScoreProcessor} will be applied
     * @return List of sources accepted
     * @see MockScoreProcessor#PROPERTIES_MANAGER
     */
    @Override
    public List<String> getSources() {
        List<String> sources = new ArrayList<>();
        try {
            String allSources = PROPERTIES_MANAGER.getProperty("mock.scoring.sources");
            if (allSources.isEmpty()) {
                return sources;
            }
            sources.addAll(Arrays.asList(allSources.split(",")));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(e.getMessage());
        }
        return sources;
    }
}

