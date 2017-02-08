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
import java.util.jar.JarEntry;
import java.util.stream.Stream;

public class CommunicationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationManager.class);
    private static CommunicationManager ourInstance = new CommunicationManager();
    private final Map<Thread, ICommunication> threadCommunications = new HashMap<>();
    private final DatabaseReader databaseReader;

    private CommunicationManager() {
        this.databaseReader = DatabaseReader.getInstance();
    }

    public static CommunicationManager getInstance() {
        return ourInstance;
    }

    public void start()  {
        String stringPath = PropertiesManager.getInstance().getProperty("communication.path");
        try (Stream<Path> paths = Files.walk(Paths.get(stringPath))) {
            paths.forEach((Path filePath) -> {
                if (Files.isRegularFile(filePath)) {
                    URLClassLoader cl = UtilManager.getURLClassLoader(this.getClass().getClassLoader(), filePath);
                    Stream<JarEntry> e = UtilManager.getEntries(filePath);

                    e.filter(UtilManager::checkIsClassFile)
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
                }
            });
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("All ICommunication has been launched");
    }

    public void stop() {
        threadCommunications.values().forEach(ICommunication::close);
        LOGGER.info("Closing communications...");

        threadCommunications.keySet().forEach(Thread::interrupt);
        LOGGER.info("CommunicationManager all communications thread has been stoped");
    }
}
