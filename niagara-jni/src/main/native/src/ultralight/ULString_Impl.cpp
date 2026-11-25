#include "utils/JNIUtils.h"

#include <Ultralight/CAPI/CAPI_String.h>

#include <locale>
#include <string>

jlong JNICALL ULCreateString_Native(JNIEnv* Environment, jclass, jstring String)
{
    if (!String)
    {
        CJNIUtils::ThrowJavaException(Environment, "java/lang/NullPointerException", "The Java string must not be null!");
        return 0;
    }

    // ulCreateString takes a C string, so (just like the FFM backend) this stops at an embedded NUL character.
    const std::string UTF8String = CJNIUtils::ToUTF8(Environment, String);

    return reinterpret_cast<jlong>(ulCreateString(UTF8String.c_str()));
}

jlong JNICALL ULCreateStringUTF8_Native(JNIEnv* Environment, jclass, jstring String, jlong Length)
{
    if (!String)
    {
        CJNIUtils::ThrowJavaException(Environment, "java/lang/NullPointerException", "string");
        return 0;
    }

    const std::string UTF8String = CJNIUtils::ToUTF8(Environment, String);
    if (Length < 0 || static_cast<size_t>(Length) > UTF8String.size())
    {
        CJNIUtils::ThrowJavaException(Environment, "java/lang/IllegalArgumentException",
            "length (" + std::to_string(Length) + ") must be between 0 and the UTF-8 size of the string (" + std::to_string(UTF8String.size()) + ")");

        return 0;
    }

    return reinterpret_cast<jlong>(ulCreateStringUTF8(UTF8String.data(), static_cast<size_t>(Length)));
}

void JNICALL ULStringAssignCString_Native(JNIEnv* Environment, jclass, jlong String, jstring NewString)
{
    const auto TargetString = reinterpret_cast<ULString>(String);
    if (!TargetString)
    {
        CJNIUtils::ThrowJavaException(Environment, "java/lang/IllegalArgumentException", "The ULString handle must not be 0!");
        return;
    }

    if (!NewString)
    {
        CJNIUtils::ThrowJavaException(Environment, "java/lang/NullPointerException", "The new Java string to assign the ULString to assign must not be null!");
        return;
    }

    const std::string UTF8String = CJNIUtils::ToUTF8(Environment, NewString);

    ulStringAssignCString(TargetString, UTF8String.c_str());
}

jstring JNICALL ULStringGetData_Native(JNIEnv* Environment, jclass, jlong Handle)
{
    // A null handle gives null, and everything else is read using the string's own length.
    return CJNIUtils::ToJavaString(Environment, reinterpret_cast<ULString>(Handle));
}
