package com.voxlearning.utopia.service.newexam.api.mapper.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

/**
 * 用于统计部分的实体
 */
@Getter
@Setter
public class NewExamStatistics implements Serializable {
    private static final long serialVersionUID = -5321800832897942436L;
    private boolean issued;
    private String issueTime;
    private String issueText = "";//未发布时候文案
    private List<Paper> papers = new LinkedList<>();//题目成绩分析
    private AchievementAnalysisPart achievementAnalysisPart = new AchievementAnalysisPart();//总成绩分析
    private ScoreDistributionPart scoreDistributionPart = new ScoreDistributionPart();//成绩分布
    private String examReportUrl; //试卷报告url

    @Getter
    @Setter
    public static class Paper implements Serializable {
        private static final long serialVersionUID = 2188711692416923194L;
        private String paperId;
        private String paperName;
        private int joinNum;
        private Set<Long> userIds = new LinkedHashSet<>();
        private Map<String, ModuleAchievement> moduleAchievementMap = new LinkedHashMap<>();
        private List<ModuleAchievement> moduleAchievements = new LinkedList<>();
    }

    @Getter
    @Setter
    public static class AchievementAnalysisPart implements Serializable {
        private static final long serialVersionUID = 6989360805308539568L;
        private double standardScore;
        private int joinNum;
        private List<Double> scoreList = new LinkedList<>();
        private double maxScore;
        private double minScore;
        private double scoreRate;
        private double averScore;
        private double varianceScore;
        private double standardDeviationScore;
    }

    @Setter
    @Getter
    public static class ScoreDistributionPart implements Serializable {
        private static final long serialVersionUID = -7468964033390097072L;
        private int joinNum;
        private Map<Double, ScoreDistribution> scoreDistributionMap = new LinkedHashMap<>();
        private List<ScoreDistribution> scoreDistributions = new LinkedList<>();
    }

    @Getter
    @Setter
    public static class ScoreDistribution implements Serializable {
        private static final long serialVersionUID = -2026098119059482599L;
        private String decs;
        private String scoreDesc;
        private int max;
        private int min;
        private int num;
        private double rate;
    }


    @Setter
    @Getter
    public static class ModuleAchievement implements Serializable {
        private static final long serialVersionUID = -5279753116210442034L;
        private String desc;
        private double totalScore;
        private Set<Long> userIds = new LinkedHashSet<>();
        private double rate;
        private double averScore;
        private double standardScore;
    }


    @Getter
    @Setter
    public static class KnowledgePoint implements Serializable {
        private static final long serialVersionUID = 731326247825853733L;
        private String kid;
        private String desc;
        private double totalScore;
        private List<Integer> qidIndex = new LinkedList<>();
        private double standardTotalScore;
        private double rate;
    }

}
