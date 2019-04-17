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

package com.voxlearning.washington.controller.nekketsu;

import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.runtime.collector.LogCollector;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author xinqiang.wang
 * @serial 2014/9/11
 */
@Controller
@RequestMapping("/student/nekketsu")
public class NekketsuController extends AbstractController {

    /**
     * @return
     */
    @RequestMapping(value = "adventure.vpage", method = RequestMethod.GET)
    public String nekketsu(Model model) {
        model.addAttribute("app_key", OrderProductServiceType.Walker.name());

        StudentDetail studentDetail = currentStudentDetail();
        if (isWalkerPlayable(studentDetail)) {
            // log pv
            LogCollector.instance().info("a17zy_app_pv_logs",
                MiscUtils.map(
                    "app_key", OrderProductServiceType.Walker.name(),
                    "user_id", currentUserId(),
                    "platform", "pc",
                    "env", RuntimeMode.getCurrentStage(),
                    "client_ip", getWebRequestContext().getRealRemoteAddr()
                ));


            return "studentv3/afenti/nekketsu/adventure";
        } else {
            return "redirect:/student/index.vpage";
        }
    }

    // 能不能玩沃克
    private boolean isWalkerPlayable(StudentDetail detail) {
        if (detail == null) {
            return false;
        }

        // 判断用户有没有沃克大冒险的权限
        if (detail.isInPaymentBlackListRegion()) {
            AppPayMapper mapper = userOrderLoaderClient.getUserAppPaidStatus(OrderProductServiceType.Walker.name(), detail.getId());
            if (mapper == null || mapper.unpaid()) {
                return false;
            }
        }

        return true;
    }
}
