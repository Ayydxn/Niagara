package me.ayydxn.niagara.ffm;

import me.ayydxn.niagara.core.backend.NativeBackend;
import me.ayydxn.niagara.core.backend.NativeBackendType;
import me.ayydxn.niagara.core.loader.NativeLibraries;
import me.ayydxn.niagara.core.loader.NativeLibraryLoadException;
import me.ayydxn.niagara.core.loader.NativeLibraryLoadStatus.FailureReason;
import me.ayydxn.niagara.core.loader.NativeLibraryPlatform;
import me.ayydxn.niagara.core.loader.NativeLibraryProvider;

import java.lang.foreign.Arena;
import java.lang.foreign.SymbolLookup;
import java.nio.file.Path;

public final class FFMNativeLibraryProvider implements NativeLibraryProvider
{
    private static volatile SymbolLookup symbolLookup;

    @Override
    public NativeBackend createNativeBackend()
    {
        return new FFMNativeBackend();
    }

    @Override
    public void load(NativeLibraryPlatform nativeLibraryPlatform, NativeLibraries nativeLibraries)
    {
        SymbolLookup combinedSymbolLookup = null;

        for (Path library : nativeLibraries.files())
        {
            SymbolLookup lookup = FFMNativeLibraryProvider.lookup(library);

            combinedSymbolLookup = combinedSymbolLookup == null ? lookup : combinedSymbolLookup.or(lookup);
        }

        symbolLookup = combinedSymbolLookup;
    }

    @Override
    public NativeBackendType getType()
    {
        return NativeBackendType.FFM;
    }

    public static SymbolLookup getSymbolLookup()
    {
        SymbolLookup lookup = symbolLookup;
        if (lookup == null)
            throw new IllegalStateException("Niagara's native libraries have not been loaded. Call Niagara.load first.");

        return lookup;
    }

    private static SymbolLookup lookup(Path library)
    {
        try
        {
            return SymbolLookup.libraryLookup(library, Arena.global());
        }
        catch (IllegalArgumentException | IllegalCallerException exception)
        {
            throw new NativeLibraryLoadException(FailureReason.NATIVE_LOAD_FAILED,
                    "Failed to load native library " + library.getFileName(), exception);
        }
    }
}
