package me.ayydxn.niagara.ultralight;

import me.ayydxn.niagara.core.backend.NativeBackendManager;
import me.ayydxn.niagara.core.natives.UltralightNatives;

public final class Ultralight
{
    private static final UltralightNatives NATIVES = NativeBackendManager.getCurrentBackendInstance().ultralight();

    public static String versionString()
    {
        return NATIVES.versionString();
    }

    public static int versionMajor()
    {
        return NATIVES.versionMajor();
    }

    public static int versionMinor()
    {
        return NATIVES.versionMinor();
    }

    public static int versionPatch()
    {
        return NATIVES.versionPatch();
    }

    public static String webKitVersionString()
    {
        return NATIVES.webKitVersionString();
    }

    public static int webKitVersionMajor()
    {
        return NATIVES.webKitVersionMajor();
    }

    public static int webKitVersionMinor()
    {
        return NATIVES.webKitVersionMinor();
    }

    public static int webKitVersionTiny()
    {
        return NATIVES.webKitVersionTiny();
    }

    public static int webKitVersionMicro()
    {
        return NATIVES.webKitVersionMicro();
    }

    public static int webKitVersionNano()
    {
        return NATIVES.webKitVersionNano();
    }
}
