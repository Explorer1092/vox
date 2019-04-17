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
import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkComment;
import com.voxlearning.utopia.service.newhomework.api.entity.UnreadHomeworkComment;
import com.voxlearning.utopia.service.newhomework.api.mapper.ExHomeworkComment;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Homework comment loader abstraction.
 *
 * @author Xiaohai Zhang
 * @since Oct 19, 2015
 */
@ServiceVersion(version = "20170607")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface HomeworkCommentLoader extends IPingable {

    @Idempotent
    long studentUnreadHomeworkCommentCount(Long studentId);

    @Idempotent
    List<ExHomeworkComment> studentUnreadHomeworkComments(StudentDetail student);

    @Idempotent
    List<UnreadHomeworkComment> __studentUnreadHomeworkComments(Long studentId);

    @Idempotent
    @CacheMethod(type = HomeworkComment.class, writeCache = false)
    Map<Long, HomeworkComment> __loadHomeworkCommentsIncludeDisabled(@CacheParameter(multiple = true) Collection<Long> ids);

    @Idempotent
    @CacheMethod(type = HomeworkComment.class, writeCache = false)
    Map<String, Set<HomeworkComment.Location>> __queryHomeworkCommentLocations(@CacheParameter(value = "H", multiple = true) Collection<String> homeworkIds);

}
