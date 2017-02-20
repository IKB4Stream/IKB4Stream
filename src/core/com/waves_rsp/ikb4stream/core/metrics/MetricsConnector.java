package com.waves_rsp.ikb4stream.core.metrics;

import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            InfluxDB influxDB = InfluxDBFactory.connect(properties.getHost(), properties.getUser(), properties.getPassword());

            try {
                String collection = PROPERTIES_MANAGER.getProperty("database.metrics.datasource");
                influxDB.createDatabase(collection);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(e.getMessage());
            }
            this.connectorService = new MetricsConnectorService(influxDB);
            LOGGER.info("Connexion to the influx database " + influxDB.version() + " for metrics is started");
        }
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
        if(connectorService!= null && connectorService.getInfluxDB() != null) {
            connectorService.getInfluxDB().disableBatch();
            connectorService.getInfluxDB().close();
            LOGGER.info("Connexion to the influx database " + connectorService.getInfluxDB().version() + " stopped");
        }
    }

    public boolean isConnexionEnabled() {
        return isConnexionEnabled;
    }


    public InfluxDB getInfluxDB() {
        Optional<InfluxDB> influxDBOptional = Optional.of(getConnectorService().getInfluxDB());
        return influxDBOptional.orElse(null);

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
