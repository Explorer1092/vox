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

package com.voxlearning.utopia.service.push.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.service.push.api.AppJpushMessageRetryService;
import com.voxlearning.utopia.service.push.api.entity.AppJpushMessageRetry;
import com.voxlearning.utopia.service.push.impl.persistence.AppJpushMessageRetryPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("com.voxlearning.utopia.service.push.impl.service.AppJpushMessageRetryServiceImpl")
@ExposeService(interfaceClass = AppJpushMessageRetryService.class)
public class AppJpushMessageRetryServiceImpl extends SpringContainerSupport implements AppJpushMessageRetryService {

    @Inject private AppJpushMessageRetryPersistence appJpushMessageRetryPersistence;

    @Override
    public AlpsFuture<List<AppJpushMessageRetry>> loadRetryList() {
        return new ValueWrapperFuture<>(appJpushMessageRetryPersistence.loadRetryList());
    }

    @Override
    public AlpsFuture<MapMessage> cleanUp(Long time) {
        return new ValueWrapperFuture<>(appJpushMessageRetryPersistence.cleanUp(time));
    }
}
