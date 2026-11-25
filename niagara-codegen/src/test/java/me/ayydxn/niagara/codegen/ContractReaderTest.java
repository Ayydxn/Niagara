package me.ayydxn.niagara.codegen;

import me.ayydxn.niagara.codegen.fixtures.InvalidNativeInterfaces;
import me.ayydxn.niagara.codegen.fixtures.SampleNatives;
import me.ayydxn.niagara.core.backend.NativeBackendType;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContractReaderTest
{
    @Test
    void readsFunctionsSortedByCFunctionName()
    {
        NativeInterfaceModel.Contract contract = NativeInterfaceReader.read(SampleNatives.class);

        List<String> cNames = contract.functions().stream().map(NativeInterfaceModel.Function::cName).toList();

        assertEquals(cNames.stream().sorted().toList(), cNames);
        assertEquals(12, cNames.size());
        assertEquals("SampleNatives", contract.name());
        assertEquals("Sample", contract.baseName());
        assertEquals(List.of("Ultralight/CAPI/CAPI_Config.h", "Ultralight/CAPI/CAPI_String.h"), contract.headers());
    }

    @Test
    void readsCustomBackends()
    {
        assertEquals(EnumSet.noneOf(NativeBackendType.class), function("ulCreateConfig").custom());
        assertEquals(EnumSet.of(NativeBackendType.JNI), function("ulCreateString").custom());
        assertEquals(EnumSet.of(NativeBackendType.JNI, NativeBackendType.FFM), function("ulStringGetData").custom());
    }

    @Test
    void readsPointersCastsAndStrings()
    {
        assertEquals("ULConfig", function("ulCreateConfig").returnType().pointerType());
        assertEquals("void*", function("ulConfigLockBuffer").returnType().pointerType());
        assertEquals("ULEffectQuality", function("ulConfigSetEffectQuality").params().get(1).type().nativeType());
        assertTrue(function("ulVersionString").returnType().isNativeString());
        assertTrue(function("ulCreateString").params().get(0).type().isNativeString());
        assertTrue(function("ulConfigTakeTitle").returnType().ownedULString());
        assertFalse(function("ulConfigGetTitle").returnType().ownedULString());
        assertEquals(NativeInterfaceModel.Kind.BOOLEAN, function("ulConfigSetDeveloperMode").params().get(1).type().kind());
    }

    @Test
    void rejectsTypesThatAreNotContracts()
    {
        assertMessage("is missing @NativeInterface", Runnable.class);
        assertMessage("is not an interface", InvalidNativeInterfaces.NotAnInterfaceNatives.class);
        assertMessage("has to be named <Something>Natives", InvalidNativeInterfaces.WrongName.class);
    }

    @Test
    void rejectsBadAnnotations()
    {
        assertMessage("@NativePointer needs a long", InvalidNativeInterfaces.PointerOnIntNatives.class);
        assertMessage("can't be combined", InvalidNativeInterfaces.BothStringKindsNatives.class);
        assertMessage("need a String", InvalidNativeInterfaces.StringAnnotationOnLongNatives.class);
    }

    @Test
    void rejectsStringsNoBackendCanMarshal()
    {
        assertMessage("needs @NativeString or @UltralightString", InvalidNativeInterfaces.BareStringReturnNatives.class);
        assertMessage("needs @NativeString,", InvalidNativeInterfaces.BareStringParameterNatives.class);

        // Handwritten for JNI isn't enough: the FFM backend generates it.
        assertMessage("needs @NativeString,", InvalidNativeInterfaces.BareStringParameterCustomForJNINatives.class);
    }

    private static NativeInterfaceModel.Function function(String cName)
    {
        return NativeInterfaceReader.read(SampleNatives.class).functions().stream()
                .filter(f -> f.cName().equals(cName))
                .findFirst()
                .orElseThrow();
    }

    private static void assertMessage(String expected, Class<?> contract)
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> NativeInterfaceReader.read(contract));

        assertTrue(exception.getMessage().contains(expected), "Expected \"" + expected + "\" in: " + exception.getMessage());
    }
}
