package com.voxlearning.utopia.service.newhomework.impl.listener;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkQueueMessageType;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalNewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.impl.listener.handler.SaveJournalNewHomeworkProcessResultCommandHandler;
import com.voxlearning.utopia.service.newhomework.impl.listener.handler.UpdateTotalAssignmentRecordCommandHandler;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.SaveJournalNewHomeworkProcessResultCommand;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.UpdateTotalAssignmentRecordCommand;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO 稍后处理成抽象 xuesong.zhang
 *
 * @author guoqiang.li
 * @version 0.1
 * @since 2016/3/3
 * @deprecated will be removed in the future.
 */
@Named
@Deprecated
public class NewHomeworkQueueHandle extends NewHomeworkSpringBean {

    @Inject private SaveJournalNewHomeworkProcessResultCommandHandler saveJournalNewHomeworkProcessResultCommandHandler;
    @Inject private UpdateTotalAssignmentRecordCommandHandler updateTotalAssignmentRecordCommandHandler;

    @Deprecated
    public void processMessage(String messageText) throws Exception {
        Map<String, Object> messageMap = JsonUtils.fromJson(messageText);
        if (MapUtils.isEmpty(messageMap)) {
            logger.warn("Ignore unrecognized notify message: {}", messageText);
            return;
        }
        String type = SafeConverter.toString(messageMap.get("type"));
        if ("updateTotalAssignmentRecord".equals(type)) {
            updateTotalAssignmentRecordMessage(messageMap);
        } else if (StringUtils.equals(type, HomeworkQueueMessageType.SJHPR.name())) {
            saveJournalNewHomeworkProcessResults(messageMap);
        } else {
            logger.warn("unknown message type: type= {}, text= {}", type, messageText);
        }
    }

    @SuppressWarnings("unchecked")
    private void updateTotalAssignmentRecordMessage(Map<String, Object> messageMap) throws Exception {
        Subject subject = conversionService.convert(messageMap.get("subject"), Subject.class);
        Integer clazzGroupSize = SafeConverter.toInt(messageMap.get("clazzGroupSize"));
        Map<String, Integer> questionMap = conversionService.convert(messageMap.get("questionMap"), Map.class);
        Set<String> packageSet = conversionService.convert(messageMap.get("packageSet"), Set.class);
        Set<String> paperSet = conversionService.convert(messageMap.get("paperSet"), Set.class);

        // 逻辑已经迁移到UpdateTotalAssignmentRecordCommandHandler
        UpdateTotalAssignmentRecordCommand command = new UpdateTotalAssignmentRecordCommand();
        command.setSubject(subject);
        command.setClazzGroupSize(clazzGroupSize);
        command.setQuestionMap(questionMap);
        command.setPackageSet(packageSet);
        command.setPaperSet(paperSet);
        updateTotalAssignmentRecordCommandHandler.handle(command);
    }

    private void saveJournalNewHomeworkProcessResults(Map<String, Object> messageMap) throws Exception {
        String content = JsonUtils.toJson(messageMap.get("P"));
        List<JournalNewHomeworkProcessResult> results = JsonUtils.fromJsonToList(content, JournalNewHomeworkProcessResult.class);

        // 逻辑已经迁移到SaveJournalNewHomeworkProcessResultCommandHandler
        SaveJournalNewHomeworkProcessResultCommand command = new SaveJournalNewHomeworkProcessResultCommand();
        command.setResults(results);
        saveJournalNewHomeworkProcessResultCommandHandler.handle(command);
    }

    private void repairJournalNewHomeworkProcessResults(List<JournalNewHomeworkProcessResult> results) throws Exception {
        if (CollectionUtils.isEmpty(results)) {
            return;
        }
        for (JournalNewHomeworkProcessResult result : results) {
            result.setCreateAt(new Date());
        }
        SaveJournalNewHomeworkProcessResultCommand command = new SaveJournalNewHomeworkProcessResultCommand();
        command.setResults(results);
        saveJournalNewHomeworkProcessResultCommandHandler.handle(command);
    }
}
