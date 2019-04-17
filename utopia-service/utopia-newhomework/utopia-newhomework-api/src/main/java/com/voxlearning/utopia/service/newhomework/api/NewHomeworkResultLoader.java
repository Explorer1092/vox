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

package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultExtendedInfo;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20180410")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface NewHomeworkResultLoader extends IPingable {

    @Idempotent
    Map<String, NewHomeworkResult> loads(Collection<String> ids, boolean needAnswer);

    /**
     * 获取学生的作业数据
     *
     * @param location 作业
     * @param userId   用户id
     * @return NewHomeworkResult
     */
    @Idempotent
    NewHomeworkResult loadNewHomeworkResult(NewHomework.Location location, Long userId, boolean needAnswer);

    List<String> initSubHomeworkResultAnswerIds(NewHomework newHomework, Long userId);

    List<String> fetchSubHomeworkResultAnswerIdsByType(NewHomework newHomework, Long userId, Set<ObjectiveConfigType> type);

    @Idempotent
    Map<Long, NewHomeworkResult> loadNewHomeworkResult(NewHomework.Location location, Collection<Long> userIds, boolean needAnswer);

    @Idempotent
    List<NewHomeworkResult> loadNewHomeworkResult(Collection<NewHomework.Location> locations, Long userId);

    @Idempotent
    List<NewHomeworkResult> loadNewHomeworkResult(Collection<NewHomework.Location> locations, Long userId, boolean needAnswer);

    @Idempotent
    Map<String, NewHomeworkResult> findByHomework(NewHomework newHomework);

    /**
     * 这个方法不会获取SubHomeworkResultAnswer，调用前请确认不需要processResultId
     */
    @Idempotent
    Map<String, NewHomeworkResult> findByHomeworkForReport(NewHomework newHomework);

    /**
     * 批量获取多个作业的NewHomeworkResult
     * 注意：SubHomeworkResult里面没有practices信息，这个方法只能用于老师端作业报告
     */
    @Idempotent
    Map<String, Set<NewHomeworkResult>> findByHomeworksForReport(Collection<NewHomework> newHomeworks);

    @Idempotent
    Integer homeworkIntegral(boolean repair, NewHomeworkResult newHomeworkResult);

    @Idempotent
    Integer generateFinishHomeworkActivityIntegral(Integer integral, NewHomework newHomework, Integer regionCode);

    @Idempotent
    Map<Long, Map<String, Integer>> getCurrentMonthHomeworkRankByGroupId(Long studentId);

    @Idempotent
    SubHomeworkResultExtendedInfo loadSubHomeworkResultExtentedInfo(String id);
}
