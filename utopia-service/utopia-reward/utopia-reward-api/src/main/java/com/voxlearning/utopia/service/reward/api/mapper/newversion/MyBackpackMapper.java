package com.voxlearning.utopia.service.reward.api.mapper.newversion;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class MyBackpackMapper implements Serializable {
    private Long prizeId;
    private String prizeName;
    private Integer prizeType;
    private Integer fragmentNum;
    private String prizePictuerUrl;
}
