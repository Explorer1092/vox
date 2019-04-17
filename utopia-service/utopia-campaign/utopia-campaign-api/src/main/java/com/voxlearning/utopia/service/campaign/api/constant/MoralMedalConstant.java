package com.voxlearning.utopia.service.campaign.api.constant;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.DateRangeUnit;

import java.util.Date;

public class MoralMedalConstant {

    public static final String MORAL_MEDAL_ACTIVITY_NAME = "德育管理计划";

    public static final String STUDENT_ACTIVITY_INDEX = "/view/mobile/parent/deyu/student_detail.vpage";
    public static final String PARENT_ACTIVITY_INDEX = "/view/mobile/parent/deyu/study_dynamic.vpage?studentId=%s&moralMedalId=%s";


    //德育活动上线日
    //:todo 上线时间未定，须修改
    public static final Date ON_LINE_DATE =DateUtils.stringToDate("2019-02-24",DateUtils.FORMAT_SQL_DATE);

    public static final String UP_SEMESTER_START = "-02-14 00:00:00";

    public static final String UP_SEMESTER_END = "-08-09 23:59:59";

    public static final String DOWN_SEMESTER_START = "-08-10 00:00:00";

    public static final String DOWN_SEMESTER_END = "-02-13 23:59:59";
}
