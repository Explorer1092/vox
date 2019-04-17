package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.callback;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkPublishMessageType;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostFinishHomework;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.pubsub.NewHomeworkPublisher;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;
import com.voxlearning.utopia.service.question.consumer.DubbingLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tanguohong
 * @since 2017/7/13
 */
@Named
public class PostFinishNewHomeworkPublishMessage extends SpringContainerSupport implements PostFinishHomework {
    @Inject private NewHomeworkPublisher newHomeworkPublisher;
    @Inject private DubbingLoaderClient dubbingLoaderClient;

    @Override
    public void afterHomeworkFinished(FinishHomeworkContext context) {
        Map<String, Object> map = new HashMap<>();
        map.put("messageType", HomeworkPublishMessageType.finished);
        map.put("homeworkId", context.getHomeworkId());
        map.put("groupId", context.getClazzGroupId());
        map.put("studentId", context.getUserId());
        map.put("studentName", context.getUser() == null ? "" : context.getUser().fetchRealname());
        map.put("subject", context.getHomework().getSubject());
        map.put("score", context.getResult().processScore());
        map.put("homeworkType", context.getHomework().getNewHomeworkType());
        map.put("homeworkTag", context.getHomework().getHomeworkTag());
        map.put("createAt", context.getHomework().getCreateAt().getTime());
        // 期末基础复习题包id
        map.put("packageId", context.getHomework().getBasicReviewPackageId());
        // 趣味配音结果 - 家长端用
        map.put("dubbingResults", generateDubbingResults(context));
        map.put("repair", context.getResult().getRepair());
        map.put("isChecked", context.getHomework().isHomeworkChecked());
        newHomeworkPublisher.getStudentPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
    }

    private List<Map<String, Object>> generateDubbingResults(FinishHomeworkContext context) {
        List<Map<String, Object>> dubbingResults = new ArrayList<>();
        NewHomeworkResult newHomeworkResult = context.getResult();
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = newHomeworkResult.getPractices();
        if (MapUtils.isNotEmpty(practices) && practices.containsKey(ObjectiveConfigType.DUBBING)) {
            NewHomeworkResultAnswer resultAnswer = practices.get(ObjectiveConfigType.DUBBING);
            LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = resultAnswer.getAppAnswers();
            if (MapUtils.isNotEmpty(appAnswers)) {
                List<String> dubbingIds = appAnswers.values().stream()
                        .filter(appAnswer -> StringUtils.isNotBlank(appAnswer.getDubbingId()))
                        .map(NewHomeworkResultAppAnswer::getDubbingId)
                        .collect(Collectors.toList());
                Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(dubbingIds);
                for (NewHomeworkResultAppAnswer appAnswer : appAnswers.values()) {
                    String dubbingId = appAnswer.getDubbingId();
                    Dubbing dubbing = dubbingMap.get(dubbingId);
                    if (dubbing == null) {
                        continue;
                    }
                    dubbingResults.add(MapUtils.m(
                            "userId", context.getUserId(),
                            "dubbingId", dubbingId,
                            "categoryId", dubbing.getCategoryId(),
                            "clazzId", context.getClazzGroup().getClazzId(),
                            "videoUrl", appAnswer.getVideoUrl(),
                            "messageType", "Homework"
                    ));
                }
            }
        }
        return dubbingResults;
    }
}
