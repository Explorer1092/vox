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

package com.voxlearning.utopia.service.parent.homework.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.entity.UserActivity;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 作业活动接口
 *
 * @author Wenlong Meng
 * @since Feb 23, 2019
 */
@ServiceVersion(version = "20190303")
@ServiceTimeout(timeout = 3, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 1)
public interface HomeworkUserActivityService {

    /**
     * 报名参加活动
     *
     * @param userActivity 活动
     * @return
     */
    MapMessage join(UserActivity userActivity);

    /**
     * 查询活动信息
     *
     * @param studentId 学生id
     * @param activityId 活动id
     * @return
     */
    MapMessage load(Long studentId, String activityId);

    /**
     * 查询用户活动信息
     *
     * @param id
     * @return
     */
    UserActivity load(String id);


    /**
     * 统计参加活动人数
     *
     * @return
     */
    int count(String activityId);

    /**
     * 查询未做活动的用户信息
     *
     * @param activityId
     * @return
     */
    List<Long> loadDNFUserIds(String activityId, Date startTime, Date endTime);

    /**
     * 查询完成任务用户
     *
     * @param activityId 活动id
     * @param startTime
     * @param endTime
     * @return
     */
    List<UserActivity> loadDoneUsers(String activityId, Date startTime, Date endTime);

}
