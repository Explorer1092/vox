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

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.vendor.api.entity.JxtUserShare;
import com.voxlearning.utopia.service.vendor.consumer.JxtLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.JxtServiceClient;
import com.voxlearning.washington.controller.open.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * 家长作业分享
 * Created by Shuai Huan on 2016/4/21.
 */
@Controller
@RequestMapping(value = "/parentMobile/share")
@Slf4j
public class MobileParentShareController extends AbstractMobileParentController {
    @Inject
    private JxtLoaderClient jxtLoaderClient;
    @Inject
    private JxtServiceClient jxtServiceClient;

    @RequestMapping(value = "/save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveUserShare() {
        Long studentId = getRequestLong("sid");
        String shareContent = getRequestString("user_share_content");
        String imgUrlStr = getRequestString("user_share_img");

        if (StringUtils.isBlank(shareContent) && StringUtils.isBlank(imgUrlStr)) {
            return MapMessage.errorMessage("分享内容不能为空");
        }
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (!studentIsParentChildren(parent.getId(), studentId)) {
            return MapMessage.errorMessage("此学生与当前家长无关联");
        }
        //把图片地址处理成相对地址
        List<String> imgList = new ArrayList<>();
        if (StringUtils.isNotBlank(imgUrlStr)) {
            JsonUtils.fromJsonToList(imgUrlStr, String.class).stream().filter(StringUtils::isNotBlank).forEach(p -> imgList.add(p.substring(OSS_IMAGE_HOST.length())));
        }
        StudentParentRef studentParentRef = studentLoaderClient.loadStudentParentRefs(studentId).stream().filter(p -> p.getParentId().equals(parent.getId())).findFirst().orElse(null);
        Student student = studentLoaderClient.loadStudent(studentId);
        String userName = student.fetchRealname() + (CallName.其它监护人.name().equals(studentParentRef.getCallName()) ? "家长" : studentParentRef.getCallName());

        JxtUserShare share = new JxtUserShare();
        share.setUserId(parent.getId());
        share.setUserName(userName);
        share.setShareType(JxtUserShare.SHARE_TYPE);
        share.setContent(shareContent);
        share.setImgList(imgList);
        MapMessage message = jxtServiceClient.saveUserShare(share);
        if (message.isSuccess()) {
            return message;
        } else {
            return MapMessage.errorMessage(message.getInfo());
        }
    }

}
