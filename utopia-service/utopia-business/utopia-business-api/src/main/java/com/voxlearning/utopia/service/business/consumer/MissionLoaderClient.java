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

package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.business.api.MissionLoader;
import com.voxlearning.utopia.entity.mission.Mission;
import com.voxlearning.utopia.mapper.MissionMapper;
import com.voxlearning.utopia.service.business.base.AbstractMissionLoader;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class MissionLoaderClient extends AbstractMissionLoader {

    @ImportService(interfaceClass = MissionLoader.class)
    private MissionLoader remoteReference;

    @Override
    @Deprecated
    public Map<Long, Mission> loadMissions(Collection<Long> ids) {
        return remoteReference.loadMissions(ids);
    }

    @Override
    @Deprecated
    public Map<Long, Set<Mission.Location>> __queryStudentMissionLocations(Collection<Long> studentIds) {
        return remoteReference.__queryStudentMissionLocations(studentIds);
    }

    @Override
    public MissionMapper transformMission(Mission mission, Long studentId, UserType userType) {
        return remoteReference.transformMission(mission, studentId, userType);
    }
}
