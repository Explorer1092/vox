package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xinxin
 * @since 29/8/2016
 */
@Getter
@Setter
public class StudentAttendanceRankMapper implements Serializable {
    private static final long serialVersionUID = -4344713851243685327L;

    private Long userId;
    private String userImg; //头像
    private String userName;
    private Integer count;  //签到次数
    private String lastSignDate;    //最后一次签到时间
    private Integer rank;   //排名
    private Integer likeCount;  //当月被点赞次数
    private Boolean liked; //当天是否被当前用户点赞
}
