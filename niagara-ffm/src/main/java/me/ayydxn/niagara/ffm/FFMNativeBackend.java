package me.ayydxn.niagara.ffm;

import me.ayydxn.niagara.core.backend.NativeBackend;
import me.ayydxn.niagara.core.backend.NativeBackendType;
import me.ayydxn.niagara.core.natives.UltralightNatives;
import me.ayydxn.niagara.ffm.generated.FFMUltralightNatives;
import org.jetbrains.annotations.NotNull;

public final class FFMNativeBackend implements NativeBackend
{
    private final FFMUltralightNatives ultralight;

    public FFMNativeBackend()
    {
        this.ultralight = new FFMUltralightNatives();
    }

    @Override
    public @NotNull UltralightNatives ultralight()
    {
        return this.ultralight;
    }

    @Override
    public @NotNull NativeBackendType getType()
    {
        return NativeBackendType.FFM;
    }
}
