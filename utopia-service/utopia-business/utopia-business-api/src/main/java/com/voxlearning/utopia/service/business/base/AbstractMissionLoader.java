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

package com.voxlearning.utopia.service.business.base;

import com.voxlearning.alps.lang.support.LocationTransformer;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.business.api.MissionLoader;
import com.voxlearning.utopia.business.api.MissionLocationLoader;
import com.voxlearning.utopia.entity.mission.Mission;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

abstract public class AbstractMissionLoader implements MissionLoader {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public MissionLocationLoader loadStudentMissions(Long studentId) {
        return loadStudentMissions(Collections.singleton(studentId));
    }

    public MissionLocationLoader loadStudentMissions(Collection<Long> studentIds) {
        Set<Mission.Location> locations = __queryStudentMissionLocations(studentIds)
                .values()
                .stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        return __newMissionLocationLoader(locations);
    }

    private MissionLocationLoader __newMissionLocationLoader(Collection<Mission.Location> locations) {
        LocationTransformer<Mission.Location, Mission> transformer = candidate -> {
            List<Long> idList = candidate.stream()
                    .map(Mission.Location::getId)
                    .collect(Collectors.toList());
            Map<Long, Mission> missions = loadMissions(idList);
            return idList.stream()
                    .map(missions::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        };
        return new MissionLocationLoader(transformer, locations);
    }
}
