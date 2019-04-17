package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class PsrPrimaryAppUserEkItem implements Serializable {

    private static final long serialVersionUID = 6186666422636774821L;

    private String ek;
    /** 状态为 E D C B A S，S为最高级 掌握状态 */
    private Character status;
}
