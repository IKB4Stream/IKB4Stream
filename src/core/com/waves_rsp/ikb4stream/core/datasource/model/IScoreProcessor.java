package com.waves_rsp.ikb4stream.core.datasource.model;

import com.waves_rsp.ikb4stream.core.model.Event;

@FunctionalInterface
public interface IScoreProcessor {
    /**
     * This method create a score to the event in param
     * @param event an event without score
     * @return an event with score gave by processScore
     */
    Event processScore(Event event);
}
