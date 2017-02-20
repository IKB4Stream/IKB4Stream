package com.waves_rsp.ikb4stream.consumer.manager;

import com.waves_rsp.ikb4stream.consumer.database.DatabaseReader;
import com.waves_rsp.ikb4stream.core.communication.ICommunication;
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
import java.util.HashMap;
import java.util.List;
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
    private final Map<Thread, ICommunication> threadCommunications = new HashMap<>();
    private final ClassLoader parent = CommunicationManager.class.getClassLoader();
    private static CommunicationManager ourInstance = new CommunicationManager();
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
        String stringPath = getPathCommunication();
        if (stringPath == null) return;
        try (Stream<Path> paths = Files.walk(Paths.get(stringPath))) {
            paths.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".jar"))
                    .forEach((Path filePath) -> {
                        JarLoader jarLoader = JarLoader.createJarLoader(filePath.toString());
                        launchModule(jarLoader);
                    });
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("All ICommunication has been launched");
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
                        .filter(c -> UtilManager.implementInterface(c, ICommunication.class))
                        .forEach(clazz -> {
                            ICommunication iCommunication = (ICommunication) UtilManager.newInstance(clazz);
                            Thread thread = new Thread(() -> iCommunication.start(databaseReader));
                            thread.setContextClassLoader(classLoader);
                            thread.setName(iCommunication.getClass().getName());
                            thread.start();
                            LOGGER.info("CommunicationManager " + iCommunication.getClass().getName() + " has been launched");
                            threadCommunications.put(thread, iCommunication);
                        });

                return null;
            });
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

    /**
     * Get path where Communication are store
     * @return Path or null if there is invalid configuration
     */
    private static String getPathCommunication() {
        try {
            return PROPERTIES_MANAGER.getProperty("communication.path");
        } catch (IllegalArgumentException e) {
            LOGGER.warn(e.getMessage());
            LOGGER.warn("There is no Communication to load");
            return null;
        }
    }
}