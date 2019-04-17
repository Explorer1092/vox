package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class ExamIcContent implements Serializable {

    private static final long serialVersionUID = -643288412064543443L;

    private String type;
    /** 题ID */
    private String eid;
    /** 正确率 */
    private Double accuracyRate;
    /** 做题次数 */
    private Integer count;
    /** 题型 */
    private String eType;
}
