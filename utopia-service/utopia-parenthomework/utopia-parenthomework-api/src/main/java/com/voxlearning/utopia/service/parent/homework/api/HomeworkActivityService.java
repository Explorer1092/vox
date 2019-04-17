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
import com.voxlearning.utopia.service.parent.homework.api.entity.Activity;

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
public interface HomeworkActivityService {

    /**
     * 新建或修改活动
     *
     * @param activity 活动
     * @return 1 -- 成功；-1 - 失败
     */
    int save(Activity activity);

    /**
     * 查询活动信息
     *
     * @param id 活动id
     * @return
     */
    Activity load(String id);

}
