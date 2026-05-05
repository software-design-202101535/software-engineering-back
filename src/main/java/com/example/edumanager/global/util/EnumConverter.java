package com.example.edumanager.global.util;

import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;

public class EnumConverter {

    private EnumConverter() {}

    public static <T extends Enum<T>> T stringToEnum(String value, Class<T> enumClass, ErrorCode errorCode) {
        for (T enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.name().equals(value)) {
                return enumConstant;
            }
        }
        throw new CustomException(errorCode);
    }
}
