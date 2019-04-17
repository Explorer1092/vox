package com.voxlearning.utopia.api.constant;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;

import java.util.Date;

public class WarmHeartPlanConstant {

    public static final int MAX_TARGET_NUM = 3;

    //当用户满足5个学生累计打卡21天时，首次进入活动也，弹出证书
    public static final int MAX_STU_POP_CERTIFICATE = 5;

    public static final String WARM_HEART_PLAN_ACTIVITY_NAME = "暖心亲子计划";
    public static final String TEACHER_APP_ICON_NAME = "亲子计划";
    public static final String TEACHER_APP_ICON_IMAGE = "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/qinzi_img.png";

    public static final String WARM_HEART_STU_PARTICIPATE_COUNT = "WHSPC";
    public static final String WARM_HEART_PARTICIPATE_COUNT = "WHPC";

    public static final String WARM_HEART_STUDENT_TARGET_CLOCK = "WarmHeartPlan:Target:Clock";
    public static final String WARM_HEART_TARGET_CHOOSE = "WarmHeartPlan:Target:Choose";

    public static final String WARM_HEART_STUDENT_ID_TARGET = "WarmHeartPlan:StudentId:Target:";
    public static final String WARM_HEART_PRAISE_STUDENT = "WarmHeartPlan:Praise:Student";

    public static final String WARM_HEART_TEACHER_21DAY = "WarmHeartPlan:Teacher_21";
    // 学生有几个计划满足21天
    public static final String WARM_HEART_STUDENT_21DAY = "WarmHeartPlan:Student_21";
    public static final String WARM_HEART_TEACHER_STUDENT_21DAY = "WarmHeartPlan:T_S_21";
    public static final String WARM_HEART_STUDENT_FIRST = "WarmHeartPlan:S_FIRST";

    public static final String TEACHER_INDEX_PAGE = "/view/mobile/teacher/activity2019/parentchild_plan/index.vpage";
    public static final String STUDENT_INDEX_PAGE = "/view/mobile/student/parentchild_plan/index.vpage";
    public static final String PARENT_INDEX_PAGE = "/view/mobile/parent/parentchild_plan/index.vpage";

    public static final String RESOURCE_PATH = "https://www.17zuoye.com/view/mobile/teacher/teaching_assistant/resourcedetail?resourceId=";


    public static final String WARM_HEART_MSG_INDEX_PARENT_1 = "WarmHeartPlan:MSG:1";
    public static final String WARM_HEART_MSG_INDEX_PARENT_2 = "WarmHeartPlan:MSG:2";
    public static final String WARM_HEART_MSG_INDEX_PARENT_3 = "WarmHeartPlan:MSG:3";
    public static final String WARM_HEART_MSG_INDEX_PARENT_4 = "WarmHeartPlan:MSG:4";


    public static final IntegralType TEACHER_INTEGRAL_TYPE = IntegralType.TEACHER_WARM_HEART;
    public static final IntegralType STUDENT_INTEGRAL_TYPE = IntegralType.STUDENT_WARM_HEART;
    public static final SmsType SMS_TYPE = SmsType.ACTIVITY_WARM_HEART_PLAN_NOTIFY;

    public static Date WARM_HEART_PLAN_SIGN_START;
    public static Date WARM_HEART_PLAN_SIGN_END;
    public static Date WARM_HEART_PLAN_END_TIME;
    public static DateRange SIGN_DATE_RANGE;

    public static final DayRange DAY_0422 = DayRange.parse("20190422");
    public static final DayRange DAY_0501 = DayRange.parse("20190501");
    public static final DayRange DAY_0506 = DayRange.parse("20190506");
    public static final DayRange DAY_0512 = DayRange.parse("20190512");
    public static final DayRange DAY_0601 = DayRange.parse("20190601");
    public static final DayRange DAY_0607 = DayRange.parse("20190607");
    public static final DayRange DAY_0616 = DayRange.parse("20190616");
    public static final DayRange DAY_0623 = DayRange.parse("20190623");

    static {
        try {
            WARM_HEART_PLAN_SIGN_START = DateUtils.parseDate("2019-04-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
            WARM_HEART_PLAN_SIGN_END = DateUtils.parseDate("2019-05-30 23:59:59", "yyyy-MM-dd HH:mm:ss");
            SIGN_DATE_RANGE = new DateRange(WARM_HEART_PLAN_SIGN_START, WARM_HEART_PLAN_SIGN_END);


            WARM_HEART_PLAN_END_TIME = DateUtils.parseDate("2019-07-15 00:00:00", "yyyy-MM-dd HH:mm:ss");
        } catch (Exception ignore) {
        }
    }
}
