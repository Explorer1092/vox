package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xinxin
 * @since 10/8/2016
 */
@Getter
@Setter
public class StudentGrowthMapper implements Serializable {
    private static final long serialVersionUID = -4096858301224337351L;

    private Long userId;
    private String userName;
    private String title;   //等级称号
    private Integer level;  //等级
    private Integer rank;   //排名
}
