package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class PsrPrimaryAppEnUserEkItem implements Serializable {

    private static final long serialVersionUID = -2603534504085348084L;

    private String ek;
    /** 状态 E D C B A S, S为最高级 掌握状态 */
    private Character status;
}
