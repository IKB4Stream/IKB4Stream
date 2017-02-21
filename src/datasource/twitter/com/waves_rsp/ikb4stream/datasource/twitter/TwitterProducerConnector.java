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

import java.util.*;

/**
 * Listen any events provided by the twitter api and load them into a IDataProducer object.
 *
 */
public class TwitterProducerConnector implements IProducerConnector {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(TwitterProducerConnector.class, "resources/datasource/twitter/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterProducerConnector.class);
    private final ConfigurationBuilder confBuilder = new ConfigurationBuilder();
    private final String source = PROPERTIES_MANAGER.getProperty("twitter.source");
    private final double latitudeMax;
    private final double latitudeMin;
    private final double longitudeMax;
    private final double longitudeMin;

    /**
     * Instantiate the object
     */
    public TwitterProducerConnector() {
        loadTwitterProperties();
        try {
            latitudeMax = Double.valueOf(PROPERTIES_MANAGER.getProperty("twitter.latitude.maximum"));
            latitudeMin = Double.valueOf(PROPERTIES_MANAGER.getProperty("twitter.latitude.minimum"));
            longitudeMax = Double.valueOf(PROPERTIES_MANAGER.getProperty("twitter.longitude.maximum"));
            longitudeMin = Double.valueOf(PROPERTIES_MANAGER.getProperty("twitter.longitude.minimum"));
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid properties {}", e.getMessage());
            throw new IllegalStateException(e);
        }
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
            TwitterStreamListener streamListener = new TwitterStreamListener(dataProducer);
            twitterStream = new TwitterStreamFactory(confBuilder.build()).getInstance();
            twitterStream.addListener(streamListener);
            FilterQuery filterQuery = new FilterQuery();
            filterQuery.locations(new double[]{longitudeMin, latitudeMin, longitudeMax, latitudeMax});
            twitterStream.filter(filterQuery);
            twitterStream.sample("fr");

            Thread.currentThread().join();
        }catch (IllegalArgumentException | IllegalStateException err) {
            LOGGER.error("Error loading : " + err.getMessage());
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

    @Override
    public boolean isActive() {
        try {
            return Boolean.valueOf(PROPERTIES_MANAGER.getProperty("twitter.enable"));
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    /**
     * Load properties for twitter connector
     */
    private void loadTwitterProperties() {
        try {
            confBuilder.setOAuthAccessToken(PROPERTIES_MANAGER.getProperty("twitter.key.auth.accesstoken"));
            confBuilder.setOAuthAccessTokenSecret(PROPERTIES_MANAGER.getProperty("twitter.secret.auth.accesstoken"));
            confBuilder.setOAuthConsumerKey(PROPERTIES_MANAGER.getProperty("twitter.key.consumer.accesstoken"));
            confBuilder.setOAuthConsumerSecret(PROPERTIES_MANAGER.getProperty("twitter.secret.consumer.accesstoken"));
            confBuilder.setJSONStoreEnabled(true);
        }catch (IllegalArgumentException err) {
            LOGGER.error("Load Twitter Properties {} ", err.getMessage());
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
            String description = status.getText();
            Date start = status.getCreatedAt();
            Date end = status.getCreatedAt();
            User user = status.getUser();
            LatLong[] latLong = getLatLong(status);

            if(latLong.length > 0) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.append("description", description);
                    jsonObject.append("user_certified", user.isVerified());
                    Event event;
                    if (latLong.length == 1) {
                        event = new Event(latLong[0], start, end, jsonObject.toString(), source);
                    } else {
                        event = new Event(latLong, start, end, jsonObject.toString(), source);
                    }
                    this.dataProducer.push(event);
                } catch (JSONException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }

        private LatLong[] getLatLong(Status status) {
            if (status.getGeoLocation() != null) {
                return new LatLong[] {new LatLong(status.getGeoLocation().getLatitude(), status.getGeoLocation().getLongitude())};
            } else if (status.getPlace() != null && status.getPlace().getBoundingBoxCoordinates() != null) {
                return getLatLongFromBoudingBox(status.getPlace().getBoundingBoxCoordinates());
            } else {
                return new LatLong[] {
                        new LatLong(latitudeMin, longitudeMin),
                        new LatLong(latitudeMax, longitudeMin),
                        new LatLong(latitudeMax, longitudeMax),
                        new LatLong(latitudeMin, longitudeMax),
                        new LatLong(latitudeMin, longitudeMin)
                };
            }
        }

        private LatLong[] getLatLongFromBoudingBox(GeoLocation[][] geoLocations) {
            List<LatLong> latLongList = new ArrayList<>();
            Arrays.stream(geoLocations)
                    .forEach(arrayGeo -> Arrays.stream(arrayGeo)
                            .forEach(geo -> latLongList.add(new LatLong(geo.getLatitude(), geo.getLongitude()))));
            LatLong[] latLong = new LatLong[latLongList.size() + 1];
            for (int i = 0; i < latLongList.size(); i++) {
                latLong[i] = latLongList.get(i);
            }
            latLong[latLong.length - 1] = latLong[0];
            return latLong;
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
