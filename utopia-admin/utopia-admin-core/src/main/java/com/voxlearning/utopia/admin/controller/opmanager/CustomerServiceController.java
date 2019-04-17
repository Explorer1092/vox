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

package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @author Longlong Yu
 * @since 下午7:53,13-11-13.
 */
@Controller
@RequestMapping("/opmanager/customerservice")
public class CustomerServiceController extends OpManagerAbstractController {

    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String customerServiceIndex(Model model) {
        String contentType = getRequestParameter("contentType", "all");
        PageBlockContent pageBlockContent = crmConfigService.$loadPageBlockContents().stream()
                .filter(e -> StringUtils.equals(e.getPageName(), "CustomerServiceIndex"))
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .sorted((o1, o2) -> {
                    int d1 = SafeConverter.toInt(o1.getDisplayOrder());
                    int d2 = SafeConverter.toInt(o2.getDisplayOrder());
                    return Integer.compare(d1, d2);
                })
                .findFirst()
                .orElse(null);
        if (pageBlockContent == null) {
            pageBlockContent = new PageBlockContent();
            pageBlockContent.setPageName("CustomerServiceIndex");
            pageBlockContent.setBlockName("TopBanner");
            pageBlockContent.setContent("");
            pageBlockContent.setMemo("客服人员，每日要闻");
            pageBlockContent.setDisplayOrder(0);
            pageBlockContent.setDisabled(false);
            pageBlockContent.setEndDatetime(new Date(Timestamp.valueOf("2038-01-01 00:00:00").getTime()));
            pageBlockContent = crmConfigService.$upsertPageBlockContent(pageBlockContent);
            addAdminLog("savePageBlockContent", pageBlockContent.getId(), "首次访问自动生成");
        }

        String content = pageBlockContent.getContent();
        if ("temporary".equals(contentType)) {
            content = content.replaceAll("<temporary>[\\s\\S]*</temporary>", "");
        } else if ("permanent".equals(contentType)) {
            content = content.replaceAll("<permanent>[\\s\\S]*</permanent>", "");
        }

        model.addAttribute("CSSIndexPageBlockContent", content);
        return "opmanager/customerservice/index";
    }

}
