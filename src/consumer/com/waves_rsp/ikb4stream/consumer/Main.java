/*
 * Copyright (C) 2017 ikb4stream team
 * ikb4stream is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * ikb4stream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

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
