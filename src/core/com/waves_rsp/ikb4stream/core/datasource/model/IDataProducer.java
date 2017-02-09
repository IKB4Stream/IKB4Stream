package com.waves_rsp.ikb4stream.core.datasource.model;

import com.waves_rsp.ikb4stream.core.model.Event;

/**
 * A functional interface
 */
@FunctionalInterface
public interface IDataProducer {
    void push(Event event);
}
