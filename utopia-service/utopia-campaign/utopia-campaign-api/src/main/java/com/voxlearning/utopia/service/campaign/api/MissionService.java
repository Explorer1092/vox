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

package com.voxlearning.utopia.service.campaign.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.utopia.entity.mission.Mission;
import com.voxlearning.utopia.entity.mission.MissionIntegralLog;
import com.voxlearning.utopia.entity.mission.MissionProgress;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.07.12")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MissionService {

    @Async
    AlpsFuture<Mission> insertMission(Mission mission);

    @Async
    AlpsFuture<Mission> updateMission(Mission mission);

    @Async
    AlpsFuture<Mission> loadMission(Long id);

    @Async
    AlpsFuture<Map<Long, Mission>> loadMissions(Collection<Long> ids);

    @Async
    AlpsFuture<Set<Mission.Location>> queryMissionLocations(Long studentId);

    @Async
    AlpsFuture<Map<Long, Set<Mission.Location>>> queryMissionLocations(Collection<Long> studentIds);

    @Async
    AlpsFuture<Boolean> increaseMissionFinishCount(Long id, int delta);

    @Async
    AlpsFuture<Boolean> updateMissionComplete(Long id);

    @Async
    AlpsFuture<Boolean> updateMissionImg(Long id, String img);

    @Async
    AlpsFuture<List<MissionProgress>> findMissionProgressList(Long missionId);

    @Async
    AlpsFuture<MissionIntegralLog> insertMissionIntegralLog(MissionIntegralLog log);

    @Async
    AlpsFuture<List<MissionIntegralLog>> findMissionIntegralLogs(Long studentId);
}
