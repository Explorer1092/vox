package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class EidItem implements Serializable {

    private static final long serialVersionUID = 0L;

    private String eid;
    private String et;            // 该eid的题型
    private int rightCount;       // 作对的题目数量
    private int allCount;         // 总题目数量
    private double accuracyRate;  // 题目准确率
    private double predictRate;   // 预估通过率
    private double irtA;          // IRT-a题目区分度
    private double irtB;          // IRT-b题目难度
    private double irtC;          // IRT-c题目难度, 新版irt参数

    public void EidItem() {
        eid = "";
        et = "";
        rightCount = 0;
        allCount = 0;
        accuracyRate = 0.0;
        predictRate = 0.0;
        irtA = 0.0;
        irtB = 0.0;
        irtC = 0.0;
    }
}
