package me.ayydxn.niagara.core.extensions;

import me.ayydxn.niagara.core.backend.NativeBackendManager;
import me.ayydxn.niagara.core.loader.NativeLibraries;
import me.ayydxn.niagara.core.loader.NativeLibraryPlatform;
import me.ayydxn.niagara.core.loader.NativeLibraryProvider;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public final class NativeBackendExtension implements BeforeAllCallback
{
    private static Throwable setupFailure;
    private static boolean isInstalled;

    @Override
    public void beforeAll(@NonNull ExtensionContext extensionContext) throws Exception
    {
        Assumptions.assumeTrue(NativeLibraries.configuredDirectory(null).isPresent(),
            "The " + NativeLibraries.LIBRARIES_DIRECTORY_PROPERTY + " system property isn't set, so there are no Ultralight libraries to test against");

        NativeBackendExtension.ensureInstalled();
    }

    private static synchronized void ensureInstalled()
    {
        if (setupFailure != null)
            throw new IllegalStateException("The native backend failed to set up earlier in this test run", setupFailure);

        if (isInstalled)
            return;

        try
        {
            NativeLibraryProvider nativeLibraryProvider = NativeBackendExtension.findProvider();
            NativeLibraryPlatform nativeLibraryPlatform = NativeLibraryPlatform.current();
            NativeLibraries nativeLibraries = NativeLibraries.locate(nativeLibraryPlatform, null);

            nativeLibraryProvider.load(nativeLibraryPlatform, nativeLibraries);

            NativeBackendManager.install(nativeLibraryProvider.createNativeBackend());

            isInstalled = true;
        }
        catch (RuntimeException | Error exception)
        {
            setupFailure = exception;

            throw exception;
        }
    }

    private static NativeLibraryProvider findProvider()
    {
        List<NativeLibraryProvider> nativeLibraryProviders = new ArrayList<>();
        ServiceLoader.load(NativeLibraryProvider.class).forEach(nativeLibraryProviders::add);

        // Exactly one so that a test class can never silently run against a different backend than the module it lives in.
        if (nativeLibraryProviders.size() != 1)
        {
            List<String> names = nativeLibraryProviders.stream().map(provider -> provider.getClass().getName()).toList();

            throw new IllegalStateException("Expected exactly one NativeLibraryProvider on the test classpath, but found " + nativeLibraryProviders.size() + ": " + names);
        }

        return nativeLibraryProviders.get(0);
    }
}
