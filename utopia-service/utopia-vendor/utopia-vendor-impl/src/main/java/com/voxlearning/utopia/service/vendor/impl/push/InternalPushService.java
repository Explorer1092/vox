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

import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.support.RangeableId;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.push.api.constant.PushTarget;
import com.voxlearning.utopia.service.push.api.constant.PushType;
import com.voxlearning.utopia.service.push.api.entity.AppJpushTimingMessage;
import com.voxlearning.utopia.service.vendor.impl.dao.AppJpushTimingMessageDao;
import com.voxlearning.utopia.service.vendor.impl.push.processor.ChannelPushProcess;
import com.voxlearning.utopia.service.vendor.impl.support.JpushTimingMessageSendTimeCalculator;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Instant;
import java.util.*;

/**
 * Jpush内部处理服务
 * Created by Shuai Huan on 2015/6/11.
 */
@Named
public class InternalPushService {
    @Inject
    AppPushChannelManager appPushChannelManager;
    @Inject
    private AppJpushTimingMessageDao appJpushTimingMessageDao;

    public void sendJpushNotify(Map<String, Object> map) {
        if (MapUtils.isEmpty(map)) {
            return;
        }

        Object sourceObject = map.get("source");
        AppMessageSource appMessageSource = AppMessageSource.of(SafeConverter.toString(sourceObject));
        if (appMessageSource == AppMessageSource.UNKNOWN) {
            return;
        }

        List<PushType> pushTypes = getPushTypes(appMessageSource);
        for (PushType pushType : pushTypes) {
            Map<String, Object> messageMap = new HashMap<>(map);
            ChannelPushProcess channelPushProcess = appPushChannelManager.get(pushType);
            Map<String, Object> oriParamMap = (Map<String, Object>) messageMap.get("params");
            Object targetObject = messageMap.get("target");
            PushTarget pushTarget = PushTarget.of(SafeConverter.toString(targetObject));
            if (pushTarget == PushTarget.UNKNOWN) {
                return;
            }

            oriParamMap.put("source", appMessageSource);
            Set<Map<String, Object>> mapSet = channelPushProcess.buildSendParams(oriParamMap, pushTarget);
            mapSet.forEach(paramMap -> {
                messageMap.put("params", paramMap);
                messageMap.put("pushType", pushType);
                channelPushProcess.product(messageMap);
            });

        }
    }

    private List<PushType> getPushTypes(AppMessageSource appMessageSource) {

        List<PushType> pushTypes = new ArrayList<>();
        pushTypes.add(PushType.JG);
        if (appMessageSource.appKey.equals("17Parent")) {
            pushTypes.add(PushType.MI);
        }

        return pushTypes;
    }

    public MapMessage sendJpushTimingNotify(Map<String, Object> map, Long sendTimestamp) {
        if (MapUtils.isEmpty(map)) {
            return MapMessage.errorMessage();
        }
        Object sourceObject = map.get("source");
        AppMessageSource source = AppMessageSource.of(SafeConverter.toString(sourceObject));
        if (source == AppMessageSource.UNKNOWN) {
            return MapMessage.errorMessage();
        }
        Long sendTime = JpushTimingMessageSendTimeCalculator.sendTimeCeil(sendTimestamp);
        Date expireAt = Date.from(Instant.ofEpochSecond(sendTime + 5 * 60));
        AppJpushTimingMessage message = new AppJpushTimingMessage();
        message.setId(RangeableId.newInstance(DateRangeType.M, sendTime * 1000).toString());
        message.setMessageSource(source.name());
        message.setNotify(JsonUtils.toJson(map));
        message.setSendTime(sendTime);
        message.setExpireAt(expireAt);
        appJpushTimingMessageDao.insert(message);
        return MapMessage.successMessage();
    }
}
