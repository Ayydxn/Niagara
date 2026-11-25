package me.ayydxn.niagara.core.natives;

import me.ayydxn.niagara.core.backend.NativeBackend;
import me.ayydxn.niagara.core.test.NativeInterfaceTest;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public abstract class ULStringNativesTest extends NativeInterfaceTest<ULStringNatives>
{
    // Japanese, a globe emoji (a surrogate pair in UTF-16) and Arabic. Written as escapes so the source file's encoding can never matter.
    private static final String UNICODE = "\u3053\u3093\u306B\u3061\u306F \uD83C\uDF10 \u0645\u0631\u062D\u0628\u0627";

    @Test
    void createdStringHoldsTheGivenText()
    {
        long handle = this.create("Hello Niagara!");

        assertEquals("Hello Niagara!", this.natives.getData(handle));
        assertFalse(this.natives.isEmpty(handle));
        assertEquals(14, this.natives.getLength(handle));
    }

    @Test
    void emptyStringIsEmpty()
    {
        long handle = this.create("");

        assertTrue(this.natives.isEmpty(handle));
        assertEquals(0, this.natives.getLength(handle));
        assertEquals("", this.natives.getData(handle));
    }

    @Test
    void distinctStringsGetDistinctHandles()
    {
        long first = this.create("Same");
        long second = this.create("Same");

        assertEquals(this.natives.getData(first), this.natives.getData(second));
        assertNotEquals(first, second);
    }

    @Test
    void unicodeSurvivesARoundTrip()
    {
        long handle = this.create(UNICODE);

        assertEquals(UNICODE, this.natives.getData(handle));
    }

    @Test
    void lengthIsMeasuredInUtf8Bytes()
    {
        long handle = this.create(UNICODE);

        assertEquals(UNICODE.getBytes(StandardCharsets.UTF_8).length, this.natives.getLength(handle));
        assertNotEquals(UNICODE.length(), this.natives.getLength(handle));
    }

    @Test
    void longStringsAreNotTruncated()
    {
        String text = "a".repeat(100_000);
        long handle = this.create(text);

        assertEquals(text.length(), this.natives.getLength(handle));
        assertEquals(text, this.natives.getData(handle));
    }

    @Test
    void createUTF8UsesTheWholeStringWhenGivenItsFullLength()
    {
        long byteLength = UNICODE.getBytes(StandardCharsets.UTF_8).length;
        long handle = this.createUTF8(UNICODE, byteLength);

        assertEquals(UNICODE, this.natives.getData(handle));
        assertEquals(byteLength, this.natives.getLength(handle));
    }

    @Test
    void createUTF8StopsAtTheGivenLength()
    {
        long handle = this.createUTF8("Hello Niagara", 5);

        assertEquals("Hello", this.natives.getData(handle));
        assertEquals(5, this.natives.getLength(handle));
    }

    @Test
    void createUTF8WithAZeroLengthIsEmpty()
    {
        long handle = this.createUTF8("Hello Niagara", 0);

        assertTrue(this.natives.isEmpty(handle));
        assertEquals("", this.natives.getData(handle));
    }

    @Test
    void copyHasItsOwnHandleAndTheSameText()
    {
        long original = this.create(UNICODE);
        long copy = this.track(this.natives.createFromCopy(original), this.natives::destroy);

        assertNotEquals(original, copy);
        assertEquals(UNICODE, this.natives.getData(copy));
        assertEquals(this.natives.getLength(original), this.natives.getLength(copy));
    }

    @Test
    void changingTheOriginalDoesNotChangeItsCopy()
    {
        long original = this.create("Before");
        long copy = this.track(this.natives.createFromCopy(original), this.natives::destroy);

        this.natives.assignCString(original, "After");

        assertEquals("After", this.natives.getData(original));
        assertEquals("Before", this.natives.getData(copy));
    }

    @Test
    void copyOutlivesTheOriginal()
    {
        // Not tracked, since this test destroys the original itself and a second destroy would be a double free.
        long original = this.natives.create("Still here");
        long copy = this.track(this.natives.createFromCopy(original), this.natives::destroy);

        this.natives.destroy(original);

        assertEquals("Still here", this.natives.getData(copy));
    }

    @Test
    void assignStringReplacesTheTargetsText()
    {
        long target = this.create("Old text that is quite a bit longer");
        long source = this.create(UNICODE);

        this.natives.assignString(target, source);

        assertEquals(UNICODE, this.natives.getData(target));
        assertEquals(this.natives.getLength(source), this.natives.getLength(target));
        assertEquals(UNICODE, this.natives.getData(source));
    }

    @Test
    void assignCStringReplacesTheTargetsText()
    {
        long handle = this.create("Old");

        this.natives.assignCString(handle, "A much longer replacement");
        assertEquals("A much longer replacement", this.natives.getData(handle));

        this.natives.assignCString(handle, UNICODE);
        assertEquals(UNICODE, this.natives.getData(handle));
        assertEquals(UNICODE.getBytes(StandardCharsets.UTF_8).length, this.natives.getLength(handle));
    }

    @Test
    void assigningAnEmptyStringEmptiesTheTarget()
    {
        long handle = this.create("Not empty");

        this.natives.assignCString(handle, "");

        assertTrue(this.natives.isEmpty(handle));
        assertEquals("", this.natives.getData(handle));
    }

    @Test
    void dataOfANullHandleIsNull()
    {
        assertNull(this.natives.getData(0L));
    }

    @Test
    void manyStringsCanBeCreatedAndDestroyed()
    {
        for (int i = 0; i < 1000; i++)
        {
            long handle = this.natives.create("String " + i);

            try
            {
                assertEquals("String " + i, this.natives.getData(handle));
            }
            finally
            {
                this.natives.destroy(handle);
            }
        }
    }

    @Override
    protected ULStringNatives selectNatives(NativeBackend nativeBackend)
    {
        return nativeBackend.ulString();
    }

    private long create(String text)
    {
        return this.track(this.natives.create(text), this.natives::destroy);
    }

    private long createUTF8(String text, long length)
    {
        return this.track(this.natives.createUTF8(text, length), this.natives::destroy);
    }
}
