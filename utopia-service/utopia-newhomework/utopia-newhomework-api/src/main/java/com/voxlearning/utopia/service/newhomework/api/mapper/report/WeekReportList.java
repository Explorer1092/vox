package com.voxlearning.utopia.service.newhomework.api.mapper.report;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 周报告列表
 */
@Setter
@Getter
public class WeekReportList implements Serializable {
    private static final long serialVersionUID = -4545370786163435563L;

    private Map<Subject, List<WeekReportBrief>> weekReportBriefMap = new HashMap<>();//各个学科对应周报

    //周报告简介
    @Getter
    @Setter
    public static class WeekReportBrief implements Serializable {
        private static final long serialVersionUID = -9019104617230075689L;

        private String startTime;//开始时间

        private String teacherIdReportEndTime;

        private String endTime;//结束时间

        private int checkedNum;//查看家长人数

        private boolean shared = true;//是否分享

        private boolean showCheckedNum;

        private String path;

        private boolean overtime;//剩余分享时间，超时的时候是-1

        private List<String> groupIdAndReportId = new LinkedList<>();//这份报告存在的groupId|reportId

    }




}
