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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Define properties to connect to the influx database for metrics
 * @author ikb4stream
 * @version 1.0
 */
public class MetricsProperties {
    /**
     * Properties of this class
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(MetricsProperties.class);
    /**
     * Logger used to log all information in this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsProperties.class);
    /**
     * Single instance of {@link MetricsProperties}
     */
    private static final MetricsProperties METRICS_PROPERTIES = new MetricsProperties();
    /**
     * Host of InfluxDB
     * @see MetricsProperties#getHost()
     */
    private final String host;
    /**
     * Username to log in into InfluxDB
     * @see MetricsProperties#getUser()
     */
    private final String user;
    /**
     * Password associate to {@link MetricsProperties#host}
     * @see MetricsProperties#getPassword()
     */
    private final String password;
    /**
     * Database name where values will be store
     * @see MetricsProperties#getDbName()
     */
    private final String dbName;
    /**
     * Name of the collection into {@link MetricsProperties#dbName}
     * @see MetricsProperties#getMeasurement()
     */
    private final String measurement;

    /**
     * Create MetricsProperties with configuration into configuration file
     * @throws IllegalStateException If configuration of Influx is not correct
     */
    private MetricsProperties() {
        try {
            this.host = PROPERTIES_MANAGER.getProperty("database.metrics.host");
            this.user = PROPERTIES_MANAGER.getProperty("database.metrics.user");
            this.password = PROPERTIES_MANAGER.getProperty("database.metrics.password");
            this.dbName = PROPERTIES_MANAGER.getProperty("database.metrics.datasource");
            this.measurement = PROPERTIES_MANAGER.getProperty("database.metrics.measurement");
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException(e);
        }
        LOGGER.info("properties for influx db have been loaded");
    }

    /**
     * Singleton to get properties from influx database
     * @return the object instance for MetricsProperties
     * @throws IllegalStateException If configuration of Influx is not correct
     * @see MetricsProperties#METRICS_PROPERTIES
     */
    public static MetricsProperties create() {
        return METRICS_PROPERTIES;
    }

    /**
     * Get host of influx database
     * @return Host of influx database
     * @see MetricsProperties#host
     */
    public String getHost() {
        return host;
    }

    /**
     * Get user of influx database
     * @return User of influx database
     * @see MetricsProperties#user
     */
    public String getUser() {
        return user;
    }

    /**
     * Get password of influx database
     * @return Password of influx database
     * @see MetricsProperties#password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get database name of influx database
     * @return Database name of influx database
     * @see MetricsProperties#dbName
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * Get table of metrics from influx database
     * @return Table's name of Measurement
     * @see MetricsProperties#measurement
     */
    public String getMeasurement() {
        return measurement;
    }
}
