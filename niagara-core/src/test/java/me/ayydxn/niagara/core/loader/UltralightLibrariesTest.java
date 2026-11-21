package me.ayydxn.niagara.core.loader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UltralightLibrariesTest
{
    private static final NativeLibraryPlatform PLATFORM = NativeLibraryPlatform.LINUX_X64;

    private Path directory;

    @BeforeEach
    void createDirectory() throws IOException
    {
        this.directory = Files.createTempDirectory("niagara-libraries-test");
    }

    @AfterEach
    void deleteDirectory() throws IOException
    {
        try (Stream<Path> paths = Files.walk(this.directory))
        {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
        }
    }

    @Test
    void returnsTheLibrariesInLoadOrder() throws IOException
    {
        this.createLibraries(PLATFORM.getUltralightLibraries());
        Files.createFile(this.directory.resolve("libUnrelated.so"));

        NativeLibraries libraries = NativeLibraries.locate(PLATFORM, this.directory, null);

        assertEquals(this.directory.toAbsolutePath().normalize(), libraries.directory());
        assertEquals(PLATFORM.getUltralightLibraries().stream().map(this.directory::resolve).toList(), libraries.files());
    }

    @Test
    void explicitDirectoryWinsOverTheProperty() throws IOException
    {
        this.createLibraries(PLATFORM.getUltralightLibraries());

        NativeLibraries libraries = NativeLibraries.locate(PLATFORM, this.directory, "/does/not/exist");

        assertEquals(this.directory.toAbsolutePath().normalize(), libraries.directory());
    }

    @Test
    void usesThePropertyWhenNoDirectoryIsPassed() throws IOException
    {
        this.createLibraries(PLATFORM.getUltralightLibraries());

        NativeLibraries libraries = NativeLibraries.locate(PLATFORM, null, "  " + this.directory + "  ");

        assertEquals(this.directory.toAbsolutePath().normalize(), libraries.directory());
    }

    @Test
    void failsWhenNoDirectoryIsConfigured()
    {
        assertFailsWith(NativeLibraryLoadStatus.FailureReason.DIRECTORY_NOT_CONFIGURED, null, null);
        assertFailsWith(NativeLibraryLoadStatus.FailureReason.DIRECTORY_NOT_CONFIGURED, null, "   ");
    }

    @Test
    void failsWhenTheDirectoryDoesNotExist()
    {
        assertFailsWith(NativeLibraryLoadStatus.FailureReason.DIRECTORY_INVALID, this.directory.resolve("missing"), null);
    }

    @Test
    void failsWhenThePathIsAFile() throws IOException
    {
        Path file = Files.createFile(this.directory.resolve("file.txt"));

        assertFailsWith(NativeLibraryLoadStatus.FailureReason.DIRECTORY_INVALID, file, null);
    }

    @Test
    void reportsEveryMissingLibrary() throws IOException
    {
        List<String> required = PLATFORM.getUltralightLibraries();

        Files.createFile(this.directory.resolve(required.get(0)));

        NativeLibraryLoadException exception = assertThrows(NativeLibraryLoadException.class,
                () -> NativeLibraries.locate(PLATFORM, this.directory, null));

        assertEquals(NativeLibraryLoadStatus.FailureReason.LIBRARY_FILE_MISSING, exception.getFailureReason());
        assertFalse(exception.getMessage().contains(required.get(0)));

        for (String missing : required.subList(1, required.size()))
            assertTrue(exception.getMessage().contains(missing));
    }

    @Test
    void aDirectoryNamedLikeALibraryDoesNotCount() throws IOException
    {
        this.createLibraries(List.of("libUltralightCore.so", "libUltralight.so"));
        Files.createDirectory(this.directory.resolve("libWebCore.so"));

        NativeLibraryLoadException exception = assertThrows(NativeLibraryLoadException.class,
                () -> NativeLibraries.locate(PLATFORM, this.directory, null));

        assertEquals(NativeLibraryLoadStatus.FailureReason.LIBRARY_FILE_MISSING, exception.getFailureReason());
    }

    private void createLibraries(List<String> names) throws IOException
    {
        for (String name : names)
            Files.createFile(this.directory.resolve(name));
    }

    private static void assertFailsWith(NativeLibraryLoadStatus.FailureReason reason, Path explicitDirectory, String propertyValue)
    {
        NativeLibraryLoadException exception = assertThrows(NativeLibraryLoadException.class, () -> NativeLibraries.locate(PLATFORM, explicitDirectory, propertyValue));

        assertEquals(reason, exception.getFailureReason());
    }
}
