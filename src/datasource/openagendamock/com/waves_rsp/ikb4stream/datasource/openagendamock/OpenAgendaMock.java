package com.waves_rsp.ikb4stream.datasource.openagendamock;


import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IOpenAgenda;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;

import java.io.*;
import java.util.List;
import java.util.Objects;

public class OpenAgendaMock implements IOpenAgenda {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(OpenAgendaMock.class, "resources/datasource/openagendamock/config.properties");
    private final String source;
    private final long sleepTime;
    private final InputStream input;

    /**
     * Instantiate the OpenAgendaMock object with load properties to connect to the OPen Agenda API
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
     * @param dataProducer producer
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


    public boolean isActive() {
        return this.isActive(PROPERTIES_MANAGER.getProperty("openagendamock.enable"));
    }

}