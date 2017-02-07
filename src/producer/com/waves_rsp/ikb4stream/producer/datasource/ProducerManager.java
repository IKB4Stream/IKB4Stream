package com.waves_rsp.ikb4stream.producer.datasource;

import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.core.util.UtilManager;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.stream.Stream;

public class ProducerManager {
    private final Map<String,IProducerConnector> producerConnectors = new HashMap<>();

    public void instanciate() throws IOException {
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
                                producerConnectors.put(scoreProcessor.getClass().getName(), scoreProcessor);
                            });
                }
            });
        }
    }

    public static void main(String[] args) throws IOException {
        ProducerManager producerManager = new ProducerManager();
        producerManager.instanciate();
    }
}
