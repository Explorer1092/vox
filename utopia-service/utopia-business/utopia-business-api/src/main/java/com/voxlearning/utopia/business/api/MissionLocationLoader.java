/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.lang.support.LocationLoader;
import com.voxlearning.alps.lang.support.LocationTransformer;
import com.voxlearning.utopia.entity.mission.Mission;

import java.util.Collection;

public class MissionLocationLoader extends LocationLoader<MissionLocationLoader, Mission.Location, Mission> {
    public MissionLocationLoader(LocationTransformer<Mission.Location, Mission> transformer,
                                 Collection<Mission.Location> locations) {
        super(transformer, locations);
    }
}
