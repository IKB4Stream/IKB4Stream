/*
 * Copyright (C) 2017 ikb4stream team
 * ikb4stream is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * ikb4stream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

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

/**
 * {@link Geocoder} allows to get a LatLong from a standard address position
 *
 * @author ikb4stream
 * @version 1.0
 */
public class Geocoder {
    /**
     * Properties of this class
     *
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(Geocoder.class, "resources/config.properties");
    /**
     * Logger used to log all information in this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Geocoder.class);
    /**
     *
     */
    private final LatLong latLong;
    /**
     *
     */
    private final LatLong[] bbox;

    /**
     * Private constructor to block instantiation
     *
     * @throws NullPointerException if latlong or bbox is null
     * @see Geocoder#latLong
     * @see Geocoder#bbox
     */
    private Geocoder(LatLong latLong, LatLong[] bbox) {
        Objects.requireNonNull(latLong);
        Objects.requireNonNull(bbox);
        this.latLong = latLong;
        this.bbox = bbox;
    }

    /**
     * Get an exact position with a {@link LatLong}
     *
     * @return {@link LatLong}
     */
    public LatLong getLatLong() {
        return latLong;
    }

    /**
     * Get a bounding box as an array of {@link LatLong}
     *
     * @return Array of {@link LatLong}
     */
    public LatLong[] getBbox() {
        return bbox;
    }

    /**
     * Geocode an address with calling the Photon API
     *
     * @param address to geolocalize
     * @return a new {@link Geocoder}
     * @throws NullPointerException if address is null
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
     * @return a {@link Geocoder} object with a {@link LatLong} and an array of {@link LatLong}
     * @throws NullPointerException if jsonStream is null
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
     * @throws NullPointerException     if address is null
     * @throws IllegalArgumentException if address is invalid
     * @throws IllegalStateException    if there isn't geocode.url in property file
     */
    private static URL createURL(String address) {
        Objects.requireNonNull(address);
        URL url;
        try {
            String addressEncode = URLEncoder.encode(address, "utf-8");
            String geocodeURL = PROPERTIES_MANAGER.getPropertyOrDefault("geocode.url", "http://photon.komoot.de/api/?osm_tag=place&lang=fr&limit=1&q=");
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
