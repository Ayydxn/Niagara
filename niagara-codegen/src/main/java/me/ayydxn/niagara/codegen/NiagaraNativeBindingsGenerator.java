package me.ayydxn.niagara.codegen;

import me.ayydxn.niagara.codegen.ffm.FFMNativeBindingsEmitter;
import me.ayydxn.niagara.codegen.jni.JNINativeBindingsEmitter;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static void main(String[] args) throws Exception
    {
        Map<String, String> options = parseOptions(args);

        String backend = option(options, "backend");
        Path contractsJar = Path.of(option(options, "contracts"));

        try (URLClassLoader loader = new URLClassLoader(new URL[] {contractsJar.toUri().toURL()}, NiagaraNativeBindingsGenerator.class.getClassLoader()))
        {
            List<NativeInterfaceModel.Contract> contracts = NativeInterfaceDiscovery.discover(contractsJar, loader).stream().map(NativeInterfaceReader::read).toList();
            if (contracts.isEmpty())
                throw new IllegalArgumentException("No @NativeContract interfaces found in " + contractsJar);

            switch (backend)
            {
                case "jni" -> generateJNIBindings(options, contracts);
                case "ffm" -> generateFFMBindings(options, contracts);
                default -> throw new IllegalArgumentException("Unknown backend \"" + backend + "\" (expected jni or ffm)");
            }
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

    private static Map<String, String> parseOptions(String[] args)
    {
        Map<String, String> options = new HashMap<>();

        for (String arg : args)
        {
            int separator = arg.indexOf('=');
            if (!arg.startsWith("--") || separator < 0)
                throw new IllegalArgumentException("Expected --name=value, got " + arg);

            options.put(arg.substring(2, separator), arg.substring(separator + 1));
        }

        return options;
    }

    private static String option(Map<String, String> options, String name)
    {
        String value = options.get(name);
        if (value == null)
            throw new IllegalArgumentException("Missing --" + name + "=<value>");

        return value;
    }
}
