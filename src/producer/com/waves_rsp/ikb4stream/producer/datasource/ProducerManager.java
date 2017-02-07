package com.waves_rsp.ikb4stream.producer.datasource;

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
import java.util.jar.JarEntry;
import java.util.stream.Stream;

public class ProducerManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static ProducerManager ourInstance = new ProducerManager();
    private final List<Thread> producerConnectors = new ArrayList<>();
    private final List<Thread> dataConsumers = new ArrayList<>();
    private final DataQueue dataQueue = new DataQueue();

    /**
     * Get instance of ProducerManager
     * @return ProducerManager
     */
    public static ProducerManager getInstance() {
        return ourInstance;
    }

    private ProducerManager() {

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
        String stringNbThreadConsumer = PropertiesManager.getInstance().getProperty("scoreprocessormanager.nbDataConsumerThread");
        if (stringNbThreadConsumer != null) {
            nbThreadConsumer = Integer.parseInt(stringNbThreadConsumer);
        }

        logger.info("ProducerManager info {}", nbThreadConsumer + " consumer will be launch");

        for (int i = 0; i < nbThreadConsumer; i++) {
            Thread thread = new Thread(() -> {
                DataConsumer dataConsumer = DataConsumer.createDataConsumer(dataQueue);
                dataConsumer.consume();
            });
            thread.start();
            logger.info("ProducerManager info {}", "Consumer #" + i + " has been launched");
            dataConsumers.add(thread);
        }
        logger.info("ProducerManager info {}", "All consumer has been launched");
    }

    /**
     * Launch all producers
     * @throws IOException If there isn't scoreprocessormanager.path in config.properties file in resource directory
     */
    private void launchDataProducer() throws IOException {
        String stringPath = PropertiesManager.getInstance().getProperty("scoreprocessormanager.path");
        try (Stream<Path> paths = Files.walk(Paths.get(stringPath))) {
            paths.forEach((Path filePath) -> {
                if (Files.isRegularFile(filePath)) {
                    URLClassLoader cl = UtilManager.getURLClassLoader(this.getClass().getClassLoader(), filePath);
                    Stream<JarEntry> e = UtilManager.getEntries(filePath);

                    e.filter(UtilManager::checkIsClassFile)
                            .map(UtilManager::getClassName)
                            .map(clazz -> UtilManager.loadClass(clazz, cl))
                            .filter(clazz -> UtilManager.implementInterface(clazz, IProducerConnector.class))
                            .forEach(clazz -> {
                                IProducerConnector scoreProcessor = (IProducerConnector) UtilManager.newInstance(clazz);
                                Thread thread = new Thread(() -> scoreProcessor.load(new DataProducer(dataQueue)));
                                thread.start();
                                logger.info("ProducerManager info {}", "Producer " + scoreProcessor.getClass().getName() + " has been launched");
                                producerConnectors.add(thread);
                            });

                    try {
                        cl.close();
                    } catch (IOException e1) {
                        logger.warn("ProducerManager warning {}", "Failed to close loader");
                    }
                }
            });
        } catch (IOException e) {
            logger.error("ProducerManager error {}", e.getMessage());
            throw new IOException(e.getMessage());
        }
        logger.info("ProducerManager info {}", "All producer has been launched");
    }

    /**
     * Stop producer and consumer when dataQueue is empty
     */
    public void stop() {
        producerConnectors.forEach(Thread::interrupt);

        // Wait the DataQueue is Empty
        while (dataQueue.size() >= 0) {

        }

        dataConsumers.forEach(Thread::interrupt);
    }

    /**
     * Force stop all producers and consumers
     */
    public void forceStop() {
        producerConnectors.forEach(Thread::interrupt);
        dataConsumers.forEach(Thread::interrupt);
    }
}
