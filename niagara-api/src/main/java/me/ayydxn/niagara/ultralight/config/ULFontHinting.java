package me.ayydxn.niagara.ultralight.config;

import me.ayydxn.niagara.core.natives.NativeEnum;

public enum ULFontHinting implements NativeEnum
{
    SMOOTH(0),
    NORMAL(1),
    MONOCHROME(2),
    NONE(3);

    private final int nativeValue;

    ULFontHinting(int nativeValue)
    {
        this.nativeValue = nativeValue;
    }

    @Override
    public int getNativeValue()
    {
        return this.nativeValue;
    }
}
