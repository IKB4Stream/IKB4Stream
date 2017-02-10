package com.waves_rsp.ikb4stream.core.datasource.model;

/**
 * This class establishes the connexion between an external source and the DataQueue
 */
@FunctionalInterface
public interface IProducerConnector {
    /**
     * This method registers a DataProducer which allows to push in DataQueue
     * @param dataProducer
     */
    void load(IDataProducer dataProducer);
}
