package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class PsrPrimaryAppEnItem implements Serializable {

    private static final long serialVersionUID = 6707996988662436882L;

    /** 题目ID */
    private String eid;
    /** 推题类型 */
    private String eType;
    /** 该EID对应的状态：E D C B A S五种 */
    private Character status;
    /** 计算得出的权重 */
    private double weight;
    /** alogv 算法类型 */
    private String algov;
}
