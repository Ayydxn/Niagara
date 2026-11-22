package me.ayydxn.niagara.codegen;

import me.ayydxn.niagara.codegen.fixtures.NotANativeInterface;
import me.ayydxn.niagara.codegen.fixtures.SampleNatives;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorEndToEndTest
{
    @TempDir
    Path temp;

    @Test
    void discoversOnlyContractsInTheJar() throws IOException
    {
        Path jar = createJar(SampleNatives.class, NotANativeInterface.class);

        assertEquals(List.of(SampleNatives.class), NativeInterfaceDiscovery.discover(jar, SampleNatives.class.getClassLoader()));
    }

    @Test
    void generatesTheJniBindingsFromAJar() throws Exception
    {
        Path jar = createJar(SampleNatives.class, NotANativeInterface.class);

        NiagaraNativeBindingsGenerator.main(new String[] {"--backend=jni", "--contracts=" + jar, "--java-out=" + temp.resolve("java"),
                "--cpp-out=" + temp.resolve("cpp"), "--jni-package=me.ayydxn.niagara.jni.generated"});

        assertTrue(Files.exists(temp.resolve("java/me/ayydxn/niagara/jni/generated/JNISampleNatives.java")));
        assertTrue(Files.exists(temp.resolve("cpp/ultralight/Sample.h")));
        assertTrue(Files.exists(temp.resolve("cpp/ultralight/Sample.cpp")));
        assertTrue(Files.exists(temp.resolve("cpp/GeneratedNativeInterfaces.h")));
    }

    @Test
    void failsWhenTheJarHasNoContracts() throws IOException
    {
        Path jar = createJar(NotANativeInterface.class);

        assertThrows(IllegalArgumentException.class, () -> NiagaraNativeBindingsGenerator.main(new String[] {"--backend=jni", "--contracts=" + jar,
                "--java-out=" + temp.resolve("java"), "--cpp-out=" + temp.resolve("cpp")}));
    }

    @Test
    void generatesTheFfmBindingsFromAJar() throws Exception
    {
        Path jar = createJar(SampleNatives.class, NotANativeInterface.class);

        NiagaraNativeBindingsGenerator.main(new String[] {"--backend=ffm", "--contracts=" + jar, "--java-out=" + temp.resolve("java"),
                "--ffm-package=me.ayydxn.niagara.ffm.generated"});

        assertTrue(Files.exists(temp.resolve("java/me/ayydxn/niagara/ffm/generated/AbstractFFMSampleNatives.java")));
    }

    @Test
    void failsOnMissingOrUnknownOptions() throws IOException
    {
        Path jar = createJar(SampleNatives.class);

        assertThrows(IllegalArgumentException.class, () -> NiagaraNativeBindingsGenerator.main(new String[] {"--backend=jni"}));
        assertThrows(IllegalArgumentException.class, () -> NiagaraNativeBindingsGenerator.main(new String[] {"--backend=nope", "--contracts=" + jar}));
        assertThrows(IllegalArgumentException.class, () -> NiagaraNativeBindingsGenerator.main(new String[] {"--backend=ffm", "--contracts=" + jar}));
    }

    private Path createJar(Class<?>... classes) throws IOException
    {
        Path jar = Files.createTempFile(temp, "contracts", ".jar");

        try (OutputStream output = Files.newOutputStream(jar); JarOutputStream jarOutput = new JarOutputStream(output))
        {
            for (Class<?> type : classes)
            {
                String entryName = type.getName().replace('.', '/') + ".class";

                try (InputStream classFile = type.getClassLoader().getResourceAsStream(entryName))
                {
                    jarOutput.putNextEntry(new JarEntry(entryName));
                    jarOutput.write(classFile.readAllBytes());
                    jarOutput.closeEntry();
                }
            }
        }

        return jar;
    }
}