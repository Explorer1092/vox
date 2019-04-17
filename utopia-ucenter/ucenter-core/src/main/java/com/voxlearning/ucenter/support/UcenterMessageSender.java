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

package com.voxlearning.ucenter.support;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.client.ZoneQueueServiceClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 这个是用来给其他业务方发送消息用的，目前先都集中在这，通过类似queue sender的方式发送消息
 * 避免耦合，将来可以逐渐的把发送这边进一步与业务解耦
 *
 * @author changyuan.liu
 * @since 2015.12.17
 */
@Named
public class UcenterMessageSender {

    @Inject private ZoneQueueServiceClient zoneQueueServiceClient;

    public void sendZoneMessage(Long groupId, Long clazzId, User user, Integer type, Integer category, String content) {
        zoneQueueServiceClient.createClazzJournal(clazzId)
                .withGroup(groupId)
                .withUser(user.getId())
                .withUser(user.fetchUserType())
                .withClazzJournalType(ClazzJournalType.safeParse(type))
                .withClazzJournalCategory(ClazzJournalCategory.safeParse(category))
                .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content)))
                .commit();
    }
}
