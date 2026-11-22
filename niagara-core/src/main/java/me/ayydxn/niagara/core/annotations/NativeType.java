package me.ayydxn.niagara.core.annotations;

import java.lang.annotation.*;

/** The C type a numeric Java parameter is cast to (e.g. {@code unsigned int}, {@code ULEffectQuality}). Only the JNI backend uses this. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface NativeType
{
    String value();
}
