package com.voxlearning.utopia.service.ai.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * ${app视频模块返回的Vo}
 *
 * @author zhiqi.yao
 * @create 2018-04-19 10:53
 **/
@Getter
@Setter
public class AIVideoHandouts implements Serializable {

    private static final long serialVersionUID = 7677243457557656724L;
    /**
     *讲义视频
     */
    public AIVideoConfig handouts;
    /**
     *
     */
    public List<AIVideoConfig> hotVideo;
    public List<AIVideoConfig> funnyVideo;
    public List<AIVideoConfig> activityVideo;
}
