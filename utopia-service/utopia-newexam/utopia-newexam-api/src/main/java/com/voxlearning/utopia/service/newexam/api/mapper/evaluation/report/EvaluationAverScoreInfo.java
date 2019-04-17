package com.voxlearning.utopia.service.newexam.api.mapper.evaluation.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class EvaluationAverScoreInfo implements Serializable {
    private static final long serialVersionUID = 923352148684420532L;
    private String paperId;
    private String paperName;
    private String newExamId;
    private boolean assigned;
    private int totalNum;
    private int totalScore;
    private int averScore;
}
