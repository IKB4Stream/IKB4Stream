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

public interface UtilManager {
    Logger LOGGER = LoggerFactory.getLogger(UtilManager.class);

    static URLClassLoader getURLClassLoader(ClassLoader classLoader, Path path) {
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

    static Stream<JarEntry> getEntries(Path path) {
        Objects.requireNonNull(path);
        String pathToJar = path.toString();
        try {
            JarFile jarFile = new JarFile(pathToJar);
            return jarFile.stream();
        } catch (IOException e) {
            throw new IllegalArgumentException("path to JAR is not correct: " + pathToJar);
        }
    }

    static Object newInstance(Class clazz) {
        Objects.requireNonNull(clazz);
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Class : " + clazz.getName() + " cannot be cast");
        }
    }

    static Class loadClass(String className, URLClassLoader cl) {
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

    static boolean checkIsClassFile(JarEntry jar) {
        Objects.requireNonNull(jar);
        return !(jar.isDirectory() || !jar.getName().endsWith(".class"));
    }

    static String getClassName(JarEntry jar) {
        Objects.requireNonNull(jar);
        String className = jar.getName().substring(0,jar.getName().length()-6);
        return className.replace(File.separator, ".");
    }

    static boolean implementInterface(Class clazz, Class interfaceClass) {
        if (clazz == null) return false;
        Objects.requireNonNull(interfaceClass);
        return Arrays.stream(clazz.getInterfaces())
                .anyMatch(i -> i.equals(interfaceClass));
    }
}