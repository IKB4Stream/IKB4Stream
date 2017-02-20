package com.aves_rsp.ikb4stream.datasource.owm;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waves_rsp.ikb4stream.core.model.Event;
import net.aksingh.owmjapis.CurrentWeather;
import net.aksingh.owmjapis.OpenWeatherMap;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

/**
 * Created by ikb4stream on 15/02/17.
 */
public class OWMProducerConnector implements IProducerConnector{
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(OWMProducerConnector.class, "resources/datasource/owm/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(OWMProducerConnector.class);
    private final String source;
    private final String owmKey;
    private final double latitude;
    private final double longitude;
    private final Long requestInterval;
    private final OpenWeatherMap openWeatherMap;

    public OWMProducerConnector() {
        try {
            this.source = PROPERTIES_MANAGER.getProperty("OWMProducerConnector.source");
            this.owmKey = PROPERTIES_MANAGER.getProperty("OWMProducerConnector.key");
            this.latitude =  Double.valueOf(PROPERTIES_MANAGER.getProperty("OWMProducerConnector.latitude"));
            this.longitude =  Double.valueOf(PROPERTIES_MANAGER.getProperty("OWMProducerConnector.longitude"));
            this.requestInterval = Long.valueOf(PROPERTIES_MANAGER.getProperty("OWMProducerConnector.sleep"));
            this.openWeatherMap = new OpenWeatherMap(owmKey);
            this.openWeatherMap.setLang(OpenWeatherMap.Language.FRENCH);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalArgumentException("Invalid configuration");
        }
    }

    /**
     *
     * @param latitude coordinate
     * @param longitude coordinate
     * @return an Event which contains information about current weather
     * @throws IOException
     */
    private Event getCurrentWeather(double latitude, double longitude) {
        Objects.requireNonNull(latitude);
        Objects.requireNonNull(longitude);
        ObjectMapper  objectMapper = new ObjectMapper();
        CurrentWeather currentWeather = openWeatherMap.currentWeatherByCoordinates((float)latitude, (float)longitude);

        try{
            JsonNode jn = objectMapper.readTree(currentWeather.getRawResponse());
            String description = currentWeather.getRawResponse().toString();
            LatLong latLong = new LatLong(Double.valueOf(jn.path("coord").path("lat").toString()), Double.valueOf(jn.path("coord").path("lon").toString()));
            Date start = new Date(Long.valueOf(jn.path("dt").toString())*1000);
            Date end = new Date(start.getTime()+requestInterval-1000);
            return new Event(latLong, start, end, description, this.source);
        }catch ( NumberFormatException e){
            LOGGER.warn("value of() failed: {}", e.getMessage());
            return null;
        }catch (IOException e){
            LOGGER.warn("Current weather failed: {}", e.getMessage());
            return  null;
        }
    }

    @Override
    public boolean isActive() {
        try {
            return Boolean.valueOf(PROPERTIES_MANAGER.getProperty("OWMProducerConnector.enable"));
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Event event = getCurrentWeather(this.latitude, this.longitude);
                if(event!= null) {
                    dataProducer.push(event);
                }
                Thread.sleep(requestInterval);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            } finally {
                Thread.currentThread().interrupt();
            }
        }
    }
}
