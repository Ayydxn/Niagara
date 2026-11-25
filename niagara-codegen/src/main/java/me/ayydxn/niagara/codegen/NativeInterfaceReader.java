package me.ayydxn.niagara.codegen;

import me.ayydxn.niagara.codegen.NativeInterfaceModel.*;
import me.ayydxn.niagara.core.annotations.*;
import me.ayydxn.niagara.core.backend.NativeBackendType;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;

public final class NativeInterfaceReader
{
    private NativeInterfaceReader()
    {
    }

    public static Contract read(Class<?> type)
    {
        NativeInterface annotation = type.getAnnotation(NativeInterface.class);
        require(annotation != null, type.getName() + " is missing @NativeInterface");
        require(type.isInterface(), type.getName() + " has @NativeInterface but is not an interface");
        require(type.getSimpleName().length() > NativeInterfaceModel.CONTRACT_SUFFIX.length() && type.getSimpleName().endsWith(NativeInterfaceModel.CONTRACT_SUFFIX),
                type.getName() + " has to be named <Something>" + NativeInterfaceModel.CONTRACT_SUFFIX);

        List<Function> functions = Arrays.stream(type.getMethods())
                .filter(m -> m.isAnnotationPresent(NativeFunction.class))
                .sorted(Comparator.comparing(m -> m.getAnnotation(NativeFunction.class).value()))
                .map(NativeInterfaceReader::function)
                .toList();

        return new Contract(type, List.of(annotation.headers()), functions);
    }

    private static Function function(Method method)
    {
        NativeFunction annotation = method.getAnnotation(NativeFunction.class);

        Set<NativeBackendType> custom = EnumSet.noneOf(NativeBackendType.class);
        custom.addAll(Arrays.asList(annotation.custom()));

        List<Param> params = Arrays.stream(method.getParameters())
                .map(p ->
                {
                    require(p.isNamePresent(), "Compile the contracts with -parameters");
                    return new Param(p.getName(), type(p.getType(), p, custom, method, false));
                })
                .toList();

        return new Function(method.getName(), annotation.value(), custom, type(method.getReturnType(), method, custom, method, true), params);
    }

    private static Type type(Class<?> javaType, AnnotatedElement source, Set<NativeBackendType> custom, Method owner, boolean isReturn)
    {
        Kind kind = javaType == void.class ? Kind.VOID
                : javaType == int.class ? Kind.INT
                : javaType == double.class ? Kind.DOUBLE
                : javaType == long.class ? Kind.LONG
                : javaType == boolean.class ? Kind.BOOLEAN
                : javaType == String.class ? Kind.STRING
                : null;

        require(kind != null, "Unsupported type " + javaType.getName() + " on " + owner);

        NativePointer nativePointer = source.getAnnotation(NativePointer.class);
        NativeType nativeType = source.getAnnotation(NativeType.class);
        UltralightString ulString = source.getAnnotation(UltralightString.class);
        boolean isNativeString = source.isAnnotationPresent(NativeString.class);

        require(nativePointer == null || kind == Kind.LONG, "@NativePointer needs a long on " + owner);
        require(!isNativeString || ulString == null, "@NativeString and @UltralightString can't be combined on " + owner);
        require((!isNativeString && ulString == null) || kind == Kind.STRING, "@NativeString and @UltralightString need a String on " + owner);

        // The generator never marshals a function that every backend writes by hand, so its strings don't need annotations.
        boolean generatedSomewhere = !custom.containsAll(EnumSet.allOf(NativeBackendType.class));
        require(kind != Kind.STRING || !generatedSomewhere || isNativeString || ulString != null,
                "String on " + owner + " needs " + (isReturn ? "@NativeString or @UltralightString" : "@NativeString")
                        + ", because at least one backend generates it (list every backend in custom to write it by hand)");

        return new Type(kind, nativePointer != null ? nativePointer.value() : null, nativeType != null ? nativeType.value() : null, ulString != null,
                ulString != null && ulString.owned(), isNativeString);
    }

    private static void require(boolean condition, String message)
    {
        if (!condition)
            throw new IllegalArgumentException(message);
    }
}
