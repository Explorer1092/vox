package com.voxlearning.utopia.service.reward.api.mapper.newversion;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class HpAdMapper implements Serializable {
    private Long asId;
    private String adUrl;
    private String adPictuerUrl;
}
