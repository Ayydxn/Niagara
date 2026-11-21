package me.ayydxn.niagara.core.loader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NativeLibraryPlatformTest
{
    @Test
    void detectsSupportedPlatforms()
    {
        assertEquals(NativeLibraryPlatform.WINDOWS_X64, NativeLibraryPlatform.detect("Windows 11", "amd64"));
        assertEquals(NativeLibraryPlatform.MACOS_ARM64, NativeLibraryPlatform.detect("Mac OS X", "aarch64"));
        assertEquals(NativeLibraryPlatform.LINUX_X64, NativeLibraryPlatform.detect("Linux", "x86_64"));
    }

    @Test
    void rejectsUnsupportedPlatforms()
    {
        String[][] unsupported = {{"Mac OS X", "x86_64"}, {"Linux", "aarch64"}, {"Windows 11", "aarch64"}, {"FreeBSD", "amd64"}, {null, null}};

        for (String[] platform : unsupported)
        {
            NativeLibraryLoadException exception = assertThrows(NativeLibraryLoadException.class,
                    () -> NativeLibraryPlatform.detect(platform[0], platform[1]));

            assertEquals(NativeLibraryLoadStatus.FailureReason.UNSUPPORTED_PLATFORM, exception.getFailureReason());
        }
    }

    @Test
    void loadsUltralightCoreFirstAndUltralightLast()
    {
        for (NativeLibraryPlatform nativeLibraryPlatform : NativeLibraryPlatform.values())
        {
            assertEquals(4, nativeLibraryPlatform.getUltralightLibraries().size());

            assertTrue(nativeLibraryPlatform.getUltralightLibraries().get(0).contains("UltralightCore"));
            assertTrue(nativeLibraryPlatform.getUltralightLibraries().get(1).contains("WebCore"));
            assertFalse(nativeLibraryPlatform.getUltralightLibraries().get(2).contains("Core"));
            assertTrue(nativeLibraryPlatform.getUltralightLibraries().get(3).contains("App"));
        }
    }
}
