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

/**
 * Created by XiaoPeng.Yang on 15-3-31.
 */
@Controller
@RequestMapping("/site/global")
public class SiteGlobalTagController extends SiteAbstractController {

    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;

    @Inject private GlobalTagServiceClient globalTagServiceClient;

    /**
     * 增加global配置
     */
    @RequestMapping(value = "addglobaltags.vpage", method = RequestMethod.GET)
    public String addGlobalSchool(Model model) {
        model.addAttribute("tagNames", GlobalTagName.values());
        return "site/globaltag/addglobaltags";
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

    /**
     * 查询
     */
    @RequestMapping(value = "globaltaglist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String globalSchoolList(Model model) {
        if (isRequestGet()) {
            model.addAttribute("tagNames", GlobalTagName.values());
            return "site/globaltag/globaltaglist";
        }
        String tagName = getRequestParameter("tagName", "");
        List<GlobalTag> tagsList = globalTagServiceClient.getGlobalTagService()
                .loadAllGlobalTagsFromDB()
                .getUninterruptibly()
                .stream()
                .filter(e -> GlobalTagName.valueOf(e.getTagName()) == GlobalTagName.valueOf(tagName))
                .collect(Collectors.toList());
        model.addAttribute("tagNames", GlobalTagName.values());
        model.addAttribute("tagsList", tagsList);
        model.addAttribute("tagName", tagName);
        return "site/globaltag/globaltaglist";
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

    @RequestMapping(value = "downloadtaginfo.vpage", method = RequestMethod.GET)
    public void downloadSchoolInfo(HttpServletResponse response) {
        String tagName = getRequestParameter("tagName", "");
        GlobalTagName gtn;
        try {
            gtn = GlobalTagName.valueOf(tagName);
        } catch (Exception ex) {
            gtn = null;
        }
        if (gtn == null) {
            try {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException e) {
                logger.error("download global tag list exception!");
            }
            return;
        }
        AtomicReference<GlobalTagName> reference = new AtomicReference<>(gtn);
        List<GlobalTag> exportList = globalTagServiceClient.getGlobalTagService()
                .loadAllGlobalTagsFromDB()
                .getUninterruptibly()
                .stream()
                .filter(e -> GlobalTagName.valueOf(e.getTagName()) == reference.get())
                .collect(Collectors.toList());

        XSSFWorkbook xssfWorkbook = convertToXSSF(exportList);
        String filename = tagName + "-" + DateUtils.dateToString(new Date()) + ".xlsx";
        try {
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            xssfWorkbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (IOException ignored) {
            try {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException e) {
                logger.error("download black school list exception!");
            }
        }
    }

    private XSSFWorkbook convertToXSSF(List<GlobalTag> dataList) {
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet xssfSheet = xssfWorkbook.createSheet();
        XSSFRow firstRow = xssfSheet.createRow(0);
        firstRow.setHeightInPoints(20);
        firstRow.createCell(0).setCellValue("TAG_NAME");
        firstRow.createCell(1).setCellValue("TAG_VALUE");
        firstRow.createCell(2).setCellValue("备注");
        firstRow.createCell(3).setCellValue("创建时间");

        int rowNum = 1;
        for (GlobalTag data : dataList) {
            XSSFRow xssfRow = xssfSheet.createRow(rowNum++);
            xssfRow.setHeightInPoints(20);
            xssfRow.createCell(0).setCellValue(data.getTagName());
            xssfRow.createCell(1).setCellValue(data.getTagValue());
            xssfRow.createCell(2).setCellValue(data.getTagComment());
            xssfRow.createCell(3).setCellValue(DateUtils.dateToString(data.getCreateDatetime()));
        }
        for (int i = 0; i < 4; i++) {
            xssfSheet.setColumnWidth(i, 400 * 15);
        }
        return xssfWorkbook;
    }
}
