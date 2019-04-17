package com.voxlearning.utopia.service.parent.homework.impl.template.activity.ocrMAv20190304.dnf;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkActivityService;
import com.voxlearning.utopia.service.parent.homework.api.entity.Activity;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.ActivityContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.IProcessor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

/**
 * 检查流程
 *
 * @author Wenlong Meng
 * @since  Feb 25, 2019
 */
@Named("activity.ocrMAv20190304.dnf.CheckProcessor")
public class CheckProcessor implements IProcessor<ActivityContext> {

    //local variables
    @Inject private HomeworkActivityService activityService;

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
    }
}
