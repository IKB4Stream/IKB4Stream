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
            LOGGER.error(e.getMessage());
        }

        Thread listener = new Thread(() -> {
            try(Scanner sc = new Scanner(System.in)) {
                while(sc.hasNextLine()) {
                    switch (sc.nextLine()) {
                        case "START":
                            producerManager.instantiate();
                            break;
                        case "RESTART":
                            producerManager.stop();
                            producerManager.instantiate();
                            break;
                        case "STOP":
                            producerManager.stop();
                            break;
                        case "STOP -F":
                            producerManager.forceStop();
                            break;
                        default:
                            //Do nothing
                            break;
                    }
                }
            } catch (IOException e) {
                LOGGER.info(e.getMessage());
            }
        });

        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(listener);
    }
}

