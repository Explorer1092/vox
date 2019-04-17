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

package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.vendor.api.entity.JxtFeedBack;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNotice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_BAD_REQUEST_CODE;
import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_NEED_RELOGIN_CODE;

/**
 * @author shiwe.liao
 * @since 2016/4/26
 */
@Controller
@Slf4j
@RequestMapping(value = "/parentMobile/jxt/notice/")
public class MobileParentJxtNoticeController extends AbstractMobileParentController {

    @RequestMapping(value = "/feed_back.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage parentFeedBack() {
        String noticeId = getRequestString("notice_id");
        Long groupId = getRequestLong("group_id");
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        JxtNotice notice = jxtLoaderClient.getJxtNoticeById(noticeId);
        if (StringUtils.isBlank(noticeId) || notice == null) {
            return MapMessage.errorMessage("您要反馈的通知不存在").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }
        if (!notice.getGroupIds().contains(groupId)) {
            return MapMessage.errorMessage("通知{}并不包含组{}", noticeId, groupId).setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }
        List<JxtFeedBack> feedBackList = jxtLoaderClient.getFeedBackListByNoticeIds(Collections.singleton(noticeId)).get(noticeId);
        if(CollectionUtils.isNotEmpty(feedBackList) && feedBackList.stream().anyMatch(p->p.getParentId().equals(parent.getId()))){
            return MapMessage.errorMessage("您已经查看过该通知了");
        }
        GroupMapper groupMapper = deprecatedGroupLoaderClient.loadGroup(groupId, true);
        if(groupMapper == null){
            return MapMessage.errorMessage("当前班级已不存在,请重试").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }

        //统计确认收到的列表是孩子的维度。所以一个家长确认的时候得把这个家长在这个groupId内的孩子都给存一条记录
        Map<Long, StudentParentRef> studentParentRefMap = parentLoaderClient.loadParentStudentRefs(parent.getId()).stream().collect(Collectors.toMap(StudentParentRef::getStudentId, t -> t));
        Set<GroupMapper.GroupUser> parentStudentInThisGroup = groupMapper.getStudents().stream().filter(p -> studentParentRefMap.keySet().contains(p.getId())).collect(Collectors.toSet());
        if(CollectionUtils.isEmpty(parentStudentInThisGroup)){
            return MapMessage.errorMessage("该通知已失效").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }
        for(GroupMapper.GroupUser groupUser:parentStudentInThisGroup){
            StudentParentRef studentParentRef = studentParentRefMap.get(groupUser.getId());
            JxtFeedBack feedBack = new JxtFeedBack();
            feedBack.setNoticeId(noticeId);
            feedBack.setStudentId(groupUser.getId());
            feedBack.setGroupId(groupId);
            feedBack.setStudentName(groupUser.getName());
            feedBack.setParentId(parent.getId());
            feedBack.setParentName(groupUser.getName() + (CallName.of(studentParentRef.getCallName()) == CallName.其它监护人 ? "家长" : studentParentRef.getCallName()));
            jxtServiceClient.saveFeedBack(feedBack);
        }
        return MapMessage.successMessage();
    }
}
