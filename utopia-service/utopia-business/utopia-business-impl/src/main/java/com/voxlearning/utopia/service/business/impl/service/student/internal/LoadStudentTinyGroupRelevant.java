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

package com.voxlearning.utopia.service.business.impl.service.student.internal;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.clazz.client.AsyncTinyGroupServiceClient;
import com.voxlearning.utopia.service.user.api.entities.TinyGroup;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.Subject.*;

/**
 * @author RuiBao
 * @version 0.1
 * @since 7/30/2015
 */
@Named
public class LoadStudentTinyGroupRelevant extends AbstractStudentIndexDataLoader {

    @Inject private AsyncTinyGroupServiceClient asyncTinyGroupServiceClient;
    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;

    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {
        StudentDetail student = context.getStudent();

        // 是否显示英语小助手任命书
        context.getParam().put("englishtgl", asyncUserCacheServiceClient.getAsyncUserCacheService()
                .TinyGroupLeaderCardCacheManager_show(student.getId(), ENGLISH).getUninterruptibly());
        // 是否显示数学小助手任命书
        context.getParam().put("mathtgl", asyncUserCacheServiceClient.getAsyncUserCacheService()
                .TinyGroupLeaderCardCacheManager_show(student.getId(), MATH).getUninterruptibly());
        // 是否显示语文小助手任命书
        context.getParam().put("chinesetgl", asyncUserCacheServiceClient.getAsyncUserCacheService()
                .TinyGroupLeaderCardCacheManager_show(student.getId(), CHINESE).getUninterruptibly());

        if (CollectionUtils.isNotEmpty(context.__studentGroups)) {
            Set<Subject> subjects = asyncTinyGroupServiceClient.getAsyncTinyGroupService()
                    .loadStudentTinyGroups(student.getId())
                    .getUninterruptibly().stream().map(TinyGroup::getSubject).collect(Collectors.toSet());
            for (GroupMapper group : context.__studentGroups) {
                if (!subjects.contains(group.getSubject())) {
                    context.getParam().put("addtg", true);
                    break;
                }
            }
        }
        return context;
    }
}
