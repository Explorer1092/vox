package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class ExamKcContent implements Serializable {

    private static final long serialVersionUID = -3628124136259196961L;

    private String type;
    private String ek;
    /** 正确率 */
    private Double accuracyRate;
    /** 做题次数 */
    private Integer count;
}
