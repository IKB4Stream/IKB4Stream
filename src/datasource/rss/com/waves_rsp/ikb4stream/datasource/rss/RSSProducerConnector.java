package com.waves_rsp.ikb4stream.datasource.rss;

import com.rometools.modules.georss.GeoRSSModule;
import com.rometools.modules.georss.GeoRSSUtils;
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
import java.util.Objects;

public class RSSProducerConnector implements IProducerConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(RSSProducerConnector.class);
    private final String source;
    private final URL url;

    public RSSProducerConnector() {
        try {
            PropertiesManager propertiesManager = PropertiesManager.getInstance(RSSProducerConnector.class, "resources/datasource/rss/config.properties");
            this.source = propertiesManager.getProperty("RSSProducerConnector.source");
            String urlString = propertiesManager.getProperty("RSSProducerConnector.url");
            this.url = new URL(urlString);
        } catch (IllegalArgumentException | MalformedURLException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException("Invalid configuration");
        }
    }

    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(this.url));

                feed.getEntries().forEach(entry -> {
                    Date startDate = entry.getPublishedDate();
                    String description = entry.getDescription().getValue();
                    if (description == null)
                        description = "";

                    Date endDate = Date.from(Instant.now());
                    if (startDate == null)
                        startDate = endDate;

                    GeoRSSModule module = GeoRSSUtils.getGeoRSS(entry);
                    if (module != null && module.getPosition() != null) {
                        LatLong latLong = new LatLong(module.getPosition().getLatitude(), module.getPosition().getLongitude());
                        Event event = new Event(latLong, startDate, endDate, description, source);
                        dataProducer.push(event);
                    }
                });
            } catch (IOException | FeedException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }
}
