package com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Setter
@Getter
public class BasicAppQuestionInfo implements Serializable {
    private static final long serialVersionUID = -3581036203377739364L;
    private String questionId;
    private List<String> answerRightInfo = new LinkedList<>();
    private List<String> answerErrorInfo = new LinkedList<>();
    private List<BasicAppSentence> sentences = new LinkedList<>();
    private Boolean needRecord;
    private int rightProportion;
    private int errorProportion;

    private List<BasicAppRecordInfo> recordInfo = new LinkedList<>();


    private String answerResultWord;
    private BasicAppRecordInfo basicAppRecordInfo;
    private Boolean grasp;

    private boolean flag;

    @Setter
    @Getter
    public static class BasicAppSentence implements Serializable {
        private static final long serialVersionUID = -7658783558427834127L;
        private Long sentenceId;
        private String sentenceContent;
    }

    @Getter
    @Setter
    public static class BasicAppRecordInfo implements Serializable {
        private static final long serialVersionUID = -2640983106661732707L;
        private String score;
        private int realScore;
        private Long userId;
        private String userName;
        private List<String> userVoiceUrl = new LinkedList<>();
        private String voiceScoringMode;
    }

}
