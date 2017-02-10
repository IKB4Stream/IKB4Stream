package com.waves_rsp.ikb4stream.core.metrics;

import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Define properties to connect to the influx database for metrics
 */
public class MetricsProperties {
    private final String host;
    private final String user;
    private final String password;
    private final String dbName;
    private final String measurement;

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsProperties.class);

    private MetricsProperties() {
        PropertiesManager propertiesManager = PropertiesManager.getInstance(MetricsProperties.class, "resources/config.properties");
        this.host = checkArgument(propertiesManager.getProperty("database.metrics.host"));
        this.user = checkArgument(propertiesManager.getProperty("database.metrics.user"));
        this.password = checkArgument(propertiesManager.getProperty("database.metrics.password"));
        this.dbName = checkArgument(propertiesManager.getProperty("database.metrics.datasource"));
        this.measurement = checkArgument(propertiesManager.getProperty("database.metrics.measurement"));
        LOGGER.info("properties for influx db have been loaded");
    }

    /**
     * Singleton to get properties from influx database
     *
     * @return the object instance for MetricsProperties
     */
    public static MetricsProperties create() {
        return new MetricsProperties();
    }

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDbName() {
        return dbName;
    }

    private static String checkArgument(String argument) {
        Objects.requireNonNull(argument);
        if(argument.isEmpty())
            throw new IllegalArgumentException(argument+" can't be empty");
        return argument;
    }

    public String getMeasurement() {
        return measurement;
    }
}
