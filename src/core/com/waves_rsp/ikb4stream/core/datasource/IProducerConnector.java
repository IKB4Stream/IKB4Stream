package com.waves_rsp.ikb4stream.core.datasource;

@FunctionalInterface
public interface IProducerConnector {
    void load(IDataProducer dataProducer);
}
