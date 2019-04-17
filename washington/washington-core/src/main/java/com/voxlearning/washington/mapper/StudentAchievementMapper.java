package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xinxin
 * @since 12/8/2016
 */
@Getter
@Setter
public class StudentAchievementMapper implements Serializable {
    private static final long serialVersionUID = 8132657590679952590L;

    private String type;
    private String title;
    private Integer level;
    private Long userId;
    private String userName;
    private String userImg; //用户头像
    private String receiveDate; //成就获取时间
    private Integer likeCount;  //被赞数量
    private Boolean liked;  //当前用户是否已赞

    private Integer current;    //当前成就值
    private Integer next;       //下一等级所需的成就值
    private Integer last;       //当前等级升级所需的成就值
}
