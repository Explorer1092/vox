/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.service.site.SiteSchoolService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;

/**
 * Created by Yuechen Wang on 2016/04/12.
 */
@Controller
@RequestMapping("/site/school")
public class SiteSchoolController extends SiteAbstractController {

    @Inject private SiteSchoolService siteSchoolService;

    @RequestMapping(value = "batchsearchschool.vpage", method = RequestMethod.GET)
    String batchSearchSchool() {
        return "site/school/batchsearchschool";
    }

    @RequestMapping(value = "batchsearchschool.vpage", method = RequestMethod.POST)
    public String batchSearchSchool(@RequestParam String content, Model model) {
        try {
            MapMessage queryMsg = siteSchoolService.batchQuerySchool(content);
            String comment = "操作结果：";
            if (!queryMsg.isSuccess()) {
                logger.error("批量查询学校信息失败:{}", queryMsg.getInfo());
                comment += "失败" + queryMsg.getInfo();
            } else {
                comment += "成功";
            }
            addAdminLog("batchSearchSchool", 0L, comment, content);
        } catch (Exception ex) {
            logger.error("批量查询学校信息失败:{}", ex);
        }
        return "site/school/batchsearchschool";
    }



}
