package me.ayydxn.niagara.codegen.jni;

import me.ayydxn.niagara.codegen.GoldenFiles;
import me.ayydxn.niagara.codegen.NativeInterfaceModel;
import me.ayydxn.niagara.codegen.NativeInterfaceReader;
import me.ayydxn.niagara.codegen.fixtures.InvalidNativeInterfaces;
import me.ayydxn.niagara.codegen.fixtures.SampleNatives;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JNINativeBindingsEmitterTest
{
    private static final String JAVA_PACKAGE = "me.ayydxn.niagara.jni.generated";

    private final JNINativeBindingsEmitter nativeBindingsEmitter = new JNINativeBindingsEmitter(JAVA_PACKAGE);

    @Test
    void javaOutputMatchesGoldenFiles() throws IOException
    {
        GoldenFiles.assertMatch("jni", nativeBindingsEmitter.javaFiles(List.of(NativeInterfaceReader.read(SampleNatives.class))));
    }

    @Test
    void cppOutputMatchesGoldenFiles() throws IOException
    {
        GoldenFiles.assertMatch("jni", nativeBindingsEmitter.cppFiles(List.of(NativeInterfaceReader.read(SampleNatives.class))));
    }

    @Test
    void rejectsStringParametersItWouldHaveToGenerate()
    {
        NativeInterfaceModel.Contract contract = NativeInterfaceReader.read(InvalidNativeInterfaces.StringParameterOnlyFFMCanGenerateNatives.class);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> nativeBindingsEmitter.cppFiles(List.of(contract)));
        assertTrue(exception.getMessage().contains("can't generate String parameters"), exception.getMessage());
    }

    @Test
    void rejectsFunctionsThatDontStartWithUl()
    {
        NativeInterfaceModel.Contract contract = NativeInterfaceReader.read(InvalidNativeInterfaces.NotAnUltralightFunctionNatives.class);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> nativeBindingsEmitter.cppFiles(List.of(contract)));
        assertTrue(exception.getMessage().contains("starting with \"ul\""), exception.getMessage());
    }
}
