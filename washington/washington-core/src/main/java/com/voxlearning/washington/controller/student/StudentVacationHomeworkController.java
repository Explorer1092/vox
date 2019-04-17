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

package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/student/vacation/homework")
public class StudentVacationHomeworkController extends AbstractController {

    /**
     * 假期作业:学生开始做作业首页
     * @param model
     * @return
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    @SuppressWarnings("unchecked")
    public String index(Model model) {
        String homeworkId = getRequestString("homeworkId");
        Long userId = currentUserId();
        model.addAttribute("homeworkId", homeworkId);
        VacationHomework vacationHomework = vacationHomeworkLoaderClient.loadVacationHomeworkById(homeworkId);
        if (vacationHomework == null) {
            String[] keys = homeworkId.split("-");
            if (keys.length == 4) {
                //校验学生作业包是否解锁，如果未解锁则自动跳转到首页
                MapMessage mapMessage = vacationHomeworkLoaderClient.loadStudentDayPackages(keys[0], userId);
                if (mapMessage.isSuccess()) {
                    List<Map> pages = (List<Map>) mapMessage.get("dayPackages");
                    for (Map page : pages) {
                        if (homeworkId.equals(SafeConverter.toString(page.get("homeworkId"))) && SafeConverter.toBoolean(page.get("locked"))) {
                            return "redirect:/student/index.vpage";
                        }
                    }
                }
                vacationHomework = vacationHomeworkService.generateVacationHomework(keys[0], SafeConverter.toInt(keys[1]), SafeConverter.toInt(keys[2]), userId);
            }
        }
        if (vacationHomework != null) {
            model.addAttribute("listUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/index.vpage", MiscUtils.m("homeworkId", homeworkId)));
            model.addAttribute("subject", vacationHomework.getSubject().name());
            return "studentv3/homeworkv3/index";
        }
        return "redirect:/student/index.vpage";
    }
}
