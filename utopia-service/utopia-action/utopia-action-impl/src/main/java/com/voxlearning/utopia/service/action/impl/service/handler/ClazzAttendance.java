package com.voxlearning.utopia.service.action.impl.service.handler;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.action.api.document.UserAttendanceLog;
import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import com.voxlearning.utopia.service.action.impl.service.AbstractActionEventHandler;

import javax.inject.Named;
import java.util.Date;

/**
 * @author xinxin
 * @since 25/8/2016
 * <p>
 * 学生班级签到
 */
@Named("actionEventHandler.clazzAttendance")
public class ClazzAttendance extends AbstractActionEventHandler {

    public static final String TOTAL_COUNT = "totalCount";
    public static final String CLAZZ_ID = "clazzId";
    public static final String SCHOOL_ID = "schoolId";

    @Override
    public ActionEventType getEventType() {
        return ActionEventType.ClazzAttendance;
    }

    @Override
    public void handle(ActionEvent event) {
        if (!event.getAttributes().containsKey(TOTAL_COUNT) || !event.getAttributes().containsKey(SCHOOL_ID)
                || !event.getAttributes().containsKey(CLAZZ_ID)) {
            return;
        }
        Long schoolId = SafeConverter.toLong(event.getAttributes().get(SCHOOL_ID), 0);
        Long clazzId = SafeConverter.toLong(event.getAttributes().get(CLAZZ_ID), 0);
        Integer totalCount = SafeConverter.toInt(event.getAttributes().get(TOTAL_COUNT), 0);

        if (0 == schoolId || 0 == clazzId || 0 == totalCount) {
            return;
        }

        //一天只能签到一次
        long count = actionEventDayRangeCounter.increase(event);
        if (count == 0 || count > 1) {
            return;
        }

        //记录用户当天的签到记录
        UserAttendanceLog userAttendanceLog = new UserAttendanceLog();
        userAttendanceLog.setUserId(event.getUserId());
        userAttendanceLog.setSignDate(new Date());
        userAttendanceLog.setClazzId(clazzId);
        userAttendanceLog.setId(UserAttendanceLog.generateId(clazzId, event.getUserId()));

        userAttendanceLogDao.insert(userAttendanceLog);

        //记录用户当月签到次数
        userAttendanceCountDao.incrUserAttendanceCount(event.getUserId());

        //更新班级当天签到数量
        clazzAttendanceCountDao.incrAttendanceCount(schoolId, clazzId, totalCount);
    }
}
