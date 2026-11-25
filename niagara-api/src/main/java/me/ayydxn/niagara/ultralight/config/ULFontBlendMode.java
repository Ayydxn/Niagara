package me.ayydxn.niagara.ultralight.config;

import me.ayydxn.niagara.core.natives.NativeEnum;

public enum ULFontBlendMode implements NativeEnum
{
    PROFILE(0),
    GAMMA_CORRECTED(1),
    SRGB(3);

    private final int nativeValue;

    ULFontBlendMode(int nativeValue)
    {
        this.nativeValue = nativeValue;
    }

    @Override
    public int getNativeValue()
    {
        return this.nativeValue;
    }
}
