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

package com.waves_rsp.ikb4stream.datasource.facebookmock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.waves_rsp.ikb4stream.core.datasource.IProducerConnectorMock;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Mock of {@link com.waves_rsp.ikb4stream.datasource.facebook.FacebookProducerConnector FacebookProducerConnector}
 *
 * @author ikb4stream
 * @version 1.0
 * @see com.waves_rsp.ikb4stream.core.datasource.IProducerConnectorMock
 * @see com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector
 */
public class FacebookMock implements IProducerConnectorMock {
    /**
     * Properties of this module
     *
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class, String)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(FacebookMock.class, "resources/datasource/facebookmock/config.properties");
    /**
     * Logger used to log all information in this module
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookMock.class);
    /**
     * Constant value {@value SOURCE}
     *
     * @see FacebookMock#getEventFromJson(ObjectNode)
     */
    private static final String SOURCE = "Facebook";

    /**
     * Override default constructor
     */
    public FacebookMock() {
        // Do nothing
    }

    /**
     * Load data registered into a json twitter file and parse them to create {@link Event}
     *
     * @param dataProducer which allows to push {@link Event} in queue
     * @throws NullPointerException if dataProducer is null
     * @see FacebookMock#PROPERTIES_MANAGER
     */
    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        load(dataProducer, PROPERTIES_MANAGER, "facebookmock.path");
    }

    /**
     * Indicates whether this producer is enabled or not, according to facebookmock.enable
     *
     * @return true is facebookmock.enable is true
     * @see FacebookMock#PROPERTIES_MANAGER
     */
    @Override
    public boolean isActive() {
        return isActive(PROPERTIES_MANAGER, "facebookmock.enable");
    }

    /**
     * Parse a json node in order to create a Date object
     *
     * @param jsonNode json to parse to Date
     * @return null if a ParseException has been thrown, else the Date object created
     * @throws NullPointerException if jsonNode is null
     */
    static Date getDateFromJson(JsonNode jsonNode) {
        Objects.requireNonNull(jsonNode);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSS");
        TimeZone tz = TimeZone.getTimeZone("CET");
        String timeString = jsonNode.toString();
        timeString = timeString.substring(1, timeString.length() - 4);
        dateFormat.setTimeZone(tz);
        try {
            return dateFormat.parse(timeString);
        } catch (ParseException err) {
            LOGGER.error(err.getMessage());
            return null;
        }
    }

    /**
     * Parse an object node in order to create an {@link Event} object
     *
     * @param objectNode object node to convert to {@link Event}
     * @return Event converted format
     * @throws NullPointerException if objectNode is null
     * @see Event
     * @see LatLong
     * @see FacebookMock#SOURCE
     */
    @Override
    public Event getEventFromJson(ObjectNode objectNode) {
        Objects.requireNonNull(objectNode);
        JsonNode startNode = objectNode.findValue("start_time");
        Date startDate = getDateFromJson(startNode);
        JsonNode endNode = objectNode.findValue("end_time");
        Date endDate = getDateFromJson(endNode);
        LatLong latLong = jsonToLatLong(objectNode);
        String description = objectNode.findValue("description").toString();
        return new Event(latLong, startDate, endDate, description, SOURCE);
    }

    /**
     * Create {@link LatLong} from an ObjectNode object and parse it to get GPS coordinates
     *
     * @param objectNode the json {@link LatLong} to format
     * @return {@link LatLong} object containing latitude and longitude values
     * @throws NullPointerException if objectNode is null
     * @see LatLong
     */
    private static LatLong jsonToLatLong(ObjectNode objectNode) {
        Objects.requireNonNull(objectNode);
        double latitude = objectNode.findValue("latitude").asDouble();
        double longitude = objectNode.findValue("longitude").asDouble();
        return new LatLong(latitude, longitude);
    }
}
