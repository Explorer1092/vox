package com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.clazz;


import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 英语报告
 */
@Getter
@Setter
public class BasicReviewHomeworkClazzStageEnglishReport implements Serializable {
    private static final long serialVersionUID = 1449801901414397207L;
    private Integer stageId;        // 关卡id
    private String stageName;       // 关卡名
    private Subject subject;
    private String subjectName;
    private String homeworkId;      // 关联的作业id
    private ContentPart contentPart;//内容掌握情况
    private StudentPart studentPart;//学生掌握情况
    private String clazzName; //班级名字
    private Long clazzId;
    private boolean share;

    /**
     * 内容掌握情况
     */
    @Getter
    @Setter
    public static class ContentPart implements Serializable {
        private static final long serialVersionUID = 1626155311182253541L;
        private List<String> words = new LinkedList<>();   //高频错误单词
        private List<String> sentences = new LinkedList<>();//高频错误句子
        private int wordFinishUserNum;//单词完成人数
        private int sentenceFinishNum;//句子完成人数
        private List<WordAnalysis> wordAnalysisList = new LinkedList<>(); //单词分析
        private List<SentenceAnalysis> sentenceAnalysisList = new LinkedList<>();//句子分析

    }

    /**
     * 学生掌握情况
     */
    @Getter
    @Setter
    public static class StudentPart implements Serializable {
        private static final long serialVersionUID = -44125506885469076L;
        private List<String> tabList = new LinkedList<>();
        private List<String> wrongMostUserName = new LinkedList<>();
        private List<StudentPersonalAnalysis> studentPersonalAnalysisList = new LinkedList<>();
    }

    /**
     * 单个单词分析
     */
    @Getter
    @Setter
    public static class WordAnalysis implements Serializable {
        private static final long serialVersionUID = -2840183315456569330L;
        private String word;    //单词
        private int mishearNum; //听错的学生人数
        private int misLookNum; //认错的学生人数
        private int wrongNum;
    }

    @Getter
    @Setter
    public static class SentenceAnalysis implements Serializable {
        private static final long serialVersionUID = 8939191269014396085L;
        private String sentence;
        private int wrongNum;
    }

    @Getter
    @Setter
    public static class StudentPersonalAnalysis implements Serializable {
        private static final long serialVersionUID = 3176135950048031619L;
        //未完成的学生默认-1，排序
        private int wrongNum = -1;
        private String userName;
        private Long userId;
        private List<String> details = new LinkedList<>();
    }
}
