package com.waves_rsp.ikb4stream.producer.datasource;

import com.waves_rsp.ikb4stream.core.model.Event;

import java.util.ArrayDeque;
import java.util.Objects;

/**
 * This class stores and provides data (Event) for DataConsumer
 * @see DataConsumer
 */
public class DataQueue {
    private final ArrayDeque<Event> queue = new ArrayDeque<>();
    private final Object key = new Object();

    /**
     * Push a new event
     * @param event you want to add
     */
    public void push(Event event) {
        Objects.requireNonNull(event);

        synchronized (key) {
            queue.add(event);
            key.notifyAll();
        }
    }

    /**
     * Return the last event (Blocking)
     * @return Event
     */
    public Event pop() throws InterruptedException {
        synchronized (key) {
            while (queue.isEmpty()) { key.wait(); }
            return queue.removeFirst();
        }
    }

    /**
     * @return Return true if the DataQueue is empty
     */
    public boolean isEmpty() {
        synchronized (key) {
            return queue.isEmpty();
        }
    }
}
