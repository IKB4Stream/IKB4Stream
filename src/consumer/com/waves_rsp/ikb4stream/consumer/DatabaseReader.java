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
    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;
    private final MongoCollection<Document> mongoCollection;

    /**
     * The constructor of DatabaseReader
     * This class is a singleton
     */
    private DatabaseReader() {
        checkConfiguration();
        this.mongoClient = MongoClients.create(PROPERTIES_MANAGER.getProperty("database.host"));
        this.mongoDatabase = mongoClient.getDatabase(PROPERTIES_MANAGER.getProperty("database.datasource"));
        this.mongoCollection = mongoDatabase.getCollection(PROPERTIES_MANAGER.getProperty("database.collection"));
        LOGGER.info("DatabaseReader has been instantiate");
    }

    /**
     * Check configuration of Database
     * @throws IllegalStateException if database configuration is not set
     */
    private static void checkConfiguration() {
        if (PROPERTIES_MANAGER.getProperty("database.host") == null) {
            LOGGER.error("DatabaseReader error cannot get database.host information");
            throw new IllegalStateException("Configuration file doesn't have database.host information");
        }
        if (PROPERTIES_MANAGER.getProperty("database.datasource") == null) {
            LOGGER.error("DatabaseReader error cannot get database.datasource information");
            throw new IllegalStateException("Configuration file doesn't have database.datasource information");
        }
        if (PROPERTIES_MANAGER.getProperty("database.collection") == null) {
            LOGGER.error("DatabaseReader error cannot get database.collection information");
            throw new IllegalStateException("Configuration file doesn't have database.collection information");
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
     * @param callback
     * @return nothing but the result is store in a ArrayList
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