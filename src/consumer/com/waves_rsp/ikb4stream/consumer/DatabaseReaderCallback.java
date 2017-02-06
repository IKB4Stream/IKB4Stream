package com.waves_rsp.ikb4stream.consumer;

@FunctionalInterface
public interface DatabaseReaderCallback {
    void onResult(Throwable t, String result);
}
