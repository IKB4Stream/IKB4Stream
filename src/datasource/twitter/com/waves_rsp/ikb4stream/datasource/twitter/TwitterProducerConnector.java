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

import java.util.Date;
import java.util.Objects;

/**
 * Listen any events provided by the twitter api and load them into a IDataProducer object.
 *
 */
public class TwitterProducerConnector implements IProducerConnector {
    private final PropertiesManager propertiesManager = PropertiesManager.getInstance(TwitterProducerConnector.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterProducerConnector.class);
    private final ConfigurationBuilder confBuilder = new ConfigurationBuilder();

    private TwitterProducerConnector() {

    }

    public static TwitterProducerConnector getInstance() {
        return new TwitterProducerConnector();
    }

    /**
     * Listen tweets from twitter with a bounding box and load them with the data producer object
     *
     * @param dataProducer
     */
    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        TwitterStream twitterStream = null;
        try {
            loadTwitterProperties();
            double latitudeMax = Double.valueOf(propertiesManager.getProperty("latitude.maximum"));
            double latitudeMin = Double.valueOf(propertiesManager.getProperty("latitude.minimum"));
            double longitudeMax = Double.valueOf(propertiesManager.getProperty("longitude.maximum"));
            double longitudeMin = Double.valueOf(propertiesManager.getProperty("longitude.minimum"));

            TwitterStreamListener streamListener = new TwitterStreamListener(dataProducer);
            twitterStream = new TwitterStreamFactory(confBuilder.build()).getInstance();
            twitterStream.addListener(streamListener);
            FilterQuery filterQuery = new FilterQuery();
            twitterStream.filter(filterQuery);
            filterQuery.locations(new double[]{latitudeMax, latitudeMin, longitudeMax, longitudeMin});

            twitterStream.sample("fr");

            Thread.currentThread().join();
        }catch (IllegalArgumentException | IllegalStateException err) {
            LOGGER.error(err.getMessage());
            throw new IllegalStateException(err.getMessage());
        } catch (InterruptedException e) {
            LOGGER.info("Close twitter");
            Thread.currentThread().interrupt();
        } finally {
            Thread.currentThread().interrupt();
            if(twitterStream != null) {
                twitterStream.shutdown();
            }
        }
    }

    /**
     * Load properties for twitter connector
     */
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
            throw new IllegalArgumentException(err.getMessage());
        }
    }

    /**
     * A status listener in order to get tweets with the method onStatus
     */
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

            LOGGER.info(""+status.getText());
            User user = status.getUser();

            GeoLocation geoLocation = status.getGeoLocation();

            if(geoLocation != null) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.append("description", description);
                    jsonObject.append("user_certified", user.isVerified());
                    LatLong latLong = new LatLong(geoLocation.getLatitude(), geoLocation.getLongitude());
                    Event event = new Event(latLong, start, end, jsonObject.toString(), source);
                    this.dataProducer.push(event);
                    LOGGER.info(event.toString());
                } catch (JSONException e) {
                    LOGGER.error(e.getMessage());
                }
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
