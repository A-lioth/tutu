package com.alioth.tutubackend.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRoleEnum {

    USER("user", "user"),
    ADMIN("admin", "admin");

    private final String label;
    private final String value;

    UserRoleEnum(String label, String value) {
        this.label = label;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     * @param value 枚举值的值
     * @return 枚举值
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.value.equals(value)) {
                return userRoleEnum;
            }
        }
        return null;
    }
}
