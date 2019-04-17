/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.feedback.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.entity.misc.UserAppeal;
import com.voxlearning.utopia.service.feedback.api.AppealService;
import com.voxlearning.utopia.service.feedback.impl.dao.UserAppealPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

@Named("com.voxlearning.utopia.service.feedback.impl.service.AppealServiceImpl")
@ExposeService(interfaceClass = AppealService.class)
public class AppealServiceImpl extends SpringContainerSupport implements AppealService {

    @Inject private UserAppealPersistence userAppealPersistence;

    @Override
    public AlpsFuture<MapMessage> saveUserAppeal(UserAppeal appeal) {
        // 校验一下用户申诉历史
        List<UserAppeal> userAppeals = userAppealPersistence.loadByUserId(appeal.getUserId());
        if (CollectionUtils.isNotEmpty(userAppeals)) {
            userAppeals = userAppeals.stream().filter(u -> u.getStatus() == UserAppeal.Status.WAIT).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(userAppeals)) {
                return new ValueWrapperFuture<>(MapMessage.errorMessage("您还有未处理的申诉，请耐心等待"));
            }
        }
        userAppealPersistence.insert(appeal);
        return new ValueWrapperFuture<>(MapMessage.successMessage());
    }
}
