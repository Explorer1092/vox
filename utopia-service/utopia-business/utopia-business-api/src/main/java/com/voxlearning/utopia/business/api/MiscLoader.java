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

package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.entity.activity.InterestingReport;
import com.voxlearning.utopia.entity.misc.IntegralActivity;
import com.voxlearning.utopia.mapper.UgcRecordMapper;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2.0")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface MiscLoader extends IPingable {

    // ========================================================================
    // 积分活动相关的方法 By Wyc 2016-01-15
    // ========================================================================

    /**
     * 获取所有的积分活动
     */
    @Deprecated
    List<IntegralActivity> loadAllIntegralActivities();

    /**
     * 获取当页的活动
     */
    @Deprecated
    Page<IntegralActivity> loadIntegralActivityPage(Pageable pageable, Integer department, Integer status);

    /**
     * 获取单条活动信息
     */
    @Deprecated
    IntegralActivity loadIntegralActivityById(Long activityId);

    /**
     * 根据活动Id，获取该活动以及活动下所有的规则
     */
    @Deprecated
    Map<String, Object> loadIntegralActivityDetail(Long activityId);

    /**
     * 判断新增的积分类型是否重复
     */
    boolean checkIntegralType(Long ruleId, Integer type);

    /**
     * 获取用户当前有效的UGC活动
     *
     * @param user
     * @return
     */
    UgcRecordMapper loadEnableUserUgcRecord(User user);

    /**
     * 获取Ugc详情 根据活动ID， APP使用
     *
     * @param recordId
     * @return
     */
    UgcRecordMapper loadEnableUserUgcRecordByRecordId(User user, Long recordId);

    InterestingReport loadUserInterestingReport(Long userId);
}
