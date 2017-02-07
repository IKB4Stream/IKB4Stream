package com.waves_rsp.ikb4stream.producer.score;

import com.waves_rsp.ikb4stream.core.model.Event;

@FunctionalInterface
public interface IScoreProcessor {
    Event processScore(Event event);
}
