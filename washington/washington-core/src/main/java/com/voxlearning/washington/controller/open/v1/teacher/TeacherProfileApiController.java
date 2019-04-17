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

package com.voxlearning.washington.controller.open.v1.teacher;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Gender;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.RealnameRule;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.TeacherAgentLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.LogisticType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Set;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.*;

/**
 * Created by Alex on 15-1-9.
 */
@Controller
@RequestMapping(value = "/v1/teacher")
@Slf4j
public class TeacherProfileApiController extends AbstractTeacherApiController {

    @Inject private TeacherAgentLoaderClient teacherAgentLoaderClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    private static final String REQ_TEACHING_YEARS = "teaching_years";

    @RequestMapping(value = "/profile.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @Deprecated
    @ResponseBody
    public MapMessage getProfile() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        User curUser = getApiRequestUser();
        resultMap.add(RES_USER_ID, curUser.getId());
        resultMap.add(RES_USER_TYPE, curUser.getUserType());
        resultMap.add(RES_REAL_NAME, curUser.getProfile().getRealname());
        resultMap.add(RES_AVATAR_URL, getUserAvatarImgUrl(curUser));

        resultMap.add(RES_AUTH_STATE, curUser.fetchCertificationState() == AuthenticationState.SUCCESS);
        // 获取用户认证信息

        String am = sensitiveUserDataServiceClient.loadUserMobileObscured(curUser.getId());
        if (!StringUtils.isEmpty(am)) {
            resultMap.add(RES_USER_MOBILE, am);
        }

        String ae = sensitiveUserDataServiceClient.loadUserEmailObscured(curUser.getId());
        if (!StringUtils.isEmpty(ae)) {
            resultMap.add(RES_USER_EMAIL, ae);
        }

        // 性别
        if (Gender.FEMALE == curUser.fetchGender() || Gender.MALE == curUser.fetchGender()) {
            resultMap.add(RES_USER_GENDER, curUser.fetchGender().getCode());
        }

        // 生日
        if (StringUtils.isNoneBlank(curUser.fetchBirthday())) {
            resultMap.add(RES_USER_BIRTHDAY, curUser.fetchBirthday());
        }
        // 职务
        TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(curUser.getId());
        if (extAttribute != null && StringUtils.isNoneBlank(extAttribute.getDuty())) {
            resultMap.add(RES_TEACHER_DUTY, extAttribute.getDuty());
        }

        // 获取用户的SHIPPING ADDRESS
        try {
            MapMessage message = userServiceClient.generateUserShippingAddress(curUser.getId());
            if (message.isSuccess()) {
                UserShippingAddress address = (UserShippingAddress) message.get("address");
                addIntoMap(resultMap, RES_SCHOOL_ID, address.getSchoolId());
                addIntoMap(resultMap, RES_SCHOOL_NAME, address.getSchoolName());
                addIntoMap(resultMap, RES_PROVINCE_CODE, address.getProvinceCode());
                addIntoMap(resultMap, RES_PROVINCE_NAME, address.getProvinceName());
                addIntoMap(resultMap, RES_CITY_CODE, address.getCityCode());
                addIntoMap(resultMap, RES_CITY_NAME, address.getCityName());
                addIntoMap(resultMap, RES_COUNTRY_CODE, address.getCountyCode());
                addIntoMap(resultMap, RES_COUNTRY_NAME, address.getCountyName());
                addIntoMap(resultMap, RES_DETAIL_ADDRESS, address.getDetailAddress());
                addIntoMap(resultMap, RES_RECEIVER_PHONE, sensitiveUserDataServiceClient.loadUserShippingAddressReceiverPhoneObscured(address.getId()));
                addIntoMap(resultMap, RES_RECEIVER, address.getReceiver());
                addIntoMap(resultMap, RES_POST_CODE, address.getPostCode());
                addIntoMap(resultMap, RES_SHIPPING_TYPE, address.getLogisticType());
            }
        } catch (Exception e) {
            // ignore
        }

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(curUser.getId());
        addIntoMap(resultMap, RES_INTEGRAL, teacherDetail.getUserIntegral().getUsable());
        addIntoMap(resultMap, RES_SUBJECT, teacherDetail.getSubject());

        // 班级数量
        Set<Clazz> teacherClazzs = loadTeacherClazzIncludeMainSub(curUser.getId());
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(teacherDetail.getTeacherSchoolId()).getUninterruptibly();
        EduSystemType eduSystem = EduSystemType.of(schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly());

        teacherClazzs = teacherClazzs.stream()
                .filter(p -> p.getClazzLevel() != ClazzLevel.PRIMARY_GRADUATED && p.getClazzLevel() != ClazzLevel.MIDDLE_GRADUATED)
                .filter(p -> p.matchEduSystem(eduSystem))
                .collect(Collectors.toSet());

        addIntoMap(resultMap, RES_RESULT_CLAZZ_COUNT, teacherClazzs.size());

        // 学校所在区域
        if (teacherDetail.getRegionCode() != null) {
            addIntoMap(resultMap, RES_SCHOOL_COUNTRY_CODE, teacherDetail.getRegionCode());
        }

        // 一起作业为您服务
        addIntoMap(resultMap, RES_TEACHER_AGENT, teacherAgentLoaderClient.getSchoolManager(teacherDetail.getTeacherSchoolId()));

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }


    /**
     * 更新老师收货地址.
     *
     * @return
     */
    @RequestMapping(value = "/shippingaddress/update.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage updateShippingAddress() {
        // Feature #54929
//        if (ForbidModifyNameAndPortrait.check()) {
//            return ForbidModifyNameAndPortrait.errorMessage;
//        }
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_PROVINCE_CODE, "省编码");
            validateRequired(REQ_PROVINCE_NAME, "省名称");
            validateRequired(REQ_CITY_CODE, "市编码");
            validateRequired(REQ_CITY_NAME, "市名称");
            validateRequired(REQ_COUNTY_CODE, "区编码");
            validateRequired(REQ_COUNTY_NAME, "区名称");
            validateRequired(REQ_DETAIL_ADDRESS, "详细地址");
            validateRequired(REQ_POST_CODE, "邮政编码");
            validateRequiredLength(REQ_TEACHER_NAME, 2, 10, "老师姓名");
            validateRequired(REQ_SHIPPING_TYPE, "配送方式");
            validateEnum(REQ_SHIPPING_TYPE, "配送方式", LogisticType.express.name(), LogisticType.ems.name());

            // 处理版本信息，1.6.8之前没有收货人和收货人电话
            String ver = getRequestString(REQ_APP_NATIVE_VERSION);
            if(VersionUtil.compareVersion(ver, "1.6.8") < 0){
                validateRequest(REQ_PROVINCE_CODE, REQ_PROVINCE_NAME,
                        REQ_CITY_CODE, REQ_CITY_NAME, REQ_COUNTY_CODE,
                        REQ_COUNTY_NAME, REQ_DETAIL_ADDRESS, REQ_POST_CODE,
                        REQ_TEACHER_NAME, REQ_SHIPPING_TYPE,REQ_DETAIL_ADDRESS);
            }else{
                validateRequired(REQ_RECEIVER, "收货人");
                validateRequired(REQ_RECEIVER_PHONE, "收货电话");

                // 收货人手机号包含*时候认为用户没有变动收货人电话信息
                if (!getRequestString(REQ_RECEIVER_PHONE).contains("*")) {
                    validateMobileNumber(REQ_RECEIVER_PHONE);// 校验收货电话
                }

                validateRequest(REQ_PROVINCE_CODE, REQ_PROVINCE_NAME,
                        REQ_CITY_CODE, REQ_CITY_NAME, REQ_COUNTY_CODE,
                        REQ_COUNTY_NAME, REQ_DETAIL_ADDRESS, REQ_POST_CODE,
                        REQ_TEACHER_NAME, REQ_SHIPPING_TYPE,REQ_DETAIL_ADDRESS,
                        REQ_RECEIVER,REQ_RECEIVER_PHONE);
            }
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Long provinceCode = getRequestLong(REQ_PROVINCE_CODE);
        String provinceName = getRequestString(REQ_PROVINCE_NAME);
        Long cityCode = getRequestLong(REQ_CITY_CODE);
        String cityName = getRequestString(REQ_CITY_NAME);
        Long countyCode = getRequestLong(REQ_COUNTY_CODE);
        String countyName = getRequestString(REQ_COUNTY_NAME);
        String detailAddress = getRequestString(REQ_DETAIL_ADDRESS);
        String postCode = getRequestString(REQ_POST_CODE);
        String teacherName = getRequestString(REQ_TEACHER_NAME);
        LogisticType shippingType = LogisticType.valueOf(getRequestString(REQ_SHIPPING_TYPE));
        User curUser = getApiRequestUser();
        String receiver = getRequestString(REQ_RECEIVER);
        String receiverPhone = getRequestString(REQ_RECEIVER_PHONE);

        // 更新用户的SHIPPING ADDRESS
        MapMessage message = userServiceClient.generateUserShippingAddress(curUser.getId());
        UserShippingAddress usa = (UserShippingAddress) message.get("address");
        usa.setProvinceCode(provinceCode);
        usa.setProvinceName(provinceName);
        usa.setCityCode(cityCode);
        usa.setCityName(cityName);
        usa.setCountyCode(countyCode);
        usa.setCountyName(countyName);
        usa.setDetailAddress(detailAddress);
        usa.setPostCode(postCode);
        usa.setLogisticType(shippingType);
        usa.setReceiver(receiver);

        if(!receiverPhone.contains("*")) {
            usa.setReceiverPhone(receiverPhone);
        }
        
        MapMessage mapMessage = userServiceClient.updateUserShippingAddress(usa);
        if (mapMessage.isSuccess()) {
            mapMessage = userServiceClient.changeName(curUser.getId(), teacherName);
            if (!mapMessage.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, mapMessage.getInfo());
            } else {
                LogCollector.info("backend-general", MiscUtils.map("usertoken", curUser.getId(),
                        "usertype", curUser.getUserType(),
                        "platform", getApiRequestApp().getAppKey(),
                        "version", getRequestString(REQ_APP_NATIVE_VERSION),
                        "op", "change user name",
                        "mod1", curUser.fetchRealname(),
                        "mod2", teacherName,
                        "mod3", curUser.getAuthenticationState()));
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            }
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, mapMessage.getInfo());
        }
        return resultMap;
    }


    @Deprecated
    @RequestMapping(value = "/shippingaddress/get.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getTeacherShippingAddress() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SHIPPING_TYPE, "配送方式");
            validateRequest(REQ_SHIPPING_TYPE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String shippingType = getRequestString(REQ_SHIPPING_TYPE);
        User curUser = getApiRequestUser();

        // 更新用户的SHIPPING TYPE
        MapMessage message = userServiceClient.generateUserShippingAddress(curUser.getId());
        UserShippingAddress usa = (UserShippingAddress) message.get("address");
        usa.setLogisticType(LogisticType.valueOf(shippingType));

        MapMessage mapMessage = userServiceClient.updateUserShippingAddress(usa);
        if (mapMessage.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, mapMessage.getInfo());
        }
        return resultMap;
    }

    /**
     * 更新老师姓名
     *
     * @return
     */
    @RequestMapping(value = "/name/update.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage updateTeacherName() {
        // Feature #54929
//        if (ForbidModifyNameAndPortrait.check()) {
//            return ForbidModifyNameAndPortrait.errorMessage;
//        }
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_TEACHER_NAME, "老师姓名");
            validateRequest(REQ_TEACHER_NAME);
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String teacherNewName = getRequestString(REQ_TEACHER_NAME);
        if (badWordCheckerClient.containsUserNameBadWord(teacherNewName)) {
            return failMessage("输入的姓名信息不合适哦\n有疑问请联系客服：\n400-160-1717");
        }

        try {
            RealnameRule.validateRealname(teacherNewName);
        } catch (Exception ex) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_TEACHER_NAME_ERROR);
            return resultMap;
        }
        User teacher = getCurrentTeacher();
        MapMessage mapMessage = userServiceClient.changeName(teacher.getId(), teacherNewName);
        if (!mapMessage.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_CHANGE_NAME_ERROR);
            return resultMap;
        }

        LogCollector.info("backend-general", MiscUtils.map("usertoken", teacher.getId(),
                "usertype", teacher.getUserType(),
                "platform", getApiRequestApp().getAppKey(),
                "version", getRequestString(REQ_APP_NATIVE_VERSION),
                "op", "change user name",
                "mod1", teacher.fetchRealname(),
                "mod2", teacherNewName,
                "mod3", teacher.getAuthenticationState()));

        return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
    }

    /**
     * 更新用户头像
     *
     * @return
     */

    @RequestMapping(value = "/avatar/update.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage updateAvatar() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_AVATAR_DAT, "头像");
            validateRequest(REQ_AVATAR_DAT);
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, "头像更新服务升级中,敬请期待!");

//        String avatarDat = getRequestString(REQ_AVATAR_DAT);
//        Teacher curUser = getCurrentTeacher();
//        if (!StringUtils.isEmpty(avatarDat)) {
//            // Feature #54929
//            if (ForbidModifyNameAndPortrait.check()) {
//                return ForbidModifyNameAndPortrait.errorMessage;
//            }
//            MapMessage changeResult = updateUserAvatar(curUser, avatarDat);
//            if (!changeResult.isSuccess()) {
//                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
//                resultMap.add(RES_MESSAGE, RES_RESULT_UPDATE_AVATAR_ERROR_MSG + changeResult.getInfo());
//                return resultMap;
//            } else {
//                // update cache info to reload user avatar
//                clearApiRequestUser();
//                resultMap.add(RES_AVATAR_URL, getUserAvatarImgUrl(getApiRequestUser()));
//                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//            }
//        }
        return resultMap;
    }

    // 更新老师职务
    @RequestMapping(value = "/duty/update.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage updateTeacherDuty() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_TEACHER_DUTY, "老师职务");
            validateRequest(REQ_TEACHER_DUTY);
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher curUser = getCurrentTeacher();
        String teacherNewDuty = getRequestString(REQ_TEACHER_DUTY);
        MapMessage mapMessage = teacherServiceClient.updateTeacherDuty(curUser.getId(), teacherNewDuty);
        if (!mapMessage.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, mapMessage.getInfo());
            return resultMap;
        }

        LogCollector.info("backend-general", MiscUtils.map("usertoken", curUser.getId(),
                "usertype", curUser.getUserType(),
                "platform", getApiRequestApp().getAppKey(),
                "version", getRequestString(REQ_APP_NATIVE_VERSION),
                "op", "change teacher duty",
                "mod1", curUser.fetchRealname(),
                "mod2", teacherNewDuty,
                "mod3", curUser.getAuthenticationState()));

        return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
    }

    // 更新老师教龄
    @RequestMapping(value = "/teachingYears/update.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage updateTeachingYears() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_TEACHING_YEARS, "老师教龄");
            validateEnum(REQ_TEACHING_YEARS, "老师教龄", "1", "2", "3", "4", "5");
            validateRequest(REQ_TEACHING_YEARS);
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher curUser = getCurrentTeacher();
        Integer teachingYears = getRequestInt(REQ_TEACHING_YEARS);
        MapMessage mapMessage = teacherServiceClient.updateTeachingYears(curUser.getId(), teachingYears);
        if (!mapMessage.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, mapMessage.getInfo());
            return resultMap;
        }
        LogCollector.info("backend-general", MiscUtils.map("usertoken", curUser.getId(),
                "usertype", curUser.getUserType(),
                "platform", getApiRequestApp().getAppKey(),
                "version", getRequestString(REQ_APP_NATIVE_VERSION),
                "op", "change teacher teachingYears",
                "mod1", curUser.fetchRealname(),
                "mod2", teachingYears,
                "mod3", curUser.getAuthenticationState()));
        return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
    }
}