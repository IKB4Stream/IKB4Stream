package com.waves_rsp.ikb4stream.producer;

import com.waves_rsp.ikb4stream.producer.datasource.ProducerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Scanner;

/**
 * This is the entry point for producer module. its goal is to initialize the class ProducerManager:
 * @see com.waves_rsp.ikb4stream.producer.datasource.ProducerManager
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final ProducerManager PRODUCER_MANAGER = ProducerManager.getInstance();

    private Main() {

    }

    /**
     * This method executes the program
     * @param args
     */
    public static void main(String[] args) {
        LOGGER.info("IKB4Stream Producer Module start");
        try {
            PRODUCER_MANAGER.instantiate();
        } catch (IOException e) {
            LOGGER.error("Unable to read config file in order to create threads producer.");
            return;
        }
        Thread listener = new Thread(() -> {
            try(Scanner sc = new Scanner(System.in)) {
                while(!Thread.interrupted()) {
                    if(sc.hasNextLine()) {
                        process(sc.nextLine());
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Unable to read config file in order to create threads producer.");
                Thread.currentThread().interrupt();
            }
        });
        Runtime runtime = Runtime.getRuntime();
        try {
            listener.start();
            if(runtime.removeShutdownHook(listener)) {
                runtime.addShutdownHook(listener);
            }
        }catch (IllegalArgumentException err) {
            LOGGER.error("Runtime thread hook got an error : thread already running. "+err.getMessage());
        }finally {
            runtime.removeShutdownHook(listener);
        }
    }

    private static void process(String command) throws IOException {
        switch (command) {
            case "START":
                LOGGER.info("threads for consumer started.");
                PRODUCER_MANAGER.instantiate();
                break;
            case "STOP":
                LOGGER.info("threads have been properly stopped.");
                PRODUCER_MANAGER.stop();
                break;
            case "FORCESTOP":
                LOGGER.info("threads have been forced stop.");
                PRODUCER_MANAGER.forceStop();
                break;
            case "RESTART":
                LOGGER.info("restarted threads for consumer.");
                PRODUCER_MANAGER.stop();
                PRODUCER_MANAGER.instantiate();
                break;
            default:
                LOGGER.warn("Wrong command send, only these commands are allowed : START, RESTART, STOP, STOP -F");
                break;
        }
    }
}

