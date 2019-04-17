package com.voxlearning.utopia.service.reward.api.mapper.newversion.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PlaytimeGameEntity implements Serializable {
    private Integer gameId;
    private String gameLinkUrl;
    private String gamePictueUrl;
}
