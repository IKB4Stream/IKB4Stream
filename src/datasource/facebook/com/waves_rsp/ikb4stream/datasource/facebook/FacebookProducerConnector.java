package com.waves_rsp.ikb4stream.datasource.facebook;

import com.restfb.*;
import com.restfb.types.Event;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * FacebookProducerConnector class provides events link to a word form coordinates
 */
public class FacebookProducerConnector implements IProducerConnector {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(FacebookProducerConnector.class, "resources/datasource/facebook/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookProducerConnector.class);
    private final String source;
    private final String pageAccessToken;
    private final String word;
    private final int limit;
    private final double lat;
    private final double lon;

    public FacebookProducerConnector() {
        try {
            this.source = PROPERTIES_MANAGER.getProperty("FacebookProducerConnector.source");
            this.pageAccessToken = PROPERTIES_MANAGER.getProperty("FacebookProducerConnector.token");
            this.word =  PROPERTIES_MANAGER.getProperty("FacebookProducerConnector.word");
            this.limit =  Integer.valueOf(PROPERTIES_MANAGER.getProperty("FacebookProducerConnector.limit"));
            this.lat =  Double.valueOf(PROPERTIES_MANAGER.getProperty("FacebookProducerConnector.latitude"));
            this.lon =  Double.valueOf(PROPERTIES_MANAGER.getProperty("FacebookProducerConnector.longitude"));
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException("Invalid configuration");
        }
    }

    /**
     * @param word      a Sting which is the event to find
     * @param limit     an int which the result limit
     * @param latitude  a long
     * @param longitude a long
     * @return a list of events form Facebook events and coodinates
     */
    private List<com.waves_rsp.ikb4stream.core.model.Event> searchWordFromGeolocation(String word, int limit, double latitude, double longitude) {
        Objects.requireNonNull(word);
        List<com.waves_rsp.ikb4stream.core.model.Event> events = new ArrayList<>();
        FacebookClient facebookClient = new DefaultFacebookClient(this.pageAccessToken, Version.LATEST);
        Connection<Event> publicSearch = facebookClient.fetchConnection("search", Event.class,
                Parameter.with("q", word),
                Parameter.with("type", "event"),
                Parameter.with("limit", limit),
                Parameter.with("place&center", latitude + "," + longitude));

        publicSearch.getData().forEach(eventData -> {
            if(isValidEvent(eventData)) {
                double latitudeEv = eventData.getPlace().getLocation().getLatitude();
                double longitudeEv = eventData.getPlace().getLocation().getLongitude();
                LatLong latLong = new LatLong(latitudeEv, longitudeEv);
                Date start = eventData.getStartTime();
                Date end = eventData.getEndTime();
                String description = eventData.getDescription();
                events.add(new com.waves_rsp.ikb4stream.core.model.Event(latLong, start, end, description, source));
            }
        });

        return events;
    }

    /**
     * Check if an event is valid i.e parameters are correctly set
     * @param event
     * @return True if valid
     */
    private boolean isValidEvent(Event event) {
        return (event != null) && (event.getPlace() != null)
                && (event.getPlace().getLocation() != null)
                && (event.getPlace().getLocation().getLongitude() != null)
                && (event.getPlace().getLocation().getLatitude() != null)
                && (event.getStartTime() != null)
                && (event.getDescription() != null)
                && (event.getEndTime() != null);
    }

    /**
     * Load valid events from Facebook into the data producer object
     *
     * @param dataProducer
     * @throws NullPointerException if dataProducer is null
     * @throws InterruptedException if the current thread to listen facebook has been interrupted
     */
    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                List<com.waves_rsp.ikb4stream.core.model.Event> events = searchWordFromGeolocation(word, limit, lat, lon);
                events.forEach(dataProducer::push);
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            } finally {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public boolean isActive() {
        try {
            return Boolean.valueOf(PROPERTIES_MANAGER.getProperty("FacebookProducerConnector.enable"));
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}
