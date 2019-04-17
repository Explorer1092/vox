package com.voxlearning.utopia.service.newhomework.api.mapper.report;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;


//周报告包含各个班级名字信息
@Getter
@Setter
public class WeekReportClazzInfo implements Serializable {
    private static final long serialVersionUID = -111701988199589658L;

    private boolean shared = true;          //是否分享

    private Subject subject;

    private String subjectName;

    private boolean overTime;// 是否超时间

    private int lastTime;

    List<GroupToReport> groupToReports;


    @Getter
    @Setter
    public static class GroupToReport implements Serializable {
        private static final long serialVersionUID = 5368165737982134357L;
        private String groupIdToReportId;
        private String clazzName;
        private boolean shared = true;
        private int clazzLevel;
        private int level;

    }


}
