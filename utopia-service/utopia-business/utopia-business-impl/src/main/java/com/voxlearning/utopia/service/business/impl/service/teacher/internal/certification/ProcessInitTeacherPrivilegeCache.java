/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.service.teacher.internal.certification;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * 初始化认证特权的缓存
 *
 * @author RuiBao
 * @version 0.1
 * @since 7/17/2015
 */
@Named
public class ProcessInitTeacherPrivilegeCache extends AbstractTcpProcessor {

    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;

    @Override
    protected TeacherCertificationContext doProcess(TeacherCertificationContext context) {
        if (context.isRewardSkipped()) return context;

        Teacher teacher = context.getUser();
        asyncUserCacheServiceClient.getAsyncUserCacheService()
                .TeacherPrivilegeCardCacheManager_init(teacher.getId(), teacher.getSubject())
                .awaitUninterruptibly();

        // 副账号也需要认证特权处理
        if (CollectionUtils.isNotEmpty(context.getSubTeacherIds())) {
            Map<Long, Teacher> teachers = teacherLoaderClient.loadTeachers(context.getSubTeacherIds());
            teachers.forEach((k, v) -> asyncUserCacheServiceClient.getAsyncUserCacheService()
                    .TeacherPrivilegeCardCacheManager_init(v.getId(), v.getSubject())
                    .awaitUninterruptibly());
        }

        return context;
    }
}
