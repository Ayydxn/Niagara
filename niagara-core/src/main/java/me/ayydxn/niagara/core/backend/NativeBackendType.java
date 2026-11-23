package me.ayydxn.niagara.core.backend;

import java.util.Optional;

public enum NativeBackendType
{
    JNI,
    FFM;

    public static final String BACKEND_PROPERTY = "niagara.nativeBackend";

    public static Optional<NativeBackendType> fromName(String name)
    {
        if (name == null)
            return Optional.empty();

        String trimmedName = name.trim();

        for (NativeBackendType nativeBackendType : NativeBackendType.values())
        {
            if (nativeBackendType.name().equalsIgnoreCase(trimmedName))
                return Optional.of(nativeBackendType);
        }

        return Optional.empty();
    }
}
