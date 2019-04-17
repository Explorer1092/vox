package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.callback.vacation;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkPublishMessageType;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostFinishVacationHomework;
import com.voxlearning.utopia.service.newhomework.api.context.FinishVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Named
public class PostFinishVacationHomeworkPublishMessage extends NewHomeworkSpringBean implements PostFinishVacationHomework {
    @Override
    public void afterVacationHomeworkFinished(FinishVacationHomeworkContext context) {
        VacationHomeworkPackage vacationHomeworkPackage = vacationHomeworkPackageDao.load(context.getVacationHomework().getPackageId());
        Map<String, Object> map = new HashMap<>();
        map.put("messageType", HomeworkPublishMessageType.finished);
        map.put("homeworkId", context.getVacationHomework().getId());
        map.put("groupId", context.getClazzGroupId());
        map.put("studentId", context.getUserId());
        map.put("subject", context.getVacationHomework().getSubject());
        map.put("score", context.getResult().processScore());
        map.put("homeworkType", context.getVacationHomework().getNewHomeworkType());
        map.put("dayRank", context.getVacationHomework().getDayRank());
        map.put("plannedDays", vacationHomeworkPackage != null ? SafeConverter.toInt(vacationHomeworkPackage.getPlannedDays(), 30) : 30);
        newHomeworkPublisher.getStudentVacationPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
    }
}
