package com.voxlearning.utopia.service.reward.api.mapper.newversion.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class HpPublicGoodPlaqueEntity implements Serializable {
    private Integer joinNum = 0;
    private Long donationIntegralNum  = 0L;
    private Integer honorNum  = 0;
}
