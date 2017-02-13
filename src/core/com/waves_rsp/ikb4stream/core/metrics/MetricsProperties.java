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
        } catch (IllegalArgumentException e) {
            LOGGER.error("database.metrics.host is not set");
            throw new IllegalStateException("Invalid configuration: database.metrics.host");
        }
        try {
            PROPERTIES_MANAGER.getProperty("database.metrics.user");
        } catch (IllegalArgumentException e) {
            LOGGER.error("database.metrics.user is not set");
            throw new IllegalStateException("Invalid configuration: database.metrics.user");
        }
        try {
            PROPERTIES_MANAGER.getProperty("database.metrics.password");
        } catch (IllegalArgumentException e) {
            LOGGER.error("database.metrics.password is not set");
            throw new IllegalStateException("Invalid configuration: database.metrics.password");
        }
        try {
            PROPERTIES_MANAGER.getProperty("database.metrics.datasource");
        } catch (IllegalArgumentException e) {
            LOGGER.error("database.metrics.datasource is not set");
            throw new IllegalStateException("Invalid configuration: database.metrics.datasource");
        }
        try {
            PROPERTIES_MANAGER.getProperty("database.metrics.measurement");
        } catch (IllegalArgumentException e) {
            LOGGER.error("database.metrics.measurement is not set");
            throw new IllegalStateException("Invalid configuration: database.metrics.measurement");
        }
    }
}
