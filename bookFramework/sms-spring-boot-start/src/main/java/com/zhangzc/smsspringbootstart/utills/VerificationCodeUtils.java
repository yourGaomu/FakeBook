package com.zhangzc.smsspringbootstart.utills;

import java.security.SecureRandom;

public abstract class VerificationCodeUtils {

    private static final String NUMBERS = "0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 生成指定长度的纯数字验证码
     */
    public static String generateNumericCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("验证码长度必须大于0");
        }
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(NUMBERS.charAt(SECURE_RANDOM.nextInt(NUMBERS.length())));
        }
        return code.toString();
    }

    /**
     * 生成数字+大写字母验证码（可选）
     */
    public static String generateAlphanumericCode(int length) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        return code.toString();
    }
}