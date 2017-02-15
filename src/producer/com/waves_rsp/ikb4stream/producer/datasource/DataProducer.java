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
    private final DataQueue dataQueue;
    private static final Logger LOGGER = LoggerFactory.getLogger(DataProducer.class);
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();

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
        METRICS_LOGGER.log("event_source", event.getSource());
        LOGGER.info("the event "+event.getSource()+" has been pushed into database.");
        dataQueue.push(event);
    }
}
