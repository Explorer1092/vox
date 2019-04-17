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

package com.voxlearning.washington.controller.open.timeep;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cipher.AesUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.service.user.api.entities.LandingSource;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.UserSecurity;
import com.voxlearning.utopia.service.user.api.mappers.timeep.TimeepStudentMapper;
import com.voxlearning.utopia.service.user.api.mappers.timeep.TimeepTeacherMapper;
import com.voxlearning.utopia.service.user.api.service.timeep.TimeepService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * 时代e博对接接口
 * 引入老师学生
 *
 * @author changyuan
 * @since 2016/3/14
 */
@Controller
@RequestMapping(value = "/open_external/timeep")
public class TimeepUserController extends AbstractTimeepController {

    private final static String TIMEEP_WEB_SOURCE = "timeep";

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = TimeepService.class) private TimeepService timeepService;

    @RequestMapping(value = "/importteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage importTeachers(@RequestBody String json) {
        if (!apiValidation()) {
            return generateErrorMsg(ErrorCode.VALIDATION_FAILED);
        }

        // 蛋疼啊，他们传来的json前面乱码，然后还找不到原因。。。
        json = json.substring(json.indexOf('['));
        List<TimeepTeacherMapper> list = JsonUtils.fromJsonToList(json, TimeepTeacherMapper.class);
        if (CollectionUtils.isEmpty(list)) {
            return generateErrorMsg(ErrorCode.WRONG_PARAM);
        }

        return timeepService.importTeachers(list);
    }

    @RequestMapping(value = "/importstudent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage importStudents(@RequestBody String json) {
        if (!apiValidation()) {
            return MapMessage.errorMessage().add("message", "validation failed");
        }

        // 蛋疼啊，他们传来的json前面乱码，然后还找不到原因。。。
        json = json.substring(json.indexOf('['));
        List<TimeepStudentMapper> list = JsonUtils.fromJsonToList(json, TimeepStudentMapper.class);
        if (CollectionUtils.isEmpty(list)) {
            return generateErrorMsg(ErrorCode.WRONG_PARAM);
        }

        return timeepService.importStudents(list);
    }

    @RequestMapping(value = "/checklogin.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkLogin(@RequestBody String json) {
        if (!apiValidation()) {
            return MapMessage.errorMessage().add("message", "validation failed");
        }

        // 蛋疼啊，他们传来的json前面乱码，然后还找不到原因。。。
        json = json.substring(json.indexOf('{'));
        Map<String, Object> data = JsonUtils.fromJson(json);
        String userId = SafeConverter.toString(data.get("u"));
        String pw = SafeConverter.toString(data.get("p"));

        List<LandingSource> landingSources = thirdPartyLoaderClient.loadLandingSource(SafeConverter.toLong(userId), TIMEEP_WEB_SOURCE);
        if (CollectionUtils.isEmpty(landingSources)) {
            return MapMessage.errorMessage();
        }

        String key = CommonConfiguration.getInstance().getTimeepPwSecretKey();
        try {
            String password = AesUtils.decryptBase64String(key, pw);

            List<UserSecurity> userSecurities = userLoaderClient.loadUserSecurities(userId, UserType.STUDENT);
            if (CollectionUtils.isEmpty(userSecurities)) {
                return MapMessage.errorMessage();
            }

            if (userSecurities.get(0).match(password)) {
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage();
            }
        } catch (Exception e) {
            logger.error("Exception happened for timeep check login: userId {}: ", userId, e);
            return MapMessage.errorMessage("Internal error");
        }
    }

    @RequestMapping(value = "/resetpwd.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetPwd(@RequestBody String json) {
        if (!apiValidation()) {
            return MapMessage.errorMessage().add("message", "validation failed");
        }

        // 蛋疼啊，他们传来的json前面乱码，然后还找不到原因。。。
        json = json.substring(json.indexOf('{'));
        Map<String, Object> data = JsonUtils.fromJson(json);
        String userId = SafeConverter.toString(data.get("u"));
        String pw = SafeConverter.toString(data.get("p"));

        List<LandingSource> landingSources = thirdPartyLoaderClient.loadLandingSource(SafeConverter.toLong(userId), TIMEEP_WEB_SOURCE);
        if (CollectionUtils.isEmpty(landingSources)) {
            return MapMessage.errorMessage();
        }

        String key = CommonConfiguration.getInstance().getTimeepPwSecretKey();

        try {
            String password = AesUtils.decryptBase64String(key, pw);
            User user = raikouSystem.loadUser(SafeConverter.toLong(userId));
            return userServiceClient.setPassword(user, password);
        } catch (Exception e) {
            logger.error("Exception happened for reset pwd: userId {}: ", userId, e);
            return MapMessage.errorMessage("Internal error");
        }
    }
}
