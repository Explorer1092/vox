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

package com.voxlearning.utopia.service.mizar.consumer.loader;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.mizar.api.entity.hbs.HbsContestant;
import com.voxlearning.utopia.service.mizar.api.entity.hbs.HbsUser;
import com.voxlearning.utopia.service.mizar.api.loader.HbsLoader;

/**
 * Created by haitian.gan on 2017/2/15.
 */
public class HbsLoaderClient implements HbsLoader {

    @ImportService(interfaceClass = HbsLoader.class)
    private HbsLoader remoteReference;

    @Override
    public HbsUser loadUser(Long userId) {
        return remoteReference.loadUser(userId);
    }

    @Override
    public HbsUser loadUserByName(String userName) {
        return remoteReference.loadUserByName(userName);
    }

    @Override
    public HbsContestant getContestant(Long studentId) {
        return remoteReference.getContestant(studentId);
    }

    @Override
    public HbsContestant loadContestant(Long studentId) {
        return remoteReference.loadContestant(studentId);
    }

    @Override
    public HbsContestant findStudentByIdCardNo(String idCardNo) {
        return remoteReference.findStudentByIdCardNo(idCardNo);
    }

    @Override
    public HbsContestant findStudentByPhoneNumber(String phoneNubmer) {
        return remoteReference.findStudentByPhoneNumber(phoneNubmer);
    }


}
