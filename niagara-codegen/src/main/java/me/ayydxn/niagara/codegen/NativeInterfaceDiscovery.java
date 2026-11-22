package me.ayydxn.niagara.codegen;

import me.ayydxn.niagara.core.annotations.NativeInterface;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class NativeInterfaceDiscovery
{
    private NativeInterfaceDiscovery()
    {
    }

    /**
     * Finds every {@link NativeInterface} type in the jar. The classes are loaded (not initialized) through the given loader, which has to be able to see the jar.
     */
    public static List<Class<?>> discover(Path jar, ClassLoader classLoader) throws IOException
    {
        List<String> classNames = new ArrayList<>();

        try (JarFile jarFile = new JarFile(jar.toFile()))
        {
            jarFile.stream()
                    .map(JarEntry::getName)
                    .filter(name -> name.endsWith(".class") && !name.startsWith("META-INF/") && !name.endsWith("module-info.class") && !name.endsWith("package-info.class"))
                    .map(name -> name.substring(0, name.length() - ".class".length()).replace('/', '.'))
                    .forEach(classNames::add);
        }

        Collections.sort(classNames);

        List<Class<?>> contracts = new ArrayList<>();

        for (String className : classNames)
        {
            Class<?> type;

            try
            {
                type = Class.forName(className, false, classLoader);
            }
            catch (ClassNotFoundException | LinkageError exception)
            {
                throw new IllegalStateException("Couldn't load " + className + " from " + jar, exception);
            }

            if (type.isAnnotationPresent(NativeInterface.class))
                contracts.add(type);
        }

        return contracts;
    }
}
