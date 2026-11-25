package me.ayydxn.niagara.ultralight.config;

import me.ayydxn.niagara.core.natives.NativeEnum;

public enum ULDiagnosticsLevel implements NativeEnum
{
    OFF(0),
    WARN(1),
    STRICT(2),
    AUTO(3);

    private final int nativeValue;

    ULDiagnosticsLevel(int nativeValue)
    {
        this.nativeValue = nativeValue;
    }

    @Override
    public int getNativeValue()
    {
        return this.nativeValue;
    }
}
