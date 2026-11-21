package me.ayydxn.niagara.core.loader;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record NativeLibraries(@NotNull Path directory, List<Path> files)
{
    public static final String LIBRARIES_DIRECTORY_PROPERTY = "niagara.libraries.path";

    public NativeLibraries
    {
        Objects.requireNonNull(directory, "directory");

        files = List.copyOf(files);
    }

    public static Optional<Path> configuredDirectory(Path explicitDirectory)
    {
        return NativeLibraries.configuredDirectory(explicitDirectory, System.getProperty(LIBRARIES_DIRECTORY_PROPERTY));
    }

    public static Optional<Path> configuredDirectory(Path explicitDirectory, String propertyValue)
    {
        if (explicitDirectory != null)
            return Optional.of(NativeLibraries.normalize(explicitDirectory));

        if (propertyValue == null || propertyValue.isBlank())
            return Optional.empty();

        try
        {
            return Optional.of(NativeLibraries.normalize(Path.of(propertyValue.trim())));
        }
        catch (InvalidPathException exception)
        {
            throw new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.DIRECTORY_INVALID,
                    "The " + LIBRARIES_DIRECTORY_PROPERTY + " system property is not a valid path: " + propertyValue, exception);
        }
    }

    public static NativeLibraries locate(NativeLibraryPlatform nativeLibraryPlatform, Path explicitDirectory)
    {
        return NativeLibraries.locate(nativeLibraryPlatform, explicitDirectory, System.getProperty(LIBRARIES_DIRECTORY_PROPERTY));
    }

    public static NativeLibraries locate(NativeLibraryPlatform nativeLibraryPlatform, Path explicitDirectory, String propertyValue)
    {
        Path directory = NativeLibraries.configuredDirectory(explicitDirectory, propertyValue).orElseThrow(() ->
        {
            String message = String.format("No Ultralight library directory was provided. Pass one to Niagara.load or set the %s system property",
                    LIBRARIES_DIRECTORY_PROPERTY);

            return new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.DIRECTORY_NOT_CONFIGURED, message);
        });

        if (!Files.isDirectory(directory))
        {
            throw new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.DIRECTORY_INVALID,
                    "The Ultralight library directory does not exist or is not a directory: " + directory);
        }

        List<Path> files = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String libraryName : nativeLibraryPlatform.getUltralightLibraries())
        {
            Path library = directory.resolve(libraryName);

            if (Files.isRegularFile(library))
            {
                files.add(library);
            }
            else
            {
                missing.add(libraryName);
            }
        }

        if (!missing.isEmpty())
        {
            String message = "The Ultralight library directory " + directory + " is missing required libraries: " + String.join(", ", missing);

            throw new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.LIBRARY_FILE_MISSING, message);
        }

        return new NativeLibraries(directory, files);
    }

    private static Path normalize(Path path)
    {
        return path.toAbsolutePath().normalize();
    }
}
