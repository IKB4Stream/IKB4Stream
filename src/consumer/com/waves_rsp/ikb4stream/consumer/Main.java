package com.waves_rsp.ikb4stream.consumer;

public class Main {
    public static void main(String[] args) {
        // Example of how to implement RDFParser & AnomalyRequest classes
        RDFParser rdfParser = new RDFParser();
        AnomalyRequest anomalyRequest = rdfParser.parse("resources/anomaly.ttl");
    }
}
