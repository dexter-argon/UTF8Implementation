package com.utf8;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class UTF8EncodingTest {

    @Test
    @DisplayName("Test ASCII characters (1-byte UTF-8)")
    public void testASCIICharacters() {
        // ASCII characters should be encoded as single bytes
        assertEquals(1, getUTF8ByteLength('A'));
        assertEquals(1, getUTF8ByteLength('z'));
        assertEquals(1, getUTF8ByteLength('0'));
        assertEquals(1, getUTF8ByteLength(' '));
        assertEquals(1, getUTF8ByteLength('!'));

        // Test actual byte encoding for ASCII
        assertArrayEquals(new byte[]{0x41}, encodeCharToUTF8('A'));
        assertArrayEquals(new byte[]{0x7A}, encodeCharToUTF8('z'));
        assertArrayEquals(new byte[]{0x30}, encodeCharToUTF8('0'));
    }

    @Test
    @DisplayName("Test Latin-1 Supplement characters (2-byte UTF-8)")
    public void testLatin1SupplementCharacters() {
        // Characters in range U+0080 to U+07FF should be 2 bytes
        assertEquals(2, getUTF8ByteLength('ñ')); // U+00F1
        assertEquals(2, getUTF8ByteLength('ü')); // U+00FC
        assertEquals(2, getUTF8ByteLength('é')); // U+00E9
        assertEquals(2, getUTF8ByteLength('©')); // U+00A9

        // Test actual byte encoding
        assertArrayEquals(new byte[]{(byte)0xC3, (byte)0xB1}, encodeCharToUTF8('ñ')); // ñ
        assertArrayEquals(new byte[]{(byte)0xC3, (byte)0xA9}, encodeCharToUTF8('©')); // ©
    }

    @Test
    @DisplayName("Test Basic Multilingual Plane characters (3-byte UTF-8)")
    public void testBMPCharacters() {
        // Characters in range U+0800 to U+FFFF should be 3 bytes
        assertEquals(3, getUTF8ByteLength('€')); // U+20AC Euro sign
        assertEquals(3, getUTF8ByteLength('中')); // U+4E2D Chinese character
        assertEquals(3, getUTF8ByteLength('日')); // U+65E5 Japanese character
        assertEquals(3, getUTF8ByteLength('한')); // U+D55C Korean character

        // Test actual byte encoding
        assertArrayEquals(new byte[]{(byte)0xE2, (byte)0x82, (byte)0xAC}, encodeCharToUTF8('€')); // €
    }

    @Test
    @DisplayName("Test Supplementary Plane characters (4-byte UTF-8)")
    public void testSupplementaryPlaneCharacters() {
        // Characters above U+FFFF should be 4 bytes (surrogate pairs in Java)
        String emoji = "😀"; // U+1F600 Grinning face
        String musical = "𝄞"; // U+1D11E Musical symbol treble clef

        assertEquals(4, getUTF8ByteLength(emoji.codePointAt(0)));
        assertEquals(4, getUTF8ByteLength(musical.codePointAt(0)));

        // Test actual byte encoding
        assertArrayEquals(new byte[]{(byte)0xF0, (byte)0x9F, (byte)0x98, (byte)0x80},
                         encodeCodePointToUTF8(0x1F600)); // 😀
    }

    @Test
    @DisplayName("Test string encoding and decoding")
    public void testStringEncodingDecoding() {
        String[] testStrings = {
            "Hello World",           // ASCII only
            "Café naïve résumé",     // Latin-1 supplement
            "中文测试",               // Chinese characters
            "Hello 世界 🌍",         // Mixed ASCII, Chinese, and emoji
            "Iñtërnâtiônàlizætiøn",  // International characters
            ""                       // Empty string
        };

        for (String original : testStrings) {
            byte[] encoded = encodeStringToUTF8(original);
            String decoded = decodeUTF8ToString(encoded);
            assertEquals(original, decoded, "Failed for string: " + original);
        }
    }

    @Test
    @DisplayName("Test byte sequence validation")
    public void testByteSequenceValidation() {
        // Valid UTF-8 sequences
        assertTrue(isValidUTF8(new byte[]{0x41})); // 'A'
        assertTrue(isValidUTF8(new byte[]{(byte)0xC3, (byte)0xA9})); // 'é'
        assertTrue(isValidUTF8(new byte[]{(byte)0xE2, (byte)0x82, (byte)0xAC})); // '€'
        assertTrue(isValidUTF8(new byte[]{(byte)0xF0, (byte)0x9F, (byte)0x98, (byte)0x80})); // '😀'

        // Invalid UTF-8 sequences
        assertFalse(isValidUTF8(new byte[]{(byte)0xFF})); // Invalid start byte
        assertFalse(isValidUTF8(new byte[]{(byte)0xC3})); // Incomplete sequence
        assertFalse(isValidUTF8(new byte[]{(byte)0xC0, (byte)0x80})); // Overlong encoding
        assertFalse(isValidUTF8(new byte[]{(byte)0x80})); // Continuation byte without start
    }

    @Test
    @DisplayName("Test boundary conditions")
    public void testBoundaryConditions() {
        // Test boundary characters
        assertEquals(1, getUTF8ByteLength(0x7F));   // Last 1-byte character
        assertEquals(2, getUTF8ByteLength(0x80));   // First 2-byte character
        assertEquals(2, getUTF8ByteLength(0x7FF));  // Last 2-byte character
        assertEquals(3, getUTF8ByteLength(0x800));  // First 3-byte character
        assertEquals(3, getUTF8ByteLength(0xFFFF)); // Last 3-byte character
        assertEquals(4, getUTF8ByteLength(0x10000)); // First 4-byte character
        assertEquals(4, getUTF8ByteLength(0x10FFFF)); // Last valid Unicode character
    }

    @Test
    @DisplayName("Test error handling")
    public void testErrorHandling() {
        // Test invalid code points
        assertThrows(IllegalArgumentException.class, () ->
            encodeCodePointToUTF8(0x110000)); // Beyond valid Unicode range

        assertThrows(IllegalArgumentException.class, () ->
            encodeCodePointToUTF8(-1)); // Negative code point

        // Test invalid UTF-8 decoding
        assertThrows(IllegalArgumentException.class, () ->
            decodeUTF8ToString(new byte[]{(byte)0xFF, (byte)0xFF}));
    }

    // Methods to implement:

    /**
     * Returns the number of bytes required to encode the given character in UTF-8
     */
    private int getUTF8ByteLength(char c) {
        return getUTF8ByteLength((int) c);
    }

    /**
     * Returns the number of bytes required to encode the given code point in UTF-8
     */
    private int getUTF8ByteLength(int codePoint) {
        // TODO: Implement this method
        // Hint: UTF-8 encoding rules:
        // - 0x0000-0x007F: 1 byte
        // - 0x0080-0x07FF: 2 bytes
        // - 0x0800-0xFFFF: 3 bytes
        // - 0x10000-0x10FFFF: 4 bytes
        throw new UnsupportedOperationException("Implement this method");
    }

    /**
     * Encodes a single character to UTF-8 bytes
     */
    private byte[] encodeCharToUTF8(char c) {
        // TODO: Implement this method
        throw new UnsupportedOperationException("Implement this method");
    }

    /**
     * Encodes a code point to UTF-8 bytes
     */
    private byte[] encodeCodePointToUTF8(int codePoint) {
        // TODO: Implement this method
        // Hint: You'll need to handle the bit manipulation for UTF-8 encoding
        throw new UnsupportedOperationException("Implement this method");
    }

    /**
     * Encodes a string to UTF-8 bytes
     */
    private byte[] encodeStringToUTF8(String str) {
        // TODO: Implement this method
        // Hint: Process each code point in the string
        throw new UnsupportedOperationException("Implement this method");
    }

    /**
     * Decodes UTF-8 bytes back to a string
     */
    private String decodeUTF8ToString(byte[] bytes) {
        // TODO: Implement this method
        // Hint: You'll need to parse the UTF-8 byte sequences
        throw new UnsupportedOperationException("Implement this method");
    }

    /**
     * Validates if a byte array contains valid UTF-8 sequences
     */
    private boolean isValidUTF8(byte[] bytes) {
        // TODO: Implement this method
        // Hint: Check for proper UTF-8 sequence patterns
        throw new UnsupportedOperationException("Implement this method");
    }
}