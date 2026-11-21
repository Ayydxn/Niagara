package me.ayydxn.niagara;

import me.ayydxn.niagara.core.loader.*;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * The entrypoint to Niagara.
 * <p>
 * Because Niagara doesn't come with the Ultralight libraries out of the box and requires you to supply them, you must first use this class to load your
 * sourced libraries before calling functions from the Ultralight API. This can be done by either by using {@link #load(Path)} and providing the directory
 * which the libraries or you can use {@link #load()} which gets that same path from a system property ({@value NativeLibraries#LIBRARIES_DIRECTORY_PROPERTY}).
 * <p>
 * This path you provide must hold all the Ultralight libraries for whichever platform you are targeting. They are as follows with any other files in that directory being ignored:
 * <ul>
 *     <li>Windows: {@code UltralightCore.dll}, {@code WebCore.dll}, {@code Ultralight.dll} and {@code AppCore.dll}</li>
 *     <li>macOS: {@code libUltralightCore.dylib}, {@code libWebCore.dylib}, {@code libUltralight.dylib} and {@code libAppCore.dylib}</li>
 *     <li>Linux: {@code libUltralightCore.so}, {@code libWebCore.so}, {@code libUltralight.so} and {@code libAppCore.so}</li>
 * </ul>
 *
 * Example Usage:
 * <pre>{@code
 *     public static void main(String[] args) {
 *         // Explicity provide a directory on disk
 *         Niagara.load(Path.of("C:\Libraries"));
 *
 *         // Rely the path from the system property
 *         Niagara.load();
 *     }
 * }</pre>
 */
public final class Niagara
{
    private static volatile NativeLibraryLoadStatus loadStatus = NativeLibraryLoadStatus.notLoaded();
    private static Path loadedDirectory;

    private Niagara()
    {
    }

    /**
     * Loads the native libraries needed by Niagara, using the Ultralight libraries in {@code libraryDirectory}.
     * <p>
     * This blocks until loading finishes and is safe to call from any thread. Calling it again after a successful load with the same directory does nothing.
     * After a failed load, it can be called again.
     *
     * @param libraryDirectory            The directory that contains all the Ultralight libraries
     *
     * @throws NativeLibraryLoadException If loading fails. The reason is also available from {@link #getLoadStatus()}.
     * @throws IllegalStateException      If the libraries were already loaded from a different directory, since native libraries can't be unloaded
     * @throws NullPointerException       If {@code libraryDirectory} is null
     */
    public static void load(@NotNull Path libraryDirectory)
    {
        Objects.requireNonNull(libraryDirectory, "The library directory must not be null!");

        Niagara.loadWith(libraryDirectory, NativeLibraryPlatform::current, Niagara::discoverProvider);
    }

    /**
     * Loads Niagara's native libraries, using the Ultralight libraries in the directory named by the {@value NativeLibraries#LIBRARIES_DIRECTORY_PROPERTY} system property.
     * <p>
     * Behaves like {@link #load(Path)} in every other way. If the libraries were already loaded and the property isn't set, this does nothing.
     *
     * @throws NativeLibraryLoadException If the property isn't set, or if loading fails. The reason is also available from {@link #getLoadStatus()}.
     * @throws IllegalStateException      If the libraries were already loaded from a different directory than the property names
     */
    public static void load()
    {
        Niagara.loadWith(null, NativeLibraryPlatform::current, Niagara::discoverProvider);
    }

    /**
     * Where loading Niagara's native libraries currently stands.
     *
     * @return {@link NativeLibraryLoadStatus.LoadingState#NOT_LOADED} until a load is attempted, then either
     * {@link NativeLibraryLoadStatus.LoadingState#LOADED} or {@link NativeLibraryLoadStatus.LoadingState#FAILED} along with the reason
     */
    public static NativeLibraryLoadStatus getLoadStatus()
    {
        return loadStatus;
    }

    private static NativeLibraryLoadException fail(NativeLibraryLoadException exception)
    {
        loadStatus = NativeLibraryLoadStatus.failed(exception.getFailureReason());

        return exception;
    }

    private static void ensureSameDirectory(Path explicitDirectory)
    {
        Optional<Path> requestedDirectory = NativeLibraries.configuredDirectory(explicitDirectory);

        if (requestedDirectory.isPresent() && !requestedDirectory.get().equals(loadedDirectory))
        {
            String message = String.format("Niagara's native libraries were already loaded from %s and can't be loaded again from %s", loadedDirectory,
                    requestedDirectory.get());

            throw new IllegalStateException(message);
        }
    }

    static synchronized void loadWith(Path explicitDirectory, Supplier<NativeLibraryPlatform> platformSupplier, Supplier<NativeLibraryProvider> providerSupplier)
    {
        if (loadStatus.isLoaded())
        {
            Niagara.ensureSameDirectory(explicitDirectory);
            return;
        }

        try
        {
            NativeLibraryPlatform nativeLibraryPlatform = platformSupplier.get();
            NativeLibraries nativeLibraries = NativeLibraries.locate(nativeLibraryPlatform, explicitDirectory);

            providerSupplier.get().load(nativeLibraryPlatform, nativeLibraries);

            loadedDirectory = nativeLibraries.directory();
            loadStatus = NativeLibraryLoadStatus.loaded();
        }
        catch (NativeLibraryLoadException exception)
        {
            throw Niagara.fail(exception);
        }
        catch (UnsatisfiedLinkError error)
        {
            throw Niagara.fail(new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.NATIVE_LOAD_FAILED, error.getMessage(), error));
        }
        catch (ServiceConfigurationError error)
        {
            throw Niagara.fail(new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.BACKEND_UNAVAILABLE,
                    "A Niagara native backend could not be loaded: " + error.getMessage(), error));
        }
        catch (RuntimeException exception)
        {
            throw Niagara.fail(new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.UNKNOWN,
                    "Unexpected failure while loading Niagara's native libraries", exception));
        }
    }

    static NativeLibraryProvider discoverProvider()
    {
        Iterator<NativeLibraryProvider> nativeLibraryProviders = ServiceLoader.load(NativeLibraryProvider.class).iterator();
        if (!nativeLibraryProviders.hasNext())
            throw new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.BACKEND_UNAVAILABLE, "No Niagara native backend was found!");

        return nativeLibraryProviders.next();
    }

    static synchronized void resetForTesting()
    {
        loadStatus = NativeLibraryLoadStatus.notLoaded();
        loadedDirectory = null;
    }
}
