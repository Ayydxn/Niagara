package me.ayydxn.niagara.core.loader;

import java.util.List;
import java.util.Locale;

public enum NativeLibraryPlatform
{
    WINDOWS_X64("windows-x64", "NiagaraJNIBridge.dll", "UltralightCore.dll", "WebCore.dll", "Ultralight.dll", "AppCore.dll"),
    MACOS_ARM64("macos-arm64", "libNiagaraJNIBridge.dylib", "libUltralightCore.dylib", "libWebCore.dylib", "libUltralight.dylib", "libAppCore.dylib"),
    LINUX_X64("linux-x64", "libNiagaraJNIBridge.so", "libUltralightCore.so", "libWebCore.so", "libUltralight.so", "libAppCore.so");

    private final String id;
    private final String jniBridgeLibrary;
    private final List<String> ultralightLibraries;

    NativeLibraryPlatform(String id, String jniBridgeLibrary, String... ultralightLibraries)
    {
        this.id = id;
        this.jniBridgeLibrary = jniBridgeLibrary;
        this.ultralightLibraries = List.of(ultralightLibraries);
    }

    public static NativeLibraryPlatform current()
    {
        return NativeLibraryPlatform.detect(System.getProperty("os.name"), System.getProperty("os.arch"));
    }

    public static NativeLibraryPlatform detect(String osName, String osArchitecture)
    {
        String name = osName == null ? "" : osName.toLowerCase(Locale.ROOT);
        String architecture = osArchitecture == null ? "" : osArchitecture.toLowerCase(Locale.ROOT);

        boolean isX64 = architecture.equals("amd64") || architecture.equals("x86_64");
        boolean isArm64 = architecture.equals("aarch64") || architecture.equals("arm64");

        if (name.startsWith("windows") && isX64)
            return WINDOWS_X64;

        if (name.contains("mac") && isArm64)
            return MACOS_ARM64;

        if (name.contains("linux") && isX64)
            return LINUX_X64;

        throw new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.UNSUPPORTED_PLATFORM,
                "Unsupported platform: " + osName + " (" + osArchitecture + "). Supported platforms are windows-x64, macos-arm64 and linux-x64.");
    }

    public String getID()
    {
        return this.id;
    }

    public String getJNIBridgeLibrary()
    {
        return this.jniBridgeLibrary;
    }

    public List<String> getUltralightLibraries()
    {
        return this.ultralightLibraries;
    }
}
