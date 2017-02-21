package com.waves_rsp.ikb4stream.producer.score;

import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.model.Event;
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
import java.util.*;
import java.util.stream.Stream;

public class ScoreProcessorManager {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(ScoreProcessorManager.class, "resources/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreProcessorManager.class);
    private final Map<String,List<IScoreProcessor>> scoreProcessors = new HashMap<>();
    private final ClassLoader parent = ScoreProcessorManager.class.getClassLoader();

    /**
     * Override default constructor
     */
    public ScoreProcessorManager() {
        instanciate();
    }

    /**
     * Process NLP Algorithm to an event
     * @param event Event to score
     * @return Copy of {@param event} with a new score
     * @throws NullPointerException if {@param event} is null
     */
    public Event processScore(Event event) {
        Objects.requireNonNull(event);
        List<IScoreProcessor> sp = findIScoreProcessor(event.getSource());
        return process(sp, event);
    }

    /**
     * Find ScoreProcessor to apply to a datasource
     * @param source Origin of the datasource
     * @return List of all ScoreProcessor to apply
     * @throws NullPointerException if {@param source} is null
     */
    private List<IScoreProcessor> findIScoreProcessor(String source) {
        Objects.requireNonNull(source);
        List<IScoreProcessor> sp = scoreProcessors.get(source);
        if (sp == null) return new ArrayList<>();
        return sp;
    }

    /**
     * Apply all ScoreProcessor to the Event
     * @param scoreProcessor List of all scoreprocessor to apply
     * @param event Event to process
     * @return Copy of {@param event} with its score process
     * @throws NullPointerException if {@param scoreProcessor} or {@param event} is null
     */
    private static Event process(List<IScoreProcessor> scoreProcessor, Event event) {
        Objects.requireNonNull(scoreProcessor);
        Objects.requireNonNull(event);
        LOGGER.info("Process {}", event);
        Event tmp = event;
        for (IScoreProcessor sp : scoreProcessor) {
            LOGGER.info("With {}", sp);
            tmp = sp.processScore(tmp);
        }
        LOGGER.info("New {}", tmp);
        return tmp;
    }

    /**
     * Get all ScoreProcessor
     */
    private void instanciate() {
        String stringPath = getPathScoreProcessor();
        if (stringPath == null) return;
        try (Stream<Path> paths = Files.walk(Paths.get(stringPath))) {
            paths.forEach((Path filePath) -> {
                if (Files.isRegularFile(filePath)) {
                    launchModule(JarLoader.createJarLoader(filePath.toString()));
                }
            });
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Get path where ScoreProcessor are store
     * @return Path or null if there is invalid configuration
     */
    private static String getPathScoreProcessor() {
        try {
            return PROPERTIES_MANAGER.getProperty("scoreprocessor.path");
        } catch (IllegalArgumentException e) {
            LOGGER.warn(e.getMessage());
            LOGGER.warn("There is no ScoreProcessor to load");
            return null;
        }
    }

    /**
     * Launch module of ScoreProcessor
     * @param jarLoader JarLoader that represents module
     */
    private void launchModule(JarLoader jarLoader) {
        if (jarLoader != null) {
            List<URL> urls = jarLoader.getUrls();
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                ClassLoader classLoader = new URLClassLoader(
                        urls.toArray(new URL[urls.size()]),
                        parent);
                List<String> classes = jarLoader.getClasses();
                classes.stream()
                        .map(c -> UtilManager.loadClass(c, classLoader))
                        .filter(c -> UtilManager.implementInterface(c, IScoreProcessor.class))
                        .forEach(clazz -> {
                            IScoreProcessor iScoreProcessor = (IScoreProcessor) UtilManager.newInstance(clazz);
                            List<String> sources = iScoreProcessor.getSources();
                            sources.forEach(source -> {
                                List<IScoreProcessor> iScoreProcessorList = scoreProcessors.computeIfAbsent(source, l -> new ArrayList<>());
                                iScoreProcessorList.add(iScoreProcessor);
                            });
                        });
                return null;
            });
        }
    }
}
