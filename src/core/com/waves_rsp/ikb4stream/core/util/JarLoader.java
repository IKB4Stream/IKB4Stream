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

package com.waves_rsp.ikb4stream.core.util;

import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JarLoader {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(JarLoader.class, "resources/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(UtilManager.class);
    private final List<String> classes = new ArrayList<>();
    private final List<URL> urls = new ArrayList<>();
    private final String jar;

    /**
     * Take jar of JAR file, and Interface to implement
     * @param jar Path to JAR file to load
     * @throws NullPointerException if {@param jar} or {@param interfaces} is null
     */
    private JarLoader(String jar) {
        Objects.requireNonNull(jar);
        this.jar = jar;
    }

    /**
     * Create a JarLoader
     * @param jar Jar to load
     * @return JarLoo
     * @throws NullPointerException if {@param jar } is null
     */
    public static JarLoader createJarLoader(String jar) {
        Objects.requireNonNull(jar);
        if (!jar.toLowerCase().endsWith(".jar")) return null;
        JarLoader jarLoader = new JarLoader(jar);
        jarLoader.getModuleClasses();
        return jarLoader;
    }

    /**
     * Get list of all modules from JAR
     */
    private void getModuleClasses(){
        File file = new File(jar);
        JarFile jarFile = getJarFile(file);
        if (jarFile != null) {
            addModule(getModuleClassName(jarFile), file);
            closeJarFile(jarFile);
        }
    }

    /**
     * Add module to JarLoader
     * @param moduleClassName Class to load
     * @param file JAR's file
     * @throws NullPointerException if {@param file} is null
     */
    private void addModule(String moduleClassName, File file) {
        Objects.requireNonNull(file);
        try {
            if (moduleClassName != null) {
                urls.add(file.toURI().toURL());
                classes.add(moduleClassName);
            }
        } catch (MalformedURLException e) {
            LOGGER.error(file.getName() + " cannot be load");
        }
    }

    /**
     * Close a JarFile
     * @param jarFile JarFile to close
     */
    private static void closeJarFile(JarFile jarFile) {
        if (jarFile != null) {
            try {
                jarFile.close();
            } catch (IOException e) {
                LOGGER.warn("Error during closing JarFile");
            }
        }
    }

    /**
     * Get jar from a file
     * @param f File which represents a JAR
     * @return A JarFile object
     * @throws NullPointerException if {@param f} is null
     * @throws IllegalArgumentException if {@param f} is not a JAR
     */
    private static JarFile getJarFile(File f) {
        Objects.requireNonNull(f);
        JarFile jarFile;
        try {
            jarFile = new JarFile(f);
        } catch (IOException e) {
            LOGGER.error(f.getName() + " is not a JAR");
            throw new IllegalArgumentException(f.getName() + " is not a JAR");
        }
        return jarFile;
    }

    /**
     * Get module class name to load
     * @param jarFile JarFile where there is the module to load
     * @return String of module's name
     * @throws NullPointerException if {@param jarFile} is null
     */
    private static String getModuleClassName(JarFile jarFile) {
        Objects.requireNonNull(jarFile);
        String manifestField = "Module-Class";
        try {
            manifestField = PROPERTIES_MANAGER.getProperty("jar.manifest");
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Use default value for jar.manifest");
        }
        try {
            Manifest manifest = jarFile.getManifest();
            return manifest.getMainAttributes().getValue(manifestField);
        } catch (IOException e) {
            LOGGER.error("There isn't " + manifestField + " information in META-INF/MANIFEST.MF of your " + jarFile.getName());
            return null;
        }
    }

    /**
     * Get all list of class to load as String
     * @return List of class name
     */
    public List<String> getClasses() {
        return classes;
    }

    /**
     * Get all list of jar to load as URL
     * @return List of jar as url
     */
    public List<URL> getUrls() {
        return urls;
    }
}
