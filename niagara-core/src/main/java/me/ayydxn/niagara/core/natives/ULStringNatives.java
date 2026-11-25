package me.ayydxn.niagara.core.natives;

import me.ayydxn.niagara.core.annotations.*;
import me.ayydxn.niagara.core.backend.NativeBackendType;

// TODO: (Ayydxn) For some unknown reason, ulCreateStringUTF16, despite being present in the CAPI headers and the docs, is not present in the Ultralight libraries.
//  To confirm this yourself, use something like Windows' DUMPBIN tool.
//  So, in the future, once it's actually there, come back and bind it.
@NativeInterface(headers = { "Ultralight/CAPI/CAPI_String.h" })
public interface ULStringNatives
{
    @NativePointer("ULString")
    @NativeFunction(value = "ulCreateString", custom = { NativeBackendType.JNI, NativeBackendType.FFM })
    long create(String string);

    @NativePointer("ULString")
    @NativeFunction(value = "ulCreateStringUTF8", custom = { NativeBackendType.JNI, NativeBackendType.FFM })
    long createUTF8(String string, @NativeType("size_t") long length);

    @NativePointer("ULString")
    @NativeFunction("ulCreateStringFromCopy")
    long createFromCopy(@NativePointer("ULString") long handle);

    @NativeFunction("ulDestroyString")
    void destroy(@NativePointer("ULString") long handle);

    @NativeFunction(value = "ulStringGetData", custom = { NativeBackendType.JNI, NativeBackendType.FFM })
    String getData(@NativePointer("ULString") long handle);

    @NativeFunction("ulStringGetLength")
    long getLength(@NativePointer("ULString") long handle);

    @NativeFunction("ulStringIsEmpty")
    boolean isEmpty(@NativePointer("ULString") long handle);

    @NativeFunction("ulStringAssignString")
    void assignString(@NativePointer("ULString") long string, @NativePointer("ULString") long newString);

    @NativeFunction(value = "ulStringAssignCString", custom = { NativeBackendType.JNI, NativeBackendType.FFM })
    void assignCString(@NativePointer("ULString") long string, String newString);
}
