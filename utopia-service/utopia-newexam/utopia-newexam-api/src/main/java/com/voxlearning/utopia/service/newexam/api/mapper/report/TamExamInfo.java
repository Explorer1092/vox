package com.voxlearning.utopia.service.newexam.api.mapper.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TamExamInfo implements Serializable {
    private static final long serialVersionUID = -132784362350172918L;
    private double avgScore;
    private int submitNum;
    private String newExamId;

    public TamExamInfo(String newExamId) {
        this.newExamId = newExamId;
    }
}
