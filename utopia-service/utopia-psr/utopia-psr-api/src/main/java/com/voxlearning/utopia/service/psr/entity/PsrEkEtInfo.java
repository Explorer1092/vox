package com.voxlearning.utopia.service.psr.entity;

/**
 * Created by Administrator on 2016/2/4.
 */

import lombok.Data;

@Data
public class PsrEkEtInfo {
    private String ek;
    private String et;
    private Integer maxCount;
    private Double weight;
    private Double weightPer;
}
