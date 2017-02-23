package com.waves_rsp.ikb4stream.datasource.openagendamock;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class OpenAgendaMock implements IProducerConnector {
    private static final String UTF8 = "utf-8";
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(OpenAgendaMock.class, "resources/datasource/openagendamock/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAgendaMock.class);
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    private final String source;
    private final long sleepTime;
    private final InputStream input;

    /**
     * Instantiate the OpenAgendaMock object with load properties to connect to the OPen Agenda API
     */
    public OpenAgendaMock() {
        try {
            this.source = PROPERTIES_MANAGER.getProperty("openagendamock.source");
            this.sleepTime = Long.valueOf(PROPERTIES_MANAGER.getProperty("openagendamock.sleep_time"));
            String mockFile = PROPERTIES_MANAGER.getProperty("openagendamock.mock");
            this.input = new FileInputStream(new File(mockFile));
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid properties loaded: {}", e);
            throw new IllegalStateException("Invalid configuration");
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found : {} ", e);
            throw new IllegalArgumentException("Invalid property");
        }
    }


    /**
     * Listen events from openAgenda and load them with the data producer object
     *
     * @param dataProducer
     */
    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        Objects.requireNonNull(dataProducer);
        ObjectMapper mapper = new ObjectMapper();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                long start = System.currentTimeMillis();
                List<Event> events = searchEvents();
                long time = System.currentTimeMillis() - start;
                events.forEach(dataProducer::push);
                METRICS_LOGGER.log("time_process_" + this.source, time);
                Thread.sleep(this.sleepTime);
            } catch (InterruptedException e) {
                LOGGER.error("Current thread has been interrupted: {}", e);
            } finally {
                try {
                    this.input.close();
                } catch (IOException e) {
                    LOGGER.error("Exception during thread interrupted");
                }
                Thread.currentThread().interrupt();
            }
        }
    }


    /**
     * Parse JSON from Open Agenda API get by the URL
     *
     * @return a list of events
     */
    private List<Event> searchEvents() {
        List<Event> events = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        ObjectMapper fieldMapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(this.input);
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
                    String address = subknode.path("address").asText();
                    Event event = createEvent(latlon, title, description, dateStart, dateEnd, city, address);
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
     * @param address     : address where the event take place
     * @return an event
     */
    private Event createEvent(String latlon, String title, String description, String dateStart, String dateEnd, String city, String address) {
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
        jsonDescription.put("address", address);
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
        }catch (ParseException e) {
            LOGGER.warn("Cannot find the date of end on OpenAgenda.");
            end = Calendar.getInstance().getTime();
        }
        return new Event(latLong, start, end, jsonDescription.toString(), this.source);

    }

    private void pushIfNotNullEvent(List<Event> events, Event event) {
        if (event != null) {
            events.add(event);
        }
    }

    @Override
    public boolean isActive() {
        try {
            return Boolean.valueOf(PROPERTIES_MANAGER.getProperty("openagendamock.enable"));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Open agenda datasource not activated: {}", e);
            return true;
        }
    }

    public static void main (String[] args){
        OpenAgendaMock p = new OpenAgendaMock();
        p.load(e->{
            System.out.println(e+"\n");
        });
    }
}

