#pragma once

#include <jni.h>

#include <string>

typedef struct C_String* ULString;

class CJNIUtils
{
public:
    template <typename Function>
    static JNINativeMethod MakeNativeMethod(const char* Name, const char* Signature, Function BridgeFunction)
    {
        JNINativeMethod NativeMethod;
        NativeMethod.name = const_cast<char*>(Name);
        NativeMethod.signature = const_cast<char*>(Signature);
        NativeMethod.fnPtr = reinterpret_cast<void*>(BridgeFunction);

        return NativeMethod;
    }

    static void ThrowJavaException(JNIEnv* Environment, const char* ExceptionClassName, const std::string& Message);

    // Converts Ultralight's UTF-8 string to a Java string. The result is a local reference, or nullptr if the string is null.
    // The whole string is converted (so embedded NULs survive), and malformed UTF-8 becomes U+FFFD instead of being passed to the JVM as-is.
    static jstring ToJavaString(JNIEnv* Environment, ULString String);

    // Creates a new ULString from a Java string. The caller owns the result and has to ulDestroyString it, unless Ultralight takes ownership of it (e.g. when returning it from a callback).
    // A null Java string gives an empty ULString.
    static ULString ToULString(JNIEnv* Environment, jstring JavaString);

    // Converts a non-null Java string to standard UTF-8 (not JNI's "modified" UTF-8). A lone surrogate becomes '?', which is what String.getBytes(UTF_8) does.
    static std::string ToUTF8(JNIEnv* Environment, jstring JavaString);
};
