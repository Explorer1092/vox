package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class PsrExamEnSimilarItemEx implements Serializable {
    public PsrExamEnSimilarItemEx() {
        eid = "";
        similarity = 0.0;
    }

    public PsrExamEnSimilarItemEx(String eid, Double similarity) {
        this.eid = eid;
        this.similarity = similarity;
    }

    public PsrExamEnSimilarItemEx(String eid, double similarity) {
        this.eid = eid;
        this.similarity = similarity;
    }

    private static final long serialVersionUID = 6851682129856316772L;
    private String eid;
    private Double similarity;
}
