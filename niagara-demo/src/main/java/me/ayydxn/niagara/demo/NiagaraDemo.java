package me.ayydxn.niagara.demo;

import me.ayydxn.niagara.Niagara;
import me.ayydxn.niagara.ultralight.ULPlatform;
import me.ayydxn.niagara.ultralight.Ultralight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Objects;

public class NiagaraDemo
{
    private static final Logger LOGGER = LoggerFactory.getLogger("Niagara Demo");

    private void start() throws Exception
    {
        LOGGER.info("Starting Niagara Demo Application... (Selected Native Backend: {})", System.getProperty("niagara.nativeBackend").toUpperCase());

        Niagara.load();

        LOGGER.info("Ultralight Version: {}", Ultralight.versionString());
        LOGGER.info("WebKit Version: {}", Ultralight.webKitVersionString());

        URL ultralightResourcesURL = Objects.requireNonNull(NiagaraDemo.class.getClassLoader().getResource("./ultralight"));
        File ultralightResourcesFolder = new File(ultralightResourcesURL.toURI());

        ULPlatform.enablePlatformFontLoader();
        ULPlatform.enablePlatformFileSystem(ultralightResourcesFolder + "/");
    }

    public static void main(String[] args) throws Exception
    {
        new NiagaraDemo().start();
    }
}
