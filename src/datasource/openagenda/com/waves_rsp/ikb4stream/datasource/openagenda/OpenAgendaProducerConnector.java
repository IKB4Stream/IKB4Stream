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

package com.waves_rsp.ikb4stream.datasource.openagenda;


import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.IOpenAgenda;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Objects;

public class OpenAgendaProducerConnector implements IOpenAgenda {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(OpenAgendaProducerConnector.class, "resources/datasource/openagenda/config.properties");
    private final String source;
    private final String propDateStart;
    private final String propDateEnd;
    private final String bbox;
    private final long sleepTime;

    /**
     * Instantiate the OpenAgendaMock object with load properties to connect to the OPen Agenda API
     */
    public OpenAgendaProducerConnector() {
        try {
            this.sleepTime = Long.valueOf(PROPERTIES_MANAGER.getProperty("openagenda.sleep_time"));
            this.source = PROPERTIES_MANAGER.getProperty("openagenda.source");
            this.propDateStart = PROPERTIES_MANAGER.getProperty("openagenda.date_start");
            this.propDateEnd = PROPERTIES_MANAGER.getProperty("openagenda.date_end");
            double latMax = Double.valueOf(PROPERTIES_MANAGER.getProperty("openagenda.latitude.maximum"));
            double latMin = Double.valueOf(PROPERTIES_MANAGER.getProperty("openagenda.latitude.minimum"));
            double lonMax = Double.valueOf(PROPERTIES_MANAGER.getProperty("openagenda.longitude.maximum"));
            double lonMin = Double.valueOf(PROPERTIES_MANAGER.getProperty("openagenda.longitude.minimum"));
            this.bbox = "(" + latMin + "," + lonMin + "),(" + latMax + "," + lonMin + "),(" + latMax + "," + lonMax + ")," +
                    "(" + latMin + "," + lonMax + "),(" + latMin + "," + lonMin + ")";
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid properties loaded: {}", e);
            throw new IllegalStateException("Invalid configuration");
        }

    }


    /**
     * Listen events from openAgenda and load them with the data producer object
     *
     * @param dataProducer data producer
     */
    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        InputStream is;
        try {
            is = createURL().openStream();
        } catch (IOException e) {
            LOGGER.error("Cannot connect to the OpenAgenda API : {} ", e);
            throw new IllegalStateException(e.getMessage());
        }
        while (!Thread.currentThread().isInterrupted()) {
            try {
                long start = System.currentTimeMillis();
                List<Event> events = searchEvents(is, this.source);
                long time = System.currentTimeMillis() - start;
                events.forEach(dataProducer::push);
                METRICS_LOGGER.log("time_process_" + this.source, time);
                Thread.sleep(this.sleepTime);
            } catch (InterruptedException e) {
                LOGGER.error("Current thread has been interrupted: {}", e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error("Exception during thread has been interrupted : {} ", e);
                }
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Create the URL from properties (bbox and dates) for accessing to the webservice
     *
     * @return an URL
     */
    private URL createURL() {
        URL url;
        try {
            //bbox
            String bboxEncode = URLEncoder.encode(this.bbox, UTF8);
            String baseURL = PROPERTIES_MANAGER.getProperty("openagenda.url");
            StringBuilder formatURL = new StringBuilder();
            formatURL.append(baseURL).append("&geofilter.polygon=").append(bboxEncode);

            if (!propDateStart.isEmpty()) {
                String dateStartEncode = URLEncoder.encode(this.propDateStart, UTF8);
                formatURL.append("&refine.date_start=").append(dateStartEncode);
            }
            if (!propDateEnd.isEmpty()) {
                String dateEndEncode = URLEncoder.encode(this.propDateEnd, UTF8);
                formatURL.append("&refine.date_end=").append(dateEndEncode);
            }
            url = new URL(formatURL.toString());
        } catch (IOException e) {
            LOGGER.error("Bad url properties found: {}", e);
            throw new IllegalArgumentException(e.getMessage());
        }
        return url;
    }


    @Override
    public boolean isActive() {
        return this.isActive(PROPERTIES_MANAGER.getProperty("openagenda.enable"));
    }

}
