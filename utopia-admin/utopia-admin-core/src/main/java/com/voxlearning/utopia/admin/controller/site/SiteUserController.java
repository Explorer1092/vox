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

package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.service.site.SiteSchoolService;
import com.voxlearning.utopia.admin.service.site.SiteUserService;
import com.voxlearning.utopia.admin.service.site.SiteUserService.QueryMobileResult;
import lombok.Cleanup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Shuai Huan on 2014/11/20.
 */
@Controller
@RequestMapping("/site/user")
public class SiteUserController extends SiteAbstractController {

    @Inject private SiteUserService siteUserService;
    @Inject private SiteSchoolService siteSchoolService;


    @RequestMapping(value = "batchqueryuseridbymobilepage.vpage", method = RequestMethod.GET)
    String batchQueryUserIdByMobilePage() {
        return "site/batch/batchqueryuseridbymobilepage";
    }

    @RequestMapping(value = "batchqueryuseridbymobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage batchQueryUserIdByMobile(@RequestParam String content) {
        if (StringUtils.isEmpty(content)) {
            return MapMessage.errorMessage("内容不能为空");
        }

        MapMessage mapMessage = siteUserService.batchQueryUserIdByMobile(content, getCurrentAdminUser().getAdminUserName());
        generateUserIdMobileExcelFile(mapMessage);
        return mapMessage;
    }

    @RequestMapping(value = "batchSetRstaffManagedRegion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchSetRstaffManagedRegion(@RequestBody String content) {

        if (StringUtils.isEmpty(content)) {
            return MapMessage.errorMessage("内容不能为空");
        }

        return siteUserService.batchSetRstaffManagedSchool(content);
    }

    /////////////////////////////////////////////////////Private Methods///////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    private void generateUserIdMobileExcelFile(MapMessage mapMessage) {
        if (mapMessage.isSuccess()) {
            List<QueryMobileResult> results = (List<QueryMobileResult>) mapMessage.remove("result");
            if (results != null) {
                Workbook wb = new XSSFWorkbook();
                Sheet sheet = wb.createSheet();

                int rowNum = 0;
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("用户ID");
                row.createCell(1).setCellValue("手机号");
                row.createCell(2).setCellValue("用户姓名");
                row.createCell(3).setCellValue("学生年级");
                row.createCell(4).setCellValue("学生班级");
                row.createCell(5).setCellValue("学生学校ID");
                row.createCell(6).setCellValue("学生学校");
                row.createCell(7).setCellValue("学生地区");

                for (QueryMobileResult queryMobileResult : results) {
                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(queryMobileResult.getUserId());
                    row.createCell(1).setCellValue(queryMobileResult.getMobile());
                    row.createCell(2).setCellValue(queryMobileResult.getUserName() != null ? queryMobileResult.getUserName() : "");
                    row.createCell(3).setCellValue(queryMobileResult.getClazzLevel() != null ? queryMobileResult.getClazzLevel().toString() : "");
                    row.createCell(4).setCellValue(queryMobileResult.getClazzName() != null ? queryMobileResult.getClazzName() : "");
                    row.createCell(5).setCellValue(queryMobileResult.getSchoolId() != null ? queryMobileResult.getSchoolId().toString() : "");
                    row.createCell(6).setCellValue(queryMobileResult.getSchoolName() != null ? queryMobileResult.getSchoolName() : "");
                    row.createCell(7).setCellValue(queryMobileResult.getRegion() != null ? queryMobileResult.getRegion() : "");
                }

                try {
                    @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                    wb.write(outStream);
                    outStream.flush();
                    HttpRequestContextUtils.currentRequestContext().downloadFile(
                            "用户手机号信息.xlsx",
                            "application/vnd.ms-excel",
                            outStream.toByteArray());
                } catch (IOException ignored) {
                    try {
                        HttpServletResponse response = getResponse();
                        response.getWriter().write("不能下载");
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    } catch (IOException e) {
                        logger.error("download cheating teacher list exception!");
                    }
                }
            }
        }
    }
}
