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

package com.waves_rsp.ikb4stream.communication.web;

import com.waves_rsp.ikb4stream.core.communication.ICommunication;
import com.waves_rsp.ikb4stream.core.communication.IDatabaseReader;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Web communication connector that handles REST requests
 *
 * @author ikb4stream
 * @version 1.0
 * @see com.waves_rsp.ikb4stream.core.communication.ICommunication
 */
public class WebCommunication implements ICommunication {
    /**
     * Properties of this module
     *
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class, String)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(WebCommunication.class,
            "resources/communication/web/config.properties");
    /**
     * Logger used to log all information in this module
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebCommunication.class);
    /**
     * Connection to database to get Event
     *
     * @see WebCommunication#start(IDatabaseReader)
     * @see WebCommunication#configureDatabaseReader(IDatabaseReader)
     */
    static IDatabaseReader databaseReader;
    /**
     * VertX use to do Web API in Java
     *
     * @see WebCommunication#start(IDatabaseReader)
     * @see WebCommunication#close()
     */
    private Vertx server;

    /**
     * Overrides default constructor
     *
     * @see WebCommunication
     */
    public WebCommunication() {
        // Do nothing
    }

    /**
     * Starts the server, implemented by vertx.
     *
     * @param databaseReader {@link IDatabaseReader} is the connection to database to get Event
     * @throws NullPointerException if databaseReader is null
     * @see WebCommunication#PROPERTIES_MANAGER
     * @see WebCommunication#server
     */
    @Override
    public void start(IDatabaseReader databaseReader) {
        Objects.requireNonNull(databaseReader);
        configureDatabaseReader(databaseReader);
        LOGGER.info("Starting WebCommunication module");
        server = Vertx.vertx();
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        int port = 8081;
        try {
            PROPERTIES_MANAGER.getProperty("communications.web.port");
            port = Integer.parseInt(PROPERTIES_MANAGER.getProperty("communications.web.port"));
            LOGGER.info("WebCommunication Server set on port {}", port);
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid 'communications.web.port' value");
            return;
        } catch (IllegalArgumentException e) {
            LOGGER.info("Property 'communications.web.port' not set. Use default value for score.target");
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("http.port", port);
        deploymentOptions.setConfig(jsonObject);
        server.deployVerticle(VertxServer.class.getName(), deploymentOptions);
    }

    /**
     * Set the static databaseReader of WebCommunication
     *
     * @param dbReader IDatabaseReader to set in WebCommunication
     * @throws NullPointerException if dbReader is null
     * @see WebCommunication#databaseReader
     */
    private static void configureDatabaseReader(IDatabaseReader dbReader) {
        Objects.requireNonNull(dbReader);
        databaseReader = dbReader;
    }

    /**
     * Closes the server if it is started.
     *
     * @see WebCommunication#server
     */
    @Override
    public void close() {
        if (server != null) {
            server.close();
        }
        LOGGER.info("WebCommunication module stopped");
    }

    /**
     * Check if this jar is active
     *
     * @return True if it should be started
     * @see WebCommunication#PROPERTIES_MANAGER
     */
    @Override
    public boolean isActive() {
        try {
            return Boolean.valueOf(PROPERTIES_MANAGER.getProperty("communications.web.enable"));
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}
