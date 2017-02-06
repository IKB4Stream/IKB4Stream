package com.waves_rsp.ikb4stream.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.producer.model.DatabaseWriterCallback;
import org.bson.Document;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

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
        Document document = Document.parse(mapper.writeValueAsString(event));

        document.remove("start");
        document.remove("end");
        document.remove("location");
        document.append("start", event.getStart().getTime());
        document.append("end", event.getEnd().getTime());
        document.append("location", new Point(new Position(event.getLocation().getLatitude(), event.getLocation().getLongitude())));

        this.mongoCollection.insertOne(document, (result, t) -> callback.onResult(t));
    }

    public static void main(String[] args) throws JsonProcessingException {
        DatabaseWriter db = DatabaseWriter.connect("mongodb://localhost:27017/", "ikb4stream", "test");

        LatLong latLong = new LatLong(1, 1);
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, 2015);
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.DATE, 15);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date start = calendar.getTime();

        calendar.set(Calendar.YEAR, 2015);
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.DATE, 17);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date end = calendar.getTime();

        Event event = new Event(latLong, start, end, "WaterPony", (byte) 10, "twitter");


        while(true) {
            db.insertEvent(event, t -> System.out.println(t.getMessage()) );
        }
    }

}
