package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.callback;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkPublishMessageType;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignHomework;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangbin
 * @since 2017/8/2
 */
@Named
public class PostAssignNewHomeworkPublishMessage extends NewHomeworkSpringBean implements PostAssignHomework {
    @Override
    public void afterHomeworkAssigned(Teacher teacher, AssignHomeworkContext context) {
        for (NewHomework newHomework : context.getAssignedGroupHomework().values()) {
            NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(newHomework.getId());
            String content = newHomeworkBook == null ? "" : "作业内容：" + StringUtils.join(newHomeworkBook.processUnitNameList(), "，");
            String unitName = newHomeworkBook == null ? "" : StringUtils.join(newHomeworkBook.processUnitNameList(), "，");
            // 儿童节作业内容特殊处理
            if (NewHomeworkType.Activity == newHomework.getType() && HomeworkTag.KidsDay == newHomework.getHomeworkTag()) {
                content = "作业内容：儿童节趣味配音";
                unitName = "儿童节趣味配音";
            }
            Map<String, Object> map = new HashMap<>();
            map.put("messageType", HomeworkPublishMessageType.assign);
            map.put("groupId", newHomework.getClazzGroupId());
            map.put("homeworkId", newHomework.getId());
            map.put("subject", newHomework.getSubject());
            map.put("teacherId", newHomework.getTeacherId());
            map.put("createAt", newHomework.getCreateAt().getTime());
            map.put("startTime", newHomework.getStartTime().getTime());
            map.put("endTime", newHomework.getEndTime().getTime());
            map.put("homeworkType", newHomework.getNewHomeworkType());
            map.put("objectiveConfigTypes", StringUtils.join(newHomework.findPracticeContents().keySet(),","));
            map.put("homeworkTag", newHomework.getHomeworkTag());
            map.put("content", content);
            map.put("unitName", unitName);
            map.put("duration", newHomework.getDuration());
            newHomeworkPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
        }
    }
}
