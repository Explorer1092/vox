package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class MathEtTime implements Serializable {

    private static final long serialVersionUID = 594585649974010691L;

    private String et;
    private Integer time;

    public MathEtTime(String et, Integer time) {
        this.et = et;
        this.time = time;
    }
}
