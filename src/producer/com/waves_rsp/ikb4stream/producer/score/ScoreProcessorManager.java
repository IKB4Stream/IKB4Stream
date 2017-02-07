package com.waves_rsp.ikb4stream.producer.score;

import com.waves_rsp.ikb4stream.core.model.Event;
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
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.stream.Stream;

public class ScoreProcessorManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String,IScoreProcessor> scoreProcessors = new HashMap<>();

    public Event processScore(Event event) {
        Objects.requireNonNull(event);
        // TODO: ProcessScore
        byte score = 20;
        return new Event(event.getLocation(), event.getStart(), event.getEnd(), event.getDescription(), score, event.getSource());
    }

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
                            .filter(clazz -> UtilManager.implementInterface(clazz, IScoreProcessor.class))
                            .forEach(clazz -> {
                                IScoreProcessor scoreProcessor = (IScoreProcessor) UtilManager.newInstance(clazz);
                                scoreProcessors.put(scoreProcessor.getClass().getName(), scoreProcessor);
                                logger.info("ScoreProcessorManager info {}", "ScoreProcessor " + scoreProcessor.getClass().getName() + " has been launched");
                            });
                }
            });
        }
    }

    public static void main(String[] args) throws IOException {
        ScoreProcessorManager producerManager = new ScoreProcessorManager();
        producerManager.instanciate();
    }
}
