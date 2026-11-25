package me.ayydxn.niagara.ultralight.config;

import me.ayydxn.niagara.core.natives.NativeEnum;

public enum ULMemoryProfile implements NativeEnum
{
    BALANCED(0),
    LOW_MEMORY(1),
    PERFORMANCE(2);

    private final int nativeValue;

    ULMemoryProfile(int nativeValue)
    {
        this.nativeValue = nativeValue;
    }

    @Override
    public int getNativeValue()
    {
        return this.nativeValue;
    }
}
