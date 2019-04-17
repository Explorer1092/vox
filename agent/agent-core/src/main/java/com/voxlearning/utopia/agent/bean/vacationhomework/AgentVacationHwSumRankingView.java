package com.voxlearning.utopia.agent.bean.vacationhomework;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 假期作业排行榜
 *
 * @author deliang.che
 * @since  2019/1/3
 **/
@Getter
@Setter
public class AgentVacationHwSumRankingView implements Serializable{

    private static final long serialVersionUID = -3961161041253154856L;
    private int ranking;

    private Long userId;

    private String userName;

    private Long groupId;

    private String groupName;

    private double indicatorValue;//指标数值

    private boolean belongToOwnGroup;//是否属于本区

    private boolean inSameRegionGroup;//是否在同一个大区


    private Integer growthSituation;//排名增长情况，1：上涨，0：持平，-1：下跌，为null的时候，表示未知

    private Integer rankingFloat;//排名浮动

    private String userAvatar;//用户头像
}
