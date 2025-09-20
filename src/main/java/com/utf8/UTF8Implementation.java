package com.utf8;

/**
 * UTF-8 Implementation Guide
 *
 * This class provides a reference implementation and detailed explanations
 * for UTF-8 encoding and decoding. Study this to understand the concepts
 * before implementing the test methods.
 */
public class UTF8Implementation {

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
    public static int getUTF8ByteLength(int codePoint) throws IllegalArgumentException {
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

    public static boolean isContinuationByte(byte b) {
        // 10xxxxxx
        return (b & 0xC0) == 0x80;
    }

    public static int getSequenceLength(byte firstByte) {
        if ((firstByte & 0x80) == 0) {
            return 1; // 0xxxxxxx
        } else if ((firstByte & 0xE0) == 0xC0) {
            // 110xxxxx & 11100000
            return 2;
        } else if ((firstByte & 0xF0) == 0xE0) {
            // 1110xxxx & 11110000
            return 3;
        } else if ((firstByte & 0xF8) == 0xF0) {
            // 11110xxx & 11111000
            return 4;
        }

        throw new IllegalArgumentException("Invalid UTF-8 start byte: " +
                String.format("0x%02X", firstByte & 0xFF));
    }


    /**
     * STEP 2: Encode a code point to UTF-8 bytes
     *
     * This is the core encoding logic. You need to:
     * 1. Determine byte length (use method above)
     * 2. Extract bits from the code point
     * 3. Distribute them according to UTF-8 patterns
     */
    public static byte[] encodeCodePointToUTF8(int codePoint) throws IllegalArgumentException {
        // Reject surrogate range (U+D800-U+DFFF)
        if (codePoint >= 0xD800 && codePoint <= 0xDFFF) {
            throw new IllegalArgumentException("Surrogate code point: " + Integer.toHexString(codePoint));
        }
        int numBytesReqForEncoding;
        try {
            numBytesReqForEncoding = getUTF8ByteLength(codePoint);
        } catch (IllegalArgumentException e) {
            throw e;
        }

        switch(numBytesReqForEncoding) {
            case 1:
                return new byte[]{
                        (byte) codePoint
                };
            case 2:
                return new byte[]{
                        (byte) (((codePoint >> 6) & 0x1F) | 0xC0),
                        (byte) ((codePoint & 0x3F) | 0x80)
                };
            case 3:
                return new byte[]{
                        (byte) (((codePoint >> 12) & 0x0F) | 0xE0),
                        (byte) (((codePoint >> 6) & 0x3F) | 0x80),
                        (byte) ((codePoint & 0x3F) | 0x80)
                };
            case 4:
                return new byte[]{
                        (byte) (((codePoint >> 18) & 0x07) | 0xF0),
                        (byte) (((codePoint >> 12) & 0x3F) | 0x80),
                        (byte) (((codePoint >> 6) & 0x3F) | 0x80),
                        (byte) ((codePoint & 0x3F) | 0x80)
                };
            default:
                throw new IllegalArgumentException("Code point too large: " + Integer.toHexString(codePoint));
        }
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

        byte firstByte = bytes[offset];
        int codePointLength = getSequenceLength(firstByte);

        if (offset + codePointLength > bytes.length) {
            throw new IllegalArgumentException("invalid bytes sequence");
        }

        int firstCodePoint, secondCodePoint, thirdCodePoint, forthCodePoint;
        int result = 0;

        // Big Endian is used for encoding.
        switch (codePointLength) {
            case 1:
                return (int) firstByte;
            case 2:
                // 110xxxxx 10xxxxxx
                firstCodePoint = (firstByte & 0x1F);
                if (!isContinuationByte(bytes[offset+1])) {
                    throw new IllegalArgumentException("Invalid UTF-8 start byte: " + Integer.toHexString(firstByte & 0xFF));
                }
                secondCodePoint = (bytes[++offset] & 0x3F);
                result = (firstCodePoint << 6) | secondCodePoint;
                if (getUTF8ByteLength(result) != 2) {
                    throw new IllegalArgumentException("Invalid UTF-8 start byte: " + Integer.toHexString(firstByte & 0xFF));
                }
                return result;
            case 3:
                // 1110xxxx 10xxxxxx 10xxxxxx
                firstCodePoint = (firstByte & 0x0F);
                if (!isContinuationByte(bytes[offset+1])) {
                    throw new IllegalArgumentException("Invalid UTF-8 start byte: " + Integer.toHexString(firstByte & 0xFF));
                }
                secondCodePoint = (bytes[++offset] & 0x3F);
                if (!isContinuationByte(bytes[offset+1])) {
                    throw new IllegalArgumentException("Invalid UTF-8 start byte: " + Integer.toHexString(firstByte & 0xFF));
                }
                thirdCodePoint = (bytes[++offset] & 0x3F);
                result = (firstCodePoint << 12) | (secondCodePoint << 6) | thirdCodePoint;
                if (getUTF8ByteLength(result) != 3) {
                    throw new IllegalArgumentException("Invalid UTF-8 start byte: " + Integer.toHexString(firstByte & 0xFF));
                }
                return result;
            case 4:
                // 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
                firstCodePoint = (firstByte & 0x07);
                if (!isContinuationByte(bytes[offset+1])) {
                    throw new IllegalArgumentException("Invalid UTF-8 start byte: " + Integer.toHexString(firstByte & 0xFF));
                }
                secondCodePoint = (bytes[++offset] & 0x3F);
                if (!isContinuationByte(bytes[offset+1])) {
                    throw new IllegalArgumentException("Invalid UTF-8 start byte: " + Integer.toHexString(firstByte & 0xFF));
                }
                thirdCodePoint = (bytes[++offset] & 0x3F);
                if (!isContinuationByte(bytes[offset+1])) {
                    throw new IllegalArgumentException("Invalid UTF-8 start byte: " + Integer.toHexString(firstByte & 0xFF));
                }
                forthCodePoint = (bytes[++offset] & 0x3F);
                result = (firstCodePoint << 18) | (secondCodePoint << 12) | (thirdCodePoint << 6) | (forthCodePoint);
                if (getUTF8ByteLength(result) != 4) {
                    throw new IllegalArgumentException("Invalid UTF-8 start byte: " + Integer.toHexString(firstByte & 0xFF));
                }
                return result;
            default:
                throw new IllegalArgumentException("Invalid UTF-8 start byte: " + Integer.toHexString(firstByte & 0xFF));
        }

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

        int totalBytes = str.codePoints()
                .map(UTF8Implementation::getUTF8ByteLength)
                .sum();

        var result = new byte[totalBytes];
        int[] pos = {0};

        str.codePoints().forEach(codepoint -> {
            var bytes = encodeCodePointToUTF8(codepoint);
            System.arraycopy(bytes, 0, result, pos[0], bytes.length);
            pos[0] += bytes.length;
        });

        return result;
    }

    /**
     * STEP 5: String decoding
     *
     * Process UTF-8 bytes and build a string from the decoded code points.
     */
    public static String decodeUTF8ToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < bytes.length;) {
            var firstByteOfCodePoint = bytes[i];
            int codePointLength = getSequenceLength(firstByteOfCodePoint);
            var codePoint = decodeUTF8ToCodePoint(bytes, i);
            sb.appendCodePoint(codePoint);

            i += codePointLength;
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