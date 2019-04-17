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

package com.voxlearning.utopia.service.zone.impl.loader;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.zone.api.GiftLoader;
import com.voxlearning.utopia.service.zone.api.entity.GiftHistory;
import com.voxlearning.utopia.service.zone.impl.persistence.GiftHistoryPersistence;
import com.voxlearning.utopia.service.zone.impl.support.AbstractGiftLoader;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Default {@link GiftLoader} implementation.
 *
 * @author Xiaohai Zhang
 * @since Feb 26, 2015
 */
@Named
@ExposeService(interfaceClass = GiftLoader.class)
public class GiftLoaderImpl extends AbstractGiftLoader {

    @Inject private GiftHistoryPersistence giftHistoryPersistence;

    @Override
    public Map<Long, GiftHistory> loadGiftHistories(Collection<Long> ids) {
        return giftHistoryPersistence.loads(ids);
    }

    @Override
    public Collection<Long> getSenderGiftHistoryIds(Long senderId) {
        if (senderId == null) {
            return Collections.emptySet();
        }
        return giftHistoryPersistence.queryIdsBySenderId(senderId);
    }

    @Override
    public Collection<Long> getReceiverGiftHistoryIds(Long receiverId) {
        if (receiverId == null) {
            return Collections.emptySet();
        }
        return giftHistoryPersistence.queryIdsByReceiverId(receiverId);
    }
}
