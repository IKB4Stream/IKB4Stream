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

    private Main() {

    }

    /**
     * This method executes the program
     * @param args
     */
    public static void main(String[] args) {
        LOGGER.info("IKB4Stream Producer Module start");
        ProducerManager producerManager = ProducerManager.getInstance();
        try {
            producerManager.instantiate();
        } catch (IOException e) {
            LOGGER.error("Unable to read config file in order to create threads producer.");
            return;
        }

        Thread listener = new Thread(() -> {
            try(Scanner sc = new Scanner(System.in)) {
                while(!Thread.interrupted()) {
                    if(sc.hasNextLine()) {
                        switch (sc.nextLine()) {
                            case "START":
                                LOGGER.info("threads for producer started");
                                producerManager.instantiate();
                                break;
                            case "RESTART":
                                LOGGER.info("threads for producer are restarting.");
                                producerManager.stop();
                                producerManager.instantiate();
                                break;
                            case "STOP":
                                LOGGER.info("threads have been properly stopped.");
                                producerManager.stop();
                                break;
                            case "STOP -F":
                                LOGGER.info("force stop all threads.");
                                producerManager.forceStop();
                                break;
                            default:
                                LOGGER.warn("Wrong command send, only these commands are allowed : START, RESTART, STOP, STOP -F");
                                break;
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.info(e.getMessage());
            }finally {
                producerManager.stop();
            }
        });

        Runtime runtime = Runtime.getRuntime();
        try {
            listener.start();
            runtime.addShutdownHook(listener);
        }catch (IllegalArgumentException err) {
            LOGGER.error("Runtime thread hook got an error : thread already running. "+err.getMessage());
        }finally {
            runtime.removeShutdownHook(listener);
        }
    }

}

