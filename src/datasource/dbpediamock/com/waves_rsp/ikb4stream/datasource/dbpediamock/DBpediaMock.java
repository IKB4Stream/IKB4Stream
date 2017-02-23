package com.waves_rsp.ikb4stream.datasource.dbpediamock;

import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class DBpediaMock implements IProducerConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBpediaMock.class);
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(DBpediaMock.class, "resources/datasource/dbpediamock/config.properties");

    public DBpediaMock() {
        //Do nothing
    }

    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);

    }

    @Override
    public boolean isActive() {
        try {
            return Boolean.valueOf(PROPERTIES_MANAGER.getProperty("dbpediamock.enable"));
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}
