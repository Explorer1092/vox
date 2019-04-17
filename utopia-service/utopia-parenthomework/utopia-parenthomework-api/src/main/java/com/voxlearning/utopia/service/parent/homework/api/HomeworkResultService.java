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
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 作业结果修改接口,提供作业结果及详情的修改接口
 *
 * @author Wenlong Meng
 * @version 20181107
 * @date 2018-11-07
 */
@ServiceVersion(version = "20181111")
public interface HomeworkResultService {

    /**
     * 保存作业结果
     *
     * @param homeworkResult 作业结果
     * @return
     */
    MapMessage saveHomeworkResult(HomeworkResult homeworkResult);

    /**
     * 保存作业结果详情
     *
     * @param homeworkProcessResults 作业结果详情
     * @return
     */
    MapMessage saveHomeworkProcessResult(List<HomeworkProcessResult> homeworkProcessResults);

}
