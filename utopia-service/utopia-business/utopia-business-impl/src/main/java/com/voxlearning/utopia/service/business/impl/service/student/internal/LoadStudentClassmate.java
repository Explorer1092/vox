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

import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 加载关联的同学（包含自己）
 *
 * @author Xiaohai Zhang
 * @since Oct 12, 2015
 */
@Named
public class LoadStudentClassmate extends AbstractStudentIndexDataLoader {

    @Inject private RaikouSDK raikouSDK;

    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;

    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {
        Clazz clazz = context.getStudent().getClazz();

        Set<Long> userIds;
        if (!clazz.isSystemClazz()) {
            userIds = new HashSet<>(asyncGroupServiceClient.getAsyncGroupService()
                    .findStudentIdsByClazzId(clazz.getId()));
        } else {
            userIds = raikouSDK.getClazzClient()
                    .getGroupStudentTupleServiceClient()
                    .findByGroupIds(context.__groupIds)
                    .stream()
                    .map(GroupStudentTuple::getStudentId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        context.__studentIds = userIds;

        return context;
    }
}
