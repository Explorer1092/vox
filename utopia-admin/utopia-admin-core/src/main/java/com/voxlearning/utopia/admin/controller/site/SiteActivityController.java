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

package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.utopia.admin.dao.ActivityRechargeTeacherPersistence;
import com.voxlearning.utopia.entity.activity.ActivityRechargeTeacher;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by XiaoPeng.Yang on 15-3-16.
 */
@Controller
@RequestMapping("/site/activity")
public class SiteActivityController extends SiteAbstractController {

    @Inject private ActivityRechargeTeacherPersistence activityRechargeTeacherPersistence;

    /**
     * 充值话费活动（运营）工具
     */
    @RequestMapping(value = "rechargeteacher.vpage", method = RequestMethod.GET)
    String batchCreateTeacherHomepage() {
        return "site/activity/rechargeteacher";
    }

    @RequestMapping(value = "batchimportrechargeteacher.vpage", method = RequestMethod.POST)
    public String batchImportRechargeTeacher(@RequestParam String teacherIds,
                                             @RequestParam String stuCount,
                                             @RequestParam String amount,
                                             Model model) {
        if (StringUtils.isEmpty(teacherIds) || !NumberUtils.isNumber(stuCount) || !NumberUtils.isNumber(amount)) {
            getAlertMessageManager().addMessageError("内容格式错误");
            return "site/activity/rechargeteacher";
        }
        if (ConversionUtils.toInt(stuCount) <= 0 || ConversionUtils.toInt(amount) <= 0) {
            getAlertMessageManager().addMessageError("内容格式错误");
            return "site/activity/rechargeteacher";
        }
        String[] ids = teacherIds.split("\\r\\n");
        List<Map<String, Object>> lstFailed = new ArrayList<>();
        for (String id : ids) {
            if (StringUtils.isEmpty(id)) {
                continue;
            }
            try {
                Long teacherId = Long.parseLong(id);
                Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
                if (teacher == null) {
                    throw new RuntimeException("老师不存在");
                }

                // FIXME: 杨宵鹏，这种逻辑能不能靠数据库来约束？
                ActivityRechargeTeacher rechargeTeacher = activityRechargeTeacherPersistence.loadByTeacherIdAndMonth(teacherId,
                        ConversionUtils.toInt(DateUtils.dateToString(new Date(), "yyyyMM")));
                if (rechargeTeacher != null) {
                    throw new RuntimeException("重复导入数据");
                }

                ActivityRechargeTeacher activityRechargeTeacher = new ActivityRechargeTeacher();
                activityRechargeTeacher.setTeacherId(teacherId);
                activityRechargeTeacher.setStuCount(ConversionUtils.toInt(stuCount));
                activityRechargeTeacher.setRechargeAmount(ConversionUtils.toInt(amount));
                activityRechargeTeacher.setMonth(ConversionUtils.toInt(DateUtils.dateToString(new Date(), "yyyyMM")));
                activityRechargeTeacherPersistence.insert(activityRechargeTeacher);
                addAdminLog("message-管理员" + getCurrentAdminUser().getAdminUserName() + "导入运营充值话费活动老师ID");
            } catch (Exception ex) {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("teacherId", id);
                errorMap.put("msg", ex.getMessage());
                lstFailed.add(errorMap);
                continue;
            }
        }
        model.addAttribute("failedlist", lstFailed);
        return "site/activity/rechargeteacher";
    }
}