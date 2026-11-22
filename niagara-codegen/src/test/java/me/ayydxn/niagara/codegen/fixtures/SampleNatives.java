package me.ayydxn.niagara.codegen.fixtures;

import me.ayydxn.niagara.core.annotations.*;
import me.ayydxn.niagara.core.backend.NativeBackendType;

/**
 * Touches every feature the generator supports, so its output is covered by the golden files.
 */
@NativeInterface(headers = {"Ultralight/CAPI/CAPI_Config.h", "Ultralight/CAPI/CAPI_String.h"})
public interface SampleNatives
{
    @NativePointer("ULConfig")
    @NativeFunction("ulCreateConfig")
    long create();

    @NativeFunction("ulDestroyConfig")
    void destroy(@NativePointer("ULConfig") long handle);

    @NativeFunction("ulConfigSetBitmapAlignment")
    void setBitmapAlignment(@NativePointer("ULConfig") long handle, int bitmapRowAlignment);

    @NativeFunction("ulConfigSetEffectQuality")
    void setEffectQuality(@NativePointer("ULConfig") long handle, @NativeType("ULEffectQuality") int effectQualityNativeValue);

    @NativeFunction("ulConfigSetDeveloperMode")
    void setDeveloperMode(@NativePointer("ULConfig") long handle, boolean enableDeveloperMode);

    @NativeFunction("ulConfigGetBitmapAlignment")
    int getBitmapAlignment(@NativePointer("ULConfig") long handle);

    @NativePointer
    @NativeFunction("ulConfigLockBuffer")
    long lockBuffer(@NativePointer("ULConfig") long handle);

    @NativeString
    @NativeFunction("ulVersionString")
    String versionString();

    @UltralightString
    @NativeFunction("ulConfigGetTitle")
    String getTitle(@NativePointer("ULConfig") long handle);

    @UltralightString(owned = true)
    @NativeFunction("ulConfigTakeTitle")
    String takeTitle(@NativePointer("ULConfig") long handle);

    // The JNI backend has a handwritten body for this one, so the FFM backend still has to generate it.
    @NativePointer("ULString")
    @NativeFunction(value = "ulCreateString", custom = NativeBackendType.JNI)
    long createString(@NativeString String string);

    // Handwritten everywhere, which is the only case where a String doesn't need an annotation.
    @NativeFunction(value = "ulStringGetData", custom = {NativeBackendType.JNI, NativeBackendType.FFM})
    String getData(@NativePointer("ULString") long handle);
}
