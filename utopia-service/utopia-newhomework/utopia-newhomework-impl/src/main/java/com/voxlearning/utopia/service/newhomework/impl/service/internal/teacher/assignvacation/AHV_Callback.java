package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignvacation;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignVacationHomework;
import com.voxlearning.utopia.service.newhomework.api.context.AssignVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignvacation.callback.PostAssignVacationHomeworkPublishMessage;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignvacation.callback.PostAssignVacationHomeworkSendMobileNotification;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignvacation.callback.PostAssignVacationHomeworkParentMessage;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Created by tanguohong on 2016/12/6.
 */
@Named
public class AHV_Callback extends SpringContainerSupport implements AssignVacationHomeworkTask {

    @Inject private PostAssignVacationHomeworkParentMessage postAssignVacationHomeworkParentMessage;
    @Inject private PostAssignVacationHomeworkSendMobileNotification postAssignVacationHomeworkSendMobileNotification;
    @Inject private PostAssignVacationHomeworkPublishMessage postAssignVacationHomeworkPublishMessage;

    private final Collection<PostAssignVacationHomework> assignVacationHomeworkCallbacks = new LinkedHashSet<>();

    @Override
    public void execute(AssignVacationHomeworkContext context) {
        if (context.isSuccessful()) {

            assignVacationHomeworkCallbacks.addAll(Arrays.asList(
                    postAssignVacationHomeworkParentMessage,//家长app 布置作业发送站内信和JPUSH
                    postAssignVacationHomeworkSendMobileNotification,//学生app JPUSH消息
                    postAssignVacationHomeworkPublishMessage//发送布置假期作业广播
            ));
            for (PostAssignVacationHomework callback : assignVacationHomeworkCallbacks)
                callback.afterVacationHomeworkAssigned(context.getTeacher(), context);
        }

    }
}
