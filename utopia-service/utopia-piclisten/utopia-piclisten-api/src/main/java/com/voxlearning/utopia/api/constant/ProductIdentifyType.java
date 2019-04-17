package com.voxlearning.utopia.api.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * @author feng.guo
 * @since 2019-03-07
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ProductIdentifyType {
    UNKNOW("", null),
    parent_EXAM("家长通同步习题", "SYNCHRONOUS_EXERCISE"),
    parent_MENTAL_ARITHMETIC("家长通在线口算", "QUICK_ORAL_ARITHMETIC"),
    parent_OCR_MENTAL_ARITHMETIC("家长通纸质口算", "ORAL_ARITHMETIC_EXERCISE"),
    parent_INTELLIGENT_TEACHING("家长通讲练测", "MATH_PRACTICE");

    private String name;
    private String selfStudyType;

    public static ProductIdentifyType identifyType(String selfStudyType) {
        for (ProductIdentifyType type : ProductIdentifyType.values()) {
            if (type.selfStudyType == selfStudyType) {
                return type;
            }
        }
        return UNKNOW;
    }
}
