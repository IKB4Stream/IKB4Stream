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

package com.waves_rsp.ikb4stream.datasource.dbpediamock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.waves_rsp.ikb4stream.core.datasource.IProducerConnectorMock;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Mock of {@link com.waves_rsp.ikb4stream.datasource.dbpedia.DBpediaProducerConnector DBpediaProducerConnector}
 * @author ikb4stream
 * @version 1.0
 * @see com.waves_rsp.ikb4stream.core.datasource.IProducerConnectorMock
 * @see com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector
 */
public class DBpediaMock implements IProducerConnectorMock {
    /**
     * Properties of this module
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class, String)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(DBpediaMock.class, "resources/datasource/dbpediamock/config.properties");
    /**
     * Constant value {@value VALUE}
     * Key in json result
     */
    private static final String VALUE = "value";
    /**
     * Source name of corresponding {@link Event}
     * @see DBpediaMock#getEventFromJson(ObjectNode)
     */
    private final String source;
    /**
     * Language of data receive
     * @see DBpediaMock#checkValidData(JsonNode, JsonNode, JsonNode, LatLong, JsonNode)
     */
    private final String lang;

    /**
     * Default constructor that init {@link DBpediaMock#source} and {@link DBpediaMock#lang}
     */
    public DBpediaMock() {
        try {
            this.source = PROPERTIES_MANAGER.getProperty("dbpediamock.source");
            this.lang = PROPERTIES_MANAGER.getProperty("dbpediamock.language");
        }catch (IllegalArgumentException e) {
            LOGGER.error("Bad properties loaded: {}", e);
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * Common method of {@link com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor IScoreProcessor} called
     * by {@link com.waves_rsp.ikb4stream.producer.datasource.ProducerManager ProducerManager}
     * @param dataProducer {@link IDataProducer} contains the data queue
     * @see DBpediaMock#PROPERTIES_MANAGER
     */
    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        load(dataProducer, PROPERTIES_MANAGER, "dbpediamock.path");
    }

    /**
     * Get an {@link Event} object from an ObjectNode if it's possible
     * @param objectNode {@link Event} as JSON Object
     * @return a valid {@link Event}
     * @throws ParseException objectNode if the current objectNode with a json cannot be parsed
     * @throws NullPointerException if objectNode is null
     * @see DBpediaMock#VALUE
     * @see DBpediaMock#source
     * @see Event
     */
    public Event getEventFromJson(ObjectNode objectNode) {
        Objects.requireNonNull(objectNode);
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
     * Check if the json nodes and {@link LatLong} objects are valid
     * @param startNode Start date in JSON of future {@link Event}
     * @param endNode End date in JSON of future {@link Event}
     * @param descriptionNode Description in JSON of future {@link Event}
     * @param latLong {@link LatLong} of the {@link Event}
     * @param langNode Language used in {@link Event}
     * @return true if data are valid
     * @see DBpediaMock#lang
     */
    private boolean checkValidData(JsonNode startNode, JsonNode endNode, JsonNode descriptionNode, LatLong latLong, JsonNode langNode) {
        return startNode != null && endNode != null && descriptionNode != null
                &&  lang.equals(langNode.asText()) && latLong != null;
    }

    /**
     * Parse a json node object in order to create a valid {@link LatLong} object
     * @param objectNode {@link LatLong} in JSON format
     * @return a valid {@link LatLong}
     * @see LatLong
     * @see DBpediaMock#VALUE
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
     * Check if this jar is active
     * @return true if it should be started
     * @see DBpediaMock#PROPERTIES_MANAGER
     */
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
