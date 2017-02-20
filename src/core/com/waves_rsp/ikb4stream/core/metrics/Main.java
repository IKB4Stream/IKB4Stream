package com.waves_rsp.ikb4stream.core.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();

    private Main() {
        //Do nothing
    }

    public static void main(String[] args) {
        if(!METRICS_LOGGER.isInfluxServiceEnabled()) {
            return;
        }

        LOGGER.info("Metrics Logger start");
        Thread listener = new Thread(() -> {
            try(Scanner sc = new Scanner(System.in)) {
                while(!Thread.interrupted()) {
                    if (sc.hasNextLine()) {
                        process(sc.nextLine());
                    }
                }
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
            if(runtime.removeShutdownHook(listener)) {
                LOGGER.info("shutdown hook.");
            }
        }
    }

    /**
     * Process command to Main Metrics
     * @param command Command to process
     */
    private static void process(String command) {
        String[] tokens = command.split(" ");
        switch (tokens[0]) {
            case "PUSH":
                processPush(tokens);
                break;
            case "STOP":
                LOGGER.info("influx db connexion has been stopped");
                METRICS_LOGGER.close();
                Thread.currentThread().interrupt();
                break;
            default:
                //Do nothing
                break;
        }
    }

    private static void processPush(String[] tokens) {
        if (tokens.length >= 3) {
            LOGGER.info("PUSH metrics into influx database.");
            METRICS_LOGGER.log(tokens[1], tokens[2]);
        }else {
            LOGGER.warn("wrong arguments for command PUSH : PUSH <arg1> <arg2>");
        }
    }
}
