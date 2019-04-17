package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class EkRegionItem implements Serializable {

    private static final long serialVersionUID = 0L;

    /** 平均区分度 */
    private double avgDifferenty;
    /** 平均难度 */
    private double avgDifficulty;
    /** 该知识点在改地区做过的 题量 */
    private int eidCount;
    /** 该地区该题型的热度近似值 */
    private double hotLevel;
}
