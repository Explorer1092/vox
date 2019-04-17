/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.wechat.impl.service.wechat;

import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.wechat.api.entities.WechatNoticeHistory;
import com.voxlearning.utopia.service.wechat.impl.dao.WechatNoticeHistoryPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author HuanYin Jia
 * @since 2015/5/22
 */
@Named
public class WechatNoticeHistoryService {

    @Inject
    private WechatNoticeHistoryPersistence wechatNoticeHistoryPersistence;

    public List<WechatNoticeHistory> loadAllByUserId(Long userId) {
        return userId == null ? null : wechatNoticeHistoryPersistence.listAllByUserId(userId);
    }

    public Collection<Long> batchAdd(List<WechatNoticeHistory> wechatNoticeHistories) {
        if (wechatNoticeHistories == null || wechatNoticeHistories.isEmpty()) {
            return null;
        }
        wechatNoticeHistoryPersistence.inserts(wechatNoticeHistories);
        return wechatNoticeHistories.stream()
                .map(AbstractDatabaseEntity::getId)
                .collect(Collectors.toList());
    }

    public int removeByCreateDatetime(Date createDatetime, Integer limit) {
        if (createDatetime == null || limit == null || limit < 1) {
            return 0;
        }
        return wechatNoticeHistoryPersistence.deleteByCreateDatetime(createDatetime, limit);
    }

}
