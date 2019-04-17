package com.voxlearning.washington.mapper.specialteacher.markstudent;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.washington.mapper.specialteacher.base.SpecialTeacherConstants;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Xiaochao.Wei
 * @since 2017/10/23
 */
@Getter
@Setter
public class MarkStudentData {
    private String grade;          // 年级
    private String className;      // 班级名称
    private String studentName;    // 学生姓名
    private String studentNumber;  // 学生校内学号
    private String isMarked;       // 标记（是否是借读生）

    // 导入成功之后回填的数据
    private String result;
    // 导入失败之后回填的数据
    private String reason;

    public MarkStudentData(String grade, String className, String studentName, String studentNumber, String isMarked) {
        this.grade = grade;
        this.className = className;
        this.studentName = StringUtils.deleteWhitespace(studentName);
        this.studentNumber = studentNumber;
        this.isMarked = isMarked;
    }

    public void success(String reason) {
        this.result = "操作成功";
        this.reason = reason;
    }

    public void failed(String reason) {
        this.result = "操作失败";
        this.reason = reason;
    }

    public String classNameKey() {
        ClazzLevel clazzLevel = SpecialTeacherConstants.parseGradeOfChinese(grade);
        return clazzLevel == null ? "" : clazzLevel.getDescription() + className;
    }

    public String fullClassName() {
        return grade + className;
    }
}
