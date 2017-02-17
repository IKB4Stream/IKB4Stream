package com.waves_rsp.ikb4stream.datasource.openagenda;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OpenAgendaProducerConnector implements IProducerConnector {

    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(OpenAgendaProducerConnector.class, "resources/datasource/openagenda/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAgendaProducerConnector.class);
    private final String bbox;

    public OpenAgendaProducerConnector() {
        double latMax = Double.valueOf(PROPERTIES_MANAGER.getProperty("openagenda.latitude.maximum"));
        double latMin = Double.valueOf(PROPERTIES_MANAGER.getProperty("openagenda.latitude.minimum"));
        double lonMax = Double.valueOf(PROPERTIES_MANAGER.getProperty("openagenda.longitude.maximum"));
        double lonMin = Double.valueOf(PROPERTIES_MANAGER.getProperty("openagenda.longitude.minimum"));
        this.bbox = "(" + latMin + "," + lonMin + "),(" + latMax + "," + lonMin + "),(" + latMax + "," + lonMax + ")," +
                "(" + latMin + "," + lonMax + "),(" + latMin + "," + lonMin + ")";
    }


    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);


    }

    private URL createURL() {
        URL url;
        try {
            String bboxEncode = URLEncoder.encode(this.bbox, "utf-8");
            String baseURL = PROPERTIES_MANAGER.getProperty("openagenda.url");

            String formatURL = baseURL + "&geofilter.polygon=" + bboxEncode;
            url = new URL(formatURL);
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
        return url;
    }


    public List<Event> searchEvents() {
        List<Event> events = new ArrayList<>();
        InputStream is = null;
        ObjectMapper mapper = new ObjectMapper();
        ObjectMapper fieldMapper = new ObjectMapper();
        JsonNode root = null;

        try {
            is = createURL().openStream();
            root = mapper.readTree(is);
            //root keyword
            JsonNode recordsNode = root.path("records");

            if (recordsNode.isArray()) {
                for (JsonNode knode : recordsNode) {
                    JsonNode fieldsNode = knode.path("fields");
                    String transform = "{\"fields\": [" + fieldsNode.toString() + "]}";
                    JsonNode rootBis = fieldMapper.readTree(transform);
                    JsonNode fieldsRoodNode = rootBis.path("fields");
                    if (fieldsRoodNode.isArray()) {
                        for (JsonNode subknode : fieldsRoodNode) {

                            //TODO : Create Event !!
                            String latlon = subknode.path("latlon").asText();
                            String title = subknode.path("title").asText();
                            String description = subknode.path("description").asText();
                            String free_text = subknode.path("free_text").asText();
                            String date_start = subknode.path("date_start").asText();
                            String date_end = subknode.path("date_end").asText();
                            String city = subknode.path("city").asText();
                            String address = subknode.path("address").asText();
                            AgendaEvent agendaEvent = new AgendaEvent(latlon, title, description, free_text, date_start, date_end, city, address);
                            createEvent(agendaEvent);

                        }//end for
                    }

                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return events;
    }

    private Event createEvent(AgendaEvent agendaEvent) {
        System.out.println(agendaEvent.getLatlon());
        String[] coord = agendaEvent.getLatlon().substring(1, agendaEvent.getLatlon().length()-1).split(",");
        LatLong latLong = new LatLong(Double.parseDouble(coord[0]), Double.parseDouble(coord[1]));

        //TODO :create JSON Object for build the description of an event
        //return an event

        //return new Event(latLong, /*TODO.......*/);
        return null;
    }


    public static void main(String[] args) throws IOException {

        OpenAgendaProducerConnector obj = new OpenAgendaProducerConnector();


        obj.searchEvents();


    }
}

