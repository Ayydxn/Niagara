package me.ayydxn.niagara.jni;

import me.ayydxn.niagara.core.loader.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;

public final class JNINativeLibraryProvider implements NativeLibraryProvider
{
    private static final String CACHE_DIRECTORY_NAME = "niagara-natives";

    @Override
    public void load(NativeLibraryPlatform platform, NativeLibraries libraries)
    {
        // Everything that can fail without touching native code happens first.
        byte[] bridgeBytes = JNINativeLibraryProvider.readEmbeddedBridge(platform);
        Path bridgePath = JNINativeLibraryProvider.extractBridge(platform, bridgeBytes);

        for (Path library : libraries.files())
            JNINativeLibraryProvider.loadLibrary(library);

        JNINativeLibraryProvider.loadLibrary(bridgePath);
    }

    private static byte[] readEmbeddedBridge(NativeLibraryPlatform platform)
    {
        String resourcePath = "/natives/" + platform.getID() + "/" + platform.getJNIBridgeLibrary();

        try (InputStream inputStream = JNINativeLibraryProvider.class.getResourceAsStream(resourcePath))
        {
            if (inputStream == null)
            {
                throw new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.BRIDGE_MISSING,
                        "Niagara's JNI bridge was not found in niagara-jni at " + resourcePath);
            }

            return inputStream.readAllBytes();
        }
        catch (IOException exception)
        {
            throw new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.NATIVE_LOAD_FAILED,
                    "Failed to read Niagara's JNI bridge from " + resourcePath, exception);
        }
    }

    // The file goes in a directory named after its hash, so a different build of the bridge never collides with one that another process has loaded.
    private static Path extractBridge(NativeLibraryPlatform nativeLibraryPlatform, byte[] jniBridgeBytes)
    {
        Path directory = Path.of(System.getProperty("java.io.tmpdir"), CACHE_DIRECTORY_NAME, JNINativeLibraryProvider.getSHA256Hash(jniBridgeBytes));
        Path target = directory.resolve(nativeLibraryPlatform.getJNIBridgeLibrary());

        try
        {
            if (JNINativeLibraryProvider.hasContents(target, jniBridgeBytes))
                return target;

            Files.createDirectories(directory);

            // Written next to the target and moved into place, so another process never loads a half-written file.
            Path temporaryFile = Files.createTempFile(directory, "bridge", ".tmp");

            try
            {
                Files.write(temporaryFile, jniBridgeBytes);
                Files.move(temporaryFile, target, StandardCopyOption.ATOMIC_MOVE);
            }
            catch (IOException exception)
            {
                Files.deleteIfExists(temporaryFile);

                // Another process may have put the same file there first.
                if (!JNINativeLibraryProvider.hasContents(target, jniBridgeBytes))
                    throw exception;
            }

            return target;
        }
        catch (IOException exception)
        {
            throw new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.NATIVE_LOAD_FAILED,
                    "Failed to extract Niagara's JNI bridge to " + target, exception);
        }
    }

    private static boolean hasContents(Path file, byte[] expected) throws IOException
    {
        return Files.isRegularFile(file) && Arrays.equals(Files.readAllBytes(file), expected);
    }

    private static void loadLibrary(Path library)
    {
        try
        {
            System.load(library.toAbsolutePath().toString());
        }
        catch (UnsatisfiedLinkError error)
        {
            throw new NativeLibraryLoadException(NativeLibraryLoadStatus.FailureReason.NATIVE_LOAD_FAILED,
                    "Failed to load native library " + library.getFileName() + ": " + error.getMessage(), error);
        }
    }

    private static String getSHA256Hash(byte[] bytes)
    {
        try
        {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        }
        catch (NoSuchAlgorithmException exception)
        {
            throw new IllegalStateException("SHA-256 is unavailable!", exception);
        }
    }
}
