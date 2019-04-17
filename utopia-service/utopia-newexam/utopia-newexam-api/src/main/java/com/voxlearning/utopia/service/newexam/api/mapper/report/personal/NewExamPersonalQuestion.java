package com.voxlearning.utopia.service.newexam.api.mapper.report.personal;

import com.voxlearning.utopia.service.newexam.api.constant.NewExamQuestionType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionOralDictAnswer;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class NewExamPersonalQuestion implements Serializable {

    private static final long serialVersionUID = -9019317337799499315L;
    private String qid;

    private List<NewExamPersonalSubQuestion> subQuestions = new LinkedList<>();

    @Getter
    @Setter
    public static class NewExamPersonalSubQuestion implements Serializable {
        private static final long serialVersionUID = -6205171567232340142L;
        private double standardScore;
        private String standardAnswer;
        private int subIndex;
        private int index;
        private boolean hasAnswer;
        private String qid;
        private int type;
        private NewExamQuestionType newExamQuestionType;
        private List<NewQuestionOralDictAnswer> referenceAnswers;//
        private String analysis;
        private double personalScore;
        private List<String> voiceUrlList = new LinkedList<>();
        private String personalAnswer;
        private boolean personalGrasp;//是否掌握
        private List<Map<String, Object>> personalAnswerDetail = new LinkedList<>();
    }

}