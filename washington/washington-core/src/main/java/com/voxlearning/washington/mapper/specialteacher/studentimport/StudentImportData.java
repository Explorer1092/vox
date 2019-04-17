package com.voxlearning.washington.mapper.specialteacher.studentimport;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.washington.mapper.specialteacher.base.SpecialTeacherConstants;
import lombok.Getter;
import lombok.Setter;


/**
 * 教务老师导入老师账号数据
 *
 * @author yuechen.wang
 * @since 2017-7-11
 **/
@Getter
@Setter
public class StudentImportData {

    private int rowIndex;

    private String grade;
    private String className;
    private String studentName;
    private String studentNumber;

    private Long clazzId;     // 查到对应的班级Id之后回填
    // 导入成功之后回填的数据
    private String scanNumber;
    // 导入失败之后回填的数据
    private String reason;

    public StudentImportData(int rowIndex, String grade, String className, String studentName, String studentNumber) {
        this.rowIndex = rowIndex + 1;
        this.grade = grade;
        this.className = className;
        this.studentName = StringUtils.deleteWhitespace(studentName);
        this.studentNumber = studentNumber;
    }

    /**
     * 吾日三省吾身
     */
    public boolean introspect() {
        // 检查姓名
        if (!SpecialTeacherConstants.checkChineseName(studentName, 12)) {
            return failed("姓名只支持12个以内的汉字和间隔符");
        }
        // 检查学号
        if (!SpecialTeacherConstants.checkDigitNumber(studentNumber, 14)) {
            return failed("学号最长支持14位数字");
        }
        return true;
    }

    public void success(String scanNumber) {
        this.scanNumber = scanNumber;
    }

    public boolean failed(String reason) {
        this.reason = reason;
        return false;
    }

    public void recovered(String scanNumber) {
        this.scanNumber = scanNumber;
    }

    public String classNameKey() {
        ClazzLevel clazzLevel = SpecialTeacherConstants.parseGradeOfChinese(grade);
        return clazzLevel == null ? "" : clazzLevel.getDescription() + className;
    }

    public String fullClassName() {
        return grade + className;
    }

    public String invalidClass() {
        return StringUtils.formatMessage("第{}行：年级或班级为空", rowIndex);
    }
}
