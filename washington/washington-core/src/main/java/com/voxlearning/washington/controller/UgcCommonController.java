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

package com.voxlearning.washington.controller;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.entity.misc.UgcAnswers;
import com.voxlearning.utopia.mapper.UgcRecordMapper;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * Created by Summer Yang on 2016/6/13.
 * UGC 收集活动统一入口  目前支持的有： 学生PC，老师PC，老师微信
 */
@Controller
@RequestMapping("/ugc")
public class UgcCommonController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;

    /**
     * 新版UGC 获取用户当前有效的UGC收集活动
     */
    @RequestMapping(value = "loadugc.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadUgc() {
        User user = currentUser();
        if (user == null || user.getCreateTime().after(DayRange.current().getStartDate())) {
            return MapMessage.errorMessage();
        }
        Long recordId = getRequestLong("recordId");
        UgcRecordMapper mapper;
        if (recordId != 0) {
            mapper = miscLoaderClient.loadEnableUserUgcRecordByRecordId(user, recordId);
        } else {
            mapper = miscLoaderClient.loadEnableUserUgcRecord(user);
        }
        if (mapper == null) {
            return MapMessage.errorMessage();
        }
        return MapMessage.successMessage().add("ugc", mapper);
    }


    /**
     * 新版UGC 用户上传答题结果
     */
    @RequestMapping(value = "saveugcanswer.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveUgcAnswer(@RequestBody Map body) {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage();
        }
        Long recordId = SafeConverter.toLong(body.get("recordId"));
        String source = SafeConverter.toString(body.get("source"));
        Long userId = SafeConverter.toLong(body.get("userId"));
        if (userId != 0L) {
            // 获取新的User
            user = raikouSystem.loadUser(userId);
        }
        //noinspection unchecked
        List<Map<String, Object>> answerMapList = (List<Map<String, Object>>) body.get("answerMapList");
        try {
            UgcAnswers.Source ugcSource = UgcAnswers.Source.valueOf(source);
            return atomicLockManager.wrapAtomic(miscServiceClient)
                    .keys(user.getId())
                    .proxy()
                    .saveUgcAnswer(user, recordId, answerMapList, ugcSource);
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    /**
     * 新版UGC 获取暑假老师收集  家长APP端
     */
    @RequestMapping(value = "loadugcforteacherconfirm.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadUgcForTeacherConfirm() {
        User user = currentUser();
        if (user == null || user.getCreateTime().after(DayRange.current().getStartDate()) || user.fetchUserType() != UserType.PARENT) {
            return MapMessage.errorMessage();
        }
        Long recordId = getRequestLong("recordId");
        // 从家长的孩子随机取一个
        List<User> userList = studentLoaderClient.loadParentStudents(user.getId());
        UgcRecordMapper mapper = null;
        for (User students : userList) {
            mapper = miscLoaderClient.loadEnableUserUgcRecordByRecordId(students, recordId);
            if (mapper != null) {
                break;
            }
        }
        if (mapper == null) {
            return MapMessage.errorMessage();
        }
        return MapMessage.successMessage().add("ugc", mapper);
    }
}
