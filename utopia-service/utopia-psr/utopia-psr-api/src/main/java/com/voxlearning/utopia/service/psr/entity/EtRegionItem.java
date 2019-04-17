package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class EtRegionItem implements Serializable {

    private static final long serialVersionUID = 0L;

    /** 题型数量 */
    private int eidCount;
    /** 该地区该题型的热度近似值，用户排序 */
    private double hotLevel;
}
