package com.waves_rsp.ikb4stream.producer.datasource;

import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.producer.DatabaseWriter;
import com.waves_rsp.ikb4stream.producer.score.ScoreProcessorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataConsumer.class);
    private final ScoreProcessorManager scoreProcessorManager;
    private final DatabaseWriter databaseWriter;
    private final DataQueue dataQueue;
    private final int targetScore;
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();

    private DataConsumer(ScoreProcessorManager scoreProcessorManager, DatabaseWriter databaseWriter, DataQueue dataQueue, int targetScore) {
        this.scoreProcessorManager = scoreProcessorManager;
        this.databaseWriter = databaseWriter;
        this.dataQueue = dataQueue;
        this.targetScore = targetScore;
    }

    /**
     * Create a DataConsumer
     * @return DataConsumer
     */
    public static DataConsumer createDataConsumer(DataQueue dataQueue) {
        /* Create ScoreProcessorManager */
        ScoreProcessorManager scoreProcessorManager = new ScoreProcessorManager();

        DatabaseWriter databaseWriter = DatabaseWriter.getInstance();

        /* Get target score */
        int targetScore = 25;
        try {
            targetScore = Integer.parseInt(PropertiesManager.getInstance(DataConsumer.class, "resources/config.properties").getProperty("score.target"));
        } catch (NumberFormatException e) {
            LOGGER.warn("score.target is not a number, use default value");
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Use default value for score.target");
        }

        return new DataConsumer(scoreProcessorManager, databaseWriter, dataQueue, targetScore);
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
                Event eventClone = scoreProcessorManager.processScore(event);
                if (filter(eventClone, targetScore)) {
                    databaseWriter.insertEvent(eventClone, t -> {
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
