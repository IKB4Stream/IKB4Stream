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
                        METRICS_LOGGER.log("event_scored", ""+eventClone.getScore());
                        LOGGER.error(t.getMessage());
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
