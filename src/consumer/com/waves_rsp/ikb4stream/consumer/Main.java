package com.waves_rsp.ikb4stream.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class executes the program and launchs the CommunicationManager
 * @see CommunicationManager will be launch
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /**
     * TODO
     */
    private Main() {
        // Private constructor to block instantiation
    }

    /**
     *This method allows the execution of the program
     * @param args a array of string
     */
    public static void main(String[] args) {
        LOGGER.info("IKB4Stream Consumer Module start");
        CommunicationManager.getInstance().start();
    }
}
