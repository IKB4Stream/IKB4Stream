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

package com.waves_rsp.ikb4stream.core.metrics;

import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.Optional;

/**
 * Defines the connector to influx database and instances it
 */
public class MetricsConnector {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(MetricsConnector.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsConnector.class);
    private final MetricsConnectorService connectorService;
    private final MetricsProperties properties;
    private final boolean isConnexionEnabled;

    private MetricsConnector() {
        this.properties = MetricsProperties.create();
        this.isConnexionEnabled = Boolean.valueOf(PROPERTIES_MANAGER.getProperty("database.connexion.enabled"));
        LOGGER.info("is influxdb connexion enabled : "+isConnexionEnabled);

        if(!isConnexionEnabled) {
            LOGGER.warn("Connexion to influxdb disabled.");
            this.connectorService = null;
        }else {
            InfluxDB influxDB = null;
            try {
                influxDB = checkInfluxConnexion();
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Bad connexion properties loaded: ", e.getMessage());
                throw new IllegalStateException(e.getMessage());
            } catch (RuntimeException | ConnectException e) {
                LOGGER.error("Can't connect to the influx service: {}", e);
            }

            if(influxDB != null) {
                this.connectorService = new MetricsConnectorService(influxDB);
                LOGGER.info("Connexion to the influx database " + influxDB.version() + " for metrics is started");
            }else {
                this.connectorService = null;
            }
        }
    }

    /**
     * Instantiates the influx connector for metrics
     * @return metrics connector
     */
    static MetricsConnector getMetricsConnector() {
        return new MetricsConnector();
    }

    /**
     * Close the connexion with influx
     */
    public void close() {
        if(connectorService!= null && connectorService.getInfluxDB() != null) {
            connectorService.getInfluxDB().disableBatch();
            connectorService.getInfluxDB().close();
            LOGGER.info("Connexion to the influx database " + connectorService.getInfluxDB().version() + " stopped");
        }
    }

    boolean isConnexionEnabled() {
        return isConnexionEnabled;
    }

    InfluxDB getInfluxDB() {
        if(connectorService == null) {
            return null;
        }

        Optional<InfluxDB> influxDBOptional = Optional.of(getConnectorService().getInfluxDB());
        return influxDBOptional.orElse(null);
    }

    public MetricsProperties getProperties() {
        return properties;
    }

    public MetricsConnectorService getConnectorService() {
        return connectorService;
    }

    /**
     * Try to connect to the influxDB with Influx factory
     *
     * @return the instance of influx object
     * @throws ConnectException if the connexion has failed
     */
    private InfluxDB checkInfluxConnexion() throws ConnectException {
        InfluxDB influxDB = InfluxDBFactory.connect(properties.getHost(), properties.getUser(), properties.getPassword());
        String collection = PROPERTIES_MANAGER.getProperty("database.metrics.datasource");
        influxDB.createDatabase(collection);
        return influxDB;
    }

    /**
     * Encapsulate an InfluxDB object in order to instantiate it
     */
    private class MetricsConnectorService {
        private final InfluxDB influxDB;

        private MetricsConnectorService(InfluxDB influxDB) {
            this.influxDB = influxDB;
        }

        InfluxDB getInfluxDB() {
            return influxDB;
        }
    }
}
