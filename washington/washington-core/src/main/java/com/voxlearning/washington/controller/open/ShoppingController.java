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

package com.voxlearning.washington.controller.open;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.integral.client.IntegralLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.AsyncStudentServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/*
public final class StatusCode {

    // public static int SUCCESS = 200;

    public static String ERROR_CODE_20201 = "20201";
    public static String ERROR_CONTENT_20201 = "登录用户未找到。";
    public static String ERROR_CODE_20202 = "20202";
    public static String ERROR_CONTENT_20202 = "定单号为空。";
    public static String ERROR_CODE_20203 = "20203";
    public static String ERROR_CONTENT_20203 = "上传头像时异常。";
    public static String ERROR_CODE_20204 = "20204";
    public static String ERROR_CONTENT_20204 = "图片流有问题。";
    public static String ERROR_CODE_20205 = "20205";
    public static String ERROR_CONTENT_20205 = "金币不足。";
    public static String ERROR_CODE_20206 = "20206";
    public static String ERROR_CONTENT_20206 = "银币不足。";


}
*/

@SuppressWarnings("SpringJavaAutowiringInspection")
@Controller
@RequestMapping(value = "/open")
@Slf4j
public class ShoppingController extends AbstractOpenController {

    @Inject private RaikouSystem raikouSystem;

    @Inject private AsyncStudentServiceClient asyncStudentServiceClient;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;

    @Inject private IntegralLoaderClient integralLoaderClient;

    /**
     * Object转换成Long
     *
     * @param obj
     * @return
     */
    public static Long toLong(Object obj) {
        if (obj == null || "".equals(obj) || false == StringUtils.isNumeric(obj + ""))
            return 0L;
        return Long.valueOf(obj + "");
    }

    /**
     * Object转换成Integer
     *
     * @param obj
     * @return
     */
    public static Integer toInteger(Object obj) {
        if (obj == null || "".equals(obj) || false == obj.toString().matches("-*\\d+\\.?\\d*"))
            return 0;
        return Integer.valueOf(obj + "");
    }

    /**
     * 用户详细信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "userInfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext open(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long uid = toLong(openAuthContext.getParams().get("uid"));
        User user = raikouSystem.loadUser(uid);

        if (null == user) {
            openAuthContext.setCode("20201");
            openAuthContext.setError("登录学号未找到。");
            return openAuthContext;
        }

        String userRole = "";

        Set<RoleType> roleTypes = userLoaderClient.loadUserRoles(user);
        List<String> roleNames = new ArrayList<>();
        for (RoleType roleType : roleTypes) {
            roleNames.add(roleType.name());
        }
        for (String roleName : roleNames) {
            userRole += roleName + ",";
        }
        userRole = userRole.substring(0, userRole.lastIndexOf(","));
        Map<String, Object> dataMap = new HashMap<>();
        if (user.isStudent()) {
            dataMap = getUserInfoByUserIdForOpen(uid);
        } else if (user.isTeacher() || user.isResearchStaff()) {
            dataMap = getUserInfoByTeacherIdForOpen(user);
        }

        openAuthContext.add("uid", user.getId());
        openAuthContext.add("username", user.getProfile().getRealname());
        openAuthContext.add("userType", user.getUserType());
        openAuthContext.add("userRole", userRole);
        openAuthContext.add("imgUrl", user.fetchImageUrl());
        openAuthContext.add("phone", sensitiveUserDataServiceClient.loadUserMobileObscured(user.getId()));
        openAuthContext.add("isVip", 0);
        openAuthContext.add("isShowVipInfo", 0);
        // 学生
        if (user.fetchUserType() == UserType.STUDENT) {
            if (dataMap != null && dataMap.size() > 0) {
                //isVip 付费用户
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(uid);
                Boolean isVip = userOrderLoaderClient.isVipUser(uid);
                //isShowVipInfo 是否显示付费信息（类似于付费区域）
                Boolean isShowVipInfo = !studentDetail.isInPaymentBlackListRegion();
                //Boolean isShowVipInfo = isVip || studentLoader.loadStudentDetail(uid).isInAfentiTrialRegion() || studentLoader.loadStudentDetail(uid).isInAfentiBasicRegion();
                openAuthContext.add("schoolId", toLong(dataMap.get("schoolId")));
                openAuthContext.add("schoolName", ConversionUtils.toString(dataMap.get("schoolName")));
                openAuthContext.add("classId", toLong(dataMap.get("classId")));

                openAuthContext.add("teacherId", toLong(dataMap.get("teacherId")));
                openAuthContext.add("teacherName", ConversionUtils.toString(dataMap.get("teacherName")));
                openAuthContext.add("acode", toInteger(dataMap.get("acode")));
                openAuthContext.add("aname", ConversionUtils.toString(dataMap.get("aname")));
                openAuthContext.add("ccode", toInteger(dataMap.get("ccode")));
                openAuthContext.add("cname", ConversionUtils.toString(dataMap.get("cname")));
                openAuthContext.add("pcode", toInteger(dataMap.get("pcode")));
                openAuthContext.add("pname", ConversionUtils.toString(dataMap.get("pname")));
                openAuthContext.add("totalIntegral", toInteger(dataMap.get("totalIntegral")));
                openAuthContext.add("usableIntegral", toInteger(dataMap.get("usableIntegral")));
                openAuthContext.add("address", ConversionUtils.toString(dataMap.get("address")));
                openAuthContext.add("post", ConversionUtils.toString(dataMap.get("post")));
                openAuthContext.add("teacherPhone", ConversionUtils.toString(dataMap.get("phone")));
                openAuthContext.add("className", ClazzLevel.of(toInteger(dataMap.get("grade"))).getDescription() + " " + ConversionUtils.toString(dataMap.get("className")));
                openAuthContext.add("grade", ClazzLevel.of(toInteger(dataMap.get("grade"))).getDescription());
                openAuthContext.add("authentication", toInteger(dataMap.get("authentication")));
                openAuthContext.add("logisticType", ConversionUtils.toString(dataMap.get("logisticType")));
                openAuthContext.add("isVip", isVip);
                openAuthContext.add("isShowVipInfo", isShowVipInfo);

                List<Object[]> parentList = new ArrayList<>();
                List<StudentParent> parents = parentLoaderClient.loadStudentParents(uid);
                for (StudentParent parentInfo : parents) {
                    String parentMobile = sensitiveUserDataServiceClient.loadUserMobileObscured(parentInfo.getParentUser().getId());
                    parentList.add(new Object[]{
                            parentInfo.getParentUser().getId(),
                            StringUtils.defaultString(parentInfo.getCallName()),
                            StringUtils.defaultString(parentMobile)
                    });
                }
                openAuthContext.add("parentInfo", parentList);
            }
            // 老师
        } else if (user.getUserType() == 1 || user.getUserType() == 8) {
            if (dataMap != null && dataMap.size() > 0) {
                if (user.getAuthenticationState() != null) {
                    openAuthContext.add("authentication", user.getAuthenticationState());
                } else {
                    openAuthContext.add("authentication", 0);
                }
                openAuthContext.add("acode", toInteger(dataMap.get("acode")));
                openAuthContext.add("aname", dataMap.get("aname"));
                openAuthContext.add("ccode", toInteger(dataMap.get("ccode")));
                openAuthContext.add("cname", dataMap.get("cname"));
                openAuthContext.add("pcode", toInteger(dataMap.get("pcode")));
                openAuthContext.add("pname", dataMap.get("pname"));
                openAuthContext.add("totalIntegral", toInteger(dataMap.get("totalIntegral")));
                openAuthContext.add("usableIntegral", toInteger(dataMap.get("usableIntegral")));
                openAuthContext.add("address", ConversionUtils.toString(dataMap.get("address")));
                openAuthContext.add("post", ConversionUtils.toString(dataMap.get("post")));
                openAuthContext.add("phone", ConversionUtils.toString(dataMap.get("phone")));
                openAuthContext.add("logisticType", ConversionUtils.toString(dataMap.get("logisticType")));

                // 教研员使用
                if (user.getUserType() == UserType.RESEARCH_STAFF.getType()) {
                    openAuthContext.add("rsaname", dataMap.get("rsaname"));
                    openAuthContext.add("rscname", dataMap.get("rscname"));
                    openAuthContext.add("rspname", dataMap.get("rspname"));
                }

                List<Map<String, Object>> classInfo = new ArrayList<Map<String, Object>>(0);
                //查询班级
                List<Clazz> clazzs = (List<Clazz>) dataMap.get("clazzs");
                for (Clazz o : clazzs) {
                    if (o != null) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("schoolId", toLong(dataMap.get("schoolId")));
                        map.put("schoolName", ConversionUtils.toString(dataMap.get("schoolName")));
                        map.put("schoolType", dataMap.get("schoolType"));// add school type, by changyuan.liu
                        map.put("classId", o.getId());
                        map.put("className", ClazzLevel.of(Integer.parseInt(o.getClassLevel())).getDescription() + " " + o.getClassName());
                        classInfo.add(map);
                    }
                }
                openAuthContext.add("classes", classInfo);
                if (user.fetchUserType() == UserType.TEACHER) {
                    TeacherDetail detail = teacherLoaderClient.loadTeacherDetail(user.getId());
                    openAuthContext.add("isVip", detail.isSchoolAmbassador());
                    openAuthContext.add("isShowVipInfo", detail.isSchoolAmbassador());
                }
            }
        }
        return openAuthContext;
    }

    @Deprecated
    private Map<String, Object> getUserInfoByUserIdForOpen(Long uid) {
        if (uid == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> dataMap = new HashMap<>();
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazzs(Collections.singleton(uid)).get(uid);
        if (clazz == null) {
            return dataMap;
        }
        School school = asyncStudentServiceClient.getAsyncStudentService()
                .loadStudentSchool(uid)
                .getUninterruptibly();
        if (school == null) {
            return dataMap;
        }
        ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
        if (region != null) {
            dataMap.put("acode", region.getCountyCode());
            dataMap.put("aname", region.getCountyName());
            dataMap.put("ccode", region.getCityCode());
            dataMap.put("cname", region.getCityName());
            dataMap.put("pcode", region.getProvinceCode());
            dataMap.put("pname", region.getProvinceName());
        }
        dataMap.put("schoolId", school.getId());
        dataMap.put("schoolName", school.getCname());
        dataMap.put("payOpen", school.getPayOpen());
        dataMap.put("classId", clazz.getId());
        dataMap.put("className", clazz.getClassName());
        dataMap.put("grade", clazz.getClassLevel());
        UserIntegral integral = integralLoaderClient.getIntegralLoader().loadStudentIntegral(uid);
        dataMap.put("totalIntegral", integral.getTotal());
        dataMap.put("usableIntegral", integral.getUsable());
        return dataMap;
    }

    private Map<String, Object> getUserInfoByTeacherIdForOpen(User user) {
        if (user == null || user.getId() == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> dataMap = new HashMap<>();
        if (user.getUserType() == 1) {
            School school = asyncTeacherServiceClient.getAsyncTeacherService()
                    .loadTeacherSchool(user.getId())
                    .getUninterruptibly();
            if (school == null) {
                return dataMap;
            }
            UserShippingAddress shippingAddress = userLoaderClient.loadUserShippingAddresses(Collections.singleton(user.getId())).get(user.getId());
            List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(Collections.singleton(user.getId())).get(user.getId());
            if (clazzs == null) {
                clazzs = Collections.emptyList();
            }
            dataMap.put("clazzs", clazzs);
            dataMap.put("schoolId", school.getId());
            dataMap.put("schoolName", school.getCname());
            dataMap.put("schoolType", school.getType());// add school type, by changyuan.liu
            ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
            if (region != null) {
                dataMap.put("acode", region.getCountyCode());
                dataMap.put("aname", region.getCountyName());
                dataMap.put("ccode", region.getCityCode());
                dataMap.put("cname", region.getCityName());
                dataMap.put("pcode", region.getProvinceCode());
                dataMap.put("pname", region.getProvinceName());
            }
            UserIntegral integral = teacherLoaderClient.loadMainSubTeacherUserIntegral(user.getId(), null);
            dataMap.put("totalIntegral", integral.getTotal());
            dataMap.put("usableIntegral", integral.getUsable());
            if (shippingAddress != null) {
                dataMap.put("address", shippingAddress.getDetailAddress());
                dataMap.put("post", shippingAddress.getPostCode());
                dataMap.put("phone", shippingAddress.getSensitivePhone());
                dataMap.put("logisticType", shippingAddress.getLogisticType());
            }
            return dataMap;
        } else {//教研员
            // FIXME 注意，这里的教研员的区域信息是从shippingAddress里取的，
            UserShippingAddress shippingAddress = userLoaderClient.loadUserShippingAddresses(Collections.singleton(user.getId()))
                    .get(user.getId());
            UserIntegral integral = teacherLoaderClient.loadMainSubTeacherUserIntegral(user.getId(), null);
            dataMap.put("totalIntegral", integral.getTotal());
            dataMap.put("usableIntegral", integral.getUsable());
            dataMap.put("clazzs", new ArrayList<>());

            if (shippingAddress == null) {
                return dataMap;
            }

            dataMap.put("acode", shippingAddress.getCountyCode());
            dataMap.put("aname", shippingAddress.getCountyName());
            dataMap.put("ccode", shippingAddress.getCityCode());
            dataMap.put("cname", shippingAddress.getCityName());
            dataMap.put("pcode", shippingAddress.getProvinceCode());
            dataMap.put("pname", shippingAddress.getProvinceName());
            dataMap.put("address", shippingAddress.getDetailAddress());
            dataMap.put("post", shippingAddress.getPostCode());
            dataMap.put("phone", shippingAddress.getSensitivePhone());
            dataMap.put("logisticType", shippingAddress.getLogisticType());
            return dataMap;
        }
    }
}