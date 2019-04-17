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

package com.voxlearning.utopia.service.mizar.consumer.service;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.service.HbsService;

/**
 * Created by ganhaitian on 2017/2/15.
 */
public class HbsServiceClient implements HbsService {

    @ImportService(interfaceClass = HbsService.class)
    private HbsService remoteReference;

    @Override
    public MapMessage updateUserPhoneNumber(Long userId, String phoneNumber) {
        return remoteReference.updateUserPhoneNumber(userId, phoneNumber);
    }

    @Override
    public MapMessage updateUserPwd(Long userId, String password) {
        return remoteReference.updateUserPwd(userId, password);
    }
}
