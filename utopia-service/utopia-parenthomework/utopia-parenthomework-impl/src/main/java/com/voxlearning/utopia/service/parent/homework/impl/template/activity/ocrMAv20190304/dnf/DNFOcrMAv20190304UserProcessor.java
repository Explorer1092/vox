package com.voxlearning.utopia.service.parent.homework.impl.template.activity.ocrMAv20190304.dnf;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserActivityService;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.ActivityContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.IProcessor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;

/**
 * 查询未完成用户
 *
 * @author Wenlong Meng
 * @since  Feb 25, 2019
 */
@Named("activity.ocrMAv20190304.dnf.LoadDNFUserProcessor")
public class DNFOcrMAv20190304UserProcessor implements IProcessor<ActivityContext> {
    //local variables
    @Inject private HomeworkUserActivityService userActivityService;

    //Logic
    /**
     * 执行
     *
     * @param c context see {@link ActivityContext}
     */
    @Override
    public void process(ActivityContext c) {
        Date startTime;
        Date endTime;
        if(!ObjectUtils.anyBlank(c.get("date"))){
            Date date = DateUtils.stringToDate(c.get("date"), FORMAT_SQL_DATE);
            startTime = DayRange.newInstance(date.getTime()).getStartDate();
            endTime = DayRange.newInstance(date.getTime()).getEndDate();
        }else{
            startTime = DateUtils.addDays(DayRange.current().getStartDate(), -3);
            endTime = DateUtils.addDays(DayRange.current().getStartDate(), -2);
        }

        List<Long> parentIds = userActivityService.loadDNFUserIds(c.getActivityId(), startTime, endTime);
        LoggerUtils.debug("loadDNFUserIds", c.getActivityId(), startTime, endTime, parentIds.size());
        c.set("udfParentIds", parentIds);
    }
}
