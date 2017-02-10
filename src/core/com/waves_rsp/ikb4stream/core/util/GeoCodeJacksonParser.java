package com.waves_rsp.ikb4stream.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waves_rsp.ikb4stream.core.model.LatLong;


public class GeoCodeJacksonParser {

    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "lon";

    private LatLong parse(final InputStream jsonStream) {
        LatLong coordinate = null;
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final List<Object> dealData = mapper.readValue(jsonStream, List.class);
            if (dealData != null && dealData.size() == 1) {
                final Map<String, Object> locationMap = (Map<String, Object>) dealData.get(0);
                if (locationMap != null && locationMap.containsKey(LATITUDE) && locationMap.containsKey(LONGITUDE)) {
                    final double lat = Double.parseDouble(locationMap.get(LATITUDE).toString());
                    final double lng = Double.parseDouble(locationMap.get(LONGITUDE).toString());
                    coordinate = new LatLong(lat, lng);
                }
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
        return coordinate;
    }

    public LatLong parse(String fullAddress) {
        InputStream is = null;
        LatLong coords = null;

        if (fullAddress != null && fullAddress.length() > 0) {
            try {
                String address = URLEncoder.encode(fullAddress, "utf-8");
                String geocodeURL = "http://nominatim.openstreetmap.org/search?format=json&limit=1&polygon=0&addressdetails=0&email=contact@EMAIL.ME&countrycodes=fr&q=";
                //query google geocode api
                String formattedUrl = geocodeURL + address;
                URL geocodeUrl = new URL(formattedUrl);
                is = geocodeUrl.openStream();
                coords = parse(is);
            } catch (IOException ex) {
                Logger.getLogger(GeoCodeJacksonParser.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    Logger.getLogger(GeoCodeJacksonParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return coords;
    }

}