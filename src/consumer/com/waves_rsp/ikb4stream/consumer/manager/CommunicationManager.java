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

package com.waves_rsp.ikb4stream.consumer.manager;

import com.waves_rsp.ikb4stream.consumer.database.DatabaseReader;
import com.waves_rsp.ikb4stream.core.communication.ICommunication;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * CommunicationManager class ensure the communication between IKB4Stream module and external services
 *
 * @author ikb4stream
 * @version 1.0
 */
public class CommunicationManager {
    /**
     * Properties of this class
     *
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(CommunicationManager.class);
    /**
     * Single instance of {@link CommunicationManager}
     */
    private static final CommunicationManager COMMUNICATION_MANAGER = new CommunicationManager();
    /**
     * Logger used to log all information in this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationManager.class);
    /**
     * Map< Thread,ICommunication > to associate a thread to a ICommunication
     *
     * @see ICommunication
     * @see CommunicationManager#launchModule(JarLoader)
     * @see CommunicationManager#stop()
     */
    private final Map<Thread, ICommunication> threadCommunications = new HashMap<>();
    /**
     * ClassLoader of {@link CommunicationManager}
     */
    private final ClassLoader parent = CommunicationManager.class.getClassLoader();
    /**
     * {@link DatabaseReader} to read event from Database
     */
    private final DatabaseReader databaseReader;

    /**
     * The constructor of {@link CommunicationManager}
     */
    private CommunicationManager() {
        this.databaseReader = DatabaseReader.getInstance();
    }

    /**
     * Get single instance of {@link CommunicationManager}
     *
     * @return Single instance of {@link CommunicationManager}
     * @see CommunicationManager#COMMUNICATION_MANAGER
     */
    public static CommunicationManager getInstance() {
        return COMMUNICATION_MANAGER;
    }

    /**
     * This method launches the CommunicationManager
     *
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
     *
     * @param jarLoader JarLoader that represents module
     * @see CommunicationManager#databaseReader
     * @see CommunicationManager#threadCommunications
     * @see CommunicationManager#parent
     */
    private void launchModule(JarLoader jarLoader) {
        if (jarLoader != null) {
            List<URL> urls = jarLoader.getUrls();
            ClassLoader classLoader = AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> new URLClassLoader(urls.toArray(new URL[urls.size()]), parent));
            jarLoader.getClasses().stream()
                    .map(c -> ClassManager.loadClass(c, classLoader))
                    .filter(c -> ClassManager.implementInterface(c, ICommunication.class))
                    .forEach(clazz -> {
                        try {
                            ICommunication iCommunication = (ICommunication) ClassManager.newInstance(clazz);
                            if (iCommunication.isActive()) {
                                Thread thread = new Thread(() -> iCommunication.start(databaseReader));
                                thread.setContextClassLoader(classLoader);
                                thread.setName(iCommunication.getClass().getName());
                                thread.start();
                                threadCommunications.put(thread, iCommunication);
                            }
                        } catch (Exception e) {
                            LOGGER.error("Error during instantiate {} : {}", clazz.getName(), e.getMessage());
                        }
                    });
        }
    }


    /**
     * This method stop the CommunicationManager properly
     *
     * @see CommunicationManager#threadCommunications
     */
    public void stop() {
        LOGGER.info("Closing communications...");
        threadCommunications.keySet().forEach(thread -> {
            ICommunication communication = threadCommunications.get(thread);
            thread.interrupt();
            communication.close();
        });
        LOGGER.info("CommunicationManager all communications thread has been stoped");
    }

    /**
     * Get path where Communication are store
     *
     * @return Path or null if there is invalid configuration
     * @see CommunicationManager#PROPERTIES_MANAGER
     */
    private static String getPathCommunication() {
        try {
            return PROPERTIES_MANAGER.getProperty("communication.path");
        } catch (IllegalArgumentException e) {
            LOGGER.warn(e.getMessage());
            return null;
        }
    }
}