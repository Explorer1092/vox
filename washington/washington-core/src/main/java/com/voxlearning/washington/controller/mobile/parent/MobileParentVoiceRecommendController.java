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

package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.open.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;

/**
 * @author shiwe.liao
 * @since 2016-6-1
 */
@Controller
@Slf4j
@RequestMapping(value = "/parentMobile/voice/")
public class MobileParentVoiceRecommendController extends AbstractMobileParentController {

    @Inject private RaikouSystem raikouSystem;

    @RequestMapping(value = "/invite_teacher_recommend.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage inviteTeacherRecommend() {
        String homeworkId = getRequestString("homeworkId");
        Long studentId = getRequestLong("sid");
        User parent = currentParent();
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业Id不能为空");
        }
        if (parent == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        //作业不存在
        if (newHomework == null) {
            return MapMessage.errorMessage("请求的作业不存在").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        User student = raikouSystem.loadUser(studentId);
        //学生不存在
        if (student == null) {
            return MapMessage.errorMessage("学生不存在").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);
        StudentParent studentParent;
        //学生无家长
        if (CollectionUtils.isEmpty(studentParents)) {
            return MapMessage.errorMessage("请求的学生与当前家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        studentParent = studentParents.stream().filter(p -> p.getParentUser().getId().equals(parent.getId())).findFirst().orElse(null);
        //学生跟当前家长无关联
        if (studentParent == null) {
            return MapMessage.errorMessage("请求的学生与当前家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        String parentName;
        if (StringUtils.isBlank(studentParent.getCallName()) || CallName.其它监护人.name().equals(studentParent.getCallName())) {
            parentName = student.fetchRealname() + "家长";
        } else {
            parentName = student.fetchRealname() + studentParent.getCallName();
        }
        return newHomeworkServiceClient.addVoiceRecommendRequestParent(homeworkId, parent.getId(), parentName);
    }
}
