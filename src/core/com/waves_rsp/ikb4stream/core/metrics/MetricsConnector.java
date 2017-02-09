package com.waves_rsp.ikb4stream.core.metrics;

import com.waves_rsp.ikb4stream.core.communication.ICommunication;
import com.waves_rsp.ikb4stream.core.communication.IDatabaseReader;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Defines the connector to influx database and instances it
 */
public class MetricsConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsConnector.class);
    private final InfluxDB influxDB;
    private final MetricsProperties properties;

    private MetricsConnector() {
        this.properties = MetricsProperties.create();
        this.influxDB = InfluxDBFactory.connect(properties.getHost(), properties.getUser(), properties.getPassword());
        LOGGER.info("Connexion to the influx database "+influxDB.version()+" for metrics is started");
    }

    /**
     * Instanciates the influx connector for metrics
     *
     * @return metrics connector
     */
    public static MetricsConnector getMetricsConnector() {
        return new MetricsConnector();
    }

    /**
     * Close the connexion with influx
     */
    public void close() {
        influxDB.close();
        LOGGER.info("Connexion to the influx database "+influxDB.version()+" stopped");
    }

    public InfluxDB getInfluxDB() {
        return influxDB;
    }

    public MetricsProperties getProperties() {
        return properties;
    }
}
