package com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class LiveCastCommonData implements Serializable {
    private static final long serialVersionUID = 6412094940413904541L;
    private String qid;
    private String contentType = "无题型";
    private Integer difficultyInt;
    private int showType;
    private int rate;
    private List<AnswerType> answerTypes = new LinkedList<>();

    private Map<String, AnswerType> answerTypeMap = new HashMap<>();
    private int totalNum;
    private int rightNum;
    private String correctionImg;               // 批改图片

    private String standardAnswers;
    private String userAnswers;
    private List<List<Boolean>> subGrasp;
    private Boolean grasp;
    private boolean flag;


    /**
     *
     */
    @Getter
    @Setter
    public static class AnswerType implements Serializable {
        private static final long serialVersionUID = 7091648784998016735L;
        private String answer;
        private List<UserAnswerToQuestion> userAnswerToQuestions = new LinkedList<>();

    }

    /**
     * 学生做题信息
     */
    @Getter
    @Setter
    public static class UserAnswerToQuestion implements Serializable {
        private Long userId;
        private String userName;
        private String imgUrl;
    }


}
