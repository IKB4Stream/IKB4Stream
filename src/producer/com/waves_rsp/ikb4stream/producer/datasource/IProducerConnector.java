package com.waves_rsp.ikb4stream.producer.datasource;

@FunctionalInterface
public interface IProducerConnector {
    void load(DataProducer dataProducer);
}
