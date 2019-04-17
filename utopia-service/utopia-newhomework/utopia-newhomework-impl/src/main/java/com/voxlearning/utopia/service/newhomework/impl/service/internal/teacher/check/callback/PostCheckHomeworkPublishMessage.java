package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check.callback;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkPublishMessageType;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostCheckHomework;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.pubsub.NewHomeworkPublisher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tanguohong
 * @since 2017/8/3
 */
@Named
public class PostCheckHomeworkPublishMessage implements PostCheckHomework {
    @Inject private NewHomeworkPublisher newHomeworkPublisher;

    @Override
    public void afterHomeworkChecked(CheckHomeworkContext context) {
        NewHomework newHomework = context.getHomework();
        Map<String, Object> map = new HashMap<>();
        map.put("messageType", HomeworkPublishMessageType.checked);
        map.put("groupId", newHomework.getClazzGroupId());
        map.put("homeworkId", newHomework.getId());
        map.put("subject", newHomework.getSubject());
        map.put("teacherId", newHomework.getTeacherId());
        map.put("createAt", newHomework.getCreateAt().getTime());
        map.put("checkedAt", newHomework.getCheckedAt().getTime());
        map.put("homeworkType", newHomework.getNewHomeworkType());
        map.put("objectiveConfigTypes", StringUtils.join(newHomework.findPracticeContents().keySet(),","));
        map.put("homeworkTag", newHomework.getHomeworkTag());
        map.put("finishCount", context.getAccomplishment() == null ? 0 : context.getAccomplishment().getDetails().size());
        newHomeworkPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
    }
}
