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
import com.voxlearning.utopia.admin.service.crm.CrmUGCStudentCountService;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCStudentOfClass;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCStudentOfClassDetail;
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
 * @since 2016/1/25.
 */

@Controller
@RequestMapping(value = "crm/ugc")
public class CrmUGCStudentCountController extends CrmAbstractController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private CrmUGCStudentCountService crmUGCStudentCountService;

    @RequestMapping(value = "studentcount_notonly.vpage")
    public String crmUgcClassNameNotOnly(Model model) {

        int triggerType = getRequestInt("trigger");
        Pageable pageable = buildPageRequest(10);

        Page<CrmUGCStudentOfClass> ugcStudentCountList = crmUGCStudentCountService.crmUgcStudentCount(triggerType, pageable);
        int page = getRequestInt("PAGE");
        model.addAttribute("page", page);
        model.addAttribute("ugcStudentCountList", ugcStudentCountList);
        model.addAttribute("triggerType", triggerType);
        return "crm/ugc/studentcount_notonly";
    }

    @RequestMapping(value = "studentcount_detail.vpage")
    public String crmUgcClassNameDetail(Model model) {

        Long clazzId = getRequestLong("clazzId");
        Long schoolId = requestLong("schoolId");
        int page = getRequestInt("PAGE");
        List<CrmUGCStudentOfClassDetail> crmUGCStudentCountDetailList = crmUGCStudentCountService.crmUGCClassDetails(clazzId);


        Integer studentCount = crmUGCStudentCountService.studentCount(clazzId);
        School school = raikouSystem.loadSchool(schoolId);
        model.addAttribute("school", school);
        model.addAttribute("schoolId", schoolId);
        model.addAttribute("clazzId", clazzId);
        model.addAttribute("page", page);
        model.addAttribute("sysstudentcount", studentCount);
        model.addAttribute("ugcStudentCountDetailList", crmUGCStudentCountDetailList);
        return "crm/ugc/studentcount_detail";
    }

    @RequestMapping(value = "updateugcstudentcount.vpage")
    public String updateUgcStudentCount() {

        Long schoolId = requestLong("schoolId");
        Long clazzId = requestLong("clazzId");
        int page = getRequestInt("PAGE");
        String studentCount = requestString("updatedUgcStudentCount");
        crmUGCStudentCountService.updateUgcStudentCount(schoolId, clazzId, studentCount);
        return redirect("studentcount_notonly.vpage?trigger=0&PAGE=" + page);
    }

    @RequestMapping(value = "ugcStudentCount.vpage")
    @ResponseBody
    public MapMessage ugcClassNameCount() {
        MapMessage message = new MapMessage();
        long count = crmUGCStudentCountService.getCrmUgcStudentsCount();
        message.add("studentCount", count);
        return message;
    }

    @RequestMapping(value = "ugcStudentDetailCount.vpage")
    @ResponseBody
    public MapMessage ugcClassNameDetailCount() {
        MapMessage message = new MapMessage();
        long count = crmUGCStudentCountService.getCrmUgcStudentCountDetailCount();
        message.add("studentDetailCount", count);
        return message;
    }

    @RequestMapping(value = "exportUgcStudentCountData.vpage")
    public void exportUgcClassData() {
        int size = getRequestInt("everySize");
        int page = getRequestInt("exportUgcStudentCountPage");

        int skip = size * page;
        XSSFWorkbook xssfWorkbook = workbookClassNameData(size, skip);
        String fileName = "班级学生汇总情况.xlsx";
        writeExcel(xssfWorkbook, fileName);
    }

    @RequestMapping(value = "exportUgcStudentCountDetailData.vpage")
    public void exportUgcClassDetailData() {
        int size = getRequestInt("everySize");
        int page = getRequestInt("exportStudentCountDetailPage");

        int skip = size * page;
        XSSFWorkbook xssfWorkbook = workbookClassNameDetailData(size, skip);
        String fileName = "班级学生明细情况.xlsx";
        writeExcel(xssfWorkbook, fileName);
    }

    private void writeExcel(XSSFWorkbook xssfWorkbook, String fileName) {
        try {
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            xssfWorkbook.write(out);
            out.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("ugcStudentCount export Excp : {};", e);
        }
    }

    private XSSFWorkbook workbookClassNameDetailData(int size, int skip) {

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        XSSFRow rowHead = sheet.createRow(0);
        rowHead.createCell(1).setCellValue("班级学生详情表");

        XSSFRow firstRow = sheet.createRow(1);

        firstRow.createCell(0).setCellValue("学校ID");
        firstRow.createCell(1).setCellValue("学校全称");
        firstRow.createCell(2).setCellValue("ClazzId");
        firstRow.createCell(3).setCellValue("系统班级名称");
        firstRow.createCell(4).setCellValue("省");
        firstRow.createCell(5).setCellValue("市");
        firstRow.createCell(6).setCellValue("区");
        firstRow.createCell(7).setCellValue("ugc班级学生数量");
        firstRow.createCell(8).setCellValue("系统处理答案");

        firstRow.createCell(9).setCellValue("参与回答人数");
        firstRow.createCell(10).setCellValue("有效答案数");
        firstRow.createCell(11).setCellValue("答案人数");
        firstRow.createCell(12).setCellValue("答案占比");

        int rowNum = 2;

        List<CrmUGCStudentOfClassDetail> crmUGCClassDetailList = crmUGCStudentCountService.allCrmUgcClassDetailData(size, skip);


        for (CrmUGCStudentOfClassDetail crmUGCClassDetail : crmUGCClassDetailList) {

            XSSFRow row = sheet.createRow(rowNum++);

            School school = raikouSystem.loadSchool(crmUGCClassDetail.getSchoolId());
            row.createCell(0).setCellValue(crmUGCClassDetail.getSchoolId());
            row.createCell(2).setCellValue(cellValue(crmUGCClassDetail.getClazzId()));
            row.createCell(3).setCellValue(cellValue(crmUGCStudentCountService.assembleClassName(crmUGCClassDetail.getClazzId())));

            Cell cell1 = row.createCell(1);
            Cell cell2 = row.createCell(4);
            Cell cell3 = row.createCell(5);
            Cell cell4 = row.createCell(6);
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
            row.createCell(7).setCellValue(cellValue(crmUGCClassDetail.getUgcStudentCount()));
            row.createCell(8).setCellValue(cellValue(crmUGCClassDetail.getModUgcStudentCount()));
            row.createCell(9).setCellValue(cellValue(crmUGCClassDetail.getTotalCount()));
            row.createCell(10).setCellValue(cellValue(crmUGCClassDetail.getValidCount()));
            row.createCell(11).setCellValue(cellValue(crmUGCClassDetail.getCount()));
            row.createCell(12).setCellValue(percentFormat(crmUGCClassDetail.getPercentage()) + "%");

        }
        return workbook;

    }

    private XSSFWorkbook workbookClassNameData(int size, int skip) {
        List<CrmUGCStudentOfClass> crmUGCClassList = crmUGCStudentCountService.allCrmUgcStudentCountData(size, skip);

        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet sheet = xssfWorkbook.createSheet();
        XSSFRow headRow = sheet.createRow(0);
        headRow.createCell(0).setCellValue("班级学生数量汇总情况");

        XSSFRow firstRow = sheet.createRow(1);
        firstRow.createCell(0).setCellValue("");

        firstRow.createCell(0).setCellValue("触发类型");
        firstRow.createCell(1).setCellValue("学校Id");
        firstRow.createCell(2).setCellValue("系统学校全称");
        firstRow.createCell(3).setCellValue("系统班级名称");
        firstRow.createCell(4).setCellValue("ClazzId");

        firstRow.createCell(5).setCellValue("省");
        firstRow.createCell(6).setCellValue("市");
        firstRow.createCell(7).setCellValue("区");
        firstRow.createCell(8).setCellValue("参与学生人数");
        firstRow.createCell(9).setCellValue("有效学生人数");
        firstRow.createCell(10).setCellValue("ugc班级学生数量");

        firstRow.createCell(11).setCellValue("ugc答案占比");
        firstRow.createCell(12).setCellValue("历史ugc答案");

        int rowNum = 2;
        for (CrmUGCStudentOfClass crmUGCClass : crmUGCClassList) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(crmUGCClass.getTriggerType());
            row.createCell(1).setCellValue(crmUGCClass.getSchoolId());
            row.createCell(3).setCellValue(crmUGCClass.getClassName());
            row.createCell(4).setCellValue(cellValue(crmUGCClass.getClazzId()));

            School school = raikouSystem.loadSchool(crmUGCClass.getSchoolId());
            Cell cell2 = row.createCell(2);
            Cell cell4 = row.createCell(5);
            Cell cell5 = row.createCell(6);
            Cell cell6 = row.createCell(7);

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

            row.createCell(8).setCellValue(cellValue(crmUGCClass.getTotalStudentCount()));
            row.createCell(9).setCellValue(cellValue(crmUGCClass.getValidStudentCount()));
            row.createCell(10).setCellValue(cellValue(crmUGCClass.getUgcStudentCount()).replace("", ""));
            row.createCell(11).setCellValue(percentFormat(crmUGCClass.getUgcAnswerPercent()) + "%");
            row.createCell(12).setCellValue(cellValue(crmUGCClass.getHistoryUgcStudentCount()));

        }
        return xssfWorkbook;
    }

    private String cellValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String percentFormat(float data) {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(data * 100);
    }
}
