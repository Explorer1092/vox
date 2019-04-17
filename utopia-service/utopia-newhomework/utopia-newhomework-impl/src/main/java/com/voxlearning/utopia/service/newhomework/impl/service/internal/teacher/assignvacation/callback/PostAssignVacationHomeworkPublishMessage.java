package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignvacation.callback;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkPublishMessageType;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignVacationHomework;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClazzGroup;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2017/12/8
 */
@Named
public class PostAssignVacationHomeworkPublishMessage extends NewHomeworkSpringBean implements PostAssignVacationHomework {

    @Override
    public void afterVacationHomeworkAssigned(Teacher teacher, AssignVacationHomeworkContext context) {
        Map<ClazzGroup, VacationHomeworkPackage> assignedHomeworks = context.getAssignedHomeworks();
        if (MapUtils.isEmpty(assignedHomeworks)) {
            return;
        }
        Long teacherId = teacher.getId();
        //这里取主学科的ID
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        teacherId = mainTeacherId == null ? teacherId : mainTeacherId;

        for (VacationHomeworkPackage vacationHomeworkPackage : assignedHomeworks.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("messageType", HomeworkPublishMessageType.assign);
            map.put("groupId", vacationHomeworkPackage.getClazzGroupId());
            map.put("homeworkId", vacationHomeworkPackage.getId());
            map.put("subject", vacationHomeworkPackage.getSubject());
            map.put("teacherId", teacherId);
            map.put("createAt", vacationHomeworkPackage.getCreateAt().getTime());
            map.put("startTime", vacationHomeworkPackage.getStartTime().getTime());
            map.put("endTime", vacationHomeworkPackage.getEndTime().getTime());
            map.put("homeworkType", NewHomeworkType.WinterVacation);
            newHomeworkPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
        }
    }
}
