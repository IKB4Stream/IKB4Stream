package com.waves_rsp.ikb4stream.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("IKB4Stream Consumer Module start");

        // Example of how to implement RDFParser & AnomalyRequest classes
        RDFParser rdfParser = new RDFParser();
        AnomalyRequest anomalyRequest = rdfParser.parse("resources/anomaly.ttl");
        LOGGER.info("IKB4Stream Consumer Module start");
    }
}
