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

package com.voxlearning.utopia.agent.controller.workspace;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.HssfUtils;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.bean.workrecord.WorkRecordData;
import com.voxlearning.utopia.agent.constants.Gender;
import com.voxlearning.utopia.agent.constants.ResearchersJobType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentResearchers;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.AgentResearchersService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.mobile.workrecord.AgentWorkRecordStatisticsService;
import com.voxlearning.utopia.service.crm.api.constants.agent.*;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmTeacherVisitInfo;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.crm.api.entities.agent.WorkRecordVisitUserInfo;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.CrmWorkRecordLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 工作记录下载
 * Created by Akex on 2015/11/10
 */
@Controller
@RequestMapping("/workspace/kpiinfo")
@Slf4j
public class WorkRecordDownloadController extends AbstractAgentController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private SchoolLoaderClient schoolLoaderClient;

    @Inject
    private WorkRecordService workRecordService;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentResearchersService agentResearchersService;
    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;

    @Inject
    private AgentWorkRecordStatisticsService agentWorkRecordStatisticsService;
    @Inject
    private CrmWorkRecordLoaderClient crmWorkRecordLoaderClient;


    @RequestMapping(value = "workrecord.vpage", method = RequestMethod.GET)
    public String workRecordIndex(Model model) {
        return "workspace/workrecord/index";
    }

    @RequestMapping(value = "downloadworkrecord.vpage", method = RequestMethod.POST)
    @OperationCode("aff082e49616472e")
    public void downloadWorkRecord(HttpServletResponse response) {
        try {
            Date startTime = getRequestDate("startDate");
            Date endTime = getRequestDate("endDate");
            String workRecordType = getRequestString("workRecordType");
            if (endTime == null) {
                endTime = new Date();
            } else {
                endTime = DateUtils.addDays(endTime, 1); // 包括选定的日期
            }

            if (startTime == null) {
                startTime = DateUtils.stringToDate(DateUtils.dateToString(DateUtils.addDays(endTime, -1), "yyyy-MM-dd"), "yyyy-MM-dd");
            }

            if (DateUtils.dayDiff(endTime, startTime) > 31) {
                startTime = DateUtils.stringToDate(DateUtils.dateToString(DateUtils.addDays(endTime, -31), "yyyy-MM-dd"), "yyyy-MM-dd");
            }

            CrmWorkRecordType crmWorkRecordType = CrmWorkRecordType.nameOf(workRecordType);
            if (null != crmWorkRecordType) {

                SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook();
                if (Objects.equals(crmWorkRecordType, CrmWorkRecordType.SCHOOL)) {
                    List<WorkRecordData> workRecords = workRecordService.loadRegionWorkRecordsNew(getCurrentUser(), Collections.singletonList(AgentWorkRecordType.SCHOOL), startTime, endTime);
                    convertSchoolRecordToWorkbook(sxssfWorkbook, workRecords);
                } else if (Objects.equals(crmWorkRecordType, CrmWorkRecordType.MEETING)) {
                    List<CrmWorkRecordType> types = new ArrayList<>();
                    types.add(CrmWorkRecordType.MEETING);
                    types.add(CrmWorkRecordType.JOIN_MEETING);
                    List<CrmWorkRecord> workRecords = workRecordService.loadRegionWorkRecords(getCurrentUser(), types, startTime, endTime);
                    convertMeetingRecordToWorkbook(sxssfWorkbook, workRecords);
                } else if (Objects.equals(crmWorkRecordType, CrmWorkRecordType.VISIT)) {
                    List<CrmWorkRecord> workRecords = workRecordService.loadRegionWorkRecords(getCurrentUser(), Collections.singletonList(CrmWorkRecordType.VISIT), startTime, endTime);
                    convertVisitRecordToWorkbook(sxssfWorkbook, workRecords);
                } else if (Objects.equals(crmWorkRecordType, CrmWorkRecordType.TEACHING)) {
                    List<CrmWorkRecord> workRecords = workRecordService.loadRegionWorkRecords(getCurrentUser(), Collections.singletonList(CrmWorkRecordType.TEACHING), startTime, endTime);
                    convertResearchersToWorkbook(sxssfWorkbook, workRecords);
                }
                String filename = "市场人员工作记录下载-" + crmWorkRecordType.getValue() + "-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xlsx";
                @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                sxssfWorkbook.write(outStream);
                outStream.flush();
                sxssfWorkbook.dispose();
                try {
                    HttpRequestContextUtils.currentRequestContext().downloadFile(
                            filename,
                            "application/vnd.ms-excel",
                            outStream.toByteArray());
                } catch (IOException ignored) {
                    response.getWriter().write("不能下载");
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
            }
        } catch (Exception ex) {
            log.error("下载失败!", ex.getMessage(), ex);
        }
    }

    private void convertSchoolRecordToWorkbook(Workbook workbook, List<WorkRecordData> workRecords) {        // 进校
        Sheet schoolSheet = workbook.createSheet("进校");
        schoolSheet.setColumnWidth(1, 5000);
        schoolSheet.setColumnWidth(2, 5000);
        schoolSheet.setColumnWidth(3, 4000);
        schoolSheet.setColumnWidth(4, 5000);
        schoolSheet.setColumnWidth(5, 5000);
        schoolSheet.setColumnWidth(6, 10000);
        schoolSheet.setColumnWidth(7, 4000);
        schoolSheet.setColumnWidth(8, 10000);
        schoolSheet.setColumnWidth(9, 5000);
        schoolSheet.setColumnWidth(10, 15000);
        schoolSheet.setColumnWidth(11, 5000);
        schoolSheet.setColumnWidth(12, 5000);
        schoolSheet.setColumnWidth(13, 5000);

        CellStyle borderStyle = workbook.createCellStyle();
        // 设置单元格边框样式
        borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
        borderStyle.setBorderTop(CellStyle.BORDER_THIN);
        borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
        borderStyle.setBorderRight(CellStyle.BORDER_THIN);

        Row firstRow = HssfUtils.createRow(schoolSheet, 0, 13, borderStyle);
        HssfUtils.setCellValue(firstRow, 0, borderStyle, "城市");
        HssfUtils.setCellValue(firstRow, 1, borderStyle, "主访人-角色");
        HssfUtils.setCellValue(firstRow, 2, borderStyle, "主访人");
        HssfUtils.setCellValue(firstRow, 3, borderStyle, "部门");
        HssfUtils.setCellValue(firstRow, 4, borderStyle, "拜访时间");
        HssfUtils.setCellValue(firstRow, 5, borderStyle, "学校");
        HssfUtils.setCellValue(firstRow, 6, borderStyle, "是否正常签到");
        HssfUtils.setCellValue(firstRow, 7, borderStyle, "非正常签到拍摄照片");
        HssfUtils.setCellValue(firstRow, 8, borderStyle, "拜访老师数量");

        HssfUtils.setCellValue(firstRow, 9, borderStyle, "单科/跨科");
        HssfUtils.setCellValue(firstRow, 10, borderStyle, "拜访英语老师数量");
        HssfUtils.setCellValue(firstRow, 11, borderStyle, "拜访数学老师数量");


        HssfUtils.setCellValue(firstRow, 12, borderStyle, "拜访老师姓名及ID");
        HssfUtils.setCellValue(firstRow, 13, borderStyle, "拜访其他角色人员数量");
        HssfUtils.setCellValue(firstRow, 14, borderStyle, "拜访主题");
        HssfUtils.setCellValue(firstRow, 15, borderStyle, "小学/中学/高中");
        HssfUtils.setCellValue(firstRow, 16, borderStyle, "代理商名称");

        Set<Long> allUserIds = workRecords.stream().map(WorkRecordData::getUserId).collect(Collectors.toSet());
//        Set<Long> partnerId = workRecords.stream().map(CrmWorkRecord::getPartnerId).collect(Collectors.toSet());
//        Set<Long> interviewerId = workRecords.stream().map(CrmWorkRecord::getInterviewerId).collect(Collectors.toSet());
//        allUserIds.addAll(partnerId);
//        allUserIds.addAll(interviewerId);

        Map<Long, AgentGroup> userGroupMap = new HashMap<>();
        allUserIds.forEach(p -> userGroupMap.put(p, baseOrgService.getUserGroupsFirstOne(p, null)));

        Map<Long, List<Integer>> allUserRoles = baseOrgService.getGroupUserRoleMapByUserIds(allUserIds);
        Set<Long> schoolIds = workRecords.stream().map(WorkRecordData::getSchoolId).collect(Collectors.toSet());
        Map<Long, School> schools = raikouSystem.loadSchools(schoolIds);
        List<WorkRecordData> schoolRecords = workRecords.stream().filter(p -> AgentWorkRecordType.SCHOOL == p.getWorkType()).collect(Collectors.toList());
        int rowNum = 1;
        if (CollectionUtils.isNotEmpty(schoolRecords)) {
            for (WorkRecordData workRecord : schoolRecords) {
                Row row = HssfUtils.createRow(schoolSheet, rowNum++, 13, borderStyle);

                String cityName = "";
                School school = schools.get(workRecord.getSchoolId());
                if (school != null) {
                    ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
                    if (exRegion != null) {
                        cityName = exRegion.getCityName();
                    }
                }
                HssfUtils.setCellValue(row, 0, borderStyle, cityName);
                // 主访人-角色
                HssfUtils.setCellValue(row, 1, borderStyle, StringUtils.join(createRolesNames(allUserRoles.get(workRecord.getUserId())), ","));
                HssfUtils.setCellValue(row, 2, borderStyle, (workRecord.getUserName()));
                // 部门
                AgentGroup group = userGroupMap.get(workRecord.getUserId());
                HssfUtils.setCellValue(row, 3, borderStyle, group != null && StringUtils.isNotBlank(group.getGroupName()) ? group.getGroupName() : "");

                HssfUtils.setCellValue(row, 4, borderStyle, workRecord.getWorkTime() == null ? "" : DateUtils.dateToString(workRecord.getWorkTime(), "yyyy-MM-dd HH:mm"));
                HssfUtils.setCellValue(row, 5, borderStyle, workRecord.getSchoolName());
                HssfUtils.setCellValue(row, 6, borderStyle, workRecord.getSignInType() != null && workRecord.getSignInType() == SignInType.GPS ? "正常签到" : "非正常签到");
                HssfUtils.setCellValue(row, 7, borderStyle, workRecord.getSignInType() != null && workRecord.getSignInType() == SignInType.PHOTO ? workRecord.getPhotoUrl() : "");
                int teacherCount = 0;
                int otherCount = 0;
                List<WorkRecordVisitUserInfo> teachers = workRecord.getVisitUserInfoList();
                if (CollectionUtils.isNotEmpty(teachers)) {
                    int sum = teachers.size();
                    teachers = teachers.stream().filter(p -> p.isRealTeacher()).collect(Collectors.toList());
                    teacherCount = teachers.size();
                    otherCount = sum - teacherCount;
                }
                HssfUtils.setCellValue(row, 8, borderStyle, teacherCount);

                Map<Subject, List<Long>> subjectTeacherMap = agentWorkRecordStatisticsService.getSubjectTeacherMapNew(workRecord);
                subjectTeacherMap.remove(Subject.UNKNOWN);

                HssfUtils.setCellValue(row, 9, borderStyle, subjectTeacherMap.size() > 1 ? "跨科" : "单科");

                HssfUtils.setCellValue(row, 10, borderStyle, subjectTeacherMap.get(Subject.ENGLISH) == null ? 0 : subjectTeacherMap.get(Subject.ENGLISH).size());

                HssfUtils.setCellValue(row, 11, borderStyle, subjectTeacherMap.get(Subject.MATH) == null ? 0 : subjectTeacherMap.get(Subject.MATH).size());

                if (CollectionUtils.isNotEmpty(teachers)) {
                    List<String> teachersInfo = new ArrayList<>();
                    teachers.forEach(item -> {
                        if (Objects.equals(item.getJob(), WorkRecordVisitUserInfo.TEACHER_JOB)) {
                            teachersInfo.add(item.getName() + item.getId());
                        } else if (item.getJob() == ResearchersJobType.UNREGISTERED_TEACHER.getJobId()) {
                            teachersInfo.add(item.getName() + "(未注册：" + item.getSubjectName() + "老师）");
                        }
                    });
                    HssfUtils.setCellValue(row, 12, borderStyle, StringUtils.join(teachersInfo, ";"));
                } else {
                    HssfUtils.setCellValue(row, 12, borderStyle, "");
                }
                HssfUtils.setCellValue(row, 13, borderStyle, otherCount);
                String workTitleId = workRecord.getWorkTitle();
                AgentSchoolWorkTitleType agentSchoolWorkTitleType = AgentSchoolWorkTitleType.of(workTitleId);
                HssfUtils.setCellValue(row, 14, borderStyle, agentSchoolWorkTitleType != null ? agentSchoolWorkTitleType.getWorkTitle() : "");
                HssfUtils.setCellValue(row, 15, borderStyle, schoolLevel(schools.get(workRecord.getSchoolId())));
                HssfUtils.setCellValue(row, 16, borderStyle, workRecord.getAgencyName());
            }
        }
    }

    private void convertMeetingRecordToWorkbook(Workbook workbook, List<CrmWorkRecord> meetingRecords) {

        // 组会

        CellStyle borderStyle = workbook.createCellStyle();
        // 设置单元格边框样式
        borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
        borderStyle.setBorderTop(CellStyle.BORDER_THIN);
        borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
        borderStyle.setBorderRight(CellStyle.BORDER_THIN);
        Sheet meetingSheet = workbook.createSheet("组会");
        meetingSheet.setColumnWidth(1, 5000);
        meetingSheet.setColumnWidth(2, 5000);
        meetingSheet.setColumnWidth(3, 4000);
        meetingSheet.setColumnWidth(4, 8000);
        meetingSheet.setColumnWidth(5, 5000);
        meetingSheet.setColumnWidth(6, 15000);
        meetingSheet.setColumnWidth(7, 5000);
        meetingSheet.setColumnWidth(8, 5000);
        meetingSheet.setColumnWidth(9, 5000);
        meetingSheet.setColumnWidth(10, 5000);
        meetingSheet.setColumnWidth(11, 5000);
        meetingSheet.setColumnWidth(12, 5000);
        meetingSheet.setColumnWidth(13, 5000);
        meetingSheet.setColumnWidth(14, 5000);
        meetingSheet.setColumnWidth(15, 5000);
        meetingSheet.setColumnWidth(16, 5000);
        meetingSheet.setColumnWidth(17, 5000);
        meetingSheet.setColumnWidth(18, 5000);

        Set<Long> allUserIds = meetingRecords.stream().map(CrmWorkRecord::getWorkerId).collect(Collectors.toSet());
        Set<Long> partnerId = meetingRecords.stream().map(CrmWorkRecord::getPartnerId).collect(Collectors.toSet());
        Set<Long> interviewerId = meetingRecords.stream().map(CrmWorkRecord::getInterviewerId).collect(Collectors.toSet());
        allUserIds.addAll(partnerId);
        allUserIds.addAll(interviewerId);
        Map<Long, AgentGroup> userGroupMap = new HashMap<>();
        allUserIds.forEach(p -> userGroupMap.put(p, baseOrgService.getUserGroupsFirstOne(p, null)));
        Map<Long, List<Integer>> allUserRoles = baseOrgService.getGroupUserRoleMapByUserIds(allUserIds);
        Row firstRow = HssfUtils.createRow(meetingSheet, 0, 17, borderStyle);
        HssfUtils.setCellValue(firstRow, 0, borderStyle, "角色");
        ;
        HssfUtils.setCellValue(firstRow, 1, borderStyle, "方式");

        HssfUtils.setCellValue(firstRow, 2, borderStyle, "姓名");
        HssfUtils.setCellValue(firstRow, 3, borderStyle, "部门");
        HssfUtils.setCellValue(firstRow, 4, borderStyle, "签到地点");
        HssfUtils.setCellValue(firstRow, 5, borderStyle, "组会日期");
        HssfUtils.setCellValue(firstRow, 6, borderStyle, "主题");
        HssfUtils.setCellValue(firstRow, 7, borderStyle, "学校名称");
        HssfUtils.setCellValue(firstRow, 8, borderStyle, "会议级别");
        HssfUtils.setCellValue(firstRow, 9, borderStyle, "宣讲时长");
        HssfUtils.setCellValue(firstRow, 10, borderStyle, "类型");
        HssfUtils.setCellValue(firstRow, 11, borderStyle, "代理商名字");
        HssfUtils.setCellValue(firstRow, 12, borderStyle, "教研员是否在场");
        HssfUtils.setCellValue(firstRow, 13, borderStyle, "现场照片");
        HssfUtils.setCellValue(firstRow, 14, borderStyle, "参加人数");
        HssfUtils.setCellValue(firstRow, 15, borderStyle, "讲师");
        HssfUtils.setCellValue(firstRow, 16, borderStyle, "教研员");
        HssfUtils.setCellValue(firstRow, 17, borderStyle, "联系方式");
        HssfUtils.setCellValue(firstRow, 18, borderStyle, "会议内容及效果");

        int rowNum = 1;
        if (CollectionUtils.isNotEmpty(meetingRecords))
            for (CrmWorkRecord item : meetingRecords) {
                Row row = HssfUtils.createRow(meetingSheet, rowNum++, 17, borderStyle);
                HssfUtils.setCellValue(row, 0, borderStyle, StringUtils.join(createRolesNames(allUserRoles.get(item.getWorkerId())), ","));
                CrmWorkRecordType workType = item.getWorkType();
                if (workType == CrmWorkRecordType.JOIN_MEETING) {
                    HssfUtils.setCellValue(row, 1, borderStyle, "参与");
                } else {
                    HssfUtils.setCellValue(row, 1, borderStyle, "组织");
                }

                HssfUtils.setCellValue(row, 2, borderStyle, item.getWorkerName());
                // 部门
                AgentGroup group = userGroupMap.get(item.getWorkerId());
                HssfUtils.setCellValue(row, 3, borderStyle, group != null && StringUtils.isNotBlank(group.getGroupName()) ? group.getGroupName() : "");
                HssfUtils.setCellValue(row, 4, borderStyle, item.getAddress());

                CrmWorkRecord workRecord;
                if (workType == CrmWorkRecordType.JOIN_MEETING) {
                    workRecord = crmWorkRecordLoaderClient.load(item.getSchoolWorkRecordId());
                } else {
                    workRecord = item;
                }
                HssfUtils.setCellValue(row, 5, borderStyle, workRecord.getWorkTime() == null ? "" : DateUtils.dateToString(workRecord.getWorkTime(), "yyyy-MM-dd HH:mm"));

                HssfUtils.setCellValue(row, 6, borderStyle, workRecord.getWorkTitle());

                HssfUtils.setCellValue(row, 7, borderStyle, workRecord.getSchoolName());


                HssfUtils.setCellValue(row, 8, borderStyle, workRecord.getMeetingType() != null ? workRecord.getMeetingType().getValue() : "");
                Integer meetTime = workRecord.getMeetingTime();
                HssfUtils.setCellValue(row, 9, borderStyle, meetTime == null ? "" : meetTime == 1 ? "小于15分钟" : meetTime == 2 ? "15-60分钟" : meetTime == 3 ? "大于1小时" : "");
                Integer showFrom = workRecord.getShowFrom();
                HssfUtils.setCellValue(row, 10, borderStyle, showFrom == null ? "" : showFrom == 1 ? "专场" : "插播");
                HssfUtils.setCellValue(row, 11, borderStyle, workRecord.getAgencyName());
                Boolean instructorAttend = workRecord.getInstructorAttend();
                HssfUtils.setCellValue(row, 12, borderStyle, instructorAttend == null ? "" : instructorAttend ? "是" : "否");
                String scenePhotoUrl = item.getScenePhotoUrl();
                HssfUtils.setCellValue(row, 13, borderStyle, scenePhotoUrl == null ? "" : scenePhotoUrl);
                int meeteeCount = 0;
                if (workRecord.getMeetingType() == CrmMeetingType.SCHOOL_LEVEL) {
                    List<CrmTeacherVisitInfo> teacherList = workRecord.getVisitTeacherList();
                    meeteeCount = null == teacherList ? 0 : teacherList.size();
                } else {
                    meeteeCount = null == workRecord.getMeeteeCount() ? 0 : workRecord.getMeeteeCount();
                }
                HssfUtils.setCellValue(row, 14, borderStyle, meeteeCount);
                HssfUtils.setCellValue(row, 15, borderStyle, workRecord.getMeetingNote());
                HssfUtils.setCellValue(row, 16, borderStyle, workRecord.getInstructorName());
                HssfUtils.setCellValue(row, 17, borderStyle, workRecord.getInstructorMobile());
                HssfUtils.setCellValue(row, 18, borderStyle, workRecord.getWorkContent());
            }
    }

    private void convertVisitRecordToWorkbook(Workbook workbook, List<CrmWorkRecord> visitRecords) {

        CellStyle borderStyle = workbook.createCellStyle();
        // 设置单元格边框样式
        borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
        borderStyle.setBorderTop(CellStyle.BORDER_THIN);
        borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
        borderStyle.setBorderRight(CellStyle.BORDER_THIN);
        // 陪访
        Sheet visitSheet = workbook.createSheet("陪访");
        visitSheet.setColumnWidth(1, 5000);
        visitSheet.setColumnWidth(2, 5000);
        visitSheet.setColumnWidth(3, 5000);
        visitSheet.setColumnWidth(4, 5000);
        visitSheet.setColumnWidth(5, 10000);
        visitSheet.setColumnWidth(6, 4000);
        visitSheet.setColumnWidth(7, 12000);
        visitSheet.setColumnWidth(8, 5000);
        visitSheet.setColumnWidth(9, 5000);
        visitSheet.setColumnWidth(10, 5000);
        visitSheet.setColumnWidth(11, 5000);
        visitSheet.setColumnWidth(12, 5000);
        visitSheet.setColumnWidth(13, 5000);
        visitSheet.setColumnWidth(14, 5000);
        visitSheet.setColumnWidth(15, 5000);
        visitSheet.setColumnWidth(16, 5000);


        Row firstRow = HssfUtils.createRow(visitSheet, 0, 15, borderStyle);
        HssfUtils.setCellValue(firstRow, 0, borderStyle, "城市");
        HssfUtils.setCellValue(firstRow, 1, borderStyle, "主访人-角色");
        HssfUtils.setCellValue(firstRow, 2, borderStyle, "主访人");
        HssfUtils.setCellValue(firstRow, 3, borderStyle, "陪访人-角色");
        HssfUtils.setCellValue(firstRow, 4, borderStyle, "陪访人");
        HssfUtils.setCellValue(firstRow, 5, borderStyle, "陪访人部门");
        HssfUtils.setCellValue(firstRow, 6, borderStyle, "学校");
        HssfUtils.setCellValue(firstRow, 7, borderStyle, "陪访时间");

        HssfUtils.setCellValue(firstRow, 8, borderStyle, "单科/跨科陪访");


        HssfUtils.setCellValue(firstRow, 9, borderStyle, "陪访目的");
        HssfUtils.setCellValue(firstRow, 10, borderStyle, "陪访建议");
        HssfUtils.setCellValue(firstRow, 11, borderStyle, "是否正常签到");
        HssfUtils.setCellValue(firstRow, 12, borderStyle, "非正常签到拍摄照片");
        HssfUtils.setCellValue(firstRow, 13, borderStyle, "小学/中学/高中");
        HssfUtils.setCellValue(firstRow, 14, borderStyle, "进校准备充分度评分");
        HssfUtils.setCellValue(firstRow, 15, borderStyle, "产品/话术熟悉度评分");
        HssfUtils.setCellValue(firstRow, 16, borderStyle, "结果符合预期度评分");

        Set<Long> allUserIds = visitRecords.stream().map(CrmWorkRecord::getWorkerId).collect(Collectors.toSet());
        Set<Long> partnerId = visitRecords.stream().map(CrmWorkRecord::getPartnerId).collect(Collectors.toSet());
        Set<Long> interviewerId = visitRecords.stream().map(CrmWorkRecord::getInterviewerId).collect(Collectors.toSet());
        allUserIds.addAll(partnerId);
        allUserIds.addAll(interviewerId);

        Map<Long, AgentGroup> userGroupMap = new HashMap<>();
        allUserIds.forEach(p -> userGroupMap.put(p, baseOrgService.getUserGroupsFirstOne(p, null)));

        Map<Long, List<Integer>> allUserRoles = baseOrgService.getGroupUserRoleMapByUserIds(allUserIds);
        Set<Long> schoolIds = visitRecords.stream().map(CrmWorkRecord::getSchoolId).collect(Collectors.toSet());
        Map<Long, School> schools = raikouSystem.loadSchools(schoolIds);
        int rowNum = 1;
        if (CollectionUtils.isNotEmpty(visitRecords)) {
            for (CrmWorkRecord workRecord : visitRecords) {
                Row row = HssfUtils.createRow(visitSheet, rowNum++, 15, borderStyle);
                HssfUtils.setCellValue(row, 0, borderStyle, workRecord.getCityName());
                // 角色
                HssfUtils.setCellValue(row, 1, borderStyle, StringUtils.join(createRolesNames(allUserRoles.get(workRecord.getInterviewerId())), ","));
                HssfUtils.setCellValue(row, 2, borderStyle, workRecord.getInterviewerName());
                // 角色 陪访人角色
                HssfUtils.setCellValue(row, 3, borderStyle, StringUtils.join(createRolesNames(allUserRoles.get(workRecord.getWorkerId())), ","));
                HssfUtils.setCellValue(row, 4, borderStyle, workRecord.getWorkerName());
                // 部门
                AgentGroup group = userGroupMap.get(workRecord.getWorkerId());
                HssfUtils.setCellValue(row, 5, borderStyle, group != null && StringUtils.isNotBlank(group.getGroupName()) ? group.getGroupName() : "");

                HssfUtils.setCellValue(row, 6, borderStyle, workRecord.getSchoolName());
                HssfUtils.setCellValue(row, 7, borderStyle, workRecord.getWorkTime() == null ? "" : DateUtils.dateToString(workRecord.getWorkTime(), "yyyy-MM-dd HH:mm"));//陪访
                CrmWorkRecord intoSchoolWorkInfo = workRecordService.getWorkInfo(workRecord.getSchoolWorkRecordId());
                if (null != intoSchoolWorkInfo) {
                    Map<Subject, List<Long>> subjectTeacherMap = agentWorkRecordStatisticsService.getSubjectTeacherMap(intoSchoolWorkInfo);
                    subjectTeacherMap.remove(Subject.UNKNOWN);
                    HssfUtils.setCellValue(row, 8, borderStyle, subjectTeacherMap.size() > 1 ? "跨科" : "单科");
                } else {
                    HssfUtils.setCellValue(row, 8, borderStyle, "");
                }

                String workTitle = workRecord.getWorkTitle();
                AgentVisitWorkTitleType agentVisitWorkTitleType = AgentVisitWorkTitleType.of(workTitle);
                HssfUtils.setCellValue(row, 9, borderStyle, agentVisitWorkTitleType != null ? agentVisitWorkTitleType.getWorkTitle() : "");
                HssfUtils.setCellValue(row, 10, borderStyle, workRecord.getPartnerSuggest());
                HssfUtils.setCellValue(row, 11, borderStyle, workRecord.getSignType() != null && workRecord.getSignType() == 1 ? "正常签到" : "非正常签到");
                HssfUtils.setCellValue(row, 12, borderStyle, workRecord.getSignType() != null && workRecord.getSignType() == 2 ? workRecord.getSchoolPhotoUrl() : "");
                HssfUtils.setCellValue(row, 13, borderStyle, schoolLevel(schools.get(workRecord.getSchoolId())));
                HssfUtils.setCellValue(row, 14, borderStyle, workRecord.getPreparationScore() == null ? "" : String.valueOf(workRecord.getPreparationScore()));
                HssfUtils.setCellValue(row, 15, borderStyle, workRecord.getProductProficiencyScore() == null ? "" : String.valueOf(workRecord.getProductProficiencyScore()));
                HssfUtils.setCellValue(row, 16, borderStyle, workRecord.getResultMeetExpectedResultScore() == null ? "" : String.valueOf(workRecord.getResultMeetExpectedResultScore()));
            }
        }
    }

    private void convertResearchersToWorkbook(Workbook workbook, List<CrmWorkRecord> teachingRecords) {

        CellStyle borderStyle = workbook.createCellStyle();
        // 设置单元格边框样式
        borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
        borderStyle.setBorderTop(CellStyle.BORDER_THIN);
        borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
        borderStyle.setBorderRight(CellStyle.BORDER_THIN);
        Sheet teachingSheet = workbook.createSheet("拜访教研员");
        teachingSheet.setColumnWidth(0, 5000);
        teachingSheet.setColumnWidth(1, 5000);
        teachingSheet.setColumnWidth(2, 5000);
        teachingSheet.setColumnWidth(3, 5000);
        teachingSheet.setColumnWidth(4, 5000);
        teachingSheet.setColumnWidth(5, 10000);
        teachingSheet.setColumnWidth(6, 4000);
        teachingSheet.setColumnWidth(7, 12000);
        teachingSheet.setColumnWidth(8, 5000);
        teachingSheet.setColumnWidth(9, 5000);
        teachingSheet.setColumnWidth(10, 5000);
        teachingSheet.setColumnWidth(11, 5000);
        teachingSheet.setColumnWidth(12, 5000);
        teachingSheet.setColumnWidth(13, 5000);
        teachingSheet.setColumnWidth(14, 5000);

        Row firstRow = HssfUtils.createRow(teachingSheet, 0, 14, borderStyle);
        HssfUtils.setCellValue(firstRow, 0, borderStyle, "拜访日期");
        HssfUtils.setCellValue(firstRow, 1, borderStyle, "部门");
        HssfUtils.setCellValue(firstRow, 2, borderStyle, "主访人");
        HssfUtils.setCellValue(firstRow, 3, borderStyle, "角色");
        HssfUtils.setCellValue(firstRow, 4, borderStyle, "拜访目的");
        HssfUtils.setCellValue(firstRow, 5, borderStyle, "地点");
        HssfUtils.setCellValue(firstRow, 6, borderStyle, "拜访过程");
        HssfUtils.setCellValue(firstRow, 7, borderStyle, "达成结果");
        HssfUtils.setCellValue(firstRow, 8, borderStyle, "拜访人员");
        HssfUtils.setCellValue(firstRow, 9, borderStyle, "教研员性别");
        HssfUtils.setCellValue(firstRow, 10, borderStyle, "电话");
        HssfUtils.setCellValue(firstRow, 11, borderStyle, "职务");
        HssfUtils.setCellValue(firstRow, 12, borderStyle, "管辖区域");
        HssfUtils.setCellValue(firstRow, 13, borderStyle, "科目");
        HssfUtils.setCellValue(firstRow, 14, borderStyle, "年级");


        Set<Long> allUserIds = teachingRecords.stream().map(CrmWorkRecord::getWorkerId).collect(Collectors.toSet());
        Set<Long> partnerId = teachingRecords.stream().map(CrmWorkRecord::getPartnerId).collect(Collectors.toSet());
        Set<Long> interviewerId = teachingRecords.stream().map(CrmWorkRecord::getInterviewerId).collect(Collectors.toSet());
        allUserIds.addAll(partnerId);
        allUserIds.addAll(interviewerId);

        Map<Long, AgentGroup> userGroupMap = new HashMap<>();
        allUserIds.forEach(p -> userGroupMap.put(p, baseOrgService.getUserGroupsFirstOne(p, null)));
        Map<Long, List<Integer>> allUserRoles = baseOrgService.getGroupUserRoleMapByUserIds(allUserIds);
        Set<Long> researchersId = teachingRecords.stream().map(CrmWorkRecord::getResearchersId).collect(Collectors.toSet());
        Map<Long, AgentResearchers> researchersMap = agentResearchersService.loadResearchers(researchersId);
        int rowNum = 1;
        if (CollectionUtils.isNotEmpty(teachingRecords)) {
            for (CrmWorkRecord workRecord : teachingRecords) {
                Row row = HssfUtils.createRow(teachingSheet, rowNum++, 14, borderStyle);
                HssfUtils.setCellValue(row, 0, borderStyle, DateUtils.dateToString(workRecord.getWorkTime(), "yyyy-MM-dd HH:mm"));
                AgentGroup userGroup = baseOrgService.getGroupFirstOne(workRecord.getWorkerId(), null);
                HssfUtils.setCellValue(row, 1, borderStyle, userGroup == null ? "" : userGroup.getGroupName());
                HssfUtils.setCellValue(row, 2, borderStyle, workRecord.getWorkerName());
                // 角色 陪访人角色
                HssfUtils.setCellValue(row, 3, borderStyle, StringUtils.join(createRolesNames(allUserRoles.get(workRecord.getWorkerId())), ","));
                VisitedResearchersIntention intention = VisitedResearchersIntention.typeOf(workRecord.getVisitedIntention());
                HssfUtils.setCellValue(row, 4, borderStyle, intention == null ? "" : intention.getDescribe());
                HssfUtils.setCellValue(row, 5, borderStyle, workRecord.getVisitedPlace());
                HssfUtils.setCellValue(row, 6, borderStyle, workRecord.getVisitedFlow());//过程
                HssfUtils.setCellValue(row, 7, borderStyle, workRecord.getVisitedConclusion());//结果
                AgentResearchers agentResearchers = researchersMap.get(workRecord.getResearchersId());
                if (agentResearchers == null) {
                    rowNum--;
                    continue;
                }
                HssfUtils.setCellValue(row, 8, borderStyle, agentResearchers.getName());
                Gender gender = Gender.typeOf(agentResearchers.getGender());
                HssfUtils.setCellValue(row, 9, borderStyle, gender == null ? "" : gender.getTypeName());
                HssfUtils.setCellValue(row, 10, borderStyle, agentResearchers.getPhone());
                ResearchersJobType jobType = ResearchersJobType.typeOf(agentResearchers.getJob());
                HssfUtils.setCellValue(row, 11, borderStyle, jobType == null ? "" : jobType.getJobName());
                String regionName = "";
                if (agentResearchers.getCounty() != null && agentResearchers.getCounty() != 0) {
                    ExRegion country = raikouSystem.loadRegion(agentResearchers.getCounty());
                    if (country != null) {
                        regionName = String.format("%s %s %s", country.getProvinceName(), country.getCityName(), country.getCountyName());
                    }
                } else if (agentResearchers.getCity() != null && agentResearchers.getCity() != 0) {
                    ExRegion city = raikouSystem.loadRegion(agentResearchers.getCity());
                    if (city != null) {
                        regionName = String.format("%s %s", city.getProvinceName(), city.getCityName());
                    }
                } else if (agentResearchers.getProvince() != null && agentResearchers.getProvince() != 0) {
                    ExRegion province = raikouSystem.loadRegion(agentResearchers.getProvince());
                    if (province != null) {
                        regionName = String.format("%s", province.getProvinceName());
                    }
                }

                HssfUtils.setCellValue(row, 12, borderStyle, regionName);
                Subject subjectType = agentResearchers.getSubject();
                HssfUtils.setCellValue(row, 13, borderStyle, subjectType.getValue());
                HssfUtils.setCellValue(row, 14, borderStyle, agentResearchers.getGrade());
            }
        }
    }

    private Set<String> createRolesNames(List<Integer> userRoles) {
        if (CollectionUtils.isEmpty(userRoles)) {
            return Collections.emptySet();
        }
        return userRoles.stream().map(AgentRoleType::of).filter(Objects::nonNull).map(AgentRoleType::getRoleName).collect(Collectors.toSet());
    }

    private String schoolLevel(School school) {
        if (school == null) {
            return "";
        }
        SchoolLevel schoolLevel = SchoolLevel.safeParse(school.getLevel());
        return schoolLevel == null ? "" : schoolLevel.getDescription();
    }

}
