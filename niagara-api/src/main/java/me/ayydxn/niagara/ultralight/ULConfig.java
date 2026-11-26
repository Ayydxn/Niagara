package me.ayydxn.niagara.ultralight;

import me.ayydxn.niagara.core.backend.NativeBackendManager;
import me.ayydxn.niagara.core.natives.ULConfigNatives;
import me.ayydxn.niagara.ultralight.config.*;

public final class ULConfig implements AutoCloseable
{
    private static final ULConfigNatives NATIVES = NativeBackendManager.getCurrentBackendInstance().ulConfig();

    private long handle;

    public ULConfig()
    {
        this.handle = NATIVES.create();
    }

    public void setCachePath(String cachePath)
    {
        try (ULString cachePathString = ULString.create(cachePath))
        {
            NATIVES.setCachePath(this.handle, cachePathString.getHandle());
        }
    }

    public void setResourcePathPrefix(String resourcePathPrefix)
    {
        try (ULString resourcePathPrefixString = ULString.create(resourcePathPrefix))
        {
            NATIVES.setResourcePathPrefix(this.handle, resourcePathPrefixString.getHandle());
        }
    }

    public void setFaceWinding(ULFaceWinding faceWinding)
    {
        NATIVES.setFaceWinding(this.handle, faceWinding.getNativeValue());
    }

    public void setFontHinting(ULFontHinting fontHinting)
    {
        NATIVES.setFontHinting(this.handle, fontHinting.getNativeValue());
    }

    public void setFontProfile(ULFontProfile fontProfile)
    {
        NATIVES.setFontProfile(this.handle, fontProfile.getNativeValue());
    }

    public void setFontBlendMode(ULFontBlendMode fontBlendMode)
    {
        NATIVES.setFontBlendMode(this.handle, fontBlendMode.getNativeValue());
    }

    public void setFontGamma(double fontGamma)
    {
        NATIVES.setFontGamma(this.handle, fontGamma);
    }

    public void setFontContrast(double fontContrast)
    {
        NATIVES.setFontContrast(this.handle, fontContrast);
    }

    public void setFontStemDarkening(double fontStemDarkening)
    {
        NATIVES.setFontStemDarkening(this.handle, fontStemDarkening);
    }

    public void setFontEmbolden(double fontEmbolden)
    {
        NATIVES.setFontEmbolden(this.handle, fontEmbolden);
    }

    public void setPixelFontRendering(ULPixelFontRendering pixelFontRendering)
    {
        NATIVES.setPixelFontRendering(this.handle, pixelFontRendering.getNativeValue());
    }

    public void setSnapPixelFontSize(boolean enabled)
    {
        NATIVES.setSnapPixelFontSize(this.handle, enabled);
    }

    public void setUserStylesheet(String cssString)
    {
        try (ULString cssULString = ULString.create(cssString))
        {
            NATIVES.setUserStylesheet(this.handle, cssULString.getHandle());
        }
    }

    public void setForceRepaint(boolean enabled)
    {
        NATIVES.setForceRepaint(this.handle, enabled);
    }

    public void setEnablePhoton(boolean enabled)
    {
        NATIVES.setEnablePhoton(this.handle, enabled);
    }

    public void setEnablePhotonText(boolean enabled)
    {
        NATIVES.setEnablePhotonText(this.handle, enabled);
    }

    public void setPhotonTextMinPx(int minPx)
    {
        NATIVES.setPhotonTextMinPx(this.handle, minPx);
    }

    public void setPageSettleDelay(double delay)
    {
        NATIVES.setPageSettleDelay(this.handle, delay);
    }

    public void setPageSettleCycles(int cycles)
    {
        NATIVES.setPageSettleCycles(this.handle, cycles);
    }

    public void setPageSettleTick(double tick)
    {
        NATIVES.setPageSettleTick(this.handle, tick);
    }

    public void setRecycleDelay(double delay)
    {
        NATIVES.setRecycleDelay(this.handle, delay);
    }

    public void setIdleGCEnabled(boolean enabled)
    {
        NATIVES.setIdleGCEnabled(this.handle, enabled);
    }

    public void setIdleGCIdleTime(double idleTime)
    {
        NATIVES.setIdleGCIdleTime(this.handle, idleTime);
    }

    public void setMemoryCacheSize(int size)
    {
        NATIVES.setMemoryCacheSize(this.handle, size);
    }

    public void setPageCacheSize(int size)
    {
        NATIVES.setPageCacheSize(this.handle, size);
    }

    public void setMemoryProfile(ULMemoryProfile memoryProfile)
    {
        NATIVES.setMemoryProfile(this.handle, memoryProfile.getNativeValue());
    }

    public void setOverrideRAMSize(int size)
    {
        NATIVES.setOverrideRAMSize(this.handle, size);
    }

    public void setMinLargeHeapSize(int size)
    {
        NATIVES.setMinLargeHeapSize(this.handle, size);
    }

    public void setMinSmallHeapSize(int size)
    {
        NATIVES.setMinSmallHeapSize(this.handle, size);
    }

    public void setMaxHeapSize(int size)
    {
        NATIVES.setMaxHeapSize(this.handle, size);
    }

    public void setNumRendererThreads(int numRendererThreads)
    {
        NATIVES.setNumRendererThreads(this.handle, numRendererThreads);
    }

    public void setMaxUpdateTime(double maxUpdateTime)
    {
        NATIVES.setMaxUpdateTime(this.handle, maxUpdateTime);
    }

    public void setBitmapRowAlignment(int bitmapRowAlignment)
    {
        NATIVES.setBitmapRowAlignment(this.handle, bitmapRowAlignment);
    }

    public void setEffectQuality(ULEffectQuality quality)
    {
        NATIVES.setEffectQuality(this.handle, quality.getNativeValue());
    }

    public void setIgnoreSSLErrors(boolean enabled)
    {
        NATIVES.setIgnoreSSLErrors(this.handle, enabled);
    }

    public void setUseSystemCertificateStore(boolean enabled)
    {
        NATIVES.setUseSystemCertificateStore(this.handle, enabled);
    }

    public void setPaintFullLayers(boolean enabled)
    {
        NATIVES.setPaintFullLayers(this.handle, enabled);
    }

    public void setResourceSampleInterval(int intervalMs)
    {
        NATIVES.setResourceSampleInterval(this.handle, intervalMs);
    }

    public void setDeveloperMode(boolean developerMode)
    {
        NATIVES.setDeveloperMode(this.handle, developerMode);
    }

    public void setJavaScriptDiagnostics(ULDiagnosticsLevel diagnosticsLevel)
    {
        NATIVES.setJavaScriptDiagnostics(this.handle, diagnosticsLevel.getNativeValue());
    }

    public void setDOMDiagnostics(ULDiagnosticsLevel diagnosticsLevel)
    {
        NATIVES.setDOMDiagnostics(this.handle, diagnosticsLevel.getNativeValue());
    }

    public void setMinLogLevel(ULLogLevel logLevel)
    {
        NATIVES.setMinLogLevel(this.handle, logLevel.getNativeValue());
    }

    @Override
    public void close()
    {
        if (this.handle != 0)
        {
            NATIVES.destroy(this.handle);

            this.handle = 0;
        }
    }
}
