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

package com.waves_rsp.ikb4stream.producer.datasource;

import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.core.util.ClassManager;
import com.waves_rsp.ikb4stream.core.util.JarLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Object which manage all {@link IProducerConnector} and {@link DataConsumer}
 *
 * @author ikb4stream
 * @version 1.0
 */
public class ProducerManager {
    /**
     * Properties of this class
     *
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(ProducerManager.class);
    /**
     * Logger used to log all information in this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerManager.class);
    /**
     * Single instance of {@link ProducerManager}
     *
     * @see ProducerManager#getInstance()
     */
    private static final ProducerManager PRODUCER_MANAGER = new ProducerManager();
    /**
     * ClassLoader of {@link ProducerManager}
     *
     * @see ProducerManager#launchModule(JarLoader)
     */
    private final ClassLoader parent = ProducerManager.class.getClassLoader();
    /**
     * List of Thread for each {@link IProducerConnector}
     *
     * @see ProducerManager#launchModule(JarLoader)
     * @see ProducerManager#stop()
     */
    private final List<Thread> producerConnectors = new ArrayList<>();
    /**
     * Single instance of {@link DataQueue}
     *
     * @see ProducerManager#launchModule(JarLoader)
     * @see ProducerManager#stop()
     */
    private final DataQueue dataQueue = DataQueue.createDataQueue();
    /**
     * List of Thread for each {@link DataConsumer}
     *
     * @see ProducerManager#launchDataConsumer()
     * @see ProducerManager#stop()
     */
    private final List<Thread> dataConsumers = new ArrayList<>();

    /**
     * Private constructor to block instantiation
     */
    private ProducerManager() {

    }

    /**
     * Get single instance of {@link ProducerManager}
     *
     * @return Single instance of {@link ProducerManager}
     * @see ProducerManager#PRODUCER_MANAGER
     */
    public static ProducerManager getInstance() {
        return PRODUCER_MANAGER;
    }

    /**
     * Instantiate all consumers and producers
     *
     * @see ProducerManager#launchDataProducer()
     * @see ProducerManager#launchDataConsumer()
     */
    public void instantiate() {
        launchDataConsumer();
        launchDataProducer();
    }

    /**
     * Launch all consumers
     *
     * @see ProducerManager#PRODUCER_MANAGER
     * @see ProducerManager#dataConsumers
     */
    private void launchDataConsumer() {
        int nbThreadConsumer = 10;
        try {
            nbThreadConsumer = Integer.parseInt(PROPERTIES_MANAGER.getProperty("producer.thread"));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Use default value for producer.thread");
        }

        LOGGER.info(nbThreadConsumer + " consumer will be launch");

        for (int i = 0; i < nbThreadConsumer; i++) {
            Thread thread = new Thread(() -> {
                DataConsumer dataConsumer = DataConsumer.createDataConsumer(dataQueue);
                dataConsumer.consume();
            });
            thread.setName("Consumer " + (i + 1));
            thread.start();
            dataConsumers.add(thread);
        }
        LOGGER.info(nbThreadConsumer + " consumer(s) has been launched");
    }

    /**
     * Launch all producers
     */
    private void launchDataProducer() {
        String stringPath = getPathProducerConnector();
        if (stringPath == null) return;
        try (Stream<Path> paths = Files.walk(Paths.get(stringPath))) {
            paths.forEach((Path filePath) -> {
                if (Files.isRegularFile(filePath)) {
                    JarLoader jarLoader = JarLoader.createJarLoader(filePath.toString());
                    launchModule(jarLoader);
                }
            });
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Get path where ProducerConnector are store
     *
     * @return Path or null if there is invalid configuration
     * @see ProducerManager#PROPERTIES_MANAGER
     */
    private static String getPathProducerConnector() {
        try {
            return PROPERTIES_MANAGER.getProperty("producer.path");
        } catch (IllegalArgumentException e) {
            LOGGER.warn("There is no ProducerConnector to load {}", e.getMessage());
            return null;
        }
    }

    /**
     * Launch module
     *
     * @param jarLoader {@link JarLoader} that represents module
     * @see ProducerManager#producerConnectors
     * @see ProducerManager#dataQueue
     * @see ProducerManager#parent
     */
    private void launchModule(JarLoader jarLoader) {
        if (jarLoader != null) {
            List<URL> urls = jarLoader.getUrls();
            ClassLoader classLoader = AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> new URLClassLoader(urls.toArray(new URL[urls.size()]), parent));
            jarLoader.getClasses().stream()
                    .map(c -> ClassManager.loadClass(c, classLoader))
                    .filter(c -> ClassManager.implementInterface(c, IProducerConnector.class))
                    .forEach(clazz -> {
                        try {
                            IProducerConnector producerConnector = (IProducerConnector) ClassManager.newInstance(clazz);
                            if (producerConnector.isActive()) {
                                Thread thread = new Thread(() -> producerConnector.load(new DataProducer(dataQueue)));
                                thread.setContextClassLoader(classLoader);
                                thread.setName(producerConnector.getClass().getName());
                                thread.start();
                                producerConnectors.add(thread);
                            }
                        } catch (Exception e) {
                            LOGGER.error("Error during instantiate {} : {}", clazz.getName(), e.getMessage());
                        }
                    });
        }
    }

    /**
     * Stop producer and consumer when dataQueue is empty
     *
     * @see ProducerManager#producerConnectors
     * @see ProducerManager#dataConsumers
     * @see ProducerManager#dataQueue
     */
    public void stop() {
        producerConnectors.forEach(Thread::interrupt);
        LOGGER.info("All producer has been stopped");
        // Wait the DataQueue is Empty
        LOGGER.info("Wait producers finished to clear the DataQueue");
        while (!dataQueue.isEmpty()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        dataConsumers.forEach(Thread::interrupt);
        LOGGER.info("All consumers has been stopped");
    }
}