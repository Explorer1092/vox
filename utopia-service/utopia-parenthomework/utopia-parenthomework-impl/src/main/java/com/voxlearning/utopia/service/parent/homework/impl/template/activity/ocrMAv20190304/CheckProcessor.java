package com.voxlearning.utopia.service.parent.homework.impl.template.activity.ocrMAv20190304;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkActivityService;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserActivityService;
import com.voxlearning.utopia.service.parent.homework.api.entity.Activity;
import com.voxlearning.utopia.service.parent.homework.api.entity.Period;
import com.voxlearning.utopia.service.parent.homework.api.entity.UserActivity;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.ActivityContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.IProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATETIME;

/**
 * 检查流程
 *
 * @author Wenlong Meng
 * @since  Feb 25, 2019
 */
@Named("activity.ocrMAv20190304.CheckProcessor")
public class CheckProcessor implements IProcessor<ActivityContext> {

    //local variables
    @Inject private HomeworkActivityService activityService;
    @Inject private HomeworkUserActivityService userActivityService;

    /**
     * 执行
     *
     * @param c context see {@link ActivityContext}
     */
    @Override
    public void process(ActivityContext c) {
        //活动状态判断
        String activityId = c.getActivityId();
        Activity activity = activityService.load(activityId);
        c.setActivity(activity);
        LoggerUtils.debug("CheckProcessor.activity", activity);
        Date now = new Date();
        if(activity == null || activity.getStatus() != 0
                || activity.getStartTime().after(now)
                || activity.getEndTime().before(now)){
            c.setMapMessage(MapMessage.errorMessage("活动不存在或已结束"));
            c.setTerminate(true);
            return;
        }
        //判断该用户是否参加活动
        UserActivity userActivity = userActivityService.load(HomeworkUtil.generatorID(c.getStudentId(), activityId));
        c.setUserActivity(userActivity);
        LoggerUtils.debug("CheckProcessor.userActivity", userActivity);
        if(userActivity == null){
            c.setMapMessage(MapMessage.errorMessage("用户未参加活动"));
            c.setTerminate(true);
            return;
        }
        //判断用户是否已完成活动
        if(userActivity.getFinished() == Boolean.TRUE){
            c.setMapMessage(MapMessage.errorMessage("用户已完成活动"));
            c.setTerminate(true);
            return;
        }
        //判断用户是否已完成该周期活动
        Map<String, Object> extInfos = userActivity.getExtInfo();
        if(!ObjectUtils.anyBlank(extInfos)){
            Map<String, Object> extinfo = (Map<String, Object>) extInfos.get(extInfos.size() + "");
            Date time = DateUtils.stringToDate((String)extinfo.get("time"), FORMAT_SQL_DATETIME);
            Period period = activity.currentPeriod();
            if(period == Period.NULL || period.contain(time) || period.getIndex() <= extInfos.size()){
                c.setMapMessage(MapMessage.errorMessage("用户已完成该周期活动"));
                c.setTerminate(true);
                return;
            }
        }
    }
}
