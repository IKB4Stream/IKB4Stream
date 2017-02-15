package com.waves_rsp.ikb4stream.core.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * GeoCoderJacksonParser allows to get a LatLong from a standard address position
 */
public class GeoCoderJacksonParser {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(GeoCoderJacksonParser.class, "resources/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(GeoCoderJacksonParser.class);
    private static final String LONGITUDE = "lon";
    private static final String LATITUDE = "lat";

    /**
     * Constructor that do nothing more
     */
    public GeoCoderJacksonParser() {
        // Do nothing else
    }

    /**
     * Get a LatLong from an address
     * @param address Address to find
     * @return Return a LatLong position of {@param address}
     * @throws NullPointerException if {@param address} is null
     * @throws IllegalArgumentException if {@param address} is invalid
     * @throws IllegalStateException if geocode.url is invalid
     */
    public LatLong parse(String address) {
        Objects.requireNonNull(address);
        InputStream is = null;
        LatLong coords = null;

        if (!address.isEmpty()) {
            try {
                URL geocodeUrl = createURL(address);
                is = geocodeUrl.openStream();
                coords = parse(is);
            } catch (IllegalArgumentException e) {
                LOGGER.error(e.getMessage());
                throw new IllegalArgumentException(e.getMessage());
            } catch (IllegalStateException e) {
                LOGGER.error(e.getMessage());
                throw new IllegalStateException(e.getMessage());
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            } finally {
                closeInputStream(is);
            }
        }
        return coords;
    }
    
    /**
     * Parse json stream to get a LatLong
     * @param jsonStream json stream data
     * @return LatLong in json
     * @throws NullPointerException if {@param jsonStream} is null
     * @throws IllegalArgumentException if {@param jsonStream} cannot be parse
     * @throws IllegalStateException if InputStream has been interrupt
     */
    private static LatLong parse(InputStream jsonStream) {
        Objects.requireNonNull(jsonStream);
        LatLong coordinate;
        final ObjectMapper mapper = new ObjectMapper();
        try {
            List<Object> dealData = mapper.readValue(jsonStream, List.class);
            coordinate = createLatLong(dealData);
        } catch (JsonParseException | JsonMappingException e) {
            LOGGER.error("Invalid JSON to parse: " + e.getMessage());
            throw new IllegalArgumentException("Invalid JSON to parse: " + e.getMessage());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
        return coordinate;
    }

    /**
     * Create LatLong from Longitude and Latitude
     * @param dealData List of Map which has city and its position
     * @return LatLong of a position
     */
    private static LatLong createLatLong(List<Object> dealData) {
        if (dealData != null && dealData.size() == 1) {
            Map<String, Object> locationMap = (Map<String, Object>) dealData.get(0);
            if (locationMap != null && locationMap.containsKey(LATITUDE) && locationMap.containsKey(LONGITUDE)) {
                double lat = Double.parseDouble(locationMap.get(LATITUDE).toString());
                double lng = Double.parseDouble(locationMap.get(LONGITUDE).toString());
                return new LatLong(lat, lng);
            }
        }
        return null;
    }

    /**
     * Close InputStream
     * @param is InputSteram to close
     */
    private static void closeInputStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    /**
     * Create a URL to request Geocode API
     * @param address Address to find with API
     * @return Complete URL
     * @throws NullPointerException if {@param address} is null
     * @throws IllegalArgumentException if {@param address} is invalid
     * @throws IllegalStateException if there isn't geocode.url in property file
     */
    private static URL createURL(String address) {
        Objects.requireNonNull(address);
        URL url;
        try {
            String addressEncode = URLEncoder.encode(address, "utf-8");
            String geocodeURL = PROPERTIES_MANAGER.getProperty("geocode.url");
            String formattedUrl = geocodeURL + addressEncode;
            url = new URL(formattedUrl);
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.error("Property geocode.url not found");
            throw new IllegalStateException("Property geocode.url not found");
        }
        return url;
    }
}