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

package com.voxlearning.wechat.controller.parent;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.wechat.constants.WechatInfoCode;
import com.voxlearning.wechat.controller.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Xin Xin
 * @since 11/8/15
 */
@Controller
@RequestMapping(value = "/parent/integral")
public class ParentIntegralController extends AbstractController {

    //下单页面
    @RequestMapping(value = "/order.vpage", method = RequestMethod.GET)
    public String order(Model model) {
        Long studentId = getRequestLong("sid");

        try {
            //如果没有传递孩子id,默认第一个孩子的id
            if (0 == studentId) {
                List<User> children = studentLoaderClient.loadParentStudents(getRequestContext().getUserId());
                if (!CollectionUtils.isEmpty(children)) {
                    studentId = children.get(0).getId();
                }
            }

            List<Teacher> teachers = userAggregationLoaderClient.loadStudentTeachers(studentId).stream().map(ClazzTeacher::getTeacher).collect(Collectors.toList());
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);

            List<Map<String, Object>> maps = new ArrayList<>();
            teachers.stream().filter(t -> t.getAuthenticationState() == AuthenticationState.SUCCESS.getState()).forEach(t -> {
                Map<String, Object> map = new HashMap<>();
                map.put("name", t.getProfile().getRealname());
                map.put("id", t.getId());
                if (null != t.getSubject()) {
                    map.put("subject", t.getSubject().name());
                }
                maps.add(map);
            });

            if (!CollectionUtils.isEmpty(maps)) {
                model.addAttribute("teachers", maps);
                model.addAttribute("classId", clazz.getId());
                model.addAttribute("sid", studentId);
                model.addAttribute("pid", getRequestContext().getUserId());
            } else {
                return infoPage(WechatInfoCode.PARENT_INTEGRAL_PRESENT_NOT_ALLOW_WITHOUT_TEACHER_AUTH, model);
            }
        } catch (Exception ex) {
            logger.error("Create integral order failed, sid:{}", studentId, ex);
        }

        return "/parent/integral/order";
    }

    @RequestMapping(value = "/order.vpage", method = RequestMethod.POST)
    public String orderPost(Model model, HttpServletResponse response) {
        return redirectWithMsg("学豆购买功能已下线", model);
    }

    private Integer transferPriceToIntegral(Integer price) {
        switch (price) {
            case 1:
                return 20;
            case 2:
                return 50;
            case 5:
                return 120;
            case 10:
                return 260;
            default:
                throw new RuntimeException("不存在此兑换金额！");
        }
    }
}
