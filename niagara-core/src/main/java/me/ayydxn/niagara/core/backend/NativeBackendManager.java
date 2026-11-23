package me.ayydxn.niagara.core.backend;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class NativeBackendManager
{
    private static volatile NativeBackend installedBackend;

    private NativeBackendManager()
    {
    }

    public static void install(@NotNull NativeBackend nativeBackend)
    {
        installedBackend = Objects.requireNonNull(nativeBackend, "A null NativeBackend instance cannot be installed!");
    }

    public static void uninstall()
    {
        installedBackend = null;
    }

    @NotNull
    public static NativeBackend getCurrentBackendInstance()
    {
        NativeBackend nativeBackend = installedBackend;
        if (nativeBackend == null)
            throw new IllegalStateException("Tried to get Niagara's current native backend before the native libraries were loaded! Call Niagara.load() first.");

        return nativeBackend;
    }
}
