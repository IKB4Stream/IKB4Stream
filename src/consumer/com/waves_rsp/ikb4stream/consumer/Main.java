package com.waves_rsp.ikb4stream.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private Main() {
        // Private constructor to block instantiation
    }

    public static void main(String[] args) {
        LOGGER.info("IKB4Stream Consumer Module start");
        CommunicationManager manager = CommunicationManager.getInstance();
        Thread listener = new Thread(() -> {
            try(Scanner sc = new Scanner(System.in)) {
                while(!Thread.interrupted()) {
                    if (sc.hasNextLine()) {
                        switch (sc.nextLine()) {
                            case "START":
                                manager.start();
                                break;
                            case "STOP":
                                manager.stop();
                                break;
                        }
                    }
                }
            }
        });

        listener.setDaemon(true);
        listener.start();
    }
}
