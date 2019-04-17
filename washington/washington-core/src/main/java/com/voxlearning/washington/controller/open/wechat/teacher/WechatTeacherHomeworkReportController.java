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

package com.voxlearning.washington.controller.open.wechat.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.lang.cipher.DesUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.exception.config.ConfigurationException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

import static com.voxlearning.washington.controller.open.OpenApiReturnCode.*;

/**
 * @author RuiBao
 * @since 10/9/2015
 */

@Controller
@RequestMapping(value = "/open/wechat/teacher/homework/report")
@Slf4j
public class WechatTeacherHomeworkReportController extends AbstractOpenController {

    @Inject private RaikouSDK raikouSDK;

    @RequestMapping(value = "unitreport.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext unitReport(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "unitreportdetail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext unitReportDetail(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "share.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext share(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        Long teacherId = ConversionUtils.toLong(context.getParams().get("uid"));
        Long groupId = ConversionUtils.toLong(context.getParams().get("groupId"));
        Long unitId = ConversionUtils.toLong(context.getParams().get("unitId"));
        String message = ConversionUtils.toString(context.getParams().get("message"));
        if (teacherId <= 0 || groupId <= 0 || unitId <= 0) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        try {
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            if (teacher == null) {
                context.setCode(BUSINESS_ERROR_CODE);
                context.setError("用户不存在");
                return context;
            }
            if (teacher.getSubject() == null || teacher.getSubject() != Subject.ENGLISH) {
                context.setCode(BUSINESS_ERROR_CODE);
                context.setError("功能暂未开放");
                return context;
            }
            GroupTeacherTuple ref = raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient()
                    .findByTeacherId(teacher.getId())
                    .stream()
                    .filter(e -> e.getGroupId() != null)
                    .filter(e -> Objects.equals(e.getGroupId(), groupId))
                    .findFirst()
                    .orElse(null);
            if (ref == null) {
                context.setCode(BUSINESS_ERROR_CODE);
                context.setError("权限不足");
                return context;
            }
            Map<String, Object> map = MiscUtils.m("groupId", groupId, "unitId", unitId, "subject", teacher.getSubject().name(), "message", message);

            String legacy_des_key = ConfigManager.instance().getCommonConfig().getConfigs().get("legacy_des_key");
            if (legacy_des_key == null) {
                throw new ConfigurationException("No 'legacy_des_key' configured");
            }

            String code = DesUtils.encryptHexString(legacy_des_key, JsonUtils.toJson(map));
            String link = ProductConfig.getMainSiteBaseUrl() + "/activity/gur.vpage";
            link = UrlUtils.buildUrlQuery(link, MiscUtils.m("code", code));
            context.add("link", UrlUtils.buildUrlQuery(link, MiscUtils.m("code", code)));
            context.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            logger.error("Teacher {} share unit report error.", teacherId, ex);
            context.setCode(SYSTEM_ERROR_CODE);
        }
        return context;
    }
}
