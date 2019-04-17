package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserAppMathEtItem implements Serializable {

    private static final long serialVersionUID = 5988017064769068018L;

    private String et;
    private Integer time;
    /** 准确率 */
    private double accuracyRate;
    /** 综合权重 */
    private double weight;
    /** 归一化权重，综合权重/权重总和，用于轮盘选知识点 [0 - 100] */
    private double weightPer;

    public UserAppMathEtItem() {
        accuracyRate = -1.0;
        weight = 0.0;
        weightPer = 0.0;
        time = 0;
    }
}
