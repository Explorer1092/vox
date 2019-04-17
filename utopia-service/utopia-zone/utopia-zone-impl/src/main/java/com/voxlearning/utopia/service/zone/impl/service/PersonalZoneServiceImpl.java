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
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentServiceClient;
import com.voxlearning.utopia.service.user.consumer.TinyGroupLoaderClient;
import com.voxlearning.utopia.service.zone.api.PersonalZoneLoader;
import com.voxlearning.utopia.service.zone.api.PersonalZoneService;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneBag;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneProduct;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneShoppingLog;
import com.voxlearning.utopia.service.zone.impl.loader.PersonalZoneLoaderImpl;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzZoneBagPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzZoneShoppingLogPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.StudentInfoPersistence;
import com.voxlearning.utopia.service.zone.impl.support.AbstractPersonalZoneService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

@Named
@ExposeService(interfaceClass = PersonalZoneService.class)
public class PersonalZoneServiceImpl extends AbstractPersonalZoneService {

    @Inject private ClazzZoneBagPersistence clazzZoneBagPersistence;
    @Inject private ClazzZoneShoppingLogPersistence clazzZoneShoppingLogPersistence;
    @Inject private PersonalZoneLoaderImpl personalZoneLoader;
    @Inject private StudentInfoPersistence studentInfoPersistence;
    @Inject private TinyGroupLoaderClient tinyGroupLoaderClient;
    @Inject private ZoneProductServiceImpl zoneProductService;
    @Inject private StudentServiceClient studentServiceClient;

    @ImportService(interfaceClass = UserIntegralService.class) protected UserIntegralService userIntegralService;

    @Override
    protected PersonalZoneLoader getPersonalZoneLoader() {
        return personalZoneLoader;
    }

    @Override
    public MapMessage changeBubble(StudentDetail student, Long bubbleId) {
        return $changeBubble(student, bubbleId, zoneProductService.getClazzZoneProductBuffer(), tinyGroupLoaderClient);
    }

    @Override
    public MapMessage changeHeadWear(Long studentId, String headWearId) {
        if (null == studentId || 0 == studentId) {
            return MapMessage.errorMessage("参数错误");
        }

        studentInfoPersistence.createOrUpdateHeadWear(studentId, headWearId);
        return studentServiceClient.changeHeadWear(studentId, headWearId);
    }

    @Override
    public MapMessage resetHeadWear(Long studentId) {
        if (null == studentId || 0 == studentId) {
            return MapMessage.errorMessage("参数错误");
        }

        studentInfoPersistence.createOrUpdateHeadWear(studentId, null);
        return studentServiceClient.resetHeadWear(studentId);
    }

    @Override
    public MapMessage __purchaseBubble(StudentDetail student, Long bubbleId) {
        ClazzZoneProduct bubble = zoneProductService.getClazzZoneProductBuffer().load(bubbleId);
        IntegralHistory history = new IntegralHistory(student.getId(),
                IntegralType.学生班级空间购买付费气泡, -bubble.getPrice());
        history.setComment("购买气泡扣除学豆");
        MapMessage message = userIntegralService.changeIntegral(student, history);
        if (!message.isSuccess()) {
            return message;
        }

        ClazzZoneShoppingLog log = new ClazzZoneShoppingLog();
        log.setUserId(student.getId());
        log.setProductId(bubble.getId());
        log.setPrice(bubble.getPrice());
        log.setCurrency(bubble.getCurrency());
        log.setPeriodOfValidity(bubble.getPeriodOfValidity());
        clazzZoneShoppingLogPersistence.insert(log);

        ClazzZoneBag bag = new ClazzZoneBag();
        bag.setUserId(student.getId());
        bag.setProductId(bubble.getId());
        bag.setExpireDate(new Date(System.currentTimeMillis() + bubble.getPeriodOfValidity() * 86400L * 1000));
        clazzZoneBagPersistence.insert(bag);

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage __changeBubble(StudentDetail student, Long bubbleId) {
        studentInfoPersistence.createOrUpdateBubble(student.getId(), bubbleId);
        return studentServiceClient.changeBubble(student.getId(), bubbleId);
    }
}
