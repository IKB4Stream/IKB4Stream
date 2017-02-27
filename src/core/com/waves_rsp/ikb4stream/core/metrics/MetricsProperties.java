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
 */
public class MetricsProperties {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(MetricsProperties.class, "resources/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsProperties.class);
    private final String host;
    private final String user;
    private final String password;
    private final String dbName;
    private final String measurement;


    /**
     * Create MetricsProperties with configuration into configuration file
     * @throws IllegalStateException If configuration of Influx is not correct
     */
    private MetricsProperties() {
        checkValid();
        this.host = PROPERTIES_MANAGER.getProperty("database.metrics.host");
        this.user = PROPERTIES_MANAGER.getProperty("database.metrics.user");
        this.password = PROPERTIES_MANAGER.getProperty("database.metrics.password");
        this.dbName = PROPERTIES_MANAGER.getProperty("database.metrics.datasource");
        this.measurement = PROPERTIES_MANAGER.getProperty("database.metrics.measurement");
        LOGGER.info("properties for influx db have been loaded");
    }

    /**
     * Singleton to get properties from influx database
     * @return the object instance for MetricsProperties
     * @throws IllegalStateException If configuration of Influx is not correct
     */
    public static MetricsProperties create() {
        return new MetricsProperties();
    }

    /**
     * Get host of influx database
     * @return Host of influx database
     */
    public String getHost() {
        return host;
    }

    /**
     * Get user of influx database
     * @return User of influx database
     */
    public String getUser() {
        return user;
    }

    /**
     * Get password of influx database
     * @return Password of influx database
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get database name of influx database
     * @return Database name of influx database
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * Get table of metrics from influx database
     * @return Table's name of Measurement
     */
    public String getMeasurement() {
        return measurement;
    }

    /**
     * Check if configuration is valid
     * @throws IllegalStateException if any property is not set
     */
    private static void checkValid() {
        try {
            PROPERTIES_MANAGER.getProperty("database.metrics.host");
            PROPERTIES_MANAGER.getProperty("database.metrics.user");
            PROPERTIES_MANAGER.getProperty("database.metrics.password");
            PROPERTIES_MANAGER.getProperty("database.metrics.datasource");
            PROPERTIES_MANAGER.getProperty("database.metrics.measurement");
        } catch (IllegalArgumentException e) {
            LOGGER.error("Bad properties read: {}", e);
            throw new IllegalStateException(e.getMessage());
        }
    }
}
