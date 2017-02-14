package com.waves_rsp.ikb4stream.datasource.weather;

import com.rometools.modules.georss.GeoRSSModule;
import com.rometools.modules.georss.GeoRSSUtils;
import com.rometools.modules.yahooweather.YWeatherFeedModule;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
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

public class WeatherProducerConnector implements IProducerConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherProducerConnector.class);
    private final String source;
    private final URL url;

    public WeatherProducerConnector() {
        try {
            PropertiesManager propertiesManager = PropertiesManager.getInstance(WeatherProducerConnector.class, "resources/config.properties");
            this.source = propertiesManager.getProperty("WeatherProducerConnector.source");
            this.url = new URL(propertiesManager.getProperty("WeatherProducerConnector.url"));
        } catch (IllegalArgumentException | MalformedURLException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalArgumentException("Invalid configuration");
        }
    }

    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                SyndFeedInput input = new SyndFeedInput(false, Locale.FRANCE);
                URL url = new URL(YWeatherFeedModule.URI);
                XmlReader reader = new XmlReader(url);
                SyndFeed feed = input.build(reader);
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
                        LOGGER.info("Event " + event + " has been pushed");
                    }
                });
            } catch (IOException | FeedException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }
}
