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

package com.voxlearning.washington.controller.open.v2.parent;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

@Controller
@Slf4j
@RequestMapping(value = "/v2/parent/")
public class ParentSszApiController extends AbstractParentApiController {

    /**
     * 极算子家长绑定学生发送验证码
     *
     * @return
     */
    @RequestMapping(value = "/verifystudentbind.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage verifyStudentBind() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_USER_MOBILE, "学生手机号");
            validateRequest(REQ_USER_MOBILE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        User currentParent = getCurrentParent();
        if (currentParent == null) {
            return failMessage(RES_RESULT_RELOGIN);
        }

        String mobile = getRequestString(REQ_USER_MOBILE);
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.STUDENT);
        if (userAuthentication == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "未查找到学生");
            return resultMap;
        }
        Student student = studentLoaderClient.loadStudent(userAuthentication.getId());

        MapMessage mapMessage = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(mobile, SmsType.APP_PARENT_BIND_STUDENT.name(), false);
        return convert2NativeMessage(mapMessage).add(RES_STUDENT_NAME, student.fetchRealname());
    }

    /**
     * 极算子家长绑定学生
     *
     * @return
     */
    @RequestMapping(value = "/bindstudent.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage bindStudent() {
        //入参 学生手机号 验证码 是否是主要家长，家长称呼
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_USER_MOBILE, "学生手机号");
            validateRequired(REQ_VERIFY_CODE, "验证码");
            validateRequired(REQ_PARENT_KEY_PARENT, "是否是主要家长");
            validateRequired(REQ_PARENT_CALL_NAME, "家长称呼");
            validateRequest(REQ_USER_MOBILE, REQ_VERIFY_CODE, REQ_PARENT_KEY_PARENT, REQ_PARENT_CALL_NAME);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Long currentParentId = getCurrentParentId();
        if (currentParentId == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, RES_RESULT_RELOGIN);
        }


        String mobile = getRequestString(REQ_USER_MOBILE);
        String verifyCode = getRequestString(REQ_VERIFY_CODE);
        String callName = getRequestString(REQ_PARENT_CALL_NAME);
        boolean keyParent = getRequestBool(REQ_PARENT_KEY_PARENT);

        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.STUDENT);
        if (userAuthentication == null) {
            return failMessage("未查找到学生");
        }

        MapMessage mapMessage = smsServiceClient.getSmsService().verifyValidateCode(mobile, verifyCode, SmsType.APP_PARENT_BIND_STUDENT.name());
        if (mapMessage.isSuccess()) {
            return convert2NativeMessage(parentServiceClient.bindExistingParent(userAuthentication.getId(), currentParentId, keyParent, callName));
        } else {
            return convert2NativeMessage(mapMessage);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/disableStudentRef.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public MapMessage disableStudentParentRef() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        User currentParent = getCurrentParent();
        if (currentParent == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, RES_RESULT_RELOGIN);
        }
        long studentId = getRequestLong(REQ_STUDENT_ID);

        List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId).stream().filter(i -> !i.isDisabledTrue()).collect(Collectors.toList());
        for (StudentParentRef ref : studentParentRefs) {
            if (ref.getParentId().equals(currentParent.getId())) {
                parentServiceClient.disableStudentParentRef(ref);

                // 记录 UserServiceRecord
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(ref.getStudentId());
                userServiceRecord.setOperatorId(currentParent.getId() + "");
                userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
                userServiceRecord.setOperationContent("解除学生与家长的关联");
                userServiceRecord.setComments("学生:" + ref.getStudentId() + ", 家长:" + ref.getParentId());
                userServiceClient.saveUserServiceRecord(userServiceRecord);
            }
        }
        return convert2NativeMessage(MapMessage.successMessage());
    }

    @ResponseBody
    @RequestMapping(value = "/getbindstudents.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public MapMessage getStudents() {
        Long currentParentId = getCurrentParentId();
        if (currentParentId == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, RES_RESULT_RELOGIN);
        }

        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(Collections.singleton(currentParentId)).get(currentParentId);
        if (CollectionUtils.isEmpty(studentParentRefs)) {
            return MapMessage.errorMessage("没有绑定孩子");
        }
        Map<Long, StudentParentRef> sidAndRef = studentParentRefs.stream().collect(Collectors.toMap(StudentParentRef::getStudentId, Function.identity()));
        List<Long> studentIds = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toList());
        Map<Long, Student> student = studentLoaderClient.loadStudents(studentIds);

        List<Map> data = student.values().stream().map(item -> {
            Map map = new HashMap();
            StudentParentRef studentParentRef = sidAndRef.get(item.getId());
            map.put(RES_STUDENT_ID, item.getId());
            map.put(RES_STUDENT_NAME, item.fetchRealname());
            map.put(RES_CALL_NAME, studentParentRef != null ? studentParentRef.getCallName() : "");
            return map;
        }).collect(Collectors.toList());

        MapMessage message = new MapMessage();
        message.add(RES_RESULT, RES_RESULT_SUCCESS);
        message.add("data", data);
        return message;
    }

}