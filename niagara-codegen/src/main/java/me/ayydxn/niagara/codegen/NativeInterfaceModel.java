package me.ayydxn.niagara.codegen;

import me.ayydxn.niagara.core.backend.NativeBackendType;

import java.util.List;
import java.util.Set;

/**
 * What the generator knows about a native interface after reading it. Nothing in here is specific to a backend.
 */
public final class NativeInterfaceModel
{
    public static final String CONTRACT_SUFFIX = "Natives";

    private NativeInterfaceModel()
    {
    }

    public record Type(Kind kind, String pointerType, String nativeType, boolean ulString, boolean ownedULString, boolean isNativeString)
    {
    }

    public record Param(String javaName, Type type)
    {
    }

    public record Function(String javaName, String cName, Set<NativeBackendType> custom, Type returnType, List<Param> params)
    {
        public boolean isCustom(NativeBackendType backendType)
        {
            return this.custom.contains(backendType);
        }
    }

    public record Contract(Class<?> type, List<String> headers, List<Function> functions)
    {
        public String name()
        {
            return this.type.getSimpleName();
        }

        public String baseName()
        {
            return name().substring(0, name().length() - CONTRACT_SUFFIX.length());
        }
    }

    public enum Kind
    {
        VOID("void"),
        INT("int"),
        LONG("long"),
        BOOLEAN("boolean"),
        STRING("String");

        public final String javaType;

        Kind(String javaType)
        {
            this.javaType = javaType;
        }
    }
}
