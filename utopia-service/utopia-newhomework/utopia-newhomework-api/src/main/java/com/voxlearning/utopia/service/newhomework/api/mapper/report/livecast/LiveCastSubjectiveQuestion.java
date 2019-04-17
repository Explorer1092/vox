package com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Setter
@Getter
public class LiveCastSubjectiveQuestion implements Serializable {
    private static final long serialVersionUID = 3865129814241094946L;
    private String qid;
    private int finishedNum;
    private int questionScore;
    private int totalNum;
    private Integer difficultyInt;
    private String contentType = "无题型";
    private List<StudentSubjectiveQuestionInfo> studentSubjectiveQuestionInfos = new LinkedList<>();
    private boolean flag;

    @Setter
    @Getter
    public static class StudentSubjectiveQuestionInfo implements Serializable {
        private static final long serialVersionUID = 5918511656426422899L;
        private Long useId;
        private String useName;
        private String processId;
        private String comment;
        private List<String> useAnswerPicture = new LinkedList<>();
        private List<String> teacherCorrectingPicture = new LinkedList<>();
        private double score;
        private boolean corrected;
        private double percentage;
        private List<String> voice;
    }
}
