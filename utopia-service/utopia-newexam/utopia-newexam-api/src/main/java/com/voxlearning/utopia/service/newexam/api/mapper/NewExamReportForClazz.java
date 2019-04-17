package com.voxlearning.utopia.service.newexam.api.mapper;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * crm班级学生exam信息
 */
@Setter
@Getter
public class NewExamReportForClazz implements Serializable {
    private static final long serialVersionUID = -8447127072375748878L;
    private boolean success;          //查询是否成功
    private int joinExamNum;          //参加的学生个数
    private int finishedExamNum;      //完成的学生个数
    private int totalStudentNum;      //班级的学生
    private String examId;                                                               //试卷Id
    private List<CrmStudentNewExamReport> crmStudentNewExamReports = new LinkedList<>(); //学生测验详情
    private String description;       //错误时候的信息
    private String paperId;           //试卷ID
    private int totalNum;     //试卷更新时间


    @Getter
    @Setter
    public static class CrmStudentNewExamReport implements Serializable {
        private String newExamResultId;             //学生模考记录Id
        private Long userId;                        //学生ID
        private String userName;                    //学生名字
        private String createAt;                    //开始时间
        private String finishAt;                    // 完成时间
        private String submitAt;                    // 交卷时间
        private Double score;                       // 实际分数
        private Double correctScore;                // 批改分数
        private String correctAt;                   // 批改时间
        private Long durationSeconds;               // 完成时长(单位:秒)
        private String clientType;                  // 客户端类型:pc,mobile
        private String clientName;                  // 客户端名称:***app
        private String paperId;
        private String detail;                      //明细
    }
}
