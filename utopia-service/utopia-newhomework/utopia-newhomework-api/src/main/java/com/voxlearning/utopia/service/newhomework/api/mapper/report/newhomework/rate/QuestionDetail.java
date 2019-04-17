package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.rate;

import com.voxlearning.utopia.service.newhomework.api.constant.Correction;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QuestionDetail implements Serializable {
    private static final long serialVersionUID = -917425505126496138L;
    private String qid;
    private String testMethodName = "";
    private String contentType;
    private Integer difficulty;
    private int rate;
    private int errorNum;
    private int totalNum;
    private int showType;
    private String articleName = "";
    private String paragraphCName = "";
    private String answerWay = "";
    private List<Answer> errorAnswerList = new LinkedList<>();
    private Map<String, Answer> errorAnswerMap = new LinkedHashMap<>();
    private Answer rightAnswer;

    @Getter
    @Setter
    public static class Answer implements Serializable {
        private static final long serialVersionUID = -4722950200546505392L;
        private String answer;
        private List<StudentDetail> users = new LinkedList<>();
    }

    @Getter
    @Setter
    public static class StudentDetail implements Serializable {
        private static final long serialVersionUID = -2232376701246993467L;
        private String answer;
        private Long userId;
        private String userName;
        private String imgUrl;
        private List<String> showPics;
        private Boolean review;
        private Correction correction;
        private String correct_des;
    }
}
