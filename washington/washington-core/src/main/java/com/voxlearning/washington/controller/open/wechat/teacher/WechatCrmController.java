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

package com.voxlearning.washington.controller.open.wechat.teacher;

import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.CrmTaskRecordCategory;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryServiceClient;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author shiwei.liao
 * @since 2015/8/7.
 */

@Named
@RequestMapping(value = "/open/wechat/crm")
public class WechatCrmController extends AbstractOpenController {

    @Inject private CrmSummaryServiceClient crmSummaryServiceClient;

    private static final String TASK_RECORD_CATEGORY;

    static {
        List<CrmTaskRecordCategory> taskRecordCategories = Arrays.asList(CrmTaskRecordCategory.认证问题, CrmTaskRecordCategory.班级or学生管理问题,
                CrmTaskRecordCategory.作业or检测or教材问题, CrmTaskRecordCategory.账号or密码or绑定问题, CrmTaskRecordCategory.园丁豆or学豆问题,
                CrmTaskRecordCategory.奖品中心问题, CrmTaskRecordCategory.活动问题, CrmTaskRecordCategory.其余问题, CrmTaskRecordCategory.无效问题);
        Map<CrmTaskRecordCategory, Map<CrmTaskRecordCategory, Set<CrmTaskRecordCategory>>> buffer = new LinkedHashMap<>();
        for (CrmTaskRecordCategory category : taskRecordCategories) {
            buffer.put(category, CrmTaskRecordCategory.TREE.get(category));
        }
        TASK_RECORD_CATEGORY = JsonUtils.toJson(buffer);
    }

    @RequestMapping(value = "teacher/record/index.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext wechatTeacherRecordIndex(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        //工作记录分类可选列表
        context.add("recordCategory", TASK_RECORD_CATEGORY);
        return context;
    }

    @RequestMapping(value = "teacher/record/add.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext addWechatTeacherRecord(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        context.setCode("200");
        //老师ID
        Long teacherId = ConversionUtils.toLong(context.getParams().get("teacherId"));
        //记录人用户名---这个之前的需求上没有,需要加一个输入框
        String userName = ConversionUtils.toString(context.getParams().get("userName"));
        //分类
        CrmTaskRecordCategory recordCategory = CrmTaskRecordCategory.nameOf(ConversionUtils.toString(context.getParams().get("category")));
        //内容
        String content = ConversionUtils.toString(context.getParams().get("content"));
        MapMessage message = crmSummaryServiceClient.addWechatTaskRecord(teacherId, userName, userName, recordCategory, content);
        if (!message.isSuccess()) {
            context.setError(message.getInfo());
            return context;
        }
        context.setError("保存成功");
        return context;
    }
}
