package com.voxlearning.utopia.entity.activity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class TeacherAssignTermReviewRecord implements java.io.Serializable{

    private Long teacherId;
    private String subject;           // 科目
    private String homeworkId;        //作业ID，在删除作业的时候使用这个字段
    private Long groupId;           //班级ID
    private Date assignDate;          // 布置的时间
    private Integer week;             //本次布置作业属于第几周，方便统计是否连续四周，和每周两次都布置作业了


}
