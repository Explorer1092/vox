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
import com.voxlearning.utopia.api.constant.MissionType;
import com.voxlearning.utopia.api.constant.WishType;
import com.voxlearning.utopia.business.api.constant.IdentificationWishType;
import com.voxlearning.utopia.entity.mission.Mission;
import com.voxlearning.utopia.service.campaign.client.MissionServiceClient;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Objects;

import static com.voxlearning.utopia.api.constant.MissionState.ONGOING;
import static com.voxlearning.utopia.api.constant.MissionState.WISH;

/**
 * @author RuiBao
 * @version 0.1
 * @since 1/14/2015
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationWishType(WishType.CUSTOMIZE)
public class MissionCreation_Customize extends MissionCreationTemplate {

    @Inject private MissionServiceClient missionServiceClient;

    @Override
    protected MapMessage canSetMission(MissionCreationContext context) {
        if (StringUtils.isBlank(context.getWish())) {
            return MapMessage.errorMessage("请填写奖励");
        }
        return MapMessage.successMessage();
    }

    @Override
    protected MapMessage save(MissionCreationContext context) {
        // 如果missionId存在，表示需要将学生的愿望更新，
        Long missionId = context.getMissionId();
        if (missionId != null && missionId != 0) {
            Mission mission = missionServiceClient.getMissionService().loadMission(missionId).getUninterruptibly();
            if (mission == null) return MapMessage.errorMessage("设置任务失败");
            if (mission.getMissionState() != WISH) return MapMessage.errorMessage("您已经设置了任务，请到未完成列表中查看");
            if (!Objects.equals(mission.getStudentId(), context.getStudentId()))
                return MapMessage.errorMessage("您不是这个孩子的家长");

            Mission candidate = new Mission();
            candidate.setId(missionId);
            candidate.setMission(context.getMission());
            candidate.setMissionType(MissionType.OTHER);
            candidate.setTotalCount(context.getTotalCount());
            candidate.setMissionState(ONGOING);
            candidate.setWishType(WishType.CUSTOMIZE);
            candidate.setIntegral(0);
            candidate.setWish(context.getWish());
            missionServiceClient.getMissionService().updateMission(candidate).awaitUninterruptibly();
        } else {
            Mission mission = new Mission();
            mission.setStudentId(context.getStudentId());
            mission.setWishType(context.getWishType());
            mission.setWish(context.getWish());
            mission.setMissionState(ONGOING);
            mission.setTotalCount(context.getTotalCount());
            mission.setMission(context.getMission());
            mission.setMissionType(context.getMissionType());
            mission.setMissionDatetime(new Date());
            missionServiceClient.getMissionService().insertMission(mission).awaitUninterruptibly();
        }
        return MapMessage.successMessage();
    }
}
