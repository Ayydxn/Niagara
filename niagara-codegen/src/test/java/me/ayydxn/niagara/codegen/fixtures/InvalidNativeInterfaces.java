package me.ayydxn.niagara.codegen.fixtures;

import me.ayydxn.niagara.core.annotations.*;
import me.ayydxn.niagara.core.backend.NativeBackendType;

/**
 * Contracts the generator has to reject. Each one breaks exactly one rule.
 */
public final class InvalidNativeInterfaces
{
    private InvalidNativeInterfaces()
    {
    }

    @NativeInterface(headers = {})
    public static class NotAnInterfaceNatives
    {
    }

    @NativeInterface(headers = {})
    public interface WrongName
    {
    }

    @NativeInterface(headers = {})
    public interface PointerOnIntNatives
    {
        @NativeFunction("ulA")
        void a(@NativePointer("ULConfig") int handle);
    }

    @NativeInterface(headers = {})
    public interface BareStringReturnNatives
    {
        @NativeFunction("ulA")
        String a();
    }

    @NativeInterface(headers = {})
    public interface BareStringParameterNatives
    {
        @NativeFunction("ulA")
        void a(String value);
    }

    // Handwritten for JNI only, so the FFM backend would still have to marshal the string.
    @NativeInterface(headers = {})
    public interface BareStringParameterCustomForJNINatives
    {
        @NativeFunction(value = "ulA", custom = NativeBackendType.JNI)
        void a(String value);
    }

    @NativeInterface(headers = {})
    public interface BothStringKindsNatives
    {
        @NativeString
        @UltralightString
        @NativeFunction("ulA")
        String a();
    }

    @NativeInterface(headers = {})
    public interface StringAnnotationOnLongNatives
    {
        @NativeString
        @NativeFunction("ulA")
        long a();
    }

    @NativeInterface(headers = {})
    public interface UnsupportedTypeNatives
    {
        @NativeFunction("ulA")
        double a();
    }

    // Fine as a contract, but the JNI backend can't generate a String parameter.
    @NativeInterface(headers = {})
    public interface StringParameterOnlyFFMCanGenerateNatives
    {
        @NativeFunction(value = "ulA", custom = NativeBackendType.FFM)
        void a(@NativeString String value);
    }

    @NativeInterface(headers = {})
    public interface NotAnUltralightFunctionNatives
    {
        @NativeFunction("someFunction")
        void a();
    }
}
