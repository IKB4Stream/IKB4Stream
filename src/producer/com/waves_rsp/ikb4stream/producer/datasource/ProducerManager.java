package com.waves_rsp.ikb4stream.producer.datasource;

import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.core.util.UtilManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ProducerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerManager.class);
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
            nbThreadConsumer = Integer.parseInt(PropertiesManager.getInstance().getProperty("producer.thread"));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Use default value for producer.thread");
        }

        LOGGER.info(nbThreadConsumer + " consumer will be launch");

        for (int i = 0; i < nbThreadConsumer; i++) {
            Thread thread = new Thread(() -> {
                DataConsumer dataConsumer = DataConsumer.createDataConsumer(dataQueue);
                dataConsumer.consume();
            });
            thread.start();
            LOGGER.info("Consumer #" + i + " has been launched");
            dataConsumers.add(thread);
        }
        LOGGER.info("All consumer has been launched");
    }

    /**
     * Launch all producers
     * @throws IOException If there isn't producer.path in config.properties file in resource directory
     */
    private void launchDataProducer() {
        String stringPath = PropertiesManager.getInstance().getProperty("producer.path");
        try (Stream<Path> paths = Files.walk(Paths.get(stringPath))) {
            paths.forEach((Path filePath) -> {
                if (Files.isRegularFile(filePath)) {
                    URLClassLoader cl = UtilManager.getURLClassLoader(this.getClass().getClassLoader(), filePath);
                    UtilManager.getEntries(filePath).filter(UtilManager::checkIsClassFile)
                            .map(UtilManager::getClassName)
                            .map(clazz -> UtilManager.loadClass(clazz, cl))
                            .filter(clazz -> UtilManager.implementInterface(clazz, IProducerConnector.class))
                            .forEach(clazz -> {
                                IProducerConnector producerConnector = (IProducerConnector) UtilManager.newInstance(clazz);
                                Thread thread = new Thread(() -> producerConnector.load(new DataProducer(dataQueue)));
                                thread.start();
                                LOGGER.info("Producer " + producerConnector.getClass().getName() + " has been launched");
                                producerConnectors.add(thread);
                            });
                    closeURLClassLoader(cl);
                }
            });
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("All producer has been launched");
    }

    private void closeURLClassLoader(URLClassLoader cl) {
        try {
            cl.close();
        } catch (IOException e1) {
            LOGGER.error(e1.getMessage());
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
