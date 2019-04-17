package com.voxlearning.utopia.service.newexam.api.mapper.report.personal;

import com.voxlearning.utopia.service.question.api.entity.NewQuestionOralDictAnswer;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author majianxin
 */
@Getter
@Setter
public class NewExamStudentQuestion implements Serializable {

    private static final long serialVersionUID = -6270859483806106000L;
    private String qid;
    private List<List<String>> userAnswer;
    private List<List<Boolean>> subGrasp;
    private List<SubQuestion> subQuestions = new LinkedList<>();

    @Getter
    @Setter
    public static class SubQuestion implements Serializable {
        private static final long serialVersionUID = -2876393597345690201L;

        private String qid;
        // private String standardAnswer;
        private int index;
        private double standardScore;
        private double userScore;
        private String examQuestionType;//题目展示类型
        private boolean grasp;//是否掌握

        /* 口语属性 */
        private List<String> voiceUrlList = new LinkedList<>();
        private List<NewQuestionOralDictAnswer> referenceAnswers;
    }

}