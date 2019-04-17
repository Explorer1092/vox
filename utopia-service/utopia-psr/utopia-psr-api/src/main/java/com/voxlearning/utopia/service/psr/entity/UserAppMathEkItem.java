package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserAppMathEkItem implements Serializable {

    private String ek;
    /** 状态 E D C B A S，S为最高级 掌握状态 */
    private Character status;
    private int days;
    /** 知识点体系中 高级节点个数 */
    private int subsetCount;
    /** 准确率 */
    private double accuracyRate;
    /** 综合权重 */
    private double weight;
    /** 归一化权重，综合权重/权重总和，用于轮盘选知识点 [ 0 - 100 ] */
    private double weightPer;
    private List<UserAppMathEtItem> eStatusEtList;

    public UserAppMathEkItem() {
        status = 'E';
        days = 0;
        subsetCount = 0;
        accuracyRate = -1.0;
        weight = 0.0;
        weightPer = 0.0;

        eStatusEtList = new ArrayList<>();
    }

    public double getStatusWeight() {
        double ret = 1.0;
        switch (status) {
            case 'D':
                ret = 1.0;
                break;
            case 'C':
                ret = 0.8;
                break;
            case 'B':
                ret = 0.5;
                break;
            case 'A':
                ret = 0.2;
                break;
            default:
                ret = 0.0;
        }
        return ret * 100;
    }

    public double getAccuracyRateWeight() {
        return (40 * (1 - accuracyRate));
    }

    public boolean isOutOfDays() {
        int outDays = 0;
        switch (status) {
            case 'D':
                outDays = 2;
                break;
            case 'C':
                outDays = 4;
                break;
            case 'B':
                outDays = 7;
                break;
            case 'A':
                outDays = 14;
                break;
            case 'S':
            case 'E':
            default:
                outDays = 0;
        }
        if (days > outDays) return true;

        return false;
    }
}
