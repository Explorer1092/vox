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
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 作业结果查询接口,提供作业结果及详情的查新接口
 *
 * @author Wenlong Meng
 * @version 20181107
 * @date 2018-11-07
 */
@ServiceVersion(version = "20181111")
@ServiceTimeout(timeout = 3, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 1)
public interface HomeworkResultLoader {

    /**
     * 根据作业id、用户id查询作业结果
     *
     * @param homeworkId 作业id
     * @param userId 用户id
     * @return 作业结果
     */
    @Idempotent
    HomeworkResult loadHomeworkResult(String homeworkId, Long userId);

    /**
     * 根据id查询作业结果
     *
     * @param id 作业结果id
     * @return 作业结果
     */
    @Idempotent
    HomeworkResult loadHomeworkResult(String id);

    /**
     * 根据作业id、用户id查询所有作业结果，包括订正、重做
     *
     * @param homeworkId 作业id
     * @param userId 用户id
     * @return 作业结果
     */
    @Idempotent
    List<HomeworkResult> loadHomeworkResults(String homeworkId, Long userId);

    /**
     * 根据用户id查询当天作业结果
     *
     * @param userId 用户id
     * @return 作业结果
     */
    @Idempotent
    List<HomeworkResult> loadHomeworkResultByUserId(Long userId);

    /**
     * 根据作业结果id查询结果详情
     *
     * @param homeworkResultId 作业结果id
     * @return 作业结果详情
     */
    @Idempotent
    List<HomeworkProcessResult> loadHomeworkProcessResults(String homeworkResultId);

    /**
     * 根据用户id、时间的作业结果:下分页
     *
     * @param userId 用户id
     * @param start 开始条数
     * @param size 获取条数
     * @param startTime 获取条数
     * @return 作业结果
     */
    @Idempotent
    List<HomeworkResult> loadHomeworkResultDown(Long userId, Integer start, Integer size, Date startTime);

}
