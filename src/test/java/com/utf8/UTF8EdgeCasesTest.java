package com.utf8;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests focused on edge cases and error conditions in UTF-8 encoding
 */
public class UTF8EdgeCasesTest {

    @Test
    @DisplayName("Test surrogate pairs handling")
    public void testSurrogatePairs() {
        // In Java, characters above U+FFFF are represented as surrogate pairs
        // High surrogate: U+D800-U+DBFF, Low surrogate: U+DC00-U+DFFF

        String emoji = "😀"; // U+1F600, represented as surrogate pair in Java
        assertEquals(2, emoji.length()); // Java string length is 2 (surrogate pair)
        assertEquals(1, emoji.codePointCount(0, emoji.length())); // But only 1 code point

        // Test proper handling of surrogate pairs
        byte[] encoded = encodeStringToUTF8(emoji);
        assertEquals(4, encoded.length); // Should be 4 bytes in UTF-8

        String decoded = decodeUTF8ToString(encoded);
        assertEquals(emoji, decoded);
    }

    @Test
    @DisplayName("Test isolated surrogate handling")
    public void testIsolatedSurrogates() {
        // Isolated surrogates should be handled gracefully
        char highSurrogate = '\uD83D'; // High surrogate without pair
        char lowSurrogate = '\uDE00';  // Low surrogate without pair

        // These should either be rejected or handled with replacement characters
        assertThrows(IllegalArgumentException.class, () ->
            encodeCharToUTF8(highSurrogate));

        assertThrows(IllegalArgumentException.class, () ->
            encodeCharToUTF8(lowSurrogate));
    }

    @Test
    @DisplayName("Test byte order independence")
    public void testByteOrderIndependence() {
        // UTF-8 is byte-order independent (no BOM needed)
        String test = "Hello 世界";
        byte[] encoded1 = encodeStringToUTF8(test);
        byte[] encoded2 = encodeStringToUTF8(test);

        assertArrayEquals(encoded1, encoded2);

        // Should not start with BOM
        assertFalse(startsWithBOM(encoded1));
    }

    @Test
    @DisplayName("Test maximum code point")
    public void testMaximumCodePoint() {
        // Maximum valid Unicode code point is U+10FFFF
        int maxCodePoint = 0x10FFFF;
        byte[] encoded = encodeCodePointToUTF8(maxCodePoint);
        assertEquals(4, encoded.length);

        int decoded = decodeUTF8ToCodePoint(encoded, 0);
        assertEquals(maxCodePoint, decoded);

        // Beyond maximum should throw exception
        assertThrows(IllegalArgumentException.class, () ->
            encodeCodePointToUTF8(0x110000));
    }

    @Test
    @DisplayName("Test replacement character handling")
    public void testReplacementCharacter() {
        // U+FFFD is the replacement character for invalid sequences
        char replacement = '\uFFFD';
        byte[] encoded = encodeCharToUTF8(replacement);

        // Should be 3 bytes: EF BF BD
        assertArrayEquals(new byte[]{(byte)0xEF, (byte)0xBF, (byte)0xBD}, encoded);
    }

    @Test
    @DisplayName("Test incomplete sequences")
    public void testIncompleteSequences() {
        // Test various incomplete UTF-8 sequences
        byte[][] incompleteSequences = {
            {(byte)0xC2},                    // 2-byte sequence missing continuation
            {(byte)0xE0, (byte)0xA0},        // 3-byte sequence missing last byte
            {(byte)0xF0, (byte)0x90, (byte)0x80}, // 4-byte sequence missing last byte
            {(byte)0xC2, (byte)0x00},        // Invalid continuation byte
            {(byte)0xE0, (byte)0xA0, (byte)0x00} // Invalid continuation byte
        };

        for (byte[] incomplete : incompleteSequences) {
            assertFalse(isValidUTF8(incomplete),
                "Should reject incomplete sequence: " + bytesToHex(incomplete));
        }
    }

    @Test
    @DisplayName("Test invalid start bytes")
    public void testInvalidStartBytes() {
        // Bytes that cannot start a UTF-8 sequence
        byte[] invalidStarts = {
            (byte)0x80, (byte)0x81, (byte)0xBF, // Continuation bytes used as start
            (byte)0xFE, (byte)0xFF              // Invalid UTF-8 bytes
        };

        for (byte invalidStart : invalidStarts) {
            assertFalse(isValidUTF8(new byte[]{invalidStart}),
                "Should reject invalid start byte: " + String.format("0x%02X", invalidStart));
        }
    }

    @Test
    @DisplayName("Test null and empty handling")
    public void testNullAndEmpty() {
        // Empty string
        assertEquals(0, encodeStringToUTF8("").length);
        assertEquals("", decodeUTF8ToString(new byte[]{}));

        // Null character (U+0000)
        byte[] nullEncoded = encodeCodePointToUTF8(0);
        assertEquals(1, nullEncoded.length);
        assertEquals(0, nullEncoded[0]);
    }

    @Test
    @DisplayName("Test performance with large strings")
    public void testLargeString() {
        // Test with a reasonably large string
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("Hello 世界 🌍 ");
        }
        String large = sb.toString();

        byte[] encoded = encodeStringToUTF8(large);
        String decoded = decodeUTF8ToString(encoded);
        assertEquals(large, decoded);
    }

    // Helper methods:

    private boolean startsWithBOM(byte[] bytes) {
        if (bytes.length < 3) return false;
        return bytes[0] == (byte)0xEF && bytes[1] == (byte)0xBB && bytes[2] == (byte)0xBF;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    // Methods to implement:

    private byte[] encodeStringToUTF8(String str) {
        // TODO: Implement string to UTF-8 encoding
        throw new UnsupportedOperationException("Implement this method");
    }

    private String decodeUTF8ToString(byte[] bytes) {
        // TODO: Implement UTF-8 to string decoding
        throw new UnsupportedOperationException("Implement this method");
    }

    private byte[] encodeCharToUTF8(char c) {
        // TODO: Implement character to UTF-8 encoding
        throw new UnsupportedOperationException("Implement this method");
    }

    private byte[] encodeCodePointToUTF8(int codePoint) {
        // TODO: Implement code point to UTF-8 encoding
        throw new UnsupportedOperationException("Implement this method");
    }

    private int decodeUTF8ToCodePoint(byte[] bytes, int offset) {
        // TODO: Implement UTF-8 to code point decoding
        throw new UnsupportedOperationException("Implement this method");
    }

    private boolean isValidUTF8(byte[] bytes) {
        // TODO: Implement UTF-8 validation
        throw new UnsupportedOperationException("Implement this method");
    }
}