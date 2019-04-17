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

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.service.crm.CrmUGCGradeService;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCGrade;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCGradeDetail;
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
import java.util.List;

/**
 * @author Zhuan liu
 * @since 2016/1/18.
 */

@Controller
@RequestMapping("/crm/ugc")
public class CrmUGCGradeController extends CrmAbstractController {

    @Inject private RaikouSystem raikouSystem;
    @Inject CrmUGCGradeService crmUGCGradeService;

    @RequestMapping(value = "gradename_notonly.vpage")
    public String ugcGradeNameNotOnly(Model model) {
        int triggerType = getRequestInt("trigger");
        Pageable pageable = buildPageRequest(10);
        Page<CrmUGCGrade> gradeList = crmUGCGradeService.crmUGCGrades(triggerType, pageable);
        int page = getRequestInt("PAGE");
        model.addAttribute("page", page);
        model.addAttribute("ugcGradeList", gradeList);
        model.addAttribute("triggerType", triggerType);
        return "crm/ugc/gradename_notonly";
    }

    @RequestMapping(value = "gradename_detail.vpage")
    public String ugcGradeNameDetail(Model model) {

        Long schoolId = requestLong("schoolId");

        List<CrmUGCGradeDetail> detailList = crmUGCGradeService.getCrmUgcGradeDetail(schoolId);
        School school = raikouSystem.loadSchool(schoolId);
        model.addAttribute("school", school);
        model.addAttribute("schoolId", schoolId);
        model.addAttribute("ugcGradeDetailList", detailList);
        int page = getRequestInt("PAGE");
        model.addAttribute("page", page);
        model.addAttribute("sysgradename", crmUGCGradeService.getGradeName(schoolId));
        return "/crm/ugc/gradename_detail";
    }

    @RequestMapping(value = "updateugcgradename.vpage")
    public String updateUgcGradeName() {
        Long schoolId = requestLong("schoolId");
        String updatedUgcGradeName = requestString("updatedUgcGradeName");
        crmUGCGradeService.updateUgcGradeName(schoolId, updatedUgcGradeName);
        String page = requestString("PAGE");
        return redirect("gradename_notonly.vpage?trigger=0&PAGE=" + page);
    }

    @RequestMapping(value = "ugcGradeCount.vpage")
    @ResponseBody
    public MapMessage getUgcSchoolCount() {
        MapMessage mapMessage = new MapMessage();
        long schoolCount = crmUGCGradeService.getUgcGradeCount();
        mapMessage.add("gradeCount", schoolCount);
        return mapMessage;
    }

    @RequestMapping(value = "ugcGradeDetailCount.vpage")
    @ResponseBody
    public MapMessage getUgcSchoolDetailCount() {
        MapMessage mapMessage = new MapMessage();
        long schoolDetailCount = crmUGCGradeService.getUgcGradeDetailCount();
        mapMessage.add("gradeDetailCount", schoolDetailCount);
        return mapMessage;
    }

    @RequestMapping(value = "exportUgcGradeData.vpage")
    public void exportUgcSchoolData() {

        int page = getRequestInt("exportUgcGradePage");
        int size = getRequestInt("everySize");
        String fileName = "年级分布汇总信息.xlsx";
        XSSFWorkbook xssfWorkbook = getUgcGradeSheet(size, page);
        writeExcel(xssfWorkbook, fileName);

    }

    private void writeExcel(XSSFWorkbook xssfWorkbook, String fileName) {
        try {
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            xssfWorkbook.write(out);
            out.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("ugcgradeName export Excp : {};", e);
        }
    }

    @RequestMapping(value = "exportUgcGradeDetailData.vpage")
    public void exportUgcSchoolDetailData() {

        int page = getRequestInt("exportgradeDetailPage");
        int size = getRequestInt("everySize");

        XSSFWorkbook xssfWorkbook = getUgcSchoolDetailSheets(size, page);
        String fileName = "年级分布明细信息.xlsx";
        writeExcel(xssfWorkbook, fileName);

    }

    private XSSFWorkbook getUgcGradeSheet(int size, int page) {

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        XSSFRow rowHead = sheet.createRow(0);
        rowHead.createCell(1).setCellValue("年级分布汇总表");
        XSSFRow firstRow = sheet.createRow(1);

        firstRow.createCell(0).setCellValue("触发类型");
        firstRow.createCell(1).setCellValue("学校Id");
        firstRow.createCell(2).setCellValue("系统学校全称");
        firstRow.createCell(3).setCellValue("系统年级分布");
        firstRow.createCell(4).setCellValue("省");
        firstRow.createCell(5).setCellValue("市");
        firstRow.createCell(6).setCellValue("区");
        firstRow.createCell(7).setCellValue("参与学生人数");
        firstRow.createCell(8).setCellValue("有效学生人数");
        firstRow.createCell(9).setCellValue("ugc年级分布");

        firstRow.createCell(10).setCellValue("ugc答案占比");
        firstRow.createCell(11).setCellValue("历史ugc答案");

        int rowNum = 2;
        int skip = page * size;
        List<CrmUGCGrade> crmUGCGrades = crmUGCGradeService.allUgcGradeData(size, skip);
        for (CrmUGCGrade crmUGCGrade : crmUGCGrades) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(crmUGCGrade.getTriggerType());
            row.createCell(1).setCellValue(crmUGCGrade.getSchoolId());
            row.createCell(3).setCellValue(crmUGCGrade.getGradeName());
            School school = raikouSystem.loadSchool(crmUGCGrade.getSchoolId());
            Cell cell2 = row.createCell(2);
            Cell cell4 = row.createCell(4);
            Cell cell5 = row.createCell(5);
            Cell cell6 = row.createCell(6);

            if (school != null) {
                cell2.setCellValue(cellValue(school.getCname()));
                if (school.getRegionCode() != null) {
                    ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
                    if (region != null) {
                        cell4.setCellValue(region.getProvinceName());
                        cell5.setCellValue(region.getCityName());
                        cell6.setCellValue(region.getCountyName());
                    }
                }
            }

            row.createCell(7).setCellValue(cellValue(crmUGCGrade.getTotalStudentCount()));
            row.createCell(8).setCellValue(cellValue(crmUGCGrade.getValidStudentCount()));
            row.createCell(9).setCellValue(cellValue(crmUGCGrade.getUgcGradeNames()).replace("NULL", ""));
            row.createCell(10).setCellValue(percentFormat(crmUGCGrade.getUgcAnswerPercent()) + "%");
            row.createCell(11).setCellValue(cellValue(crmUGCGrade.getHistoryUgcGradeName()).replace("NULL", ""));
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
        rowHead.createCell(1).setCellValue("年级分布详情表");

        XSSFRow firstRow = sheet.createRow(1);

        firstRow.createCell(0).setCellValue("学校ID");
        firstRow.createCell(1).setCellValue("学校全称");

        firstRow.createCell(2).setCellValue("省");
        firstRow.createCell(3).setCellValue("市");
        firstRow.createCell(4).setCellValue("区");
        firstRow.createCell(5).setCellValue("ugc年级分布");
        firstRow.createCell(6).setCellValue("系统处理答案");

        firstRow.createCell(7).setCellValue("参与回答人数");
        firstRow.createCell(8).setCellValue("有效答案数");
        firstRow.createCell(9).setCellValue("答案人数");
        firstRow.createCell(10).setCellValue("答案占比");

        int rowNum = 2;

        int skip = page * size;
        List<CrmUGCGradeDetail> crmUGCGradeDetailList = crmUGCGradeService.allUgcGradeDetailData(size, skip);


        for (CrmUGCGradeDetail crmUGCGradeDetail : crmUGCGradeDetailList) {

            XSSFRow row = sheet.createRow(rowNum++);

            School school = raikouSystem.loadSchool(crmUGCGradeDetail.getSchoolId());
            row.createCell(0).setCellValue(crmUGCGradeDetail.getSchoolId());

            String authStatus = "";//鉴定状态

            Cell cell1 = row.createCell(1);
            Cell cell2 = row.createCell(2);
            Cell cell3 = row.createCell(3);
            Cell cell4 = row.createCell(4);
            if (school != null) {

                cell1.setCellValue(school.getCname());
                cell2.setCellValue(school.getShortName());
                if (school.getRegionCode() != null) {
                    ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
                    if (region != null) {
                        cell2.setCellValue(String.valueOf(region.getProvinceName()));
                        cell3.setCellValue(String.valueOf(region.getCityName()));
                        cell4.setCellValue(String.valueOf(region.getCountyName()));
                    }
                }
            }
            row.createCell(5).setCellValue(cellValue(crmUGCGradeDetail.getUgcGradeNames()).replace("NULL", ""));
            row.createCell(6).setCellValue(cellValue(crmUGCGradeDetail.getModUgcGradeName()));
            row.createCell(7).setCellValue(cellValue(crmUGCGradeDetail.getTotalCount()));
            row.createCell(8).setCellValue(cellValue(crmUGCGradeDetail.getValidCount()));
            row.createCell(9).setCellValue(cellValue(crmUGCGradeDetail.getCount()));
            row.createCell(10).setCellValue(percentFormat(crmUGCGradeDetail.getPercentage()) + "%");

        }
        return workbook;
    }
}
