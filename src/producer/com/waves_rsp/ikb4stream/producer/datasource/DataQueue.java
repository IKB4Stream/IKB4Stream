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
 * This class stores and provides data (Event) for DataConsumer
 * @see DataConsumer
 */
class DataQueue {

	    private class PackagedEvent{
        private final Event event;
        private final long arrivedTime;

        private PackagedEvent(Event event, long arrivedTime) {
            Objects.requireNonNull(event);
            Objects.requireNonNull(arrivedTime);
            this.event = event;
            this.arrivedTime = arrivedTime;
        }
    }

    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(DataQueue.class);
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    private static final Logger LOGGER = LoggerFactory.getLogger(DataQueue.class);
    private final BlockingQueue<PackagedEvent> queue;
    private final int size;

    public DataQueue() {
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
     * Push a new event
     * @param event you want to add
     * @throws NullPointerException if {@param event} is null
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
     * Return the last event (Blocking)
     * @return Event
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
     */
    public boolean isEmpty() {
        return queue.remainingCapacity() == size;
    }
}
