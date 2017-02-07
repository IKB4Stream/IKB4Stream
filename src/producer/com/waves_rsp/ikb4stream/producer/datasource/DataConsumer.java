package com.waves_rsp.ikb4stream.producer.datasource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.producer.DatabaseWriter;
import com.waves_rsp.ikb4stream.producer.score.ScoreProcessorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataConsumer {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ScoreProcessorManager scoreProcessorManager;
    private final DatabaseWriter databaseWriter;
    private final DataQueue dataQueue;
    private final int targetScore;


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
        String stringTargetScore = PropertiesManager.getInstance().getProperty("score.target");
        if (stringTargetScore != null) {
            targetScore = Integer.valueOf(stringTargetScore);
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
        return event.getScore() >= score;
    }

    /**
     * Consume Event in dataQueue and send to scoreProcessor
     */
    public void consume() {
        try {
            while (Thread.currentThread().isInterrupted()) {
                Event event = dataQueue.pop();
                Event eventClone = scoreProcessorManager.processScore(event);
                if (filter(eventClone, targetScore)) {
                    databaseWriter.insertEvent(eventClone, t -> logger.error("DatabaseWriter error {}", t.getMessage()));
                }
            }
        } catch (JsonProcessingException | InterruptedException e) {
            logger.error("DatabaseConsumer error {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
