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

package com.waves_rsp.ikb4stream.datasource.openagendamock;


import com.waves_rsp.ikb4stream.core.datasource.IOpenAgenda;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;

import java.io.*;
import java.util.List;
import java.util.Objects;

/**
 * Mock of {@link com.waves_rsp.ikb4stream.datasource.openagenda.OpenAgendaProducerConnector OpenAgendaProducerConnector}
 *
 * @author ikb4stream
 * @version 1.0
 * @see com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector
 * @see com.waves_rsp.ikb4stream.core.datasource.IOpenAgenda
 */
public class OpenAgendaMock implements IOpenAgenda {
    /**
     * Properties of this module
     *
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class, String)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(OpenAgendaMock.class, "resources/datasource/openagendamock/config.properties");
    /**
     * @see OpenAgendaMock#load(IDataProducer)
     */
    private final InputStream input;
    /**
     * Interval time between two batch
     *
     * @see OpenAgendaMock#load(IDataProducer)
     */
    private final long sleepTime;
    /**
     * Source name of corresponding {@link Event}
     *
     * @see OpenAgendaMock#load(IDataProducer)
     */
    private final String source;

    /**
     * Instantiate the OpenAgendaMock object with load properties to connect to the OPen Agenda API
     *
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @see OpenAgendaMock#source
     * @see OpenAgendaMock#sleepTime
     * @see OpenAgendaMock#input
     */
    public OpenAgendaMock() {
        try {
            this.source = PROPERTIES_MANAGER.getProperty("openagendamock.source");
            this.sleepTime = Long.valueOf(PROPERTIES_MANAGER.getProperty("openagendamock.sleep_time"));
            String mockFile = PROPERTIES_MANAGER.getProperty("openagendamock.mock");
            this.input = new FileInputStream(new File(mockFile));
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid properties loaded: {}", e);
            throw new IllegalStateException("Invalid configuration");
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found : {} ", e);
            throw new IllegalArgumentException("Invalid property");
        }
    }

    /**
     * Listen events from openAgenda and load them with the data producer object
     *
     * @param dataProducer Instance of {@link IDataProducer}
     * @see OpenAgendaMock#input
     * @see OpenAgendaMock#source
     * @see OpenAgendaMock#sleepTime
     */
    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        Objects.requireNonNull(dataProducer);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                long start = System.currentTimeMillis();
                List<Event> events = searchEvents(this.input, this.source);
                long time = System.currentTimeMillis() - start;
                events.forEach(dataProducer::push);
                METRICS_LOGGER.log("time_process_" + this.source, time);
                Thread.sleep(this.sleepTime);
            } catch (InterruptedException e) {
                LOGGER.error("Current thread has been interrupted: {}", e);
            } finally {
                try {
                    this.input.close();
                } catch (IOException e) {
                    LOGGER.error("Exception during thread interrupted");
                }
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Check if this jar is active
     *
     * @return true if it should be started
     * @see OpenAgendaMock#PROPERTIES_MANAGER
     */
    public boolean isActive() {
        return this.isActive(PROPERTIES_MANAGER.getProperty("openagendamock.enable"));
    }
}