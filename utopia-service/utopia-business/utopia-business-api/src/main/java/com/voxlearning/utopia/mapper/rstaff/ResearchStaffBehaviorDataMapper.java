package com.voxlearning.utopia.mapper.rstaff;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Changyuan on 2015/1/20.
 */
@Data
public class ResearchStaffBehaviorDataMapper implements Serializable {

    private static final long serialVersionUID = -5465412624801842630L;

    private String name;

    private int schoolNum;      // 学校个数

    private int homeworkStuNum; // 作业学生人数

    private long homeworkStuTime;    // 作业学生人次

    private int homeworkTeacherNum; // 作业老师人数

    private long homeworkTeacherTime;    // 作业老师人次

    List<ResearchStaffBehaviorDataMapper> childBehaviorData;    // 仅供市级教研员使用，查看校级行为数据
}
