package me.ayydxn.niagara.jni;

import me.ayydxn.niagara.core.backend.NativeBackend;
import me.ayydxn.niagara.core.backend.NativeBackendType;
import me.ayydxn.niagara.core.natives.ULConfigNatives;
import me.ayydxn.niagara.core.natives.ULStringNatives;
import me.ayydxn.niagara.core.natives.UltralightNatives;
import me.ayydxn.niagara.jni.generated.JNIULConfigNatives;
import me.ayydxn.niagara.jni.generated.JNIULStringNatives;
import me.ayydxn.niagara.jni.generated.JNIUltralightNatives;
import org.jetbrains.annotations.NotNull;

public final class JNINativeBackend implements NativeBackend
{
    private final JNIUltralightNatives ultralight;
    private final JNIULConfigNatives ulConfig;
    private final JNIULStringNatives ulString;

    public JNINativeBackend()
    {
        this.ultralight = new JNIUltralightNatives();
        this.ulConfig = new JNIULConfigNatives();
        this.ulString = new JNIULStringNatives();
    }

    @Override
    public @NotNull UltralightNatives ultralight()
    {
        return this.ultralight;
    }

    @Override
    public @NotNull ULConfigNatives ulConfig()
    {
        return this.ulConfig;
    }

    @Override
    public @NotNull ULStringNatives ulString()
    {
        return this.ulString;
    }

    @Override
    public @NotNull NativeBackendType getType()
    {
        return NativeBackendType.JNI;
    }
}
