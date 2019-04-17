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
import com.voxlearning.utopia.service.zone.api.ClazzZoneService;
import com.voxlearning.utopia.service.zone.api.mapper.SendGiftContext;
import com.voxlearning.utopia.service.zone.impl.persistence.StudentInfoPersistence;
import com.voxlearning.utopia.service.zone.impl.service.gift.GiftSender;
import com.voxlearning.utopia.service.zone.impl.service.gift.GiftSenderManager;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Default {@link ClazzZoneService} implementation.
 *
 * @author Xiaohai Zhang
 * @since Feb 25, 2015
 */
@Named
@ExposeService(interfaceClass = ClazzZoneService.class)
public class ClazzZoneServiceImpl extends SpringContainerSupport implements ClazzZoneService {

    @Inject private GiftSenderManager giftSenderManager;
    @Inject private StudentInfoPersistence studentInfoPersistence;

    @Override
    public MapMessage increaseStudyMasterCountByOne(Long studentId) {
        if (studentId == null) {
            return MapMessage.errorMessage();
        }
        studentInfoPersistence.createOrIncreaseStudyMasterCountByOne(studentId);
        return MapMessage.successMessage();
    }

    public MapMessage sendGift(SendGiftContext context) {
        if (context == null) {
            return MapMessage.errorMessage();
        }
        GiftSender sender = giftSenderManager.get(context.getType());
        if (sender == null) {
            logger.error("Unrecognized gift type {}", context.getType());
            return MapMessage.errorMessage();
        }
        return sender.sendGift(context);
    }
}
