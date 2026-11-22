package me.ayydxn.niagara.ffm;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public final class FFMNativeSupport
{
    private static final Linker LINKER = Linker.nativeLinker();

    private FFMNativeSupport()
    {
    }

    public static MethodHandle downcall(String function, FunctionDescriptor descriptor)
    {
        MemorySegment symbol = FFMNativeLibraryProvider.getSymbolLookup().find(function)
                .orElseThrow(() -> new IllegalStateException("Couldn't find " + function + " in Niagara's native libraries"));

        return LINKER.downcallHandle(symbol, descriptor);
    }

    public static String readNativeString(MemorySegment pointer)
    {
        if (pointer.address() == 0)
            return null;

        return pointer.reinterpret(Long.MAX_VALUE).getString(0);
    }

    public static String readULString(MemorySegment ulString)
    {
        if (ulString.address() == 0)
            return null;

        try
        {
            String data = readNativeString((MemorySegment) ULStringFunctions.GET_DATA.invokeExact(ulString));

            return data == null ? "" : data;
        }
        catch (Throwable throwable)
        {
            throw failure("ulStringGetData", throwable);
        }
    }

    public static void destroyULString(MemorySegment ulString)
    {
        if (ulString.address() == 0)
            return;

        try
        {
            ULStringFunctions.DESTROY.invokeExact(ulString);
        }
        catch (Throwable throwable)
        {
            throw failure("ulDestroyString", throwable);
        }
    }

    public static RuntimeException failure(String function, Throwable throwable)
    {
        if (throwable instanceof Error error)
            throw error;

        if (throwable instanceof RuntimeException exception)
            return exception;

        return new IllegalStateException("Calling " + function + " failed", throwable);
    }

    private static final class ULStringFunctions
    {
        private static final MethodHandle GET_DATA = downcall("ulStringGetData", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        private static final MethodHandle DESTROY = downcall("ulDestroyString", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    }
}