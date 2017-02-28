package com.waves_rsp.ikb4stream.core.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.Objects;


public class Geocoder {

    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(Geocoder.class, "resources/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(Geocoder.class);
    private final LatLong latLong;
    private final LatLong[] bbox;

    /**
     * Private constructor
     */
    private Geocoder(LatLong latLong, LatLong[] bbox) {
        this.latLong = latLong;
        this.bbox = bbox;
    }

    public LatLong getLatLong() {
        return latLong;
    }

    public LatLong[] getBbox() {
        return bbox;
    }

    /**
     * Geocode an address with calling the Photon API
     *
     * @param address to geolocalize
     * @return a new Geocoder
     */
    public static Geocoder geocode(String address) {
        Objects.requireNonNull(address);
        InputStream is = null;

        if (!address.isEmpty()) {
            try {
                URL geocodeUrl = createURL(address);
                is = geocodeUrl.openStream();
                return parseAddress(is);

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
        return new Geocoder(null, null);
    }

    /**
     * Parse GeoJSON from Photon geocoder API
     *
     * @param jsonStream is the response from Photon
     * @return a Geocoder object with a @LatLong and a bbox
     */
    private static Geocoder parseAddress(InputStream jsonStream) {
        Objects.requireNonNull(jsonStream);
        LatLong coordinate = null;
        LatLong[] bbox = new LatLong[5];
        final ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(jsonStream);
            //root
            JsonNode rootNode = root.path("features");

            for (JsonNode knode : rootNode) {
                JsonNode geometry = knode.path("geometry");
                JsonNode props = knode.path("properties");

                JsonNode coord = geometry.get("coordinates");
                JsonNode extent = props.get("extent");

                coordinate = new LatLong(coord.get(1).asDouble(), coord.get(0).asDouble());

                if (extent != null) {
                    double lonMin = extent.get(0).asDouble();
                    double latMin = extent.get(1).asDouble();
                    double lonMax = extent.get(2).asDouble();
                    double latMax = extent.get(3).asDouble();
                    bbox[0] = new LatLong(latMin, lonMin);
                    bbox[1] = new LatLong(latMax, lonMin);
                    bbox[2] = new LatLong(latMax, lonMax);
                    bbox[3] = new LatLong(latMin, lonMax);
                    bbox[4] = new LatLong(latMin, lonMin); //last point = first point
                } else {
                    extent = null; //put bbox = null if extent is null
                }
            }
            return new Geocoder(coordinate, bbox);
        } catch (JsonParseException | JsonMappingException e) {
            LOGGER.error("Invalid JSON to parse: " + e.getMessage());
            throw new IllegalArgumentException("Invalid JSON to parse: " + e.getMessage());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * Create a URL to request Geocode API
     *
     * @param address Address to find with API
     * @return Complete URL
     * @throws NullPointerException     if {@param address} is null
     * @throws IllegalArgumentException if {@param address} is invalid
     * @throws IllegalStateException    if there isn't geocode.url in property file
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

    /**
     * Close InputStream
     *
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
}

