package com.waves_rsp.ikb4stream.producer.score;

import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreProcessorManager.class);
    private final Map<String,IScoreProcessor> scoreProcessors = new HashMap<>();

    public Event processScore(Event event) {
        Objects.requireNonNull(event);
        // TODO: ProcessScore
        byte score = 20;
        return new Event(event.getLocation(), event.getStart(), event.getEnd(), event.getDescription(), score, event.getSource());
    }

    public void instanciate() {
        String stringPath = PropertiesManager.getInstance().getProperty("scoreprocessor.path");
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
                                LOGGER.info("ScoreProcessor " + scoreProcessor.getClass().getName() + " has been launched");
                            });
                }
            });
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
