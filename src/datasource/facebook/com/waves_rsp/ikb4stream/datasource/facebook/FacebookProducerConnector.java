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

package com.waves_rsp.ikb4stream.datasource.facebook;

import com.restfb.*;
import com.restfb.types.Event;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * {@link FacebookProducerConnector} class provides events link to a word form coordinates
 * @author ikb4stream
 * @version 1.0
 * @see com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector
 */
public class FacebookProducerConnector implements IProducerConnector {
    /**
     * Properties of this module
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class, String)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(FacebookProducerConnector.class, "resources/datasource/facebook/config.properties");
    /**
     * Logger used to log all information in this module
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookProducerConnector.class);
    /**
     * Object to add metrics from this class
     * @see MetricsLogger#log(String, long)
     * @see MetricsLogger#getMetricsLogger()
     */
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    /**
     * Token to access to Facebook API
     * @see FacebookProducerConnector#searchWordFromGeolocation(String, int, double, double)
     */
    private final String pageAccessToken;
    /**
     * Name of source in {@link com.waves_rsp.ikb4stream.core.model.Event#source}
     * @see FacebookProducerConnector#searchWordFromGeolocation(String, int, double, double)
     */
    private final String source;
    /**
     * Keyword to search
     * @see FacebookProducerConnector#load(IDataProducer)
     */
    private final String word;
    /**
     * Latitude limit to get {@link com.waves_rsp.ikb4stream.core.model.Event Event}
     * @see FacebookProducerConnector#load(IDataProducer)
     */
    private final double lat;
    /**
     * Longitude limit to get {@link com.waves_rsp.ikb4stream.core.model.Event Event}
     * @see FacebookProducerConnector#load(IDataProducer)
     */
    private final double lon;
    /**
     * Limit to get {@link com.waves_rsp.ikb4stream.core.model.Event Event}
     * @see FacebookProducerConnector#load(IDataProducer)
     */
    private final int limit;

    /**
     * Default constructor that init all members with {@link FacebookProducerConnector#PROPERTIES_MANAGER}
     * @see FacebookProducerConnector#PROPERTIES_MANAGER
     * @see FacebookProducerConnector#source
     * @see FacebookProducerConnector#pageAccessToken
     * @see FacebookProducerConnector#word
     * @see FacebookProducerConnector#limit
     * @see FacebookProducerConnector#lat
     * @see FacebookProducerConnector#lon
     */
    public FacebookProducerConnector() {
        try {
            this.source = PROPERTIES_MANAGER.getProperty("FacebookProducerConnector.source");
            this.pageAccessToken = PROPERTIES_MANAGER.getProperty("FacebookProducerConnector.token");
            this.word =  PROPERTIES_MANAGER.getProperty("FacebookProducerConnector.word");
            this.limit =  Integer.valueOf(PROPERTIES_MANAGER.getProperty("FacebookProducerConnector.limit"));
            this.lat =  Double.valueOf(PROPERTIES_MANAGER.getProperty("FacebookProducerConnector.latitude"));
            this.lon =  Double.valueOf(PROPERTIES_MANAGER.getProperty("FacebookProducerConnector.longitude"));
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid configuration {} ",e);
            throw new IllegalStateException("Invalid configuration");
        }
    }

    /**
     * Search all {@link com.waves_rsp.ikb4stream.core.model.Event Event} in a {@link com.waves_rsp.ikb4stream.core.communication.model.BoundingBox BoundingBox}
     * with keyword
     * @param word a Sting which is the event to find
     * @param limit an int which the result limit
     * @param latitude a long
     * @param longitude a long
     * @return a list of {@link com.waves_rsp.ikb4stream.core.model.Event Event} form Facebook events and coodinates
     * @throws NullPointerException if word is null
     * @see FacebookProducerConnector#pageAccessToken
     * @see FacebookProducerConnector#source
     * @see com.waves_rsp.ikb4stream.core.model.Event Event
     */
    private List<com.waves_rsp.ikb4stream.core.model.Event> searchWordFromGeolocation(String word, int limit, double latitude, double longitude) {
        Objects.requireNonNull(word);
        List<com.waves_rsp.ikb4stream.core.model.Event> events = new ArrayList<>();
        FacebookClient facebookClient = new DefaultFacebookClient(this.pageAccessToken, Version.LATEST);
        long startTime = System.currentTimeMillis();
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
                com.waves_rsp.ikb4stream.core.model.Event event = new com.waves_rsp.ikb4stream.core.model.Event(latLong, start, end, description, source);
                events.add(event);
                long endTime = System.currentTimeMillis();
                long result = endTime - startTime;
                METRICS_LOGGER.log("time_process_"+this.source, result);
            }
        });

        return events;
    }

    /**
     * Check if an event is valid i.e parameters are correctly set
     * @param event {@link com.waves_rsp.ikb4stream.core.model.Event Event} to test
     * @return true if valid
     * @see com.waves_rsp.ikb4stream.core.model.Event Event
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
     * @param dataProducer contains the {@link com.waves_rsp.ikb4stream.producer.datasource.DataQueue DataQueue}
     * @throws NullPointerException if dataProducer is null
     * @throws InterruptedException if the current thread to listen facebook has been interrupted
     * @see com.waves_rsp.ikb4stream.core.model.Event Event
     * @see FacebookProducerConnector#word
     * @see FacebookProducerConnector#limit
     * @see FacebookProducerConnector#lat
     * @see FacebookProducerConnector#lon
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

    /**
     * Check if this jar is active
     * @return True if it should be started
     * @see FacebookProducerConnector#PROPERTIES_MANAGER
     */
    @Override
    public boolean isActive() {
        try {
            return Boolean.valueOf(PROPERTIES_MANAGER.getProperty("FacebookProducerConnector.enable"));
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}
