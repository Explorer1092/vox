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

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.newhomework.api.OfflineHomeworkLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineHomework;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.vendor.api.entity.OfflineHomeworkSignRecord;
import com.voxlearning.utopia.service.vendor.consumer.JxtServiceClient;
import com.voxlearning.washington.controller.open.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwe.liao
 * @since 2016-9-13
 */
@Controller
@Slf4j
@RequestMapping(value = "/parentMobile/offlineHomework")
public class MobileParentOfflineHomeworkController extends AbstractMobileParentController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = OfflineHomeworkLoader.class)
    private OfflineHomeworkLoader offlineHomeworkLoader;
    @Inject
    private JxtServiceClient jxtServiceClient;


    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getOfflineHomeworkDetail() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        String offlineHomeworkId = getRequestString("offlineHomeworkId");
        Long studentId = getRequestLong("sid");
        if (StringUtils.isBlank(offlineHomeworkId)) {
            return MapMessage.errorMessage("练习ID不能为空").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        OfflineHomework offlineHomework = offlineHomeworkLoader.loadOfflineHomework(offlineHomeworkId);
        if (offlineHomework == null) {
            return MapMessage.errorMessage("您要查看的练习单不存在").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        GroupMapper groupMapper = deprecatedGroupLoaderClient.loadGroup(offlineHomework.getClazzGroupId(), true);
        if (groupMapper == null) {
            return MapMessage.errorMessage("您要查看的练习单班级已不存在").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        //作业单内容部分==全部由guoqiang.li那边维护
        MapMessage mapMessage = offlineHomeworkLoader.loadOfflineHomeworkDetail(offlineHomeworkId);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        //处理语音签字部分
        Map<String, List<OfflineHomeworkSignRecord>> offlineHomeworkSignMap = jxtLoaderClient.getSignRecordByOfflineHomeworkIds(Collections.singleton(offlineHomeworkId));
        List<OfflineHomeworkSignRecord> homeworkSignRecords = offlineHomeworkSignMap.containsKey(offlineHomeworkId) ? offlineHomeworkSignMap.get(offlineHomeworkId) : new ArrayList<>();
        //语音签字模块的文案
        List<String> signRecordTextList = new ArrayList<>();
        //是否已签字
        boolean hadSign = true;
        //学生名称
        String studentName = "";
        int studentCount = 0;
        if (offlineHomework.getNeedSign()) {
            if (CollectionUtils.isEmpty(homeworkSignRecords) || !homeworkSignRecords.stream().anyMatch(p -> Objects.equals(p.getParentId(), user.getId()) && Objects.equals(p.getStudentId(), studentId))) {
                signRecordTextList.add("您还未语音确认");
                hadSign = false;
            }
            if (CollectionUtils.isNotEmpty(homeworkSignRecords)) {
                studentCount = homeworkSignRecords.stream().map(OfflineHomeworkSignRecord::getStudentId).collect(Collectors.toSet()).size();
            }
            String totalText = studentCount + "/" + groupMapper.getStudents().size() + "位学生家长已语音确认";
            signRecordTextList.add(totalText);
            User student = raikouSystem.loadUser(studentId);
            studentName = student == null ? "" : student.fetchRealname();
        }
        mapMessage.add("signRecordTextList", signRecordTextList).add("hadSign", hadSign).add("studentName", studentName);
        return mapMessage;
    }

    @RequestMapping(value = "sign.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage voiceSignOfflineHomework() {
        Long studentId = getRequestLong("sid");
        String offlineHomeworkId = getRequestString("offlineHomeworkId");
        String voiceUrl = getRequestString("voice");
        User user = currentUser();
        //是否登录
        if (user == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        //语音不能为空
        if (StringUtils.isBlank(voiceUrl)) {
            return MapMessage.errorMessage("语音不能为空").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        //作业单id不能为空
        if (StringUtils.isBlank(offlineHomeworkId)) {
            return MapMessage.errorMessage("练习ID不能为空").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        OfflineHomework offlineHomework = offlineHomeworkLoader.loadOfflineHomework(offlineHomeworkId);
        if (offlineHomework == null) {
            return MapMessage.errorMessage("您要查看的练习单不存在").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        //必须是自己的孩子
        if (!studentIsParentChildren(user.getId(), studentId)) {
            return MapMessage.errorMessage(RES_RESULT_STUDENT_NOT_RELATION_TO_PARENT).setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }
        String lock = "parent_sign_offline_homework_" + offlineHomeworkId + "_" + user.getId().toString() + "_" + studentId.toString();
        try {
            AtomicLockManager.instance().acquireLock(lock);
            //是否已签字过
            Map<String, List<OfflineHomeworkSignRecord>> offlineHomeworkSignMap = jxtLoaderClient.getSignRecordByOfflineHomeworkIds(Collections.singleton(offlineHomeworkId));
            List<OfflineHomeworkSignRecord> homeworkSignRecords = offlineHomeworkSignMap.containsKey(offlineHomeworkId) ? offlineHomeworkSignMap.get(offlineHomeworkId) : new ArrayList<>();
            if (CollectionUtils.isNotEmpty(homeworkSignRecords) && homeworkSignRecords.stream().anyMatch(p -> Objects.equals(p.getParentId(), user.getId()) && Objects.equals(p.getStudentId(), studentId))) {
                return MapMessage.errorMessage("您已经确认过了").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }
            OfflineHomeworkSignRecord homeworkSignRecord = new OfflineHomeworkSignRecord();
            homeworkSignRecord.setOfflineHomeworkId(offlineHomeworkId);
            homeworkSignRecord.setParentId(user.getId());
            homeworkSignRecord.setStudentId(studentId);
            homeworkSignRecord.setVoiceUrl(voiceUrl);
            return jxtServiceClient.saveOfflineHomeworkSignRecord(homeworkSignRecord);
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("请不要重复确认").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        } finally {
            AtomicLockManager.instance().releaseLock(lock);
        }
    }
}
