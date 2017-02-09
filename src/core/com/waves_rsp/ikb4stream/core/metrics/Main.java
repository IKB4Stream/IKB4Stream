package com.waves_rsp.ikb4stream.core.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private Main() {
        //Do nothing
    }

    public static void main(String[] args) {
        LOGGER.info("Metrics Logger start");
        MetricsLogger metricsLogger = MetricsLogger.getMetricsLogger();
        metricsLogger.log("test_reports", "test23");
    }
}
