package com.voxlearning.utopia.service.reward.api.mapper.newversion;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class HpPublicGoodMapper implements Serializable {
    private Long publicGoodId;
    private String status;
    private String publicGoodPictuerUrl;
}
