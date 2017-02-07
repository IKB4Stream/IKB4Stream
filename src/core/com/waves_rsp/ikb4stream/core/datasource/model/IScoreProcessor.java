package com.waves_rsp.ikb4stream.core.datasource.model;

import com.waves_rsp.ikb4stream.core.model.Event;

@FunctionalInterface
public interface IScoreProcessor {
    /**
     * This associates a score to the event in param
     * @param event
     * @return Event
     */
    Event processScore(Event event);
}
