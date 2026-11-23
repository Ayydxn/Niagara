package me.ayydxn.niagara;

import me.ayydxn.niagara.core.backend.NativeBackendManager;
import me.ayydxn.niagara.core.backend.NativeBackendType;
import me.ayydxn.niagara.core.loader.*;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
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
 * <p>
 * Niagara has two native backends: JNI ({@code niagara-jni}, Java 17+) and FFM ({@code niagara-ffm}, Java 22+). If both are on the classpath, FFM is used wherever the JVM can run it
 * and JNI is used otherwise. To choose one yourself, set the {@value NativeBackendType#BACKEND_PROPERTY} system property to {@code jni} or {@code ffm}.
 * <p>
 * <h2>Example Usage</h2>
 * <pre>{@code
 *     public static void main(String[] args) {
 *         // Explicity provide a directory on disk
 *         Niagara.load(Path.of("C:/Libraries"));
 *
 *         // Rely the path from the system property
 *         Niagara.load();
 *     }
 * }</pre>
 */
public final class Niagara
{
    private static final List<NativeBackendType> BACKEND_PREFERENCE = List.of(NativeBackendType.FFM, NativeBackendType.JNI);

    // Only a safeguard. The service loader skips each failing provider, so we should never get anywhere near this.
    private static final int MAX_PROVIDER_FAILURES = 8;

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

            NativeLibraryProvider nativeLibraryProvider = providerSupplier.get();
            nativeLibraryProvider.load(nativeLibraryPlatform, nativeLibraries);

            NativeBackendManager.install(nativeLibraryProvider.createNativeBackend());

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
        List<NativeLibraryProvider> nativeLibraryProviders = new ArrayList<>();
        Throwable loadFailure = null;
        int failureCount = 0;

        Iterator<NativeLibraryProvider> iterator = ServiceLoader.load(NativeLibraryProvider.class).iterator();

        while (true)
        {
            try
            {
                if (!iterator.hasNext())
                    break;

                nativeLibraryProviders.add(iterator.next());
            }
            catch (ServiceConfigurationError | LinkageError error)
            {
                // A backend the JVM can't load (e.g., niagara-ffm on Java 17) must not stop us from using one that it can.
                if (loadFailure == null)
                {
                    loadFailure = error;
                }
                else
                {
                    loadFailure.addSuppressed(error);
                }

                if (++failureCount >= MAX_PROVIDER_FAILURES)
                    break;
            }
        }

        return Niagara.selectProvider(nativeLibraryProviders, System.getProperty(NativeBackendType.BACKEND_PROPERTY), loadFailure);

    }

    static NativeLibraryProvider selectProvider(List<NativeLibraryProvider> nativeLibraryProviders, String requestedBackend, Throwable loadFailure)
    {
        if (requestedBackend != null && !requestedBackend.isBlank())
        {
            NativeBackendType requestedType = NativeBackendType.fromName(requestedBackend).orElseThrow(() ->
            {
                String message = String.format("The %s system property must be jni or ffm, but it is \"%s\"", NativeBackendType.BACKEND_PROPERTY, requestedBackend.trim());

                return new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.BACKEND_UNAVAILABLE, message);
            });

            return nativeLibraryProviders.stream().filter(provider -> provider.getType() == requestedType).findFirst().orElseThrow(() ->
            {
                String message = String.format("The %s system property asks for the %s backend, but it isn't available. Check that niagara-jni or niagara-ffm is on the classpath and that " +
                    "your JVM can run it (FFM needs Java 22 or newer)", NativeBackendType.BACKEND_PROPERTY, requestedType);

                return new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.BACKEND_UNAVAILABLE, message, loadFailure);
            });
        }

        return nativeLibraryProviders.stream().min(Comparator.comparingInt(nativeLibraryProvider -> BACKEND_PREFERENCE.indexOf(nativeLibraryProvider.getType()))).orElseThrow(() ->
        {
            String message = loadFailure == null ? "No Niagara native backend was found!" : "No Niagara native backend could be loaded: " + loadFailure.getMessage();

            return new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.BACKEND_UNAVAILABLE, message, loadFailure);
        });
    }

    static synchronized void resetForTesting()
    {
        loadStatus = NativeLibraryLoadStatus.notLoaded();
        loadedDirectory = null;

        NativeBackendManager.uninstall();
    }
}
