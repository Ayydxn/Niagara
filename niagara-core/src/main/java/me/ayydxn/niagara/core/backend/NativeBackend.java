package me.ayydxn.niagara.core.backend;

import me.ayydxn.niagara.core.natives.ULConfigNatives;
import me.ayydxn.niagara.core.natives.ULStringNatives;
import me.ayydxn.niagara.core.natives.UltralightNatives;
import org.jetbrains.annotations.NotNull;

public interface NativeBackend
{
    @NotNull
    UltralightNatives ultralight();

    @NotNull
    ULConfigNatives ulConfig();

    @NotNull
    ULStringNatives ulString();

    @NotNull
    NativeBackendType getType();
}
