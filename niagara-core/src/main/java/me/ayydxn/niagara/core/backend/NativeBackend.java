package me.ayydxn.niagara.core.backend;

import me.ayydxn.niagara.core.natives.UltralightNatives;
import org.jetbrains.annotations.NotNull;

public interface NativeBackend
{
    @NotNull
    UltralightNatives ultralight();

    @NotNull
    NativeBackendType getType();
}
