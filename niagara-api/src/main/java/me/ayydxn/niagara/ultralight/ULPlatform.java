package me.ayydxn.niagara.ultralight;

import me.ayydxn.niagara.core.backend.NativeBackendManager;
import me.ayydxn.niagara.core.natives.ULPlatformNatives;

public final class ULPlatform
{
    private static final ULPlatformNatives NATIVES = NativeBackendManager.getCurrentBackendInstance().ulPlatform();

    public static void enablePlatformClipboard()
    {
        NATIVES.enablePlatformClipboard();
    }

    public static void enablePlatformFileSystem(String baseDirectory)
    {
        try (ULString baseDirectoryString = ULString.create(baseDirectory))
        {
            NATIVES.enablePlatformFileSystem(baseDirectoryString.getHandle());
        }
    }

    public static void enablePlatformFontLoader()
    {
        NATIVES.enablePlatformFontLoader();
    }
}
