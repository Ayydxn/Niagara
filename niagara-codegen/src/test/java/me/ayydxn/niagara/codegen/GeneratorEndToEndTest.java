package me.ayydxn.niagara.codegen;

import me.ayydxn.niagara.codegen.fixtures.NotANativeInterface;
import me.ayydxn.niagara.codegen.fixtures.PlainNatives;
import me.ayydxn.niagara.codegen.fixtures.SampleNatives;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorEndToEndTest
{
    @TempDir
    Path temp;

    @Test
    void generatesTheJniBindings() throws Exception
    {
        NiagaraNativeBindingsGenerator.main(new String[] {"--backend=jni", "--java-out=" + temp.resolve("java"), "--cpp-out=" + temp.resolve("cpp"),
                "--jni-package=me.ayydxn.niagara.jni.generated", SampleNatives.class.getName()});

        assertTrue(Files.exists(temp.resolve("java/me/ayydxn/niagara/jni/generated/JNISampleNatives.java")));
        assertTrue(Files.exists(temp.resolve("cpp/ultralight/Sample.h")));
        assertTrue(Files.exists(temp.resolve("cpp/ultralight/Sample.cpp")));
        assertTrue(Files.exists(temp.resolve("cpp/GeneratedNativeInterfaces.h")));
    }

    @Test
    void generatesTheFfmBindings() throws Exception
    {
        NiagaraNativeBindingsGenerator.main(new String[] {"--backend=ffm", "--java-out=" + temp.resolve("java"),
                "--ffm-package=me.ayydxn.niagara.ffm.generated", SampleNatives.class.getName()});

        assertTrue(Files.exists(temp.resolve("java/me/ayydxn/niagara/ffm/generated/AbstractFFMSampleNatives.java")));
    }

    @Test
    void generatesEveryNativeInterfaceThatIsPassed() throws Exception
    {
        NiagaraNativeBindingsGenerator.main(new String[] {"--backend=ffm", "--java-out=" + temp.resolve("java"), SampleNatives.class.getName(), PlainNatives.class.getName()});

        assertTrue(Files.exists(temp.resolve("java/me/ayydxn/niagara/ffm/generated/AbstractFFMSampleNatives.java")));
        assertTrue(Files.exists(temp.resolve("java/me/ayydxn/niagara/ffm/generated/FFMPlainNatives.java")));
    }

    @Test
    void failsWhenNoNativeInterfaceIsPassed()
    {
        assertThrows(IllegalArgumentException.class, () -> NiagaraNativeBindingsGenerator.main(new String[] {"--backend=jni",
                "--java-out=" + temp.resolve("java"), "--cpp-out=" + temp.resolve("cpp")}));
    }

    @Test
    void failsWhenAClassCantBeFound()
    {
        assertThrows(IllegalArgumentException.class, () -> NiagaraNativeBindingsGenerator.main(new String[] {"--backend=jni",
                "--java-out=" + temp.resolve("java"), "--cpp-out=" + temp.resolve("cpp"), "me.ayydxn.niagara.DoesNotExist"}));
    }

    @Test
    void failsWhenAClassIsNotANativeInterface()
    {
        assertThrows(IllegalArgumentException.class, () -> NiagaraNativeBindingsGenerator.main(new String[] {"--backend=jni",
                "--java-out=" + temp.resolve("java"), "--cpp-out=" + temp.resolve("cpp"), NotANativeInterface.class.getName()}));
    }

    @Test
    void failsOnMissingOrUnknownOptions()
    {
        String sample = SampleNatives.class.getName();

        assertThrows(IllegalArgumentException.class, () -> NiagaraNativeBindingsGenerator.main(new String[] {sample}));
        assertThrows(IllegalArgumentException.class, () -> NiagaraNativeBindingsGenerator.main(new String[] {"--backend=nope", sample}));
        assertThrows(IllegalArgumentException.class, () -> NiagaraNativeBindingsGenerator.main(new String[] {"--backend=ffm", sample}));
        assertThrows(IllegalArgumentException.class, () -> NiagaraNativeBindingsGenerator.main(new String[] {"--backend", sample}));
    }
}
