package com.waves_rsp.ikb4stream.producer.score;

import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class ScoreProcessorManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreProcessorManager.class);
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(ScoreProcessorManager.class, "resources/config.properties");
    private final Map<String,IScoreProcessor> scoreProcessors = new HashMap<>();

    public Event processScore(Event event) {
        Objects.requireNonNull(event);
        try {
            String scoreProcessor = PROPERTIES_MANAGER.getProperty(event.getSource());
            LOGGER.info(scoreProcessor + " will be applied in an event from " + event.getSource());
            return process(getProcessors(scoreProcessor), event);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("There isn't any ScoreProcessor for " + event.getSource());
            return event;
        }
    }

    private String[] getProcessors(String scoreProcessors) {
        Objects.requireNonNull(scoreProcessors);
        return scoreProcessors.split(",");
    }

    private Event process(String[] scoreProcessor, Event event) {
        Objects.requireNonNull(scoreProcessor);
        Arrays.stream(scoreProcessor).forEach(Objects::requireNonNull);
        Objects.requireNonNull(event);
        Event tmp = event;
        for (String stringSp : scoreProcessor) {
            IScoreProcessor sp = scoreProcessors.get(stringSp);
            if (sp != null) {
                tmp = sp.processScore(tmp);
            }
        }
        return tmp;
    }

    public void instanciate() {
        String stringPath = PROPERTIES_MANAGER.getProperty("scoreprocessor.path");
        try (Stream<Path> paths = Files.walk(Paths.get(stringPath))) {
            paths.forEach((Path filePath) -> {
                if (Files.isRegularFile(filePath)) {
                    URLClassLoader cl = UtilManager.getURLClassLoader(this.getClass().getClassLoader(), filePath);
                    UtilManager.getEntries(filePath).filter(UtilManager::checkIsClassFile)
                            .map(UtilManager::getClassName)
                            .map(clazz -> UtilManager.loadClass(clazz, cl))
                            .filter(clazz -> UtilManager.implementInterface(clazz, IScoreProcessor.class))
                            .forEach(clazz -> {
                                String jarName = filePath.getFileName().toString();
                                scoreProcessors.put(jarName, (IScoreProcessor) UtilManager.newInstance(clazz));
                                LOGGER.info("ScoreProcessor " + jarName + " has been launched");
                            });
                    closeURLClassLoader(cl);
                }
            });
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void closeURLClassLoader(URLClassLoader cl) {
        try {
            cl.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
