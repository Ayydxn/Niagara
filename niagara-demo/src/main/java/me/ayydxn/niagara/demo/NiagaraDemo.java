package me.ayydxn.niagara.demo;

import me.ayydxn.niagara.Niagara;
import me.ayydxn.niagara.demo.core.Window;
import me.ayydxn.niagara.ultralight.ULConfig;
import me.ayydxn.niagara.ultralight.ULPlatform;
import me.ayydxn.niagara.ultralight.Ultralight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Objects;

import static org.lwjgl.sdl.SDLInit.*;

public class NiagaraDemo
{
    private static final Logger LOGGER = LoggerFactory.getLogger("Niagara Demo");

    private static boolean isRunning = true;

    private Window window;

    private void initialize() throws Exception
    {
        String nativeBackendString = System.getProperty("niagara.nativeBackend").toUpperCase();

        LOGGER.info("Starting Niagara Demo Application... (Selected Native Backend: {})", nativeBackendString);

        Niagara.load();

        LOGGER.info("Ultralight Version: {}", Ultralight.versionString());
        LOGGER.info("WebKit Version: {}", Ultralight.webKitVersionString());

        // Initialize SDL and setup window
        if (!SDL_Init(SDL_INIT_VIDEO))
            throw new IllegalStateException("Failed to initialize SDL!");

        this.window = new Window(String.format("Niagara Demo App (Native Backend: %s)", nativeBackendString), 1600, 900);
        this.window.create();
        this.window.setOnWindowCloseCallback(() -> isRunning = false);

        // Setup Ultralight
        URL ultralightResourcesURL = Objects.requireNonNull(NiagaraDemo.class.getClassLoader().getResource("./ultralight"));
        File ultralightResourcesFolder = new File(ultralightResourcesURL.toURI());

        ULPlatform.enablePlatformFontLoader();
        ULPlatform.enablePlatformFileSystem(ultralightResourcesFolder + "/");

        try (ULConfig config = new ULConfig())
        {
            config.setResourcePathPrefix(ultralightResourcesFolder + "/");
            config.setUserStylesheet("body { background: white; }");

            // TODO: (Ayydxn) Create ULRenderer
        }
    }

    private void update()
    {
        this.window.update();
    }

    private void render()
    {
        this.window.swapBuffers();
    }

    private void shutdown()
    {
        LOGGER.info("Shutting down....");

        this.window.destroy();

        SDL_Quit();
    }

    public static void main(String[] args) throws Exception
    {
        NiagaraDemo niagaraDemo = new NiagaraDemo();
        niagaraDemo.initialize();

        while (isRunning)
        {
            niagaraDemo.update();
            niagaraDemo.render();
        }

        niagaraDemo.shutdown();
    }
}
