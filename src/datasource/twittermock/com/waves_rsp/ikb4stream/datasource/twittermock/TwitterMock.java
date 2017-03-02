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

package com.waves_rsp.ikb4stream.datasource.twittermock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.waves_rsp.ikb4stream.core.datasource.IProducerConnectorMock;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;

/**
 * @author ikb4stream
 * @version 1.0
 * @see com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector
 * @see com.waves_rsp.ikb4stream.core.datasource.IProducerConnectorMock
 */
public class TwitterMock implements IProducerConnectorMock {
    /**
     * Properties of this module
     *
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class, String)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(TwitterMock.class, "resources/datasource/twittermock/config.properties");
    /**
     * Logger used to log all information in this module
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterMock.class);
    /**
     * Source name of corresponding {@link Event}
     *
     * @see TwitterMock#getEventFromJson(ObjectNode)
     */
    private static final String SOURCE = "Twitter";

    /**
     * Override default constructor
     */
    public TwitterMock() {
        // Do Nothing
    }

    /**
     * Load data registered into a json twitter file and parse them to create event
     *
     * @param dataProducer {@link IDataProducer} contains the data queue
     * @see IProducerConnectorMock#load(IDataProducer, PropertiesManager, String)
     */
    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        load(dataProducer, PROPERTIES_MANAGER, "twittermock.path");
    }

    /**
     * Indicates whether this producer is enabled or not, according to twittermock.enable
     *
     * @return true is facebookmock.enable is true
     * @see TwitterMock#PROPERTIES_MANAGER
     */
    @Override
    public boolean isActive() {
        return isActive(PROPERTIES_MANAGER, "twittermock.enable");
    }

    /**
     * Parse an object node in order to create an {@link Event} object
     *
     * @param objectNode object node to convert to {@link Event}
     * @return {@link Event} converted format
     * @see TwitterMock#SOURCE
     */
    @Override
    public Event getEventFromJson(ObjectNode objectNode) {
        Date startDate = Date.from(Instant.ofEpochMilli(objectNode.findValue("timestamp_ms").asLong()));
        Date endDate = Date.from(Instant.now());
        JsonNode jsonNode = objectNode.findValue("place");
        JsonNode jsonCoordinates = jsonNode.findValue("coordinates");
        String description = objectNode.findValue("text").toString();
        LatLong latLong = jsonToLatLong(jsonCoordinates);
        try {
            return new Event(latLong, startDate, endDate, description, SOURCE);
        } catch (IllegalArgumentException | NullPointerException err) {
            LOGGER.error(err.getMessage());
            return null;
        }
    }

    /**
     * Create {@link LatLong} from a jsonNode object and parse it to get GPS coordinates
     *
     * @param jsonCoordinates json coordinates to parse
     * @return {@link LatLong} the parsed latlong from jsonCoordinates
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
