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

package com.voxlearning.utopia.service.business.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.SmartClazzRewardItem;
import com.voxlearning.utopia.business.api.BusinessClazzIntegralService;
import com.voxlearning.utopia.service.business.impl.service.teacher.TeacherSmartClazzServiceImpl;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Created by Summer Yang on 2016/1/8.
 */
@Named
@Service(interfaceClass = BusinessClazzIntegralService.class)
@ExposeService(interfaceClass = BusinessClazzIntegralService.class)
public class BusinessClazzIntegralServiceImpl extends BusinessServiceSpringBean implements BusinessClazzIntegralService {

    @Inject
    private TeacherSmartClazzServiceImpl teacherSmartClazzService;

    public MapMessage rewardSmartClazzStudent(TeacherDetail teacherDetail, Clazz clazz, List<User> userList,
                                              int rewardIntegralCnt, SmartClazzRewardItem item,
                                              String customContent) {
        return teacherSmartClazzService.rewardSmartClazzStudent(teacherDetail, clazz, userList, rewardIntegralCnt, item, customContent);
    }
}
