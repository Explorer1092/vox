package com.voxlearning.utopia.service.newhomework.api.mapper.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;


/**
 * 周报告学生个人信息
 */
@Getter
@Setter
public class WeekReportForStudent implements Serializable {
    private static final long serialVersionUID = -5466077895418996682L;
    private Long sid;//学生Id
    private String sname;//学生Name
    private String teacherName;//老师Name
    private String teacherUrl;//老师Url

    private String subjectName;

    private int homeworkNum;//一共作业数目
    private int finishedHomeworkNum;//完成作业数
    private int finishedRate;// 这周完成率
    private int upFinishedRate;//上周完成率


    private boolean allSubjective;//这周完成是否都是主观题作业
    private boolean upAllSubjective;//上周完成是否都是主观题作业
    private boolean allSubjectiveToTeacher;//老师布置作业是否都是主观题作业
    private int highestScore;//最高成绩

    private Integer score;//这周个人成绩
    private String scoreLevel;//这周个人成绩
    private Integer upScore;//上周个人成绩
    private Integer avgScore;//班级平均成绩
    private String avgScoreLevel;//班级平均成绩

    private boolean blackRegion;//灰度地区

    private int wrongQuestionNum;//这周错题数
    private int upWrongQuestionNum;//上周错题数
    private int avgWrongQuestionNum;//班级错题数

    private int cumulativeWrongQuestionNum;// 累计错题数


    private int unitNum; //单元个数
    private List<String> unitNames = new LinkedList<>();// 单元名字

    private int questionNum;// 做题数目
    private int wordNum;//单词数目
    private int knowledgeNum;//练习知识点
    private int handleKnowledgeNum;//掌握知识点数目

}
