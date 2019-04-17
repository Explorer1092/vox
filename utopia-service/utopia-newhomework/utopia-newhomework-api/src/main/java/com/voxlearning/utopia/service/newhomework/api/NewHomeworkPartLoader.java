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

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.homework.api.mapper.WechatHomeworkMapper;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkFinishRewardInParentApp;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkStudyMaster;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineListenPaper;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author xuesong.zhang
 * @since 2016/8/3
 */
@ServiceVersion(version = "20171109")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface NewHomeworkPartLoader extends IPingable {

    @Idempotent
    @CacheMethod(type = NewHomeworkStudyMaster.class, writeCache = false)
    Map<String, NewHomeworkStudyMaster> getNewHomeworkStudyMasterMap(@CacheParameter(multiple = true) Collection<String> newHomeworkIds);

    @Idempotent
    List<WechatHomeworkMapper> getAllHomeworkMapper(List<NewHomework.Location> newHomeworkList, Long studentId);

    /**
     * 学生某次作业的基础应用练习的语音url
     *
     * @param newHomework 作业
     * @param userId      学生id
     * @return Map <String, List<String>>
     * 组装成lessonId-categoryId-categoryName为Key的Map
     */
    @Idempotent
    Map<String, List<String>> getBasicAppVoiceUrl(NewHomework newHomework, Long userId);

    @Idempotent
    @CacheMethod(type = NewHomeworkFinishRewardInParentApp.class, writeCache = false)
    NewHomeworkFinishRewardInParentApp getRewardInParentApp(@CacheParameter() Long userId);

    MapMessage updateTimeoutInteger(Long userId, String homeworkId);

    MapMessage updateBeforeReceivedInteger(Long userId, String homeworkId);

    /**
     * 获取学生某次作业的进度或者最新进度
     *
     * @param studentId  学生ID
     * @param subject    学科
     * @param homeworkId 作业ID
     */
    MapMessage getStudentHomeworkProgress(Long studentId, Subject subject, String homeworkId);

    @Idempotent
    List<NewHomework.Location> loadNewHomeworkByClazzGroupId(Collection<Long> groupIds, Date startDate, Date endDate);

    @Idempotent
    Map<String, OfflineListenPaper> findOfflineListenPaperByIds(Collection<String> ids);

    @Idempotent
    MapMessage getTeacherHomeworkProgress(Long teacherId, Subject subject);
}
