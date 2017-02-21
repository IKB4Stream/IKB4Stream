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
 */
public class WebCommunication implements ICommunication {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(WebCommunication.class, "resources/communication/web/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(WebCommunication.class);
    static IDatabaseReader databaseReader;
    private Vertx server;

    /**
     * Overrides default constructor
     */
    public WebCommunication() {
        // Do nothing
    }

    /**
     * Starts the server, implemented by vertx.
     * @param databaseReader
     * @throws NullPointerException if {@param databaseReader} is null
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
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid 'communications.web.port' value");
            return;
        } catch (IllegalArgumentException e) {
            LOGGER.info("Property 'communications.web.port' not set. Use default value for score.target");
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.put("http.port", port);

        deploymentOptions.setConfig(jsonObject);

        server.deployVerticle(VertxServer.class.getName());
    }

    /**
     * Set the static databaseReader of WebCommunication
     * @param dbReader IDatabaseReader to set in WebCommunication
     */
    private static void configureDatabaseReader(IDatabaseReader dbReader) {
        databaseReader = dbReader;
    }

    /**
     * Closes the server if it is started.
     */
    @Override
    public void close() {
        if (server != null) {
            server.close();
        }
        LOGGER.info("WebCommunication module stopped");
    }

    @Override
    public boolean isActive() {
        try {
            return Boolean.valueOf(PROPERTIES_MANAGER.getProperty("communications.web.enable"));
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}
