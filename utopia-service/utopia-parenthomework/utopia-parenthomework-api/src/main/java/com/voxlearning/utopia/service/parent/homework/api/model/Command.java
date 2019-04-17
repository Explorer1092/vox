package com.voxlearning.utopia.service.parent.homework.api.model;

/**
 * 家长通作业命令定义
 *
 * @author Wenlong Meng
 * @since Mar 14, 2019
 */
public enum Command {
    INDEX("作业首页"),
    DO("做"),
    QUESTIONS("题目"),
    ANSWERS("答案"),
    SUBMIT("提交");

    private String name;

    /**
     * @param name
     */
    Command(String name){
        this.name = name;
    }
}
