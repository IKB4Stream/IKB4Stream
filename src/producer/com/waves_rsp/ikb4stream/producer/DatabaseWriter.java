package com.waves_rsp.ikb4stream.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.producer.model.DatabaseWriterCallback;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class writes data in mongodb database
 */
public class DatabaseWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseWriter.class);
    private static final DatabaseWriter ourInsance = new DatabaseWriter();
    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;
    private final MongoCollection<Document> mongoCollection;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * DataWriter constructor
     */
    private DatabaseWriter() {
        PropertiesManager propertiesManager = PropertiesManager.getInstance(DatabaseWriter.class, "resources/config.properties");

        /* Get information about database */
        String host = propertiesManager.getProperty("database.host");
        String datasource = propertiesManager.getProperty("database.datasource");
        String collection = propertiesManager.getProperty("database.collection");

        if (host == null || datasource == null || collection == null) {
            LOGGER.error("DatabaseWriter error cannot get database information");
            throw new IllegalStateException("Configuration file doesn't have any information about database");
        }

        this.mongoClient = MongoClients.create(host);
        this.mongoDatabase = mongoClient.getDatabase(datasource);
        this.mongoCollection = mongoDatabase.getCollection(collection);

        LOGGER.info("DatabaseWriter has been instantiate");
    }

    /**
     * Return an intance of {@link DatabaseWriter}
     * @return DatabaseWriter
     */
    public static DatabaseWriter getInstance() {
        return ourInsance;
    }

    /**
     * This method inserts an event in the database
     * @param event an event
     * @param callback a functional interface
     * @throws JsonProcessingException in case of problem during inserting
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
}
