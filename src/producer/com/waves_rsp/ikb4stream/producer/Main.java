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

package com.waves_rsp.ikb4stream.producer;

import com.waves_rsp.ikb4stream.producer.datasource.ProducerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the entry point for producer module. its goal is to initialize the class {@link ProducerManager}
 *
 * @author ikb4stream
 * @version 1.0
 */
public class Main {
    /**
     * Single instance of {@link ProducerManager}
     *
     * @see ProducerManager#getInstance()
     * @see ProducerManager#instantiate()
     * @see ProducerManager#stop()
     * @see Main#main(String[])
     */
    private static final ProducerManager PRODUCER_MANAGER = ProducerManager.getInstance();
    /**
     * Logger used to log all information in this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /**
     * Private constructor to block instantiation
     */
    private Main() {

    }

    /**
     * This method executes the program
     *
     * @param args producer module starting arguments
     * @see Main#PRODUCER_MANAGER
     */
    public static void main(String[] args) {
        LOGGER.info("IKB4Stream Producer Module start");
        PRODUCER_MANAGER.instantiate();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("IKB4Stream Producer Module stop");
            PRODUCER_MANAGER.stop();
        }));
    }
}

