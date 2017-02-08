package com.waves_rsp.ikb4stream.consumer;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.model.geojson.Polygon;
import com.mongodb.client.model.geojson.Position;
import com.waves_rsp.ikb4stream.core.communication.DatabaseReaderCallback;
import com.waves_rsp.ikb4stream.core.communication.IDatabaseReader;
import com.waves_rsp.ikb4stream.core.communication.model.Request;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*;

public class DatabaseReader implements IDatabaseReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseReader.class);
    private static final DatabaseReader ourInstance = new DatabaseReader();
    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;
    private final MongoCollection<Document> mongoCollection;

    private DatabaseReader() {
        PropertiesManager propertiesManager = PropertiesManager.getInstance();

        /* Get information about Database */
        String host = propertiesManager.getProperty("database.host");
        String datasource = propertiesManager.getProperty("database.datasource");
        String collection = propertiesManager.getProperty("database.collection");

        if (host == null || datasource == null || collection == null) {
            LOGGER.error("DatabaseReader error cannot get database information");
            throw new IllegalStateException("Configuration file doesn't have any information about database");
        }

        this.mongoClient = MongoClients.create(host);
        this.mongoDatabase = mongoClient.getDatabase(datasource);
        this.mongoCollection = mongoDatabase.getCollection(collection);

        LOGGER.info("DatabaseReader has been instantiate");
    }

    /**
     * Start a connection to the MongoDB Server
     * @return DatabaseWriter
     */
    public static DatabaseReader getInstance() {
        return ourInstance;
    }

    /**
     * Get the result of an event which intersects the bbox
     * @param callback
     * @return
     */
    @Override
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
}