package me.ayydxn.niagara.ffm;

import me.ayydxn.niagara.core.backend.NativeBackend;
import me.ayydxn.niagara.core.backend.NativeBackendType;
import me.ayydxn.niagara.core.natives.ULConfigNatives;
import me.ayydxn.niagara.core.natives.ULStringNatives;
import me.ayydxn.niagara.core.natives.UltralightNatives;
import me.ayydxn.niagara.ffm.generated.FFMULConfigNatives;
import me.ayydxn.niagara.ffm.generated.FFMUltralightNatives;
import me.ayydxn.niagara.ffm.ultralight.FFMULStringNatives;
import org.jetbrains.annotations.NotNull;

public final class FFMNativeBackend implements NativeBackend
{
    private final FFMUltralightNatives ultralight;
    private final FFMULConfigNatives ulConfig;
    private final FFMULStringNatives ulString;

    public FFMNativeBackend()
    {
        this.ultralight = new FFMUltralightNatives();
        this.ulConfig = new FFMULConfigNatives();
        this.ulString = new FFMULStringNatives();
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
        return NativeBackendType.FFM;
    }
}
