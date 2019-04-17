package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class PsrExamItem implements Serializable {

    private static final long serialVersionUID = 8721503376710875590L;

    /** EK 知识点 */
    private String ek;
    /** EID 题目ID */
    private String eid;
    /** ET 题型 */
    private String et;
    /** 预估通过率 另外计算 */
    private double weight;
    /** algov 算法类型， fuck to algov */
    private String alogv;
    /** psr内部逻辑类型，记录该EID是由那个逻辑推出来的 */
    private String psrExamType;
}
