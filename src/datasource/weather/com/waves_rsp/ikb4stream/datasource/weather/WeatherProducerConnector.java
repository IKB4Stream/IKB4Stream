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

package com.waves_rsp.ikb4stream.datasource.weather;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * @author ikb4stream
 * @version 1.0
 * @see com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector
 */
public class WeatherProducerConnector implements IProducerConnector {
    /**
     * Properties of this module
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class, String)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(WeatherProducerConnector.class, "resources/datasource/weather/config.properties");
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherProducerConnector.class);
    /**
     *
     */
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    /**
     *
     */
    private final String source;
    /**
     *
     */
    private final URL url;

    /**
     * Instantiate the WeatherProducerConnector object with load properties to connect to the RSS flow
     */
    public WeatherProducerConnector() {
        try {
            this.source = PROPERTIES_MANAGER.getProperty("WeatherProducerConnector.source");
            this.url = new URL(PROPERTIES_MANAGER.getProperty("WeatherProducerConnector.url"));
        } catch (IllegalArgumentException | MalformedURLException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException("Invalid configuration");
        }
    }

    /**
     * Check a RSS flow from an
     * @param dataProducer {@link IDataProducer} contains the data queue
     */
    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                SyndFeedInput input = new SyndFeedInput(false, Locale.FRANCE);
                XmlReader reader = new XmlReader(url);
                SyndFeed feed = input.build(reader);
                long start = System.currentTimeMillis();
                feed.getEntries().forEach(entry -> {
                    Date date = entry.getPublishedDate();
                    String description = entry.getDescription().getValue();
                    GeoRSSModule module = GeoRSSUtils.getGeoRSS(entry);
                    if (date == null) {
                        date = Date.from(Instant.now());
                    }
                    if (description == null) {
                        description = "no description";
                    }
                    if (module != null && module.getPosition() != null) {
                        LatLong latLong = new LatLong(module.getPosition().getLatitude(), module.getPosition().getLongitude());
                        Event event = new Event(latLong, date, date, description, source);
                        dataProducer.push(event);
                        long end = System.currentTimeMillis();
                        long result = end - start;
                        METRICS_LOGGER.log("time_process_"+this.source, result);
                        LOGGER.info("Event " + event + " has been pushed");
                    }
                });
            } catch (IOException | FeedException e) {
                LOGGER.error(e.getMessage());
                return;
            }
        }
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isActive() {
        try {
            return Boolean.valueOf(PROPERTIES_MANAGER.getProperty("WeatherProducerConnector.enable"));
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}
