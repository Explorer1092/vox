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

package com.voxlearning.washington.controller.open;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.piclisten.client.TextBookManagementLoaderClient;
import com.voxlearning.washington.service.parent.ParentSelfStudyPublicHelper;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;

import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_USER_TYPE_ERROR_MSG;

/**
 * Abstract controller class for Student Open Api
 * Created by Shuai Huan on 2015/05/08.
 */
public class AbstractStudentApiController extends AbstractApiController {

    @Inject
    protected TextBookManagementLoaderClient textBookManagementLoaderClient;

    @Inject protected MessageCommandServiceClient messageCommandServiceClient;

    @Inject
    protected ParentSelfStudyPublicHelper parentSelfStudyPublicHelper; //实在不想引入,么得办法,谁让学生端也有随身听。。。

    public void validateRequest(String... paramKeys) {
        super.validateRequest(paramKeys);

        User curUser = getApiRequestUser();
        // 验证用户身份
        if (curUser.fetchUserType() != UserType.STUDENT) {
            throw new IllegalArgumentException(RES_RESULT_USER_TYPE_ERROR_MSG);
        }
    }

    protected Student getCurrentStudent() {
        User curUser = getApiRequestUser();
        if (curUser == null) return null;
        Student student;
        if (curUser instanceof Student) {
            student = (Student) curUser;
        } else {
            student = studentLoaderClient.loadStudent(curUser.getId());
        }
        return student;
    }

    protected StudentDetail getCurrentStudentDetail() {
        User curUser = getApiRequestUser();
        if (curUser == null) return null;
        StudentDetail student;
        if (curUser instanceof StudentDetail) {
            student = (StudentDetail) curUser;
        } else {
            student = studentLoaderClient.loadStudentDetail(curUser.getId());
        }
        return student;
    }

    protected String generateBigVersion(String ver, StudentDetail studentDetail) {
        if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "AppH5Release", "testlist")) {
            return "V0_0_0";
        }
        return super.generateBigVersion(ver);
    }

    // 获取网络环境和超时时间的mapping
    protected Map getTimeoutParam() {
        String regStr = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", "h5_timeout_param");
        regStr = regStr.replace("\r", "").replace("\n", "").replace("\t", "");
        Map<String, Object> regMap = JsonUtils.fromJson(regStr);
        if (regMap == null) {
            return Collections.emptyMap();
        }
        return regMap;
    }

    // 根据年级获取学生的语音打分系数
    protected String getVoiceRatio(ClazzLevel clazzLevel) {
        if (clazzLevel == null) {
            return "1";
        }
        String regStr = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", "voice_ratio");
        regStr = regStr.replace("\r", "").replace("\n", "").replace("\t", "");
        Map<String, Object> regMap = JsonUtils.fromJson(regStr);
        if (regMap == null) {
            return "1";
        }
        return (String) regMap.get(String.valueOf(clazzLevel.getLevel()));
    }


}
