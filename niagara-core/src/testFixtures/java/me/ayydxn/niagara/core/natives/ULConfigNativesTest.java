package me.ayydxn.niagara.core.natives;

import me.ayydxn.niagara.core.backend.NativeBackend;
import me.ayydxn.niagara.core.test.NativeInterfaceTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public abstract class ULConfigNativesTest extends NativeInterfaceTest<ULConfigNatives>
{
    private static final int FIRST_ENUM_VALUE = 0;

    @Test
    void createReturnsANonNullHandle()
    {
        assertNotEquals(0L, this.createConfig());
    }

    @Test
    void distinctConfigsGetDistinctHandles()
    {
        assertNotEquals(this.createConfig(), this.createConfig());
    }

    @Test
    void acceptsEveryStringSetter(@TempDir Path directory)
    {
        long config = this.createConfig();

        assertDoesNotThrow(() ->
        {
            this.natives.setCachePath(config, this.createString(directory.toString()));
            this.natives.setResourcePathPrefix(config, this.createString("resources/"));
            this.natives.setUserStylesheet(config, this.createString("body { background: transparent; user-select: none; }"));
        });
    }

    @Test
    void acceptsEveryEnumSetter()
    {
        long config = this.createConfig();

        assertDoesNotThrow(() ->
        {
            this.natives.setFaceWinding(config, FIRST_ENUM_VALUE);
            this.natives.setFontHinting(config, FIRST_ENUM_VALUE);
            this.natives.setFontProfile(config, FIRST_ENUM_VALUE);
            this.natives.setFontBlendMode(config, FIRST_ENUM_VALUE);
            this.natives.setPixelFontRendering(config, FIRST_ENUM_VALUE);
            this.natives.setMemoryProfile(config, FIRST_ENUM_VALUE);
            this.natives.setEffectQuality(config, FIRST_ENUM_VALUE);
            this.natives.setJavaScriptDiagnostics(config, FIRST_ENUM_VALUE);
            this.natives.setDOMDiagnostics(config, FIRST_ENUM_VALUE);
            this.natives.setMinLogLevel(config, FIRST_ENUM_VALUE);
        });
    }

    @Test
    void acceptsEveryBooleanSetter()
    {
        long config = this.createConfig();

        assertDoesNotThrow(() ->
        {
            for (boolean enabled : new boolean[] { true, false })
            {
                this.natives.setSnapPixelFontSize(config, enabled);
                this.natives.setForceRepaint(config, enabled);
                this.natives.setEnablePhoton(config, enabled);
                this.natives.setEnablePhotonText(config, enabled);
                this.natives.setIdleGCEnabled(config, enabled);
                this.natives.setIgnoreSSLErrors(config, enabled);
                this.natives.setUseSystemCertificateStore(config, enabled);
                this.natives.setPaintFullLayers(config, enabled);
                this.natives.setDeveloperMode(config, enabled);
            }
        });
    }

    @Test
    void acceptsEveryDoubleSetter()
    {
        long config = this.createConfig();

        assertDoesNotThrow(() ->
        {
            this.natives.setFontGamma(config, 1.8);
            this.natives.setFontContrast(config, 0.5);
            this.natives.setFontStemDarkening(config, 0.25);
            this.natives.setFontEmbolden(config, 0.25);
            this.natives.setPageSettleDelay(config, 0.1);
            this.natives.setPageSettleTick(config, 0.016);
            this.natives.setRecycleDelay(config, 4.0);
            this.natives.setIdleGCIdleTime(config, 5.0);
            this.natives.setMaxUpdateTime(config, 0.008);
        });
    }

    @Test
    void acceptsEveryUnsignedIntegerSetter()
    {
        long config = this.createConfig();

        assertDoesNotThrow(() ->
        {
            this.natives.setPhotonTextMinPx(config, 12);
            this.natives.setPageSettleCycles(config, 2);
            this.natives.setMemoryCacheSize(config, 64 * 1024 * 1024);
            this.natives.setPageCacheSize(config, 8);
            this.natives.setOverrideRAMSize(config, 1024 * 1024 * 1024);
            this.natives.setMinLargeHeapSize(config, 32 * 1024 * 1024);
            this.natives.setMinSmallHeapSize(config, 1024 * 1024);
            this.natives.setMaxHeapSize(config, 512 * 1024 * 1024);
            this.natives.setNumRendererThreads(config, 1);
            this.natives.setBitmapRowAlignment(config, 16);
            this.natives.setResourceSampleInterval(config, 100);
        });
    }

    @Test
    void aSetterCanBeCalledRepeatedlyOnTheSameConfig()
    {
        long config = this.createConfig();

        assertDoesNotThrow(() ->
        {
            for (int i = 0; i < 100; i++)
                this.natives.setBitmapRowAlignment(config, i % 2 == 0 ? 0 : 16);
        });
    }

    @Test
    void manyConfigsCanBeCreatedAndDestroyed()
    {
        for (int i = 0; i < 100; i++)
        {
            long config = this.natives.create();

            try
            {
                assertNotEquals(0L, config);

                this.natives.setBitmapRowAlignment(config, 16);
            }
            finally
            {
                this.natives.destroy(config);
            }
        }
    }

    @Override
    protected ULConfigNatives selectNatives(NativeBackend nativeBackend)
    {
        return nativeBackend.ulConfig();
    }

    private long createConfig()
    {
        return this.track(this.natives.create(), this.natives::destroy);
    }

    private long createString(String text)
    {
        ULStringNatives stringNatives = this.backend().ulString();

        return this.track(stringNatives.create(text), stringNatives::destroy);
    }
}
