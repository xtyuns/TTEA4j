package io.github.xtyuns.tool;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * TEA 算法工具
 *
 * @author xtyuns
 * At: 2022/9/18
 */
public abstract class TTEATools {
    public static final Long UINT32_MASK = 0xffffffffL;
    private static final Long DELTA = 0x9e3779b9 & TTEATools.UINT32_MASK;
    private static final int LOOP_EXPONENT = 4;
    private static final int ONE_BLOCK = 8;
    private static final int H1 = 1;
    private static final int H2 = 2;
    private static final int T7 = 7;

    public static Byte[] encrypt(String dataHex, String keyHex) {
        Byte[] dataBytes = hex2bytes(dataHex);
        Byte[] keyBytes = hex2bytes(keyHex);

        return encrypt(dataBytes, 0, dataBytes.length, keyBytes);
    }

    public static Byte[] decrypt(String cryptHex, String keyHex) {
        Byte[] cryptBytes = hex2bytes(cryptHex);
        Byte[] keyBytes = hex2bytes(keyHex);

        return decrypt(cryptBytes, 0, cryptBytes.length, keyBytes);
    }

    public static Byte[] encrypt(Byte[] dataBytes, int offset, int length, Byte[] keyBytes) {
        int floatPaddingLength = (TTEATools.ONE_BLOCK - ((length + TTEATools.H1 + TTEATools.H2 + TTEATools.T7) % TTEATools.ONE_BLOCK)) % TTEATools.ONE_BLOCK;
        // 前 8 位为 0x00 的 prePlain, 接下来填充后的明文数据
        int richLength = TTEATools.ONE_BLOCK + TTEATools.H1 + floatPaddingLength + TTEATools.H2 + length + TTEATools.T7;
        int block = (richLength - TTEATools.ONE_BLOCK) / TTEATools.ONE_BLOCK;

        Byte[] richPlain = new Byte[richLength];
        Byte[] richCrypt = new Byte[richLength];
        Arrays.fill(richPlain, 0, TTEATools.ONE_BLOCK, Byte.valueOf("0"));
        Arrays.fill(richCrypt, 0, TTEATools.ONE_BLOCK, Byte.valueOf("0"));

        int pos = TTEATools.ONE_BLOCK;
        // 将不定长填充长度保存在明文中的第一位
        richPlain[pos++] = (byte) ((randByte() & ((TTEATools.ONE_BLOCK - 1) ^ 0xff)) | floatPaddingLength);
        while (pos <= TTEATools.ONE_BLOCK + floatPaddingLength) {
            richPlain[pos++] = randByte();
        }
        for (int i = 0; i < TTEATools.H2; i++) {
            richPlain[pos++] = randByte();
        }
        System.arraycopy(dataBytes, offset, richPlain, pos, length);
        Arrays.fill(richPlain, pos + length, pos + length + TTEATools.T7, Byte.valueOf("0"));

        for (int i = 1; i <= block; i++) {
            int blockHead = i * TTEATools.ONE_BLOCK;
            for (int j = 0; j < TTEATools.ONE_BLOCK; j++) {
                pos = blockHead + j;
                richPlain[pos] = (byte) (richPlain[pos] ^ richCrypt[pos - TTEATools.ONE_BLOCK]);
            }
            Byte[] blockEncode = encode(richPlain, blockHead, keyBytes);
            for (int j = 0; j < TTEATools.ONE_BLOCK; j++) {
                pos = blockHead + j;
                richCrypt[pos] = (byte) (blockEncode[j] ^ richPlain[pos - TTEATools.ONE_BLOCK]);
            }
        }

        return Arrays.copyOfRange(richCrypt, TTEATools.ONE_BLOCK, richCrypt.length);
    }

    public static Byte[] decrypt(Byte[] cryptBytes, int offset, int length, Byte[] keyBytes) {
        if (length % TTEATools.ONE_BLOCK != 0 || length < 2 * TTEATools.ONE_BLOCK) {
            return null;
        }

        Byte[] richCrypt = new Byte[length];
        Byte[] richPlain = new Byte[length];
        System.arraycopy(cryptBytes, offset, richCrypt, 0, length);

        int block = length / TTEATools.ONE_BLOCK;
        for (int i = 0; i < block; i++) {
            int blockHead = i * TTEATools.ONE_BLOCK;
            for (int j = 0; j < TTEATools.ONE_BLOCK; j++) {
                int pos = blockHead + j;
                richPlain[pos] = richCrypt[pos];
                if (i > 0) {
                    richPlain[pos] = (byte) (richPlain[pos] ^ richPlain[pos - TTEATools.ONE_BLOCK]);
                }
                if (i > 1) {
                    richPlain[pos] = (byte) (richPlain[pos] ^ richCrypt[pos - 2 * TTEATools.ONE_BLOCK]);
                }
            }
            Byte[] blockDecode = decode(richPlain, blockHead, keyBytes);
            for (int j = 0; j < TTEATools.ONE_BLOCK; j++) {
                int pos = blockHead + j;
                richPlain[pos] = blockDecode[j];
                if (i > 0) {
                    richPlain[pos] = (byte) (richPlain[pos] ^ richCrypt[pos - TTEATools.ONE_BLOCK]);
                }
            }
        }

        int contextStart = TTEATools.H1 + (richPlain[0] & (TTEATools.ONE_BLOCK - 1)) + TTEATools.H2;
        for (int i = 1; i <= TTEATools.T7; i++) {
            if (richPlain[richPlain.length - i] != 0) {
                return null;
            }
        }


        return Arrays.copyOfRange(richPlain, contextStart, richPlain.length - TTEATools.T7);
    }

    public static Byte[] encode(Byte[] bytes, int offset, Byte[] key) {
        long v0 = packUint32(bytes, offset);
        long v1 = packUint32(bytes, offset + 4);
        long k0 = packUint32(key, 0);
        long k1 = packUint32(key, 4);
        long k2 = packUint32(key, 8);
        long k3 = packUint32(key, 12);
        long sum = 0L;

        for (long i = 0; i < (1L << TTEATools.LOOP_EXPONENT & TTEATools.UINT32_MASK); i++) {
            sum = (sum + TTEATools.DELTA) & TTEATools.UINT32_MASK;
            v0 = (v0 + (((v1 << 4) + k0) ^ (v1 + sum) ^ ((v1 >>> 5) + k1))) & TTEATools.UINT32_MASK;
            v1 = (v1 + (((v0 << 4) + k2) ^ (v0 + sum) ^ ((v0 >>> 5) + k3))) & TTEATools.UINT32_MASK;
        }

        Byte[] result = Arrays.copyOf(unpackUint32(v0), 8);
        System.arraycopy(unpackUint32(v1), 0, result, 4, 4);

        return result;
    }

    public static Byte[] decode(Byte[] bytes, int offset, Byte[] key) {
        long v1 = packUint32(bytes, offset + 4);
        long v0 = packUint32(bytes, offset);
        long k0 = packUint32(key, 0);
        long k1 = packUint32(key, 4);
        long k2 = packUint32(key, 8);
        long k3 = packUint32(key, 12);
        long sum = (TTEATools.DELTA << TTEATools.LOOP_EXPONENT) & TTEATools.UINT32_MASK;

        for (long i = 0; i < ((1L << TTEATools.LOOP_EXPONENT) & TTEATools.UINT32_MASK); i++) {
            v1 = (v1 - (((v0 << 4) + k2) ^ (v0 + sum) ^ ((v0 >>> 5) + k3))) & TTEATools.UINT32_MASK;
            v0 = (v0 - (((v1 << 4) + k0) ^ (v1 + sum) ^ ((v1 >>> 5) + k1))) & TTEATools.UINT32_MASK;
            sum = (sum - TTEATools.DELTA) & TTEATools.UINT32_MASK;
        }

        Byte[] result = Arrays.copyOf(unpackUint32(v0), 8);
        System.arraycopy(unpackUint32(v1), 0, result, 4, 4);

        return result;
    }

    private static Long packUint32(Byte[] bytes, int offset) {
        int end = offset + 4;
        long result = 0L;
        for (int i = offset; i < end; i++) {
            result = result << 8 | Byte.toUnsignedInt(bytes[i]);
        }

        return (result >>> 32) | (result & TTEATools.UINT32_MASK);
    }

    private static Byte[] unpackUint32(Long uint32) {
        return new Byte[]{
                (byte) (uint32 >> 24 & 0xff),
                (byte) (uint32 >> 16 & 0xff),
                (byte) (uint32 >> 8 & 0xff),
                (byte) (uint32 & 0xff)
        };
    }

    public static Byte[] hex2bytes(String hex) {
        if (hex.trim().indexOf(' ') == -1) {
            hex = hex.replaceAll("(..)", "$1 ");
        }
        return Arrays.stream(hex.trim().split("\\s+")).map(h -> {
            if (h.length() > 2) {
                throw new NumberFormatException(String.format("Value out of range. Value:\"%s\" byte:1", h));
            }
            return Short.valueOf(h, 16).byteValue();
        }).toArray(Byte[]::new);
    }

    public static String bytes2hex(Byte[] bytes, Boolean pretty) {
        String sep = Boolean.TRUE.equals(pretty) ? " " : "";
        return Arrays.stream(bytes)
                .map(b -> String.format("%02x", Byte.toUnsignedInt(b)))
                .collect(Collectors.joining(sep));
    }

    public static String bytes2hex(Byte[] bytes) {
        return bytes2hex(bytes, false);
    }

    public static byte randByte() {
        return (byte) String.valueOf(Math.random()).codePointAt(3);
    }
}
