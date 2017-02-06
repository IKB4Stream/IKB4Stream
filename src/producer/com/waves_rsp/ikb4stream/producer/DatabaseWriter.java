package com.waves_rsp.ikb4stream.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.producer.model.DatabaseWriterCallback;
import org.bson.Document;

public class DatabaseWriter {
    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;
    private final MongoCollection<Document> mongoCollection;
    private final ObjectMapper mapper = new ObjectMapper();

    private DatabaseWriter(MongoClient mongoClient, MongoDatabase mongoDatabase, MongoCollection mongoCollection) {
        this.mongoClient = mongoClient;
        this.mongoDatabase = mongoDatabase;
        this.mongoCollection = mongoCollection;
    }

    /**
     * Start a connection to the MongoDB Server
     * @param connectionString the MongoDB Connection String
     * @param databaseName the MongoDB database Name
     * @param collectionName the MongoDB collection Name
     * @return DatabaseWriter
     */
    public static DatabaseWriter connect(String connectionString, String databaseName, String collectionName) {
        MongoClient mongoClient = MongoClients.create(connectionString);
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
        MongoCollection mongoCollection = mongoDatabase.getCollection(collectionName);
        return new DatabaseWriter(mongoClient, mongoDatabase, mongoCollection);
    }

    /**
     * Insert One Event to the Database
     * @param event
     */
    public void insertEvent(Event event, DatabaseWriterCallback callback) throws JsonProcessingException {
        this.mongoCollection.insertOne(Document.parse(mapper.writeValueAsString(event)),
                (result, t) -> callback.onResult(t));
    }

}
