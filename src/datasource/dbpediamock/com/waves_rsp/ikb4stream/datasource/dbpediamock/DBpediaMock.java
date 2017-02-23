package com.waves_rsp.ikb4stream.datasource.dbpediamock;

import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;

public class DBpediaMock implements IProducerConnector {


    @Override
    public void load(IDataProducer dataProducer) {

    }

    @Override
    public boolean isActive() {
        return false;
    }
}
