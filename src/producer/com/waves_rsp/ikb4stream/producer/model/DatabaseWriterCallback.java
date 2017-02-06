package com.waves_rsp.ikb4stream.producer.model;

@FunctionalInterface
public interface DatabaseWriterCallback {
    void onResult(Throwable t);
}
