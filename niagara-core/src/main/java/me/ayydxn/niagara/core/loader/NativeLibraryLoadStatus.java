package me.ayydxn.niagara.core.loader;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record NativeLibraryLoadStatus(@NotNull LoadingState loadingState, FailureReason failureReason)
{
    public NativeLibraryLoadStatus
    {
        Objects.requireNonNull(loadingState);

        if (loadingState == LoadingState.FAILED && failureReason == null)
            throw new IllegalArgumentException("A failed native library load status must have a failure reason!");

        if (loadingState != LoadingState.FAILED && failureReason != null)
            throw new IllegalArgumentException("Only a failed native library load status can have a failure reason!");
    }

    public static NativeLibraryLoadStatus notLoaded()
    {
        return new NativeLibraryLoadStatus(LoadingState.NOT_LOADED, null);
    }

    public static NativeLibraryLoadStatus loaded()
    {
        return new NativeLibraryLoadStatus(LoadingState.LOADED, null);
    }

    public static NativeLibraryLoadStatus failed(FailureReason failureReason)
    {
        return new NativeLibraryLoadStatus(LoadingState.FAILED, Objects.requireNonNull(failureReason, "failureReason"));
    }

    public boolean isLoaded()
    {
        return this.loadingState == LoadingState.LOADED;
    }

    public boolean isFailed()
    {
        return this.loadingState == LoadingState.FAILED;
    }

    public enum LoadingState
    {
        NOT_LOADED,
        LOADED,
        FAILED
    }

    public enum FailureReason
    {
        /** The operating system and architecture combination isn't one Niagara ships support for. */
        UNSUPPORTED_PLATFORM,

        /** No library directory was passed in code, and the system property wasn't set. */
        DIRECTORY_NOT_CONFIGURED,

        /** The library directory doesn't exist, isn't a directory, or isn't a valid path. */
        DIRECTORY_INVALID,

        /** The library directory exists, but one or more of the required Ultralight libraries aren't in it. */
        LIBRARY_FILE_MISSING,

        /** The JNI bridge library isn't embedded in the {@code niagara-jni} jar for this platform. */
        BRIDGE_MISSING,

        /** The operating system refused to load one of the libraries. */
        NATIVE_LOAD_FAILED,

        /** No native backend ({@code niagara-jni} or {@code niagara-ffm}) is available at runtime. */
        BACKEND_UNAVAILABLE,

        UNKNOWN
    }
}
