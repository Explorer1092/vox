package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 阿分题排行榜信息
 *
 * @author Maofeng Lu
 * @since 13-9-22 上午10:51
 */
public class AfentiArenaRankInfoMapper implements Serializable {

    private static final long serialVersionUID = -832106967002730217L;

    @Getter @Setter private Long userId;                //用户ID
    @Getter @Setter private String userName;            //用户名字
    @Getter @Setter private String userImg;             //用户头像
    @Getter @Setter private Integer afentiBean;         //积分
    @Getter @Setter private String clazzName;           //班级名称
    @Getter @Setter private String englishTeacherName;  //英语老师
    @Getter @Setter private String schoolName;          //学校名称
    @Getter @Setter private Integer userArenaRank;      //用户竞技场排名
}
