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
import com.voxlearning.utopia.admin.service.crm.CrmUGCTeacherService;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCTeacher;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCTeacherDetail;
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
 * @since 2016/1/28.
 */

@Controller
@RequestMapping("crm/ugc")
public class CrmUGCTeacherController extends CrmAbstractController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private CrmUGCTeacherService crmUgcTeacherService;

    @RequestMapping(value = "teachername_notonly.vpage")
    public String crmUgcTeacherNotOnly(Model model) {

        int triggerType = getRequestInt("trigger");
        int subjectTrigger = getRequestInt("subjectTrigger");

        Pageable pageable = buildPageRequest(10);

        Page<CrmUGCTeacher> ugcTeacherList = crmUgcTeacherService.crmUgcTeacheres(triggerType, subjectTrigger, pageable);
        int page = getRequestInt("PAGE");
        model.addAttribute("page", page);
        model.addAttribute("ugcTeacherList", ugcTeacherList);
        model.addAttribute("triggerType", triggerType);
        model.addAttribute("subject", subjectTrigger);

        return "crm/ugc/teachername_notonly";
    }

    @RequestMapping(value = "teachername_detail.vpage")
    public String crmUgcTeacherNameDetail(Model model) {

        Long clazzId = getRequestLong("clazzId");
        Long schoolId = requestLong("schoolId");
        String subject = getRequestString("subject");
        int page = getRequestInt("PAGE");
        List<CrmUGCTeacherDetail> crmUGCTeacherDetailList = crmUgcTeacherService.crmUGCTeacherDetails(clazzId);
        String sysTeacherName = crmUgcTeacherService.getTeacherName(clazzId, subject);
        School school = raikouSystem.loadSchool(schoolId);
        model.addAttribute("school", school);
        model.addAttribute("schoolId", schoolId);
        model.addAttribute("page", page);
        model.addAttribute("subject", subject);
        model.addAttribute("clazzId", clazzId);
        model.addAttribute("sysTeacherName", sysTeacherName);
        model.addAttribute("ugcTeacherDetailList", crmUGCTeacherDetailList);
        return "crm/ugc/teachername_detail";
    }

    @RequestMapping(value = "updateugcteachername.vpage")
    public String updateUgcTeacherName() {

        Long clazzId = getRequestLong("clazzId");
        Long schoolId = requestLong("schoolId");
        String subject = getRequestString("subject");
        int page = getRequestInt("PAGE");
        String ugcTeacherName = requestString("updatedUgcTeacherName");

        crmUgcTeacherService.updateUgcTeacherName(schoolId, clazzId, subject, ugcTeacherName);
        return redirect("teachername_notonly.vpage?trigger=0&PAGE=" + page);
    }

    @RequestMapping(value = "ugcTeacherCount.vpage")
    @ResponseBody
    public MapMessage ugcTeacherNameCount() {
        MapMessage message = new MapMessage();
        long count = crmUgcTeacherService.getCrmUgcTeacherCount();
        message.add("teacherCount", count);
        return message;
    }

    @RequestMapping(value = "ugcTeacherDetailCount.vpage")
    @ResponseBody
    public MapMessage ugcTeacherNameDetailCount() {
        MapMessage message = new MapMessage();
        long count = crmUgcTeacherService.getCrmUgcTeacherDetailCount();
        message.add("teacherDetailCount", count);
        return message;
    }

    @RequestMapping(value = "exportUgcTeacherData.vpage")
    public void exportUgcTeacherData() {
        int size = getRequestInt("everySize");
        int page = getRequestInt("exportUgcTeacherPage");

        int skip = size * page;
        XSSFWorkbook xssfWorkbook = workbookTeacherNameData(size, skip);
        String fileName = "老师名称汇总情况.xlsx";
        writeExcel(xssfWorkbook, fileName);
    }

    @RequestMapping(value = "exportUgcTeacherDetailData.vpage")
    public void exportUgcTeacherDetailData() {
        int size = getRequestInt("everySize");
        int page = getRequestInt("exportTeacherDetailPage");

        int skip = size * page;
        XSSFWorkbook xssfWorkbook = workbookTeacherNameDetailData(size, skip);
        String fileName = "老师名称明细情况.xlsx";
        writeExcel(xssfWorkbook, fileName);
    }

    private void writeExcel(XSSFWorkbook xssfWorkbook, String fileName) {
        try {
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            xssfWorkbook.write(out);
            out.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("ugcclassName export Excp : {};", e);
        }
    }

    private XSSFWorkbook workbookTeacherNameDetailData(int size, int skip) {

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        XSSFRow rowHead = sheet.createRow(0);
        rowHead.createCell(1).setCellValue("班级名称详情表");

        XSSFRow firstRow = sheet.createRow(1);

        firstRow.createCell(0).setCellValue("学校ID");
        firstRow.createCell(1).setCellValue("学校全称");
        firstRow.createCell(2).setCellValue("班级Id");
        firstRow.createCell(3).setCellValue("系统班级名称");
        firstRow.createCell(4).setCellValue("省");
        firstRow.createCell(5).setCellValue("市");
        firstRow.createCell(6).setCellValue("区");
        firstRow.createCell(7).setCellValue("ugc老师名称");
        firstRow.createCell(8).setCellValue("系统处理答案");

        firstRow.createCell(9).setCellValue("参与回答人数");
        firstRow.createCell(10).setCellValue("有效答案数");
        firstRow.createCell(11).setCellValue("答案人数");
        firstRow.createCell(12).setCellValue("答案占比");
        firstRow.createCell(13).setCellValue("科目");
        firstRow.createCell(14).setCellValue("系统老师名");


        int rowNum = 2;

        List<CrmUGCTeacherDetail> crmUGCTeacherDetailList = crmUgcTeacherService.allCrmUgcTeacherDetailData(size, skip);


        for (CrmUGCTeacherDetail crmUGCTeacherDetail : crmUGCTeacherDetailList) {

            XSSFRow row = sheet.createRow(rowNum++);

            School school = raikouSystem.loadSchool(crmUGCTeacherDetail.getSchoolId());
            row.createCell(0).setCellValue(crmUGCTeacherDetail.getSchoolId());
            row.createCell(2).setCellValue(cellValue(crmUGCTeacherDetail.getClazzId()));
            row.createCell(3).setCellValue(cellValue(crmUgcTeacherService.assembleClassName(crmUGCTeacherDetail.getClazzId())));

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
            row.createCell(7).setCellValue(cellValue(crmUGCTeacherDetail.getUgcTeacherName()).replace("NULL", ""));
            row.createCell(8).setCellValue(cellValue(crmUGCTeacherDetail.getModUgcTeacherName()));
            row.createCell(9).setCellValue(cellValue(crmUGCTeacherDetail.getTotalCount()));
            row.createCell(10).setCellValue(cellValue(crmUGCTeacherDetail.getValidCount()));
            row.createCell(11).setCellValue(cellValue(crmUGCTeacherDetail.getCount()));
            row.createCell(12).setCellValue(percentFormat(crmUGCTeacherDetail.getPercentage()) + "%");
            row.createCell(13).setCellValue(cellValue(crmUGCTeacherDetail.getSubject()).replace("NULL", ""));
            row.createCell(14).setCellValue(crmUgcTeacherService.getTeacherName(crmUGCTeacherDetail.getSchoolId(), crmUGCTeacherDetail.getSubject()));

        }
        return workbook;

    }

    private XSSFWorkbook workbookTeacherNameData(int size, int skip) {
        List<CrmUGCTeacher> crmUGCTeacherList = crmUgcTeacherService.allCrmUgcTeacherData(size, skip);

        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet sheet = xssfWorkbook.createSheet();
        XSSFRow headRow = sheet.createRow(0);
        headRow.createCell(0).setCellValue("老师名称汇总情况");

        XSSFRow firstRow = sheet.createRow(1);
        firstRow.createCell(0).setCellValue("");

        firstRow.createCell(0).setCellValue("触发类型");
        firstRow.createCell(1).setCellValue("学校Id");
        firstRow.createCell(2).setCellValue("系统学校全称");
        firstRow.createCell(3).setCellValue("系统班级名称");
        firstRow.createCell(4).setCellValue("班级Id");

        firstRow.createCell(5).setCellValue("省");
        firstRow.createCell(6).setCellValue("市");
        firstRow.createCell(7).setCellValue("区");
        firstRow.createCell(8).setCellValue("参与学生人数");
        firstRow.createCell(9).setCellValue("有效学生人数");
        firstRow.createCell(10).setCellValue("ugc老师名");

        firstRow.createCell(11).setCellValue("ugc答案占比");
        firstRow.createCell(12).setCellValue("历史ugc答案");
        firstRow.createCell(13).setCellValue("科目");
        firstRow.createCell(14).setCellValue("系统老师名");

        int rowNum = 2;
        for (CrmUGCTeacher crmUGCTeacher : crmUGCTeacherList) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(crmUGCTeacher.getTriggerType());
            row.createCell(1).setCellValue(crmUGCTeacher.getSchoolId());
            row.createCell(3).setCellValue(crmUGCTeacher.getClassName());
            row.createCell(4).setCellValue(cellValue(crmUGCTeacher.getClazzId()));

            School school = raikouSystem.loadSchool(crmUGCTeacher.getSchoolId());
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

            row.createCell(8).setCellValue(cellValue(crmUGCTeacher.getTotalStudentCount()));
            row.createCell(9).setCellValue(cellValue(crmUGCTeacher.getValidStudentCount()));
            row.createCell(10).setCellValue(cellValue(crmUGCTeacher.getUgcTeacherName()).replace("NULL", ""));
            row.createCell(11).setCellValue(percentFormat(crmUGCTeacher.getUgcAnswerPercent()) + "%");
            row.createCell(12).setCellValue(cellValue(crmUGCTeacher.getHistoryUgcTeacherName()).replace("NULL", ""));
            row.createCell(13).setCellValue(cellValue(crmUGCTeacher.getSubject()).replace("NULL", ""));
            row.createCell(14).setCellValue(cellValue(crmUgcTeacherService.getTeacherName(crmUGCTeacher.getClazzId(), crmUGCTeacher.getSubject())));

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
