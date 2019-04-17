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

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.constants.UserExtensionAttributeKeyType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 2017.5.25 CRM手机绑定功能下线
 *
 * @author Longlong Yu
 * @since 下午12:03,13-12-30.
 */
@Deprecated
@Controller
@RequestMapping("crm/activatemobile")
public class CrmActivateMobileController extends CrmAbstractController {

    @RequestMapping(value = "unactivateuserlist.vpage", method = RequestMethod.GET)
    public String unactivateUserList(Model model) {
        // by changyuan.liu
        // 2017.5.25 CRM手机绑定功能下线
//        model.addAttribute("unactivateUserList", getUnactivateUserList());
        return "crm/activatemobile/unactivateuserlist";
    }

    @RequestMapping(value = "activateusermobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage activateUserMobile() {

        long userId = getRequestLong("userId", -1L);
        final User user = userLoaderClient.loadUser(userId);
        String mobile = getRequestParameter("mobile", "");

        if (userId < 0 || StringUtils.isBlank(mobile))
            return MapMessage.errorMessage("用户：" + userId + "，手机：" + mobile + "不存在");

        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentications(mobile)
                .stream()
                .filter(t -> t.getUserType() == user.fetchUserType())
                .findFirst()
                .orElse(null);

        if (userAuthentication == null) {
            MapMessage message = userServiceClient.activateUserMobile(userId, mobile, true, getCurrentAdminUser().getAdminUserName(), "管理员");
            if (!message.isSuccess()) {
                MapMessage.successMessage("激活用户" + userId + "手机" + mobile + "失败");
            }
        } else {
            return MapMessage.errorMessage("手机" + mobile + "已被其他用户绑定");
        }

        return MapMessage.successMessage("激活用户" + userId + "手机" + mobile + "成功");
    }

    @RequestMapping(value = "deleterecord.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage deleteRecord() {

        long userId = getRequestLong("userId", -1L);
        String mobile = getRequestParameter("mobile", "");

        if (userId < 0 || StringUtils.isBlank(mobile))
            return MapMessage.errorMessage("用户：" + userId + "，手机" + mobile + "不存在");

        String query = "DELETE uea.* FROM VOX_USER_EXTENSION_ATTRIBUTE uea WHERE uea.USER_ID = ? AND uea.EXTENSION_ATTRIBUTE_KEY = ?";
        utopiaSql.withSql(query).useParamsArgs(userId, UserExtensionAttributeKeyType.NO_CERTIFICATION_CODE_RECEIVED.name()).executeUpdate();

        // admin log
        String operation = "crm-deleterecord-" + getCurrentAdminUser().getAdminUserName() + "删除记录：用户" + userId + "，手机" + StringUtils.mobileObscure(mobile);
        addAdminLog(operation);

        return MapMessage.successMessage("删除记录（用户：" + userId + "，手机：" + mobile + "）成功");
    }

    /*************************
     * private method
     *************************************************************/
//    private List<Map<String, Object>> getUnactivateUserList() {
//
//        String query = "SELECT uea.USER_ID AS userId, uea.EXTENSION_ATTRIBUTE_VALUE AS mobile, uea.CREATETIME as createTime FROM VOX_USER_EXTENSION_ATTRIBUTE uea " +
//                " WHERE uea.EXTENSION_ATTRIBUTE_KEY = ? " +
//                " AND NOT EXISTS(SELECT 1 FROM UCT_USER_AUTHENTICATION ua where ua.USER_ID = uea.USER_ID and ua.MOBILE != \'\' and ua.MOBILE IS NOT NULL and ua.DISABLED = 0) " +
//                " GROUP BY uea.USER_ID ORDER BY uea.CREATETIME DESC";
//
//        List<Map<String, Object>> unactivateUserList = utopiaSql.withSql(query).useParamsArgs(UserExtensionAttributeKeyType.NO_CERTIFICATION_CODE_RECEIVED.name()).queryAll();
//
//        for (Map<String, Object> it : unactivateUserList) {
//            Teacher teacher = teacherLoaderClient.loadTeacher(NumberUtils.toLong(String.valueOf(it.get("userId"))));
//            if (teacher == null) continue;
//
//            it.put("userName", (teacher.getProfile() == null) ? null : teacher.getProfile().getRealname());
//            School school = asyncTeacherServiceClient.getAsyncTeacherService()
//                    .loadTeacherSchool(teacher.getId())
//                    .getUninterruptibly();
//            if (school == null) continue;
//
//            it.put("schoolName", school.getCname());
//            it.put("schoolId", school.getId());
//            it.put("regionCode", school.getRegionCode());
//            ExRegion exRegion = regionServiceClient.loadRegion(school.getRegionCode());
//            if (exRegion == null) {
//                it.put("regionName", "null/null");
//            } else {
//                it.put("regionName", exRegion.getProvinceName() + "/" + exRegion.getCityName());
//            }
//        }
//
//        return unactivateUserList;
//    }
}
