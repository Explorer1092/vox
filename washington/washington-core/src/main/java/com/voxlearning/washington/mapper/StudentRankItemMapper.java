package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xinxin
 * @since 5/8/2016
 */
@Getter
@Setter
public class StudentRankItemMapper implements Serializable {
    private static final long serialVersionUID = 7555458081088684122L;

    private Long userId;
    private String name; //学生姓名
    private String imgUrl; //头像地址
    private Integer level; //等级
    private String title; //等级称号
    private Integer value; //成长值、学豆数、学霸次数
    private Integer likeCount; //点赞数
    private Boolean liked;    //当前用户是否已赞过
    private Integer rank;   //排名
    private String headWearImgUrl;
}
