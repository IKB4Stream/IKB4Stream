package com.waves_rsp.ikb4stream.datasource.weather;

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
import java.util.Locale;
import java.util.Objects;

public class WeatherProducerConnector implements IProducerConnector {

    private final URL url;

    public WeatherProducerConnector(String urlString) throws MalformedURLException {
        Objects.requireNonNull(urlString);
        if(urlString.equals(""))
            throw new IllegalArgumentException("Empty url found");
        this.url = new URL(urlString);
    }

    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        new Thread(() -> {
            try {
                SyndFeedInput input = new SyndFeedInput(false, Locale.FRANCE);
                XmlReader reader = new XmlReader(url);
                SyndFeed feed = input.build(reader);

                feed.getEntries().forEach(entry -> {
                    System.out.println(entry);
                    String source = entry.getUri();
                    Date date = entry.getPublishedDate();
                    String description = entry.getDescription().getValue();
                    GeoRSSModule module = GeoRSSUtils.getGeoRSS(entry);

                    if(date == null) {
                        date = Date.from(Instant.now());
                    }

                    if(source == null) {
                        source = url.getHost();
                    }

                    if(description == null) {
                        description = "no description";
                    }

                    if(module != null && module.getPosition() != null) {
                        LatLong latLong = new LatLong(module.getPosition().getLatitude(), module.getPosition().getLongitude());
                        Event event = new Event(latLong, date, date, description, source);
                        System.out.println(event);
                        dataProducer.push(event);
                    }
                });
            } catch (FeedException e) {
                throw new IllegalArgumentException(e.getMessage());
            }catch (IOException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }).start();
    }
}
