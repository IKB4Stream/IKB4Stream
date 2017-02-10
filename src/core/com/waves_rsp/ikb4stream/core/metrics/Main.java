package com.waves_rsp.ikb4stream.core.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private Main() {
        //Do nothing
    }

    public static void main(String[] args) {
        LOGGER.info("Metrics Logger start");
        MetricsLogger metricsLogger = MetricsLogger.getMetricsLogger();

        Thread listener = new Thread(() -> {
            while(!Thread.interrupted()) {
                try(Scanner sc = new Scanner(System.in)) {
                    if (sc.hasNextLine()) {
                        String[] tokens = sc.nextLine().split(" ");
                        switch (tokens[0]) {
                            case "PUSH":
                                if (tokens.length >= 3) {
                                    LOGGER.info("PUSH metrics into influx database.");
                                    metricsLogger.log(tokens[1], tokens[2]);
                                }else {
                                    LOGGER.warn("wrong arguments for command PUSH : PUSH <arg1> <arg2>");
                                }
                                break;
                            case "STOP":
                                metricsLogger.close();
                                break;
                            default:
                                //Do nothing
                                break;
                        }
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
}
