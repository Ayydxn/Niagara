package me.ayydxn.niagara;

import me.ayydxn.niagara.core.loader.*;
import me.ayydxn.niagara.core.loader.NativeLibraryLoadStatus.FailureReason;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NiagaraTest
{
    private static final NativeLibraryPlatform PLATFORM = NativeLibraryPlatform.LINUX_X64;

    private Path directory;
    private RecordingProvider provider;

    @BeforeEach
    void setUp() throws IOException
    {
        System.clearProperty(NativeLibraries.LIBRARIES_DIRECTORY_PROPERTY);
        Niagara.resetForTesting();

        this.directory = NiagaraTest.createLibraryDirectory();
        this.provider = new RecordingProvider();
    }

    @AfterEach
    void tearDown() throws IOException
    {
        System.clearProperty(NativeLibraries.LIBRARIES_DIRECTORY_PROPERTY);
        Niagara.resetForTesting();

        NiagaraTest.deleteDirectory(this.directory);
    }

    @Test
    void startsNotLoaded()
    {
        assertEquals(NativeLibraryLoadStatus.notLoaded(), Niagara.getLoadStatus());
    }

    @Test
    void loadsOnceFromAnExplicitDirectory()
    {
        this.load(this.directory);

        assertEquals(NativeLibraryLoadStatus.loaded(), Niagara.getLoadStatus());
        assertEquals(1, this.provider.loadCount);
        assertEquals(PLATFORM, this.provider.platform);
        assertEquals(4, this.provider.libraries.files().size());
    }

    @Test
    void loadingAgainFromTheSameDirectoryDoesNothing()
    {
        this.load(this.directory);
        this.load(this.directory);
        this.load(null);

        assertEquals(1, this.provider.loadCount);
    }

    @Test
    void loadingFromADifferentDirectoryAfterLoadingFails() throws IOException
    {
        Path otherDirectory = NiagaraTest.createLibraryDirectory();

        try
        {
            this.load(this.directory);

            assertThrows(IllegalStateException.class, () -> this.load(otherDirectory));
            assertEquals(NativeLibraryLoadStatus.loaded(), Niagara.getLoadStatus());
        }
        finally
        {
            NiagaraTest.deleteDirectory(otherDirectory);
        }
    }

    @Test
    void usesTheSystemPropertyWhenNoDirectoryIsPassed()
    {
        System.setProperty(NativeLibraries.LIBRARIES_DIRECTORY_PROPERTY, this.directory.toString());

        this.load(null);

        assertEquals(NativeLibraryLoadStatus.loaded(), Niagara.getLoadStatus());
    }

    @Test
    void failsWithoutAConfiguredDirectory()
    {
        this.assertLoadFails(FailureReason.DIRECTORY_NOT_CONFIGURED, null, this.provider);

        assertEquals(0, this.provider.loadCount);
    }

    @Test
    void failsOnAnUnsupportedPlatform()
    {
        NativeLibraryLoadException exception = assertThrows(NativeLibraryLoadException.class, () -> Niagara.loadWith(this.directory,
                () -> NativeLibraryPlatform.detect("FreeBSD", "amd64"), () -> this.provider));

        assertEquals(FailureReason.UNSUPPORTED_PLATFORM, exception.getFailureReason());
        assertEquals(NativeLibraryLoadStatus.failed(FailureReason.UNSUPPORTED_PLATFORM), Niagara.getLoadStatus());
    }

    @Test
    void reportsAnUnsatisfiedLinkErrorAsANativeLoadFailure()
    {
        NativeLibraryProvider failingProvider = (platform, libraries) ->
        {
            throw new UnsatisfiedLinkError("boom");
        };

        this.assertLoadFails(FailureReason.NATIVE_LOAD_FAILED, this.directory, failingProvider);
    }

    @Test
    void reportsAnythingElseAsUnknown()
    {
        NativeLibraryProvider failingProvider = (platform, libraries) ->
        {
            throw new IllegalArgumentException("boom");
        };

        this.assertLoadFails(FailureReason.UNKNOWN, this.directory, failingProvider);
    }

    @Test
    void canBeRetriedAfterAFailure()
    {
        this.assertLoadFails(FailureReason.DIRECTORY_NOT_CONFIGURED, null, this.provider);

        this.load(this.directory);

        assertEquals(NativeLibraryLoadStatus.loaded(), Niagara.getLoadStatus());
    }

    @Test
    void failsWhenNoBackendIsAvailable()
    {
        NativeLibraryLoadException exception = assertThrows(NativeLibraryLoadException.class, Niagara::discoverProvider);

        assertEquals(FailureReason.BACKEND_UNAVAILABLE, exception.getFailureReason());
    }

    private void load(Path explicitDirectory)
    {
        Niagara.loadWith(explicitDirectory, () -> PLATFORM, () -> this.provider);
    }

    private void assertLoadFails(FailureReason reason, Path explicitDirectory, NativeLibraryProvider failingProvider)
    {
        Supplier<NativeLibraryProvider> providerSupplier = () -> failingProvider;

        NativeLibraryLoadException exception = assertThrows(NativeLibraryLoadException.class,
                () -> Niagara.loadWith(explicitDirectory, () -> PLATFORM, providerSupplier));

        assertEquals(reason, exception.getFailureReason());
        assertEquals(NativeLibraryLoadStatus.failed(reason), Niagara.getLoadStatus());
    }

    private static Path createLibraryDirectory() throws IOException
    {
        Path directory = Files.createTempDirectory("niagara-api-test");

        for (String name : PLATFORM.getUltralightLibraries())
            Files.createFile(directory.resolve(name));

        return directory;
    }

    private static void deleteDirectory(Path directory) throws IOException
    {
        try (Stream<Path> paths = Files.walk(directory))
        {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
        }
    }

    private static final class RecordingProvider implements NativeLibraryProvider
    {
        private int loadCount;
        private NativeLibraryPlatform platform;
        private NativeLibraries libraries;

        @Override
        public void load(NativeLibraryPlatform platform, NativeLibraries libraries)
        {
            this.loadCount++;

            this.platform = platform;
            this.libraries = libraries;
        }
    }
}
