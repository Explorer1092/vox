package com.voxlearning.washington.helpers;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwei.liao
 * @since 2018-4-12
 */
@Named
public class ParentStudentCallNameHelper {
    @Inject
    private ParentLoaderClient parentLoaderClient;
    @Inject
    private UserLoaderClient userLoaderClient;

    public MapMessage validateStudentParentRef(Long studentId, User parent, CallName callName) {
        if (callName == null) {
            return MapMessage.errorMessage(RES_RESULT_CALLNAME_ERROR_MSG);
        }
        if (studentId == null) {
            return MapMessage.errorMessage(RES_RESULT_STUDENT_ID_ERROR_MSG);
        }
        if (parent == null) {
            return MapMessage.errorMessage(RES_RESULT_PARENT_ERROR_MSG);
        }
        boolean keyParent = true;
        MapMessage message = MapMessage.successMessage().add("create", true);
        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);
        if (CollectionUtils.isNotEmpty(studentParents)) {
            //当前要添加的学生已有请求身份的家长
            if (studentParents.stream().anyMatch(p -> callName.name().equals(p.getCallName()))) {
                return MapMessage.errorMessage(RES_RESULT_IDENTITY_EXIST_ERROR_MSG);
            }
            //当前学生已经与当前家长关联
            if (studentParents.stream().anyMatch(p -> parent.getId().equals(p.getParentUser().getId()) && StringUtils.isNotBlank(p.getCallName()))) {
                return MapMessage.errorMessage(RES_RESULT_PARENT_HAD_BEEN_OTHER_IDENTITY);
            }
            //判断应该是创建还是更新身份
            if (studentParents.stream().anyMatch(p -> parent.getId().equals(p.getParentUser().getId()) && StringUtils.isBlank(p.getCallName()))) {
                message.set("create", false);
            }
            //该学生是否有关键家长
            //判断是否应该创建为关键家长
            if (studentParents.stream().anyMatch(StudentParent::isKeyParent)) {
                keyParent = false;
            }

        }

        //判断该家长是否有效
        if (keyParent) {
            UserAuthentication parentUserAuthentication = userLoaderClient.loadUserAuthentication(parent.getId());
            if (parentUserAuthentication == null || !parentUserAuthentication.isMobileAuthenticated()) {
                keyParent = false;
            }
        }
        MapMessage mapMessage = validateParentWithCallName(parent.getId(), callName);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        return message.add("keyParent", keyParent);
    }

    public MapMessage validateParentWithCallName(Long parentId, CallName callName) {
        if (parentId == null) {
            return MapMessage.errorMessage(RES_RESULT_PARENT_ERROR_MSG);
        }
        if (callName == null) {
            return MapMessage.errorMessage(RES_RESULT_CALLNAME_ERROR_MSG);
        }
        List<StudentParentRef> parentStudentRefs = parentLoaderClient.loadParentStudentRefs(parentId);
        //请求的身份与该家长已存在的StudentParentRef中的身份性别必须相同
        if (CollectionUtils.isNotEmpty(parentStudentRefs)) {
            if (parentStudentRefs.stream()
                    .filter(p -> CallName.of(p.getCallName()) != null)
                    .anyMatch(p -> CallName.isGenderDiff(callName, CallName.of(p.getCallName())))) {
                return MapMessage.errorMessage(RES_RESULT_EXIST_IDENTITY_NOT_MATCH);
            }
        }
        //请求的身份与该家长已存在的StudentParentRef中的身份年龄层必须相同
        if (CollectionUtils.isNotEmpty(parentStudentRefs)) {
            if (parentStudentRefs.stream().filter(p -> CallName.of(p.getCallName()) != null).anyMatch(p -> !CallName.isSameAge(CallName.of(p.getCallName()), callName))) {
                return MapMessage.errorMessage(RES_RESULT_EXIST_IDENTITY_AGE_NOT_MATCH);
            }
        }
        return MapMessage.successMessage();
    }
}
