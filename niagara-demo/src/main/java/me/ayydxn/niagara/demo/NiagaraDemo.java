package me.ayydxn.niagara.demo;

import me.ayydxn.niagara.Niagara;
import me.ayydxn.niagara.ultralight.Ultralight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NiagaraDemo
{
    private static final Logger LOGGER = LoggerFactory.getLogger("Niagara Demo");

    private void start()
    {
        LOGGER.info("Starting Niagara Demo Application... (Selected Native Backend: {})", System.getProperty("niagara.nativeBackend").toUpperCase());

        Niagara.load();

        LOGGER.info("Ultralight Version: {}", Ultralight.versionString());
        LOGGER.info("WebKit Version: {}", Ultralight.webKitVersionString());
    }

    public static void main(String[] args)
    {
        new NiagaraDemo().start();
    }
}
