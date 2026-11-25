package me.ayydxn.niagara.ultralight.config;

import me.ayydxn.niagara.core.natives.NativeEnum;

public enum ULEffectQuality implements NativeEnum
{
    LOW(0),
    MEDIUM(1),
    HIGH(3);

    private final int nativeValue;

    ULEffectQuality(int nativeValue)
    {
        this.nativeValue = nativeValue;
    }

    @Override
    public int getNativeValue()
    {
        return this.nativeValue;
    }
}
