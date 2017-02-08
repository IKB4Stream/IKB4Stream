package com.waves_rsp.ikb4stream.producer.datasource;

import com.waves_rsp.ikb4stream.core.datasource.IDataProducer;
import com.waves_rsp.ikb4stream.core.model.Event;

/**
 *This class receives data (Event) from connectors and push in DataQueue
 * @see DataQueue
 */
public class DataProducer implements IDataProducer {
    private final DataQueue dataQueue;

    /**
     * The constructor with a DataQueue in param
     * @param dataQueue
     */
    public DataProducer(DataQueue dataQueue) {
        this.dataQueue = dataQueue;
    }

    /**
     * Push an event into DataQueue
     * @param event Event to push in DataQueue to be analysed
     */
    public void push(Event event) {
        dataQueue.push(event);
    }

}
