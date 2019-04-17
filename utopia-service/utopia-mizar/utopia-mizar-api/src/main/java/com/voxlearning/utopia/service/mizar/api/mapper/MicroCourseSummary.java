package com.voxlearning.utopia.service.mizar.api.mapper;


import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.mizar.api.constants.microcourse.MicroCourseStatus;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCourse;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCoursePeriod;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Yuechen Wang on 2016/12/09.
 * 课程详情页 展示实体
 */
@Getter
@Setter
public class MicroCourseSummary implements Serializable, Comparable<MicroCourseSummary> {

    private static final long serialVersionUID = 1L;
    private String courseId;           // 课程ID
    private String courseName;         // 课程名称
    private String category;           // 课程分类
    private Boolean payAll;            // 按课程支付
    private String btnContent;         // 按钮文字
    private Double price;              // 课程价格
    private String tip;                // 备注提示
    private String qqTip;              // 课程加群提示文字
    private String qqUrl;              // 课程加群链接
    private List<String> periodRefs;   // 所有课时ID
    private String photo;              // 显示图片
    private MicroCoursePeriod latestPeriod;  // 最近一次课时
    private List<String> nextPeriods;        // 后续课时时间
    private MicroCourseStatus status;        // 课程状态
    private List<TeacherInfo> lecturers;     // 讲师列表
    private List<TeacherInfo> assistants;    // 助教列表

    public static MicroCourseSummary newInstance() {
        return new MicroCourseSummary();
    }

    // 加入课程信息
    public MicroCourseSummary withCourse(MicroCourse course) {
        if (course == null) {
            return this;
        }
        this.courseId = course.getId();
        this.courseName = course.getName();
        this.category = course.getCategory();
        this.payAll = Boolean.TRUE.equals(course.getPayAll());
        this.btnContent = course.getBtnContent();
        this.price = course.getPrice();
        this.tip = course.getTip();
        this.status = MicroCourseStatus.parse(course.getStatus());
        this.qqTip = course.getQqTip();
        this.qqUrl = course.getQqUrl();
        return this;
    }

    // 加入课时相关信息
    public MicroCourseSummary withPeriods(List<MicroCoursePeriod> periods, List<String> periodIds) {
        this.periodRefs = periodIds;
        if (CollectionUtils.isEmpty(periods) || CollectionUtils.isEmpty(periodIds)) {
            return this;
        }
        // 先按时间排好顺序
        periods = periods.stream()
                .filter(p -> p.getStartTime() != null && p.getEndTime() != null)
                .sorted(MicroCoursePeriod::sortByTime)
                .collect(Collectors.toList());
        // 默认设置图片为最后一张
        this.photo = periods.get(periods.size() - 1).getPhoto().stream().findFirst().orElse(null);
        Date now = new Date();
        // 根据结束时间过滤
        periods = periods.stream()
                .filter(p -> now.before(p.getEndTime())) // 过滤掉当前时间之后的
                .collect(Collectors.toList());
        // 课程全部已经过期
        if (periods.isEmpty()) {
            this.status = MicroCourseStatus.EXPIRE;
        } else {
            this.latestPeriod = periods.get(0);
            // 如果有最近课时，取最近的一个课时图片
            this.photo = this.latestPeriod.getPhoto().stream().findFirst().orElse(null);
            if (this.status == MicroCourseStatus.ONLINE && now.after(latestPeriod.getStartTime()))
                this.status = MicroCourseStatus.LIVE;
            // 设置后续课时
            this.nextPeriods = periods.stream()
                    .skip(1)
                    .map(p -> DateUtils.dateToString(p.getStartTime(), "MM-dd HH:mm"))
                    .collect(Collectors.toList());
        }
        return this;
    }

    // 加入主讲相关信息
    public MicroCourseSummary withLecturers(List<MizarUser> lecturers) {
        if (CollectionUtils.isEmpty(lecturers)) {
            return this;
        }
        this.lecturers = lecturers.stream()
                .map(u -> new TeacherInfo(u.getId(), u.getRealName()))
                .collect(Collectors.toList());
        return this;
    }

    // 加入助教相关信息
    public MicroCourseSummary withAssistants(List<MizarUser> assistants) {
        if (CollectionUtils.isEmpty(assistants)) {
            return this;
        }
        this.assistants = assistants.stream()
                .map(u -> new TeacherInfo(u.getId(), u.getRealName()))
                .collect(Collectors.toList());
        return this;
    }

    public int compareTo(MicroCourseSummary other) {
        if (other == null || other.status == null) return -1;
        // 优先根据状态排序
        int stCmp = Integer.compare(this.getStatus().getOrder(), other.getStatus().getOrder());
        if (stCmp != 0) return stCmp;
        // 之后根据最近课时的时间排序
        int cpCmp = MicroCoursePeriod.sortByTime(latestPeriod, other.latestPeriod);
        if (cpCmp != 0) return cpCmp;
        // 都没有最近课时的话，课时多的排前面
        return Objects.compare(this.getPeriodRefs(), other.getPeriodRefs(), (o1, o2) -> {
            if (CollectionUtils.isEmpty(o1) || CollectionUtils.isEmpty(o2)) {
                return 0;
            }
            return Integer.compare(o2.size(), o1.size());
        });
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    @AllArgsConstructor
    public class TeacherInfo implements Serializable {
        private static final long serialVersionUID = -5755981744727784337L;
        private String id;    // 教师ID
        private String name;   // 教师名称
    }

}
