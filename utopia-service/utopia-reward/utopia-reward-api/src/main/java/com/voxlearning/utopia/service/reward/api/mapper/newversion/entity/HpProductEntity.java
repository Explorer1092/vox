package com.voxlearning.utopia.service.reward.api.mapper.newversion.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class HpProductEntity implements Serializable {
    private Integer productId;
    private String name;
    private String pice;
    private Integer achieveNum;
    private String pictuerUrl;
    private Integer tag;
}
