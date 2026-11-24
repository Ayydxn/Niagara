package me.ayydxn.niagara.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NiagaraDemo
{
    private static final Logger LOGGER = LoggerFactory.getLogger("Niagara Demo");

    private void start()
    {
        LOGGER.info("Starting Niagara Demo Application... (Selected Native Backend: {})", System.getProperty("niagara.nativeBackend").toUpperCase());
    }

    public static void main(String[] args)
    {
        new NiagaraDemo().start();
    }
}
