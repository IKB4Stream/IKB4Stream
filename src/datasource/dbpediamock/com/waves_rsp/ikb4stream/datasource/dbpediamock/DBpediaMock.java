package com.waves_rsp.ikb4stream.datasource.dbpediamock;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.core.util.IProducerConnectorMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class DBpediaMock implements IProducerConnectorMock {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBpediaMock.class);
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(DBpediaMock.class, "resources/datasource/dbpediamock/config.properties");
    private static final String VALUE = "value";
    private final String lang;
    private final long sleepTime;
    private final InputStream inputStream;
    private final String source;

    public DBpediaMock() {
        try {
            this.source = PROPERTIES_MANAGER.getProperty("dbpediamock.source");
            this.lang = PROPERTIES_MANAGER.getProperty("dbpediamock.language");
            sleepTime = Long.valueOf(PROPERTIES_MANAGER.getProperty("dbpediamock.sleep_time"));
            this.inputStream = new FileInputStream(PROPERTIES_MANAGER.getProperty("dbpediamock.path"));
        }catch (IllegalArgumentException | IOException e) {
            LOGGER.error("Bad properties loaded: {}", e);
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        load(dataProducer, PROPERTIES_MANAGER, "dbpediamock.path");
    }

    /**
     * Check if the current parser is valid in order to parse the json from dbpedia sources files.
     * Then create a valid event object and push into the dataProducer object with pushIfValidEvent() method.
     *
     * @param mapper
     * @param parser
     * @param dataProducer
     * @param start
     * @throws IOException if the current json or parser is invalid
     */
    private void checkParser(ObjectMapper mapper, JsonParser parser, IDataProducer dataProducer, long start) throws IOException {
        if (parser != null) {
            while ((parser.nextToken()) == JsonToken.START_OBJECT) {
                ObjectNode objectNode = mapper.readTree(parser);
                Event event = getEventFromJson(objectNode);
                pushIfValidEvent(dataProducer, event, start);
            }
        }
    }

    /**
     * get an Event object from an ObjectNode if it's possible
     *
     * @param objectNode
     * @return a valid Event
     * @throws ParseException {@param objectNode} if the current objectNode with a json cannot be parsed
     */
    public Event getEventFromJson(ObjectNode objectNode) {
        Date startDate;
        Date endDate;

        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd+hh:mm");
            JsonNode startNode = objectNode.findValue("startDate").findValue(VALUE);
            JsonNode endNode = objectNode.findValue("endDate").findValue(VALUE);
            JsonNode descriptionNode = objectNode.findValue("description").findValue(VALUE);
            JsonNode langNode = objectNode.findValue("xml:lang");
            LatLong latLong = jsonToLatLong(objectNode);

            if(checkValidData(startNode, endNode, descriptionNode, latLong, langNode)) {
                String start = startNode.asText();
                String end = endNode.asText();
                startDate = dateFormat.parse(start);
                endDate = dateFormat.parse(end);
                String description = descriptionNode.asText();
                return new Event(latLong, startDate, endDate, description, source);
            }
        }catch (ParseException e) {
            LOGGER.error("Invalid fields found: {}", e);
        }

        return null;
    }

    /**
     * Check if the json nodes and latlong objects are valid
     *
     * @param startNode
     * @param endNode
     * @param descriptionNode
     * @param latLong
     * @param langNode
     * @return
     */
    private boolean checkValidData(JsonNode startNode, JsonNode endNode, JsonNode descriptionNode, LatLong latLong, JsonNode langNode) {
        return startNode != null && endNode != null && descriptionNode != null
                &&  lang.equals(langNode.asText()) && latLong != null;
    }

    /**
     * Parse a json node object in order to create a valid latlong object
     *
     * @param objectNode
     * @return a valid LatLong
     */
    private static LatLong jsonToLatLong(JsonNode objectNode) {
        JsonNode latitudeNode = objectNode.findValue("latitude");
        JsonNode longitudeNode = objectNode.findValue("longitude");
        if(latitudeNode != null && longitudeNode != null) {
            double latitude = latitudeNode.findValue(VALUE).asDouble();
            double longitude = longitudeNode.findValue(VALUE).asDouble();
            return new LatLong(latitude, longitude);
        }

        return null;
    }

    /**
     * Push a valid event into the dataProducer object
     *
     * @param dataProducer
     * @param event
     * @param start the time at the beginning of the processing
     */
    private void pushIfValidEvent(IDataProducer dataProducer, Event event, long start) {
        if(event != null) {
            long end = System.currentTimeMillis();
            long result = end - start;
            dataProducer.push(event);
            METRICS_LOGGER.log("time_process_"+this.source, result);
        }else {
            LOGGER.error("An event was discard (missing field)");
        }
    }

    @Override
    public boolean isActive() {
        try {
            return Boolean.valueOf(PROPERTIES_MANAGER.getProperty("dbpediamock.enable"));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Can't determine if the current datasource is enable: {}", e);
            return true;
        }
    }
}
