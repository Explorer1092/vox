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

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.business.api.MiscLoader;
import com.voxlearning.utopia.entity.activity.InterestingReport;
import com.voxlearning.utopia.entity.misc.IntegralActivity;
import com.voxlearning.utopia.mapper.UgcRecordMapper;
import com.voxlearning.utopia.service.business.base.AbstractMiscLoader;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.util.List;
import java.util.Map;

public class MiscLoaderClient extends AbstractMiscLoader {

    @ImportService(interfaceClass = MiscLoader.class)
    private MiscLoader remoteReference;

    @Override
    @Deprecated
    public List<IntegralActivity> loadAllIntegralActivities() {
        return remoteReference.loadAllIntegralActivities();
    }

    @Override
    @Deprecated
    public Page<IntegralActivity> loadIntegralActivityPage(Pageable pageable, Integer department, Integer status) {
        return remoteReference.loadIntegralActivityPage(pageable, department, status);
    }

    @Override
    @Deprecated
    public IntegralActivity loadIntegralActivityById(Long activityId) {
        return remoteReference.loadIntegralActivityById(activityId);
    }

    @Override
    @Deprecated
    public Map<String, Object> loadIntegralActivityDetail(Long activityId) {
        return remoteReference.loadIntegralActivityDetail(activityId);
    }

    @Override
    public boolean checkIntegralType(Long ruleId, Integer type) {
        return remoteReference.checkIntegralType(ruleId, type);
    }

    @Override
    public UgcRecordMapper loadEnableUserUgcRecord(User user) {
        return remoteReference.loadEnableUserUgcRecord(user);
    }

    @Override
    public UgcRecordMapper loadEnableUserUgcRecordByRecordId(User user, Long recordId) {
        return remoteReference.loadEnableUserUgcRecordByRecordId(user, recordId);
    }

    public InterestingReport loadUserInterestingReport(Long userId) {
        return remoteReference.loadUserInterestingReport(userId);
    }
}
