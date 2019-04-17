package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.callback;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostFinishHomework;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
public class PostFinishKidsDayHomeworkParentMessage extends SpringContainerSupport implements PostFinishHomework {
    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private StudentLoaderClient studentLoaderClient;

    public void afterHomeworkFinished(FinishHomeworkContext context) {

        if (NewHomeworkType.Activity == context.getNewHomeworkType() && HomeworkTag.KidsDay == context.getHomework().getHomeworkTag()) {
            List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(context.getUserId());

            if (!CollectionUtils.isEmpty(studentParentRefs)) {
                List<Long> parentIds = studentParentRefs.stream().map(StudentParentRef::getParentId).collect(Collectors.toList());

                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(context.getUserId());
                String content = "您的孩子" + studentDetail.fetchRealname() + "完成了儿童节趣味练习，快去奖励孩子并发送鼓励吧~";
                Map<String, Object> jpushExtInfo = new HashMap<>();
                jpushExtInfo.put("s", ParentAppPushType.HOMEWORK_CHECK.name());
                jpushExtInfo.put("url", "/view/mobile/parent/childrendayhomework/index.vpage?hid=" + context.getHomeworkId());
                appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.PARENT, parentIds, jpushExtInfo);
            }
        }
    }
}
