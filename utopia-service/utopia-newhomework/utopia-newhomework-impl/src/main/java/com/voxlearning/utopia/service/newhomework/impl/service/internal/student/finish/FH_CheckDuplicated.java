package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewAccomplishmentLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/2/16
 */
@Named
public class FH_CheckDuplicated extends SpringContainerSupport implements FinishHomeworkTask {
    @Inject private NewAccomplishmentLoaderImpl newAccomplishmentLoader;

    @Override
    public void execute(FinishHomeworkContext context) {
        NewHomework.Location location = context.getHomework().toLocation();
        NewAccomplishment acc = newAccomplishmentLoader.loadNewAccomplishment(location);
        if (!context.getSupplementaryData()) {
            if (acc != null && acc.size() > 0 && acc.getDetails().containsKey(String.valueOf(context.getUserId()))) {
                logger.debug("Student {} already finished homework {}, ignore", context.getUserId(), location);
                context.setTerminateTask(true);
            }
        }
    }
}
