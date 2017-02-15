package com.waves_rsp.ikb4stream.datasource.twittermock;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;


public class TwitterMock implements IProducerConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterMock.class);
    private final InputStream inputStream;

    private TwitterMock(InputStream inputStream) {
        Objects.requireNonNull(inputStream);
        this.inputStream = inputStream;
    }

    public static TwitterMock getInstance(InputStream inputStream) {
        return new TwitterMock(inputStream);
    }

    /**
     * Load data registered into a json twitter file and parse them to create event
     *
     * @param dataProducer
     */
    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser;

        try {
            parser = mapper.getFactory().createParser(this.inputStream);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return;
        }

        while(!Thread.interrupted()) {
            try {
                while(parser.nextToken() == JsonToken.START_OBJECT) {
                    ObjectNode objectNode = mapper.readTree(parser);
                    Event event = getEventFromJson(objectNode);
                    pushIfValidEvent(dataProducer, event);
                }
            } catch (IOException e) {
                LOGGER.error("something went wrong with the tweet reading");
                return;
            }finally {
                Thread.currentThread().interrupt();
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }
    }

    /**
     * Parse an object node in order to create an Event object
     *
     * @param objectNode
     * @return null if an IllegalArgumentException or NullPointerException has been thrown, else the Event object created
     */
    private static Event getEventFromJson(ObjectNode objectNode) {
        Date startDate = Date.from(Instant.ofEpochMilli(objectNode.findValue("timestamp_ms").asLong()));
        Date endDate = Date.from(Instant.now());
        String description;
        String source = "Twitter";
        JsonNode jsonNode = objectNode.findValue("place");
        JsonNode jsonCoordinates = jsonNode.findValue("coordinates");
        description = objectNode.findValue("text").toString();
        LatLong latLong = jsonToLatLong(jsonCoordinates);
        try {
            return new Event(latLong, startDate, endDate, description, source);
        }catch (IllegalArgumentException | NullPointerException err) {
            LOGGER.error(err.getMessage());
            return null;
        }
    }

    /**
     * Push a valid event into the data producer object
     *
     * @param dataProducer
     * @param event
     */
    private void pushIfValidEvent(IDataProducer dataProducer, Event event) {
        if(event != null) {
            dataProducer.push(event);
            LOGGER.info("Event "+event.toString()+" was correctly pushed");
        }else {
            LOGGER.error("An event was discard (missing field)");
        }
    }

    /**
     * Create LatLong from a jsonNode object and parse it to get GPS coordinates
     *
     * @param jsonCoordinates
     * @return latlong object
     */
    private static LatLong jsonToLatLong(JsonNode jsonCoordinates) {
        JsonNode main = jsonCoordinates.elements().next();
        JsonNode coords = main.elements().next();
        Iterator<JsonNode> characteristics = coords.elements();
        double latitude = characteristics.next().asDouble();
        double longitude = characteristics.next().asDouble();
        return new LatLong(latitude, longitude);
    }
}
