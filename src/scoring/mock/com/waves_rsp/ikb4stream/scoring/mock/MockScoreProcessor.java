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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class MockScoreProcessor implements IScoreProcessor {
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(MockScoreProcessor.class, "resources/scoreprocessor/mock/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(MockScoreProcessor.class);

    public MockScoreProcessor() {
        // Do nothing else
    }

    @Override
    public Event processScore(Event event) {
        long start = System.currentTimeMillis();
        Random rand = new Random();
        byte nombreAleatoire = (byte)rand.nextInt(100 + 1);
        LOGGER.info("Score aléatoire: " + nombreAleatoire);
        long time = System.currentTimeMillis() - start;
        METRICS_LOGGER.log("time_scoring_" + event.getSource(), time);
        return new Event(event.getLocation(), event.getStart(), event.getEnd(), event.getDescription(), nombreAleatoire, event.getSource());
    }

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

