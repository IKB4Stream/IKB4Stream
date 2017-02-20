package com.waves_rsp.ikb4stream.datasource.openagenda;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class OpenAgendaProducerConnector implements IProducerConnector {
    private static final String UTF8 = "utf-8";
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(OpenAgendaProducerConnector.class, "resources/datasource/openagenda/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAgendaProducerConnector.class);
    private final String source;
    private final String propDateStart;
    private final String propDateEnd;
    private final String bbox;

    /**
     * Instantiate the OpenAgendaProducerConnector object with load properties to connect to the OPen Agenda API
     */
    public OpenAgendaProducerConnector() {
        try {
            this.source = PROPERTIES_MANAGER.getProperty("openagenda.source");
            this.propDateStart = PROPERTIES_MANAGER.getProperty("openagenda.date_start");
            this.propDateEnd = PROPERTIES_MANAGER.getProperty("openagenda.date_end");
            double latMax = Double.valueOf(PROPERTIES_MANAGER.getProperty("openagenda.latitude.maximum"));
            double latMin = Double.valueOf(PROPERTIES_MANAGER.getProperty("openagenda.latitude.minimum"));
            double lonMax = Double.valueOf(PROPERTIES_MANAGER.getProperty("openagenda.longitude.maximum"));
            double lonMin = Double.valueOf(PROPERTIES_MANAGER.getProperty("openagenda.longitude.minimum"));
            this.bbox = "(" + latMin + "," + lonMin + "),(" + latMax + "," + lonMin + "),(" + latMax + "," + lonMax + ")," +
                    "(" + latMin + "," + lonMax + "),(" + latMin + "," + lonMin + ")";
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException("Invalid configuration");
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
        while (!Thread.currentThread().isInterrupted()) {
            try {
                List<Event> events = searchEvents();
                events.stream().forEach(dataProducer::push);
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            } finally {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Create the URL from properties (bbox and dates) for accessing to the webservice
     * @return an URL
     */
    private URL createURL() {
        URL url;
        try {
            //bbox
            String bboxEncode = URLEncoder.encode(this.bbox, UTF8);
            String baseURL = PROPERTIES_MANAGER.getProperty("openagenda.url");
            StringBuilder formatURL = new StringBuilder();
            formatURL.append(baseURL).append("&geofilter.polygon=").append(bboxEncode);

            if (!propDateStart.isEmpty()) {
                String dateStartEncode = URLEncoder.encode(this.propDateStart, UTF8);
                formatURL.append("&refine.date_start=").append(dateStartEncode);
            }
            if (!propDateEnd.isEmpty()) {
                String dateEndEncode = URLEncoder.encode(this.propDateEnd, UTF8);
                formatURL.append("&refine.date_end=").append(dateEndEncode);
            }
            url = new URL(formatURL.toString());
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
        return url;
    }


    /**
     * Parse JSON from Open Agenda API get by the URL
     *
     * @return a list of events
     */
    private List<Event> searchEvents() {
        List<Event> events = new ArrayList<>();
        InputStream is;
        ObjectMapper mapper = new ObjectMapper();
        ObjectMapper fieldMapper = new ObjectMapper();

        try {
            is = createURL().openStream();
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
                    String address = subknode.path("address").asText();

                    events.add(createEvent(latlon, title, description, dateStart, dateEnd, city, address));

                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return events;
    }


    /**
     * Format attributes from the Open Agenda API to create an event
     * @param latlon : event's location
     * @param title : event's title
     * @param description : event's description
     * @param dateStart : date when the event starting
     * @param dateEnd : date when the event ending
     * @param city : city where the event take place
     * @param address : address where the event take place
     * @return an event
     */
    private Event createEvent(String latlon, String title, String description, String dateStart, String dateEnd, String city, String address) {
        String[] coord = latlon.substring(1, latlon.length() - 1).split(",");
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
            end = df.parse(dateEnd);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalArgumentException("Wrong date format from open agenda connector");
        }
        return new Event(latLong, start, end, jsonDescription.toString(), this.source);
    }

    @Override
    public boolean isActive() {
        try {
            return Boolean.valueOf(PROPERTIES_MANAGER.getProperty("openagenda.enable"));
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}

