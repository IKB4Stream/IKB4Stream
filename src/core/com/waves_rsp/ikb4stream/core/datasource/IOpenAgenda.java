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

package com.waves_rsp.ikb4stream.core.datasource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Share code between instance of {@link com.waves_rsp.ikb4stream.datasource.openagenda.OpenAgendaProducerConnector OpenAgendaProducerConnector} and {@link com.waves_rsp.ikb4stream.datasource.openagendamock.OpenAgendaMock OpenAgendaMock}
 *
 * @author ikb4stream
 * @version 1.0
 */
public interface IOpenAgenda extends IProducerConnector {
    /**
     * Object to add metrics from this interface
     *
     * @see MetricsLogger#getMetricsLogger()
     */
    MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();

    Logger LOGGER = LoggerFactory.getLogger(IOpenAgenda.class);
    /**
     * Default charset
     */
    String UTF8 = "utf-8";

    /**
     * Parse JSON from Open Agenda API get by the URL
     *
     * @param is     Stream of {@link Event} from OpenAgenda
     * @param source Name of source of this data
     * @return a list of {@link Event}
     * @throws NullPointerException if is or source is null
     * @see Event
     */
    default List<Event> searchEvents(InputStream is, String source) {
        Objects.requireNonNull(is);
        Objects.requireNonNull(source);
        List<Event> events = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        ObjectMapper fieldMapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(is);
            //root
            JsonNode recordsNode = root.path("records");
            for (JsonNode knode : recordsNode) {
                JsonNode fieldsNode = knode.path("fields");
                String transform = "{\"fields\": [" + fieldsNode.toString() + "]}";
                JsonNode rootBis = fieldMapper.readTree(transform);
                JsonNode fieldsRoodNode = rootBis.path("fields");
                for (JsonNode subknode : fieldsRoodNode) {
                    String latlon = subknode.path("latlon").toString();
                    String title = subknode.path("title").asText();
                    String description = subknode.path("description").asText() + " " + subknode.path("free_text").asText();
                    String dateStart = subknode.path("date_start").asText();
                    String dateEnd = subknode.path("date_end").asText();
                    String city = subknode.path("city").asText();
                    Event event = createEvent(latlon, title, description, dateStart, dateEnd, city, source);
                    pushIfNotNullEvent(events, event);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Bad json format or tree cannot be read: {}", e);
        }
        return events;
    }


    /**
     * Format attributes from the Open Agenda API to create an event
     *
     * @param latlong     event's location
     * @param title       event's title
     * @param description event's description
     * @param dateStart   date when the event starting
     * @param dateEnd     date when the event ending
     * @param city        city where the event take place
     * @param source      event's source
     * @return {@link Event} with this information
     * @throws NullPointerException if one of this param is null
     * @see Event
     */
    default Event createEvent(String latlong, String title, String description, String dateStart, String dateEnd, String city, String source) {
        Objects.requireNonNull(latlong);
        Objects.requireNonNull(title);
        Objects.requireNonNull(description);
        Objects.requireNonNull(dateStart);
        Objects.requireNonNull(dateEnd);
        Objects.requireNonNull(city);
        Objects.requireNonNull(source);
        String[] coord;
        try {
            coord = latlong.substring(1, latlong.length() - 1).split(",");
        } catch (StringIndexOutOfBoundsException e) {
            LOGGER.warn("Cannot find latlong attribute");
            return null;
        }
        LatLong latLong = new LatLong(Double.parseDouble(coord[0]), Double.parseDouble(coord[1]));
        DateFormat df = new SimpleDateFormat("yyyy-mm-dd");
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonDescription = objectMapper.createObjectNode();
        jsonDescription.put("title", title);
        jsonDescription.put("description", description);
        jsonDescription.put("city", city);
        Date start;
        Date end;
        try {
            start = df.parse(dateStart);
        } catch (ParseException e) {
            LOGGER.warn("Cannot find the date of start on OpenAgenda.");
            return null;
        }
        try {
            end = df.parse(dateEnd);
        } catch (ParseException e) {
            LOGGER.warn("Cannot find the date of end on OpenAgenda.");
            end = Calendar.getInstance().getTime();
        }
        return new Event(latLong, start, end, jsonDescription.toString(), source);
    }

    /**
     * Add {@link Event} if it's not null
     *
     * @param events List of {@link Event}
     * @param event  {@link Event} to add in this list
     * @throws NullPointerException if events is null
     * @see Event
     */
    default void pushIfNotNullEvent(List<Event> events, Event event) {
        Objects.requireNonNull(events);
        if (event != null) {
            events.add(event);
        }
    }

    /**
     * Check if this jar is active
     *
     * @param property boolean as string
     * @return true if it should be started
     */
    default boolean isActive(String property) {
        try {
            return Boolean.valueOf(property);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Open agenda datasource not activated: {}", e);
            return true;
        }
    }
}