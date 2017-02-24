package com.waves_rsp.ikb4stream.consumer;

import com.waves_rsp.ikb4stream.consumer.manager.CommunicationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Main class executes the program and launchs the CommunicationManager
 * @see CommunicationManager will be launch
 */
public class Main {
    private static final CommunicationManager COMMUNICATION_MANAGER = CommunicationManager.getInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /**
     * Private constructor to block instantiation
     */
    private Main() {

    }

    /**
     *This method allows the execution of the program
     * @param args a array of string
     */
    public static void main(String[] args) {
        LOGGER.info("IKB4Stream Consumer Module start");
        COMMUNICATION_MANAGER.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("IKB4Stream Consumer Module stop");
            COMMUNICATION_MANAGER.stop();
        }));
    }
}
