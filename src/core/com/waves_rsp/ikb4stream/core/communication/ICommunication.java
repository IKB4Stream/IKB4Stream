package com.waves_rsp.ikb4stream.core.communication;

public interface ICommunication {
    /**
     * Called at startup.
     */
    void start(IDatabaseReader databaseReader);

    /**
     * Called at closure.
     */
    void close();
}
