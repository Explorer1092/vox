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
import com.voxlearning.utopia.service.parent.homework.api.entity.Homework;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkPractice;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 作业查询接口，提供作业及详情的查询接口
 *
 * @author Wenlong Meng
 * @version 20181107
 * @date 2018-11-07
 */
@ServiceVersion(version = "20181111")
@ServiceTimeout(timeout = 3, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 1)
public interface HomeworkLoader {

    /**
     * 根据id查询作业信息
     *
     * @param id 作业id
     * @return 作业信息
     */
    @Idempotent
    default Homework loadHomework(String id) {
        return loadHomeworks(Collections.singleton(id)).get(id);
    }

    /**
     * 根据id批量查询作业信息
     *
     * @param ids 作业id集合
     * @return 作业id - 作业信息
     */
    @Idempotent
    Map<String, Homework> loadHomeworks(Collection<String> ids);

    /**
     * 根据学生id查询作业信息
     *
     * @param userId
     * @return
     */
    @Idempotent
    List<Homework> loadHomeworkByUserId(Long userId);

    /**
     * 根据id查询作业详情
     *
     * @param id 作业id
     * @return 作业信息
     */
    @Idempotent
    default HomeworkPractice loadHomeworkPractice(String id) {
        return loadHomeworkPractices(Collections.singleton(id)).get(id);
    }

    /**
     * 根据id批量查询作业详情
     *
     * @param ids 作业id集合
     * @return 作业id - 作业详情
     */
    @Idempotent
    Map<String, HomeworkPractice> loadHomeworkPractices(Collection<String> ids);

}
