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
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.service.zone.api.ZonePhotoService;
import com.voxlearning.utopia.service.zone.impl.support.UploadPhotoCacheManager;

import javax.inject.Inject;
import javax.inject.Named;

@Named("com.voxlearning.utopia.service.zone.impl.service.ZonePhotoServiceImpl")
@ExposeService(interfaceClass = ZonePhotoService.class)
public class ZonePhotoServiceImpl implements ZonePhotoService {

    @Inject private UploadPhotoCacheManager uploadPhotoCacheManager;

    @Override
    public AlpsFuture<Boolean> photoUploaded(Long studentId, Long clazzId) {
        uploadPhotoCacheManager.photoUploaded(studentId, clazzId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> alreadyUploaded(Long studentId, Long clazzId) {
        boolean ret = uploadPhotoCacheManager.alreadyUploaded(studentId, clazzId);
        return new ValueWrapperFuture<>(true);
    }
}
