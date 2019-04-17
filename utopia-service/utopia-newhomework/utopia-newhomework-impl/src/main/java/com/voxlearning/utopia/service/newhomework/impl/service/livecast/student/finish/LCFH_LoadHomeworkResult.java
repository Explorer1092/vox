package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.finish;

import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.FinishLiveCastHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkResultDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xuesong.zhang
 * @since 2017/1/3
 */
@Named
public class LCFH_LoadHomeworkResult extends SpringContainerSupport implements FinishLiveCastHomeworkTask {

    @Inject private LiveCastHomeworkResultDao liveCastHomeworkResultDao;

    @Override
    public void execute(FinishLiveCastHomeworkContext context) {
        LiveCastHomework.Location location = context.getLiveCastHomework().toLocation();
        String month = MonthRange.newInstance(location.getCreateTime()).toString();
        LiveCastHomeworkResult.ID id = new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), context.getUserId());

        LiveCastHomeworkResult result = liveCastHomeworkResultDao.load(id.toString());
        if (result == null) {
            logger.error("Cannot locate student {}'s LiveCastHomeworkResult {}", context.getUserId(), id.toString());
            context.errorResponse();
            return;
        }
        context.setLiveCastHomeworkResult(result);
    }
}
