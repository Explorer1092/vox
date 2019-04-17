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
public class ChangeClassModelMapper implements Serializable {

    private static final long serialVersionUID = 8638039335433331939L;
    private String classLevel;            // 年级
    private String originClass;           // 换班前班级（非必填）
    private String originStudyNo;         // 换班前学号（非必填）
    private String afterStudyNo;          // 换班后学号（非必填）
    private String studentName;           // 学生姓名
    private String afterClass;            // 换班后班级
    private Long teacherId;               // 换班后老师ID
    private String result;                // 操作后回填结果（成功/失败）
    private String remark;                // 备注

    @JsonIgnore
    public boolean isEmpty() {
        return StringUtils.isBlank(classLevel)
                || StringUtils.isBlank(studentName)
                || StringUtils.isBlank(afterClass)
                || teacherId == null;
    }

    @JsonIgnore
    public boolean isAllEmpty() {
        return StringUtils.isBlank(classLevel)
                && StringUtils.isBlank(studentName)
                && StringUtils.isBlank(afterClass)
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
        return StringUtils.join(classLevel, "_", afterClass, "_", teacherId);
    }

    public String classKey() {
        return StringUtils.join(classLevel, "_", afterClass);
    }

    public String classStudentKey() {
        return StringUtils.join(classLevel, "_", studentName);
    }

}
