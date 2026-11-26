package me.ayydxn.niagara;

import me.ayydxn.niagara.core.backend.NativeBackend;
import me.ayydxn.niagara.core.backend.NativeBackendManager;
import me.ayydxn.niagara.core.backend.NativeBackendType;
import me.ayydxn.niagara.core.loader.*;
import me.ayydxn.niagara.core.loader.NativeLibraryLoadStatus.FailureReason;
import me.ayydxn.niagara.core.natives.ULConfigNatives;
import me.ayydxn.niagara.core.natives.ULPlatformNatives;
import me.ayydxn.niagara.core.natives.ULStringNatives;
import me.ayydxn.niagara.core.natives.UltralightNatives;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class NiagaraTest
{
    private static final NativeLibraryPlatform PLATFORM = NativeLibraryPlatform.LINUX_X64;

    private Path directory;
    private TestProvider provider;

    @BeforeEach
    void setUp() throws IOException
    {
        System.clearProperty(NativeLibraries.LIBRARIES_DIRECTORY_PROPERTY);
        System.clearProperty(NativeBackendType.BACKEND_PROPERTY);
        Niagara.resetForTesting();

        this.directory = NiagaraTest.createLibraryDirectory();
        this.provider = new TestProvider(NativeBackendType.JNI);
    }

    @AfterEach
    void tearDown() throws IOException
    {
        System.clearProperty(NativeLibraries.LIBRARIES_DIRECTORY_PROPERTY);
        System.clearProperty(NativeBackendType.BACKEND_PROPERTY);
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
        TestProvider failingProvider = new TestProvider(NativeBackendType.JNI);
        failingProvider.loadAction = () ->
        {
            throw new UnsatisfiedLinkError("boom");
        };

        this.assertLoadFails(FailureReason.NATIVE_LOAD_FAILED, this.directory, failingProvider);
    }

    @Test
    void reportsAnythingElseAsUnknown()
    {
        TestProvider failingProvider = new TestProvider(NativeBackendType.JNI);
        failingProvider.loadAction = () ->
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
    void installsTheBackendOfTheLoadedProvider()
    {
        this.load(this.directory);

        assertSame(this.provider.backend, NativeBackendManager.getCurrentBackendInstance());
    }

    @Test
    void doesNotInstallABackendWhenLoadingFails()
    {
        TestProvider failingProvider = new TestProvider(NativeBackendType.JNI);
        failingProvider.loadAction = () ->
        {
            throw new UnsatisfiedLinkError("boom");
        };

        this.assertLoadFails(FailureReason.NATIVE_LOAD_FAILED, this.directory, failingProvider);

        assertThrows(IllegalStateException.class, NativeBackendManager::getCurrentBackendInstance);
    }

    @Test
    void failsWhenNoBackendIsAvailable()
    {
        NativeLibraryLoadException exception = assertThrows(NativeLibraryLoadException.class, Niagara::discoverProvider);

        assertEquals(FailureReason.BACKEND_UNAVAILABLE, exception.getFailureReason());
    }

    @Test
    void prefersFfmWhenBothBackendsAreAvailable()
    {
        TestProvider jni = new TestProvider(NativeBackendType.JNI);
        TestProvider ffm = new TestProvider(NativeBackendType.FFM);

        assertSame(ffm, Niagara.selectProvider(List.of(jni, ffm), null, null));
        assertSame(ffm, Niagara.selectProvider(List.of(ffm, jni), null, null));
    }

    @Test
    void usesTheOnlyBackendThatIsAvailable()
    {
        TestProvider jni = new TestProvider(NativeBackendType.JNI);
        TestProvider ffm = new TestProvider(NativeBackendType.FFM);

        assertSame(jni, Niagara.selectProvider(List.of(jni), null, null));
        assertSame(ffm, Niagara.selectProvider(List.of(ffm), "", null));
    }

    @Test
    void theBackendPropertyForcesTheRequestedBackend()
    {
        TestProvider jni = new TestProvider(NativeBackendType.JNI);
        TestProvider ffm = new TestProvider(NativeBackendType.FFM);

        assertSame(jni, Niagara.selectProvider(List.of(jni, ffm), "jni", null));
        assertSame(jni, Niagara.selectProvider(List.of(ffm, jni), "  JNI ", null));
        assertSame(ffm, Niagara.selectProvider(List.of(jni, ffm), "Ffm", null));
    }

    @Test
    void failsWhenTheRequestedBackendIsNotAvailable()
    {
        TestProvider jni = new TestProvider(NativeBackendType.JNI);
        Error loadFailure = new UnsupportedClassVersionError("too new");

        NativeLibraryLoadException exception = assertThrows(NativeLibraryLoadException.class, () -> Niagara.selectProvider(List.of(jni),
            "ffm", loadFailure));

        assertEquals(FailureReason.BACKEND_UNAVAILABLE, exception.getFailureReason());
        assertSame(loadFailure, exception.getCause());
    }

    @Test
    void failsOnAnUnknownBackendName()
    {
        TestProvider jni = new TestProvider(NativeBackendType.JNI);

        NativeLibraryLoadException exception = assertThrows(NativeLibraryLoadException.class, () -> Niagara.selectProvider(List.of(jni),
            "panama", null));

        assertEquals(FailureReason.BACKEND_UNAVAILABLE, exception.getFailureReason());
    }

    @Test
    void reportsWhyNoBackendCouldBeLoaded()
    {
        Error loadFailure = new UnsupportedClassVersionError("too new");

        NativeLibraryLoadException exception = assertThrows(NativeLibraryLoadException.class, () -> Niagara.selectProvider(List.of(),
            null, loadFailure));

        assertEquals(FailureReason.BACKEND_UNAVAILABLE, exception.getFailureReason());
        assertSame(loadFailure, exception.getCause());
    }

    private void load(Path explicitDirectory)
    {
        Niagara.loadWith(explicitDirectory, () -> PLATFORM, () -> this.provider);
    }

    private void assertLoadFails(FailureReason reason, Path explicitDirectory, NativeLibraryProvider failingProvider)
    {
        Supplier<NativeLibraryProvider> providerSupplier = () -> failingProvider;

        NativeLibraryLoadException exception = assertThrows(NativeLibraryLoadException.class, () -> Niagara.loadWith(explicitDirectory,
            () -> PLATFORM, providerSupplier));

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

    private static final class TestProvider implements NativeLibraryProvider
    {
        private final NativeBackendType type;
        private final NativeBackend backend;

        private Runnable loadAction = () -> {};
        private NativeLibraryPlatform platform;
        private NativeLibraries libraries;
        private int loadCount;

        private TestProvider(NativeBackendType nativeBackendType)
        {
            this.type = nativeBackendType;
            this.backend = new StubBackend(nativeBackendType);
        }

        @Override
        public NativeBackend createNativeBackend()
        {
            return this.backend;
        }

        @Override
        public void load(NativeLibraryPlatform platform, NativeLibraries libraries)
        {
            this.loadAction.run();

            this.loadCount++;

            this.platform = platform;
            this.libraries = libraries;
        }

        @Override
        public NativeBackendType getType()
        {
            return this.type;
        }
    }

    // None of these are ever called. They only have to exist for the backend to be complete.
    private record StubBackend(NativeBackendType type) implements NativeBackend
    {
        @Override
        public @NotNull NativeBackendType getType()
        {
            return this.type;
        }

        @Override
        public @NotNull UltralightNatives ultralight()
        {
            return StubBackend.stub(UltralightNatives.class);
        }

        @Override
        public @NotNull ULConfigNatives ulConfig()
        {
            return StubBackend.stub(ULConfigNatives.class);
        }

        @Override
        public @NotNull ULPlatformNatives ulPlatform()
        {
            return StubBackend.stub(ULPlatformNatives.class);
        }

        @Override
        public @NotNull ULStringNatives ulString()
        {
            return StubBackend.stub(ULStringNatives.class);
        }

        private static <T> T stub(Class<T> type)
        {
            return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (proxy, method, arguments) ->
            {
                throw new UnsupportedOperationException();
            }));
        }
    }
}
