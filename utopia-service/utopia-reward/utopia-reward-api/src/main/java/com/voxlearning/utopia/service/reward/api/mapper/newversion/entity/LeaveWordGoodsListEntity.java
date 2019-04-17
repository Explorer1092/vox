package com.voxlearning.utopia.service.reward.api.mapper.newversion.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class LeaveWordGoodsListEntity implements Serializable {
    private Long id;
    private String name;
    private String portraitUrl;
    private Integer price;
    private Integer spendType;
}
