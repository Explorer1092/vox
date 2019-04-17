package com.voxlearning.utopia.service.reward.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PGDonateMsg implements Serializable{

    private static final long serialVersionUID = 2289514619689001513L;

    private String collectId;
    private Long money;
    private Boolean finish;
    private Long userId;

}
