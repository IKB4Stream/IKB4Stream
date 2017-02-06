package com.waves_rsp.ikb4stream.consumer;

import com.mongodb.Block;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.model.geojson.Polygon;
import com.mongodb.client.model.geojson.Position;
import com.waves_rsp.ikb4stream.consumer.model.BoundingBox;
import com.waves_rsp.ikb4stream.consumer.model.Request;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import org.bson.Document;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.geoIntersects;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.lte;

public class DatabaseReader {

    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;
    private final MongoCollection<Document> mongoCollection;

    private DatabaseReader(MongoClient mongoClient, MongoDatabase mongoDatabase, MongoCollection<Document> mongoCollection) {
        this.mongoClient = mongoClient;
        this.mongoDatabase = mongoDatabase;
        this.mongoCollection = mongoCollection;
    }

    /**
     * Start a connection to the MongoDB Server
     *
     * @param connectionString the MongoDB Connection String
     * @param databaseName     the MongoDB database Name
     * @param collectionName   the MongoDB collection Name
     * @return DatabaseWriter
     */
    public static DatabaseReader connect(String connectionString, String databaseName, String collectionName) {
        MongoClient mongoClient = MongoClients.create(connectionString);
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);
        return new DatabaseReader(mongoClient, mongoDatabase, mongoCollection);
    }

    /**
     * Get the result of an event which intersects the bbox
     * @param callback
     * @return
     */
    public void getEvent(Request request, DatabaseReaderCallback callback) {
        List<Position> polygon = Arrays.stream(request.getBoundingBox().getLatLongs())
                .map(l -> new Position(l.getLatitude(), l.getLongitude()))
                .collect(Collectors.toList());

        this.mongoCollection.find(and(
            geoIntersects("location", new Polygon(polygon)),
            gte("start", request.getStart().getTime()),
            lte("end", request.getEnd().getTime())
        )).into(new ArrayList<Document>(),
                (result, t) -> callback.onResult(
                        t,
                        "[" + result.stream().map(d -> d.toJson()).collect(Collectors.joining(", ")) + "]"
                ));
    }

    public static void main(String[] args) {
        DatabaseReader dbr = DatabaseReader.connect("mongodb://localhost", "ikb4stream", "test");

        BoundingBox boundingBox = new BoundingBox(new LatLong[]{
            new LatLong(0,0),
            new LatLong(0,2),
            new LatLong(2,2),
            new LatLong(2,0),
            new LatLong(0,0)
        });
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

        Date now = Date.from(Instant.now());

        Request r = new Request(start, end, boundingBox, now);

        dbr.getEvent(r, (t, result) -> {
            System.out.println(result);
        });


        while(true){}
    }
}