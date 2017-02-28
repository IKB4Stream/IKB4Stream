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
import java.util.Objects;
import java.util.Optional;

/**
 * Defines the connector to influx database and instances it
 * @author ikb4stream
 * @version 1.0
 */
public class MetricsConnector {
    /**
     * Properties of this class
     * @see PropertiesManager#getInstance(Class)
     * @see PropertiesManager#getProperty(String)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(MetricsConnector.class);
    /**
     * Logger used to log all information in this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsConnector.class);
    /**
     * Single instance of {@link MetricsConnector}
     */
    private static final MetricsConnector METRICS_CONNECTOR = new MetricsConnector();
    /**
     * Properties of InfluxDB
     * @see MetricsConnector#checkInfluxConnexion()
     * @see MetricsConnector#getProperties()
     */
    private final MetricsProperties properties = MetricsProperties.create();
    /**
     * Internal class of {@link MetricsConnector}
     * @see MetricsConnectorService
     * @see MetricsConnectorService#getInfluxDB()
     * @see MetricsConnector#getInfluxDB()
     * @see MetricsConnector#getConnectorService()
     * @see MetricsConnector#close()
     */
    private final MetricsConnectorService connectorService;
    /**
     * Status of InfluxDB
     * @see MetricsConnector#isConnexionEnabled()
     */
    private final boolean isConnexionEnabled;

    /**
     * Instantiate a {@link MetricsConnector} with initialisation of InfluxDB
     */
    private MetricsConnector() {
        this.isConnexionEnabled = Boolean.valueOf(PROPERTIES_MANAGER.getProperty("database.connexion.enabled"));
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
        return METRICS_CONNECTOR;
    }

    /**
     * Close the connexion with influx
     * @see MetricsConnector#connectorService
     */
    public void close() {
        if(connectorService!= null && connectorService.getInfluxDB() != null) {
            connectorService.getInfluxDB().disableBatch();
            connectorService.getInfluxDB().close();
            LOGGER.info("Connexion to the influx database " + connectorService.getInfluxDB().version() + " stopped");
        }
    }

    /**
     * Get the state of {@link MetricsConnector}
     * @return Status of {@link MetricsConnector}
     * @see MetricsConnector#isConnexionEnabled
     */
    boolean isConnexionEnabled() {
        return isConnexionEnabled;
    }

    /**
     * Get an instance of InfluxDB object
     * @return Instance of InfluxDB associate to {@link MetricsConnector#connectorService}
     * @see MetricsConnector#connectorService
     */
    InfluxDB getInfluxDB() {
        if(connectorService == null) {
            return null;
        }

        Optional<InfluxDB> influxDBOptional = Optional.of(getConnectorService().getInfluxDB());
        return influxDBOptional.orElse(null);
    }

    /**
     * Get {@link MetricsProperties} associate to instance of {@link MetricsConnector}
     * @return {@link MetricsProperties} object
     * @see MetricsConnector#properties
     */
    public MetricsProperties getProperties() {
        return properties;
    }

    /**
     * Get {@link MetricsConnectorService} associate to instance of {@link MetricsConnector}
     * @return {@link MetricsConnectorService} object
     * @see MetricsConnector#connectorService
     */
    public MetricsConnectorService getConnectorService() {
        return connectorService;
    }

    /**
     * Try to connect to the influxDB with Influx factory
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
     * @author ikb4stream
     * @version 1.0
     * Encapsulate an InfluxDB object in order to instantiate it
     */
    private class MetricsConnectorService {
        /**
         * InfluxDB java object
         * @see MetricsConnectorService#getInfluxDB()
         */
        private final InfluxDB influxDB;

        /**
         * Allow instantiation only in {@link MetricsConnector}
         * @param influxDB Copy of influxDB object
         * @throws NullPointerException if influxDB is null
         */
        private MetricsConnectorService(InfluxDB influxDB) {
            Objects.requireNonNull(influxDB);
            this.influxDB = influxDB;
        }

        /**
         * Get instance of {@link MetricsConnectorService#influxDB}
         * @return {@link InfluxDB} object
         * @see InfluxDB
         * @see MetricsConnectorService#influxDB
         */
        InfluxDB getInfluxDB() {
            return influxDB;
        }
    }
}
