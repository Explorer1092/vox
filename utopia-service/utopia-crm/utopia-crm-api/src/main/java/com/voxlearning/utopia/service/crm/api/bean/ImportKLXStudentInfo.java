package com.voxlearning.utopia.service.crm.api.bean;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by dell on 2017/4/7.
 *  导入快乐学学生账号相关信息
 */
@Getter
@Setter
public class ImportKLXStudentInfo implements Serializable {
    private Long schoolId;//学校id
    private String schoolName;//学校名称
    private Long teacherId;//老师Id
    private String teacherName;//老师姓名
    private Integer gradeLevel;//年级
    private String calzzName;//班级
    private String studentName;//学生姓名
    private String studentNumber;//学生号
    private Integer rows;//对应的行
    private String scanNumber;//填涂号
    public boolean isAllEmpty() {
        return schoolId == null
                &&teacherId == null
                && gradeLevel == null
                && rows == null
                &&calzzName ==null
                && StringUtils.isBlank(schoolName)
                && StringUtils.isBlank(teacherName)
                && StringUtils.isBlank(teacherName)
                && StringUtils.isBlank(studentName)
                && StringUtils.isBlank(studentName);
    }
    public boolean notEmpty(){
        return schoolId != null
                &&teacherId != null
                && gradeLevel != null
                && rows != null
                &&calzzName !=null
                && StringUtils.isNotBlank(schoolName)
                && StringUtils.isNotBlank(teacherName)
                && StringUtils.isNotBlank(teacherName)
                && StringUtils.isNotBlank(studentName)
                && StringUtils.isNotBlank(studentName);
    }
}
