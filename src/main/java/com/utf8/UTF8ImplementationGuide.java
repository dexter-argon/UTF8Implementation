package com.utf8;

/**
 * UTF-8 Implementation Guide
 *
 * This class provides a reference implementation and detailed explanations
 * for UTF-8 encoding and decoding. Study this to understand the concepts
 * before implementing the test methods.
 */
public class UTF8ImplementationGuide {

    /*
     * UTF-8 ENCODING RULES:
     *
     * Range                 Binary Pattern               Bytes
     * U+0000   - U+007F     0xxxxxxx                     1
     * U+0080   - U+07FF     110xxxxx 10xxxxxx            2
     * U+0800   - U+FFFF     1110xxxx 10xxxxxx 10xxxxxx   3
     * U+10000  - U+10FFFF   11110xxx 10xxxxxx 10xxxxxx 10xxxxxx  4
     *
     * Key points:
     * - First byte indicates sequence length with leading 1s
     * - Continuation bytes always start with 10xxxxxx
     * - x bits contain the actual Unicode code point data
     * - Overlong encodings (using more bytes than necessary) are invalid
     */

    /**
     * STEP 1: Determine how many bytes are needed for a code point
     *
     * This is your starting point - given a Unicode code point,
     * determine how many UTF-8 bytes it requires.
     */
    public static int getUTF8ByteLength(int codePoint) {
        if (codePoint < 0) {
            throw new IllegalArgumentException("Negative code point: " + codePoint);
        }
        if (codePoint <= 0x7F) {
            return 1;  // ASCII range: 0xxxxxxx
        }
        if (codePoint <= 0x7FF) {
            return 2;  // 110xxxxx 10xxxxxx
        }
        if (codePoint <= 0xFFFF) {
            return 3;  // 1110xxxx 10xxxxxx 10xxxxxx
        }
        if (codePoint <= 0x10FFFF) {
            return 4;  // 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
        }
        throw new IllegalArgumentException("Code point too large: " + Integer.toHexString(codePoint));
    }

    /**
     * STEP 2: Encode a code point to UTF-8 bytes
     *
     * This is the core encoding logic. You need to:
     * 1. Determine byte length (use method above)
     * 2. Extract bits from the code point
     * 3. Distribute them according to UTF-8 patterns
     */
    public static byte[] encodeCodePointToUTF8(int codePoint) {
        if (codePoint < 0 || codePoint > 0x10FFFF) {
            throw new IllegalArgumentException("Invalid code point: " + Integer.toHexString(codePoint));
        }

        // Reject surrogate range (U+D800-U+DFFF)
        if (codePoint >= 0xD800 && codePoint <= 0xDFFF) {
            throw new IllegalArgumentException("Surrogate code point: " + Integer.toHexString(codePoint));
        }

        if (codePoint <= 0x7F) {
            // 1 byte: 0xxxxxxx
            return new byte[]{(byte) codePoint};
        }

        if (codePoint <= 0x7FF) {
            // 2 bytes: 110xxxxx 10xxxxxx
            // Split 11 bits: 5 bits + 6 bits
            return new byte[]{
                (byte) (0xC0 | (codePoint >> 6)),           // 110xxxxx
                (byte) (0x80 | (codePoint & 0x3F))          // 10xxxxxx
            };
        }

        if (codePoint <= 0xFFFF) {
            // 3 bytes: 1110xxxx 10xxxxxx 10xxxxxx
            // Split 16 bits: 4 bits + 6 bits + 6 bits
            return new byte[]{
                (byte) (0xE0 | (codePoint >> 12)),          // 1110xxxx
                (byte) (0x80 | ((codePoint >> 6) & 0x3F)),  // 10xxxxxx
                (byte) (0x80 | (codePoint & 0x3F))          // 10xxxxxx
            };
        }

        // 4 bytes: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
        // Split 21 bits: 3 bits + 6 bits + 6 bits + 6 bits
        return new byte[]{
            (byte) (0xF0 | (codePoint >> 18)),              // 11110xxx
            (byte) (0x80 | ((codePoint >> 12) & 0x3F)),     // 10xxxxxx
            (byte) (0x80 | ((codePoint >> 6) & 0x3F)),      // 10xxxxxx
            (byte) (0x80 | (codePoint & 0x3F))              // 10xxxxxx
        };
    }

    /**
     * STEP 3: Decode UTF-8 bytes back to a code point
     *
     * This is the reverse process:
     * 1. Look at first byte to determine sequence length
     * 2. Validate continuation bytes
     * 3. Extract and combine bits to reconstruct code point
     * 4. Validate against overlong encodings
     */
    public static int decodeUTF8ToCodePoint(byte[] bytes, int offset) {
        if (offset >= bytes.length) {
            throw new IllegalArgumentException("Offset beyond array bounds");
        }

        int firstByte = bytes[offset] & 0xFF;

        if ((firstByte & 0x80) == 0) {
            // 1 byte: 0xxxxxxx
            return firstByte;
        }

        if ((firstByte & 0xE0) == 0xC0) {
            // 2 bytes: 110xxxxx 10xxxxxx
            if (offset + 1 >= bytes.length) {
                throw new IllegalArgumentException("Incomplete 2-byte sequence");
            }

            int secondByte = bytes[offset + 1] & 0xFF;
            if ((secondByte & 0xC0) != 0x80) {
                throw new IllegalArgumentException("Invalid continuation byte");
            }

            int codePoint = ((firstByte & 0x1F) << 6) | (secondByte & 0x3F);

            // Check for overlong encoding
            if (codePoint < 0x80) {
                throw new IllegalArgumentException("Overlong 2-byte sequence");
            }

            return codePoint;
        }

        if ((firstByte & 0xF0) == 0xE0) {
            // 3 bytes: 1110xxxx 10xxxxxx 10xxxxxx
            if (offset + 2 >= bytes.length) {
                throw new IllegalArgumentException("Incomplete 3-byte sequence");
            }

            int secondByte = bytes[offset + 1] & 0xFF;
            int thirdByte = bytes[offset + 2] & 0xFF;

            if ((secondByte & 0xC0) != 0x80 || (thirdByte & 0xC0) != 0x80) {
                throw new IllegalArgumentException("Invalid continuation byte");
            }

            int codePoint = ((firstByte & 0x0F) << 12) |
                           ((secondByte & 0x3F) << 6) |
                           (thirdByte & 0x3F);

            // Check for overlong encoding and surrogate range
            if (codePoint < 0x800 || (codePoint >= 0xD800 && codePoint <= 0xDFFF)) {
                throw new IllegalArgumentException("Invalid 3-byte sequence");
            }

            return codePoint;
        }

        if ((firstByte & 0xF8) == 0xF0) {
            // 4 bytes: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
            if (offset + 3 >= bytes.length) {
                throw new IllegalArgumentException("Incomplete 4-byte sequence");
            }

            int secondByte = bytes[offset + 1] & 0xFF;
            int thirdByte = bytes[offset + 2] & 0xFF;
            int fourthByte = bytes[offset + 3] & 0xFF;

            if ((secondByte & 0xC0) != 0x80 ||
                (thirdByte & 0xC0) != 0x80 ||
                (fourthByte & 0xC0) != 0x80) {
                throw new IllegalArgumentException("Invalid continuation byte");
            }

            int codePoint = ((firstByte & 0x07) << 18) |
                           ((secondByte & 0x3F) << 12) |
                           ((thirdByte & 0x3F) << 6) |
                           (fourthByte & 0x3F);

            // Check for overlong encoding and maximum code point
            if (codePoint < 0x10000 || codePoint > 0x10FFFF) {
                throw new IllegalArgumentException("Invalid 4-byte sequence");
            }

            return codePoint;
        }

        throw new IllegalArgumentException("Invalid UTF-8 start byte: " + Integer.toHexString(firstByte));
    }

    /**
     * STEP 4: String encoding
     *
     * Process each code point in the string and concatenate the UTF-8 bytes.
     * Remember to handle surrogate pairs correctly!
     */
    public static byte[] encodeStringToUTF8(String str) {
        // Hint: Use str.codePoints() to iterate over actual Unicode code points
        // This handles surrogate pairs automatically

        int totalBytes = 0;
        int[] codePoints = str.codePoints().toArray();

        // Calculate total byte length needed
        for (int codePoint : codePoints) {
            totalBytes += getUTF8ByteLength(codePoint);
        }

        byte[] result = new byte[totalBytes];
        int pos = 0;

        for (int codePoint : codePoints) {
            byte[] encoded = encodeCodePointToUTF8(codePoint);
            System.arraycopy(encoded, 0, result, pos, encoded.length);
            pos += encoded.length;
        }

        return result;
    }

    /**
     * STEP 5: String decoding
     *
     * Process UTF-8 bytes and build a string from the decoded code points.
     */
    public static String decodeUTF8ToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int offset = 0;

        while (offset < bytes.length) {
            int codePoint = decodeUTF8ToCodePoint(bytes, offset);
            sb.appendCodePoint(codePoint);

            // Move offset to next sequence
            offset += getUTF8ByteLength(codePoint);
        }

        return sb.toString();
    }

    /**
     * STEP 6: Validation
     *
     * Check if a byte array contains valid UTF-8 sequences.
     */
    public static boolean isValidUTF8(byte[] bytes) {
        try {
            decodeUTF8ToString(bytes);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /*
     * IMPLEMENTATION TIPS:
     *
     * 1. Bit Manipulation:
     *    - Use & (AND) to extract bits: value & 0x3F extracts lower 6 bits
     *    - Use | (OR) to combine: 0xC0 | bits sets the 110xxxxx pattern
     *    - Use >> and << for shifting: value >> 6 shifts right 6 positions
     *
     * 2. Java Specifics:
     *    - char is 16-bit, can only hold values up to U+FFFF
     *    - Higher code points are represented as surrogate pairs
     *    - Use String.codePoints() to iterate over actual Unicode code points
     *    - Use StringBuilder.appendCodePoint() to build strings from code points
     *
     * 3. Error Handling:
     *    - Check for negative code points
     *    - Validate code point ranges (max U+10FFFF)
     *    - Reject surrogate range U+D800-U+DFFF in encoding
     *    - Check for incomplete sequences in decoding
     *    - Detect overlong encodings
     *
     * 4. Testing Strategy:
     *    - Start with ASCII characters (simplest case)
     *    - Test boundary values (0x7F, 0x80, 0x7FF, 0x800, etc.)
     *    - Test round-trip encoding/decoding
     *    - Test error conditions
     *    - Test with real Unicode text
     */
}