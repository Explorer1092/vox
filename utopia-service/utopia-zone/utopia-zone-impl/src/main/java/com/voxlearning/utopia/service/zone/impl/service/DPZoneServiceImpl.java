package com.voxlearning.utopia.service.zone.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.ICharset;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.queue.zone.ZoneEventType;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.zone.api.DPZoneService;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.constant.JournalDuplicationPolicy;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Ruib
 * @since 2017/4/12
 */
@Named
@Service(interfaceClass = DPZoneService.class)
@ExposeService(interfaceClass = DPZoneService.class)
public class DPZoneServiceImpl implements DPZoneService {
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private ZoneQueueServiceImpl zoneQueueService;

    @Override
    public MapMessage createClazzJournal(Long studentId, ClazzJournalType type, ClazzJournalCategory category, String json) {
        if (null == studentId || type == null || category == null || StringUtils.isBlank(json)) {
            return MapMessage.errorMessage("参数错误");
        }

        StudentDetail student = studentLoaderClient.loadStudentDetail(studentId);
        if (student == null || student.getClazz() == null) {
            return MapMessage.errorMessage("学生信息错误");
        }

        Map<String, Object> clazzJournal = new LinkedHashMap<>();
        clazzJournal.put("clazzId", student.getClazz().getId());
        clazzJournal.put("relevantUserId", student.getId());
        clazzJournal.put("relevantUserType", student.fetchUserType());
        clazzJournal.put("journalType", type);
        clazzJournal.put("journalCategory", category);
        clazzJournal.put("journalJson", json);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("TS", System.currentTimeMillis());
        message.put("CJ", clazzJournal);
        message.put("P", JournalDuplicationPolicy.NONE);
        message.put("T", ZoneEventType.CLAZZ_JOURNAL);

        zoneQueueService.sendMessage(Message.newMessage().withStringBody(JsonUtils.toJson(message)));
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage createClazzJournalDaily(Long studentId, String journalType, String journalCategory, String json, String unique) {
        if (null == studentId || StringUtils.isAnyBlank(journalType, journalCategory, json)) {
            return MapMessage.errorMessage("参数错误");
        }

        StudentDetail student = studentLoaderClient.loadStudentDetail(studentId);
        if (student == null || student.getClazz() == null) {
            return MapMessage.errorMessage("学生信息错误");
        }

        ClazzJournalType type;
        try {
            type = ClazzJournalType.valueOf(journalType);
        } catch (Exception ex) {
            return MapMessage.errorMessage("错误的动态类型: " + journalType);
        }

        ClazzJournalCategory category;
        try {
            category = ClazzJournalCategory.valueOf(journalCategory);
        } catch (Exception ex) {
            return MapMessage.errorMessage("错误的动态分类: " + journalCategory);
        }

        Map<String, Object> clazzJournal = new LinkedHashMap<>();
        clazzJournal.put("clazzId", student.getClazzId());
        clazzJournal.put("relevantUserId", student.getId());
        clazzJournal.put("relevantUserType", student.fetchUserType());
        clazzJournal.put("journalType", type);
        clazzJournal.put("journalCategory", category);
        clazzJournal.put("journalJson", json);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("TS", System.currentTimeMillis());
        message.put("CJ", clazzJournal);
        message.put("P", JournalDuplicationPolicy.DAILY);
        message.put("T", ZoneEventType.CLAZZ_JOURNAL);
        message.put("K", unique);

        zoneQueueService.sendMessage(Message.newMessage().withStringBody(JsonUtils.toJson(message)));
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage createClazzJournal(Long studentId, String journalType, String journalCategory, String json, String journalDuplicationPolicy, String unique) {
        if (studentId == null || StringUtils.isAnyBlank(journalType, journalCategory, json)) {
            return MapMessage.errorMessage("参数错误");
        }

        StudentDetail student = studentLoaderClient.loadStudentDetail(studentId);
        if (student == null || student.getClazz() == null) {
            return MapMessage.errorMessage("学生信息错误");
        }

        ClazzJournalType type;
        try {
            type = ClazzJournalType.valueOf(journalType);
        } catch (Exception e) {
            return MapMessage.errorMessage("错误的动态类型: " + journalType);
        }

        ClazzJournalCategory category;
        try {
            category = ClazzJournalCategory.valueOf(journalCategory);
        } catch (Exception e) {
            return MapMessage.errorMessage("错误的动态分类: " + journalCategory);
        }

        // 默认不限制
        JournalDuplicationPolicy policy = JournalDuplicationPolicy.NONE;
        if (StringUtils.isNotBlank(journalDuplicationPolicy)) {
            try {
                policy = JournalDuplicationPolicy.valueOf(journalDuplicationPolicy);
            } catch (Exception e) {
                return MapMessage.errorMessage("错误的日志查重策略类型: " + journalDuplicationPolicy);
            }
        }

        Map<String, Object> clazzJournal = new LinkedHashMap<>();
        clazzJournal.put("clazzId", student.getClazzId());
        clazzJournal.put("relevantUserId", student.getId());
        clazzJournal.put("relevantUserType", student.fetchUserType());
        clazzJournal.put("journalType", type);
        clazzJournal.put("journalCategory", category);
        clazzJournal.put("journalJson", json);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("TS", System.currentTimeMillis());
        message.put("CJ", clazzJournal);
        message.put("T", ZoneEventType.CLAZZ_JOURNAL);
        message.put("P", policy);

        if (JournalDuplicationPolicy.DAILY.equals(policy)) {
            message.put("K", unique);
        }

        zoneQueueService.sendMessage(Message.newMessage().writeBinaryBody(JsonUtils.toJson(message).getBytes(ICharset.DEFAULT_CHARSET)));
        return MapMessage.successMessage();
    }
}
