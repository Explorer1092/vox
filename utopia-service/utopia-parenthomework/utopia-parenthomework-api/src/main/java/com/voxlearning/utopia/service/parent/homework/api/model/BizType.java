package com.voxlearning.utopia.service.parent.homework.api.model;

/**
 * 家长通作业业务类型定义
 *
 * @author Wenlong Meng
 * @since Mar 18, 2019
 */
public enum BizType {
    EXAM("同步习题"),
    MATAL_ARITHMETIC("口算速算"),
    OCR_MATAL_ARITHMetIC("纸质口算速算"),
    INTELLIAGENT_TEACHING("讲练测")
    ;

    private String name;

    /**
     * @param name
     */
    BizType(String name){
        this.name = name;
    }
}
