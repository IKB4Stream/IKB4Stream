package com.waves_rsp.ikb4stream.consumer;

import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.core.util.UtilManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.stream.Stream;

public class CommunicationManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static CommunicationManager ourInstance = new CommunicationManager();
    private final List<Thread> threadCommunications = new LinkedList<>();
    private final DatabaseReader databaseReader;

    public static CommunicationManager getInstance() {
        return ourInstance;
    }

    private CommunicationManager() {
        this.databaseReader = DatabaseReader.getInstance();
    }

    public void start() throws IOException {
        String stringPath = PropertiesManager.getInstance().getProperty("scoreprocessormanager.path");
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
                                logger.info("CommunicationManager info {}", "CommunicationManager " + iCommunication.getClass().getName() + " has been launched");
                                threadCommunications.add(thread);
                            });
                }
            });
        }
    }

    public void stop() {
        threadCommunications.forEach(Thread::interrupt);
        logger.info("CommunicationManager info {}", "CommunicationManager all communications thread has been stoped");
    }
}
