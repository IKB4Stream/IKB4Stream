package com.waves_rsp.ikb4stream.datasource.twitter;

import com.waves_rsp.ikb4stream.core.communication.model.BoundingBox;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.*;

/**
 * Listen any events provided by the twitter api and load them into a IDataProducer object.
 *
 */
public class TwitterProducerConnector implements IProducerConnector {
    private final PropertiesManager propertiesManager = PropertiesManager.getInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterProducerConnectorTest.class);
    private final ConfigurationBuilder confBuilder = new ConfigurationBuilder();

    private TwitterProducerConnector() {

    }

    public static TwitterProducerConnector getInstance() {
        return new TwitterProducerConnector();
    }

    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        TwitterStream twitterStream = null;
        try {
            loadTwitterProperties();
            double latitudeMax = Double.valueOf(propertiesManager.getProperty("twitter.latitude.maximum"));
            double latitudeMin = Double.valueOf(propertiesManager.getProperty("twitter.latitude.minimum"));
            double longitudeMax = Double.valueOf(propertiesManager.getProperty("twitter.longitude.maximum"));
            double longitudeMin = Double.valueOf(propertiesManager.getProperty("twitter.longitude.minimum"));
            BoundingBox boundingBox = new BoundingBox(new LatLong[]{new LatLong(latitudeMax, longitudeMax),
                                                                    new LatLong(latitudeMin, longitudeMin)});

            TwitterStreamListener streamListener = new TwitterStreamListener(dataProducer);
            twitterStream = new TwitterStreamFactory(confBuilder.build()).getInstance();
            twitterStream.addListener(streamListener);
            FilterQuery filterQuery = new FilterQuery();
            twitterStream.filter(filterQuery);
            Arrays.stream(boundingBox.getLatLongs()).forEach(latLong -> {
                filterQuery.locations(new double[]{latLong.getLatitude(), latLong.getLongitude()});
            });

            twitterStream.sample("fr");
            twitterStream.onStatus(status -> LOGGER.info(status.getText()));

            while(!Thread.interrupted()) {
                Thread.sleep(1000);
            }
        }catch (IllegalArgumentException | IllegalStateException err) {
            LOGGER.error(err.getMessage());
            Thread.currentThread().interrupt();
            return;
        } catch (InterruptedException e) {
            LOGGER.error("Current thread has been interrupted. ");
            Thread.interrupted();
            return;
        } finally {
            if(twitterStream != null) {
                twitterStream.shutdown();
            }
        }
    }

    private void loadTwitterProperties() {
        try {
            String keyAuthToken = propertiesManager.getProperty("twitter.key.auth.accesstoken");
            String secretAuthToken = propertiesManager.getProperty("twitter.secret.auth.accesstoken");
            String keyConsumerToken = propertiesManager.getProperty("twitter.key.consumer.accesstoken");
            String secretConsumerToken = propertiesManager.getProperty("twitter.secret.consumer.accesstoken");

            confBuilder.setOAuthAccessToken(keyAuthToken);
            confBuilder.setOAuthAccessTokenSecret(secretAuthToken);
            confBuilder.setOAuthConsumerKey(keyConsumerToken);
            confBuilder.setOAuthConsumerSecret(secretConsumerToken);
            confBuilder.setJSONStoreEnabled(true);
        }catch (IllegalArgumentException err) {
            LOGGER.error(err.getMessage());
        }
    }

    private class TwitterStreamListener implements StatusListener {
        private final IDataProducer dataProducer;

        private TwitterStreamListener(IDataProducer dataProducer) {
            this.dataProducer = dataProducer;
        }

        @Override
        public void onStatus(Status status) {
            String source = status.getSource();
            String description = status.getText();
            Date start = status.getCreatedAt();
            Date end = status.getCreatedAt();
            LOGGER.info(status.getText());
            GeoLocation geoLocation = status.getGeoLocation();
            if(geoLocation != null) {
                LatLong latLong = new LatLong(geoLocation.getLatitude(), geoLocation.getLongitude());
                Event event = new Event(latLong, start, end, description, source);
                this.dataProducer.push(event);
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
    }
}
