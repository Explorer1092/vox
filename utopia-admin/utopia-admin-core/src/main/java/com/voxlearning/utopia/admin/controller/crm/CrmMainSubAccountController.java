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

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.dao.CrmMainSubAccountApplyDao;
import com.voxlearning.utopia.admin.util.HssfUtils;
import com.voxlearning.utopia.data.CrmMainSubApplyStatus;
import com.voxlearning.utopia.entity.crm.CrmMainSubAccountApply;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import lombok.Cleanup;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CRM包班制记录查询
 *
 * @author Yuechen Wang 2016-07-29
 */
@Controller
@RequestMapping("/crm/main_sub_account")
public class CrmMainSubAccountController extends CrmAbstractController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private CrmMainSubAccountApplyDao crmMainSubAccountApplyDao;

    // 任务列表
    @RequestMapping(value = "apply_list.vpage")
    public String applyList(Model model) {
        int curPage = getRequestInt("page");
        Long teacherId = getRequestLong("teacherId");
        String status = getRequestString("status");
        Date startDate = requestDate("start");
        Date endDate = requestDate("end");
        String applicant = getRequestString("applicant");
        String auditor = getRequestString("auditor");
        model.addAttribute("teacherId", getRequestString("teacherId"));
        model.addAttribute("status", status);
        model.addAttribute("applicant", applicant);
        model.addAttribute("auditor", auditor);

        if (startDate == null) {
            startDate = DayRange.current().getStartDate();
        }
        if (endDate == null) {
            endDate = DayRange.current().getEndDate();
        } else {
            endDate.setTime(endDate.getTime() + 86399 * 1000);
        }
        model.addAttribute("start", DateUtils.dateToString(startDate, DateUtils.FORMAT_SQL_DATE));
        model.addAttribute("end", DateUtils.dateToString(endDate, DateUtils.FORMAT_SQL_DATE));
        Pageable pageable = new PageRequest(curPage, 30);
        // teacherId 可能是副账号ID
        Long mainTeacher = teacherLoaderClient.loadMainTeacherId(teacherId);
        mainTeacher = mainTeacher == null ? teacherId : mainTeacher;
        Page<CrmMainSubAccountApply> applyPage = crmMainSubAccountApplyDao.findByPage(pageable, mainTeacher, status, startDate, endDate, applicant, auditor);
        model.addAttribute("applyList", applyPage.getContent().stream().map(this::mapApplyInfo).collect(Collectors.toList()));
        model.addAttribute("currentPage", curPage);
        model.addAttribute("totalPage", applyPage.getTotalPages());
        model.addAttribute("hasPrev", applyPage.hasPrevious());
        model.addAttribute("hasNext", applyPage.hasNext());
        return "crm/mainsubaccount/apply_list";
    }

    // 申请数据统计
    @RequestMapping(value = "apply_statistic.vpage", method = RequestMethod.GET)
    public String applyStatistic(Model model) {
        Date startDate = requestDate("start");
        Date endDate = requestDate("end");
        model.addAttribute("start", getRequestString("start"));
        model.addAttribute("end", getRequestString("end"));
        if (startDate != null && endDate != null) {
            endDate.setTime(endDate.getTime() + 86399 * 1000);
            List<CrmMainSubAccountApply> applyList = crmMainSubAccountApplyDao.findByPeriod(startDate, endDate);
            // 处理数据
            List<Map<String, Object>> applyData = processApplyData(applyList);
            model.addAttribute("recordList", applyData);
        }
        return "crm/mainsubaccount/apply_statistic";
    }

    @RequestMapping(value = "download_statistic.vpage", method = RequestMethod.POST)
    public void downloadStatistic() {
        Date startDate = requestDate("start");
        Date endDate = requestDate("end");
        try {
            if (startDate != null && endDate != null) {
                endDate.setTime(endDate.getTime() + 86399 * 1000);
                List<CrmMainSubAccountApply> applyList = crmMainSubAccountApplyDao.findByPeriod(startDate, endDate);
                // 处理数据
                List<Map<String, Object>> applyData = processApplyData(applyList);

                HSSFWorkbook hssfWorkbook = convertDataToHSSfWorkbook(applyData);
                String filename = StringUtils.formatMessage("包班制统计数据({}-{}).xls",
                        DateUtils.dateToString(startDate, "yyyyMMdd"),
                        DateUtils.dateToString(endDate, "yyyyMMdd")
                );
                @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                hssfWorkbook.write(outStream);
                outStream.flush();

                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        filename,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } else {
                getAlertMessageManager().addMessageError("请选择日期区间");
            }
        } catch (Exception ex) {
            logger.error("下载失败:" + ex.getMessage(), ex);
            getAlertMessageManager().addMessageError("下载失败");
        }
    }

    private Map<String, Object> mapApplyInfo(CrmMainSubAccountApply apply) {
        Map<String, Object> infoMap = new HashMap<>();
        Long teacherId = apply.getTeacherId();
        infoMap.put("teacherId", apply.getTeacherId());
        infoMap.put("teacherName", apply.getTeacherName());

        infoMap.put("mainAccount", teacherId);
        infoMap.put("mainSubject", apply.getCurrentSubject().getValue());
        List<Long> subAccounts = teacherLoaderClient.loadSubTeacherIds(teacherId);
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(subAccounts);
        Long subAccount = teacherMap.values().stream().filter(t -> apply.getApplySubject().equals(t.getSubject())).map(Teacher::getId).findFirst().orElse(null);
        infoMap.put("subAccounts", subAccount);
        infoMap.put("applySubject", apply.getApplySubject().getValue());

        infoMap.put("agentUser", apply.getApplicantName());
        infoMap.put("agentUid", apply.getApplicantId());
        infoMap.put("clazzId", apply.getClazzId());
        infoMap.put("clazzName", apply.getClazzName());

        infoMap.put("createTime", DateUtils.dateToString(apply.getCreateTime(), DateUtils.FORMAT_SQL_DATETIME));
        infoMap.put("auditor", apply.getAuditor());
        infoMap.put("auditorName", apply.getAuditorName());
        infoMap.put("auditTime", DateUtils.dateToString(apply.getAuditTime(), DateUtils.FORMAT_SQL_DATETIME));
        infoMap.put("auditNote", apply.getAuditNote());
        infoMap.put("status", apply.getAuditStatus().name());

        return infoMap;
    }

    private List<Map<String, Object>> processApplyData(List<CrmMainSubAccountApply> applyData) {
        List<Map<String, Object>> result = new ArrayList<>();
        // 以学校为主体查询地区信息
        Map<Long, List<CrmMainSubAccountApply>> schoolDataMap = applyData.stream()
                .filter(t -> t.getSchoolId() != null)
                .collect(Collectors.groupingBy(CrmMainSubAccountApply::getSchoolId));

        // 根据regionCode分组
        Map<Integer, List<School>> regionSchools = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolDataMap.keySet())
                .getUninterruptibly()
                .values()
                .stream().collect(Collectors.groupingBy(School::getRegionCode));

        for (Map.Entry<Integer, List<School>> entry : regionSchools.entrySet()) {
            Map<String, Object> infoMap = new HashMap<>();

            Integer regionCode = entry.getKey();
            ExRegion region = raikouSystem.loadRegion(regionCode);
            infoMap.put("provName", region == null ? regionCode : region.getProvinceName());
            infoMap.put("cityName", region == null ? regionCode : region.getCityName());
            infoMap.put("countyName", region == null ? regionCode : region.getCountyName());

            List<CrmMainSubAccountApply> dataList = new ArrayList<>();
            entry.getValue().forEach(school -> dataList.addAll(schoolDataMap.get(school.getId())));
            infoMap.put("applyCnt", dataList.size());
            int successCnt = 0;
            Map<String, Integer> keyCount = new HashMap<>();
            for (CrmMainSubAccountApply apply : dataList) {
                if (!CrmMainSubApplyStatus.APPROVED.equals(apply.getAuditStatus())) continue;
                successCnt++;
                if (apply.getCurrentSubject() == null || apply.getApplySubject() == null) continue;
                String key = apply.getCurrentSubject().name() + "_" + apply.getApplySubject().name();
                if (!keyCount.containsKey(key)) {
                    keyCount.put(key, 1);
                } else {
                    keyCount.put(key, keyCount.get(key) + 1);
                }
            }
            infoMap.put("successCnt", successCnt);
            infoMap.put("eng2mat", keyCount.get("ENGLISH_MATH"));
            infoMap.put("eng2chn", keyCount.get("ENGLISH_CHINESE"));
            infoMap.put("chn2mat", keyCount.get("CHINESE_MATH"));
            infoMap.put("chn2eng", keyCount.get("CHINESE_ENGLISH"));
            infoMap.put("mat2chn", keyCount.get("MATH_CHINESE"));
            infoMap.put("mat2eng", keyCount.get("MATH_ENGLISH"));
            result.add(infoMap);
        }
        return result;
    }

    private HSSFWorkbook convertDataToHSSfWorkbook(List<Map<String, Object>> applyData) {
        String[] dataTitle = new String[]{
                "省", "市", "区",
                "申请总数量", "申请通过数",
                "英语→数学", "英语→语文",
                "语文→数学", "语文→英语",
                "数学→英语", "数学→语文",
        };
        int[] dataWidth = new int[]{
                5000, 5000, 5000,
                5000, 5000,
                4000, 4000,
                4000, 4000,
                4000, 4000
        };
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        // 文本单元格边框样式
        HSSFCellStyle stringStyle = hssfWorkbook.createCellStyle();
        stringStyle.setBorderBottom(CellStyle.BORDER_THIN);
        stringStyle.setBorderTop(CellStyle.BORDER_THIN);
        stringStyle.setBorderLeft(CellStyle.BORDER_THIN);
        stringStyle.setBorderRight(CellStyle.BORDER_THIN);
        // 数字单元格边框样式
        HSSFCellStyle numberStyle = hssfWorkbook.createCellStyle();
        numberStyle.setBorderBottom(CellStyle.BORDER_THIN);
        numberStyle.setBorderTop(CellStyle.BORDER_THIN);
        numberStyle.setBorderLeft(CellStyle.BORDER_THIN);
        numberStyle.setBorderRight(CellStyle.BORDER_THIN);
        numberStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));

        HSSFSheet hssfSheet = hssfWorkbook.createSheet();
        HSSFRow firstRow = HssfUtils.createRow(hssfSheet, 0, 9, stringStyle);
        for (int i = 0; i < dataTitle.length; ++i) {
            hssfSheet.setColumnWidth(i, dataWidth[i]);
            HssfUtils.setCellValue(firstRow, i, stringStyle, dataTitle[i]);
        }

        int rowNum = 1;
        for (Map<String, Object> data : applyData) {
            HSSFRow row = HssfUtils.createRow(hssfSheet, rowNum++, 10, stringStyle);
            HssfUtils.setCellValue(row, 0, stringStyle, SafeConverter.toString(data.get("provName")));
            HssfUtils.setCellValue(row, 1, stringStyle, SafeConverter.toString(data.get("cityName")));
            HssfUtils.setCellValue(row, 2, stringStyle, SafeConverter.toString(data.get("countyName")));
            HssfUtils.setCellValue(row, 3, numberStyle, SafeConverter.toInt(data.get("applyCnt")));
            HssfUtils.setCellValue(row, 4, numberStyle, SafeConverter.toInt(data.get("successCnt")));
            HssfUtils.setCellValue(row, 5, numberStyle, SafeConverter.toInt(data.get("eng2mat")));
            HssfUtils.setCellValue(row, 6, numberStyle, SafeConverter.toInt(data.get("eng2chn")));
            HssfUtils.setCellValue(row, 7, numberStyle, SafeConverter.toInt(data.get("chn2mat")));
            HssfUtils.setCellValue(row, 8, numberStyle, SafeConverter.toInt(data.get("chn2eng")));
            HssfUtils.setCellValue(row, 9, numberStyle, SafeConverter.toInt(data.get("mat2chn")));
            HssfUtils.setCellValue(row, 10, numberStyle, SafeConverter.toInt(data.get("mat2eng")));
        }
        return hssfWorkbook;
    }

}
