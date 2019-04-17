package com.voxlearning.utopia.service.reward.api.mapper.newversion.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class TobyDressCVOption implements Serializable {
    private Double price;
    private Integer activeDay;
    private Integer priceNumType;
}
