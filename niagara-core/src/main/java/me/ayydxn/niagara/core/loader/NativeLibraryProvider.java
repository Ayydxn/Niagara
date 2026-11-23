package me.ayydxn.niagara.core.loader;

import me.ayydxn.niagara.core.backend.NativeBackend;
import me.ayydxn.niagara.core.backend.NativeBackendType;

public interface NativeLibraryProvider
{
    NativeBackend createNativeBackend();

    void load(NativeLibraryPlatform nativeLibraryPlatform, NativeLibraries nativeLibraries);

    NativeBackendType getType();
}
