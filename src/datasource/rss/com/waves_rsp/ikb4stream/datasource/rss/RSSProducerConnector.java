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

package com.waves_rsp.ikb4stream.datasource.rss;

import com.rometools.modules.georss.GeoRSSModule;
import com.rometools.modules.georss.GeoRSSUtils;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.core.util.Geocoder;
import com.waves_rsp.ikb4stream.core.util.nlp.OpenNLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Get data flow from RSS
 *
 * @author ikb4stream
 * @version 1.0
 * @see com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector
 */
public class RSSProducerConnector implements IProducerConnector {
    /**
     * Properties of this module
     *
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class, String)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(RSSProducerConnector.class, "resources/datasource/rss/config.properties");
    /**
     * Logger used to log all information in this module
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RSSProducerConnector.class);
    /**
     * Object to add metrics from this class
     *
     * @see MetricsLogger#log(String, long)
     * @see MetricsLogger#getMetricsLogger()
     */
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    /**
     * Single instance of {@link OpenNLP} per each Thread
     *
     * @see RSSProducerConnector#geocodeRSS(String)
     */
    private final OpenNLP openNLP = OpenNLP.getOpenNLP(Thread.currentThread());
    /**
     * Source name of corresponding {@link Event}
     *
     * @see RSSProducerConnector#geocodeRSS(String)
     * @see RSSProducerConnector#load(IDataProducer)
     */
    private final String source;
    /**
     * Interval time between two batch
     *
     * @see RSSProducerConnector#load(IDataProducer)
     */
    private final int interval;
    /**
     * @see RSSProducerConnector#load(IDataProducer)
     */
    private final URL url;

    /**
     * Public constructor to init variable from {@link RSSProducerConnector#PROPERTIES_MANAGER}
     *
     * @throws IllegalStateException if invalid value in configuration file
     * @see RSSProducerConnector#source
     * @see RSSProducerConnector#interval
     * @see RSSProducerConnector#url
     */
    public RSSProducerConnector() {
        try {
            this.source = PROPERTIES_MANAGER.getProperty("RSSProducerConnector.source");
            this.interval = Integer.parseInt(PROPERTIES_MANAGER.getProperty("RSSProducerConnector.interval"));
            this.url = new URL(PROPERTIES_MANAGER.getProperty("RSSProducerConnector.url"));
        } catch (IllegalArgumentException | MalformedURLException e) {
            LOGGER.error("Invalid configuration [] ", e);
            throw new IllegalStateException("Invalid configuration");
        }
    }

    /**
     * Listen {@link Event} from RSS
     *
     * @param dataProducer {@link IDataProducer} contains the data queue
     * @throws NullPointerException if dataProducer is null
     * @see RSSProducerConnector#source
     * @see RSSProducerConnector#url
     * @see RSSProducerConnector#interval
     */
    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        final boolean[] first = {true};
        final Date[] lastTime = {Date.from(Instant.now())};
        while (!Thread.currentThread().isInterrupted()) {
            try {
                long start = System.currentTimeMillis(); //metrics
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(this.url));
                Date currentTime = Date.from(Instant.now());
                feed.getEntries().stream()
                        .filter(entry -> first[0] || entry.getPublishedDate().after(lastTime[0]))
                        .forEach(entry -> {
                            lastTime[0] = currentTime;
                            Date startDate = (entry.getPublishedDate() != null) ? entry.getPublishedDate() : currentTime;
                            String description = (entry.getDescription().getValue() != null) ? entry.getDescription().getValue() : "";
                            String completeDesc = entry.getTitle() + " " + description;
                            GeoRSSModule module = GeoRSSUtils.getGeoRSS(entry);
                            LatLong latLong = getLatLong(module, completeDesc);
                            if (latLong != null) {
                                Event event = new Event(latLong, startDate, currentTime, completeDesc, source);
                                dataProducer.push(event);
                            }
                        });
                first[0] = false;
                long time = System.currentTimeMillis() - start;
                METRICS_LOGGER.log("time_process_" + this.source, time);
                Thread.sleep(interval);
            } catch (IOException | FeedException e) {
                LOGGER.error("Can't parse RSS [] ", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Getting a {@link LatLong} from a GeoRSSModule or a description
     *
     * @param module GeoRSSModule that represent a {@link LatLong}
     * @param desc   Description of {@link Event}
     * @return {@link LatLong} if found something or null
     * @see LatLong
     */
    private LatLong getLatLong(GeoRSSModule module, String desc) {
        if (module != null) {
            return new LatLong(module.getPosition().getLatitude(), module.getPosition().getLongitude());
        } else if (desc != null) {
            return geocodeRSS(desc);
        }
        return null;
    }

    /**
     * Check if this jar is active
     *
     * @return true if it should be started
     * @see RSSProducerConnector#PROPERTIES_MANAGER
     */
    @Override
    public boolean isActive() {
        try {
            return Boolean.valueOf(PROPERTIES_MANAGER.getProperty("RSSProducerConnector.enable"));
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    /**
     * Select a list of location from a RSS with the NER OpenNLP algorithme.
     * Then, geolocalize the first location found with the geocoder Nominatim (OSM)
     *
     * @param text to analyze
     * @return a latLong coordinates
     * @see RSSProducerConnector#openNLP
     * @see RSSProducerConnector#source
     */
    private LatLong geocodeRSS(String text) {
        long start = System.currentTimeMillis();

        List<String> locations = openNLP.applyNLPner(text, OpenNLP.nerOptions.LOCATION);
        if (!locations.isEmpty()) {
            long time = System.currentTimeMillis() - start;
            METRICS_LOGGER.log("time_geocode_" + this.source, time);
            return Geocoder.geocode(locations.get(0)).getLatLong();
        }
        return null;
    }
}
