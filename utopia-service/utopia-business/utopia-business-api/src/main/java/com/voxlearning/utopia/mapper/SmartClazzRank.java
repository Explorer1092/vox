package com.voxlearning.utopia.mapper;

import lombok.Data;

import java.io.Serializable;

/**
 * 智慧教室排行
 * @author Maofeng Lu
 * @since 14-7-1 下午2:52
 */
@Data
public class SmartClazzRank implements Serializable {

    private static final long serialVersionUID = -9155026452915117576L;

    private Long    studentId;
    private String  studentName;
    private String  studentImg;
    private int     integral;
    private int     studentWeight;  //学生权重值
    private String  initial;        //初始排名(按学生名字拼音)
    private Long tinyGroupId = 0L;
    private String tinyGroupName = "";
}
