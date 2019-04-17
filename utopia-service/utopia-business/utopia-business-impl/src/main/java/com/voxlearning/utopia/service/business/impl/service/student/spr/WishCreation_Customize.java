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

package com.voxlearning.utopia.service.business.impl.service.student.spr;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.MissionState;
import com.voxlearning.utopia.api.constant.MissionType;
import com.voxlearning.utopia.api.constant.WishType;
import com.voxlearning.utopia.business.api.constant.IdentificationWishType;
import com.voxlearning.utopia.entity.mission.Mission;
import com.voxlearning.utopia.service.business.impl.service.AsyncBusinessCacheServiceImpl;
import com.voxlearning.utopia.service.campaign.client.MissionServiceClient;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author RuiBao
 * @version 0.1
 * @since 1/12/2015
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationWishType(WishType.CUSTOMIZE)
public class WishCreation_Customize extends WishCreationTemplate {

    @Inject private AsyncBusinessCacheServiceImpl asyncBusinessCacheService;
    @Inject private MissionServiceClient missionServiceClient;

    @Override
    protected MapMessage canMakeWish(WishCreationContext context) {
        if (StringUtils.isBlank(context.getWish())) {
            return MapMessage.errorMessage("请先填写你的心愿");
        }
        Long studentId = context.getUserId();
        if (asyncBusinessCacheService.StudentWishCreationCacheManager_wishMadeThisWeek(studentId).getUninterruptibly()) {
            return MapMessage.errorMessage("每星期只能许一个愿望哦");
        }
        return MapMessage.successMessage();
    }

    @Override
    protected Long save(WishCreationContext context) {
        Mission mission = new Mission();
        mission.setStudentId(context.getUserId());
        mission.setWishType(context.getType());
        mission.setWish(context.getWish());
        mission.setMissionState(MissionState.WISH);
        mission.setMissionType(MissionType.OTHER);
        mission = missionServiceClient.getMissionService().insertMission(mission).getUninterruptibly();
        return mission == null ? null : mission.getId();
    }
}
