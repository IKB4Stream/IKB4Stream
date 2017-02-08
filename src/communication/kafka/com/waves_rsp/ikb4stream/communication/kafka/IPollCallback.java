package com.waves_rsp.ikb4stream.communication.kafka;

import com.waves_rsp.ikb4stream.core.communication.model.Request;

@FunctionalInterface
public interface IPollCallback {
    String onNewRequest(Request request);
}
