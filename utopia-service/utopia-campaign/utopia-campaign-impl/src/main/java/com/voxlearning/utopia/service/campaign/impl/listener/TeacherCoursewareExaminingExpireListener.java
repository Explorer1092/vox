package com.voxlearning.utopia.service.campaign.impl.listener;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;
import com.voxlearning.utopia.service.campaign.impl.dao.TeacherCoursewareDao;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.campaign.teacher.courseware.examining.expire"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.campaign.teacher.courseware.examining.expire")
        },
        maxPermits = 4
)
@Slf4j
public class TeacherCoursewareExaminingExpireListener implements MessageListener {
    @Inject
    private TeacherCoursewareDao teacherCoursewareDao;

    @Override
    public void onMessage(Message message) {
        log.info("TeacherCoursewareExaminingExpireListener...");
        Object body = message.decodeBody();
        if (body == null || !(body instanceof String)) {
            return;
        }
        String json = (String) body;
        Map<String, Object> param = JsonUtils.fromJson(json);
        if (param == null || param.isEmpty()) {
            return;
        }

        String id = SafeConverter.toString(param.get("CID"));
        if (StringUtils.isBlank(id)) {
            return;
        }

        TeacherCourseware courseware = teacherCoursewareDao.load(id);
        if (courseware == null || courseware.getExamineStatus() != TeacherCourseware.ExamineStatus.EXAMINING
                ||(courseware.getDisabled() != null && courseware.getDisabled())) {
            return;
        }
        log.info("TeacherCoursewareExaminingExpireListener...");
        teacherCoursewareDao.updateExamineStatus(courseware.getId(), TeacherCourseware.ExamineStatus.EXAMINING, TeacherCourseware.ExamineStatus.WAITING, "", "");
    }


}
