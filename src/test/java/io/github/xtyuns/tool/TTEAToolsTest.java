package io.github.xtyuns.tool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * TEA 算法工具测试类
 *
 * @author xtyuns
 * At: 2022/9/18
 */
public class TTEAToolsTest {
    @Test
    public void compare() {
        String key = "b1 5c fc 2a a3 9c 37 c0 46 c1 dc dc 15 2d 4c a6";
        String data = "00 18 00 16 00 01";
        String crypt = "B3 E0 FD F1 60 25 66 8C D7 20 13 DF 92 28 36 70";

        Byte[] expect = TTEATools.decrypt(crypt, key);

        Byte[] encrypt = TTEATools.encrypt(data, key);
        Byte[] decrypt = TTEATools.decrypt(TTEATools.bytes2hex(encrypt), key);

        Assertions.assertNotNull(expect, "bad crypt");
        Assertions.assertArrayEquals(expect, decrypt, "bad util");
    }
}
