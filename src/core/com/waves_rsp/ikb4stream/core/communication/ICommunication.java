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

    /**
     * Called to know if module should be launch
     * @return True if Communication module must be started
     */
    boolean isActive();
}
