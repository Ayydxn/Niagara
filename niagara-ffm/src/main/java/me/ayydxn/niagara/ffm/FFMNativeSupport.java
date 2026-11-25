package me.ayydxn.niagara.ffm;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

public final class FFMNativeSupport
{
    private static final Linker LINKER = Linker.nativeLinker();

    public static final MemoryLayout SIZE_T = LINKER.canonicalLayouts().get("size_t");

    private static volatile MethodHandle ulStringGetData;
    private static volatile MethodHandle ulStringGetLength;

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
            MemorySegment data = (MemorySegment) FFMNativeSupport.ulStringGetData().invokeExact(ulString);
            long length = (long) FFMNativeSupport.ulStringGetLength().invokeExact(ulString);

            return FFMNativeSupport.readUTF8(data, length);
        }
        catch (Throwable throwable)
        {
            throw failure("ulStringGetData", throwable);
        }
    }

    /**
     * Reads exactly {@code length} bytes of UTF-8. Malformed bytes become U+FFFD.
     */
    public static String readUTF8(MemorySegment pointer, long length)
    {
        if (pointer.address() == 0 || length == 0)
            return "";

        byte[] bytes = pointer.reinterpret(length).toArray(ValueLayout.JAVA_BYTE);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Copies UTF-8 bytes into native memory followed by a NUL terminator. It's always at least one byte, so the pointer is never null.
     */
    public static MemorySegment allocateCString(SegmentAllocator allocator, byte[] utf8Bytes)
    {
        MemorySegment nativeString = allocator.allocate(utf8Bytes.length + 1L);

        MemorySegment.copy(utf8Bytes, 0, nativeString, ValueLayout.JAVA_BYTE, 0, utf8Bytes.length);
        nativeString.set(ValueLayout.JAVA_BYTE, utf8Bytes.length, (byte) 0);

        return nativeString;
    }

    public static RuntimeException failure(String function, Throwable throwable)
    {
        if (throwable instanceof Error error)
            throw error;

        if (throwable instanceof RuntimeException exception)
            return exception;

        return new IllegalStateException("Calling " + function + " failed", throwable);
    }

    private static MethodHandle ulStringGetData()
    {
        MethodHandle handle = ulStringGetData;
        if (handle == null)
        {
            handle = downcall("ulStringGetData", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
            ulStringGetData = handle;
        }

        return handle;
    }

    private static MethodHandle ulStringGetLength()
    {
        MethodHandle handle = ulStringGetLength;
        if (handle == null)
        {
            handle = downcall("ulStringGetLength", FunctionDescriptor.of(SIZE_T, ValueLayout.ADDRESS));
            ulStringGetLength = handle;
        }

        return handle;
    }
}
