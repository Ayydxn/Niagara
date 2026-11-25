#include "JNIUtils.h"

#include <Ultralight/CAPI/CAPI_String.h>

#include <codecvt>
#include <cstddef>
#include <cstdint>
#include <locale>
#include <vector>

namespace
{
    constexpr jchar ReplacementCharacter = 0xFFFD;

    void AppendUTF8(std::string& Output, uint32_t CodePoint)
    {
        if (CodePoint < 0x80)
        {
            Output.push_back(static_cast<char>(CodePoint));
        }
        else if (CodePoint < 0x800)
        {
            Output.push_back(static_cast<char>(0xC0 | (CodePoint >> 6)));
            Output.push_back(static_cast<char>(0x80 | (CodePoint & 0x3F)));
        }
        else if (CodePoint < 0x10000)
        {
            Output.push_back(static_cast<char>(0xE0 | (CodePoint >> 12)));
            Output.push_back(static_cast<char>(0x80 | ((CodePoint >> 6) & 0x3F)));
            Output.push_back(static_cast<char>(0x80 | (CodePoint & 0x3F)));
        }
        else
        {
            Output.push_back(static_cast<char>(0xF0 | (CodePoint >> 18)));
            Output.push_back(static_cast<char>(0x80 | ((CodePoint >> 12) & 0x3F)));
            Output.push_back(static_cast<char>(0x80 | ((CodePoint >> 6) & 0x3F)));
            Output.push_back(static_cast<char>(0x80 | (CodePoint & 0x3F)));
        }
    }

    std::string EncodeUTF8(const jchar* Characters, jsize Length)
    {
        std::string Result;
        Result.reserve(static_cast<size_t>(Length));

        for (jsize Index = 0; Index < Length; ++Index)
        {
            uint32_t CodePoint = Characters[Index];

            if (CodePoint >= 0xD800 && CodePoint <= 0xDBFF)
            {
                const bool HasLowSurrogate = Index + 1 < Length && Characters[Index + 1] >= 0xDC00 && Characters[Index + 1] <= 0xDFFF;
                if (!HasLowSurrogate)
                {
                    Result.push_back('?');
                    continue;
                }

                CodePoint = 0x10000 + ((CodePoint - 0xD800) << 10) + (Characters[Index + 1] - 0xDC00);
                ++Index;
            }
            else if (CodePoint >= 0xDC00 && CodePoint <= 0xDFFF)
            {
                // A low surrogate with no high surrogate before it.
                Result.push_back('?');
                continue;
            }

            AppendUTF8(Result, CodePoint);
        }

        return Result;
    }

    // Every malformed sequence (the lead byte plus whichever continuation bytes before it were still valid) becomes a single U+FFFD.
    std::vector<jchar> DecodeUTF8(const char* Data, size_t Length)
    {
        std::vector<jchar> Result;
        Result.reserve(Length);

        size_t Index = 0;
        while (Index < Length)
        {
            const auto Lead = static_cast<uint8_t>(Data[Index]);
            if (Lead < 0x80)
            {
                Result.push_back(static_cast<jchar>(Lead));
                ++Index;

                continue;
            }

            size_t SequenceLength;
            uint32_t CodePoint;

            // The range the second byte has to be in, which excludes overlong encodings, surrogates and anything past U+10FFFF.
            uint8_t Lower = 0x80;
            uint8_t Upper = 0xBF;

            if (Lead >= 0xC2 && Lead <= 0xDF)
            {
                SequenceLength = 2;
                CodePoint = Lead & 0x1F;
            }
            else if (Lead >= 0xE0 && Lead <= 0xEF)
            {
                SequenceLength = 3;
                CodePoint = Lead & 0x0F;

                if (Lead == 0xE0)
                    Lower = 0xA0;
                else if (Lead == 0xED)
                    Upper = 0x9F;
            }
            else if (Lead >= 0xF0 && Lead <= 0xF4)
            {
                SequenceLength = 4;
                CodePoint = Lead & 0x07;

                if (Lead == 0xF0)
                    Lower = 0x90;
                else if (Lead == 0xF4)
                    Upper = 0x8F;
            }
            else
            {
                Result.push_back(ReplacementCharacter);
                ++Index;

                continue;
            }

            size_t Consumed = 1;
            bool IsValid = true;

            while (Consumed < SequenceLength)
            {
                if (Index + Consumed >= Length)
                {
                    IsValid = false;
                    break;
                }

                const auto Next = static_cast<uint8_t>(Data[Index + Consumed]);
                if (Next < Lower || Next > Upper)
                {
                    IsValid = false;
                    break;
                }

                CodePoint = (CodePoint << 6) | (Next & 0x3F);
                Lower = 0x80;
                Upper = 0xBF;

                ++Consumed;
            }

            if (!IsValid)
            {
                Result.push_back(ReplacementCharacter);
                Index += Consumed;

                continue;
            }

            if (CodePoint >= 0x10000)
            {
                CodePoint -= 0x10000;

                Result.push_back(static_cast<jchar>(0xD800 + (CodePoint >> 10)));
                Result.push_back(static_cast<jchar>(0xDC00 + (CodePoint & 0x3FF)));
            }
            else
            {
                Result.push_back(static_cast<jchar>(CodePoint));
            }

            Index += SequenceLength;
        }

        return Result;
    }
}

void CJNIUtils::ThrowJavaException(JNIEnv* Environment, const char* ExceptionClassName, const std::string& Message)
{
    const jclass ExceptionClass = Environment->FindClass(ExceptionClassName);
    if (!ExceptionClass)
        return; // FindClass has already left a NoClassDefFoundError pending.

    Environment->ThrowNew(ExceptionClass, Message.c_str());
    Environment->DeleteLocalRef(ExceptionClass);
}

jstring CJNIUtils::ToJavaString(JNIEnv* Environment, ULString String)
{
    if (!String)
        return nullptr;

    const char* Data = ulStringGetData(String);
    const size_t Length = ulStringGetLength(String);

    if (!Data || Length == 0)
    {
        constexpr jchar Empty = 0;

        return Environment->NewString(&Empty, 0);
    }

    const std::vector<jchar> Characters = DecodeUTF8(Data, Length);

    return Environment->NewString(Characters.data(), static_cast<jsize>(Characters.size()));
}

ULString CJNIUtils::ToULString(JNIEnv* Environment, jstring JavaString)
{
    if (!JavaString)
        return ulCreateString("");

    const std::string UTF8String = CJNIUtils::ToUTF8(Environment, JavaString);

    // Created with a length rather than as a C string, so an embedded NUL doesn't cut it short.
    return ulCreateStringUTF8(UTF8String.data(), UTF8String.size());
}

std::string CJNIUtils::ToUTF8(JNIEnv* Environment, jstring JavaString)
{
    const jsize Length = Environment->GetStringLength(JavaString);
    if (Length <= 0)
        return {};

    // GetStringRegion copies into our buffer, so there's nothing to release and no way to leak it on an early return.
    std::vector<jchar> Characters(static_cast<size_t>(Length));
    Environment->GetStringRegion(JavaString, 0, Length, Characters.data());

    return EncodeUTF8(Characters.data(), Length);
}
