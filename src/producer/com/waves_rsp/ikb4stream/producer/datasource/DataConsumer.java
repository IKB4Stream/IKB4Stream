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

package com.waves_rsp.ikb4stream.producer.datasource;

import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.producer.DatabaseWriter;
import com.waves_rsp.ikb4stream.producer.score.ScoreProcessorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataConsumer {
    private final ScoreProcessorManager scoreProcessorManger = new ScoreProcessorManager();
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    private static final DatabaseWriter DATABASE_WRITER = DatabaseWriter.getInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger(DataConsumer.class);
    private final DataQueue dataQueue;
    private final int targetScore;

    private DataConsumer(DataQueue dataQueue, int targetScore) {
        this.dataQueue = dataQueue;
        this.targetScore = targetScore;
    }

    /**
     * Create a DataConsumer
     * @return DataConsumer
     */
    public static DataConsumer createDataConsumer(DataQueue dataQueue) {
        /* Get target score */
        int targetScore = 25;
        try {
            targetScore = Integer.parseInt(PropertiesManager.getInstance(DataConsumer.class, "resources/config.properties").getProperty("score.target"));
        } catch (NumberFormatException e) {
            LOGGER.warn("score.target is not a number, use default value");
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Use default value for score.target");
        }

        return new DataConsumer(dataQueue, targetScore);
    }


    /**
     * Filter an event
     * @param event Event to be filter
     * @param score Target score to reach to be insert into database
     * @return True if it's greater than score
     */
    private boolean filter(Event event, int score) {
        return event.getScore() >= 0 && event.getScore() >= score;
    }

    /**
     * Consume Event in dataQueue and send to scoreProcessor
     */
    public void consume() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Event event = dataQueue.pop();
                Event eventClone = scoreProcessorManger.processScore(event);
                LOGGER.info("Event {} has been scored", eventClone);

                if (filter(eventClone, targetScore)) {
                    DATABASE_WRITER.insertEvent(eventClone, t -> {
                        METRICS_LOGGER.log("event_scored_"+event.getSource(), eventClone.getScore());
                        LOGGER.error(t.getMessage());
                    });
                }else {
                    METRICS_LOGGER.log("scored_not_kept_"+event.getSource(), eventClone.getScore());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
