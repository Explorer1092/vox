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

package com.voxlearning.utopia.agent.service.apply;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.School;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dell on 2015/7/13.
 */
@Named
public class ApplyService extends AbstractAgentService {

    @Inject private RaikouSystem raikouSystem;

    public Map<Integer, ExRegion> loadRegions(List<Integer> regionCodes) {
        return raikouSystem.getRegionBuffer().loadRegions(regionCodes);
    }

    public List<School> searchSchool(List<Integer> regionCodes, SchoolLevel schoolLevel) {
        Set<Integer> codes = CollectionUtils.toLinkedHashSet(regionCodes);
        if (codes.isEmpty()) {
            return Collections.emptyList();
        }
        return raikouSystem.querySchoolLocations(codes)
                .enabled()
                .filter(e -> e.match(schoolLevel))
                .transform()
                .asList()
                .stream()
                .sorted(Comparator.comparing(School::getId))
                .collect(Collectors.toList());
    }
}
