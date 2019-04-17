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

package com.voxlearning.utopia.admin.controller;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.connection.DataSourceConnectionBuilder;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.concurrent.atomic.AtomicReference;

@Controller
@RequestMapping("/")
public class AdminIndexController extends AbstractAdminController {

    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        PageBlockContent pageBlockContent = crmConfigService.$loadPageBlockContents().stream()
                .filter(e -> StringUtils.equals(e.getPageName(), "AdminIndex"))
                .sorted((o1, o2) -> {
                    int d1 = SafeConverter.toInt(o1.getDisplayOrder());
                    int d2 = SafeConverter.toInt(o2.getDisplayOrder());
                    return Integer.compare(d1, d2);
                })
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .findFirst()
                .orElse(null);
        if (pageBlockContent != null) {
            model.addAttribute("adminIndexPageBlockContent", pageBlockContent.getContent());
        }
        String watcher = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("myNewFeedbackCount", getMyNewFeedbackCount(watcher));
        model.addAttribute("watcher", watcher);
        model.addAttribute("startDate", "2012-01-01");
        return "index";
    }

    private Integer getMyNewFeedbackCount(String watcher) {
        String s = "SELECT COUNT(1) FROM VOX_USER_FEEDBACK tf,VOX_USER_FEEDBACK_TAG tft " +
                "WHERE tf.DISABLED=0 AND tf.STATE=0 AND tf.TAG=tft.NAME AND tft.WATCHER_NAME=?";
        AtomicReference<String> sql = new AtomicReference<>(s);
        JdbcTemplate jdbcTemplate = DataSourceConnectionBuilder.getInstance()
                .getDataSourceConnection("main")
                .getJdbcTemplate();
        return jdbcTemplate.queryForObject(sql.get(), Integer.class, watcher);
    }

}
