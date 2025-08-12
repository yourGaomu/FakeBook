package com.zhangzc.bookuserbiz.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @date: 2023-08-15 10:33
 * @description: 性别
 **/
@Getter
@AllArgsConstructor
public enum SexEnum {

    WOMAN(0),
    MAN(1);

    private final Integer value;

    public static boolean isValid(Integer value) {
        for (SexEnum loginTypeEnum : SexEnum.values()) {
            if (Objects.equals(value, loginTypeEnum.getValue())) {
                return true;
            }
        }
        return false;
    }

}

