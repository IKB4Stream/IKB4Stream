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
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.producer.model.DatabaseWriterCallback;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

public class DatabaseWriter {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final DatabaseWriter ourInsance = new DatabaseWriter();
    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;
    private final MongoCollection<Document> mongoCollection;
    private final ObjectMapper mapper = new ObjectMapper();

    private DatabaseWriter() {
        PropertiesManager propertiesManager = PropertiesManager.getInstance();

        /* Get information about database */
        String host = propertiesManager.getProperty("database.host");
        String datasource = propertiesManager.getProperty("database.datasource");
        String collection = propertiesManager.getProperty("database.collection");

        if (host == null || datasource == null || collection == null) {
            logger.error("DatabaseWriter error cannot get database information");
            throw new IllegalStateException("Configuration file doesn't have any information about database");
        }

        this.mongoClient = MongoClients.create(host);
        this.mongoDatabase = mongoClient.getDatabase(datasource);
        this.mongoCollection = mongoDatabase.getCollection(collection);

        logger.info("DatabaseWriter info {}", "DatabaseWriter has been instantiate");
    }

    public static DatabaseWriter getInstance() {
        return ourInsance;
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
        DatabaseWriter db = DatabaseWriter.getInstance();

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
