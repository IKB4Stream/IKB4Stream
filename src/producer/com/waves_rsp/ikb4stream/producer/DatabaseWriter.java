package com.waves_rsp.ikb4stream.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Polygon;
import com.mongodb.client.model.geojson.Position;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.producer.model.DatabaseWriterCallback;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class writes data in mongodb database
 */
public class DatabaseWriter {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(DatabaseWriter.class, "resources/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseWriter.class);
    private static final DatabaseWriter DATABASE_WRITER = new DatabaseWriter();
    private final MongoCollection<Document> mongoCollection;
    private final ObjectMapper mapper = new ObjectMapper();
    private final MongoDatabase mongoDatabase;
    private final MongoClient mongoClient;

    /**
     * DataWriter constructor
     */
    private DatabaseWriter() {
        checkConfiguration();
        this.mongoClient = MongoClients.create(PROPERTIES_MANAGER.getProperty("database.host"));
        this.mongoDatabase = mongoClient.getDatabase(PROPERTIES_MANAGER.getProperty("database.datasource"));
        this.mongoCollection = mongoDatabase.getCollection(PROPERTIES_MANAGER.getProperty("database.collection"));
        LOGGER.info("DatabaseWriter has been instantiate");
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
     * Return an intance of {@link DatabaseWriter}
     * @return DatabaseWriter
     */
    public static DatabaseWriter getInstance() {
        return DATABASE_WRITER;
    }

    /**
     * This method inserts an event in the database
     * @param event an event
     * @param callback a functional interface
     * @throws JsonProcessingException in case of problem during inserting
     * @throws NullPointerException if {@param event} or {@param callback} is null
     */
    public void insertEvent(Event event, DatabaseWriterCallback callback) {
        Objects.requireNonNull(event);
        Objects.requireNonNull(callback);
        try {
            Document document = Document.parse(mapper.writeValueAsString(event));
            document.remove("start");
            document.remove("end");
            document.remove("location");
            document.append("start", event.getStart().getTime());
            document.append("end", event.getEnd().getTime());
            if(event.getLocation().length == 1) {
                document.append("location", new Point(new Position(
                        event.getLocation()[0].getLatitude(), event.getLocation()[0].getLongitude())));
            } else {
                List<Position> positions = Arrays.asList(event.getLocation()).stream()
                        .map(p -> new Position(p.getLatitude(), p.getLongitude())).collect(Collectors.toList());
                document.append("location", new Polygon(positions));
            }
            this.mongoCollection.insertOne(document, (result, t) -> callback.onResult(t));
        } catch (JsonProcessingException e) {
            LOGGER.error("Invalid event format: event not inserted in database.");
        }
    }
}
