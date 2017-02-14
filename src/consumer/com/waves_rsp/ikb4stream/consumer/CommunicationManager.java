package com.waves_rsp.ikb4stream.consumer;

import com.waves_rsp.ikb4stream.core.communication.ICommunication;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.core.util.UtilManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * CommunicationManager class ensure the communication between IKB4Stream module and external services
 * @see DatabaseReader which allows request from mongodb
 * @see ICommunication which start and stop communication
 */
public class CommunicationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationManager.class);
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(CommunicationManager.class, "resources/config.properties");
    private static CommunicationManager ourInstance = new CommunicationManager();
    private final Map<Thread, ICommunication> threadCommunications = new HashMap<>();
    private final DatabaseReader databaseReader;

    /**
     * the constructor of CommunicationManager
     */
    private CommunicationManager() {
        this.databaseReader = DatabaseReader.getInstance();
    }

    public static CommunicationManager getInstance() {
        return ourInstance;
    }

    /**
     * This method launches the CommunicationManager
     * @throws IllegalArgumentException if communication.path is not set
     */
    public void start() {
        String stringPath = PROPERTIES_MANAGER.getProperty("communication.path");
        try (Stream<Path> paths = Files.walk(Paths.get(stringPath))) {
            paths.forEach((Path filePath) -> {
                if (Files.isRegularFile(filePath) && filePath.endsWith(".jar")) {
                    URLClassLoader cl = UtilManager.getURLClassLoader(this.getClass().getClassLoader(), filePath);
                    UtilManager.getEntries(filePath).filter(UtilManager::checkIsClassFile)
                            .map(UtilManager::getClassName)
                            .map(clazz -> UtilManager.loadClass(clazz, cl))
                            .filter(clazz -> UtilManager.implementInterface(clazz, ICommunication.class))
                            .forEach(clazz -> {
                                ICommunication iCommunication = (ICommunication) UtilManager.newInstance(clazz);
                                Thread thread = new Thread(() -> iCommunication.start(databaseReader));
                                thread.start();
                                LOGGER.info("CommunicationManager " + iCommunication.getClass().getName() + " has been launched");
                                threadCommunications.put(thread, iCommunication);
                            });
                    closeURLClassLoader(cl);
                }
            });
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("All ICommunication has been launched");
    }

    private void closeURLClassLoader(URLClassLoader cl) {
        try {
            cl.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * This method stop the CommunicationManager properly
     */
    public void stop() {
        threadCommunications.values().forEach(ICommunication::close);
        LOGGER.info("Closing communications...");

        threadCommunications.keySet().forEach(Thread::interrupt);
        LOGGER.info("CommunicationManager all communications thread has been stoped");
    }
}