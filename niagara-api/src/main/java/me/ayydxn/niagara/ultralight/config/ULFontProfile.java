package me.ayydxn.niagara.ultralight.config;

import me.ayydxn.niagara.core.natives.NativeEnum;

public enum ULFontProfile implements NativeEnum
{
    DEFAULT(0),
    MACOS_LIKE(1),
    WINDOWS_LIKE(2),
    LIGHT(3);

    private final int nativeValue;

    ULFontProfile(int nativeValue)
    {
        this.nativeValue = nativeValue;
    }

    @Override
    public int getNativeValue()
    {
        return this.nativeValue;
    }
}
