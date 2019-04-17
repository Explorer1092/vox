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

package com.voxlearning.utopia.service.zone.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.zone.api.GiftService;
import com.voxlearning.utopia.service.zone.impl.persistence.GiftHistoryPersistence;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Default {@link GiftService} implementation.
 *
 * @author Xiaohai Zhang
 * @since Feb 26, 2015
 */
@Named
@ExposeService(interfaceClass = GiftService.class)
public class GiftServiceImpl extends SpringContainerSupport implements GiftService {

    @Inject private GiftHistoryPersistence giftHistoryPersistence;

    @Override
    public MapMessage deleteGiftHistoryById(Long id) {
        if (id == null) {
            return MapMessage.errorMessage();
        }
        return giftHistoryPersistence.delete(id) ? MapMessage.successMessage() : MapMessage.errorMessage();
    }

    @Override
    public MapMessage updateLatestReply(Long id, String reply) {
        if (id == null) {
            return MapMessage.errorMessage();
        }
        giftHistoryPersistence.updateLatestReply(id, reply);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage updateThanks(Long id) {
        if (id == null) {
            return MapMessage.errorMessage();
        }
        giftHistoryPersistence.updateThanks(id);
        return MapMessage.successMessage();
    }
}
