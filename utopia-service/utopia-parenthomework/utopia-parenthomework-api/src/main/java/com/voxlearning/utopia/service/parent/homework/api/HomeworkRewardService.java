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

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.api.mapper.StudentInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 作业奖励接口
 *
 * @author Wenlong Meng
 * @since Feb 21, 2019
 */
@ServiceVersion(version = "20190301")
@ServiceTimeout(timeout = 3, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 1)
public interface HomeworkRewardService {

    /**
     * 根据学生id查询作业奖励
     *
     * @param userId
     * @return
     */
    @Idempotent
    List<Map<String, Object>> loadByUserId(Long userId);

    /**
     * 作业奖励
     *
     * @param studentInfo
     * @param homeworkResult
     * @return
     */
    int reward(HomeworkResult homeworkResult, StudentInfo studentInfo);

}
