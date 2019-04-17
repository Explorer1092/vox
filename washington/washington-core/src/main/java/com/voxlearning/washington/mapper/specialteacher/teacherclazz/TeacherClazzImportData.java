package com.voxlearning.washington.mapper.specialteacher.teacherclazz;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * 建班授课导入模板
 *
 * @author Xiaochao.Wei
 * @since 2017/12/18
 */

@Setter
@Getter
public class TeacherClazzImportData {
    private String teacherName;
    private String subject;         // 学科
    private String grade;           // 年级
    private String clazzName;       // 班级名称
    private String clazzType;       // 班级类型 行政班/教学班

    private String reason;          // 失败回填数据

    public TeacherClazzImportData(String teacherName, String subject, String grade, String clazzName, String clazzType) {
        this.teacherName = teacherName;
        this.subject = subject;
        this.grade = grade;
        this.clazzName = clazzName;
        this.clazzType = clazzType;
    }

    public void failed(String reason) {
        this.reason = reason;
    }

    public String teacherKey() {
        return StringUtils.join(teacherName, "_", subject);
    }
}
