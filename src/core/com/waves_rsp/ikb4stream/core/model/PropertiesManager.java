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

package com.waves_rsp.ikb4stream.core.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Singleton PropertiesManager
 * Load property config
 */
public class PropertiesManager {
    private static final Map<Class, PropertiesManager> PROPERTIES_MANAGER_HASH_MAP = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesManager.class);
    private static final String DEFAULT_PATH = "resources/config.properties";
    private final Properties config = new Properties();

    /**
     * Singleton PropertiesManager
     * @param path Path to config file
     * @throws NullPointerException if {@param path} is null
     */
    private PropertiesManager(String path) {
        Objects.requireNonNull(path);
        Path configLocation = Paths.get(path);
        try (InputStream stream = Files.newInputStream(configLocation)) {
            config.load(stream);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Get instance for PropertiesManager, one per class
     * @param clazz Class in which PropertiesManager will be instantiate
     * @return An instance of PropertiesManager for {@param clazz}
     * @throws NullPointerException if {@param clazz} is null
     */
    public static PropertiesManager getInstance(Class clazz) {
        Objects.requireNonNull(clazz);
        return getInstance(clazz, DEFAULT_PATH);
    }

    /**
     * Get instance for PropertiesManager, one per class
     * @param clazz Class in which PropertiesManager will be instantiate
     * @param path Path to load configuration for this {@param clazz}
     * @return An instance of PropertiesManager for {@param clazz}
     * @throws NullPointerException if {@param clazz} or {@param path} is null
     */
    public static PropertiesManager getInstance(Class clazz, String path) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(path);
        PropertiesManager propertiesManager = PROPERTIES_MANAGER_HASH_MAP.get(clazz);
        if (propertiesManager == null) {
            PropertiesManager pm = new PropertiesManager(path);
            PROPERTIES_MANAGER_HASH_MAP.put(clazz, pm);
            return pm;
        }
        return propertiesManager;
    }

    /**
     * Get property
     * @param property Property to get from configuration file
     * @return Value of property
     * @throws NullPointerException if {@param property} is null
     * @throws IllegalArgumentException if {@param property} is not set in property file
     */
    public String getProperty(String property) {
        Objects.requireNonNull(property);
        String value = config.getProperty(property);
        if (value == null) {
            throw new IllegalArgumentException("Property not found : " + property);
        }
        return value;
    }
}
