package com.voxlearning.washington.mapper.specialteacher.changeclass;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.ClazzType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.washington.mapper.specialteacher.base.SpecialTeacherConstants;
import lombok.Getter;
import lombok.Setter;

/**
 * 校内打散换班导入模板
 *
 * @author Xiaochao.Wei
 * @since 2017/7/13
 */

@Getter
@Setter
public class ChangeClassImportData {

    private String gradeName;       // 年级名称
    private String studentName;     // 学生姓名
    private String targetClazz;     // 目标班级 打散换班中叫换班后新班级，复制教学班学生中叫关联的教学班
    private ClazzLevel grade;

    private ClazzType importType;   // 导入类型

    private String result;          // 回填数据,成功/失败
    private String remark;          // 回填数据，备注

    public ChangeClassImportData(String gradeName, String studentName, String targetClazz, String importTypeName) {
        this.gradeName = gradeName;
        this.studentName = studentName;
        this.targetClazz = targetClazz;
        this.importType = SpecialTeacherConstants.parseImportType(importTypeName);
        this.grade = SpecialTeacherConstants.parseGradeOfChinese(gradeName);
    }

    public void success(String remark) {
        this.result = "1.操作成功";
        this.remark = remark;
    }

    public void failed(String remark) {
        this.result = "2.操作失败";
        this.remark = remark;
    }

    public String classKey() {
        return StringUtils.join(grade.getLevel(), "_", targetClazz);
    }

    public String classStudentKey() {
        return StringUtils.join(grade.getLevel(), "_", studentName);
    }

}
