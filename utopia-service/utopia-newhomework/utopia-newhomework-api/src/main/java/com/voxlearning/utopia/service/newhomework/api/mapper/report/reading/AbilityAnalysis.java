package com.voxlearning.utopia.service.newhomework.api.mapper.report.reading;

import com.voxlearning.utopia.service.newhomework.api.constant.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//绘本报告：学生能力分析
@Getter
@Setter
public class AbilityAnalysis implements Serializable {
    private static final long serialVersionUID = -317570152107219305L;
    private int clazzLevel;//班级阅读水平
    private List<Map<String, Object>> readingLevelList;//阅读等级信息
    private ClazzPart clazzPart = new ClazzPart();//班级详情
    private List<StudentPartRecord> studentPartRecordList = new LinkedList<>();//学生详情

    @Getter
    @Setter
    public static class ClazzPart implements Serializable {
        private static final long serialVersionUID = 6834079879164180741L;
        private DecodingAbilityLevelModule decodingAbilityLevel = new DecodingAbilityLevelModule(); //解码能力
        private PhoneticKnowledgeLevelModule phoneticKnowledgeLevel = new PhoneticKnowledgeLevelModule(); //语言知识
        private ReadingComprehensionLevelModule readingComprehensionLevel = new ReadingComprehensionLevelModule(); //阅读理解
        private CulturalConsciousnessLevelModule culturalConsciousnessLevel = new CulturalConsciousnessLevelModule(); //文化意识
        private ReadingHabitsLevelModule readingHabitsLevel = new ReadingHabitsLevelModule();//阅读习惯
    }

    @Getter
    @Setter
    public static class DecodingAbilityLevelModule implements Serializable {
        private static final long serialVersionUID = 8355310571289347125L;
        private Integer level = DecodingAbilityLevel.prepare.getLevel();
        private int totalSize = 8;
        private String desc = DecodingAbilityLevel.prepare.getDesc();
        private String detail = DecodingAbilityLevel.prepare.getDetail();
    }

    @Getter
    @Setter
    public static class PhoneticKnowledgeLevelModule implements Serializable {
        private static final long serialVersionUID = -9021702022811876110L;
        private Integer level = PhoneticKnowledgeLevel.prepare.getLevel();
        private int totalSize = PhoneticKnowledgeLevel.phoneticKnowledgeLevelMap.size();
        private String desc = PhoneticKnowledgeLevel.prepare.getDesc();
        private String detail = PhoneticKnowledgeLevel.prepare.getDetail();
    }

    @Getter
    @Setter
    public static class ReadingComprehensionLevelModule implements Serializable {
        private static final long serialVersionUID = -116634684733878110L;
        private Integer level = ReadingComprehensionLevel.prepare.getLevel();
        private int totalSize = ReadingComprehensionLevel.readingComprehensionLevelMap.size();
        private String desc = ReadingComprehensionLevel.prepare.getDesc();
        private String detail = ReadingComprehensionLevel.prepare.getDetail();
    }


    @Getter
    @Setter
    public static class CulturalConsciousnessLevelModule implements Serializable {
        private static final long serialVersionUID = 6467640377725356395L;
        private Integer level = CulturalConsciousnessLevel.prepare.getLevel();
        private int totalSize = CulturalConsciousnessLevel.culturalConsciousnessLevelMap.size();
        private String desc = CulturalConsciousnessLevel.prepare.getDesc();
        private String detail = CulturalConsciousnessLevel.prepare.getDetail();
    }

    @Getter
    @Setter
    public static class ReadingHabitsLevelModule implements Serializable {
        private static final long serialVersionUID = -5867717837967245443L;
        private Integer level = ReadingHabitsLevel.prepare.getLevel();
        private int totalSize = ReadingHabitsLevel.readingHabitsLevelMap.size();
        private String desc = ReadingHabitsLevel.prepare.getDesc();
        private String detail = ReadingHabitsLevel.prepare.getDetail();
    }


    @Getter
    @Setter
    public static class StudentPartRecord implements Serializable {
        private static final long serialVersionUID = -3406527912161490896L;
        private String userName;
        private Long userId;
        private int cumulativeBookCnt;
        private int cumulativeVocabularyCnt;
        private int cumulativeDuration;
        private String cumulativeDurationStr;
        private int avgScore;
        private int readingFrequency;
    }
}
