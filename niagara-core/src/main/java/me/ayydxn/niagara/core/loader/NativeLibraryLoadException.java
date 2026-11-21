package me.ayydxn.niagara.core.loader;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class NativeLibraryLoadException extends RuntimeException
{
    private final NativeLibraryLoadStatus.FailureReason failureReason;

    public NativeLibraryLoadException(@NotNull NativeLibraryLoadStatus.FailureReason failureReason, String message)
    {
        this(failureReason, message, null);
    }

    public NativeLibraryLoadException(@NotNull NativeLibraryLoadStatus.FailureReason failureReason, String message, Throwable cause)
    {
        super(message, cause);

        this.failureReason = Objects.requireNonNull(failureReason, message);
    }

    public NativeLibraryLoadStatus.FailureReason getFailureReason()
    {
        return this.failureReason;
    }
}
