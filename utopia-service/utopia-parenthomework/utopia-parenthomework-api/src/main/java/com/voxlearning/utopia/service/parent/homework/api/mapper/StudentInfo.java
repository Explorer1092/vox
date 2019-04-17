package com.voxlearning.utopia.service.parent.homework.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 学生信息
 * @author chongfeng.qi
 * @date 2018-11-07
 */
@Getter
@Setter
public class StudentInfo implements Serializable {

    private static final long serialVersionUID = -3775866081796336377L;

    private Long studentId;

    private String studentName;

    private Long clazzId;

    private String clazzName;

    private Long schoolId;

    private String schoolName;

    private Integer clazzLevel;

    private Integer regionCode;

    private Integer cityCode;
}
