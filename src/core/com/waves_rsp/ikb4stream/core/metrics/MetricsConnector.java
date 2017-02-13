package com.waves_rsp.ikb4stream.core.metrics;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the connector to influx database and instances it
 */
public class MetricsConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsConnector.class);
    private final MetricsConnectorService connectorService;
    private final MetricsProperties properties;

    private MetricsConnector() {
        this.properties = MetricsProperties.create();
        InfluxDB influxDB = InfluxDBFactory.connect(properties.getHost(), properties.getUser(), properties.getPassword());
        this.connectorService = new MetricsConnectorService(influxDB);
        LOGGER.info("Connexion to the influx database " + influxDB.version() + " for metrics is started");
    }

    /**
     * Instantiates the influx connector for metrics
     * @return metrics connector
     */
    public static MetricsConnector getMetricsConnector() {
        return new MetricsConnector();
    }

    /**
     * Close the connexion with influx
     */
    public void close() {
        connectorService.getInfluxDB().close();
        LOGGER.info("Connexion to the influx database " + connectorService.getInfluxDB().version() + " stopped");
    }

    public InfluxDB getInfluxDB() {
        return this.getConnectorService().getInfluxDB();
    }

    public MetricsProperties getProperties() {
        return properties;
    }

    public MetricsConnectorService getConnectorService() {
        return connectorService;
    }

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
