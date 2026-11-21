package me.ayydxn.niagara.core.loader;

public interface NativeLibraryProvider
{
    void load(NativeLibraryPlatform nativeLibraryPlatform, NativeLibraries nativeLibraries);
}
