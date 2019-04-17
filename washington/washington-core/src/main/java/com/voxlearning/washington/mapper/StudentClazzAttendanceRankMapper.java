package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xinxin
 * @since 30/8/2016
 */
@Getter
@Setter
public class StudentClazzAttendanceRankMapper implements Serializable {
    private static final long serialVersionUID = 2487044485344295873L;

    private String schoolName;
    private String clazzName;
    private Double rate;    //签到率
    private Integer rank;
}
