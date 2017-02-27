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

import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
import com.waves_rsp.ikb4stream.core.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *This class receives data (Event) from connectors and push in DataQueue
 * @see DataQueue
 */
public class DataProducer implements IDataProducer {
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    private static final Logger LOGGER = LoggerFactory.getLogger(DataProducer.class);
    private final DataQueue dataQueue;

    /**
     * The constructor with a DataQueue in param
     * @param dataQueue Set the dataQueue to this Producer
     */
    public DataProducer(DataQueue dataQueue) {
        this.dataQueue = dataQueue;
    }

    /**
     * Push an event into DataQueue
     * @param event Event to push in DataQueue to be analysed
     */
    public void push(Event event) {
        long start = System.currentTimeMillis();
        dataQueue.push(event);
        long end = System.currentTimeMillis();
        long result = end - start;
        METRICS_LOGGER.log("time_process_"+event.getSource(), result);
        LOGGER.info("The event {} has been pushed into database.", event.getSource());
    }
}
