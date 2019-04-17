package com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast;


import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkCorrectStatus;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * livecast 作业报告数据类
 */
@Setter
@Getter
public class LiveHomeworkReport implements Serializable {
    private static final long serialVersionUID = -3403016448883730454L;
    private boolean success;// 是否成功
    private String failedInfo; // 失败的信息
    private int homeworkFinishedNum;// 完成作业人数
    private int calculationScoreNum;//计算班级平均分的人
    private int totalStudentNum;//一共学生人数
    private int clazzAverScore;// 平均成绩
    private boolean subjective;
    private List<String> objectiveConfigTypeNames; // 类型的中文名字
    private List<StudentReportBrief> studentReportBriefs = new LinkedList<>();//学生成绩的列表
    private List<StatisticsToObjectiveConfigType> statisticsToObjectiveConfigTypes = new LinkedList<>();//按照类型进行统计信息

    /**
     * 学生单人的成绩简介
     */
    @Setter
    @Getter
    public static class StudentReportBrief implements Serializable {
        private static final long serialVersionUID = -2759778919886655698L;
        private Long sid;//学生ID
        private String sname;//学生名字
        private String personAverScore = "--";// 个人成绩
        private int score;
        private Long finishTime = Long.MAX_VALUE;
        private List<String> scoreInfo;//各个类型成绩信息
        private String finishAt = "未完成";//完成时间
        private String duration = "--";//花费时间
        private Long castTime = Long.MAX_VALUE;
        private String comment;
        private boolean repair;
        private boolean finished;
        private boolean corrected = false; //是否批改完成
        private List<Double> percentageInfo;//百分比信息列表，按照题目顺序返回
    }

    /**
     * 作业报告对作业类型的统计
     */
    @Setter
    @Getter
    public static class StatisticsToObjectiveConfigType implements Serializable {
        private static final long serialVersionUID = 9063645711877862436L;
        private ObjectiveConfigType objectiveConfigType;//类型名字
        private String objectiveConfigTypeName;//类型中文名字
        private int objectiveConfigTypeFinishedNum;//这个类型完成的人数
        private int totalStudentNum;//一共学生人数
        private int clazzAverScoreToObjectiveConfig;// 类型平均成绩
        private int clazzAverDurationToObjectiveConfig;// 类型平均花费时间

        private List<StatisticsToNewQuestion> statisticsToNewQuestions = new LinkedList<>();//每题的统计

        private List<BasicAppInformation> basicAppInformation = new LinkedList<>();// 基础练习的类型的特有字段数据

    }

    /**
     * 每题成绩的统计
     */
    @Setter
    @Getter
    public static class StatisticsToNewQuestion implements Serializable {
        private static final long serialVersionUID = 3394297517589470594L;
        private String questionId;//题ID
        private int rightNum;//正确人数
        private int totalNum;//一共做题的人数
        private int proportion;//正确百分比
        private String ContentType;
        private int showType;
        private int difficultyInt;
        private Map<String, List<StatisticsToNewQuestionAnswer>> answer = new LinkedHashMap<>();
        private List<Map<String, Object>> answerList;
        private int questionScore;

        private List<LiveCastSubjectiveQuestion.StudentSubjectiveQuestionInfo> subjectiveQuestionInfos = new LinkedList<>();
    }

    @Setter
    @Getter
    public static class StatisticsToNewQuestionAnswer implements Serializable {
        private static final long serialVersionUID = 3170161381994283458L;
        private Long userId;
        private String userName;
    }


}
