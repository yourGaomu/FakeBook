package com.zhangzc.bookmarketbiz.Const;

/**
 * 用户地址相关常量
 */
public class UserAddressConst {
    /**
     * 用户地址 Redis Key 前缀
     * 采用 Redis Hash 结构存储 (ZHash)
     * Key: market:user:address:{userId}
     * Field: addressId
     * Value: UserAddressJson
     */
    public static final String USER_ADDRESS_KEY_PREFIX = "market:user:address:";

    public static String getUserAddressKey(Long userId) {
        return USER_ADDRESS_KEY_PREFIX + userId;
    }
}
