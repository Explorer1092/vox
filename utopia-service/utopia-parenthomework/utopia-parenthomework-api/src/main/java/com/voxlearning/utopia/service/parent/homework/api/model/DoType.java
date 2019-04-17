package com.voxlearning.utopia.service.parent.homework.api.model;

/**
 * 家长通作业业务类型定义
 *
 * @author Wenlong Meng
 * @since Mar 18, 2019
 */
public enum DoType {
    DO("做"),
    REDO("重做"),
    CORRECT("订正"),
    ;

    private String name;

    /**
     * @param name
     */
    DoType(String name){
        this.name = name;
    }
}
