package me.ayydxn.niagara.jni;

import me.ayydxn.niagara.core.backend.NativeBackend;
import me.ayydxn.niagara.core.backend.NativeBackendType;
import me.ayydxn.niagara.core.natives.UltralightNatives;
import me.ayydxn.niagara.jni.generated.JNIUltralightNatives;
import org.jetbrains.annotations.NotNull;

public final class JNINativeBackend implements NativeBackend
{
    private final JNIUltralightNatives ultralight;

    public JNINativeBackend()
    {
        this.ultralight = new JNIUltralightNatives();
    }

    @Override
    public @NotNull UltralightNatives ultralight()
    {
        return this.ultralight;
    }

    @Override
    public @NotNull NativeBackendType getType()
    {
        return NativeBackendType.JNI;
    }
}
