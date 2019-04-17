package com.voxlearning.utopia.admin.controller.junior;

/**
 * Created by alex on 2018/6/8.
 */
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

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.controller.site.SiteAbstractController;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.constant.GlobalTagName;
import com.voxlearning.utopia.service.config.api.entity.GlobalTag;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.constant.GlobalTagName;
import com.voxlearning.utopia.service.config.api.entity.GlobalTag;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/junior/config")
public class CrmJuniorConfigController extends AbstractAdminSystemController {

    private List<GlobalTagName> JUNIOR_TAGS = Arrays.asList(GlobalTagName.JuniorNoStudentRankSchools);


    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;

    @Inject
    private GlobalTagServiceClient globalTagServiceClient;

    /**
     * 增加global配置
     */
    @RequestMapping(value = "addglobaltags.vpage", method = RequestMethod.GET)
    public String addGlobalSchool(Model model) {
        model.addAttribute("tagNames", JUNIOR_TAGS);
        return "junior/config/addglobaltags";
    }

    @RequestMapping(value = "addglobaltag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addGlobalGrayConfigPost() {
        String tagValue = getRequestParameter("tagValue", "");
        String comment = getRequestParameter("comment", "");
        String tagName = getRequestParameter("tagName", "");
        GlobalTagName globalTagName = null;
        try {
            globalTagName = GlobalTagName.valueOf(tagName);
        } catch (Exception ex) {
            return MapMessage.errorMessage("无效的tagName");
        }
        Set<String> configSet = new HashSet<>();
        try {
            String[] configList = tagValue.split("[,，\\s]+");
            for (String config : configList) {
                if (StringUtils.isNotBlank(config)) {
                    configSet.add(config);
                }
            }
        } catch (Exception ignored) {
            return MapMessage.errorMessage("存在不符合规范的值");
        }

        if (configSet.size() == 0)
            return MapMessage.errorMessage("不存在有效的值");

        for (String s : configSet) {
            GlobalTag tag = new GlobalTag();
            tag.setTagName(globalTagName.name());
            tag.setTagValue(s);
            tag.setTagComment(comment);
            globalTagServiceClient.getGlobalTagService().insertGlobalTag(tag).awaitUninterruptibly();
        }
        addAdminLog("site-global-tags-" + getCurrentAdminUser().getAdminUserName() + "增加global配置");
        return MapMessage.successMessage("增加global配置成功");
    }

    @RequestMapping(value = "globaltaglist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String globalTagList(Model model) {
        if (isRequestGet()) {
            model.addAttribute("tagNames", JUNIOR_TAGS);
            return "junior/config/globaltaglist";
        }
        String tagName = getRequestParameter("tagName", "");
        List<GlobalTag> tagsList = globalTagServiceClient.getGlobalTagService()
                .loadAllGlobalTagsFromDB()
                .getUninterruptibly()
                .stream()
                .filter(e -> GlobalTagName.valueOf(e.getTagName()) == GlobalTagName.valueOf(tagName))
                .collect(Collectors.toList());
        model.addAttribute("tagNames", JUNIOR_TAGS);
        model.addAttribute("tagsList", tagsList);
        model.addAttribute("tagName", tagName);
        return "junior/config/globaltaglist";
    }

    @RequestMapping(value = "deleteglobaltag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteGlobalTag() {
        String tagId = getRequestParameter("tagId", "");
        if (StringUtils.isBlank(tagId)) {
            return MapMessage.errorMessage("非法的参数");
        }
        if (globalTagServiceClient.getGlobalTagService().removeGlobalTag(tagId).getUninterruptibly()) {
            addAdminLog("global-deleteglobaltag-" + getCurrentAdminUser().getAdminUserName() + "删除Tag,tagId=" + tagId);
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage();
        }
    }

}
