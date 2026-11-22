package me.ayydxn.niagara.core.annotations;

import me.ayydxn.niagara.core.backend.NativeBackendType;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NativeFunction
{
    /** The C function this method calls. */
    String value();

    /** The backends whose implementation of this function are written by hand. Every other backend generates it. */
    NativeBackendType[] custom() default {};
}