package me.ayydxn.niagara.ultralight.config;

import me.ayydxn.niagara.core.natives.NativeEnum;

public enum ULLogLevel implements NativeEnum
{
    FATAL(0),
    ERROR(1),
    WARNING(2),
    INFO(3),
    DEBUG(4);

    private final int nativeValue;

    ULLogLevel(int nativeValue)
    {
        this.nativeValue = nativeValue;
    }

    @Override
    public int getNativeValue()
    {
        return this.nativeValue;
    }
}
