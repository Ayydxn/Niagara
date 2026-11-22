package me.ayydxn.niagara.codegen;

import me.ayydxn.niagara.codegen.ffm.FFMNativeBindingsEmitter;
import me.ayydxn.niagara.codegen.jni.JNINativeBindingsEmitter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NiagaraNativeBindingsGenerator
{
    private static final String DEFAULT_JNI_PACKAGE = "me.ayydxn.niagara.jni.generated";
    private static final String DEFAULT_FFM_PACKAGE = "me.ayydxn.niagara.ffm.generated";

    private NiagaraNativeBindingsGenerator()
    {
    }

    /**
     * Options are passed as {@code --name=value}. Every other argument is the fully qualified name of a native interface, which has to be on the generator's classpath.
     */
    public static void main(String[] args) throws Exception
    {
        Map<String, String> options = new HashMap<>();
        List<String> nativeInterfaceNames = new ArrayList<>();

        parseArguments(args, options, nativeInterfaceNames);

        String backend = option(options, "backend");
        if (nativeInterfaceNames.isEmpty())
            throw new IllegalArgumentException("Pass the class names of the native interfaces to generate bindings for");

        List<NativeInterfaceModel.Contract> contracts = nativeInterfaceNames.stream().map(NiagaraNativeBindingsGenerator::load).map(NativeInterfaceReader::read).toList();

        switch (backend)
        {
            case "jni" -> generateJNIBindings(options, contracts);
            case "ffm" -> generateFFMBindings(options, contracts);
            default -> throw new IllegalArgumentException("Unknown backend \"" + backend + "\" (expected jni or ffm)");
        }
    }

    private static Class<?> load(String className)
    {
        try
        {
            return Class.forName(className, false, NiagaraNativeBindingsGenerator.class.getClassLoader());
        }
        catch (ClassNotFoundException exception)
        {
            throw new IllegalArgumentException("Couldn't find the native interface " + className + " on the generator's classpath", exception);
        }
    }

    private static void generateJNIBindings(Map<String, String> options, List<NativeInterfaceModel.Contract> contracts) throws IOException
    {
        Path javaOut = Path.of(option(options, "java-out"));
        Path cppOut = Path.of(option(options, "cpp-out"));

        JNINativeBindingsEmitter nativeBindingsEmitter = new JNINativeBindingsEmitter(options.getOrDefault("jni-package", DEFAULT_JNI_PACKAGE));

        write(javaOut, nativeBindingsEmitter.javaFiles(contracts));
        write(cppOut, nativeBindingsEmitter.cppFiles(contracts));
    }

    private static void generateFFMBindings(Map<String, String> options, List<NativeInterfaceModel.Contract> contracts) throws IOException
    {
        Path javaOut = Path.of(option(options, "java-out"));

        FFMNativeBindingsEmitter nativeBindingsEmitter = new FFMNativeBindingsEmitter(options.getOrDefault("ffm-package", DEFAULT_FFM_PACKAGE));

        write(javaOut, nativeBindingsEmitter.javaFiles(contracts));
    }

    private static void write(Path root, Map<String, String> files) throws IOException
    {
        for (Map.Entry<String, String> file : files.entrySet())
        {
            Path path = root.resolve(file.getKey());

            Files.createDirectories(path.getParent());
            Files.writeString(path, file.getValue());
        }
    }

    private static void parseArguments(String[] args, Map<String, String> options, List<String> nativeInterfaceNames)
    {
        for (String arg : args)
        {
            if (!arg.startsWith("--"))
            {
                nativeInterfaceNames.add(arg);
                continue;
            }

            int separator = arg.indexOf('=');
            if (separator < 0)
                throw new IllegalArgumentException("Expected --name=value, got " + arg);

            options.put(arg.substring(2, separator), arg.substring(separator + 1));
        }
    }

    private static String option(Map<String, String> options, String name)
    {
        String value = options.get(name);
        if (value == null)
            throw new IllegalArgumentException("Missing --" + name + "=<value>");

        return value;
    }
}
