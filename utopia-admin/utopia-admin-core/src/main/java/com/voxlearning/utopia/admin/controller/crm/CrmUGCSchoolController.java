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

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext;
import com.voxlearning.utopia.admin.service.crm.CrmUGCSchoolService;
import com.voxlearning.utopia.entity.ambassador.SchoolAmbassador;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCSchool;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCSchoolDetail;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCSchoolTask;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorSchoolRef;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.ambassador.client.SchoolAmbassadorServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.School;
import lombok.Cleanup;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zhuan liu
 * @since 2015/12/31.
 */
@Controller
@RequestMapping("/crm/ugc")
public class CrmUGCSchoolController extends CrmAbstractController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private SchoolAmbassadorServiceClient schoolAmbassadorServiceClient;
    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;

    @Inject
    private CrmUGCSchoolService crmUgcSchoolService;

    @RequestMapping(value = "schoolname_notonly.vpage")
    public String ugcSchoolNameNotOnly(Model model) {
        int triggerType = getRequestInt("trigger");
        Integer checkupStatus = requestInteger("authenticationstate");
        Boolean isTaskFinished = requestBoolean("isTaskFinished");
        Pageable pageable = buildPageRequest(10);
        Page<CrmUGCSchool> result = crmUgcSchoolService.crmUGCSchools(triggerType, checkupStatus, isTaskFinished, pageable);
        int page = getRequestInt("PAGE");
        model.addAttribute("page", page);
        model.addAttribute("ugcSchoolList", result);
        model.addAttribute("triggerType", triggerType);
        model.addAttribute("checkupStatus", checkupStatus);
        model.addAttribute("isTaskFinished", isTaskFinished);
        return "crm/ugc/schoolname_notonly";
    }

    @RequestMapping(value = "schoolname_detail.vpage")
    public String ugcSchoolDetailInfo(Model model) {
        Long schoolId = requestLong("schoolId");
        List<AmbassadorSchoolRef> refList = ambassadorLoaderClient.getAmbassadorLoader().findSchoolAmbassadorRefs(schoolId);
        List<Map<String, Object>> schoolAmbassadorList = new ArrayList<>();
        if (refList != null && !refList.isEmpty()) {
            for (AmbassadorSchoolRef ambassadorSchoolRef : refList) {
                SchoolAmbassador schoolAmbassador = schoolAmbassadorServiceClient.getSchoolAmbassadorService()
                        .loadSchoolAmbassadorByUserId(ambassadorSchoolRef.getAmbassadorId())
                        .getUninterruptibly();
                if (schoolAmbassador != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("isFx", schoolAmbassador.getIsFx());
                    map.put("provinceName", schoolAmbassador.getPname());
                    map.put("city", schoolAmbassador.getCname());
                    map.put("county", schoolAmbassador.getAname());
                    map.put("address", schoolAmbassador.getAddress());
                    map.put("schoolName", schoolAmbassador.getSchoolName());
                    map.put("userName", schoolAmbassador.getName());

                    //FIXME: 没必要获取手机号吧? 如果真的要获取, 改成用户详情页面的逻辑, 按需点击后查看
                    map.put("phone", "请查看用户" + schoolAmbassador.getUserId());

                    schoolAmbassadorList.add(map);
                }
            }
        }
        model.addAttribute("schoolAmbassadorList", schoolAmbassadorList);
        List<CrmUGCSchoolDetail> ugcSchoolDetail = crmUgcSchoolService.ugcSchoolDetail(schoolId);
        model.addAttribute("ugcSchoolDetailList", ugcSchoolDetail);
        School school = raikouSystem.loadSchool(schoolId);
        model.addAttribute("school", school);
        model.addAttribute("schoolId", schoolId);
        int page = getRequestInt("PAGE");
        model.addAttribute("trigger", requestInteger("trigger"));
        model.addAttribute("page", page);
        return "crm/ugc/schoolname_detail";
    }

    @RequestMapping(value = "updateSchoolShortName.vpage")
    public String updateUgcSchoolShortName() {
        Long schoolId = requestLong("schoolId");
        String shortName = requestString("shortName");
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        String actionUrl = ((AdminHttpRequestContext) HttpRequestContextUtils.currentRequestContext()).getRelativeUriPath();
        crmUgcSchoolService.updateSchoolShortName(schoolId, shortName, adminUser, actionUrl);
        String page = requestString("PAGE");
        return redirect("schoolname_notonly.vpage?trigger=0&PAGE=" + page);
    }

    @RequestMapping(value = "ugcSchoolCount.vpage")
    @ResponseBody
    public MapMessage getUgcSchoolCount() {
        MapMessage mapMessage = new MapMessage();
        long schoolCount = crmUgcSchoolService.getUgcSchoolCount();
        mapMessage.add("schoolCount", schoolCount);
        return mapMessage;
    }

    @RequestMapping(value = "ugcSchoolDetailCount.vpage")
    @ResponseBody
    public MapMessage getUgcSchoolDetailCount() {
        MapMessage mapMessage = new MapMessage();
        long schoolDetailCount = crmUgcSchoolService.getUgcSchoolDetailCount();
        mapMessage.add("schoolDetailCount", schoolDetailCount);
        return mapMessage;
    }

    @RequestMapping(value = "exportUgcSchoolData.vpage")
    public void exportUgcSchoolData() {

        int page = getRequestInt("exportUgcSchoolPage");
        int size = getRequestInt("everySize");

        XSSFWorkbook xssfWorkbook = getUgcSchoolSheets(size, page);
        try {
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            xssfWorkbook.write(out);
            out.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile("学校汇总信息.xlsx", "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("TaskRecord export Excp : {};", e);
        }

    }

    @RequestMapping(value = "exportUgcSchoolDetailData.vpage")
    public void exportUgcSchoolDetailData() {

        int page = getRequestInt("exportschoolDetailPage");
        int size = getRequestInt("everySize");

        XSSFWorkbook xssfWorkbook = getUgcSchoolDetailSheets(size, page);
        try {
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            xssfWorkbook.write(out);
            out.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile("学校明细信息.xlsx", "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("TaskRecord export Excp : {};", e);
        }

    }

    private XSSFWorkbook getUgcSchoolSheets(int size, int page) {

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        XSSFRow rowHead = sheet.createRow(0);
        rowHead.createCell(1).setCellValue("学校情况汇总表");
        XSSFRow firstRow = sheet.createRow(1);

        firstRow.createCell(0).setCellValue("触发类型");
        firstRow.createCell(1).setCellValue("学校Id");
        firstRow.createCell(2).setCellValue("系统学校全称");
        firstRow.createCell(3).setCellValue("系统学校简称");
        firstRow.createCell(4).setCellValue("省");
        firstRow.createCell(5).setCellValue("市");
        firstRow.createCell(6).setCellValue("区");
        firstRow.createCell(7).setCellValue("鉴定状态");
        firstRow.createCell(8).setCellValue("参与学生人数");
        firstRow.createCell(9).setCellValue("有效答案数");

        firstRow.createCell(10).setCellValue("ugc学校名");
        firstRow.createCell(11).setCellValue("ugc答案占比");
        firstRow.createCell(12).setCellValue("历史ugc答案");

        int rowNum = 2;
        int skip = page * size;
        List<CrmUGCSchool> crmUGCSchools = crmUgcSchoolService.allTriggerTypeSchoolExportData(size, skip);
        for (CrmUGCSchool crmUGCSchool : crmUGCSchools) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(crmUGCSchool.getTriggerType());
            row.createCell(1).setCellValue(crmUGCSchool.getSchoolId());
            School school = raikouSystem.loadSchool(crmUGCSchool.getSchoolId());
            String authStatus = "";//鉴定状态
            Cell cell2 = row.createCell(2);
            Cell cell3 = row.createCell(3);
            Cell cell4 = row.createCell(4);
            Cell cell5 = row.createCell(5);
            Cell cell6 = row.createCell(6);
            if (school != null) {
                authStatus = AuthenticationState.safeParse(school.getAuthenticationState()).getDescription();
                cell2.setCellValue(school.getCname());
                cell3.setCellValue(school.getShortName());
                if (school.getRegionCode() != null) {
                    ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
                    if (region != null) {
                        cell4.setCellValue(region.getProvinceName());
                        cell5.setCellValue(region.getCityName());
                        cell6.setCellValue(region.getCountyName());
                    }
                }
            }

            row.createCell(7).setCellValue(authStatus);
            row.createCell(8).setCellValue(cellValue(crmUGCSchool.getTotalstudentcount()));
            row.createCell(9).setCellValue(cellValue(crmUGCSchool.getJoinStudentCount()));
            row.createCell(10).setCellValue(cellValue(crmUGCSchool.getUgcSchoolName()).replace("NA", ""));
            row.createCell(11).setCellValue(percentFormat(crmUGCSchool.getUgcAnswerPercent()) + "%");
            row.createCell(12).setCellValue(cellValue(crmUGCSchool.getHistoryUgcSchoolName()).replace("NA", ""));
        }
        return workbook;
    }

    private static String cellValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String percentFormat(float data) {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(data * 100);
    }

    private XSSFWorkbook getUgcSchoolDetailSheets(int size, int page) {

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        XSSFRow rowHead = sheet.createRow(0);
        rowHead.createCell(1).setCellValue("答案详情表");

        XSSFRow firstRow = sheet.createRow(1);

        firstRow.createCell(0).setCellValue("学校ID");
        firstRow.createCell(1).setCellValue("学校全称");
        firstRow.createCell(2).setCellValue("学校简称");

        firstRow.createCell(3).setCellValue("省");
        firstRow.createCell(4).setCellValue("市");
        firstRow.createCell(5).setCellValue("区");
        firstRow.createCell(6).setCellValue("鉴定状态");
        firstRow.createCell(7).setCellValue("ugc答案");
        firstRow.createCell(8).setCellValue("系统处理答案");

        firstRow.createCell(9).setCellValue("参与回答人数");
        firstRow.createCell(10).setCellValue("有效答案数");
        firstRow.createCell(11).setCellValue("答案人数");
        firstRow.createCell(12).setCellValue("答案占比");

        int rowNum = 2;

        int skip = page * size;
        List<CrmUGCSchoolDetail> crmUGCSchoolDetailList = crmUgcSchoolService.allSchoolDetailExportData(size, skip);


        for (CrmUGCSchoolDetail crmUGCSchoolDetail : crmUGCSchoolDetailList) {

            XSSFRow row = sheet.createRow(rowNum++);

            School school = raikouSystem.loadSchool(crmUGCSchoolDetail.getSchoolId());
            row.createCell(0).setCellValue(crmUGCSchoolDetail.getSchoolId());

            String authStatus = "";//鉴定状态

            Cell cell1 = row.createCell(1);
            Cell cell2 = row.createCell(2);
            Cell cell3 = row.createCell(4);
            Cell cell4 = row.createCell(5);
            Cell cell5 = row.createCell(6);
            if (school != null) {
                authStatus = AuthenticationState.safeParse(school.getAuthenticationState()).getDescription();

                cell1.setCellValue(school.getCname());
                cell2.setCellValue(school.getShortName());
                if (school.getRegionCode() != null) {
                    ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
                    if (region != null) {
                        cell3.setCellValue(String.valueOf(region.getProvinceName()));
                        cell4.setCellValue(String.valueOf(region.getCityName()));
                        cell5.setCellValue(String.valueOf(region.getCountyName()));
                    }
                }
            }
            row.createCell(6).setCellValue(authStatus);
            row.createCell(7).setCellValue(cellValue(crmUGCSchoolDetail.getUgcSchoolName()).replace("NA", ""));
            row.createCell(8).setCellValue(cellValue(crmUGCSchoolDetail.getModUgcSchoolName()));
            row.createCell(9).setCellValue(cellValue(crmUGCSchoolDetail.getTotalCount()));
            row.createCell(10).setCellValue(cellValue(crmUGCSchoolDetail.getValidCount()));
            row.createCell(11).setCellValue(cellValue(crmUGCSchoolDetail.getCount()));
            row.createCell(12).setCellValue(percentFormat(crmUGCSchoolDetail.getPercentage()) + "%");

        }
        return workbook;
    }

    @RequestMapping(value = "dispatch_task.vpage")
    @ResponseBody
    public CrmUGCSchoolTask dispatchTask() {
        Long schoolId = requestLong("schoolId");
        Boolean branchSchool = getRequestBool("branchSchool");
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        return crmUgcSchoolService.dispatchUGCSchoolTask(schoolId, branchSchool, adminUser);
    }
}
