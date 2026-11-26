package me.ayydxn.niagara.core.natives;

import me.ayydxn.niagara.core.annotations.NativeFunction;
import me.ayydxn.niagara.core.annotations.NativeInterface;
import me.ayydxn.niagara.core.annotations.NativePointer;

@NativeInterface(headers = { "AppCore/CAPI.h" })
public interface ULPlatformNatives
{
    @NativeFunction("ulEnablePlatformClipboard")
    void enablePlatformClipboard();

    @NativeFunction("ulEnablePlatformFileSystem")
    void enablePlatformFileSystem(@NativePointer("ULString") long baseDirectoryStringHandle);

    @NativeFunction("ulEnablePlatformFontLoader")
    void enablePlatformFontLoader();
}
