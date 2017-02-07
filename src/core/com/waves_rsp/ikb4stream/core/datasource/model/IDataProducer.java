package com.waves_rsp.ikb4stream.core.datasource.model;

import com.waves_rsp.ikb4stream.core.model.Event;

@FunctionalInterface
public interface IDataProducer {
    void push(Event event);
}
