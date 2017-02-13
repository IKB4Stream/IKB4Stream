package com.waves_rsp.ikb4stream.communication.web;


import com.waves_rsp.ikb4stream.core.communication.ICommunication;
import com.waves_rsp.ikb4stream.core.communication.IDatabaseReader;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebCommunication implements ICommunication {
    private final PropertiesManager propertiesManager = PropertiesManager.getInstance(WebCommunication.class, "resources/config.properties");
    private final Logger LOGGER = LoggerFactory.getLogger(WebCommunication.class);
    private Vertx server;

    @Override
    public void start(IDatabaseReader databaseReader) {
        server = Vertx.vertx();
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        int port = 8081;
        try {
            propertiesManager.getProperty("communications.web.port");
            port = Integer.parseInt(propertiesManager.getProperty("communications.web.port"));
        } catch (java.lang.NumberFormatException e) {
            LOGGER.error("Invalid 'communications.web.port' value");
            return;
        } catch (IllegalArgumentException e) {
            LOGGER.info("Property 'communications.web.port' not set. Use default value for score.target");
        }

        deploymentOptions.setConfig(new JsonObject().put("http.port", port).put("database", databaseReader));
        server.deployVerticle(VertxServer.class.getName());
        LOGGER.info("WebCommunication module started");
    }

    @Override
    public void close() {
        server.close();
    }
}
