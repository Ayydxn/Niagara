package me.ayydxn.niagara.core.annotations;

import java.lang.annotation.*;

/**
 * On a method: a returned {@code String} that is a {@code const char*} owned by the library.
 * On a parameter: a {@code String} passed to the library as a UTF-8 {@code const char*} for the duration of the call.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface NativeString
{
}
