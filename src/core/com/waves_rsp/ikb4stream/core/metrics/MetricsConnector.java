package com.waves_rsp.ikb4stream.core.metrics;

import com.waves_rsp.ikb4stream.core.communication.ICommunication;
import com.waves_rsp.ikb4stream.core.communication.IDatabaseReader;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Define the connector to influx database and instance it
 */
public class MetricsConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsConnector.class);
    private final InfluxDB influxDB;
    private final MetricsProperties properties;

    private MetricsConnector(MetricsProperties properties) {
        this.properties = Objects.requireNonNull(properties);
        this.influxDB = InfluxDBFactory.connect(properties.getHost(), properties.getUser(), properties.getPassword());
        LOGGER.info("Connexion to the influx database "+influxDB.version()+" for metrics is started");
    }

    /**
     * Instanciate the influx connector for metrics
     *
     * @param properties define the connexions properties
     * @return metrics connector
     */
    public static MetricsConnector getMetricsConnector(MetricsProperties properties) {
        return new MetricsConnector(properties);
    }
    
    /**
     * Close the influx connexion
     */
    public void close() {
        //Auto-close from influx
        LOGGER.info("Connexion to the influx database "+influxDB.version()+" has stopped");
    }

    public InfluxDB getInfluxDB() {
        return influxDB;
    }

    public MetricsProperties getProperties() {
        return properties;
    }
}
