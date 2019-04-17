package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class ExamEnTestFiveEidItem implements Serializable {
    private static final long serialVersionUID = 3693908392463409175L;

    private String ek;
    private String eid;
    private String et;
    private String algov;
    private Double weight;
    private Integer eidStatus;

    public ExamEnTestFiveEidItem() {
        weight = 0.0;
        eidStatus = 0;
    }
}
