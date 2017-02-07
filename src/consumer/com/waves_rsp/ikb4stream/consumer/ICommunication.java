package com.waves_rsp.ikb4stream.consumer;

public interface ICommunication {
    /**
     * Called at startup.
     */
    void start(DatabaseReader databaseReader);

    /**
     * Called at closure.
     */
    void close();
}
