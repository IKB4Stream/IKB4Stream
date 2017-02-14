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

/**
 * DatabaseReader class reads data (Events) from mongodb database
 */
public class DatabaseReader implements IDatabaseReader {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(DatabaseReader.class, "resources/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseReader.class);
    private static final DatabaseReader DATABASE_READER = new DatabaseReader();
    private final MongoCollection<Document> mongoCollection;
    private final MongoDatabase mongoDatabase;
    private final MongoClient mongoClient;
    private final int limit;

    /**
     * The constructor of DatabaseReader
     * This class is a singleton
     */
    private DatabaseReader() {
        checkConfiguration();
        this.mongoClient = MongoClients.create(PROPERTIES_MANAGER.getProperty("database.host"));
        this.mongoDatabase = mongoClient.getDatabase(PROPERTIES_MANAGER.getProperty("database.datasource"));
        this.mongoCollection = mongoDatabase.getCollection(PROPERTIES_MANAGER.getProperty("database.collection"));
        int tmp = 50000;
        try {
            tmp = Integer.parseInt(PROPERTIES_MANAGER.getProperty("database.limit"));
        } catch (IllegalArgumentException e) {
            // NumberFormatException is a subclass of IllegalArgumentException
            LOGGER.warn("Use default database.limit");
        }
        this.limit = tmp;
        LOGGER.info("DatabaseReader has been instantiate");
    }

    /**
     * Check configuration of Database
     * @throws IllegalStateException if database configuration is not set
     */
    private static void checkConfiguration() {
        try {
            PROPERTIES_MANAGER.getProperty("database.host");
            PROPERTIES_MANAGER.getProperty("database.datasource");
            PROPERTIES_MANAGER.getProperty("database.collection");
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * Get instance of Singleton DatabaseReader
     * @return an instance of DatabaseReader
     */
    public static DatabaseReader getInstance() {
        return DATABASE_READER;
    }

    /**
     * This method requests events from mongodb database and filters from data coming to the request object in parameter
     * @param request Request to apply to Mongo
     * @param callback Callback method call after select operation
     * @return nothing but the result is store in a ArrayList
     */
    @Override
    public void getEvent(Request request, DatabaseReaderCallback callback) {
        List<Position> polygon = Arrays.stream(request.getBoundingBox().getLatLongs())
                .map(l -> new Position(l.getLatitude(), l.getLongitude()))
                .collect(Collectors.toList());

        this.mongoCollection
                .find(and(
                        geoIntersects("location", new Polygon(polygon)),
                        gte("start", request.getStart().getTime()),
                        lte("end", request.getEnd().getTime())
                ))
                .limit(limit)
                .into(new ArrayList<Document>(),
                        (result, t) -> callback.onResult(
                                t,
                                "[" + result.stream().map(d -> d.toJson()).collect(Collectors.joining(", ")) + "]"
                        ));
    }
}