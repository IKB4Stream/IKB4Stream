package com.waves_rsp.ikb4stream.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;

public class UtilManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(UtilManager.class);

    private UtilManager() {

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
     * @param child ClassLoader used to load this {@param className}
     * @return Class if it's can be instantiate or null otherwise
     * @throws NullPointerException if {@param className} or {@param child} is null
     */
    public static Class<?> loadClass(String className, ClassLoader child) {
        Objects.requireNonNull(className);
        Objects.requireNonNull(child);
        try {
            return Class.forName(className, true, child);
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Class " + className + " cannot be found");
            return null;
        } catch (NoClassDefFoundError e1) {
            LOGGER.warn("ClassDef" + className + " cannot be found");
            return null;
        }
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