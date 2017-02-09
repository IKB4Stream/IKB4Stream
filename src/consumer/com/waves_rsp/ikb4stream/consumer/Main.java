package com.waves_rsp.ikb4stream.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Scanner;

/**
 * Main class executes the program and launchs the CommunicationManager
 * @see CommunicationManager will be launch
 */
public class Main {
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
        CommunicationManager.getInstance().start();
        CommunicationManager manager = CommunicationManager.getInstance();
        manager.start();

        Thread listener = new Thread(() -> {
            try(Scanner sc = new Scanner(System.in)) {
                while(!Thread.interrupted()) {
                    if (sc.hasNextLine()) {
                        switch (sc.nextLine()) {
                            case "STOP":
                                manager.stop();
                                break;
                            case "RESTART":
                                manager.stop();
                                Thread.sleep(500);
                                manager.start();
                                break;
                            default:
                                //Do nothing
                                break;
                        }
                    }
                }
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
        });

        listener.start();
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(listener);
    }
}
