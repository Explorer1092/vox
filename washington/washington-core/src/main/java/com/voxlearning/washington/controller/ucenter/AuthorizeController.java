/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.ucenter;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.utopia.api.legacy.MemcachedKeyConstants;
import com.voxlearning.utopia.service.user.api.mappers.UserSecurity;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ucenter/authorize")
public class AuthorizeController extends AbstractController {
    @RequestMapping(value = "{page}.vpage", method = RequestMethod.GET)
    public String page(@PathVariable("page") String page,
                       @RequestParam(value = "p", required = false, defaultValue = "0") Integer p,
                       Model model,
                       HttpServletRequest request) {
        if (currentUserId() != null) {
            model.addAttribute("userId", ConversionUtils.toString(currentUserId()));
            model.addAttribute("adminFlag", currentUser().fetchUserType() == UserType.EMPLOYEE);
        }
        StringBuilder serverUrl = new StringBuilder();
        serverUrl.append(request.getScheme()).append("://").append(request.getServerName())
                .append(request.getServerPort() == 80 ? "" : ":" + request.getServerPort())
                .append("/".equals(request.getContextPath()) ? "" : request.getContextPath()).append("/");

        model.addAttribute("serverUrl", serverUrl);
        model.addAttribute("serverName", request.getServerName().toLowerCase());
        model.addAttribute("p", p);
        return "ucenter/authorize/" + page;
    }

    @RequestMapping(value = "selectuser.vpage", method = RequestMethod.GET)
    public String selectUser(Model model) {
        String key = getRequestParameter("key", "");
        if (StringUtils.isBlank(key)) {
            return "redirect:/login.vpage#error=adult";
        }
        try {
            Map<String, Object> map = washingtonCacheSystem.CBS.unflushable.load(MemcachedKeyConstants.MULTI_USER_LOGIN_PREFIX + key);
            // 如果东西没有拿到，登陆失败，重新登陆
            if (map == null || !map.containsKey("candidates")) {
                return "redirect:/login.vpage#error=adult";
            }
            List<Map<String, Object>> result = new ArrayList<>();
            // noinspection unchecked
            List<UserSecurity> securities = (List<UserSecurity>) map.get("candidates");
            for (UserSecurity security : securities) {
                Map<String, Object> each = new HashMap<>();
                each.put("useId", security.getUserId());
                each.put("realname", security.getRealname());
                RoleType roleType = security.getRoleTypes().get(0);
                each.put("userType", UserType.of(roleType.getType()));
                result.add(each);
            }
            model.addAttribute("candidates", result);
            model.addAttribute("key", key);

            Object dataKey = map.get("dataKey");
            if (dataKey != null) {
                model.addAttribute("dataKey", dataKey);
            }

        } catch (Exception e) {
            return "redirect:/login.vpage#error=adult";
        }
        return "ucenter/authorize/selectuser";
    }
}
