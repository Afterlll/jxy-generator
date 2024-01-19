package com.jxy.maker.template.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 文件范围枚举类
 */
@Getter
public enum FileFilterRangeEnum {

    FILE_NAME("文件名称", "fileName"),
    FILE_CONTENT("文件内容", "fileContent");

    private final String text;
    private final String value;

    FileFilterRangeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static FileFilterRangeEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }

        for (FileFilterRangeEnum anEnum : FileFilterRangeEnum.values()) {
            if (anEnum.getValue().equals(value)) {
                return anEnum;
            }
        }

        return null;
    }

}
