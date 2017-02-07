package com.waves_rsp.ikb4stream.core.communication;

import com.waves_rsp.ikb4stream.core.communication.model.Request;

@FunctionalInterface
public interface IDatabaseReader {
    void getEvent(Request request, DatabaseReaderCallback callback);
}
