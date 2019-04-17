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

package com.voxlearning.utopia.service.business.impl.mission;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.business.api.MissionLoader;
import com.voxlearning.utopia.entity.mission.Mission;
import com.voxlearning.utopia.mapper.MissionMapper;
import com.voxlearning.utopia.service.business.base.AbstractMissionLoader;
import com.voxlearning.utopia.service.business.impl.service.AsyncBusinessCacheServiceImpl;
import com.voxlearning.utopia.service.campaign.client.MissionServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.voxlearning.alps.annotation.meta.UserType.STUDENT;
import static com.voxlearning.utopia.mapper.MissionMapper.*;
import static com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeType.TEMPLATE_REMIND_REWARD;
import static com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeType.TEMPLATE_REMIND_UPDATE_PROGRESS;

@Named("com.voxlearning.utopia.service.business.impl.mission.MissionLoaderImpl")
@Service(interfaceClass = MissionLoader.class)
@ExposeService(interfaceClass = MissionLoader.class)
public class MissionLoaderImpl extends AbstractMissionLoader {

    @Inject private MissionServiceClient missionServiceClient;
    @Inject private AsyncBusinessCacheServiceImpl asyncBusinessCacheService;

    @Override
    @Deprecated
    public Map<Long, Mission> loadMissions(Collection<Long> ids) {
        Set<Long> set = CollectionUtils.toLinkedHashSet(ids);
        if (set.isEmpty()) {
            return Collections.emptyMap();
        }
        return missionServiceClient.getMissionService().loadMissions(set).getUninterruptibly();
    }

    @Override
    @Deprecated
    public Map<Long, Set<Mission.Location>> __queryStudentMissionLocations(Collection<Long> studentIds) {
        Set<Long> set = CollectionUtils.toLinkedHashSet(studentIds);
        if (set.isEmpty()) {
            return Collections.emptyMap();
        }
        return missionServiceClient.getMissionService().queryMissionLocations(set).getUninterruptibly();
    }

    @Override
    public MissionMapper transformMission(Mission mission, Long studentId, UserType userType) {
        if (mission == null || studentId == null) return null;

        if (mission.getWishType() == null || mission.getMissionState() == null || mission.getMissionType() == null)
            return null;

        MissionMapper mm = MissionMapper.newInstance(mission.getWishType(), mission.getMissionType(),
                mission.getMissionState());
        switch (mission.getMissionState()) {
            case ONGOING: {
                if (mission.getFinishCount() < mission.getTotalCount()) {
                    if (userType == STUDENT) {
                        mm.setOp(STUDENT_REMIND_PROGRESS);
                        mm.setCanClick(!asyncBusinessCacheService.StudentMissionNoticeCacheManager_sendToday(studentId, mission.getId(), TEMPLATE_REMIND_UPDATE_PROGRESS.name())
                                .getUninterruptibly());
                    } else {
                        mm.setOp(PARENT_UPDATE_PROGRESS);
                    }
                } else {
                    if (userType == STUDENT) {
                        mm.setOp(STUDENT_REMIND_REWARD);
                        mm.setCanClick(!asyncBusinessCacheService.StudentMissionNoticeCacheManager_sendToday(studentId, mission.getId(), TEMPLATE_REMIND_REWARD.name())
                                .getUninterruptibly());
                    } else {
                        mm.setOp(PARENT_REWARD);
                    }
                }
                break;
            }
            case COMPLETE:
                break;
            case WISH:
                break;
            default:
                return null;
        }

        mm.setImg(mission.getImg());
        mm.setRewards(mission.formalizeWishContent());
        mm.setMission(mission.formalizeMissionContent());
        mm.setTotalCount(mission.getTotalCount());
        mm.setFinishCount(mission.getFinishCount());
        mm.setId(mission.getId());
        Date missionDate = mission.getMissionDatetime() == null ? new Date(mission.fetchCreateTimestamp()) : mission.getMissionDatetime();
        mm.setMissionDate(DateUtils.dateToString(missionDate, "yyyy.MM.dd"));
        return mm;
    }
}
