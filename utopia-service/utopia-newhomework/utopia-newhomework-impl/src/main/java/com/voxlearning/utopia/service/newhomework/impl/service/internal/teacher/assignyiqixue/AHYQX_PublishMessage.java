package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignyiqixue;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkPublishMessageType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.pubsub.LiveCastHomeworkPublisher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author tanguohong
 * @since 2017/7/13
 */
@Named
public class AHYQX_PublishMessage extends SpringContainerSupport implements AssignYiQiXueHomeworkTask {
    @Inject private LiveCastHomeworkPublisher liveCastHomeworkPublisher;

    @Override
    public void execute(AssignHomeworkContext context) {
        Map<String, Object> map = new HashMap<>();
        List<String> groupHomeworkId = new ArrayList<>();
        for (NewHomework newHomework : context.getAssignedGroupHomework().values()) {
            groupHomeworkId.add(StringUtils.join(Arrays.asList(newHomework.getClazzGroupId(), newHomework.getId()), ":"));
        }
        map.put("messageType", HomeworkPublishMessageType.assign);
        map.put("groupHomeworkId", groupHomeworkId);
        //发送广播，给21解析作业里面的内容
        map.put("homeworkMap", context.getAssignedGroupHomework());
        map.put("extData", context.getSource().get("extData"));
        liveCastHomeworkPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
    }
}
