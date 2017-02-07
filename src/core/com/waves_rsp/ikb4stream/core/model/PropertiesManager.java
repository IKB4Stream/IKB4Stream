package com.waves_rsp.ikb4stream.core.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertiesManager {
    private static PropertiesManager ourInstance = new PropertiesManager();
    private final Properties config = new Properties();

    public static PropertiesManager getInstance() {
        return ourInstance;
    }

    private PropertiesManager() {
        Path configLocation = Paths.get("resources/config.properties");
        try (InputStream stream = Files.newInputStream(configLocation)) {
            config.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String property) {
        String value = config.getProperty(property);
        if (value == null) {
            throw new IllegalArgumentException("Property not found : " + property);
        }
        return value;
    }
}
