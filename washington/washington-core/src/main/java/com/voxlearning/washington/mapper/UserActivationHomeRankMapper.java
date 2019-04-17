package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UserActivationHomeRankMapper implements Serializable{
    private static final long serialVersionUID = -6368437568338019177L;

    private Long userId;
    private Integer rank;
    private String name;
    private String avatar;
    private Integer level;
    private String levelName;
}
