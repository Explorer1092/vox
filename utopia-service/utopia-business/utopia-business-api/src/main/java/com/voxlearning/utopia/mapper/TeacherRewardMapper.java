package com.voxlearning.utopia.mapper;

import lombok.Data;

import java.io.Serializable;

/**
 * 老师奖励mapper，目前包括智慧课堂和星星奖励
 * Created by Shuai Huan on 2015/7/14.
 */
@Data
public class TeacherRewardMapper implements Serializable {

    private String rewardContent;
    private String date;
    private String type;
}
