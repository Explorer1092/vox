package com.voxlearning.utopia.service.dubbing.impl.listener;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.dubbing.api.constant.DubbingPublishMessageType;
import com.voxlearning.utopia.service.dubbing.api.entity.DubbingHistory;
import com.voxlearning.utopia.service.dubbing.impl.service.DubbingHistoryServiceImpl;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkPublishMessageType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * @Author: wei.jiang
 * @Date: Created on 2017/10/31
 */
@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.homework.student.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.homework.student.topic")
        }
)
public class HomeworkDubbingQueueListener implements MessageListener {

    @Inject
    private DubbingHistoryServiceImpl dubbingHistoryService;

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(Message message) {
        Object body = message.decodeBody();
        if (body == null) {
            return;
        }
        Map<String, Object> messageMap = JsonUtils.fromJson((String) body);
        if (MapUtils.isEmpty(messageMap)) {
            return;
        }
        HomeworkPublishMessageType messageType = HomeworkPublishMessageType.of(SafeConverter.toString(messageMap.get("messageType")));
        if (messageType == HomeworkPublishMessageType.UNKNOWN || messageType != HomeworkPublishMessageType.finished) {
            return;
        }
        List<Map<String, Object>> dubbingResults = (List<Map<String, Object>>) messageMap.get("dubbingResults");
        if (CollectionUtils.isEmpty(dubbingResults)) {
            return;
        }
        Long studentId = SafeConverter.toLong(messageMap.get("studentId"));
        if (studentId == 0L) {
            return;
        }
        String homeworkId = SafeConverter.toString(messageMap.get("homeworkId"));
        if (StringUtils.isBlank(homeworkId)) {
            return;
        }
        saveHomeworkDubbing(dubbingResults, studentId, homeworkId);
    }

    private void saveHomeworkDubbing(List<Map<String, Object>> dubbingResults, Long studentId, String homeworkId) {
        dubbingResults.forEach(e -> {
            DubbingHistory dubbingHistory = new DubbingHistory();
            String dubbingId = SafeConverter.toString(e.get("dubbingId"));
            if (StringUtils.isBlank(dubbingId)) {
                return;
            }
            String categoryId = SafeConverter.toString(e.get("categoryId"));
            if (StringUtils.isBlank(categoryId)) {
                return;
            }
            Long clazzId = SafeConverter.toLong(e.get("clazzId"));
            if (clazzId == 0L) {
                return;
            }
            String videoUrl = SafeConverter.toString(e.get("videoUrl"));
            if (StringUtils.isBlank(videoUrl)) {
                return;
            }
            DubbingPublishMessageType messageType = DubbingPublishMessageType.of(SafeConverter.toString(e.get("messageType")));
            if (messageType != DubbingPublishMessageType.Homework) {
                return;
            }
            String historyId = DubbingHistory.generateId(studentId, dubbingId, clazzId, categoryId);
            dubbingHistory.setId(historyId);
            dubbingHistory.setUserId(studentId);
            dubbingHistory.setClazzId(clazzId);
            dubbingHistory.setDubbingId(dubbingId);
            dubbingHistory.setCategoryId(categoryId);
            dubbingHistory.setVideoUrl(videoUrl);
            dubbingHistory.setIsPublished(Boolean.TRUE);
            dubbingHistory.setIsHomework(Boolean.TRUE);
            dubbingHistory.setHomeworkId(homeworkId);
            dubbingHistoryService.saveDubbingHistory(dubbingHistory);

        });

    }

}
