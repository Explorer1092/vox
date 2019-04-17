package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;

@Data
@Deprecated
public class ExamEnGlobalWrongItem implements Serializable {

    private static final long serialVersionUID = -3296983295127855260L;

    private String eid;
    private String ek;
    private String et;
    /** 准确率 */
    private Double rate;
    /** 做题总数 */
    private Integer sumCount;
    /** 做对总数 */
    private Integer rightCount;
    /** 做错的人数 */
    private Integer wrongCountPerson;
    /** 权重 根据权重排序 */
    private Double weight;
    /** 百分比轮盘算法归一化权重使用 */
    private Double weightPer;
}
