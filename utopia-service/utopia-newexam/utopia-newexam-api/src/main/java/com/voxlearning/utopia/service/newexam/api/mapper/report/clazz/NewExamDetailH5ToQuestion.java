package com.voxlearning.utopia.service.newexam.api.mapper.report.clazz;

import com.voxlearning.utopia.service.newexam.api.constant.NewExamQuestionType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionOralDictAnswer;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class NewExamDetailH5ToQuestion implements Serializable {
    private static final long serialVersionUID = 6851051818750724598L;
    private String qid;
    private List<SubQuestion> subQuestions = new LinkedList<>();

    @Getter
    @Setter
    public static class SubQuestion implements Serializable {
        private static final long serialVersionUID = -3226916788334542839L;
        private double averScore;
        private String analysis;
        private double StandardScore;
        private int subIndex;
        private int index;
        private String qid;
        private double rate;
        private int type;
        private NewExamQuestionType newExamQuestionType;
        private String standardAnswer;
        private double totalScore;
        private int num;
        private int rightNum;
        private int wrongNum;
        private List<StudentAnswer> kouYuAnswer = new LinkedList<>();
        private List<NewQuestionOralDictAnswer> kouYuReferenceAnswers;//
        private List<Answer> xuanZeAnswer = new LinkedList<>();
        private Map<String, Answer> xuanZeAnswerMap = new LinkedHashMap<>();
        private List<StudentAnswer> tianKongRightStudents = new LinkedList<>();
        private List<StudentAnswer> tianKongWrongStudents = new LinkedList<>();
    }


    @Setter
    @Getter
    public static class StudentAnswer implements Serializable {
        private static final long serialVersionUID = -4242339630270112358L;
        private String userName;
        private Long userId;
        private String answer;
        private List<String> voiceUrls = new LinkedList<>();
        private List<Map<String, Object>> answerList = new LinkedList<>();
        private double score;
    }


    @Getter
    @Setter
    public static class Answer implements Serializable {
        private static final long serialVersionUID = 6147468408340735507L;
        private String answer;
        private boolean grasp;
        private List<StudentAnswer> students = new LinkedList<>();
    }

}
