package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 取消判假老师
 * Created by dell on 2017/3/9.
 */
@Getter
@Setter
public class CancleFakeTeacherInfo implements Serializable {
    private Date cancleDate;//取消日期 -> yyyymmdd
    private String region;//区域
    private String department;//部门
    private String operationName;//操作人员
    private Long teacherId;//教师id
    private String teacherName;//教师姓名
    private String reason;//取消判假理由
}
