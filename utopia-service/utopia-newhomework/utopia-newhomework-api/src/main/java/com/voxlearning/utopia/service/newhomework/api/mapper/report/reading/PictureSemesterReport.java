package com.voxlearning.utopia.service.newhomework.api.mapper.report.reading;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//班级能力分析
@Getter
@Setter
public class PictureSemesterReport implements Serializable {
    private static final long serialVersionUID = -3432031120076788595L;

    private int clazzLevel;//班级阅读水平
    private List<Map<String, Object>> readingLevelList;//阅读等级信息
    private Integer standardNum;//大纲推荐周阅读量
    private List<WeekPictureReport> weekPictureReports = new LinkedList<>();//周统计信息
    private List<List<PeerHomeworkReport>> peerHomeworkReports = new LinkedList<>();//单份作业信息（按月处理）

    @Getter
    @Setter
    public static class WeekPictureReport implements Serializable {
        private static final long serialVersionUID = -2787659700391822192L;
        private Date beginAt;
        private Date endAt;
        private String time;
        private Integer readCnt;//阅读量
    }

    @Getter
    @Setter
    public static class PeerHomeworkReport implements Serializable {
        private static final long serialVersionUID = 2039318626112468602L;
        private String hid;
        private Date beginAt;
        private String beginAtStr;
        private Integer avgScore;//平均分数
        private Integer avgDuration;//平均时间（分）
        private Integer additionCnt;//新增阅读量
        private Integer cumulativeCnt;//累计阅读量
    }
}
