package com.waves_rsp.ikb4stream.core.communication;

@FunctionalInterface
public interface DatabaseReaderCallback {
    void onResult(Throwable t, String result);
}
