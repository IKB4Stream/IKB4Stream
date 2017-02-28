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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;

/**
 * ClassManager which provides static method to apply to Class
 * @author ikb4stream
 * @version 1.0
 * @see JarLoader
 */
public class ClassManager {
    /**
     * Logger used to log all information in this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassManager.class);

    /**
     * Private constructor to prevent instantiation
     */
    private ClassManager() {

    }

    /**
     * Create a new instance of clazz
     * @param clazz Class to instantiate
     * @return New object instantiated of clazz
     * @throws NullPointerException if clazz is null
     * @throws IllegalArgumentException if clazz cannot be instantiated
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
     * Load class which has className as class name
     * @param className Class name to load
     * @param child ClassLoader used to load this className
     * @return Class if it's can be instantiate or null otherwise
     * @throws NullPointerException if className or child is null
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
     * Test if clazz implements interfaceClass
     * @param clazz Class to test
     * @param interfaceClass Interface to test if it's implement
     * @return True if clazz implement interfaceClass
     * @throws NullPointerException if interfaceClass is null
     */
    public static boolean implementInterface(Class clazz, Class interfaceClass) {
        if (clazz == null) return false;
        Objects.requireNonNull(interfaceClass);
        return Arrays.stream(clazz.getInterfaces())
                .anyMatch(i -> i.equals(interfaceClass));
    }
}