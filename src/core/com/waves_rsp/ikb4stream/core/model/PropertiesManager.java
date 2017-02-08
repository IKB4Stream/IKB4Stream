package com.waves_rsp.ikb4stream.core.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertiesManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesManager.class);
    private static PropertiesManager ourInstance = new PropertiesManager();
    private final Properties config = new Properties();

    private PropertiesManager() {
        Path configLocation = Paths.get("resources/config.properties");
        try (InputStream stream = Files.newInputStream(configLocation)) {
            config.load(stream);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static PropertiesManager getInstance() {
        return ourInstance;
    }

    public String getProperty(String property) {
        String value = config.getProperty(property);
        if (value == null) {
            throw new IllegalArgumentException("Property not found : " + property);
        }
        return value;
    }
}
