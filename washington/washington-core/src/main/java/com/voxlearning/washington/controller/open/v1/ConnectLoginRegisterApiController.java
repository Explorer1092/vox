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

package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.user.api.CrmSummaryService;
import com.voxlearning.utopia.service.user.api.ThirdPartyService;
import com.voxlearning.utopia.service.user.api.constants.FindPasswordMethod;
import com.voxlearning.utopia.service.user.api.constants.UserRecordMode;
import com.voxlearning.utopia.service.user.api.entities.LandingSource;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.washington.controller.open.AbstractApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * 第三方登录模式的Login/Register Controller Class
 *
 * @author Zhilong Hu
 * @since 2014-10-27
 */
@Controller
@RequestMapping(value = "/v1/connect/user")
@Slf4j
public class ConnectLoginRegisterApiController extends AbstractApiController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = ThirdPartyService.class) private ThirdPartyService thirdPartyService;
    @ImportService(interfaceClass = CrmSummaryService.class) private CrmSummaryService crmSummaryService;

    // ======================================================================================
    // 注册流程限制只能口袋学社调用，并且只能注册为学生
    // 注册后默认可以使用
    // 注册内容：
    //     身份：3（学生）
    //     用户名：用户输入的唯一用户名
    //     密码：用户密码
    // ======================================================================================
    @RequestMapping(value = "/register.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage registerUser() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequiredNumber(REQ_USER_TYPE, "用户类型");
            validateEnum(REQ_USER_TYPE, "用户类型", "1", "2", "3");
            validateRequired(REQ_USER_CODE, "用户名");
            validateRequired(REQ_PASSWD, "密码");
            validateRequestNoSessionKey(REQ_USER_TYPE, REQ_USER_CODE, REQ_PASSWD, REQ_NICK_NAME, REQ_AVATAR_DAT);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // 只允许口袋学社调用
        VendorApps apps = getApiRequestApp();
        String appKey = apps.getAppKey();
        if (!"StudyCraft".equals(appKey)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_NO_ACCESS_RIGHT_MSG);
            return resultMap;
        }

        // 判断用户名是否已经存在
        String userName = getRequestString(REQ_USER_CODE);
        LandingSource userInfo = thirdPartyLoaderClient.loadLandingSource(appKey, userName);
        if (userInfo != null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_DUPLICATE_USERNAME);
            return resultMap;
        }

        // 生成新用户
        NeonatalUser neonatalUser = new NeonatalUser();
        neonatalUser.setRoleType(RoleType.of(getRequestInt(REQ_USER_TYPE)));
        neonatalUser.setUserType(UserType.of(getRequestInt(REQ_USER_TYPE)));
        if (StringUtils.isEmpty(getRequestString(REQ_NICK_NAME))) {
            neonatalUser.setNickName(getRequestString(REQ_USER_CODE));
        } else {
            neonatalUser.setNickName(getRequestString(REQ_NICK_NAME));
        }
        neonatalUser.setPassword(getRequestString(REQ_PASSWD));
        neonatalUser.setWebSource(getRequestString(REQ_APP_KEY));

        MapMessage message = userServiceClient.registerUserAndSendMessage(neonatalUser);
        if (!message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, message.getInfo());
            return resultMap;
        }

        User user = (User) message.get("user");
        Long userId = user.getId();

        String avatarDat = getRequestString(REQ_AVATAR_DAT);
        if (!StringUtils.isEmpty(avatarDat)) {
            MapMessage changeResult = updateUserAvatar(user, avatarDat);
            if (!changeResult.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_UPDATE_AVATAR_ERROR_MSG + changeResult.getInfo());
                return resultMap;
            }
        }

//        userServiceClient.createUserRecord(userId, getWebRequestContext().getRealRemoteAddress(), OperationSourceType.app);
//        userServiceClient.createUserRecordDaily(userId, getWebRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN);
        asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(userId,
                getWebRequestContext().getRealRemoteAddress(),
                UserRecordMode.LOGIN,
                OperationSourceType.app,
                false,
                getAppType());

        // 绑定用户
        String sourceUserName = getRequestString(REQ_NICK_NAME);
        thirdPartyService.persistLandingSource(appKey, userName, sourceUserName, userId);

        // 返回值
        String sessionKey = attachUser2RequestApp(userId);

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_USER_ID, userId);
        resultMap.add(RES_SESSION_KEY, sessionKey);
        return resultMap;
    }

    // ======================================================================================
    // 第三方账号用户登录
    // ======================================================================================
    @RequestMapping(value = "/login.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage login() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_USER_CODE, "用户名");
            validateRequired(REQ_PASSWD, "密码");
            validateRequestNoSessionKey(REQ_USER_CODE, REQ_PASSWD);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        VendorApps apps = getApiRequestApp();
        String appKey = apps.getAppKey();

        if (!"StudyCraft".equals(appKey)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_NO_ACCESS_RIGHT_MSG);
            return resultMap;
        }

        // 判断一下对方的IP是否在IP白名单里面
        if (RuntimeMode.isStaging() || RuntimeMode.isProduction()) {
            String appClientIp = getWebRequestContext().getRealRemoteAddress();
            if (apps.getServerIps() == null || !apps.getServerIps().contains(appClientIp)) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_NO_ACCESS_RIGHT_MSG);
                return resultMap;
            }
        }

        // 根据用户输入的用户名查询Landing_Source是否存在
        String sourceUid = getRequestString(REQ_USER_CODE);
        LandingSource userInfo = thirdPartyLoaderClient.loadLandingSource(appKey, sourceUid);
        if (userInfo == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOW_USER_ACCOUNT_MSG);
            return resultMap;
        }

        // 验证用户名和密码
        // 口袋学社由于要支持第三方登录和手机号码登录，密码问题无法统一，取消密码验证
        // 改为IP地址白名单处理
        Long userId = userInfo.getUserId();
        // String userPassword = getRequestString(REQ_PASSWD);
        User loginUser = raikouSystem.loadUser(userId);

//        if (!loginUser.fetchUserPassword().match(userPassword)) {
//            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOW_USER_ACCOUNT_MSG);
//            // 登录失败时记录信息
//            userServiceClient.recordUserLoginFailure(userId);
//            return  resultMap;
//        }

//        userServiceClient.createUserRecord(userId, getWebRequestContext().getRealRemoteAddress(), OperationSourceType.app);
//        userServiceClient.createUserRecordDaily(userId, getWebRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN);
        asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(userId,
                getWebRequestContext().getRealRemoteAddress(),
                UserRecordMode.LOGIN,
                OperationSourceType.app,
                false,
                getAppType());

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        List<Map<String, Object>> retUserList = new ArrayList<>();
        Map<String, Object> userItem = new HashMap<>();
        userItem.put(RES_USER_TYPE, loginUser.getUserType());
        userItem.put(RES_USER_ID, loginUser.getId());
        String sessionKey = attachUser2RequestApp(loginUser.getId());
        userItem.put(RES_SESSION_KEY, sessionKey);
        retUserList.add(userItem);
        resultMap.add(RES_USER_LIST, retUserList);

        return resultMap;
    }

    @RequestMapping(value = "/password/update.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage updateUserPassword() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_PASSWD, "旧密码");
            validateRequired(REQ_NEW_PASSWD, "新密码");
            validateRequest(REQ_PASSWD, REQ_NEW_PASSWD);
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // 获取用户信息
        User curUser = getApiRequestUser();
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(curUser.getId());
        if (!ua.fetchUserPassword().match(getRequestString(REQ_PASSWD))) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOW_USER_ACCOUNT_MSG);
            return resultMap;
        }

        // 变更用户密码
        if (userServiceClient.setPassword(curUser, getRequestString(REQ_NEW_PASSWD)).isSuccess()) {
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(curUser.getId());
            userServiceRecord.setOperatorId(curUser.getId().toString());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent("修改密码");
            userServiceRecord.setComments(FindPasswordMethod.MODIFY_PASSWORD.getDescription());
            userServiceRecord.setAdditions("refer:ConnectLoginRegisterApiController.updateUserPassword");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/mobile/update.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage updateUserMobile() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_USER_CODE, "用户手机号");
            validateRequest(REQ_USER_CODE);
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // 只允许口袋学社调用
        VendorApps apps = getApiRequestApp();
        String appKey = apps.getAppKey();
        if (!"StudyCraft".equals(appKey)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_NO_ACCESS_RIGHT_MSG);
            return resultMap;
        }

        // 验证手机号码唯一性
        String userCode = getRequestString(REQ_USER_CODE);
        List<User> registeredUserList = userLoaderClient.loadUsers(userCode, UserType.STUDENT);
        if (registeredUserList != null && registeredUserList.size() > 0) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_MOBILE_EXIST_MSG);
            return resultMap;
        }
        // 这里如果只验证学生身份，可能会有安全性问题，老师也验一下，家长就算了
        registeredUserList = userLoaderClient.loadUsers(userCode, UserType.TEACHER);
        if (registeredUserList != null && registeredUserList.size() > 0) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_MOBILE_EXIST_MSG);
            return resultMap;
        }

        try {
            User curUser = getApiRequestUser();
            MapMessage result = userServiceClient.activateUserMobile(curUser.getId(), userCode);
            if (!result.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_BIND_MOBILE_ERROR_MSG + result.getInfo());
                return resultMap;
            }

            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        } catch (Exception e) {
            logger.error("Error happened while bind user mobile!", e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_BIND_MOBILE_ERROR_MSG);
            return resultMap;
        }

    }
}
