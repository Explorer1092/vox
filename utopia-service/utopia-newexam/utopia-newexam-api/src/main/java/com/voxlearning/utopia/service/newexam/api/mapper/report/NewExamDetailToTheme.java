package com.voxlearning.utopia.service.newexam.api.mapper.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Setter
@Getter
public class NewExamDetailToTheme implements Serializable {
    private String desc;
    private double averScore;
    private long averDuration;//秒
    private int num;
    private double totalScore;
    private long totalDuration;
    private Set<Long> userIds = new LinkedHashSet<>();
    private List<NewExamDetailToQuestion> newExamQuestionDetailToQuestions = new LinkedList<>();
    private List<NewExamDetailToSubQuestion> subQuestions = new LinkedList<>();

    @Setter
    @Getter
    public static class NewExamDetailToQuestion implements Serializable {
        private static final long serialVersionUID = 7265682270148266256L;
        private String qid;
        private String docId;
        private boolean oral;
        private double totalScore;
        private long totalDuration;
        private List<NewExamDetailToSubQuestion> subQuestions = new LinkedList<>();
    }

    @Setter
    @Getter
    public static class NewExamDetailToSubQuestion implements Serializable {
        private static final long serialVersionUID = 4434235827850696489L;
        private String qid;
        private int type;
        private double totalScore;
        private int subIndex;
        private int index;
        private int num;// 参与人数
        private boolean group;//是否掌握
        private double rate;
        private double averScore;
        private double standardScore;
    }
}
