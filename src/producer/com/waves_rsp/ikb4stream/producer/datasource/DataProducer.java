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

import java.util.Objects;

/**
 * This class receives {@link Event} from connectors and push in {@link DataQueue}
 * @author ikb4stream
 * @version 1.0
 */
public class DataProducer implements IDataProducer {
    /**
     * Object to add metrics from this class
     * @see DataProducer#push(Event)
     * @see MetricsLogger#log(String, long)
     * @see MetricsLogger#getMetricsLogger()
     */
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    /**
     * Logger used to log all information in this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataProducer.class);
    /**
     * Single instance of {@link DataQueue}
     * @see DataQueue#pop()
     * @see DataProducer#push(Event)
     * @see DataProducer#DataProducer(DataQueue)
     */
    private final DataQueue dataQueue;

    /**
     * Give the unique instance of {@link DataQueue}
     * @param dataQueue Set the {@link DataQueue} to this Producer
     * @throws NullPointerException if dataQueue is null
     * @see DataProducer#dataQueue
     */
    public DataProducer(DataQueue dataQueue) {
        Objects.requireNonNull(dataQueue);
        this.dataQueue = dataQueue;
    }

    /**
     * Push an {@link Event} into DataQueue
     * @param event {@link Event} to push in {@link DataQueue} to be analysed
     * @throws NullPointerException if event is null
     * @see Event
     * @see DataProducer#dataQueue
     * @see DataProducer#METRICS_LOGGER
     */
    public void push(Event event) {
        Objects.requireNonNull(event);
        long start = System.currentTimeMillis();
        dataQueue.push(event);
        long end = System.currentTimeMillis();
        long result = end - start;
        METRICS_LOGGER.log("time_process_"+event.getSource(), result);
        LOGGER.info("The event {} has been pushed into database.", event.getSource());
    }
}
