package me.ayydxn.niagara.core.annotations;

import java.lang.annotation.*;

/**
 * Marks an interface as a set of native functions. Everything else (class names, files, registration) is derived from it.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NativeInterface
{
    /**
     * C/C++ headers the generated code includes, relative to the include path. Only the JNI backend uses these.
     */
    String[] headers();
}
