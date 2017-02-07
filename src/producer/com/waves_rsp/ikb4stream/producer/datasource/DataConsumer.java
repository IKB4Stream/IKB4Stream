package com.waves_rsp.ikb4stream.producer.datasource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.producer.DatabaseWriter;
import com.waves_rsp.ikb4stream.producer.model.DatabaseWriterCallback;
import com.waves_rsp.ikb4stream.producer.score.ScoreProcessorManager;

public class DataConsumer {
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
    public static DataConsumer createDataConsumer() {
        /* Get properties */
        PropertiesManager propertiesManager = PropertiesManager.getInstance();

        /* Create ScoreProcessorManager */
        ScoreProcessorManager scoreProcessorManager = new ScoreProcessorManager();

        /* Create DatabaseWriter */
        String host = propertiesManager.getProperty("database.host");
        String datasource = propertiesManager.getProperty("database.datasource");
        String collection = propertiesManager.getProperty("database.collection");
        if (host == null || datasource == null || collection == null) {
            throw new IllegalStateException("Configuration file doesn't have any information about database");
        }
        DatabaseWriter databaseWriter = DatabaseWriter.connect(host, datasource, collection);

        /* Create DataQueue */
        DataQueue dataQueue = new DataQueue();

        /* Get target score */
        int targetScore = Integer.valueOf(propertiesManager.getProperty("score.target"));

        return new DataConsumer(scoreProcessorManager, databaseWriter, dataQueue, targetScore);
    }


    private boolean filter(Event event, int score) {
        return event.getScore() >= score;
    }

    /**
     * Consume Event in dataQueue and send to scoreProcessor
     * @param callback Method call after insert
     */
    public void consume(DatabaseWriterCallback callback) {
        try {
            while (true) {
                Event event = dataQueue.pop();
                scoreProcessorManager.processScore(event);
                if (filter(event, targetScore)) {
                    databaseWriter.insertEvent(event, callback);
                }
            }
        } catch (JsonProcessingException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
