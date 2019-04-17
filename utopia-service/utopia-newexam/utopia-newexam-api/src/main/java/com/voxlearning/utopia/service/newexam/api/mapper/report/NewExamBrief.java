package com.voxlearning.utopia.service.newexam.api.mapper.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

//会分页处理
@Setter
@Getter
public class NewExamBrief implements Serializable {
    private static final long serialVersionUID = 280119748831267673L;
    private String newExamId;     //试卷ID
    private String newExamName;   //试卷名字
    private String clazzName;     //班级名字
    private Long clazzId;         //班级ID
    private String startAt;     //考试开始时间
    private String stopAt;       //考试结束时间
    private int jointNum;         //学生参与人数
    private int unJointNum;       //未参与的人数
    private boolean oldExam;      //老模考
    private Set<Long> joinUsers = new HashSet<>();
    private Set<Long> submitUsers = new HashSet<>();
    private Set<Long> finishedUsers = new HashSet<>();
    private Set<Long> unJoinStudents = new LinkedHashSet<>();

    private boolean banView;      //是否禁止查看详情 true:禁止查看
    private String banReason;    //禁止查看详情文案
    private String shareUrl;     //分享报告地址（当前时间＞成绩发布时间允许分享报告）

}
