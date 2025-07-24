public class XXTEA {

    /* 解密字节数组 */
    public static byte[] decrypt(byte[] contentBytes, byte[] keyBytes) {
        return contentBytes.length == 0
                ? contentBytes
                : intsToBytes(decrypt(bytesToInts(contentBytes, false), bytesToInts(keyBytes, false)), true);
    }

    /* 解密 int 数组 */
    public static int[] decrypt(int[] iArr, int[] iArr2) {
        int length = iArr.length - 1;
        if (length < 1) {
            return iArr;
        }
        int[] iArr3 = iArr2;
        if (iArr2.length < 4) {
            iArr3 = new int[4];
            System.arraycopy(iArr2, 0, iArr3, 0, iArr2.length);
        }
        int i = iArr[length];
        int i2 = iArr[0];
        int i3 = ((52 / (length + 1)) + 6) * (-1640531527);
        while (i3 != 0) {
            int i5 = (i3 >>> 2) & 3;
            for (int i6 = length; i6 > 0; i6--) {
                int i7 = iArr[i6 - 1];
                i2 = iArr[i6] - (((i2 ^ i3) + (i7 ^ iArr3[(i6 & 3) ^ i5]))
                        ^ (((i7 >>> 5) ^ (i2 << 2)) + ((i2 >>> 3) ^ (i7 << 4))));
                iArr[i6] = i2;
            }
            int i8 = iArr[length];
            i2 = iArr[0] - (((i2 ^ i3) + (iArr3[i5 ^ (0 & 3)] ^ i8))
                    ^ (((i8 >>> 5) ^ (i2 << 2)) + ((i2 >>> 3) ^ (i8 << 4))));
            iArr[0] = i2;
            i3 += 1640531527;
        }
        return iArr;
    }

    /* 加密字节数组 */
    public static byte[] encrypt(byte[] bArr, byte[] bArr2) {
        return bArr.length == 0
                ? bArr
                : intsToBytes(encrypt(bytesToInts(bArr, true), bytesToInts(bArr2, false)), false);
    }

    /* 加密 int 数组 */
    public static int[] encrypt(int[] iArr, int[] iArr2) {
        int length = iArr.length - 1;
        if (length < 1) {
            return iArr;
        }
        int[] iArr3 = iArr2;
        if (iArr2.length < 4) {
            iArr3 = new int[4];
            System.arraycopy(iArr2, 0, iArr3, 0, iArr2.length);
        }
        int i2 = iArr[length];
        int i4 = (52 / (length + 1)) + 6;
        int sum = 0;
        while (i4-- > 0) {
            sum -= 1640531527;
            int e = (sum >>> 2) & 3;
            for (int i = 0; i < length; i++) {
                int next = iArr[i + 1];
                i2 = iArr[i] + ((((i2 >>> 5) ^ (next << 2)) + ((next >>> 3) ^ (i2 << 4)))
                        ^ ((next ^ sum) + (i2 ^ iArr3[(i & 3) ^ e])));
                iArr[i] = i2;
            }
            int first = iArr[0];
            i2 = iArr[length] + ((((i2 >>> 5) ^ (first << 2)) + ((first >>> 3) ^ (i2 << 4)))
                    ^ ((first ^ sum) + (i2 ^ iArr3[e ^ (length & 3)])));
            iArr[length] = i2;
        }
        return iArr;
    }

    /* int 数组转 byte 数组 */
    private static byte[] intsToBytes(int[] iArr, boolean includeLength) {
        int length = iArr.length << 2;
        int realLength = includeLength ? iArr[iArr.length - 1] : length;
        if (includeLength && realLength > length) {
            return null;
        }
        byte[] bArr = new byte[realLength];
        for (int i = 0; i < realLength; i++) {
            bArr[i] = (byte) ((iArr[i >>> 2] >>> ((i & 3) << 3)) & 0xFF);
        }
        return bArr;
    }

    /* byte 数组转 int 数组 */
    private static int[] bytesToInts(byte[] bArr, boolean includeLength) {
        int n = ((bArr.length + 3) >>> 2);
        int[] result = includeLength ? new int[n + 1] : new int[n];
        if (includeLength) {
            result[n] = bArr.length;
        }
        for (int i = 0; i < bArr.length; i++) {
            result[i >>> 2] |= (bArr[i] & 0xFF) << ((i & 3) << 3);
        }
        return result;
    }
}
