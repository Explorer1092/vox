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

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.MissionType;
import com.voxlearning.utopia.api.constant.WishType;
import com.voxlearning.utopia.business.api.constant.IdentificationWishType;
import com.voxlearning.utopia.entity.mission.Mission;
import com.voxlearning.utopia.entity.mission.MissionIntegralLog;
import com.voxlearning.utopia.service.campaign.client.MissionServiceClient;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

import static com.voxlearning.utopia.api.constant.MissionState.ONGOING;

/**
 * @author RuiBao
 * @version 0.1
 * @since 1/14/2015
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationWishType(WishType.INTEGRAL)
public class MissionCreation_Integral extends MissionCreationTemplate {

    @Inject private MissionServiceClient missionServiceClient;

    @Override
    protected MapMessage canSetMission(MissionCreationContext context) {
        if (context.getIntegral() == null || context.getIntegral() == 0) {
            return MapMessage.errorMessage("请填写奖励");
        }
        Long studentId = context.getStudentId();
        String month = DateUtils.dateToString(new Date(), DateUtils.FORMAT_YEAR_MONTH);
        if (missionServiceClient.getMissionService()
                .findMissionIntegralLogs(studentId)
                .getUninterruptibly()
                .stream()
                .filter(t -> StringUtils.equals(month, t.getMonth()))
                .count() > 0) {
            return MapMessage.errorMessage("不能布置奖励为学豆的目标");
        }
        return MapMessage.successMessage();
    }

    @Override
    protected MapMessage save(MissionCreationContext context) {
        // 学生不能许奖励为学豆的愿望，所以直接存即可
        Mission mission = new Mission();
        mission.setStudentId(context.getStudentId());
        mission.setWishType(context.getWishType());
        mission.setIntegral(context.getIntegral());
        mission.setMissionState(ONGOING);
        mission.setTotalCount(context.getTotalCount());
        mission.setMission(context.getMission());
        mission.setMissionType(MissionType.OTHER);
        mission.setMissionDatetime(new Date());
        mission = missionServiceClient.getMissionService().insertMission(mission).getUninterruptibly();
        if (mission == null) {
            return MapMessage.errorMessage();
        }
        Long missionId = mission.getId();
        // 记录花费10学豆
        String month = DateUtils.dateToString(new Date(), DateUtils.FORMAT_YEAR_MONTH);
        MissionIntegralLog log = MissionIntegralLog.newInstance(context.getStudentId(), missionId, month);
        try {
            missionServiceClient.getMissionService()
                    .insertMissionIntegralLog(log)
                    .awaitUninterruptibly();
        } catch (Exception ignored) {
        }
        return MapMessage.successMessage();
    }
}
