package com.waves_rsp.ikb4stream.core.util;

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

public abstract class UtilManager {
    public static URLClassLoader getURLClassLoader(ClassLoader classLoader, Path path) {
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

    public static Object newInstance(Class clazz) {
        Objects.requireNonNull(clazz);
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Class : " + clazz.getName() + " cannot be cast");
        }
    }

    public static Class loadClass(String className, URLClassLoader cl) {
        Objects.requireNonNull(className);
        Objects.requireNonNull(cl);
        try {
            return Class.forName(className, true, cl);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class " + className + " cannot be instanced");
        }
    }

    public static boolean checkIsClassFile(JarEntry jar) {
        Objects.requireNonNull(jar);
        return !(jar.isDirectory() || !jar.getName().endsWith(".class"));
    }

    public static String getClassName(JarEntry jar) {
        Objects.requireNonNull(jar);
        String className = jar.getName().substring(0,jar.getName().length()-6);
        return className.replace(File.separator, ".");
    }

    public static boolean implementInterface(Class clazz, Class interfaceClass) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(interfaceClass);
        return Arrays.stream(clazz.getInterfaces())
            .anyMatch(i -> i.equals(interfaceClass));
    }
}
