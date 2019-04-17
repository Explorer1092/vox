package com.voxlearning.utopia.service.newhomework.api.mapper.report.reading;

import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.ReadingDubbingRecommend;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PictureInfo implements Serializable {
    private static final long serialVersionUID = 3895546742573337551L;
    private String pictureId;
    private String pictureName;
    private PicturePart picturePart = new PicturePart();//绘本信息
    private boolean containsDubbing;//是否包含配音
    private boolean recomended;//是否配音推荐
    private List<ReadingDubbingRecommend.ReadingDubbing> recommendParts = new LinkedList<>();//推荐信息：1，推荐的时候，显示推荐的内容2.没推荐的时候，显示要推荐的配音
    private List<StudentRecord> studentRecords = new LinkedList<>();//学生做题信息
    private QuestionDetailPart questionDetailPart = new QuestionDetailPart();//题目分析信息


    @Getter
    @Setter
    public static class PicturePart implements Serializable {
        private static final long serialVersionUID = -4377748530798729305L;
        private Map<String, Object> pictureMap;
        private int finishedNum;
        private int totalNum;
        private int avgScore;
        private int totalScore;
    }


    @Getter
    @Setter
    public static class StudentRecord implements Serializable {
        private static final long serialVersionUID = 2429734457936383876L;
        private Long userId;
        private String userName;
        private boolean finished;
        private String dubbingId;
        private int score;
        private String dubbingLevel = "--";
        private List<String> voices = new LinkedList<>();
        private Long duration;
        private String durationStr;
        private double dubbingScore;
    }

    @Getter
    @Setter
    public static class QuestionDetailPart implements Serializable {
        private static final long serialVersionUID = -959512031891830165L;
        private List<SentenceDetail> sentenceDetails = new LinkedList<>();//录音题
        private List<QuestionDetail> questionDetails = new LinkedList<>();//简答题
    }

    @Getter
    @Setter
    public static class SentenceDetail implements Serializable {
        private static final long serialVersionUID = -8596507713585252510L;
        private String qid;
        private String text;
        private double totalScore;
        private int totalNum;
        private int avgScore;
        private List<Map<String, Object>> voiceInfo = new LinkedList<>();
    }

    @Getter
    @Setter
    public static class QuestionDetail implements Serializable {
        private static final long serialVersionUID = -5709919449733887387L;
        private String qid;
        private boolean showRate;
        private int totalNum;
        private Map<String, QuestionAnswerInfo> questionAnswerInfoMap = new LinkedHashMap<>();
        private List<QuestionAnswerInfo> questionAnswerInfos = new LinkedList<>();
    }

    @Getter
    @Setter
    public static class QuestionAnswerInfo implements Serializable {
        private static final long serialVersionUID = 1561418476376783049L;
        private int rate;
        private int finishNum;
        private String answer;
        private List<Map<String, Object>> userInfo = new LinkedList<>();
    }

}
