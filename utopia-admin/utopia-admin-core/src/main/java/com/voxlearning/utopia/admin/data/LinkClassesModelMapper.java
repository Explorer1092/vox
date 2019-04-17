package com.voxlearning.utopia.admin.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author:XiaochaoWei
 * @Description: 打散换班Excel数据格式
 * @CreateTime: 2017/5/8
 */
@Getter
@Setter
public class LinkClassesModelMapper implements Serializable {

    private String classLevel;            // 年级
    private String currentClass;          // 当前班级（非必填）
    private String studyNo;               // 校内学号（非必填）
    private String studentName;           // 学生姓名
    private String linkedClass;           // 关联的新班级
    private Long teacherId;               // 新班级老师ID
    private String result;                // 操作后回填结果（成功/失败）
    private String remark;                // 备注

    @JsonIgnore
    public boolean isEmpty() {
        return StringUtils.isBlank(classLevel)
                || StringUtils.isBlank(studentName)
                || StringUtils.isBlank(linkedClass)
                || teacherId == null;
    }

    @JsonIgnore
    public boolean isAllEmpty() {
        return StringUtils.isBlank(classLevel)
                && StringUtils.isBlank(studentName)
                && StringUtils.isBlank(linkedClass)
                && teacherId == null;
    }

    public void success(String remark) {
        this.result = "1.操作成功";
        this.remark = remark;
    }

    public void failed(String remark) {
        this.result = "2.操作失败";
        this.remark = remark;
    }

    public String classTeacherKey() {
        return StringUtils.join(classLevel, "_", linkedClass, "_", teacherId);
    }

    public String classKey() {
        return StringUtils.join(classLevel, "_", linkedClass);
    }

    public String classStudentKey() {
        return StringUtils.join(classLevel, "_", studentName);
    }

}
