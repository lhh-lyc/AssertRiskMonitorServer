package com.lhh.serverbase.utils;


import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;

import java.security.MessageDigest;

/**
 * MD5处理
 *
 * @author rona
 */
public class MD5 {

    public static String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("M" +
                    "D5");
            md.update(str.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0) {
                    i += 256;
                }

                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            str = buf.toString();
        } catch (Exception e) {
            e.printStackTrace();

        }
        return str;
    }

    /**
     * MD5方法
     *
     * @param text    明文
     * @param charset 密钥
     * @return 密文
     * @throws Exception
     */
    public static String md5(String text, String charset) throws Exception {
        if (charset == null || charset.length() == 0) {
            charset = "UTF-8";
        }

        byte[] bytes = text.getBytes(charset);

        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(bytes);
        bytes = messageDigest.digest();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if ((bytes[i] & 0xff) < 0x10) {
                sb.append("0");
            }

            sb.append(Long.toString(bytes[i] & 0xff, 16));
        }

        return sb.toString().toLowerCase();
    }

    /**
     * MD5验证方法
     *
     * @param text    明文
     * @param charset 字符编码
     * @param md5     密文
     * @return true/false
     * @throws Exception
     */
    public static boolean verify(String text, String charset, String md5) throws Exception {
        String md5Text = md5(text, charset);
        if (md5Text.equalsIgnoreCase(md5)) {
            return true;
        }

        return false;
    }

    public static String getSalt() {
        String salt = RandomStringUtils.randomAlphanumeric(20);
        System.out.println(salt);
        return salt;
    }

    /**
     * 获取加密的密码
     *
     * @param psw
     * @return
     */
    public static String encryptPwdFirst(String psw) {
        return DigestUtils.md5Hex(psw);
    }

    /**
     * 获取加密的密码
     *
     * @param psw
     * @param salt
     * @return
     */
    public static String getEncryptPwd(String psw, String salt) {
        return new Sha256Hash(psw, salt).toHex();
    }

}
