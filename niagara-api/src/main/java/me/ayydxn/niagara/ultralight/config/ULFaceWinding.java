package me.ayydxn.niagara.ultralight.config;

import me.ayydxn.niagara.core.natives.NativeEnum;

public enum ULFaceWinding implements NativeEnum
{
    CLOCKWISE(0),
    COUNTER_CLOCKWISE(1);

    private final int nativeValue;

    ULFaceWinding(int nativeValue)
    {
        this.nativeValue = nativeValue;
    }

    @Override
    public int getNativeValue()
    {
        return this.nativeValue;
    }
}
