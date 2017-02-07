package com.waves_rsp.ikb4stream.datasource.rss;

import com.rometools.modules.georss.GeoRSSModule;
import com.rometools.modules.georss.GeoRSSUtils;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.waves_rsp.ikb4stream.core.datasource.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.IProducerConnector;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

public class RSSProducerConnector implements IProducerConnector {
    private final URL url;

    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);

        new Thread(() -> {
            try {
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(this.url));

                feed.getEntries().forEach(entry -> {
                    String source = entry.getUri();
                    Date startDate = entry.getPublishedDate();
                    String description = entry.getDescription().getValue();

                    GeoRSSModule module = GeoRSSUtils.getGeoRSS(entry);
                    if (module != null && module.getPosition() != null) {
                        LatLong latLong = new LatLong(module.getPosition().getLatitude(), module.getPosition().getLongitude());

                        if (source == null)
                            source = this.url.toString();
                        Date endDate = Date.from(Instant.now());
                        if (startDate == null)
                            startDate = endDate;
                        if (description == null)
                            description = "";

                        Event event = new Event(latLong, startDate, endDate, description, source);

                        dataProducer.push(event);
                    }
                });

            } catch (FeedException e) {
                throw new IllegalArgumentException("Wrong url: " + this.url.toString());
            } catch (IOException e) {
                throw new IllegalStateException("XMl Reader can't open the file at the url " + this.url.toString());
            }
        }).start();

    }

    public RSSProducerConnector(String url) throws MalformedURLException {
        if (url.equals("")) {
            throw new IllegalArgumentException();
        }

        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Wrong url format: " + url);
        }
    }
}
