package com.voxlearning.utopia.agent.mapper;

import com.voxlearning.utopia.data.ActivityBaseRule;
import com.voxlearning.utopia.data.ActivityReport;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class ActivityConfigMapper implements Serializable {

    private String id;

    private String type;

    private String title; // 标题

    private String description; // 描述

    private ActivityReport report;

    private List<String> clazzLevels;

    private List<Map> clazzs;

    private List<Long> schoolIds;

    private List<Long> areaIds;

    private String schoolName;

    private String startTime;

    private String endTime;

    private String createTime;

    private Long startDays;

    private Long endDays;

    private ActivityBaseRule rules;

    private Integer status;

    private Integer activityStatus; // 活动的状态 ：1 进行中 2 未开始 3结束

}
