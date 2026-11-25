package me.ayydxn.niagara.ultralight;

import me.ayydxn.niagara.core.backend.NativeBackendManager;
import me.ayydxn.niagara.core.natives.ULStringNatives;

public final class ULString implements AutoCloseable
{
    private static final ULStringNatives NATIVES = NativeBackendManager.getCurrentBackendInstance().ulString();

    private long handle;

    private ULString(long handle)
    {
        this.handle = handle;
    }

    public static ULString create(String string)
    {
        if (string == null)
            throw new IllegalArgumentException("String cannot be null");

        return new ULString(NATIVES.create(string));
    }

    public static ULString createUTF8(String string, long length)
    {
        // length gets validated by the JNI and FFM backends
        if (string == null)
            throw new IllegalArgumentException("String cannot be null");

        return new ULString(NATIVES.createUTF8(string, length));
    }

    public static ULString createFromCopy(ULString other)
    {
        if (other.handle == 0)
            throw new IllegalArgumentException("String cannot be null");

        return new ULString(NATIVES.createFromCopy(other.handle));
    }

    @Override
    public void close()
    {
        if (this.handle != 0)
        {
            NATIVES.destroy(this.handle);

            this.handle = 0;
        }
    }

    public void assign(ULString newString)
    {
        if (newString.handle == 0)
            throw new IllegalArgumentException("String cannot be null");

        NATIVES.assignString(this.handle, newString.handle);
    }

    public void assignCString(String newString)
    {
        if (newString == null)
            throw new IllegalArgumentException("String cannot be null");

        NATIVES.assignCString(this.handle, newString);
    }

    public String getData()
    {
        return NATIVES.getData(this.handle);
    }

    public long getLength()
    {
        return NATIVES.getLength(this.handle);
    }

    long getHandle()
    {
        return this.handle;
    }
}
