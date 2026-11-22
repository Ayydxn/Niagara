package me.ayydxn.niagara.codegen.ffm;

import me.ayydxn.niagara.codegen.GoldenFiles;
import me.ayydxn.niagara.codegen.NativeInterfaceModel.Contract;
import me.ayydxn.niagara.codegen.NativeInterfaceReader;
import me.ayydxn.niagara.codegen.fixtures.PlainNatives;
import me.ayydxn.niagara.codegen.fixtures.SampleNatives;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FFMNativeBindingsEmitterTest
{
    private static final String JAVA_PACKAGE = "me.ayydxn.niagara.ffm.generated";
    private static final String JAVA_DIRECTORY = "me/ayydxn/niagara/ffm/generated/";

    private final FFMNativeBindingsEmitter nativeBindingsEmitter = new FFMNativeBindingsEmitter(JAVA_PACKAGE);

    private final List<Contract> contracts = List.of(NativeInterfaceReader.read(SampleNatives.class), NativeInterfaceReader.read(PlainNatives.class));

    @Test
    void javaOutputMatchesGoldenFiles() throws IOException
    {
        GoldenFiles.assertMatch("ffm", this.nativeBindingsEmitter.javaFiles(this.contracts));
    }

    @Test
    void functionsHandwrittenForFfmMakeTheClassAbstract()
    {
        Map<String, String> files = this.nativeBindingsEmitter.javaFiles(this.contracts);

        // SampleNatives has one, so FFMSampleNatives is left for a handwritten subclass. PlainNatives have none.
        assertEquals(Set.of(JAVA_DIRECTORY + "AbstractFFMSampleNatives.java", JAVA_DIRECTORY + "FFMPlainNatives.java"), files.keySet());
    }

    @Test
    void leavesOutOnlyTheFunctionsHandwrittenForFfm()
    {
        String source = this.nativeBindingsEmitter.javaFiles(this.contracts).get(JAVA_DIRECTORY + "AbstractFFMSampleNatives.java");

        assertFalse(source.contains("getData"), "ulStringGetData is handwritten for FFM");
        assertTrue(source.contains("createString"), "ulCreateString is only handwritten for JNI");
    }
}