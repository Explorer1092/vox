package com.voxlearning.utopia.agent.constants;

import lombok.Getter;

/**
 * Created by dell on 2017/3/30.
 */
public enum SchoolQuizBankAdministratorOperationType {
    ALREADY(0,"已经是校本管理员"),
    ONLYBEMODIFIEDONCEAMONTH(1,"一个月内只能修改一次"),
    OTHEREXISTENCE(2,"存在其他的校本管理员"),
    NOTEXIST(3,"该科目不存在校本管理员"),
    NOTEXISTSUBJECT(4,"老师暂无负责科目")
    ;
    @Getter
    private final int key;
    @Getter
    private final String value;

    SchoolQuizBankAdministratorOperationType(int key,String value) {
        this.key = key;
        this.value = value;
    }
}
