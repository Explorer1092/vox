package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserExamUcContent implements Serializable {

    private static final long serialVersionUID = 1548155032777510818L;

    private String type;
    private Long userId;
    /** 用户的能力值 */
    private Double uc;
    /** 上一天做题正确率 */
    private Double dayAccuracyRate;
    /** 总做题正确率 */
    private Double allAccuracyRate;
    /** 上一天做题数量 */
    private Integer dayCount;
    /** 总做题数量 */
    private Integer allCount;
}

