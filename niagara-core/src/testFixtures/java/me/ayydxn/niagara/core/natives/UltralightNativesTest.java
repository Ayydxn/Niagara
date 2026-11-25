package me.ayydxn.niagara.core.natives;

import me.ayydxn.niagara.core.backend.NativeBackend;
import me.ayydxn.niagara.core.test.NativeInterfaceTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class UltralightNativesTest extends NativeInterfaceTest<UltralightNatives>
{
    @Test
    void versionStringIsNotBlank()
    {
        String versionString = this.natives.versionString();

        assertNotNull(versionString);
        assertFalse(versionString.isBlank());
    }

    @Test
    void webKitVersionStringIsNotBlank()
    {
        String webKitVersionString = this.natives.webKitVersionString();

        assertNotNull(webKitVersionString);
        assertFalse(webKitVersionString.isBlank());
    }

    @Test
    void versionStringsContainDigits()
    {
        assertTrue(this.natives.versionString().chars().anyMatch(Character::isDigit));
        assertTrue(this.natives.webKitVersionString().chars().anyMatch(Character::isDigit));
    }

    // Both strings come from a C pointer, so this catches a stray terminator, a trailing newline or garbage past the end of the string.
    @Test
    void versionStringsAreCleanText()
    {
        for (String versionString : new String[] { this.natives.versionString(), this.natives.webKitVersionString() })
        {
            assertEquals(versionString.strip(), versionString);
            assertTrue(versionString.chars().noneMatch(Character::isISOControl));
        }
    }

    @Test
    void repeatedCallsReturnTheSameValue()
    {
        assertEquals(this.natives.versionString(), this.natives.versionString());
        assertEquals(this.natives.webKitVersionString(), this.natives.webKitVersionString());
    }

    // Ultralight and WebKit have different versions, so equal strings would mean one binding is wired to the other's function.
    @Test
    void ultralightAndWebKitVersionStringsAreDistinct()
    {
        assertNotEquals(this.natives.versionString(), this.natives.webKitVersionString());
    }

    @Test
    void versionNumbersAgreeWithTheVersionString()
    {
        int major = this.natives.versionMajor();
        int minor = this.natives.versionMinor();
        int patch = this.natives.versionPatch();

        assertTrue(major > 0, "Major version was " + major);
        assertTrue(minor >= 0, "Minor version was " + minor);
        assertTrue(patch >= 0, "Patch version was " + patch);

        // A pre-release suffix (e.g. "-beta.3") is fine, but the numbers themselves have to lead the string.
        String versionString = this.natives.versionString();
        String expectedPrefix = major + "." + minor + "." + patch;

        assertTrue(versionString.startsWith(expectedPrefix), "Expected \"" + versionString + "\" to start with " + expectedPrefix);
    }

    @Test
    void webKitVersionNumbersAreNotNegative()
    {
        assertNotNegative("ulWebKitVersionMajor", this.natives.webKitVersionMajor());
        assertNotNegative("ulWebKitVersionMinor", this.natives.webKitVersionMinor());
        assertNotNegative("ulWebKitVersionTiny", this.natives.webKitVersionTiny());
        assertNotNegative("ulWebKitVersionMicro", this.natives.webKitVersionMicro());
        assertNotNegative("ulWebKitVersionNano", this.natives.webKitVersionNano());
    }

    @Override
    protected UltralightNatives selectNatives(NativeBackend nativeBackend)
    {
        return nativeBackend.ultralight();
    }

    private static void assertNotNegative(String function, int value)
    {
        assertTrue(value >= 0, function + " returned " + value);
    }
}
