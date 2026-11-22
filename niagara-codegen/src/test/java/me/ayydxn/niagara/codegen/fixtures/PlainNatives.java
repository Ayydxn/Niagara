package me.ayydxn.niagara.codegen.fixtures;

import me.ayydxn.niagara.core.annotations.*;


@NativeInterface(headers = {"Ultralight/CAPI/CAPI_View.h", "Ultralight/CAPI/CAPI_String.h"})
public interface PlainNatives
{
    @NativePointer("ULViewConfig")
    @NativeFunction("ulCreateViewConfig")
    long create();

    @NativeFunction("ulDestroyViewConfig")
    void destroy(@NativePointer("ULViewConfig") long handle);

    @NativeFunction("ulViewConfigSetIsTransparent")
    void setIsTransparent(@NativePointer("ULViewConfig") long handle, boolean isTransparent);

    @NativeFunction("ulSurfaceGetSize")
    long getSize(@NativePointer("ULSurface") long handle);

    @NativeString
    @NativeFunction("ulVersionString")
    String versionString();

    @NativePointer("ULString")
    @NativeFunction("ulCreateString")
    long createString(@NativeString String string);
}