package com.utf8;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests focused on understanding UTF-8 bit patterns and encoding rules
 */
public class UTF8BitPatternsTest {

    @Test
    @DisplayName("Test 1-byte UTF-8 pattern: 0xxxxxxx")
    public void testOneBytePatter() {
        // ASCII characters: 0x00-0x7F
        // Pattern: 0xxxxxxx (bit 7 = 0)

        char[] asciiChars = {'A', 'z', '0', '9', ' ', '!', '\t', '\n'};

        for (char c : asciiChars) {
            byte[] encoded = encodeToUTF8(c);
            assertEquals(1, encoded.length, "ASCII char should be 1 byte: " + c);

            // First bit should be 0 for ASCII
            assertTrue((encoded[0] & 0x80) == 0,
                "First bit should be 0 for ASCII char: " + c);
        }
    }

    @Test
    @DisplayName("Test 2-byte UTF-8 pattern: 110xxxxx 10xxxxxx")
    public void testTwoBytePatter() {
        // Characters: 0x80-0x7FF
        // Pattern: 110xxxxx 10xxxxxx

        char[] twoByteChars = {'ñ', 'é', 'ü', '©', 'α', 'β'};

        for (char c : twoByteChars) {
            byte[] encoded = encodeToUTF8(c);
            assertEquals(2, encoded.length, "Should be 2 bytes: " + c);

            // First byte: 110xxxxx
            assertEquals(0xC0, encoded[0] & 0xE0,
                "First byte should start with 110: " + c);

            // Second byte: 10xxxxxx
            assertEquals(0x80, encoded[1] & 0xC0,
                "Second byte should start with 10: " + c);
        }
    }

    @Test
    @DisplayName("Test 3-byte UTF-8 pattern: 1110xxxx 10xxxxxx 10xxxxxx")
    public void testThreeBytePatter() {
        // Characters: 0x800-0xFFFF
        // Pattern: 1110xxxx 10xxxxxx 10xxxxxx

        char[] threeByteChars = {'€', '中', '日', '한', '🙂'};

        for (char c : threeByteChars) {
            if (Character.isHighSurrogate(c) || Character.isLowSurrogate(c)) {
                continue; // Skip surrogate pairs for this test
            }

            byte[] encoded = encodeToUTF8(c);
            assertEquals(3, encoded.length, "Should be 3 bytes: " + c);

            // First byte: 1110xxxx
            assertEquals(0xE0, encoded[0] & 0xF0,
                "First byte should start with 1110: " + c);

            // Second byte: 10xxxxxx
            assertEquals(0x80, encoded[1] & 0xC0,
                "Second byte should start with 10: " + c);

            // Third byte: 10xxxxxx
            assertEquals(0x80, encoded[2] & 0xC0,
                "Third byte should start with 10: " + c);
        }
    }

    @Test
    @DisplayName("Test 4-byte UTF-8 pattern: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx")
    public void testFourBytePatter() {
        // Characters: 0x10000-0x10FFFF (represented as surrogate pairs in Java)
        // Pattern: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx

        int[] fourByteCodePoints = {0x1F600, 0x1F1FA, 0x1D11E, 0x10000, 0x10FFFF};

        for (int codePoint : fourByteCodePoints) {
            byte[] encoded = encodeCodePointToUTF8(codePoint);
            assertEquals(4, encoded.length, "Should be 4 bytes for codepoint: " + Integer.toHexString(codePoint));

            // First byte: 11110xxx
            assertEquals(0xF0, encoded[0] & 0xF8,
                "First byte should start with 11110");

            // Continuation bytes: 10xxxxxx
            for (int i = 1; i < 4; i++) {
                assertEquals(0x80, encoded[i] & 0xC0,
                    "Byte " + i + " should start with 10");
            }
        }
    }

    @Test
    @DisplayName("Test bit extraction and reconstruction")
    public void testBitManipulation() {
        // Test that we can correctly extract and reconstruct code points
        int[] testCodePoints = {0x41, 0xE9, 0x20AC, 0x1F600};

        for (int original : testCodePoints) {
            byte[] encoded = encodeCodePointToUTF8(original);
            int reconstructed = decodeUTF8ToCodePoint(encoded, 0);
            assertEquals(original, reconstructed,
                "Failed to reconstruct codepoint: " + Integer.toHexString(original));
        }
    }

    @Test
    @DisplayName("Test overlong encoding detection")
    public void testOverlongEncoding() {
        // Overlong encodings should be rejected
        // Example: encoding 'A' (0x41) as 2 bytes instead of 1

        byte[] overlongA = {(byte)0xC1, (byte)0x81}; // Overlong encoding of 'A'
        byte[] overlongNull = {(byte)0xC0, (byte)0x80}; // Overlong encoding of null

        assertFalse(isValidUTF8Sequence(overlongA), "Should reject overlong encoding");
        assertFalse(isValidUTF8Sequence(overlongNull), "Should reject overlong null");
    }

    // Methods to implement:

    /**
     * Encode a single character to UTF-8 bytes
     */
    private byte[] encodeToUTF8(char c) {
        // TODO: Implement UTF-8 encoding for a character
        throw new UnsupportedOperationException("Implement this method");
    }

    /**
     * Encode a code point to UTF-8 bytes
     */
    private byte[] encodeCodePointToUTF8(int codePoint) {
        // TODO: Implement UTF-8 encoding for a code point
        // Handle 1-4 byte sequences based on code point value
        throw new UnsupportedOperationException("Implement this method");
    }

    /**
     * Decode UTF-8 bytes starting at offset to a code point
     */
    private int decodeUTF8ToCodePoint(byte[] bytes, int offset) {
        // TODO: Implement UTF-8 decoding to get code point
        // Parse the bit patterns to reconstruct the original code point
        throw new UnsupportedOperationException("Implement this method");
    }

    /**
     * Validate if bytes form a valid UTF-8 sequence
     */
    private boolean isValidUTF8Sequence(byte[] bytes) {
        // TODO: Implement UTF-8 validation
        // Check for proper bit patterns and reject overlong encodings
        throw new UnsupportedOperationException("Implement this method");
    }
}