package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.callback;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignHomework;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.queue.ClazzZoneQueueProducer;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xinxin
 * @since 16/8/2016
 */
@Named
public class PostAssignNewHomeworkClazzHeadline extends NewHomeworkSpringBean implements PostAssignHomework {

    @Inject private ClazzZoneQueueProducer clazzZoneQueueProducer;

    @Override
    public void afterHomeworkAssigned(Teacher teacher, AssignHomeworkContext context) {
        if (MapUtils.isEmpty(context.getAssignedGroupHomework())) return;

        clazzZoneQueueProducer.sendPahwClazzHeadline(teacher, context);

//        for (Map.Entry<ClazzGroup, NewHomework> entry : context.getAssignedHomeworks().entrySet()) {
//            zoneQueueServiceClient.createClazzJournal(entry.getKey().getClazzId())
//                    .withUser(teacher.getId())
//                    .withUser(UserType.TEACHER)
//                    .withClazzJournalType(ClazzJournalType.HOMEWORK_HEADLINE)
//                    .withClazzJournalCategory(ClazzJournalCategory.APPLICATION_STD)
//                    .withJournalJson(JsonUtils.toJson(MiscUtils.m(
//                            "subject", entry.getValue().getSubject().name(),
//                            "createAt", entry.getValue().getCreateAt(),
//                            "homeworkId", entry.getValue().getId()
//                    )))
//                    .commit();
//        }
    }
}
