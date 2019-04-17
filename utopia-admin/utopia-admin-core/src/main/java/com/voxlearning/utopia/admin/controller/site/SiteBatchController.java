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

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.admin.service.site.SiteUserService;
import com.voxlearning.utopia.admin.support.WorkbookUtils;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.api.constant.CrmTeacherFakeValidationType;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.data.NeonatalClazz;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.homework.api.constant.CheatingTeacherStatus;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.integral.api.constants.CreditType;
import com.voxlearning.utopia.service.integral.api.entities.CreditHistory;
import com.voxlearning.utopia.service.integral.api.mapper.CreditChangeResult;
import com.voxlearning.utopia.service.integral.client.CreditServiceClient;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.newhomework.api.entity.PossibleCheatingHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.PossibleCheatingTeacher;
import com.voxlearning.utopia.service.privilege.client.BlackWhiteListManagerClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.ActivityType;
import com.voxlearning.utopia.service.user.api.constants.UserWebSource;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.ClassMapper;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryServiceClient;
import com.voxlearning.utopia.service.user.consumer.ParentServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by XiaoPeng.Yang on 15-4-30.
 */
@Controller
@RequestMapping("/site/batch")
public class SiteBatchController extends SiteAbstractController {

    private static final String EXAMPLE_PATH = "/config/templates/account_mobile.xlsx";

    @Inject private RaikouSDK raikouSDK;

    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private BlackWhiteListManagerClient blackWhiteListManagerClient;
    @Inject private SiteUserService siteUserService;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private CreditServiceClient creditServiceClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private CrmSummaryServiceClient crmSummaryServiceClient;
    @Inject private UserServiceClient userServiceClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private ParentServiceClient parentServiceClient;
    @Inject private EmailServiceClient emailServiceClient;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    String index() {
        return "site/batch/index";
    }

    //批量导入老师特殊属性
    @RequestMapping(value = "batchimportattr.vpage", method = RequestMethod.GET)
    String batchimportattr(Model model) {
        List<KeyValuePair<Integer, String>> activityTypeList = ActivityType.toKeyValuePairs();
        model.addAttribute("activityTypeList", activityTypeList);
        return "site/batch/batchimportattr";
    }

    @RequestMapping(value = "batchimportattr.vpage", method = RequestMethod.POST)
    String batchImportUserActivityType(@RequestParam String teacherIds,
                                       @RequestParam String activities,
                                       Model model) {
        if (StringUtils.isBlank(teacherIds) || StringUtils.isBlank(activities)) {
            getAlertMessageManager().addMessageError("参数错误");
            return "site/batch/batchimportattr";
        }
        String[] teacherArray = teacherIds.split("\\n");
        String[] keys = StringUtils.split(activities, ",");
        List<String> lstFailed = new ArrayList<>();
        for (String tid : teacherArray) {
            try {
                Long teacherId = ConversionUtils.toLong(tid);
                if (teacherId == 0) {
                    lstFailed.add(tid);
                    continue;
                }
                Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
                if (teacher == null) {
                    lstFailed.add(tid);
                    continue;
                }
                for (String key : keys) {
                    ActivityType activityType = ActivityType.parse(conversionService.convert(key, Integer.class));
                    if (activityType == null) {
                        continue;
                    }
                    blackWhiteListManagerClient.getBlackWhiteListManager().createUserBlackWhiteList(teacherId, activityType);
                }
            } catch (Exception ex) {
                logger.error("批量导入用户特殊属性异常:" + ex.getMessage());
                lstFailed.add(tid);
                continue;
            }
        }

        List<KeyValuePair<Integer, String>> activityTypeList = ActivityType.toKeyValuePairs();
        model.addAttribute("activityTypeList", activityTypeList);
        model.addAttribute("failedList", lstFailed);
        return "site/batch/batchimportattr";
    }

    //批量操作作弊老师
    @RequestMapping(value = "batcheditcheatingteacher.vpage", method = RequestMethod.GET)
    String batchEditCheatingTeacher(Model model) {
        return "site/batch/batcheditcheatingteacher";
    }

    @RequestMapping(value = "batcheditcheatingteacher.vpage", method = RequestMethod.POST)
    String batchEditCheatingTeacher(@RequestParam String teacherIds,
                                    @RequestParam String desc,
                                    @RequestParam String editType,
                                    Model model) {
        if (StringUtils.isBlank(teacherIds) || StringUtils.isBlank(desc) || StringUtils.isBlank(editType)) {
            getAlertMessageManager().addMessageError("参数错误");
            return "site/batch/batcheditcheatingteacher";
        }
        String[] teacherArray = teacherIds.split("\\n");
        List<String> lstFailed = new ArrayList<>();
        for (String tid : teacherArray) {
            try {
                Long teacherId = ConversionUtils.toLong(tid);
                if (teacherId == 0) {
                    lstFailed.add(tid);
                    continue;
                }
                Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
                if (teacher == null) {
                    lstFailed.add(tid);
                    continue;
                }
                if ("Delete".equals(editType)) {
                    PossibleCheatingTeacher possibleCheatingTeacher = newHomeworkLoaderClient.loadPossibleCheatingTeacherByTeacherId(teacherId);
                    if (possibleCheatingTeacher != null) {
                        newHomeworkServiceClient.disabledPossibleCheatingTeacherById(possibleCheatingTeacher.getId());
                    }
                } else if ("Save".equals(editType)) {
                    //同时添加作弊详细记录
                    PossibleCheatingHomework possibleCheatingHomework = new PossibleCheatingHomework();
                    possibleCheatingHomework.setTeacherId(teacherId);
                    possibleCheatingHomework.setReason("管理员" + getCurrentAdminUser().getAdminUserName() + "手动添加");
                    possibleCheatingHomework.setRecordOnly(true);
                    possibleCheatingHomework.setIsAddIntegral(false);
                    possibleCheatingHomework.setHomeworkId("");
                    possibleCheatingHomework.setHomeworkType(HomeworkType.UNKNOWN);
                    newHomeworkServiceClient.insertPossibleCheatingHomework(possibleCheatingHomework);

                    //添加黑名单记录
                    PossibleCheatingTeacher possibleCheatingTeacher = PossibleCheatingTeacher.newInstance(teacherId, CheatingTeacherStatus.BLACK, desc, new Date());
                    if (newHomeworkLoaderClient.loadPossibleCheatingTeacherByTeacherId(teacherId) == null) {
                        newHomeworkServiceClient.insertPossibleCheatingTeacher(possibleCheatingTeacher);
                    } else {
                        lstFailed.add(tid);
                    }
                } else if ("GoldDelete".equals(editType)) {
                    //金币已清零
                    PossibleCheatingTeacher possibleCheatingTeacher = newHomeworkLoaderClient.loadPossibleCheatingTeacherByTeacherId(teacherId);
                    if (possibleCheatingTeacher == null) {
                        lstFailed.add(tid);
                    } else {
                        //修改状态
                        newHomeworkServiceClient.updatePossibleCheatingTeacherStatus(possibleCheatingTeacher.getId(), CheatingTeacherStatus.GOLD_DELETE);
                    }
                } else if ("AuthDelete".equals(editType)) {
                    //认证已取消
                    PossibleCheatingTeacher possibleCheatingTeacher = newHomeworkLoaderClient.loadPossibleCheatingTeacherByTeacherId(teacherId);
                    if (possibleCheatingTeacher == null) {
                        lstFailed.add(tid);
                    } else {
                        //修改状态
                        newHomeworkServiceClient.updatePossibleCheatingTeacherStatus(possibleCheatingTeacher.getId(), CheatingTeacherStatus.AUTH_DELETE);
                    }
                }
            } catch (Exception ex) {
                logger.error("批量编辑作弊老师异常:" + ex.getMessage());
                lstFailed.add(tid);
            }
        }
        addAdminLog("管理员" + getCurrentAdminUser().getAdminUserName() + "批量编辑了作弊老师，操作:" + editType);
        model.addAttribute("failedList", lstFailed);
        return "site/batch/batcheditcheatingteacher";
    }

    // FIXME: 也许这个方法需要重写
    @RequestMapping(value = "exportcheating.vpage", method = RequestMethod.GET)
    public void downloadSchoolInfo(HttpServletResponse response) {
        String startDate = getRequestParameter("startDate", "");
        String endDate = getRequestParameter("endDate", "");
        String type = getRequestParameter("type", "");
        if (StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate) || StringUtils.isBlank(type)) {
            try {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException e) {
                logger.error("download cheating teacher list exception!");
            }
        }
        DateRange range = new DateRange(DateUtils.stringToDate(startDate + " 00:00:00"), DateUtils.stringToDate(endDate + " 23:59:59"));
        CheatingTeacherStatus status = CheatingTeacherStatus.valueOf(type);
        XSSFWorkbook xssfWorkbook;
        if (CheatingTeacherStatus.BLACK == status) {
            //作弊详细历史
            List<PossibleCheatingHomework> homeworks = newHomeworkLoaderClient.findPossibleCheatingHomeworkListByDateRange(range);
            xssfWorkbook = convertToXSSFB(homeworks);
        } else {
            List<PossibleCheatingTeacher> teachers = newHomeworkLoaderClient.findPossibleCheatingTeacherListByDateRangeAndStatus(range, status);
            xssfWorkbook = convertToXSSFW(teachers);
        }
        String filename = type + "-" + DateUtils.dateToString(new Date()) + ".xlsx";
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
                logger.error("download cheating teacher list exception!");
            }
        }
    }

    @RequestMapping(value = "exportallcheatingteacher.vpage", method = RequestMethod.GET)
    public void exportAllCheatingTeacher(HttpServletResponse response) {
        XSSFWorkbook xssfWorkbook;
        List<PossibleCheatingTeacher> teachers = newHomeworkLoaderClient.loadAllCheatingTeacher();
        xssfWorkbook = convertToXSSFW(teachers);
        String filename = "act-" + DateUtils.dateToString(new Date()) + ".xlsx";
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
                logger.error("download all cheating teacher list exception!");
            }
        }
    }

    private XSSFWorkbook convertToXSSFB(List<PossibleCheatingHomework> dataList) {
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet xssfSheet = xssfWorkbook.createSheet();
        XSSFRow firstRow = xssfSheet.createRow(0);
        firstRow.setHeightInPoints(20);
        firstRow.createCell(0).setCellValue("老师ID");
        firstRow.createCell(1).setCellValue("作业ID");
        firstRow.createCell(2).setCellValue("作业类型");
        firstRow.createCell(3).setCellValue("学校");
        firstRow.createCell(4).setCellValue("作弊原因");
        firstRow.createCell(5).setCellValue("作弊时间");
        int rowNum = 1;
        List<Long> teacherIds = dataList.stream().map(PossibleCheatingHomework::getTeacherId).collect(Collectors.toList());
        Map<Long, School> schoolMap = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchools(teacherIds)
                .getUninterruptibly();
        for (PossibleCheatingHomework data : dataList) {
            XSSFRow xssfRow = xssfSheet.createRow(rowNum++);
            xssfRow.setHeightInPoints(20);
            xssfRow.createCell(0).setCellValue(data.getTeacherId());
            xssfRow.createCell(1).setCellValue(data.getHomeworkId());
            xssfRow.createCell(2).setCellValue(data.getHomeworkType().getDescription());
            xssfRow.createCell(3).setCellValue(schoolMap.get(data.getTeacherId()) == null ? "" : schoolMap.get(data.getTeacherId()).getCname());
            xssfRow.createCell(4).setCellValue(data.getReason());
            xssfRow.createCell(5).setCellValue(DateUtils.dateToString(data.getCreateDatetime(), "yyyy-MM-dd"));
        }
        for (int i = 0; i < 6; i++) {
            xssfSheet.setColumnWidth(i, 400 * 15);
        }
        return xssfWorkbook;
    }

    private XSSFWorkbook convertToXSSFW(List<PossibleCheatingTeacher> dataList) {
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet xssfSheet = xssfWorkbook.createSheet();
        XSSFRow firstRow = xssfSheet.createRow(0);
        firstRow.setHeightInPoints(20);
        firstRow.createCell(0).setCellValue("老师ID");
        firstRow.createCell(1).setCellValue("老师姓名");
        firstRow.createCell(2).setCellValue("学校");
        firstRow.createCell(3).setCellValue("状态");
        firstRow.createCell(4).setCellValue("描述");
        firstRow.createCell(5).setCellValue("第一次作弊时间");
        firstRow.createCell(6).setCellValue("最后一次作弊时间");
        firstRow.createCell(7).setCellValue("认证状态");
        int rowNum = 1;
        List<Long> teacherIds = dataList.stream().map(PossibleCheatingTeacher::getTeacherId).collect(Collectors.toList());
        Map<Long, School> schoolMap = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchools(teacherIds)
                .getUninterruptibly();
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIds);
        for (PossibleCheatingTeacher data : dataList) {
            XSSFRow xssfRow = xssfSheet.createRow(rowNum++);
            xssfRow.setHeightInPoints(20);
            xssfRow.createCell(0).setCellValue(data.getTeacherId());
            xssfRow.createCell(1).setCellValue(teacherMap.get(data.getTeacherId()) == null ? "" : teacherMap.get(data.getTeacherId()).fetchRealname());
            xssfRow.createCell(2).setCellValue(schoolMap.get(data.getTeacherId()) == null ? "" : schoolMap.get(data.getTeacherId()).getCname());
            xssfRow.createCell(3).setCellValue(data.getStatus().getDescription());
            xssfRow.createCell(4).setCellValue(data.getDesc());
            xssfRow.createCell(5).setCellValue(DateUtils.dateToString(data.getCreateDatetime()));
            xssfRow.createCell(6).setCellValue(data.getLastCheatDate() == null ? "" : DateUtils.dateToString(data.getLastCheatDate()));
            xssfRow.createCell(7).setCellValue(teacherMap.get(data.getTeacherId()) == null ? "老师不存在" : teacherMap.get(data.getTeacherId()).fetchCertificationState().getDescription());
        }
        for (int i = 0; i < 8; i++) {
            xssfSheet.setColumnWidth(i, 400 * 15);
        }
        return xssfWorkbook;
    }

    //批量操作作弊老师
    @RequestMapping(value = "batchfaketeachers.vpage", method = RequestMethod.GET)
    String batchFakeTeachers(Model model) {
        model.addAttribute("validationTypes", CrmTeacherFakeValidationType.values());
        return "site/batch/batchfaketeachers";
    }

    @RequestMapping(value = "batchfaketeachers.vpage", method = RequestMethod.POST)
    String batchFakeTeachers(@RequestParam String teacherIds,
                             @RequestParam String desc,
                             @RequestParam String validationTypeName,
                             Model model) {
//        getAlertMessageManager().addMessageError("功能已停用");
//        return "site/batch/batchfaketeachers";

        model.addAttribute("validationTypes", CrmTeacherFakeValidationType.values());

        if (StringUtils.isBlank(teacherIds) || StringUtils.isBlank(desc) || StringUtils.isBlank(validationTypeName)
                || CrmTeacherFakeValidationType.get(validationTypeName) == null) {
            getAlertMessageManager().addMessageError("参数错误");
            return "site/batch/batchfaketeachers";
        }

        String[] teacherArray = teacherIds.split("\\n");
        List<String> lstFailed = new ArrayList<>();
        for (String tid : teacherArray) {
            try {
                Long teacherId = ConversionUtils.toLong(tid);
                if (teacherId == 0) {
                    lstFailed.add(tid);
                    continue;
                }

                Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
                if (teacher == null) {
                    lstFailed.add(tid);
                    continue;
                }

                CrmTeacherFakeValidationType validationType = CrmTeacherFakeValidationType.get(validationTypeName);

                MapMessage message = crmSummaryServiceClient.updateTeacherFakeType(teacherId, validationType, desc);
                if (!message.isSuccess()) {
                    lstFailed.add(tid);
                    continue;
                }

                // 发送申诉消息
                miscServiceClient.sendFakeAppealMessage(teacherId);

                // 记录 UserServiceRecord
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(teacherId);
                userServiceRecord.setOperatorId("System");
                userServiceRecord.setOperationType(UserServiceRecordOperationType.老师判假.name());
                userServiceRecord.setOperationContent("老师判假");
                userServiceRecord.setComments(desc);
                userServiceClient.saveUserServiceRecord(userServiceRecord);

            } catch (Exception ex) {
                logger.error("批量判定为假老师异常:" + ex.getMessage());
                lstFailed.add(tid);
            }
        }

        model.addAttribute("failedList", lstFailed);
        return "site/batch/batchfaketeachers";
    }

    //批量操作作弊老师
    @RequestMapping(value = "batchexportteacherinvites.vpage", method = RequestMethod.GET)
    String batchExportTeacherInvites(Model model) {
        return "site/batch/batchexportteacherinvites";
    }

    @RequestMapping(value = "downloadteacherinvites.vpage", method = RequestMethod.POST)
    public String downloadStatInfo(@RequestParam String teacherIds, HttpServletResponse response) {
        if (StringUtils.isBlank(teacherIds)) {
            getAlertMessageManager().addMessageError("参数错误");
            return "site/batch/batchexportteacherinvites";
        }
        String[] teacherArray = teacherIds.split("\\n");
        List<Map<String, Object>> result = new ArrayList<>();
        for (String tid : teacherArray) {
            long teacherId = ConversionUtils.toLong(tid);
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
            //处理老师ID  先查找他的邀请人
            InviteHistory invitee = MiscUtils.firstElement(asyncInvitationServiceClient.loadByInvitee(teacherId).toList());
            Map<String, Object> inviteeMap = new HashMap<>();
            inviteeMap.put("teacherId", teacherId);
            if (invitee != null) {
                inviteeMap.put("inviteId", invitee.getUserId());
                inviteeMap.put("inviteType", invitee.getInvitationType() == null ? "" : invitee.getInvitationType().getDescription());
                inviteeMap.put("inviteDate", DateUtils.dateToString(invitee.getCreateTime()));
            }
            inviteeMap.put("schoolName", teacherDetail.getTeacherSchoolName());
            inviteeMap.put("authentication", teacherDetail.fetchCertificationState().getDescription());
            inviteeMap.put("isBlack", newHomeworkLoaderClient.loadPossibleCheatingTeacherByTeacherId(teacherId) != null);
            //查找我邀请的人 往下
            result.add(inviteeMap);
            result.addAll(getInviteData(teacherId));
        }
        XSSFWorkbook xssfWorkbook = convertToXSSI(result);
        String filename = "老师邀请关系" + "-" + DateUtils.dateToString(new Date()) + ".xlsx";
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
                logger.error("download cheating teacher list exception!");
            }
        }
        return "site/batch/batchexportteacherinvites";
    }

    // 虚拟学校

    // 因为有虚拟班级的需求，所以在这做个虚拟账号生成的页面
    // 这个页面不会对外暴露，需要俺们自己手动输入url才能访问
    // 访问地址：/site/batch/virtualschool.vpage
    @RequestMapping(value = "virtualschool.vpage", method = RequestMethod.GET)
    public String virtualSchool() {
        return "site/batch/virtualschool";
    }

    @RequestMapping(value = "createvirtualschool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createVirtualSchool() {
        return MapMessage.successMessage();
    }

    // 学校ID，学校名，老师姓名，学科，年级，班级，班级人数，姓名前缀，［学生姓名（可选）］
    @RequestMapping(value = "createvirtualteachers.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createVirtualTeachers() {
        String content = getRequestString("content");
        if (StringUtils.isBlank(content)) {
            return MapMessage.errorMessage("输入内容不能为空");
        }
        String[] rows = content.split("\\n");
        for (String row : rows) {
            String[] data = row.split("\\t");
            if (data.length != 8 && data.length != 9) {
                continue;
            }

            long schoolId = SafeConverter.toLong(data[0]);
            if (schoolId <= 0) {
                continue;
            }
            String schoolName = SafeConverter.toString(data[1]);
            if (StringUtils.isBlank(schoolName)) {
                continue;
            }

            String teacherName = SafeConverter.toString(data[2]);
            if (StringUtils.isBlank(teacherName)) {
                continue;
            }

            Subject teacherSubject = Subject.ofWithUnknown(SafeConverter.toString(data[3]));
            if (teacherSubject == Subject.UNKNOWN) {
                continue;
            }

            ClazzLevel clazzLevel = ClazzLevel.parse(SafeConverter.toInt(data[4]));
            if (clazzLevel == null) {
                continue;
            }

            String clazzName = SafeConverter.toString(data[5]);
            if (StringUtils.isBlank(clazzName)) {
                continue;
            }

            int studentNum = SafeConverter.toInt(data[6]);
            if (studentNum < 0) {
                continue;
            }

            String studentNamePrefix = SafeConverter.toString(data[7]);

            School school = schoolLoaderClient.getSchoolLoader()
                    .loadSchool(schoolId)
                    .getUninterruptibly();
            if (school.getType() != SchoolType.CONFIDENTIAL.getType() || !StringUtils.equals(school.getCname(), schoolName)) {
                continue;
            }

            // create teacher
            MapMessage mapMessage = createTeacher("123456", teacherName);
            if (mapMessage.isSuccess()) {
                User user = (User) mapMessage.get("user");
                mapMessage = teacherServiceClient.setTeacherSubjectSchool(user, teacherSubject, Ktwelve.of(school.getLevel()), schoolId);
                if (!mapMessage.isSuccess()) {
                    continue;
                }

                // get clazz
                Clazz clazz = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadSchoolClazzs(school.getId())
                        .enabled()
                        .clazzLevel(clazzLevel)
                        .toList()
                        .stream()
                        .filter(Clazz::isSystemClazz)
                        .filter(c -> StringUtils.equals(c.getClassName(), clazzName))
                        .findFirst()
                        .orElse(null);

                if (clazz == null) {
                    continue;
                }

                // create students
                List<String> studentNameList = new LinkedList<>();
                if (data.length == 8) {// auto generate student name
                    // 生成姓名
                    for (int j = 1; j <= studentNum; j++) {
                        studentNameList.add(studentNamePrefix + toChineseNum(SafeConverter.toString(j)));
                    }
                }

                MapMessage msg = clazzServiceClient.teacherJoinSystemClazzWithSource(user.getId(), clazz.getId(), studentNameList, UserWebSource.crm_batch.getSource());
                if (!msg.isSuccess()) {
                    return msg;
                }
            }
        }
        return MapMessage.successMessage();
    }

    @AllArgsConstructor
    @Getter
    private static class CreateVirtualStudentEntity {
        long teacherId;
        ClazzLevel grade;
        String clazzName;
        String stuName;
    }

    /**
     * 创建虚拟学生
     * 当班级不存在时创建班级并让老师加入到班级中
     * 分组:虚拟
     * 学生:虚拟
     * <p>
     * 老师ID,年级,班级,学生姓名
     *
     * @return
     */
    @RequestMapping(value = "createvirtualstudents.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createVirtualStudents() {
        String content = getRequestString("content");
        if (StringUtils.isBlank(content)) {
            return MapMessage.errorMessage("输入内容不能为空");
        }
        String[] rows = content.split("\\n");
        List<CreateVirtualStudentEntity> entities = new ArrayList<>();
        for (String row : rows) {
            String[] data = row.split("\\t");
            if (data.length != 4) {
                continue;
            }

            long teacherId = SafeConverter.toLong(data[0]);
            if (teacherId <= 0) {
                continue;
            }

            ClazzLevel clazzLevel = ClazzLevel.parse(SafeConverter.toInt(data[1]));
            if (clazzLevel == null) {
                continue;
            }

            String clazzName = SafeConverter.toString(data[2]);
            if (StringUtils.isBlank(clazzName)) {
                continue;
            }

            String studentName = SafeConverter.toString(data[3]);
            if (StringUtils.isEmpty(studentName)) {
                continue;
            }
            entities.add(new CreateVirtualStudentEntity(teacherId, clazzLevel, clazzName, studentName));
        }

        // 读取所有老师
        Set<Long> teacherIds = entities.stream().map(CreateVirtualStudentEntity::getTeacherId).collect(Collectors.toSet());
        Map<Long, Teacher> teachers = teacherLoaderClient.loadTeachers(teacherIds);
        // 读取老师所在学校
        Map<Long, School> teacherSchools = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchools(teacherIds)
                .getUninterruptibly();
        // 读取学校班级
        Set<Long> schoolIds = teacherSchools.values().stream().map(School::getId).collect(Collectors.toSet());
        Map<Long, List<Clazz>> schoolClazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolIds).toGroup(Clazz::getSchoolId);
        // 老师_班级 => 学生姓名
        Map<String, List<String>> teacherClazzStudentNamesMap = new HashMap<>();
        for (CreateVirtualStudentEntity entity : entities) {
            long teacherId = entity.getTeacherId();

            Teacher teacher = teachers.get(teacherId);
            if (teacher == null) {
                logger.error("no teacher found for {}", teacherId);
                continue;// 跳过该老师
            }
            School school = teacherSchools.get(teacherId);
            if (school == null) {
                logger.error("no school found for teacher {}", teacherId);
                continue;
            }

            // 读取行政班,如果没有该班级则创建一个
            List<Clazz> clazzs = schoolClazzs.get(school.getId());
            Clazz clazz = clazzs.stream()
                    .filter(c -> c.getClazzLevel() == entity.getGrade())
                    .filter(c -> StringUtils.equals(c.getClassName(), entity.getClazzName()))
                    .findFirst()
                    .orElse(null);
            Long clazzId;
            if (clazz == null) {
                ClassMapper classMapper = new ClassMapper();
                classMapper.setSchoolId(school.getId());
                classMapper.setClassLevel(ConversionUtils.toString(entity.getGrade().getLevel()));
                classMapper.setClazzName(entity.getClazzName());
                classMapper.setFreeJoin(Boolean.TRUE);
                classMapper.setEduSystem(schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly());
                MapMessage createSysClazzMessage = clazzServiceClient.createSystemClazz(Collections.singleton(classMapper));
                Collection<NeonatalClazz> neonatals = (Collection<NeonatalClazz>) createSysClazzMessage.get("neonatals");
                NeonatalClazz neonatalClazz = MiscUtils.firstElement(neonatals);
                clazzId = neonatalClazz.getClazzId();

                // 更新这个学校的班级数据
                schoolClazzs.put(school.getId(), raikouSDK.getClazzClient().getClazzLoaderClient().loadSchoolClazzs(school.getId()).toList());
            } else {
                clazzId = clazz.getId();
            }

            String key = teacherId + "_" + clazzId;
            List<String> studentNames = teacherClazzStudentNamesMap.get(key);
            if (studentNames == null) {
                studentNames = new ArrayList<>();
                teacherClazzStudentNamesMap.put(key, studentNames);
            }
            studentNames.add(entity.getStuName());
        }

        // 老师加入班级
        teacherClazzStudentNamesMap.forEach((k, v) -> {
            String[] teacherClazz = k.split("_");
            clazzServiceClient.teacherJoinSystemClazzWithVirtualAccounts(
                    SafeConverter.toLong(teacherClazz[0]), SafeConverter.toLong(teacherClazz[1]), v, UserWebSource.crm_batch.getSource());
        });


        return MapMessage.successMessage();
    }

    @RequestMapping(value = "deletevirtualschool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deletetVirtualSchool() {
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "deletevirtualteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteVirtualTeacher() {
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "batchaddstudentstoclazzpage.vpage", method = RequestMethod.GET)
    public String batchAddStudentsToGroupPage() {
        return "site/batch/batchaddstudentstoclazzpage";
    }

    @RequestMapping(value = "batchaddstudentstoclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchAddStudentsToClazz(@RequestParam String content) {
        if (StringUtils.isEmpty(content)) {
            return MapMessage.errorMessage("内容不能为空");
        }

        return siteUserService.batchAddStudentsToClazz(content);
    }

    @RequestMapping(value = "batchaddcreditindex.vpage", method = RequestMethod.GET)
    public String batchaddcreditIndex() {
        return "/site/batch/batchaddlearningcredit";
    }

    @RequestMapping(value = "batchaddcredit.vpage", method = RequestMethod.POST)
    public String batchaddcredit(Model model) {
        String content = getRequestString("batchAddCredit");
        if (StringUtils.isBlank(content)) {
            getAlertMessageManager().addMessageError("请输入添加学分的名单");
            return "/site/batch/batchaddlearningcredit";
        }

        List<StringBuffer> lstFailed = new ArrayList<>();

        String[] contents = content.split("\\r\\n");

        List<String> lstSuccess = new ArrayList<>();

        int totalRecord = contents.length;

        for (String m : contents) {
            String[] contextArray = m.trim().split("[\\s]+");
            int contextArrayLen = contextArray.length;
            if (contextArrayLen < 2 || contextArrayLen > 3) {
                StringBuffer errorMessage = new StringBuffer(m).append(" 存在错误数据或不完整数据");
                lstFailed.add(errorMessage);
                continue;
            }

            //按用户ID导入数据
            if (checkCreditDataByStudentId(lstFailed, m, contextArray)) continue;

            CreditHistory creditHistory = new CreditHistory();

            creditHistory.setUserId(SafeConverter.toLong(contextArray[0]));
            Integer amount = SafeConverter.toInt(contextArray[1]);
            creditHistory.setAmount(amount);
            if (amount > 0) {
                creditHistory.setType((CreditType.crm_modify_increase).getType());
            } else {
                creditHistory.setType((CreditType.crm_modify_decrease).getType());
            }
            if (contextArrayLen == 3) {
                creditHistory.setComment(contextArray[2]);
            } else {
                creditHistory.setComment("");
            }

            CreditChangeResult creditChangeResult = creditServiceClient.getCreditService().changeCredit(creditHistory);
            if (!creditChangeResult.getSuccess()) {
                StringBuffer errorMessage = new StringBuffer(m).append(" 存在错误数据或不完整数据");
                lstFailed.add(errorMessage);
            } else {
                lstSuccess.add(m);
            }
        }

        model.addAttribute("successlist", lstSuccess);
        model.addAttribute("failedlist", lstFailed);
        model.addAttribute("totalRecord", totalRecord);
        return "/site/batch/batchaddlearningcredit";
    }

    @RequestMapping(value = "queryuserlogin.vpage")
    public String queryUserLogin(Model model) {
        String userIds = getRequestString("userIds");

        List<Long> userIdSet = Stream.of(userIds.replaceAll("\\s", ",").split(","))
                .map(SafeConverter::toLong)
                .filter(id -> id > 0)
                .distinct()
                .sorted(Long::compare)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(userIdSet)) {
            Map<Long, UserLoginInfo> loginInfo = new HashMap<>();
            int slice = userIdSet.size() / 200 + 1; // 每次查询两百条吧
            CollectionUtils.splitList(userIdSet, slice).forEach(ids -> loginInfo.putAll(userLoginServiceClient.getUserLoginService().loadUserLoginInfo(userIdSet).getUninterruptibly()));

            // 整合
            List<Map<String, Object>> loginResult = new LinkedList<>();
            for (Long userId : userIdSet) {
                Map<String, Object> info = new HashMap<>();
                info.put("userId", userId);
                UserLoginInfo userLoginInfo = loginInfo.get(userId);
                Date loginDate = userLoginInfo == null ? null : userLoginInfo.getLoginTime();
                info.put("loginDate", loginDate);
                loginResult.add(info);
            }

            model.addAttribute("loginResult", loginResult);
        }
        model.addAttribute("userIds", userIds);
        return "/site/batch/queryuserlogin";
    }

    @RequestMapping(value = "createAccountIndex.vpage")
    public String createAccountIndex(Model model) {
        return "/site/batch/batchCreateAccount";
    }

    /**
     *
     */
    @RequestMapping(value = "downloadExample.vpage", method = RequestMethod.GET)
    public void downloadExample() {
        try {
            Resource resource = new ClassPathResource(EXAMPLE_PATH);
            if (!resource.exists()) {
                logger.error("example is not exists");
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            int bytesRead;
            byte[] buffer = new byte[8192];
            while ((bytesRead = in.read(buffer, 0, 8192)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            HttpRequestContextUtils.currentRequestContext().downloadFile("创建账号模板.xlsx",
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (Exception ex) {
            logger.error("download example is failed", ex);
        }
    }

    @RequestMapping(value = "batchCreateAccount.vpage")
    @ResponseBody
    public MapMessage batchCreateAccount(HttpServletRequest request) {
        MultiValueMap<String, MultipartFile> multiValuedMap = ((DefaultMultipartHttpServletRequest) request).getMultiFileMap();
        List<MultipartFile> multipartFiles = multiValuedMap.get("file");
        try {
            MultipartFile file = multipartFiles.get(0);
            if (file == null || file.isEmpty()) {
                logger.error("getRequestWorkbook - Empty MultipartFile with name['{}']");
                return null;
            }
            String fileName = file.getOriginalFilename();
            String fileExt = StringUtils.substringAfterLast(fileName, ".");
            fileExt = StringUtils.defaultString(fileExt).trim().toLowerCase();
            SupportedFileType fileType = SupportedFileType.valueOf(fileExt);
            if (SupportedFileType.xls != fileType && SupportedFileType.xlsx != fileType) {
                return MapMessage.errorMessage().add("info", "不是excel文件");
            }
            @Cleanup InputStream in = file.getInputStream();
            Workbook wb = WorkbookFactory.create(in);
            //获取excel中的内容，电话号码，输出excel，电话号码，家长ID，学生ID
            List<String[]> sheet1 = new LinkedList<>();
            List<String[]> sheet2 = new LinkedList<>();
            //每次3000条
            Sheet sheet = wb.getSheetAt(0);
            int length = sheet.getLastRowNum();
            if (length > 3001) {
                return MapMessage.errorMessage().add("info", "每次导入不能超过3000条记录");
            }
            for (int index = 1; index <= sheet.getLastRowNum(); index++) {
                try {
                    boolean isNew = true;
                    Row row = sheet.getRow(index);
                    if (Objects.isNull(row)) {
                        continue;
                    }
                    String[] exportAccount = new String[3];
                    String mobile = WorkbookUtils.getCellValue(row.getCell(0));
                    //根据电话号码创建家长账号，学生账号
                    if (StringUtils.isNotBlank(mobile)) {
                        exportAccount[0] = mobile;
                        Long parentId;
                        UserAuthentication parentAuthentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.PARENT);
                        if (Objects.isNull(parentAuthentication)) {
                            NeonatalUser neonatalUser = new NeonatalUser();
                            neonatalUser.setUserType(UserType.PARENT);
                            neonatalUser.setRoleType(RoleType.ROLE_PARENT);
                            String password = RandomUtils.randomString(6);
                            neonatalUser.setPassword(password);
                            neonatalUser.setMobile(mobile);
                            neonatalUser.setCode("17abzy");
                            neonatalUser.setWebSource(UserWebSource.crm_batch.getSource());
                            MapMessage result = userServiceClient.registerUser(neonatalUser);
                            User user = (User) result.get("user");
                            parentId = user.getId();
                        } else {
                            parentId = parentAuthentication.getId();
                            isNew = false;
                        }
                        exportAccount[1] = SafeConverter.toString(parentId);
                        Long studentId;
                        UserAuthentication studentAuthentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.STUDENT);
                        if (Objects.isNull(studentAuthentication)) {
                            NeonatalUser neonatalUser = new NeonatalUser();
                            neonatalUser.setUserType(UserType.STUDENT);
                            neonatalUser.setRoleType(RoleType.ROLE_STUDENT);
                            String password = RandomUtils.randomString(6);
                            neonatalUser.setPassword(password);
                            neonatalUser.setMobile(mobile);
                            neonatalUser.setCode("17abzy");
                            neonatalUser.setWebSource(UserWebSource.crm_batch.getSource());
                            MapMessage result = userServiceClient.registerUser(neonatalUser);
                            User user = (User) result.get("user");
                            studentId = user.getId();
                        } else {
                            studentId = studentAuthentication.getId();
                            isNew = false;
                        }
                        exportAccount[2] = SafeConverter.toString(studentId);
                        parentServiceClient.bindExistingParent(studentId, parentId, false, "");
                        if (isNew) {
                            sheet1.add(exportAccount);
                        } else {
                            sheet2.add(exportAccount);
                        }
                    }
                } catch (Exception e) {
                    logger.error("生成账号失败", e);
                }
            }
            String filePath = writeExcel(sheet1, sheet2);
            if (RuntimeMode.isProduction()) {
                emailServiceClient.createPlainEmail()
                        .to("li.zhang.d@17zuoye.com;weiwei.zhao@17zuoye.com;")
                        .cc("yong.liu@17zuoye.com")
                        .subject("批量根据手机号创建家长和学生账号")
                        .body("如果界面上没有弹出excel文件，可以使用此邮件（备用），生成的账号信息：https://oss-data.17zuoye.com/" + filePath)
                        .send();
            } else {
                emailServiceClient.createPlainEmail()
                        .to("yong.liu@17zuoye.com")
                        .subject("批量根据手机号创建家长和学生账号")
                        .body("如果界面上没有弹出excel文件，可以使用此邮件（备用），生成的账号信息：https://oss-data.17zuoye.com/" + filePath)
                        .send();
            }
            return MapMessage.successMessage().set("filePath", filePath).set("sheet1", sheet1).set("sheet2", sheet2);
        } catch (Exception e) {
            logger.error("导入电话号码失败", e);
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "/downReport.vpage", method = RequestMethod.GET)
    public void downReport(String filePath, String fileName) {
        try {
            InputStream inputStream = AdminOssManageUtils.downFile(filePath);
            getResponse().reset();
            getResponse().setContentType("application/vnd.ms-excel");
            getResponse().setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            OutputStream out = getResponse().getOutputStream();
            byte buffer[] = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            inputStream.close();
            out.close();

        } catch (Exception e) {
            logger.error("下载报告失败", e);
        }
    }

    @RequestMapping(value = "queryShippingAddressIndex.vpage")
    public String queryShippingAddressIndex(Model model) {
        return "/site/batch/queryShippingAddressIndex";
    }

    private String writeExcel(List<String[]> sheet1, List<String[]> sheet2) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheetNew = workbook.createSheet("新账号");
        XSSFRow topRow = sheetNew.createRow(0);
        String[] titles = new String[]{"手机号", "家长ID", "学生ID"};
        for (int i = 0; i < titles.length; i++) {
            String title = titles[i];
            XSSFCell topCell0 = topRow.createCell(i);
            topCell0.setCellValue(title);
        }
        for (int i = 0; i < sheet1.size(); i++) {
            String[] exportAccount = sheet1.get(i);
            XSSFRow dataRow = sheetNew.createRow(i + 1);
            XSSFCell dataCell0 = dataRow.createCell(0);
            dataCell0.setCellValue(SafeConverter.toString(exportAccount[0]));
            XSSFCell dataCell1 = dataRow.createCell(1);
            dataCell1.setCellValue(SafeConverter.toString(exportAccount[1]));
            XSSFCell dataCell2 = dataRow.createCell(2);
            dataCell2.setCellValue(SafeConverter.toString(exportAccount[2]));
        }

        XSSFSheet sheetOld = workbook.createSheet("已存在的账号");
        XSSFRow topRowOld = sheetOld.createRow(0);
        for (int i = 0; i < titles.length; i++) {
            String title = titles[i];
            XSSFCell topCell0 = topRowOld.createCell(i);
            topCell0.setCellValue(title);
        }

        for (int i = 0; i < sheet2.size(); i++) {
            String[] exportAccount = sheet2.get(i);
            XSSFRow dataRow = sheetOld.createRow(i + 1);
            XSSFCell dataCell0 = dataRow.createCell(0);
            dataCell0.setCellValue(SafeConverter.toString(exportAccount[0]));
            XSSFCell dataCell1 = dataRow.createCell(1);
            dataCell1.setCellValue(SafeConverter.toString(exportAccount[1]));
            XSSFCell dataCell2 = dataRow.createCell(2);
            dataCell2.setCellValue(SafeConverter.toString(exportAccount[2]));
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            workbook.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] content = os.toByteArray();
        InputStream is = new ByteArrayInputStream(content);
        String fileName = "gansuAccount";
        String filePath = null;
        try {
            filePath = AdminOssManageUtils.upload(is, content.length, fileName, "xlsx");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }

    /////////////////////////////////////Private Methods///////////////////////////////////

    // PS 暂时没想好放哪 先这样吧
    // 单位数组
    static String[] units = new String[]{"十", "百", "千", "万", "十", "百", "千", "亿"};
    // 中文大写数字数组
    static String[] numeric = new String[]{"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};

    private String toChineseNum(String num) {

        // 遍历一行中所有数字
        String res = "";
        for (int k = -1; num.length() > 0; k++) {
            // 解析最后一位
            int j = Integer.parseInt(num.substring(num.length() - 1, num.length()));
            String rtemp = numeric[j];

            // 数值不是0且不是个位 或者是万位或者是亿位 则去取单位
            if (j != 0 && k != -1 || k % 8 == 3 || k % 8 == 7) {
                rtemp += units[k % 8];
            }

            // 拼在之前的前面
            res = rtemp + res;

            // 去除最后一位
            num = num.substring(0, num.length() - 1);
        }

        // 去除后面连续的零零..
        while (res.endsWith(numeric[0])) {
            res = res.substring(0, res.lastIndexOf(numeric[0]));
        }

        // 将零零替换成零
        while (res.contains(numeric[0] + numeric[0])) {
            res = res.replaceAll(numeric[0] + numeric[0], numeric[0]);
        }

        // 将 零+某个单位 这样的窜替换成 该单位 去掉单位前面的零
        for (int m = 1; m < units.length; m++) {
            res = res.replaceAll(numeric[0] + units[m], units[m]);
        }

        return res;
    }

    private MapMessage createTeacher(String password, String realName) {
        NeonatalUser neonatalUser = new NeonatalUser();
        neonatalUser.setRoleType(RoleType.ROLE_TEACHER);
        neonatalUser.setUserType(UserType.TEACHER);
        neonatalUser.setPassword(password);
        neonatalUser.setRealname(realName);
        neonatalUser.attachPasswordState(PasswordState.AUTO_GEN);
        neonatalUser.setWebSource(UserWebSource.crm_batch.getSource());
        return userServiceClient.registerUserAndSendMessage(neonatalUser);
    }


    private List<Map<String, Object>> getInviteData(Long teacherId) {
        List<Map<String, Object>> dataMaps = new ArrayList<>();
        List<InviteHistory> histories = asyncInvitationServiceClient.loadByInviter(teacherId).toList();
        if (CollectionUtils.isEmpty(histories)) {
            return Collections.emptyList();
        }
        for (InviteHistory history : histories) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(history.getInviteeUserId());
            if (teacherDetail == null) {
                continue;
            }
            Map<String, Object> inviteeMap = new HashMap<>();
            inviteeMap.put("teacherId", teacherDetail.getId());
            inviteeMap.put("inviteId", teacherId);
            inviteeMap.put("schoolName", teacherDetail.getTeacherSchoolName());
            inviteeMap.put("authentication", teacherDetail.fetchCertificationState().getDescription());
            inviteeMap.put("isBlack", newHomeworkLoaderClient.loadPossibleCheatingTeacherByTeacherId(teacherDetail.getId()) != null);
            inviteeMap.put("inviteType", history.getInvitationType() == null ? "" : history.getInvitationType().getDescription());
            inviteeMap.put("inviteDate", DateUtils.dateToString(history.getCreateTime()));
            dataMaps.add(inviteeMap);

            List<Map<String, Object>> dataMap = getInviteData(history.getInviteeUserId());
            dataMaps.addAll(dataMap);
        }
        return dataMaps;
    }

    private XSSFWorkbook convertToXSSI(List<Map<String, Object>> dataList) {
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet xssfSheet = xssfWorkbook.createSheet();
        XSSFRow firstRow = xssfSheet.createRow(0);
        firstRow.setHeightInPoints(20);
        firstRow.createCell(0).setCellValue("老师ID");
        firstRow.createCell(1).setCellValue("原始邀请人");
        firstRow.createCell(2).setCellValue("学校");
        firstRow.createCell(3).setCellValue("认证状态");
        firstRow.createCell(4).setCellValue("是否黑名单");
        firstRow.createCell(5).setCellValue("邀请类型");
        firstRow.createCell(6).setCellValue("邀请时间");
        int rowNum = 1;
        for (Map<String, Object> data : dataList) {
            XSSFRow xssfRow = xssfSheet.createRow(rowNum++);
            xssfRow.setHeightInPoints(20);
            xssfRow.createCell(0).setCellValue(ConversionUtils.toString(data.get("teacherId")));
            xssfRow.createCell(1).setCellValue(ConversionUtils.toString(data.get("inviteId")));
            xssfRow.createCell(2).setCellValue(ConversionUtils.toString(data.get("schoolName")));
            xssfRow.createCell(3).setCellValue(ConversionUtils.toString(data.get("authentication")));
            xssfRow.createCell(4).setCellValue(ConversionUtils.toString(data.get("isBlack")));
            xssfRow.createCell(5).setCellValue(ConversionUtils.toString(data.get("inviteType")));
            xssfRow.createCell(6).setCellValue(ConversionUtils.toString(data.get("inviteDate")));
        }
        for (int i = 0; i < 7; i++) {
            xssfSheet.setColumnWidth(i, 400 * 15);
        }
        return xssfWorkbook;
    }

    private boolean checkCreditDataByStudentId(List<StringBuffer> lstFailed, String m, String[] contextArray) {
        if (!NumberUtils.isNumber(contextArray[0])) {
            StringBuffer errorMessage = new StringBuffer(m).append("  该用户id不正确,请核实");
            lstFailed.add(errorMessage);
            return true;
        }
        if (SafeConverter.toLong(contextArray[0]) == 0) {
            StringBuffer errorMessage = new StringBuffer(m).append("  该用户id不正确,请核实");
            lstFailed.add(errorMessage);
            return true;
        }
        if (!NumberUtils.isNumber(StringUtils.trim(contextArray[1]))) {
            StringBuffer errorMessage = new StringBuffer(m).append(" 学分数量错误,请核实");
            lstFailed.add(errorMessage);
            return true;
        }
        if (SafeConverter.toInt(contextArray[1]) == 0) {
            StringBuffer errorMessage = new StringBuffer(m).append(" 输入的学分为0，请重新输入");
            lstFailed.add(errorMessage);
            return true;
        }
        User user = userLoaderClient.loadUser(SafeConverter.toLong(contextArray[0]));
        if (user == null) {
            StringBuffer errorMessage = new StringBuffer(m).append("  不存在此ID的用户或提交信息错误,请核实");
            lstFailed.add(errorMessage);
            return true;
        }
        return false;
    }
}