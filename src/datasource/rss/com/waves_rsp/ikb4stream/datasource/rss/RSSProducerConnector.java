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
import com.waves_rsp.ikb4stream.core.util.GeoCoderJacksonParser;
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

public class RSSProducerConnector implements IProducerConnector {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(RSSProducerConnector.class, "resources/datasource/rss/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(RSSProducerConnector.class);
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    private final OpenNLP openNLP = OpenNLP.getOpenNLP(Thread.currentThread());
    private final String source;
    private final int interval;
    private final URL url;

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

    private LatLong getLatLong(GeoRSSModule module, String desc) {
        if (module != null) {
            return new LatLong(module.getPosition().getLatitude(), module.getPosition().getLongitude());
        } else if (desc != null) {
            return geocodeRSS(desc);
        }
        return null;
    }

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
     */
    private LatLong geocodeRSS(String text) {
        long start = System.currentTimeMillis();
        GeoCoderJacksonParser geocoder = new GeoCoderJacksonParser();
        List<String> locations = openNLP.applyNLPner(text, OpenNLP.nerOptions.LOCATION);
        if (!locations.isEmpty()) {
            long time = System.currentTimeMillis() - start;
            METRICS_LOGGER.log("time_geocode_" + this.source, time);
            return geocoder.parse(locations.get(0));
        }
        return null;
    }
}
