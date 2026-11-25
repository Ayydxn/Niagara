package me.ayydxn.niagara.ffm.ultralight;

import me.ayydxn.niagara.ffm.FFMNativeSupport;
import me.ayydxn.niagara.ffm.generated.AbstractFFMULStringNatives;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class FFMULStringNatives extends AbstractFFMULStringNatives
{
    private final MethodHandle ulCreateString;
    private final MethodHandle ulCreateStringUTF8;
    private final MethodHandle ulStringAssignCString;

    public FFMULStringNatives()
    {
        this.ulCreateString = FFMNativeSupport.downcall("ulCreateString", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));

        this.ulCreateStringUTF8 = FFMNativeSupport.downcall("ulCreateStringUTF8", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS,
            FFMNativeSupport.SIZE_T));

        this.ulStringAssignCString = FFMNativeSupport.downcall("ulStringAssignCString", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    }

    @Override
    public long create(String string)
    {
        byte[] stringUTF8Bytes = Objects.requireNonNull(string, "string").getBytes(StandardCharsets.UTF_8);

        try (Arena arena = Arena.ofConfined())
        {
            MemorySegment nativeString = FFMNativeSupport.allocateCString(arena, stringUTF8Bytes);

            return ((MemorySegment) this.ulCreateString.invokeExact(nativeString)).address();
        }
        catch (Throwable throwable)
        {
            throw FFMNativeSupport.failure("ulCreateString", throwable);
        }
    }

    @Override
    public long createUTF8(String string, long length)
    {
        byte[] stringUTF8Bytes = Objects.requireNonNull(string, "string").getBytes(StandardCharsets.UTF_8);
        if (length < 0 || length > stringUTF8Bytes.length)
            throw new IllegalArgumentException("length (" + length + ") must be between 0 and the UTF-8 size of the string (" + stringUTF8Bytes.length + ")");

        try (Arena arena = Arena.ofConfined())
        {
            MemorySegment nativeBytes = FFMNativeSupport.allocateCString(arena, stringUTF8Bytes);

            return ((MemorySegment) this.ulCreateStringUTF8.invokeExact(nativeBytes, length)).address();
        }
        catch (Throwable throwable)
        {
            throw FFMNativeSupport.failure("ulCreateStringUTF8", throwable);
        }
    }

    @Override
    public String getData(long handle)
    {
        return FFMNativeSupport.readULString(MemorySegment.ofAddress(handle));
    }

    @Override
    public void assignCString(long string, String newString)
    {
        if (string == 0)
            throw new IllegalArgumentException("The string handle must not be 0");

        byte[] stringUTF8Bytes = Objects.requireNonNull(newString, "newString").getBytes(StandardCharsets.UTF_8);

        try (Arena arena = Arena.ofConfined())
        {
            MemorySegment nativeString = FFMNativeSupport.allocateCString(arena, stringUTF8Bytes);

            this.ulStringAssignCString.invokeExact(MemorySegment.ofAddress(string), nativeString);
        }
        catch (Throwable throwable)
        {
            throw FFMNativeSupport.failure("ulStringAssignCString", throwable);
        }
    }
}
