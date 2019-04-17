package com.voxlearning.utopia.agent.bean;

import lombok.Getter;

/**
 * Created by tao.zang on 2017/4/11.
 */
public enum  ImportKLXStudentsErrorType {
    ADMINISTERERROR(0,"学校不在管辖范围内"),
    SCHOOLNOTEXITERROR(1,"学校id非字典表学校"),
    SCHOOLNAMEDIFFERENTID(2,"学校名称与id不对应"),
    CLASSCORRESPONDENCE(3,"老师名下没有对应班级或班级名称不符合规则"),
    TEACHERRELATIONERROR(4,"老师ID错误或老师与学校对应关系错误"),
    MANYSCHOOLS(5,"表格有多个学校id"),
    STUDENTIDREPEAT(6,"学生学号重复"),
    FAKETEACHER(7,"假老师"),
    NOTKLXTEACHER(8,"不是快乐学老师"),
    MANYSTUDENTSSAMENAME(9,"学生重名"),
    VACANCDATALINE(10,"存在空缺数据行"),
    TEACHERMISMATCH(11,"老师id与姓名不匹配"),
    STUDENTOUTRANGE(12,"单次上传时，每个班级只能上传150人");
    @Getter
    private  final int code;
    @Getter
    private final String value;

    ImportKLXStudentsErrorType(int code, String value) {
        this.code = code;
        this.value = value;
    }
}
