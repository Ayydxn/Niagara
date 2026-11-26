package me.ayydxn.niagara.core.natives;

import me.ayydxn.niagara.core.backend.NativeBackend;
import me.ayydxn.niagara.core.test.NativeInterfaceTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public abstract class ULPlatformNativesTest extends NativeInterfaceTest<ULPlatformNatives>
{
    @Test
    void enablingDefaultPlatformHandlersDoesntThrow()
    {
        assertDoesNotThrow(() ->
        {
            long baseDirectoryString = this.backend().ulString().create("./");

            this.natives.enablePlatformClipboard();
            this.natives.enablePlatformFileSystem(baseDirectoryString);
            this.natives.enablePlatformFontLoader();

            this.backend().ulString().destroy(baseDirectoryString);
        });
    }

    @Override
    protected ULPlatformNatives selectNatives(NativeBackend nativeBackend)
    {
        return nativeBackend.ulPlatform();
    }
}
