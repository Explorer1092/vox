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
import com.voxlearning.utopia.service.zone.api.ZoneBagService;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneBag;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzZoneBagPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;

@Named("com.voxlearning.utopia.service.zone.impl.service.ZoneBagServiceImpl")
@ExposeService(interfaceClass = ZoneBagService.class)
public class ZoneBagServiceImpl extends SpringContainerSupport implements ZoneBagService {

    @Inject private ClazzZoneBagPersistence clazzZoneBagPersistence;

    @Override
    public AlpsFuture<List<ClazzZoneBag>> findClazzZoneBagList(Long userId) {
        if (userId == null) {
            return new ValueWrapperFuture<>(Collections.emptyList());
        }
        List<ClazzZoneBag> list = clazzZoneBagPersistence.findByUserId(userId);
        return new ValueWrapperFuture<>(list);
    }
}
