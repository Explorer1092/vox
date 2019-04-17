package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class NewQuestionReportBO implements Serializable {
    private static final long serialVersionUID = -4874017214826245756L;
    private String qid;
    private String contentType;
    private int type = 1;
    private int difficulty;
    private String difficultyName;
    private int errorNum;
    private int errorRate;
    private int num;
    private String questionAnswer;
    private String lessonId;
    private List<SubQuestion> subQuestions = new LinkedList<>();

    public NewQuestionReportBO(String qid) {
        this.qid = qid;
    }

    @Getter
    @Setter
    public static class SubQuestion implements Serializable {
        private static final long serialVersionUID = 7902592244314744118L;
        private List<Map<String, Object>> answer = new LinkedList<>();
        private Map<String, List<UserToQuestion>> map = new LinkedHashMap<>();
        private Map<String,List<String>> userAnswersMap = new LinkedHashMap<>();
        private List<UserToQuestion> users = new LinkedList<>();
    }


    @Getter
    @Setter
    public static class UserToQuestion implements Serializable {
        private static final long serialVersionUID = -1496408300388565728L;
        private String answer;
        private Long uid;
        private String userName;
        private int realScore;
        private String score;
        private List<String> userVoiceUrls = new LinkedList<>();
    }
}
