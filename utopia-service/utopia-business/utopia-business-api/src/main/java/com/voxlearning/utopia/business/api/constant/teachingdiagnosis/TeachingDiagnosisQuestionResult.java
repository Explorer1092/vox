package com.voxlearning.utopia.business.api.constant.teachingdiagnosis;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
@Getter
@Setter
public class TeachingDiagnosisQuestionResult implements Serializable {
    private Long studentId;
    private boolean master;
    private List<List<String>> userAnswer;
    private String questionId;
    private String courseId;
    private String taskId;
    private Date finishTime;
    private Long duration;
}
