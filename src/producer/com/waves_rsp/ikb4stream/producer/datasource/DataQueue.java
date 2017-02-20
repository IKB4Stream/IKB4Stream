package com.waves_rsp.ikb4stream.producer.datasource;

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
public class DataQueue {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(DataQueue.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(DataQueue.class);
    private final BlockingQueue<Event> queue;
    private final int size;

    public DataQueue() {
        int size = 500;
        try {
            size = Integer.parseInt(PROPERTIES_MANAGER.getProperty("producer.sizequeue"));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(e.getMessage());
        }
        this.queue = new ArrayBlockingQueue<>(size);
        this.size = size;
    }

    /**
     * Push a new event
     * @param event you want to add
     * @throws NullPointerException if {@param event} is null
     */
    public void push(Event event) {
        Objects.requireNonNull(event);
        boolean inserted = queue.offer(event);
        if (!inserted) {
            LOGGER.warn(event + " cannot be push");
            // TODO: Add metrics
        }
    }

    /**
     * Return the last event (Blocking)
     * @return Event
     */
    public Event pop() throws InterruptedException {
        return queue.take();
    }

    /**
     * @return Return true if the DataQueue is empty
     */
    public boolean isEmpty() {
        return queue.remainingCapacity() == size;
    }
}
