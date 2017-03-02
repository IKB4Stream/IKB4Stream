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

package com.waves_rsp.ikb4stream.producer.score;

import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.model.Event;
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
import java.util.*;
import java.util.stream.Stream;

/**
 * Object which manage all {@link IScoreProcessor}
 *
 * @author ikb4stream
 * @version 1.0
 */
public class ScoreProcessorManager {
    /**
     * Properties of this class
     *
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(ScoreProcessorManager.class);
    /**
     * Logger used to log all information in this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreProcessorManager.class);
    /**
     * Association {@link Event#source} to a list of {@link IScoreProcessor}
     *
     * @see ScoreProcessorManager#findIScoreProcessor(String)
     * @see ScoreProcessorManager#launchModule(JarLoader)
     */
    private final Map<String, List<IScoreProcessor>> scoreProcessors = new HashMap<>();
    /**
     * ClassLoader of {@link ScoreProcessorManager}
     *
     * @see ScoreProcessorManager#launchModule(JarLoader)
     */
    private final ClassLoader parent = ScoreProcessorManager.class.getClassLoader();

    /**
     * Override default constructor
     *
     * @see ScoreProcessorManager#instanciate()
     */
    public ScoreProcessorManager() {
        instanciate();
    }

    /**
     * Process NLP Algorithm to an event
     *
     * @param event {@link Event} to score
     * @return Copy of {@link Event} with a new score
     * @throws NullPointerException if event is null
     * @see ScoreProcessorManager#findIScoreProcessor(String)
     * @see ScoreProcessorManager#process(List, Event)
     */
    public Event processScore(Event event) {
        Objects.requireNonNull(event);
        List<IScoreProcessor> sp = findIScoreProcessor(event.getSource());
        return process(sp, event);
    }

    /**
     * Find {@link IScoreProcessor} to apply to a {@link Event#source}
     *
     * @param source Origin of the {@link Event}
     * @return List of all {@link IScoreProcessor} to apply
     * @throws NullPointerException if source is null
     * @see ScoreProcessorManager#scoreProcessors
     */
    private List<IScoreProcessor> findIScoreProcessor(String source) {
        Objects.requireNonNull(source);
        List<IScoreProcessor> sp = scoreProcessors.get(source);
        if (sp == null) return new ArrayList<>();
        return sp;
    }

    /**
     * Apply all {@link IScoreProcessor} to the {@link Event}
     *
     * @param scoreProcessor List of all {@link IScoreProcessor} to apply
     * @param event          {@link Event} to process
     * @return Copy of {@link Event} with its {@link Event#score} process
     * @throws NullPointerException if scoreProcessor or event is null
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
     * Get all {@link IScoreProcessor} associated to {@link Event#source}
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
     * Get path where {@link IScoreProcessor} are store
     *
     * @return Path or null if there is invalid configuration
     * @see ScoreProcessorManager#PROPERTIES_MANAGER
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
     * Launch module of {@link IScoreProcessor}
     *
     * @param jarLoader {@link JarLoader} that represents module
     * @see ScoreProcessorManager#scoreProcessors
     * @see ScoreProcessorManager#parent
     * @see IScoreProcessor
     */
    private void launchModule(JarLoader jarLoader) {
        if (jarLoader != null) {
            List<URL> urls = jarLoader.getUrls();
            ClassLoader classLoader = AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> new URLClassLoader(urls.toArray(new URL[urls.size()]), parent));
            jarLoader.getClasses().stream()
                    .map(c -> ClassManager.loadClass(c, classLoader))
                    .filter(c -> ClassManager.implementInterface(c, IScoreProcessor.class))
                    .forEach(clazz -> {
                        try {
                            IScoreProcessor iScoreProcessor = (IScoreProcessor) ClassManager.newInstance(clazz);
                            iScoreProcessor.getSources().forEach(source -> {
                                List<IScoreProcessor> iScoreProcessorList = scoreProcessors.computeIfAbsent(source, l -> new ArrayList<>());
                                iScoreProcessorList.add(iScoreProcessor);
                            });
                        } catch (Exception e) {
                            LOGGER.error("Error during instantiate {} : {}", clazz.getName(), e.getMessage());
                        }
                    });
        }
    }
}
