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

package com.voxlearning.washington.controller.reward;

import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaffDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.washington.support.AbstractController;

import javax.inject.Inject;

/**
 * Abstract reward controller implementation.
 *
 * @author Xiaohai Zhang
 * @since Dec 5, 2014
 */
abstract public class AbstractRewardController extends AbstractController {
    @Inject protected GrayFunctionManagerClient grayFunctionManagerClient;
    /**
     * 为奖品中心获取当前的用户。会自动根据用户类型获取其相应的扩展。
     * 如果是老师帐户返回TeacherDetail
     * 如果是学生帐户返回StudentDetail
     * 如果是教研员帐户返回ResearchStaffDetail
     * TODO: 这个方法可以继续向基类移动，不过需要修改方法名
     */
    protected User currentRewardUser() {
        User user = currentUser();
        if (user == null) {
            return null;
        }
        switch (user.fetchUserType()) {
            case TEACHER: {
                if (user instanceof TeacherDetail) {
                    return user;
                }
                return currentTeacherDetail();
            }
            case STUDENT: {
                if (user instanceof StudentDetail) {
                    return user;
                }
                return currentStudentDetail();
            }
            case RESEARCH_STAFF: {
                if (user instanceof ResearchStaffDetail) {
                    return user;
                }
                return currentResearchStaffDetail();
            }
            default: {
                return user;
            }
        }
    }
}
