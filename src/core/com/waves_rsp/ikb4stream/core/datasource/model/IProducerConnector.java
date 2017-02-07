package com.waves_rsp.ikb4stream.core.datasource.model;

import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;

@FunctionalInterface
public interface IProducerConnector {
    void load(IDataProducer dataProducer);
}
