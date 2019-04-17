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

package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.entity.mission.Mission;
import com.voxlearning.utopia.entity.mission.MissionIntegralLog;
import com.voxlearning.utopia.entity.mission.MissionProgress;
import com.voxlearning.utopia.service.campaign.api.MissionService;
import com.voxlearning.utopia.service.campaign.impl.persistence.MissionIntegralLogPersistence;
import com.voxlearning.utopia.service.campaign.impl.persistence.MissionPersistence;
import com.voxlearning.utopia.service.campaign.impl.persistence.MissionProgressPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Named("com.voxlearning.utopia.service.campaign.impl.service.MissionServiceImpl")
@ExposeService(interfaceClass = MissionService.class)
public class MissionServiceImpl extends SpringContainerSupport implements MissionService {

    @Inject private MissionPersistence missionPersistence;
    @Inject private MissionIntegralLogPersistence missionIntegralLogPersistence;
    @Inject private MissionProgressPersistence missionProgressPersistence;

    @Override
    public AlpsFuture<Mission> insertMission(Mission mission) {
        if (mission == null) {
            return ValueWrapperFuture.nullInst();
        }
        missionPersistence.insert(mission);
        return new ValueWrapperFuture<>(mission);
    }

    @Override
    public AlpsFuture<Mission> updateMission(Mission mission) {
        if (mission == null || mission.getId() == null) {
            return ValueWrapperFuture.nullInst();
        }
        Mission modified = missionPersistence.replace(mission);
        return new ValueWrapperFuture<>(modified);
    }

    @Override
    public AlpsFuture<Mission> loadMission(Long id) {
        return new ValueWrapperFuture<>(missionPersistence.load(id));
    }

    @Override
    public AlpsFuture<Map<Long, Mission>> loadMissions(Collection<Long> ids) {
        return new ValueWrapperFuture<>(missionPersistence.loads(ids));
    }

    @Override
    public AlpsFuture<Set<Mission.Location>> queryMissionLocations(Long studentId) {
        if (studentId == null) {
            return ValueWrapperFuture.emptySet();
        }
        return new ValueWrapperFuture<>(missionPersistence.queryLocations(studentId));
    }

    @Override
    public AlpsFuture<Map<Long, Set<Mission.Location>>> queryMissionLocations(Collection<Long> studentIds) {
        Set<Long> set = CollectionUtils.toLinkedHashSet(studentIds);
        if (set.isEmpty()) {
            return ValueWrapperFuture.emptyMap();
        }
        return new ValueWrapperFuture<>(missionPersistence.queryLocations(set));
    }

    @Override
    public AlpsFuture<Boolean> increaseMissionFinishCount(Long id, int delta) {
        if (id == null || delta <= 0) {
            return new ValueWrapperFuture<>(false);
        }
        boolean ret = missionPersistence.increaseFinishCount(id, delta);
        return new ValueWrapperFuture<>(ret);
    }

    @Override
    public AlpsFuture<Boolean> updateMissionComplete(Long id) {
        if (id == null) {
            return new ValueWrapperFuture<>(false);
        }
        boolean ret = missionPersistence.updateComplete(id);
        return new ValueWrapperFuture<>(ret);
    }

    @Override
    public AlpsFuture<Boolean> updateMissionImg(Long id, String img) {
        if (id == null || img == null) {
            return new ValueWrapperFuture<>(false);
        }
        boolean ret = missionPersistence.updateImg(id, img);
        return new ValueWrapperFuture<>(ret);
    }

    @Override
    public AlpsFuture<List<MissionProgress>> findMissionProgressList(Long missionId) {
        if (missionId == null) {
            return ValueWrapperFuture.emptyList();
        }
        List<MissionProgress> progressList = missionProgressPersistence.findByMissionId(missionId);
        return new ValueWrapperFuture<>(progressList);
    }

    @Override
    public AlpsFuture<MissionIntegralLog> insertMissionIntegralLog(MissionIntegralLog log) {
        if (log == null) {
            return ValueWrapperFuture.nullInst();
        }
        missionIntegralLogPersistence.insert(log);
        return new ValueWrapperFuture<>(log);
    }

    @Override
    public AlpsFuture<List<MissionIntegralLog>> findMissionIntegralLogs(Long studentId) {
        if (studentId == null) {
            return ValueWrapperFuture.emptyList();
        }
        List<MissionIntegralLog> logs = missionIntegralLogPersistence.findByStudentId(studentId);
        return new ValueWrapperFuture<>(logs);
    }
}
