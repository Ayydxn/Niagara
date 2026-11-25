package me.ayydxn.niagara.core.natives;

import me.ayydxn.niagara.core.annotations.NativeFunction;
import me.ayydxn.niagara.core.annotations.NativeInterface;
import me.ayydxn.niagara.core.annotations.NativePointer;
import me.ayydxn.niagara.core.annotations.NativeType;

@NativeInterface(headers = { "Ultralight/CAPI/CAPI_Config.h" })
public interface ULConfigNatives
{
    @NativePointer("ULConfig")
    @NativeFunction("ulCreateConfig")
    long create();

    @NativeFunction("ulDestroyConfig")
    void destroy(@NativePointer("ULConfig") long handle);

    @NativeFunction("ulConfigSetCachePath")
    void setCachePath(@NativePointer("ULConfig") long handle, @NativePointer("ULString") long cachePath);

    @NativeFunction("ulConfigSetResourcePathPrefix")
    void setResourcePathPrefix(@NativePointer("ULConfig") long handle, @NativePointer("ULString") long resourcePathPrefix);

    @NativeFunction("ulConfigSetFaceWinding")
    void setFaceWinding(@NativePointer("ULConfig") long handle, @NativeType("ULFaceWinding") int winding);

    @NativeFunction("ulConfigSetFontHinting")
    void setFontHinting(@NativePointer("ULConfig") long handle, @NativeType("ULFontHinting") int fontHinting);

    @NativeFunction("ulConfigSetFontProfile")
    void setFontProfile(@NativePointer("ULConfig") long handle, @NativeType("ULFontProfile") int fontProfile);

    @NativeFunction("ulConfigSetFontBlendMode")
    void setFontBlendMode(@NativePointer("ULConfig") long handle, @NativeType("ULFontBlendMode") int fontBlendMode);

    @NativeFunction("ulConfigSetFontGamma")
    void setFontGamma(@NativePointer("ULConfig") long handle, double fontGamma);

    @NativeFunction("ulConfigSetFontContrast")
    void setFontContrast(@NativePointer("ULConfig") long handle, double fontContrast);

    @NativeFunction("ulConfigSetFontStemDarkening")
    void setFontStemDarkening(@NativePointer("ULConfig") long handle, double fontStemDarkening);

    @NativeFunction("ulConfigSetFontEmbolden")
    void setFontEmbolden(@NativePointer("ULConfig") long handle, double fontEmbolden);

    @NativeFunction("ulConfigSetPixelFontRendering")
    void setPixelFontRendering(@NativePointer("ULConfig") long handle, @NativeType("ULPixelFontRendering") int mode);

    @NativeFunction("ulConfigSetSnapPixelFontSize")
    void setSnapPixelFontSize(@NativePointer("ULConfig") long handle, boolean enabled);

    @NativeFunction("ulConfigSetUserStylesheet")
    void setUserStylesheet(@NativePointer("ULConfig") long handle, @NativePointer("ULString") long cssString);

    @NativeFunction("ulConfigSetForceRepaint")
    void setForceRepaint(@NativePointer("ULConfig") long handle, boolean enabled);

    @NativeFunction("ulConfigSetEnablePhoton")
    void setEnablePhoton(@NativePointer("ULConfig") long handle, boolean enabled);

    @NativeFunction("ulConfigSetEnablePhotonText")
    void setEnablePhotonText(@NativePointer("ULConfig") long handle, boolean enabled);

    @NativeFunction("ulConfigSetPhotonTextMinPx")
    void setPhotonTextMinPx(@NativePointer("ULConfig") long handle, @NativeType("unsigned int") int minPx);

    @NativeFunction("ulConfigSetPageSettleDelay")
    void setPageSettleDelay(@NativePointer("ULConfig") long handle, double delay);

    @NativeFunction("ulConfigSetPageSettleCycles")
    void setPageSettleCycles(@NativePointer("ULConfig") long handle, @NativeType("unsigned int") int cycles);

    @NativeFunction("ulConfigSetPageSettleTick")
    void setPageSettleTick(@NativePointer("ULConfig") long handle, double tick);

    @NativeFunction("ulConfigSetRecycleDelay")
    void setRecycleDelay(@NativePointer("ULConfig") long handle, double delay);

    @NativeFunction("ulConfigSetIdleGCEnabled")
    void setIdleGCEnabled(@NativePointer("ULConfig") long handle, boolean enabled);

    @NativeFunction("ulConfigSetIdleGCIdleTime")
    void setIdleGCIdleTime(@NativePointer("ULConfig") long handle, double idleTime);

    @NativeFunction("ulConfigSetMemoryCacheSize")
    void setMemoryCacheSize(@NativePointer("ULConfig") long handle, @NativeType("unsigned int") int size);

    @NativeFunction("ulConfigSetPageCacheSize")
    void setPageCacheSize(@NativePointer("ULConfig") long handle, @NativeType("unsigned int") int size);

    @NativeFunction("ulConfigSetMemoryProfile")
    void setMemoryProfile(@NativePointer("ULConfig") long handle, @NativeType("ULMemoryProfile") int memoryProfile);

    @NativeFunction("ulConfigSetOverrideRAMSize")
    void setOverrideRAMSize(@NativePointer("ULConfig") long handle, @NativeType("unsigned int") int size);

    @NativeFunction("ulConfigSetMinLargeHeapSize")
    void setMinLargeHeapSize(@NativePointer("ULConfig") long handle, @NativeType("unsigned int") int size);

    @NativeFunction("ulConfigSetMinSmallHeapSize")
    void setMinSmallHeapSize(@NativePointer("ULConfig") long handle, @NativeType("unsigned int") int size);

    @NativeFunction("ulConfigSetMaxHeapSize")
    void setMaxHeapSize(@NativePointer("ULConfig") long handle, @NativeType("unsigned int") int size);

    @NativeFunction("ulConfigSetNumRendererThreads")
    void setNumRendererThreads(@NativePointer("ULConfig") long handle, @NativeType("unsigned int") int numRendererThreads);

    @NativeFunction("ulConfigSetMaxUpdateTime")
    void setMaxUpdateTime(@NativePointer("ULConfig") long handle, double maxUpdateTime);

    @NativeFunction("ulConfigSetBitmapRowAlignment")
    void setBitmapRowAlignment(@NativePointer("ULConfig") long handle, @NativeType("unsigned int") int bitmapRowAlignment);

    @NativeFunction("ulConfigSetEffectQuality")
    void setEffectQuality(@NativePointer("ULConfig") long handle, @NativeType("ULEffectQuality") int quality);

    @NativeFunction("ulConfigSetIgnoreSSLErrors")
    void setIgnoreSSLErrors(@NativePointer("ULConfig") long handle, boolean enabled);

    @NativeFunction("ulConfigSetUseSystemCertificateStore")
    void setUseSystemCertificateStore(@NativePointer("ULConfig") long handle, boolean enabled);

    @NativeFunction("ulConfigSetPaintFullLayers")
    void setPaintFullLayers(@NativePointer("ULConfig") long handle, boolean enabled);

    @NativeFunction("ulConfigSetResourceSampleInterval")
    void setResourceSampleInterval(@NativePointer("ULConfig") long handle, @NativeType("unsigned int") int intervalMs);

    @NativeFunction("ulConfigSetDeveloperMode")
    void setDeveloperMode(@NativePointer("ULConfig") long handle, boolean developerMode);

    @NativeFunction("ulConfigSetJavaScriptDiagnostics")
    void setJavaScriptDiagnostics(@NativePointer("ULConfig") long handle, @NativeType("ULDiagnosticsLevel") int level);

    @NativeFunction("ulConfigSetDOMDiagnostics")
    void setDOMDiagnostics(@NativePointer("ULConfig") long handle, @NativeType("ULDiagnosticsLevel") int level);

    @NativeFunction("ulConfigSetMinLogLevel")
    void setMinLogLevel(@NativePointer("ULConfig") long handle, @NativeType("ULLogLevel") int level);
}
