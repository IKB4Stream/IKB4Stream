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

package com.waves_rsp.ikb4stream.core.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public interface IProducerConnectorMock extends IProducerConnector {
    Logger LOGGER = LoggerFactory.getLogger(IProducerConnectorMock.class);
    MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();

    /**
     * Load data registered into a json file and parse them to create event
     *
     * @param dataProducer contains the queue
     */
    default void load(IDataProducer dataProducer, PropertiesManager propertiesManager, String pathString) {
        Objects.requireNonNull(dataProducer);
        ObjectMapper mapper = new ObjectMapper();
        Path path = Paths.get(propertiesManager.getProperty(pathString));

        long start = System.currentTimeMillis();
        try (InputStream inputStream = new FileInputStream(path.toString());
                JsonParser parser = mapper.getFactory().createParser(inputStream)) {
            while (!Thread.currentThread().isInterrupted()) {
                if (readMockFile(dataProducer, mapper, parser, start)) {
                    return;
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return;
        }
    }

    /**
     * Indicates whether this producer is enabled or not, according to enable
     * @return true is enable is true
     */
    default boolean isActive(PropertiesManager propertiesManager, String enableString) {
        try {
            return Boolean.valueOf(propertiesManager.getProperty(enableString));
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    /**
     * Read events mock file, and push it.
     * @param dataProducer contains the queue
     * @param mapper indicates how to parse the file
     * @param parser contains mocks
     * @return True if interrupted, False if it ended with normal behaviour
     */
    default boolean readMockFile(IDataProducer dataProducer, ObjectMapper mapper, JsonParser parser, long start) {
        try {
            while (parser.nextToken() == JsonToken.START_OBJECT) {
                ObjectNode objectNode = mapper.readTree(parser);
                Event event = getEventFromJson(objectNode);
                pushIfValidEvent(dataProducer, event, start);
            }
        } catch (IOException e) {
            LOGGER.error("Something went wrong with the facebook post reading");
            return true;
        } finally {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    /**
     * Parse an object node in order to create an Event object
     * @param objectNode object node to convert to Event
     * @return Event converted format
     */
    Event getEventFromJson(ObjectNode objectNode);

    /**
     * Push a valid event into the data producer object
     * @param dataProducer contains the queue
     * @param event event to push
     */
    static void pushIfValidEvent(IDataProducer dataProducer, Event event, long start) {
        if (event != null) {
            dataProducer.push(event);
            long end = System.currentTimeMillis();
            long result = end - start;
            METRICS_LOGGER.log("time_process_"+event.getSource(), result);
            LOGGER.info("Event "+event.toString()+" was correctly pushed");
        } else {
            LOGGER.error("An event was discard (missing field)");
        }
    }

}
