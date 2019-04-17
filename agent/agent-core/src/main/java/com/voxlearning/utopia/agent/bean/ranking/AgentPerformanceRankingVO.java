package com.voxlearning.utopia.agent.bean.ranking;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 排行榜
 *
 * @author chunlin.yu
 * @create 2018-03-21 20:16
 **/
@Getter
@Setter
public class AgentPerformanceRankingVO implements Serializable{
    private static final long serialVersionUID = -8751127347846238224L;
    /**
     * 全国排名
     */
    private int ranking;

    private Long userId;

    private String userName;

    private Long groupId;

    private String groupName;

    /**
     * 指标数值
     */
    private double indicatorValue;

    /**
     * 是否属于本区
     */
    private boolean belongToOwnGroup;

    /**
     * 是否在同一个大区
     */
    private boolean inSameRegionGroup;



    /**
     * 排名增长情况，1：上涨，0：持平，-1：下跌，为null的时候，表示未知
     */
    private Integer growthSituation;

    /**
     * 排名浮动
     */
    private Integer rankingFloat;

    /**
     * 用户头像
     */
    private String userAvatar;


}
