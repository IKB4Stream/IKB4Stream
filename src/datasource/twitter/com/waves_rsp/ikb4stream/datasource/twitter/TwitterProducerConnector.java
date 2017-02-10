package com.waves_rsp.ikb4stream.datasource.twitter;

import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * Listen any events provided by the twitter api and load them into a IDataProducer object.
 *
 */
public class TwitterProducerConnector implements IProducerConnector {
    private final PropertiesManager propertiesManager = PropertiesManager.getInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterProducerConnectorTest.class);

    private TwitterProducerConnector() {

    }

    public static TwitterProducerConnector create() {
        return new TwitterProducerConnector();
    }

    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        try {
            String keyAuthToken = propertiesManager.getProperty("twitter.key.auth.accesstoken");
            String secretAuthToken = propertiesManager.getProperty("twitter.secret.auth.accesstoken");
            String keyConsumerToken = propertiesManager.getProperty("twitter.key.consumer.accesstoken");
            String secretConsumerToken = propertiesManager.getProperty("twitter.secret.consumer.accesstoken");

            ConfigurationBuilder confBuilder = new ConfigurationBuilder();
            confBuilder.setOAuthAccessToken(keyAuthToken);
            confBuilder.setOAuthAccessTokenSecret(secretAuthToken);
            confBuilder.setOAuthConsumerKey(keyConsumerToken);
            confBuilder.setOAuthConsumerSecret(secretConsumerToken);
            TwitterStream twitterStream = new TwitterStreamFactory(confBuilder.build()).getInstance();

            while (!Thread.interrupted()) {
                TwitterStreamListener twitterListener = new TwitterStreamListener();
                twitterStream.addListener(twitterListener);

                FilterQuery filterQuery = new FilterQuery();
                filterQuery.track("#Versailles");

                twitterStream.onStatus(status -> {
                    String source = status.getSource();
                    String description = status.getText();
                    Date start = status.getCreatedAt();
                    Date end = status.getCreatedAt();
                    GeoLocation geoLocation = status.getGeoLocation();
                    if (geoLocation != null) {
                        LatLong latLong = new LatLong(geoLocation.getLatitude(), geoLocation.getLongitude());
                        Event event = new Event(latLong, start, end, description, source);
                        dataProducer.push(event);
                        LOGGER.info(event.toString());
                    }
                }).filter(filterQuery);
                twitterStream.user();
                twitterStream.sample("fr");
            }
        }catch (IllegalArgumentException | IllegalStateException err) {
            LOGGER.error(err.getMessage());
        }
    }

    private class TwitterStreamListener implements StatusListener {
        private EventContainer container = new EventContainer();

        @Override
        public void onStatus(Status status) {
            String source = status.getSource();
            String description = status.getText();
            Date start = status.getCreatedAt();
            Date end = status.getCreatedAt();
            GeoLocation geoLocation = status.getGeoLocation();
            if(geoLocation != null) {
                LatLong latLong = new LatLong(geoLocation.getLatitude(), geoLocation.getLongitude());
                Event event = new Event(latLong, start, end, description, source);
                container.push(event);
                LOGGER.info(event.toString());
            }
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            LOGGER.info(""+statusDeletionNotice.getStatusId());
        }

        @Override
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            LOGGER.info("number of limited status : "+numberOfLimitedStatuses);
        }

        @Override
        public void onScrubGeo(long userId, long upToStatusId) {
            LOGGER.info("user id : "+userId+", "+upToStatusId);
        }

        @Override
        public void onStallWarning(StallWarning warning) {
            LOGGER.warn(warning.getMessage());
        }

        @Override
        public void onException(Exception ex) {
            LOGGER.error(ex.getMessage());
        }

        Optional<Event> getEvent() {
            return container.getEvents().isEmpty() ? Optional.empty() : Optional.of(container.pop());
        }
    }

    private class EventContainer {
        private final ArrayDeque<Event> events = new ArrayDeque<>();

        void push(Event event) {
            Objects.requireNonNull(event);
            events.push(event);
        }

        Event pop() {
            return events.pop();
        }

        ArrayDeque<Event> getEvents() {
            return events;
        }
    }
}
