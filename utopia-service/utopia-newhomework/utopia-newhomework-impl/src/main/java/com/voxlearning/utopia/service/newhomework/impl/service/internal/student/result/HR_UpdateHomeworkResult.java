package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.NewHomeworkResultUpdateFactory;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.NewHomeworkResultUpdateTemplate;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/16
 */
@Named
public class HR_UpdateHomeworkResult extends SpringContainerSupport implements HomeworkResultTask {

    @Inject private NewHomeworkResultUpdateFactory newHomeworkResultUpdateFactory;

    @Override
    public void execute(HomeworkResultContext context) {
        NewHomeworkResultUpdateTemplate template = newHomeworkResultUpdateFactory.getTemplate(context.getObjectiveConfigType().getNewHomeworkContentProcessTemp());
        template.processHomeworkContent(context);
    }
}
