/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.vendor.impl.push;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.push.api.constant.PushTarget;
import lombok.Getter;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shiwe.liao
 * @since 2016/1/11
 */

@Named
public class AppMessagePushProcessor extends SpringContainerSupport {

    @Getter
    protected AppMessageSource appMessageSource;

    @Inject
    private PushProducer pushProducer;

    @Inject
    private InternalPushService internalPushService;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
    }

    public final void processUid(AppMessageSource source, String content, List<Long> userIds, Map<String, Object> extInfo) {
        processUid(source, content, userIds, extInfo, null);
    }

    public final void processUid(AppMessageSource source, String content, List<Long> userIds, Map<String, Object> extInfo, Long sendTimeEpochMilli) {
        if (source == null || source == AppMessageSource.UNKNOWN) {
            return;
        }
        String jpushContent = content;
        String umengContent = content;
        if (RuntimeMode.current().le(Mode.STAGING)) {
            jpushContent += "(测试包: 极光)";
            umengContent += "(测试包: 友盟)";
        }

        logParentPush(source, jpushContent, extInfo);

        processUidForJpush(source, jpushContent, userIds, extInfo, sendTimeEpochMilli);
    }

    private void logParentPush(AppMessageSource source, String content, Map<String, Object> extInfo) {
//        if (source != AppMessageSource.PARENT) {
//            return;
//        }

        Date time = DateUtils.stringToDate("2018-11-13 00:00:00");
        DayRange dayRange = DayRange.newInstance(time.getTime());

        if (dayRange.contains(new Date())) {
            LogCollector.info("parent_push_temp_logs",
                    MiscUtils.map("source", source.name(), "content", content, "extInfo", JsonUtils.toJson(extInfo)));
        }
    }


    public final void processTag(AppMessageSource source, String content, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Integer durationTime) {
        processTag(source, content, tags, tagsAnd, extInfo, durationTime, null);
    }

    public final void processTag(AppMessageSource source, String content, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Integer durationTime, Long sendTimeEpochMilli) {
        if (source == null || source == AppMessageSource.UNKNOWN) {
            return;
        }
        String jpushContent = content;
        String umengContent = content;
        if (RuntimeMode.current().le(Mode.STAGING)) {
            jpushContent += "(测试包: 极光)";
            umengContent += "(测试包: 友盟)";
        }

        logParentPush(source, jpushContent, extInfo);

        processTagForJpush(source, jpushContent, tags, tagsAnd, extInfo, durationTime, sendTimeEpochMilli);
    }

    private void processUidForJpush(AppMessageSource source, String content, List<Long> userIds, Map<String, Object> extInfo, Long sendTimeEpochMilli) {
        if (CollectionUtils.isNotEmpty(userIds)) {
            //每次发1000个用户id，防止被JPush平台搞掉
            int time = (int) Math.ceil(userIds.size() / 1000d);
            List<List<Long>> userIdListCollection = CollectionUtils.splitList(userIds, time);
            for (List<Long> userIdList : userIdListCollection) {

                Date date = new Date();
                ObjectId objectId = new ObjectId();
                String id = SafeConverter.toString(date.getTime()) + "-" + objectId.toString();
                Map<String, Object> paramMap = new HashMap<>();
                extInfo.put("notifyId", id);
                paramMap.put("content", content);
                paramMap.put("extInfo", extInfo);
                paramMap.put("userIdList", userIdList);
                Map<String, Object> messageMap = new HashMap<>();
                messageMap.put("params", paramMap);
                messageMap.put("notifyId", id);
                messageMap.put("target", PushTarget.BATCH);
                messageMap.put("source", source);
                if (extInfo.containsKey("taskId")) {
                    messageMap.put("taskId", extInfo.get("taskId"));
                    extInfo.remove("taskId");
                }
                if (null == sendTimeEpochMilli || sendTimeEpochMilli <= 0) {
                    internalPushService.sendJpushNotify(messageMap);
                } else {
                    internalPushService.sendJpushTimingNotify(messageMap, sendTimeEpochMilli);
                }
            }
        }
    }

    private void processTagForJpush(AppMessageSource source, String content, List<String> tags, List<String> tagsAnd, Map<String, Object> extInfo, Integer durationTime, Long sendTimeEpochMilli) {
        if (CollectionUtils.isNotEmpty(tags) || CollectionUtils.isNotEmpty(tagsAnd)) {
            Date date = new Date();
            ObjectId objectId = new ObjectId();
            String id = SafeConverter.toString(date.getTime()) + "-" + objectId.toString();
            extInfo.put("notifyId", id);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("tags", tags);
            paramMap.put("content", content);
            paramMap.put("tagsAnd", tagsAnd);
            paramMap.put("extInfo", extInfo);
            paramMap.put("durationTime", durationTime);
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("params", paramMap);
            messageMap.put("notifyId", id);
            messageMap.put("target", PushTarget.TAG);
            messageMap.put("source", source);
            if (extInfo.containsKey("taskId")) {
                messageMap.put("taskId", extInfo.get("taskId"));
                extInfo.remove("taskId");
            }
            if (null == sendTimeEpochMilli || 0 >= sendTimeEpochMilli) {
                internalPushService.sendJpushNotify(messageMap);
            } else {
                internalPushService.sendJpushTimingNotify(messageMap, sendTimeEpochMilli);
            }
        }
    }
}
