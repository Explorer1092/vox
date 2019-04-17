package com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class ReadingExercisesInfo implements Serializable {
    private static final long serialVersionUID = 7094626628846784863L;
    private int rightNum;
    private int totalExercises;
    private List<ExercisesQuestionInfo> exercisesQuestionInfo = new LinkedList<>();


    @Getter
    @Setter
    public static class OralQuestion implements Serializable {
        private static final long serialVersionUID = 5342812840433626708L;
        private String text;
        private String audio;
    }


    @Getter
    @Setter
    public static class ExercisesQuestionInfo implements Serializable {

        private static final long serialVersionUID = 3480391765602434689L;
        private String questionId;
        private String userAnswers;
        private String standardAnswers;
        private String difficultyName;
        private String questionType;

    }

}
