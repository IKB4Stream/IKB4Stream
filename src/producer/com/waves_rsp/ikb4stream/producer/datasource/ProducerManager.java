package com.waves_rsp.ikb4stream.producer.datasource;

import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.core.util.JarLoader;
import com.waves_rsp.ikb4stream.core.util.UtilManager;
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

public class ProducerManager {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(ProducerManager.class, "resources/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerManager.class);
    private final ClassLoader parent = ProducerManager.class.getClassLoader();
    private static ProducerManager ourInstance = new ProducerManager();
    private final List<Thread> producerConnectors = new ArrayList<>();
    private final List<Thread> dataConsumers = new ArrayList<>();
    private final DataQueue dataQueue = new DataQueue();

    private ProducerManager() {

    }

    /**
     * Get instance of ProducerManager
     * @return ProducerManager
     */
    public static ProducerManager getInstance() {
        return ourInstance;
    }

    /**
     * Instantiate all consumers and producers
     * @throws IOException If there isn't config.properties file in resource directory
     */
    public void instantiate() throws IOException {
        launchDataConsumer();
        launchDataProducer();
    }

    /**
     * Launch all consumers
     */
    private void launchDataConsumer() {
        int nbThreadConsumer = 10;
        try {
            nbThreadConsumer = Integer.parseInt(PROPERTIES_MANAGER.getProperty("producer.thread"));
        } catch (NumberFormatException e) {
            LOGGER.warn("producer.thread is not a number, use default value");
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
     * @return Path or null if there is invalid configuration
     */
    private static String getPathProducerConnector() {
        try {
            return PROPERTIES_MANAGER.getProperty("producer.path");
        } catch (IllegalArgumentException e) {
            LOGGER.warn(e.getMessage());
            LOGGER.warn("There is no ProducerConnector to load");
            return null;
        }
    }

    /**
     * Launch module
     * @param jarLoader JarLoader that represents module
     */
    private void launchModule(JarLoader jarLoader) {
        if (jarLoader != null) {
            List<String> classes = jarLoader.getClasses();
            List<URL> urls = jarLoader.getUrls();
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                ClassLoader classLoader = new URLClassLoader(
                        urls.toArray(new URL[urls.size()]),
                        parent);
                classes.stream()
                        .map(c -> UtilManager.loadClass(c, classLoader))
                        .filter(c -> UtilManager.implementInterface(c, IProducerConnector.class))
                        .forEach(clazz -> {
                            IProducerConnector producerConnector = (IProducerConnector) UtilManager.newInstance(clazz);
                            if (producerConnector.isActive()) {
                                Thread thread = new Thread(() -> producerConnector.load(new DataProducer(dataQueue)));
                                thread.setContextClassLoader(classLoader);
                                thread.setName(producerConnector.getClass().getName());
                                thread.start();
                                producerConnectors.add(thread);
                            }
                        });

                return null;
            });
        }
    }

    /**
     * Stop producer and consumer when dataQueue is empty
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

    /**
     * Force stop all producers and consumers
     */
    public void forceStop() {
        LOGGER.info("Force stop");
        producerConnectors.forEach(Thread::interrupt);
        LOGGER.info("All producer has been stopped");
        dataConsumers.forEach(Thread::interrupt);
        LOGGER.info("All consumers has been stopped");
    }
}