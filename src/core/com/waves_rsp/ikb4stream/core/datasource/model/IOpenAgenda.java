package com.waves_rsp.ikb4stream.core.datasource.model;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public interface IOpenAgenda extends IProducerConnector {
    String UTF8 = "utf-8";
    Logger LOGGER = LoggerFactory.getLogger(IOpenAgenda.class);
    MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();

    /**
     * Parse JSON from Open Agenda API get by the URL
     *
     * @return a list of events
     */
    default List<Event> searchEvents(InputStream is, String source) {
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
     * @param latlon      : event's location
     * @param title       : event's title
     * @param description : event's description
     * @param dateStart   : date when the event starting
     * @param dateEnd     : date when the event ending
     * @param city        : city where the event take place
     * @param source      : event's source
     * @return an event
     */
    default Event createEvent(String latlon, String title, String description, String dateStart, String dateEnd, String city, String source) {
        String[] coord = new String[2];
        try {
            coord = latlon.substring(1, latlon.length() - 1).split(",");
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

    default void pushIfNotNullEvent(List<Event> events, Event event) {
        if (event != null) {
            events.add(event);
        }
    }


    default boolean isActive(String property) {
        try {
            return Boolean.valueOf(property);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Open agenda datasource not activated: {}", e);
            return true;
        }
    }
}