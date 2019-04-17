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

package com.voxlearning.utopia.service.zone.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.service.zone.api.ZoneConfigService;
import com.voxlearning.utopia.service.zone.data.VersionedClazzZoneProductData;
import com.voxlearning.utopia.service.zone.data.VersionedGiftData;
import com.voxlearning.utopia.service.zone.data.VersionedUserMoodData;

import javax.inject.Inject;
import javax.inject.Named;

@Named("com.voxlearning.utopia.service.zone.impl.service.ZoneConfigServiceImpl")
@ExposeService(interfaceClass = ZoneConfigService.class)
@Deprecated
public class ZoneConfigServiceImpl extends SpringContainerSupport implements ZoneConfigService {

    @Inject private UserMoodServiceImpl userMoodService;
    @Inject private ZoneGiftServiceImpl zoneGiftService;
    @Inject private ZoneProductServiceImpl zoneProductService;

    @Override
    @Deprecated
    public AlpsFuture<VersionedClazzZoneProductData> loadVersionedClazzZoneProductData(long version) {
        VersionedClazzZoneProductData data = zoneProductService.loadClazzZoneProductBufferData(version).getUninterruptibly();
        return new ValueWrapperFuture<>(data);
    }

    @Override
    @Deprecated
    public AlpsFuture<VersionedGiftData> loadVersionedGiftData(long version) {
        VersionedGiftData data = zoneGiftService.loadGiftBufferData(version).getUninterruptibly();
        return new ValueWrapperFuture<>(data);
    }

    @Override
    @Deprecated
    public AlpsFuture<VersionedUserMoodData> loadVersionedUserMoodData(long version) {
        VersionedUserMoodData data = userMoodService.loadUserMoodBufferData(version).getUninterruptibly();
        return new ValueWrapperFuture<>(data);
    }
}
