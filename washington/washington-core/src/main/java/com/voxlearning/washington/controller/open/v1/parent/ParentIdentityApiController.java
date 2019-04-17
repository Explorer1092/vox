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

package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.helpers.ValidateStudentIdHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;


/**
 * @author shiwei.liao
 * @since 1/7/2016
 */
@Controller
@Slf4j
@RequestMapping(value = "/v1/parent/identity")
public class ParentIdentityApiController extends AbstractParentApiController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private ValidateStudentIdHelper validateStudentIdHelper;
    /**
     * 获取家长身份列表（所有学生都是给爸爸、妈妈、爷爷、奶奶、姥姥、姥爷、其他这7种固定身份）
     *
     */
    @RequestMapping(value = "/identitylist.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage parentIdentityList() {
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            //todo: 有无session_key,校验方法不同
            if(!hasSessionKey()){
                validateRequestNoSessionKey(REQ_STUDENT_ID);
            }
            else {
                validateRequest(REQ_STUDENT_ID);
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return failMessage(e.getMessage());
            } else {
                return failMessage("系统错误，请重试");
            }
        }
        try {
            Long studentId = getRequestLong(REQ_STUDENT_ID);
            User student = raikouSystem.loadUser(studentId);
            if (student == null) {
                return failMessage("用户不存在");
            }
            List<Map<String, Object>> identityList = new ArrayList<>();
            for (CallName callName : CallName.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", callName.getKey());
                map.put(RES_CALL_NAME, StringUtils.replace(callName.name(),"它","他"));
                identityList.add(map);
            }
            MapMessage mapMessage = successMessage("获取家长身份列表成功");
            mapMessage.add(RES_REAL_NAME, student.fetchRealname());
            mapMessage.add(RES_IDENTITY_LIST, identityList);
            return mapMessage;
        } catch (Exception e) {
            return failMessage("获取家长身份列表失败");
        }
    }

    @RequestMapping(value = "/verifyidentity.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage getMobileAfterChooseIdentity() {

        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequired(REQ_CALL_NAME, "家长身份");
            //todo: 有无session_key,校验方法不同
            if(!hasSessionKey()){
                validateRequestNoSessionKey(REQ_STUDENT_ID, REQ_CALL_NAME);
            }
            else {
                validateRequest(REQ_STUDENT_ID, REQ_CALL_NAME);
            }

        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return failMessage(e.getMessage());
            } else {
                return failMessage("系统错误，请重试");
            }
        }

        try {
            Long studentId = getRequestLong(REQ_STUDENT_ID);
            Integer callNameId = getRequestInt(REQ_CALL_NAME);
            return getWantToBindParentIdentityMobileBySid(studentId, callNameId,"");
        } catch (Exception e) {
            log.error("bind mobile failure", e);
            return failMessage("验证选择身份失败");
        }
    }

    @RequestMapping(value = "bindcallname.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindCallName() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        Integer callNameId = getRequestInt(REQ_CALL_NAME);
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequired(REQ_CALL_NAME, "家长身份");
            validateRequest(REQ_STUDENT_ID, REQ_CALL_NAME);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return failMessage(e.getMessage());
            } else {
                return failMessage("系统错误，请重试");
            }
        }
        try {
            User parent = getCurrentParent();
            if (parent == null) {
                return failMessage("请登录家长号");
            }
            CallName callName = CallName.of(callNameId);
            if (callName == null) {
                return failMessage("家长身份错误，请重新选择身份");
            }
            //这个学生ID来源非常多。只能两种都校验一下。都失败才错误
            MapMessage parentIdMessage = validateStudentIdHelper.validateBindRequestStudentIdWithParentId(parent.getId(), studentId);
            String uuid = getRequestString(REQ_UUID);
            MapMessage uuidMessage = validateStudentIdHelper.validateBindRequestStudentIdWithUUID(uuid, studentId);
            if (!parentIdMessage.isSuccess() && !uuidMessage.isSuccess()) {
                //有些studentId不是通过绑定关系的关联接口获取的，没有调用之前的store方法
                //校验不通过再验一下是否已经存在parent-student关系
                List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);
                if (CollectionUtils.isEmpty(studentParents)) {
                    return failMessage("学生ID错误");
                }
                StudentParent studentParent = studentParents.stream().filter(p -> p.getParentUser() != null)
                        .filter(p -> Objects.equals(p.getParentUser().getId(), parent.getId()))
                        .filter(p -> StringUtils.isBlank(p.getCallName()))
                        .findFirst()
                        .orElse(null);
                if (studentParent == null) {
                    return failMessage("学生ID错误");
                }
            }
            MapMessage validateMessage = parentStudentCallNameHelper.validateStudentParentRef(studentId, parent, callName);
            if (!validateMessage.isSuccess()) {
                return failMessage(validateMessage.getInfo());
            }
            boolean keyParent = SafeConverter.toBoolean(validateMessage.get("keyParent"));
            boolean create = SafeConverter.toBoolean(validateMessage.get("create"));
            MapMessage message;
            if(create){
                message = parentServiceClient.bindExistingParent(studentId, parent.getId(), keyParent, callName.name());
            }else {
                message = parentServiceClient.setParentCallName(parent.getId(), studentId, callName);
            }
            if (!message.isSuccess()) {
                return failMessage("家长孩子关系绑定失败");
            }
            Map<Long, StudentDetail> studentUserMaps = studentLoaderClient.loadStudentDetails(Collections.singleton(studentId));
            List<Map<String, Object>> studentInfo = generateParentStudentInfo(parent, studentUserMaps, false);
            return successMessage().add(RES_CLAZZ_STUDENTS, studentInfo);
        } catch (Exception e) {
            String message;
            if (e instanceof UtopiaRuntimeException || e instanceof IllegalArgumentException) {
                message = e.getMessage();
            } else {
                log.error("verify select identity failure,[studentId:{}, callNameId:{}]",studentId,callNameId);
                message = "验证所选身份失败";
            }
            return failMessage(message);
        }

    }

    @RequestMapping(value = "bind_identity.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindIdentity() {
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequired(REQ_CALL_NAME, "家长身份");
            validateRequest(REQ_STUDENT_ID, REQ_CALL_NAME);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        Integer callNameId = getRequestInt(REQ_CALL_NAME);
        User parent = getCurrentParent();
        CallName callName = CallName.of(callNameId);
        if (callName == null) {
            return failMessage("家长身份错误，请重新选择身份");
        }
        //这个学生ID来源非常多。只能两种都校验一下。都失败才错误
        MapMessage parentIdMessage = validateStudentIdHelper.validateBindRequestStudentIdWithParentId(parent.getId(), studentId);
        String uuid = getRequestString(REQ_UUID);
        MapMessage uuidMessage = validateStudentIdHelper.validateBindRequestStudentIdWithUUID(uuid, studentId);
        if (!parentIdMessage.isSuccess() && !uuidMessage.isSuccess()) {
            //有些studentId不是通过绑定关系的关联接口获取的，没有调用之前的store方法
            //校验不通过再验一下是否已经存在parent-student关系
            List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);
            if (CollectionUtils.isEmpty(studentParents)) {
                return failMessage("学生ID错误");
            }
            StudentParent studentParent = studentParents.stream().filter(p -> p.getParentUser() != null)
                    .filter(p -> Objects.equals(p.getParentUser().getId(), parent.getId()))
                    .filter(p -> StringUtils.isBlank(p.getCallName()))
                    .findFirst()
                    .orElse(null);
            if (studentParent == null) {
                return failMessage("学生ID错误");
            }
        }
        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);
        boolean keyParent = studentParents.stream().noneMatch(StudentParent::isKeyParent);
        //没有家长直接绑定
        if (CollectionUtils.isEmpty(studentParents)) {
            MapMessage message = parentServiceClient.bindExistingParent(studentId, parent.getId(), keyParent, callName.name());
            if (message.isSuccess()) {
                //返回此次绑定的学生信息
                Map<Long, StudentDetail> studentUserMaps = studentLoaderClient.loadStudentDetails(Collections.singleton(studentId));
                List<Map<String, Object>> studentInfo = generateParentStudentInfo(parent, studentUserMaps, false);
                return successMessage().add(RES_CLAZZ_STUDENTS, studentInfo);
            }
            return failMessage(message.getInfo());
        }
        //当前登录家长跟孩子有关系没身份
        StudentParent studentParent = studentParents.stream().filter(p -> p.getParentUser() != null)
                .filter(p -> Objects.equals(p.getParentUser().getId(), parent.getId()))
                .findFirst()
                .orElse(null);
        if (studentParent != null) {
            if (StringUtils.isBlank(studentParent.getCallName())) {
                MapMessage message = parentServiceClient.setParentCallName(parent.getId(), studentId, callName);
                if (message.isSuccess()) {
                    //返回此次绑定的学生信息
                    Map<Long, StudentDetail> studentUserMaps = studentLoaderClient.loadStudentDetails(Collections.singleton(studentId));
                    List<Map<String, Object>> studentInfo = generateParentStudentInfo(parent, studentUserMaps, false);
                    return successMessage().add(RES_CLAZZ_STUDENTS, studentInfo);
                }
                return failMessage(message.getInfo());
            } else {
                return failMessage(RES_RESULT_PARENT_HAD_BEEN_OTHER_IDENTITY);
            }
        }
        //选择的身份可能有家长了。可能需要解绑
        StudentParent sameCallNameParent = studentParents.stream().filter(p -> callName.name().equals(p.getCallName())).findFirst().orElse(null);
        if (sameCallNameParent != null && sameCallNameParent.getParentUser() != null) {
            UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(sameCallNameParent.getParentUser().getId());
            if (userAuthentication == null || StringUtils.isBlank(userAuthentication.getSensitiveMobile())) {
                //已绑定的家长没有手机号，解除原来的。绑定新的家长
                StudentParentRef studentParentRef = studentLoaderClient.loadStudentParentRefs(studentId).stream().filter(p -> Objects.equals(p.getParentId(), sameCallNameParent.getParentUser().getId())).findFirst().orElse(null);
                parentServiceClient.disableStudentParentRef(studentParentRef);
            } else {
                String userMobileObscured = sensitiveUserDataServiceClient.loadUserMobileObscured(userAuthentication.getId());
                return failMessage(StringUtils.formatMessage(RES_RESULT_IDENTITY_EXIST_WITH_MOBILE_ERROR_MSG, userMobileObscured.substring(userMobileObscured.length() - 4)));
            }
        }
        //绑定新的关系
        MapMessage message = parentServiceClient.bindExistingParent(studentId, parent.getId(), keyParent, callName.name());
        if (!message.isSuccess()) {
            return failMessage(message.getInfo());
        }
        //返回此次绑定的学生信息
        Map<Long, StudentDetail> studentUserMaps = studentLoaderClient.loadStudentDetails(Collections.singleton(studentId));
        List<Map<String, Object>> studentInfo = generateParentStudentInfo(parent, studentUserMaps, false);
        return successMessage().add(RES_CLAZZ_STUDENTS, studentInfo);
    }
}