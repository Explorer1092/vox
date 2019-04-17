package com.voxlearning.utopia.service.parent.homework.impl.template.activity.ocrMAv20190304;

import com.google.common.collect.Maps;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.service.parent.homework.api.entity.Activity;
import com.voxlearning.utopia.service.parent.homework.api.entity.UserActivity;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkUserActivityDao;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.ActivityContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.IProcessor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATETIME;

/**
 * 更新活动状态
 *
 * @author Wenlong Meng
 * @since  Feb 25, 2019
 */
@Named("activity.ocrMAv20190304.UpdateStatusProcessor")
public class UpdateStatusProcessor  implements IProcessor<ActivityContext> {
    //local variables
    @Inject private HomeworkUserActivityDao userActivityDao;

    //Logic
    /**
     * 执行
     *
     * @param c context see {@link ActivityContext}
     */
    @Override
    public void process(ActivityContext c) {
        UserActivity userActivity = c.getUserActivity();
        Activity activity = c.getActivity();
        Map<String, Object> extInfo = userActivity.getExtInfo();
        if(extInfo == null){
            extInfo = Maps.newHashMap();
        }
        int i = extInfo.size() + 1;
        extInfo.put("" + i, MapUtils.m("cid", c.get("cid"),
                "time", DateUtils.dateToString(new Date(), FORMAT_SQL_DATETIME),
                "parentId", c.getParentId(),
                "url", c.get("url"),
                "xCount", c.get("xCount"),
                "vCount", c.get("vCount")));
        userActivity.setFinished(activity.periodCount() == extInfo.size());
        userActivity.setExtInfo(extInfo);
        userActivity.setUpdateTime(new Date());
        LoggerUtils.debug("UpdateStatusProcessor.userActivity", userActivity);
        userActivityDao.upsert(userActivity);
    }
}
