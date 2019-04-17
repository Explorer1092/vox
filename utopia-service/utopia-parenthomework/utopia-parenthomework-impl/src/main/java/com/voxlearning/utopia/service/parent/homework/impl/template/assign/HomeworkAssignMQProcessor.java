package com.voxlearning.utopia.service.parent.homework.impl.template.assign;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.core.utils.MQUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.entity.Homework;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.Collections;
import java.util.Map;

import static com.voxlearning.utopia.service.parent.homework.impl.util.Constants.HOMEWORK_TOPIC;

/**
 * 布置作业发送消息
 *
 * @author chongfeng.qi
 * @since Nov, 20, 2018
 */
@Named
@Slf4j
public class HomeworkAssignMQProcessor implements HomeworkProcessor {

    @Override
    public void process(HomeworkContext hc) {
        if(hc.getMapMessage() != null){
            return;
        }
        HomeworkParam param = hc.getHomeworkParam();
        Homework homework = hc.getHomework();
        // 发消息
        Map<String, Object> message = MapUtils.m(
                "messageType", "assign",
                "actionId", ObjectUtils.get(() -> param.getData().get("actionId")),
                "bizType", homework.getBizType(),
                "studentId", param.getStudentId(),
                "subject", param.getSubject(),
                "bookId", param.getBookId(),
                "unitId", hc.getUnitId(),
                "startTime", DateUtils.dateToString(homework.getStartTime()),
                "endTime", DateUtils.dateToString(homework.getEndTime()),
                "homeworkIds", Collections.singleton(homework.getId()),
                "duration", homework.getDuration(),
                "userId", param.getStudentId()
        );
        MQUtils.send(HOMEWORK_TOPIC, message);
    }
}
