package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UserActivationRankMapper implements Serializable{
    private static final long serialVersionUID = -2281488301035898299L;


    private Long userId;
    private String name;
    private Integer level;
    private String levelName;
    private Integer rank;
    private String avatar;
    private Long value;
}
