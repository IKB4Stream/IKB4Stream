package com.waves_rsp.ikb4stream.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class UtilManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(UtilManager.class);

    private UtilManager() {

    }

    /**
     * Get URLClassLoader for a JAR file
     * @param classLoader ClassLoader from class where to instantiate class in JAR
     * @param path Path to JAR File
     * @return URLClassLoader for this {@param path}
     * @throws NullPointerException if {@param classloader} or {@param path} is null
     * @throws IllegalArgumentException if {@param path} is not a JAR file
     */
    public static URLClassLoader getURLClassLoader(ClassLoader classLoader, Path path) {
        Objects.requireNonNull(classLoader);
        Objects.requireNonNull(path);
        String pathToJar = path.toString();
        URLClassLoader cl;
        try {
            URL[] urls = new URL[]{ new URL("jar:file:" + pathToJar+"!/") };
            cl = URLClassLoader.newInstance(urls, classLoader);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("path to JAR is not correct: " + pathToJar);
        }
        return cl;
    }

    /**
     * Get stream of JarEntry from path
     * @param path Path to JAR File
     * @return Stream of JarEntry
     * @throws NullPointerException if {@param path} is null
     */
    public static Stream<JarEntry> getEntries(Path path) {
        Objects.requireNonNull(path);
        String pathToJar = path.toString();
        try {
            JarFile jarFile = new JarFile(pathToJar);
            return jarFile.stream();
        } catch (IOException e) {
            throw new IllegalArgumentException("path to JAR is not correct: " + pathToJar);
        }
    }

    /**
     * Create a new instance of {@param clazz}
     * @param clazz Class to instantiate
     * @return New object instantiated of {@param clazz}
     * @throws NullPointerException if {@param clazz} is null
     * @throws IllegalArgumentException if {@param clazz} cannot be instantiated
     */
    public static Object newInstance(Class clazz) {
        Objects.requireNonNull(clazz);
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Class : " + clazz.getName() + " cannot be cast");
        }
    }

    /**
     * Load class which has {@param className} as class name
     * @param className Class name to load
     * @param cl URLClassLoader used to load this {@param className}
     * @return Class if it's can be instantiate or null otherwise
     * @throws NullPointerException if {@param className} or {@param cl} is null
     */
    public static Class loadClass(String className, URLClassLoader cl) {
        Objects.requireNonNull(className);
        Objects.requireNonNull(cl);
        try {
            return Class.forName(className, false, cl);
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Class " + className + " cannot be found");
            return null;
        } catch (NoClassDefFoundError e1) {
            LOGGER.warn("ClassDef" + className + " cannot be found");
            return null;
        }
    }

    /**
     * Check if this JarEntry is a Class
     * @param jar JarEntry to test
     * @return True if it's a class
     * @throws NullPointerException if {@param jar} is null
     */
    public static boolean checkIsClassFile(JarEntry jar) {
        Objects.requireNonNull(jar);
        return !(jar.isDirectory() || !jar.getName().endsWith(".class"));
    }

    /**
     * Get class name of class file
     * @param jar Class as JarEntry
     * @return Class name of {@param jar}
     * @throws NullPointerException if {@param jar} is null
     */
    public static String getClassName(JarEntry jar) {
        Objects.requireNonNull(jar);
        String className = jar.getName().substring(0,jar.getName().length()-6);
        return className.replace(File.separator, ".");
    }

    /**
     * Test if {@param clazz} implements {@param interfaceClass}
     * @param clazz Class to test
     * @param interfaceClass Interface to test if it's implement
     * @return True if {@param clazz} implement {@param interfaceClass}
     * @throws NullPointerException if {@param interfaceClass} is null
     */
    public static boolean implementInterface(Class clazz, Class interfaceClass) {
        if (clazz == null) return false;
        Objects.requireNonNull(interfaceClass);
        return Arrays.stream(clazz.getInterfaces())
                .anyMatch(i -> i.equals(interfaceClass));
    }
}