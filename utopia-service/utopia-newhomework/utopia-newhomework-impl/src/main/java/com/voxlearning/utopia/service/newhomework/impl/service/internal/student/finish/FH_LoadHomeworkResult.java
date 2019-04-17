package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish;

import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/15
 */
@Named
public class FH_LoadHomeworkResult extends SpringContainerSupport implements FinishHomeworkTask {
    @Inject private NewHomeworkResultLoaderImpl newHomeworkResultLoader;

    @Override
    public void execute(FinishHomeworkContext context) {
        NewHomework.Location location = context.getHomework().toLocation();
        String day = DayRange.newInstance(location.getCreateTime()).toString();
        NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, location.getSubject(),
                location.getId(), context.getUserId().toString());
        NewHomeworkResult result = newHomeworkResultLoader.loads(Collections.singletonList(id.toString()), true).get(id.toString());

        if (result == null) {
            logger.error("Cannot locate student {}'s NewHomeworkResult {}", context.getUserId(), id.toString());
            context.errorResponse();
            return;
        }

        context.setResult(result);
    }
}
