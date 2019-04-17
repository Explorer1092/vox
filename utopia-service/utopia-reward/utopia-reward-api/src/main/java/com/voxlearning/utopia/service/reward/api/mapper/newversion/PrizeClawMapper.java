package com.voxlearning.utopia.service.reward.api.mapper.newversion;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PrizeClawMapper implements Serializable {
    private Long id;
    private String prizeName;
    private Integer prizeType;
    private String prizePicterUrl;
}
