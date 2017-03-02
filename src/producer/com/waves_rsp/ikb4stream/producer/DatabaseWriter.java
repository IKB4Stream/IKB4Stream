/*
 * Copyright (C) 2017 ikb4stream team
 * ikb4stream is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * ikb4stream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

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
import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
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
 *
 * @author ikb4stream
 * @version 1.0
 */
public class DatabaseWriter {
    /**
     * Properties of this class
     *
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(DatabaseWriter.class);
    /**
     * Object to add metrics from this class
     *
     * @see DatabaseWriter#insertEvent(Event, DatabaseWriterCallback)
     * @see MetricsLogger#getMetricsLogger()
     * @see MetricsLogger#log(String, long)
     */
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    /**
     * Logger used to log all information in this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseWriter.class);
    /**
     * Single instance of {@link DatabaseWriter}
     *
     * @see DatabaseWriter#getInstance()
     */
    private static final DatabaseWriter DATABASE_WRITER = new DatabaseWriter();
    /**
     * Mongo collection containing {@link Event}
     *
     * @see DatabaseWriter#insertEvent(Event, DatabaseWriterCallback)
     */
    private final MongoCollection<Document> mongoCollection;
    /**
     * Constant value {@value LOCATION_FIELD}
     *
     * @see DatabaseWriter#insertEvent(Event, DatabaseWriterCallback)
     */
    private static final String LOCATION_FIELD = "location";
    /**
     * Mapper to read JSON
     *
     * @see DatabaseWriter#insertEvent(Event, DatabaseWriterCallback)
     */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * DataWriter constructor
     *
     * @throws IllegalStateException if database configuration is not set
     */
    private DatabaseWriter() {
        try {
            final MongoClient mongoClient = MongoClients.create(PROPERTIES_MANAGER.getProperty("database.host"));
            final MongoDatabase mongoDatabase = mongoClient.getDatabase(PROPERTIES_MANAGER.getProperty("database.datasource"));
            this.mongoCollection = mongoDatabase.getCollection(PROPERTIES_MANAGER.getProperty("database.collection"));
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
        LOGGER.info("DatabaseWriter has been instantiate");
    }

    /**
     * Return an instance of {@link DatabaseWriter}
     *
     * @return Single instance of {@link DatabaseWriter}
     * @see DatabaseWriter#DATABASE_WRITER
     */
    public static DatabaseWriter getInstance() {
        return DATABASE_WRITER;
    }

    /**
     * This method inserts an {@link Event} in the database
     *
     * @param event    {@link Event} to insert into database
     * @param callback {@link DatabaseWriterCallback} called after inserting
     * @throws NullPointerException    if event or callback is null
     * @see DatabaseWriter#mongoCollection
     * @see DatabaseWriter#LOCATION_FIELD
     * @see DatabaseWriter#METRICS_LOGGER
     * @see DatabaseWriter#mapper
     */
    public void insertEvent(Event event, DatabaseWriterCallback callback) {
        Objects.requireNonNull(event);
        Objects.requireNonNull(callback);
        try {
            long start = System.currentTimeMillis();
            Document document = Document.parse(mapper.writeValueAsString(event));
            document.remove("start");
            document.remove("end");
            document.remove(LOCATION_FIELD);
            document.append("start", event.getStart().getTime());
            document.append("end", event.getEnd().getTime());
            if (event.getLocation().length == 1) {
                document.append(LOCATION_FIELD, new Point(new Position(
                        event.getLocation()[0].getLongitude(), event.getLocation()[0].getLatitude())));
            } else {
                List<Position> positions = Arrays.stream(event.getLocation())
                        .map(p -> new Position(p.getLongitude(), p.getLatitude())).collect(Collectors.toList());
                document.append(LOCATION_FIELD, new Polygon(positions));
            }
            this.mongoCollection.insertOne(document, (result, t) -> callback.onResult(t));
            long time = System.currentTimeMillis() - start;
            METRICS_LOGGER.log("time_dbwriter_" + event.getSource(), time);
        } catch (JsonProcessingException e) {
            LOGGER.error("Invalid event format: event not inserted in database.");
        }
    }
}
