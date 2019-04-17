package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class PsrPrimaryAppMathItem implements Serializable {

    private static final long serialVersionUID = -1499813781838291549L;

    /** 知识点ID */
    private String ek;
    /** 推题类型 */
    private String eType;
    /** 单位秒 S */
    private Integer time;
    /** 该EID对应的状态：E D C B A S五种 */
    private Character status;
    /** 计算得出的权重 */
    private double weight;
    /** alogv 算法类型 */
    private String algov;
}
