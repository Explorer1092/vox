package com.voxlearning.utopia.service.mizar.api.mapper;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCourse;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCoursePeriod;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Yuechen Wang on 2016/12/19.
 * 课时详情 展示实体
 */
@Getter
@Setter
public class CoursePeriodMapper implements Serializable {

    private static final long serialVersionUID = 1L;
    private String courseId;                // 关联课程ID
    private String courseName;              // 课程名称
    private Boolean payAll;                 // 是否按课程购买
    private String qqTip;                   // 课程加群提示文字
    private String qqUrl;                   // 课程加群链接
    private String category;                // 课程分类
    private String periodId;                // 课时ID
    private String periodName;              // 课时名称
    private Double price;                   // 课时价格
    private Date startTime;                 // 课时开始时间
    private Date endTime;                   // 课时结束时间
    private String intro;                   // 内容详情
    private String img;                     // 课时图片
    private String video;                   // 视频地址
    private String tip;                     // 备注提示文字
    private String lecturerName;            // 主讲人名称
    private String lecturerAvatar;          // 主讲人头像
    private String liveUrl;                 // 直播地址
    private String replayUrl;               // 回放地址
    private String btnContent1;             // 自定义文字1
    private String btnContent2;             // 自定义文字2
    private List<PeriodInfo> series;        // 系列课程
    private String longClassUrl;            // 配套长期班URL
    private String longClassPhoto;          // 配套长期班图片

    public static CoursePeriodMapper newInstance() {
        return new CoursePeriodMapper();
    }

    public CoursePeriodMapper withCourse(MicroCourse course) {
        if (course == null) {
            return this;
        }
        this.courseId = course.getId();
        this.courseName = course.getName();
        this.category = course.getCategory();
        this.payAll = Boolean.TRUE.equals(course.getPayAll());
        this.qqTip = course.getQqTip();
        this.qqUrl = course.getQqUrl();
        // 如果按照课程购买，更新价格以及按钮文字
        if (payAll) {
            this.price = course.getPrice();
            this.btnContent1 = this.btnContent2 = course.getBtnContent();
            this.tip = course.getTip();
        }
        return this;
    }

    public CoursePeriodMapper withPeriod(MicroCoursePeriod period) {
        if (period == null) {
            return this;
        }
        this.periodId = period.getId();
        this.periodName = period.getTheme();
        this.intro = period.getInfo();
        this.img = CollectionUtils.isEmpty(period.getPhoto()) ? "" : period.getPhoto().stream().findFirst().orElse(null);
        this.video = period.getUrl();
        this.startTime = period.getStartTime();
        this.endTime = period.getEndTime();
        this.liveUrl = period.getLiveUrl();
        this.replayUrl = period.getReplayUrl();
        this.price = period.getPrice();
        this.tip = period.getTip();
        if (period.getBtnContent() != null) {
            this.btnContent1 = period.getBtnContent().get(MicroCoursePeriod.BTN_BEFORE_PURCHASE);
            this.btnContent2 = period.getBtnContent().get(MicroCoursePeriod.BTN_AFTER_PURCHASE);
        }
        this.longClassUrl = period.getLongClassUrl();
        this.longClassPhoto = CollectionUtils.isEmpty(period.getLongClassPhoto()) ? "" : period.getLongClassPhoto().stream().findFirst().orElse(null);
        return this;
    }

    public CoursePeriodMapper withSeries(List<MicroCoursePeriod> series) {
        if (CollectionUtils.isEmpty(series)) {
            return this;
        }
        Date now = new Date();
        this.series = series.stream()
                .sorted(MicroCoursePeriod::sortByTime)
                .map(p -> new PeriodInfo(p.getId(), p.getTheme(), p.getStartTime(), p.getEndTime(), now))
                .collect(Collectors.toList());
        return this;
    }

    public CoursePeriodMapper withLecturer(MizarUser lecturer) {
        if (lecturer == null) {
            return this;
        }
        this.lecturerName = lecturer.getRealName();
        this.lecturerAvatar = lecturer.getPortrait();
        return this;
    }

    public String fetchClassTime() {
        if (startTime == null) {
            return null;
        }
        return DateUtils.dateToString(startTime, "MM月dd日 HH:mm");
    }

    // 正在直播, 开始前15分钟至结束
    public boolean live(Date current) {
        return !(current == null || startTime == null || endTime == null || startTime.after(endTime))
                && current.before(endTime) && current.after(DateUtils.addMinutes(startTime, -15));

    }

    public String targetId() {
        return payAll() ? courseId : periodId;
    }

    public boolean payAll() {
        return Boolean.TRUE.equals(payAll);
    }

    @JsonIgnore
    public boolean isReserve() {
        return price == 0;
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    public class PeriodInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private String id;     // 课时ID
        private String name;   // 课时名称
        private String desc;   // 其他描述

        PeriodInfo(String id, String name, Date startTime, Date endTime, Date currentTime) {
            String desc = null;
            if (endTime != null && currentTime != null && currentTime.after(endTime)) {
                desc = "查看回放";
            } else if (startTime != null) {
                desc = DateUtils.dateToString(startTime, "MM-dd");
            }
            this.id = id;
            this.name = name;
            this.desc = desc;
        }
    }

    public static void main(String[] args) {
        CoursePeriodMapper mapper = new CoursePeriodMapper();
        mapper.setStartTime(new Date());
        mapper.setEndTime(new Date());
        System.out.println(mapper.fetchClassTime());
    }

}
