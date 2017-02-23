package com.waves_rsp.ikb4stream.consumer;

import com.waves_rsp.ikb4stream.consumer.manager.CommunicationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Scanner;


/**
 * Main class executes the program and launchs the CommunicationManager
 * @see CommunicationManager will be launch
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final CommunicationManager COMMUNICATION_MANAGER = CommunicationManager.getInstance();

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
        /*Thread listener = new Thread(() -> {
            try(Scanner sc = new Scanner(System.in)) {
                while(!Thread.interrupted()) {
                    if (sc.hasNextLine()) {
                        process(sc.nextLine());
                    }
                }
            }
        });
        listener.setName("ConsumerMain");
        Runtime runtime = Runtime.getRuntime();
        try {
            listener.start();
            if(runtime.removeShutdownHook(listener)) {
                runtime.addShutdownHook(listener);
            }
        }catch (IllegalArgumentException err) {
            LOGGER.error("Hook has already running: {}", err.toString());
            return;
        }finally {
            runtime.removeShutdownHook(listener);
        }*/
    }

    private static void process(String command) {
        switch (command) {
            case "START":
                LOGGER.info("threads for consumer started.");
                COMMUNICATION_MANAGER.start();
                break;
            case "STOP":
                LOGGER.info("threads have been properly stopped.");
                COMMUNICATION_MANAGER.stop();
                break;
            case "RESTART":
                LOGGER.info("restarted threads for consumer.");
                COMMUNICATION_MANAGER.stop();
                COMMUNICATION_MANAGER.start();
                break;
            default:
                LOGGER.warn("Wrong command send, only these commands are allowed : START, RESTART, STOP, STOP -F");
                break;
        }
    }
}
