package com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast;

import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class LiveCastBasicData implements Serializable {
    private static final long serialVersionUID = 2426104018909870383L;
    private Integer categoryId;
    private String lessonId;
    private String categoryName;
    private String lessonName;
    private String unitName;
    private String unitId;
    private int practiceCategory;
    private boolean needRecord;
    private List<PersonalStatistic> personalStatistics = new LinkedList<>();

    private Map<String, ContentStatistics> contentStatisticsMap = new LinkedHashMap<>();

    private List<ContentStatistics> contentStatisticsList = new LinkedList<>();

    @Getter
    @Setter
    public static class PersonalStatistic implements Serializable {
        private static final long serialVersionUID = 8281146511144715062L;
        private Long userId;
        private String userName;
        private int score;
        private List<String> voiceUrls = new LinkedList<>();
        private String voiceScoringMode;

    }

    @Getter
    @Setter
    public static class ContentStatistics implements Serializable {
        private static final long serialVersionUID = 6355698701116187562L;
        private String qId;
        private double totalScore;
        private int size = 1;
        private List<Sentence> sentences = new LinkedList<>();
        private AppOralScoreLevel appOralScoreLevel;
        private List<StudentContentInfo> studentContentInfo = new LinkedList<>();
        private List<StudentContentInfo> rightStudentInformation = new LinkedList<>();
        private List<StudentContentInfo> errorStudentInformation = new LinkedList<>();
        private int rate;
    }


    @Getter
    @Setter
    public static class Sentence implements Serializable {
        private Long sentenceId;
        private String sentenceContent;
    }

    @Getter
    @Setter
    public static class StudentContentInfo implements Serializable {
        private static final long serialVersionUID = 3985019694843241128L;
        private Long userId;
        private String userName;
        private List<String> voiceUrls;
        private String voiceScoringMode;
        private double score;
        private AppOralScoreLevel appOralScoreLevel;
    }
}
