package com.voxlearning.utopia.business.api.constant;

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import lombok.Getter;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

/**
 * 老师新学期活动, 以后再说入库的事儿
 */
public enum TeacherNewTermActivityCategory {
    JuniorEnglishStudent(
            110001L,
            Ktwelve.JUNIOR_SCHOOL,
            Subject.ENGLISH,
            "中学英语老师带新学生",
            "中学英语老师带新学生",
            DateUtils.stringToDate("2018-03-01 00:00:00"),
            DateUtils.stringToDate("2018-04-30 23:59:59"),
            DateUtils.stringToDate("2018-04-30 23:59:59")
    ),
    PrimaryEnglishStudent(
            120001L,
            Ktwelve.PRIMARY_SCHOOL,
            Subject.ENGLISH,
            "小学英语老师带新学生",
            "小学英语老师带新学生",
            DateUtils.stringToDate("2018-03-12 00:00:00"),
            DateUtils.stringToDate("2018-04-10 23:59:59"),
            DateUtils.stringToDate("2018-05-09 23:59:59")
    ),
    PrimaryChineseStudent(
            130001L,
            Ktwelve.PRIMARY_SCHOOL,
            Subject.CHINESE,
            "小学语文老师带新学生",
            "小学语文老师带新学生",
            DateUtils.stringToDate("2018-03-01 00:00:00"),
            DateUtils.stringToDate("2018-04-30 23:59:59"),
            DateUtils.stringToDate("2018-05-30 23:59:59")
    ),
    TeacherCoursewareContest(
            140001L,
            Ktwelve.PRIMARY_SCHOOL,
            Subject.CHINESE,
            "小学老师课件大赛",
            "小学老师课件大赛",
            DateUtils.stringToDate("2018-03-01 00:00:00"),
            DateUtils.stringToDate("2018-06-10 23:59:59"),
            DateUtils.stringToDate("2018-06-09 23:59:59")
    ),
    JuniorEnglishStudent_TMP(
            150001L,
            Ktwelve.JUNIOR_SCHOOL,
            Subject.ENGLISH,
            "中学英语老师带新学生",
            "中学英语老师带新学生",
            DateUtils.stringToDate("2018-04-19 00:00:00"),
            DateUtils.stringToDate("2018-05-31 23:59:59"),
            DateUtils.stringToDate("2018-06-01 23:59:59")
    ),
    InvalidActivity(0L, null, null, "无效的活动", "无效的活动", null, null, null) // 无效的活动
    ;
    @Getter private final Long id;
    @Getter private final Ktwelve ktwelve;
    @Getter private final Subject subject;
    @Getter private final String name;
    @Getter private final String desc;
    @Getter private final Date startDate;
    @Getter private final Date endDate;
    @Getter private final Date deadline;

    TeacherNewTermActivityCategory(Long id,
                                   Ktwelve ktwelve,
                                   Subject subject,
                                   String name,
                                   String desc,
                                   Date startDate,
                                   Date endDate,
                                   Date deadline) {
        this.id = id;
        this.ktwelve = ktwelve;
        this.subject = subject;
        this.name = name;
        this.desc = desc;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deadline = deadline;
    }

    public static TeacherNewTermActivityCategory parse(Long activityId) {
        return Stream.of(values()).filter(v -> v.getId().equals(activityId))
                .findFirst().orElse(InvalidActivity);
    }

    public boolean match(Ktwelve ktwelve, List<Subject> subjects) {
        return ktwelve != null
                && CollectionUtils.isNotEmpty(subjects)
                && this.ktwelve == ktwelve && subjects.contains(this.subject);
    }

    public boolean expire(Date date) {
        return date == null || endDate != null && this.endDate.before(date);
    }
}
