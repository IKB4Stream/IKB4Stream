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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This class stores and provides {@link Event} for {@link DataConsumer} and {@link DataProducer}
 *
 * @author ikb4stream
 * @version 1.0
 */
class DataQueue {
    /**
     * Properties of this class
     *
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(DataQueue.class);
    /**
     * Object to add metrics from this class
     *
     * @see DataProducer#push(Event)
     * @see MetricsLogger#log(String, long)
     * @see MetricsLogger#getMetricsLogger()
     */
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    /**
     * Logger used to log all information in this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataQueue.class);
    /**
     * Single instance of {@link DataQueue}
     *
     * @see DataQueue#createDataQueue()
     */
    private static final DataQueue DATA_QUEUE = new DataQueue();
    /**
     * {@link Event} will be push in this {@link DataQueue#queue}
     *
     * @see DataQueue#push(Event)
     * @see DataQueue#isEmpty()
     * @see DataQueue#pop()
     */
    private final BlockingQueue<PackagedEvent> queue;
    /**
     * Size of {@link DataQueue#queue}
     *
     * @see DataQueue#isEmpty()
     */
    private final int size;

    /**
     * Private constructor to block instantiation, use {@link DataQueue#createDataQueue()} instead
     */
    private DataQueue() {
        int defaultSize = 500;
        try {
            defaultSize = Integer.parseInt(PROPERTIES_MANAGER.getProperty("producer.sizequeue"));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(e.getMessage());
        }
        this.queue = new ArrayBlockingQueue<>(defaultSize);
        this.size = defaultSize;
    }

    /**
     * Singleton of {@link DataQueue}
     *
     * @return Single instance {@link DataQueue}
     * @see DataQueue#DATA_QUEUE
     */
    public static DataQueue createDataQueue() {
        return DATA_QUEUE;
    }

    /**
     * Push a new {@link Event}, if {@link DataQueue#queue} is full the event is ignored
     *
     * @param event {@link Event} to push in this {@link DataQueue}
     * @throws NullPointerException if event is null
     * @see DataQueue#METRICS_LOGGER
     * @see DataQueue#queue
     */
    public void push(Event event) {
        Objects.requireNonNull(event);
        long arrivedTime = System.currentTimeMillis();
        boolean inserted = queue.offer(new PackagedEvent(event, arrivedTime));
        if (!inserted) {
            METRICS_LOGGER.log("event_dropped_fullqueue", event.getSource());
            LOGGER.warn(event + " cannot be push");
        }
    }

    /**
     * Return the first {@link Event} in {@link DataQueue#queue}
     *
     * @return {@link Event} in {@link DataQueue}
     * @see DataQueue#METRICS_LOGGER
     * @see DataQueue#queue
     * @see PackagedEvent
     * @see Event
     */
    public Event pop() throws InterruptedException {
        PackagedEvent packEvent = queue.take();
        Event popEvent = packEvent.event;
        long time = System.currentTimeMillis() - packEvent.arrivedTime;
        METRICS_LOGGER.log("life_in_queue_" + popEvent.getSource(), time);
        return popEvent;
    }

    /**
     * @return Return true if the DataQueue is empty
     * @see DataQueue#queue
     * @see DataQueue#size
     */
    public boolean isEmpty() {
        return queue.remainingCapacity() == size;
    }

    /**
     * Packaged {@link Event} to apply metrics in {@link DataQueue#queue}
     *
     * @author ikb4stream
     * @version 1.0
     */
    private class PackagedEvent {
        /**
         * Arrival time in {@link DataQueue#queue}
         *
         * @see DataQueue#push(Event)
         * @see DataQueue#pop()
         */
        private final long arrivedTime;
        /**
         * {@link Event} to package
         *
         * @see DataQueue#push(Event)
         * @see DataQueue#pop()
         */
        private final Event event;

        /**
         * Create a {@link PackagedEvent} with {@link Event}
         *
         * @param event       {@link Event} to package before insertion in {@link DataQueue#queue}
         * @param arrivedTime Arrival time in {@link DataQueue#queue}
         * @throws NullPointerException if event or arrivedTime is null
         * @see Event
         */
        private PackagedEvent(Event event, long arrivedTime) {
            Objects.requireNonNull(event);
            Objects.requireNonNull(arrivedTime);
            this.event = event;
            this.arrivedTime = arrivedTime;
        }
    }
}
