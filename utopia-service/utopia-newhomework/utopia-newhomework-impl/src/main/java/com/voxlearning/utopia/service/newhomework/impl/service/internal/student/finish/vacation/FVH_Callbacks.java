package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.vacation;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostFinishVacationHomework;
import com.voxlearning.utopia.service.newhomework.api.context.FinishVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.callback.vacation.PostFinishVacationHomeworkPublishMessage;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.callback.vacation.PostFinishVacationHomeworkSaveStudents;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.callback.vacation.PostFinishVacationHomeworkSendParentMessage;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;

/**
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class FVH_Callbacks extends SpringContainerSupport implements FinishVacationHomeworkTask {
    @Inject
    private PostFinishVacationHomeworkPublishMessage postFinishVacationHomeworkPublishMessage;
    @Inject
    private PostFinishVacationHomeworkSendParentMessage postFinishVacationHomeworkSendParentMessage;
    @Inject
    private PostFinishVacationHomeworkSaveStudents postFinishVacationHomeworkSaveStudents;

    private final List<PostFinishVacationHomework> callbacks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        callbacks.add(postFinishVacationHomeworkPublishMessage);
        callbacks.add(postFinishVacationHomeworkSendParentMessage);
        callbacks.add(postFinishVacationHomeworkSaveStudents);
    }

    @Override
    public void execute(FinishVacationHomeworkContext context) {
        for (PostFinishVacationHomework callback : callbacks) {
            try {
                callback.afterVacationHomeworkFinished(context);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
}
