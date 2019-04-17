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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkCacheServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/2/2
 */
@Named
public class CH_CacheCheckHomeworkIntegralDetail extends SpringContainerSupport implements CheckHomeworkTask {
    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;

    @Override
    public void execute(CheckHomeworkContext context) {
        // 记录CheckHomeworkIntegralDetail，现在只要是检查作业就记录次数了~~
        newHomeworkCacheService.getCheckHomeworkIntegralCacheManager().record(context.getTeacherId(),
                context.getGroupId(), context.getHomeworkType(), context.getDetail());
    }
}
