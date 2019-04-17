package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework;

import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BasicAppQuestion implements Serializable {
    private static final long serialVersionUID = -4498850073882061506L;

    public BasicAppQuestion(String qid) {
        this.questionId = qid;
    }

    private String questionId;
    private List<Map<String, Object>> sentences;
    private int errorRate;
    private int num;
    private double totalScore;
    private AppOralScoreLevel appOralScoreLevel;
    private List<BasicAppQuestionUser> rightStudentInformation = new LinkedList<>();
    private List<BasicAppQuestionUser> errorStudentInformation = new LinkedList<>();
    private List<BasicAppQuestionUser> users = new LinkedList<>();

    @Getter
    @Setter
    public static class BasicAppQuestionUser implements Serializable {
        private static final long serialVersionUID = 7754641915869298790L;
        private Long uid;
        private String userName;
        private List<String> voiceUrls = new LinkedList<>();
        private double score;
        private String voiceScoringMode;
        private AppOralScoreLevel appOralScoreLevel;
    }

}
