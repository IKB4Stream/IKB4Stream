package com.waves_rsp.ikb4stream.producer.datasource;

/**
 * This class establishes the connexion between an external source and the DataQueue
 * @see DataProducer
 */
@FunctionalInterface
public interface IProducerConnector {
    /**
     * This method registers a DataProducer which allows to push in DataQueue
     * @param dataProducer
     */
    void load(DataProducer dataProducer);
}
