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
        LOGGER.info("Metrics Logger start");
        Thread listener = new Thread(() -> {
            while(!Thread.interrupted()) {
                try(Scanner sc = new Scanner(System.in)) {
                    if (sc.hasNextLine()) {
                        process(sc.nextLine());
                    }
                }
            }
        });
        listener.start();
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

    /**
     * Process command to Main Metrics
     * @param command Command to process
     */
    private static void process(String command) {
        String[] tokens = command.split(" ");
        switch (tokens[0]) {
            case "PUSH":
                if (tokens.length >= 3) {
                    LOGGER.info("PUSH metrics into influx database.");
                    METRICS_LOGGER.log(tokens[1], tokens[2]);
                }else {
                    LOGGER.warn("wrong arguments for command PUSH : PUSH <arg1> <arg2>");
                }
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
}
