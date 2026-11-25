package me.ayydxn.niagara.ultralight.config;

import me.ayydxn.niagara.core.natives.NativeEnum;

public enum ULPixelFontRendering implements NativeEnum
{
    SMOOTH(0),
    PIXELATED(1);

    private final int nativeValue;

    ULPixelFontRendering(int nativeValue)
    {
        this.nativeValue = nativeValue;
    }

    @Override
    public int getNativeValue()
    {
        return this.nativeValue;
    }
}
