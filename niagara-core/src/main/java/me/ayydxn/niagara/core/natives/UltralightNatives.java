package me.ayydxn.niagara.core.natives;

import me.ayydxn.niagara.core.annotations.NativeFunction;
import me.ayydxn.niagara.core.annotations.NativeInterface;
import me.ayydxn.niagara.core.annotations.NativeString;

@NativeInterface(headers = { "Ultralight/CAPI/CAPI_Defines.h" })
public interface UltralightNatives
{
    @NativeString
    @NativeFunction("ulVersionString")
    String versionString();

    @NativeFunction("ulVersionMajor")
    int versionMajor();

    @NativeFunction("ulVersionMinor")
    int versionMinor();

    @NativeFunction("ulVersionPatch")
    int versionPatch();

    @NativeString
    @NativeFunction("ulWebKitVersionString")
    String webKitVersionString();

    @NativeFunction("ulWebKitVersionMajor")
    int webKitVersionMajor();

    @NativeFunction("ulWebKitVersionMinor")
    int webKitVersionMinor();

    @NativeFunction("ulWebKitVersionTiny")
    int webKitVersionTiny();

    @NativeFunction("ulWebKitVersionMicro")
    int webKitVersionMicro();

    @NativeFunction("ulWebKitVersionNano")
    int webKitVersionNano();
}
