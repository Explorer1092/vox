package com.voxlearning.utopia.service.reward.api.mapper.newversion.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PlaytimeVideoEntity implements Serializable {
    private String videoLinkUrl;
    private String videoPictueUrl;
    private Integer videoId;
}
