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

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.admin.service.crm.CrmTeacherSummaryService;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.core.helper.ClassJieHelper;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.business.consumer.MiscServiceClient;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationState;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.ClazzTeacherAlteration;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryServiceClient;
import lombok.Cleanup;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.*;
import static java.util.stream.Collectors.toList;

/**
 * @author RuiBao
 * @version 0.1
 * @since 2/3/2015
 */
@Controller
@RequestMapping("/crm/clazz/alteration")
public class CrmClazzAlterationController extends CrmAbstractController {

    private final static String FAKE_TEACHER_OPERATION = "换班外呼排假";
    private final static String DATETIME_FORMAT = "yyyy-MM-dd hh:mm:ss";

    @Inject private RaikouSDK raikouSDK;

    @Inject private MiscServiceClient miscServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;

    /**
     * 供[导出Excel]功能使用SQL By Wyc 2016-01-18
     */
    private final static String DOWNLOAD_TEACHER_ALTERATION_SQL_SIMPLIFY = "SELECT " +
            " APPLICANT_ID AS applicantId, CLAZZ_ID, RESPONDENT_ID, ALTERATION_TYPE, DATE_FORMAT(CREATE_DATETIME, '%Y-%m-%d') CREATE_TIME " +
            " FROM VOX_CLAZZ_TEACHER_ALTERATION " +
            " WHERE DISABLED=0 AND UPDATE_DATETIME BETWEEN :startDate AND :endDate " +
            " AND ALTERATION_STATE='PENDING' AND ALTERATION_TYPE IN ('TRANSFER','REPLACE','LINK') " +
            " ORDER BY CREATE_DATETIME DESC ";

    private final static String ALTERATION_SCHOOL_SQL = "SELECT DISTINCT " +
            " T1.SCHOOL_ID AS schoolId, T2.CNAME AS schoolName, T1.APPLICANT_ID AS applicantId " +
            " FROM VOX_CLAZZ_TEACHER_ALTERATION T1 " +
            " INNER JOIN VOX_SCHOOL T2 ON T2.ID=T1.SCHOOL_ID AND T2.DISABLED=0 " +
            " INNER JOIN VOX_REGION_VIEW T3 ON T3.acode=T1.REGION_CODE AND T3.pcode=:pcode " +
            " WHERE T1.DISABLED=0 AND T1.UPDATE_DATETIME BETWEEN :startDate AND :endDate " +
            " AND T1.ALTERATION_STATE='PENDING' AND T1.ALTERATION_TYPE IN ('TRANSFER','REPLACE','LINK' ) ";

    private final static String ALTERATION_MANUAL_SQL = "SELECT ID AS id " +
            ", ALTERATION_TYPE AS type, CLAZZ_ID AS clazzId " +
            ", APPLICANT_ID AS applicantId, RESPONDENT_ID AS respondentId " +
            " FROM VOX_CLAZZ_TEACHER_ALTERATION " +
            " WHERE DISABLED=0 " +
            " AND UPDATE_DATETIME BETWEEN :startDate AND :endDate " +
            " AND ALTERATION_STATE='PENDING' " +
            " AND ALTERATION_TYPE IN ('TRANSFER','REPLACE','LINK' ) " +
            " AND CC_PROCESS_STATE = 'PROCESS_MANUAL'";

    private final static String ALTERATION_DATA_BY_SCHOOL_SQL = "SELECT ID AS id " +
            ", ALTERATION_TYPE AS type, CLAZZ_ID AS clazzId " +
            ", APPLICANT_ID AS applicantId, RESPONDENT_ID AS respondentId " +
            " FROM VOX_CLAZZ_TEACHER_ALTERATION " +
            " WHERE SCHOOL_ID=:schoolId AND DISABLED=0 " +
            " AND UPDATE_DATETIME BETWEEN :startDate AND :endDate " +
            " AND ALTERATION_STATE='PENDING' AND ALTERATION_TYPE IN ('TRANSFER','REPLACE','LINK') ";

    private final static String ALTERATION_DATA_BY_TEACHER_SQL = "SELECT ID AS id " +
            " , ALTERATION_TYPE AS type, CLAZZ_ID AS clazzId " +
            " , APPLICANT_ID AS applicantId, RESPONDENT_ID AS respondentId " +
            " FROM VOX_CLAZZ_TEACHER_ALTERATION " +
            " WHERE DISABLED=0 AND (APPLICANT_ID=:applicantId OR RESPONDENT_ID=:respondentId) " +
//            " AND UPDATE_DATETIME <=:endDate " +
            " AND ALTERATION_STATE='PENDING' AND ALTERATION_TYPE IN ('TRANSFER','REPLACE','LINK') ";

    // FIXME Enhancement #19528 (3/5) 为了统计数据而用上了 CASE WHEN 以及 GROUP BY DATE_FORMAT
    private final static String ALTERATION_RECORD_SQL = "SELECT DATE_FORMAT(UPDATE_DATETIME, '%Y-%m-%d') AS recordDate, " +
            " SUM(CASE WHEN ALTERATION_STATE='SUCCESS' AND ALTERATION_TYPE='TRANSFER' THEN 1 ELSE 0 END) AS tranSucCnt, " +
            " SUM(CASE WHEN ALTERATION_STATE='SUCCESS' AND ALTERATION_TYPE='REPLACE' THEN 1 ELSE 0 END) AS repSucCnt, " +
            " SUM(CASE WHEN ALTERATION_STATE='SUCCESS' AND ALTERATION_TYPE='LINK' THEN 1 ELSE 0 END) AS lnkSucCnt, " +
            " SUM(CASE WHEN ALTERATION_STATE='CANCELED' AND ALTERATION_TYPE='TRANSFER' THEN 1 ELSE 0 END) AS tranCanCnt, " +
            " SUM(CASE WHEN ALTERATION_STATE='CANCELED' AND ALTERATION_TYPE='REPLACE' THEN 1 ELSE 0 END) AS repCanCnt, " +
            " SUM(CASE WHEN ALTERATION_STATE='CANCELED' AND ALTERATION_TYPE='LINK' THEN 1 ELSE 0 END) AS lnkCanCnt " +
            " FROM VOX_CLAZZ_TEACHER_ALTERATION " +
            " WHERE DISABLED=0 AND UPDATE_DATETIME BETWEEN :startDate AND :endDate " +
            " AND ALTERATION_TYPE IN ('TRANSFER','REPLACE','LINK') AND ALTERATION_STATE IN ('CANCELED','SUCCESS') " +
            " GROUP BY DATE_FORMAT(UPDATE_DATETIME, '%Y-%m-%d') ";

    private final static String ALTERATION_ADMIN_RECORD_SQL = " SELECT DATE_FORMAT(UPDATE_DATETIME,'%Y-%m-%d') AS recordDate " +
            " , COUNT(1) AS fakeCnt  " +
            " FROM ADMIN_CUSTOMER_SERVICE_RECORD " +
            " WHERE OPERATION='" + FAKE_TEACHER_OPERATION + "' " +
            " AND UPDATE_DATETIME BETWEEN :startDate AND :endDate " +
            " GROUP BY DATE_FORMAT(UPDATE_DATETIME,'%Y-%m-%d') ";

    @Inject private CrmTeacherSummaryService crmTeacherSummaryService;
    @Inject private CrmSummaryServiceClient crmSummaryServiceClient;

    @RequestMapping(value = "/index.vpage", method = RequestMethod.GET)
    public String index() {
        return "crm/clazz/alteration/index";
    }

    // 获取当前地区所有学校信息 By Wyc 2016-01-06
    @RequestMapping(value = "/getschools.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getAlterationSchoolData() {
        Integer pcode = getRequestInt("pcode");
        Date endDate = calculateDateDay(new Date(), -3);
        Date startDate = calculateDateDay(new Date(), -30);

        Map<String, Date> dateRange = defaultDateRange(null, null);
        // Enhancement #19528 (1/5) 过滤掉Applicant的假老师
        List<Map<String, Object>> records = queryRecordsFilterFakeApplicant(ALTERATION_SCHOOL_SQL,
                MiscUtils.m("pcode", pcode, "startDate", dateRange.get("startDate"), "endDate", dateRange.get("endDate")));

        // 过滤掉之后的记录还要保证 schoolId 的唯一性
        records = filterDuplicateSchool(records);
        return MapMessage.successMessage().add("json", records);
    }

    // 获取当前学校所有换班记录 By Wyc 2016-01-07
    @RequestMapping(value = "/getclasses.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getAlterationClazzData() {
        Long schoolId = getRequestLong("schoolId");
        Date endDate = calculateDateDay(new Date(), -3);
        Date startDate = calculateDateDay(new Date(), -30);

        Map<String, Date> dateRange = defaultDateRange(null, null);
        // Enhancement #19528 (1/5) 过滤掉Applicant的假老师
        List<Map<String, Object>> records = queryRecordsFilterFakeApplicant(ALTERATION_DATA_BY_SCHOOL_SQL,
                MiscUtils.m("schoolId", schoolId, "startDate", dateRange.get("startDate"), "endDate", dateRange.get("endDate")));
        if (CollectionUtils.isEmpty(records)) {
            return MapMessage.successMessage().add("json", null);
        }
        // 返回值
        List<Map<String, Object>> result = getAlterationData(records);
        return MapMessage.successMessage().add("json", result);
    }

    /**
     * 获得需要人工处理的换班记录数据
     */
    @RequestMapping(value = "/getmanualclasses.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getManualAlterationClazzData() {

        Map<String, Date> dateRange = defaultDateRange(null, null);
        Map<String, Object> queryParams = MapUtils.m("startDate", dateRange.get("startDate"), "endDate", dateRange.get("endDate"));

        List<Map<String, Object>> records = queryRecordsFilterFakeApplicant(ALTERATION_MANUAL_SQL, queryParams);
        if (CollectionUtils.isEmpty(records)) {
            return MapMessage.successMessage().add("json", null);
        }

        // 返回值
        List<Map<String, Object>> result = getAlterationData(records);
        return MapMessage.successMessage().add("json", result);
    }

    /**
     * 修改： 只用根据传入的老师的手机或手机号确定唯一的教师ID
     * 根据Id加载出所有的学校，点击学校直接走 getAlterationClazzData()
     * By Wyc 2016-01-08
     * Enhancement #19528 (4/5) 查询时直接显示换班信息 By Wyc 2016-03-09
     */
    @RequestMapping(value = "searchdata.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSearchAlterationData() {
        Long teacherId = getRequestLong("teacherId");
        String teacherMobile = getRequestString("teacherMobile");

        // 时间范围
        Date endDate = calculateDateDay(new Date(), -3);
        Date startDate = calculateDateDay(new Date(), -30);

        if (teacherId == 0L && StringUtils.isBlank(teacherMobile)) {
            return MapMessage.errorMessage("查询条件不能都为空");
        }

        // 优先teacherId查询，其次手机号
        Long tid = null;
        if (teacherId != 0L) {// 如果填了老师id
            tid = teacherId;
        } else if (StringUtils.isNotBlank(teacherMobile)) {
            UserAuthentication ua = userLoaderClient.loadMobileAuthentication(teacherMobile, UserType.TEACHER);
            if (ua != null) {
                tid = ua.getId();
            }
        }

        // Enhancement #19528 (1/5) 过滤掉Applicant的假老师
        List<Map<String, Object>> records = queryRecordsFilterFakeApplicant(ALTERATION_DATA_BY_TEACHER_SQL,
                MiscUtils.m(
                        "applicantId", tid
                        , "respondentId", tid
//                        ,"startDate", startDate
//                        ,"endDate", endDate
                )
        );
        if (CollectionUtils.isEmpty(records)) {
            return MapMessage.successMessage().add("json", null);
        }
        // 返回值
        List<Map<String, Object>> result = getAlterationData(records);
        return MapMessage.successMessage().add("json", result);
    }

    @RequestMapping(value = "approvetransfer.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage approveTransferApplication() {
        Long respondentId = getRequestLong("respondentId");
        Long applicantId = getRequestLong("applicantId");
        Long clazzId = getRequestLong("clazzId");
        Long recordId = getRequestLong("recordId");
        String type = getRequestString("type");

        ClazzTeacherAlteration alteration = teacherLoaderClient.loadClazzTeacherAlteration(recordId);
        if (null == alteration) {
            return MapMessage.errorMessage("操作失败");
        }
        if (!alteration.getApplicantId().equals(applicantId) || !alteration.getRespondentId().equals(respondentId)
                || !alteration.getClazzId().equals(clazzId) || !StringUtils.equals(alteration.getType().name(), type)) {
            return MapMessage.errorMessage("操作失败");
        }

        try {
            MapMessage message = teacherAlterationServiceClient.approveApplication(respondentId, recordId, ClazzTeacherAlterationType.TRANSFER, OperationSourceType.crm);

            if (message.isSuccess()) {
                Teacher applicant = (Teacher) message.remove("applicant");
                Teacher respondent = (Teacher) message.remove("respondent");
                Clazz clazz = (Clazz) message.remove("clazz");
                // 发送教师首页通知 FIXME 这里是不是写反了啊...
                String text = StringUtils.formatMessage("{}老师同意了您转让{}的申请。",
                        clazz.formalizeClazzName(), respondent.getProfile().getRealname());
                savePopup(text, applicant.getId());
            } else {
                GroupMapper group = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(applicantId, clazzId, false);
                if (group == null) {// 说明申请人在该班级已没有学生资源，取消申请
                    teacherAlterationServiceClient.cancelApplication(applicantId, recordId, ClazzTeacherAlterationType.TRANSFER, OperationSourceType.crm);
                }
            }
            return message;
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.successMessage("正在处理，请不要重复提交");
            }
            logger.error("ERROR OCCURS WHEN APPROVING HAND_OVER APPLICATION IN CRM. RESPONDENTID: {}, RECORDID: {}. EX: {}",
                    respondentId, recordId, ex.getMessage(), ex);
            return MapMessage.errorMessage("同意转让任课申请失败");
        }
    }

    @RequestMapping(value = "canceltransfer.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage cancelTransferApplication() {
        Long applicantId = getRequestLong("applicantId");
        Long recordId = getRequestLong("recordId");
        try {
            return teacherAlterationServiceClient.cancelApplication(applicantId, recordId, ClazzTeacherAlterationType.TRANSFER, OperationSourceType.crm);
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.successMessage("正在处理，请不要重复提交");
            }
            logger.error("ERROR OCCURS WHEN CANCELING HAND_OVER APPLICATION IN CRM. APPLICANTID: {}, RECORDID: {}. EX: {}",
                    applicantId, recordId, ex.getMessage(), ex);
            return MapMessage.errorMessage("取消转让任课申请失败");
        }
    }

    @RequestMapping(value = "approvereplace.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage approveReplaceApplication() {
        Long respondentId = getRequestLong("respondentId");
        Long applicantId = getRequestLong("applicantId");
        Long clazzId = getRequestLong("clazzId");
        Long recordId = getRequestLong("recordId");
        String type = getRequestString("type");

        ClazzTeacherAlteration alteration = teacherLoaderClient.loadClazzTeacherAlteration(recordId);
        if (null == alteration) {
            return MapMessage.errorMessage("操作失败");
        }
        if (!alteration.getApplicantId().equals(applicantId) || !alteration.getRespondentId().equals(respondentId)
                || !alteration.getClazzId().equals(clazzId) || !StringUtils.equals(alteration.getType().name(), type)) {
            return MapMessage.errorMessage("操作失败");
        }

        try {
            MapMessage message = teacherAlterationServiceClient.approveApplication(respondentId, recordId, ClazzTeacherAlterationType.REPLACE, OperationSourceType.crm);
            if (message.isSuccess()) {
                Teacher applicant = (Teacher) message.remove("applicant");
                Teacher respondent = (Teacher) message.remove("respondent");
                Clazz clazz = (Clazz) message.remove("clazz");
                // 发送教师首页通知
                String text = StringUtils.formatMessage("{}老师同意了您接管{}的申请。",
                        respondent.getProfile().getRealname(), clazz.formalizeClazzName());
                savePopup(text, applicant.getId());
            } else {
                GroupMapper group = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(respondentId, clazzId, false);
                if (group == null) {// 说明被申请人在该班级已没有学生资源，取消申请
                    teacherAlterationServiceClient.cancelApplication(applicantId, recordId, ClazzTeacherAlterationType.REPLACE, OperationSourceType.crm);
                }
            }
            return message;
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.successMessage("正在处理，请不要重复提交");
            }
            logger.error("ERROR OCCURS WHEN APPROVING SUBSTITUTE APPLICATION IN CRM. RESPONDENTID: {}, RECORDID: {}. EX: {}",
                    respondentId, recordId, ex.getMessage(), ex);
            return MapMessage.errorMessage("同意接管申请失败");
        }
    }

    @RequestMapping(value = "cancelreplace.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage cancelReplaceApplication() {
        Long applicantId = getRequestLong("applicantId");
        Long recordId = getRequestLong("recordId");
        try {
            return teacherAlterationServiceClient.cancelApplication(applicantId, recordId, ClazzTeacherAlterationType.REPLACE, OperationSourceType.crm);
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.successMessage("正在处理，请不要重复提交");
            }
            logger.error("ERROR OCCURS WHEN CANCELING SUBSTITUTE APPLICATION IN CRM. APPLICANTID: {}, RECORDID: {}. EX: {}",
                    applicantId, recordId, ex.getMessage(), ex);
            return MapMessage.errorMessage("操作失败");
        }
    }

    @RequestMapping(value = "approvelink.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage approveLinkApplication() {
        Long respondentId = getRequestLong("respondentId");
        Long applicantId = getRequestLong("applicantId");
        Long clazzId = getRequestLong("clazzId");
        Long recordId = getRequestLong("recordId");
        String type = getRequestString("type");

        ClazzTeacherAlteration alteration = teacherLoaderClient.loadClazzTeacherAlteration(recordId);
        if (null == alteration) {
            return MapMessage.errorMessage("操作失败");
        }
        if (!alteration.getApplicantId().equals(applicantId) || !alteration.getRespondentId().equals(respondentId)
                || !alteration.getClazzId().equals(clazzId) || !StringUtils.equals(alteration.getType().name(), type)) {
            return MapMessage.errorMessage("操作失败");
        }

        try {
            MapMessage message = teacherAlterationServiceClient.approveApplication(respondentId, recordId, ClazzTeacherAlterationType.LINK, OperationSourceType.crm);
            if (message.isSuccess()) {
                Teacher applicant = (Teacher) message.remove("applicant");
                Teacher respondent = (Teacher) message.remove("respondent");
                Clazz clazz = (Clazz) message.remove("clazz");
                // 发送教师首页通知
                String text = StringUtils.formatMessage("{}老师同意了您加入{}的申请。",
                        respondent.getProfile().getRealname(), clazz.formalizeClazzName());
                savePopup(text, applicant.getId());
            } else {
                GroupMapper group = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(respondentId, clazzId, false);
                if (group == null) {// 说明被申请人在该班级已没有学生资源，取消申请
                    teacherAlterationServiceClient.cancelApplication(applicantId, recordId, ClazzTeacherAlterationType.LINK, OperationSourceType.crm);
                }
            }
            return message;
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.successMessage("正在处理，请不要重复提交");
            }
            logger.error("ERROR OCCURS WHEN APPROVING JOIN APPLICATION IN CRM. RESPONDENTID: {}, RECORDID: {}. EX: {}",
                    respondentId, recordId, ex.getMessage(), ex);
            return MapMessage.errorMessage("同意加入申请失败");
        }
    }

    @RequestMapping(value = "cancellink.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage cancelLinkApplication() {
        Long applicantId = getRequestLong("applicantId");
        Long recordId = getRequestLong("recordId");
        try {
            return teacherAlterationServiceClient.cancelApplication(applicantId, recordId, ClazzTeacherAlterationType.LINK, OperationSourceType.crm);
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.successMessage("正在处理，请不要重复提交");
            }
            logger.error("ERROR OCCURS WHEN CANCELING LINK APPLICATION IN CRM. APPLICANTID: {}, RECORDID: {}. EX: {}",
                    applicantId, recordId, ex.getMessage(), ex);
            return MapMessage.errorMessage("操作失败");
        }
    }

//    @RequestMapping(value = "dealwithfeedback.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public MapMessage dealWithFeedback() {
//        Long feedbackId = getRequestLong("feedbackId");
//        userManagementClient.updateClazzTeacherAlterationCrmState(feedbackId, ClazzTeacherAlterationState.SUCCESS);
//        return MapMessage.successMessage();
//    }

    /**
     * 稍稍改动先前 导出Excel 的方法
     * 仅查询 VOX_CLAZZ_TEACHER_ALTERATION 表，取消关联 User/UserAuthentication/Clazz 表
     * 其他字段通过Loader获得 By Wyc 2016-01-18
     */
    @RequestMapping(value = "downloadalteration.vpage", method = RequestMethod.POST)
    public String downloadAlteration(Model model) {
        try {
            // Enhancement #19528 (2/5) 请求有时间区间
            String startDateStr = getRequestParameter("startDate", "");
//            Date startDate = StringUtils.isBlank(startDateStr) ?
//                    calculateDateDay(new Date(), -30) : stringToDate(startDateStr + " 00:00:00", DATETIME_FORMAT);

            String endDateStr = getRequestParameter("endDate", "");
//            Date lastDate = calculateDateDay(new Date(), -3);
//            lastDate = DayRange.newInstance(lastDate.getTime()).getEndDate();
//            Date endDate = StringUtils.isBlank(endDateStr) ? lastDate : stringToDate(endDateStr + " 23:59:59", DATETIME_FORMAT);
//            endDate = endDate.after(lastDate) ? lastDate : endDate;

            Map<String, Date> dateMap = defaultDateRange(startDateStr, endDateStr);
            Date startDate = dateMap.get("startDate");
            Date endDate = dateMap.get("endDate");

            if (StringUtils.isBlank(startDateStr) || StringUtils.isBlank(endDateStr) || endDate.before(startDate)) {
                model.addAttribute("error", "请选择正确的时间区间！");
                return "crm/clazz/alteration/index";
            }
            if (dayDiff(endDate, startDate) > 30L) {
                model.addAttribute("error", "只允许导出30天的换班数据！");
                return "crm/clazz/alteration/index";
            }

            // Enhancement #19528 (1/5) 过滤掉Applicant的假老师
            List<Map<String, Object>> records = queryRecordsFilterFakeApplicant(DOWNLOAD_TEACHER_ALTERATION_SQL_SIMPLIFY,
                    MiscUtils.m("startDate", startDate, "endDate", endDate));
            if (CollectionUtils.isEmpty(records)) {
                model.addAttribute("error", "该时间区间无换班记录");
                return "crm/clazz/alteration/index";
            }
            // 处理查询结果
            Set<Long> clazzIdSet = new HashSet<>(); // 班级Id集合
            Set<Long> teacherIdSet = new HashSet<>(); // 老师Id集合
            records.forEach(record -> {
                clazzIdSet.add(ConversionUtils.toLong(record.get("CLAZZ_ID")));
                teacherIdSet.add(ConversionUtils.toLong(record.get("applicantId")));
                teacherIdSet.add(ConversionUtils.toLong(record.get("RESPONDENT_ID")));
            });


            Map<Long, Teacher> teacherSet = teacherLoaderClient.loadTeachers(teacherIdSet);
            Map<Long, Clazz> clazzSet = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazzs(clazzIdSet)
                    .stream()
                    .collect(Collectors.toMap(Clazz::getId, Function.identity()));
            Map<Long, String> teacherMobileMap = new HashMap<>(); // 老师ID所对应的解码的手机号码

            for (Map<String, Object> record : records) {
                record.put("CREATE_TIME", ConversionUtils.toString(record.get("CREATE_TIME")));
                // Applicant Information
                Long applicantId = ConversionUtils.toLong(record.get("applicantId"));
                record.put("APPLICANT_NAME", teacherSet.get(applicantId).getProfile().getRealname());
                if (!teacherMobileMap.containsKey(applicantId)) {
                    String phone = sensitiveUserDataServiceClient.showUserMobile(applicantId, "CRM-换班-导出Excel", SafeConverter.toString(applicantId));
                    teacherMobileMap.put(applicantId, phone != null ? phone : "");
                }
                record.put("APPLICANT_MOBILE", teacherMobileMap.get(applicantId) == null ? "" : teacherMobileMap.get(applicantId));
                // Respondent Information
                Long respondentId = ConversionUtils.toLong(record.get("RESPONDENT_ID"));
                record.put("RESPONDENT_NAME", teacherSet.get(respondentId).getProfile().getRealname());
                if (!teacherMobileMap.containsKey(respondentId)) {
                    String phone = sensitiveUserDataServiceClient.showUserMobile(respondentId, "CRM-换班-导出Excel", SafeConverter.toString(respondentId));
                    teacherMobileMap.put(respondentId, phone != null ? phone : "");
                }
                record.put("RESPONDENT_MOBILE", teacherMobileMap.get(respondentId) == null ? "" : teacherMobileMap.get(respondentId));
                // Clazz Information
                Clazz clazz = clazzSet.get(ConversionUtils.toLong(record.get("CLAZZ_ID")));
                record.put("CLASS_INFO", formalizeClazzName(clazz));

            } // 结果集处理完毕

            HSSFWorkbook hssfWorkbook = createAlterationResult(records);
            String filename = "换班申请列表-" + dateToString(new Date(), FORMAT_SQL_DATE) + ".xls";
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            hssfWorkbook.write(outStream);
            outStream.flush();

            try {
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        filename,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException ignored) {
//                response.getWriter().write("不能下载");
//                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                model.addAttribute("error", "不能下载");
            }
        } catch (Exception ex) {
            logger.error("下载失败!", ex.getMessage(), ex);
            model.addAttribute("error", "下载失败!");
        }
        return "crm/clazz/alteration/index";
    }

    @RequestMapping(value = "alterationrecords.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getAlterationRecords() {
        try {
            // Enhancement #19528 (2/5) 请求有时间区间, 换班的时间控制在一周之内
            String startDateStr = getRequestParameter("startDate", "");
            Date startDate = StringUtils.isBlank(startDateStr) ?
                    calculateDateDay(new Date(), -30) : stringToDate(startDateStr + " 00:00:00", DATETIME_FORMAT);

            String endDateStr = getRequestParameter("endDate", "");
            Date endDate = StringUtils.isBlank(endDateStr) ?
                    calculateDateDay(new Date(), -3) : stringToDate(endDateStr + " 23:59:59", DATETIME_FORMAT);
            if (StringUtils.isBlank(startDateStr) || StringUtils.isBlank(endDateStr) || endDate.before(startDate)) {
                return MapMessage.errorMessage("请填写正确的日期区间！");
            }
            if (dayDiff(endDate, startDate) > 7L) {
                return MapMessage.errorMessage("只能查看间隔7天以内的记录！");
            }

            Map<String, Object> paramMap = MiscUtils.m("startDate", startDate, "endDate", endDate);
            // 从 VOX_CLAZZ_TEACHER_ALTERATION 里取 处理数、通过数、拒绝数以及计算通过率等
            List<Map<String, Object>> record_yang = utopiaSql.withSql(ALTERATION_RECORD_SQL)
                    .useParams(paramMap).queryAll();
            // 还要单独从 ADMIN_CUSTOMER_SERVICE_RECORD 里取出 判假数
            List<Map<String, Object>> record_yin = utopiaSqlAdmin.withSql(ALTERATION_ADMIN_RECORD_SQL).useParams(paramMap).queryAll();

            List<Map<String, Object>> result = mergeAlterRecords(startDate, endDate, record_yang, record_yin);

            return MapMessage.successMessage().add("result", result);

        } catch (Exception ex) {
            logger.error("获取换班操作记录失败!", ex.getMessage(), ex);
            return MapMessage.errorMessage("请填写正确的日期区间！");
        }

    }

    /**
     * Enhancement #19528 (5/5) 换班外呼排假
     */
    @RequestMapping(value = "faketeacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage fakeTeacherById() {
        Long teacherId = getRequestLong("teacherId");
        if (teacherId == 0L) {
            return MapMessage.errorMessage("无效的教师ID!");
        }
        String desc = getRequestParameter("desc", "");
        if (StringUtils.isBlank(desc)) {
            desc = "换班外呼排假";
        }
        Set<Long> teacherMainSubIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        Set<Long> receivers = new HashSet<>();
        for (Long tempTeacherId : teacherMainSubIds) {
            // 加载出相关换班申请
            List<ClazzTeacherAlteration> alterations = teacherLoaderClient.loadApplicantOrRespondentAlterations(tempTeacherId)
                    .stream()
                    .filter(alteration -> ClazzTeacherAlterationState.PENDING == alteration.getState())
                    .collect(toList());

            // 然后全部取消
            if (CollectionUtils.isNotEmpty(alterations)) {
                for (ClazzTeacherAlteration alteration : alterations) {
                    teacherAlterationServiceClient.cancelApplication(
                            alteration.getApplicantId(), alteration.getId(),
                            alteration.getType(), OperationSourceType.crm
                    );
                    // 取消完了之后给老师发消息
                    Long receiver = null;
                    if (Objects.equals(tempTeacherId, alteration.getApplicantId())) {
                        receiver = alteration.getRespondentId();
                    } else if (Objects.equals(tempTeacherId, alteration.getRespondentId())) {
                        receiver = alteration.getApplicantId();
                    }
                    CollectionUtils.addNonNullElement(receivers, receiver);
                }
            }

        }

        MapMessage message = crmSummaryServiceClient.updateTeacherFakeType(teacherId, CrmTeacherFakeValidationType.MANUAL_VALIDATION, desc);
        if (message.isSuccess()) {
            // 发送申诉消息
            miscServiceClient.sendFakeAppealMessage(teacherId);

            // 发送通知消息
            miscServiceClient.sendFakeNoticeMessage(teacherId, receivers);

            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacherId);
            userServiceRecord.setOperatorId("System");
            userServiceRecord.setOperationType(UserServiceRecordOperationType.老师判假.name());
            userServiceRecord.setOperationContent("老师判假");
            userServiceRecord.setComments(desc);
            userServiceClient.saveUserServiceRecord(userServiceRecord);
        }

        return message;
    }

    private void savePopup(String text, Long userId) {
        userPopupServiceClient.createPopup(userId)
                .content(text)
                .type(PopupType.TEACHER_ALTERATION_FOR_RESPONDENT)
                .category(PopupCategory.LOWER_RIGHT)
                .create();
    }

    private HSSFWorkbook createAlterationResult(List<Map<String, Object>> records) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();

        HSSFSheet hssfSheet = hssfWorkbook.createSheet("换班列表");
        hssfSheet.setColumnWidth(0, 5000);
        hssfSheet.setColumnWidth(1, 5000);
        hssfSheet.setColumnWidth(2, 3300);
        hssfSheet.setColumnWidth(3, 3300);
        hssfSheet.setColumnWidth(4, 3500);
        hssfSheet.setColumnWidth(5, 5000);
        hssfSheet.setColumnWidth(6, 5000);
        hssfSheet.setColumnWidth(7, 3300);
        hssfSheet.setColumnWidth(8, 4000);

        HSSFCellStyle borderStyle = hssfWorkbook.createCellStyle();
        // 设置单元格边框样式
        borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
        borderStyle.setBorderTop(CellStyle.BORDER_THIN);
        borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
        borderStyle.setBorderRight(CellStyle.BORDER_THIN);

        HSSFRow firstRow = createRow(hssfSheet, 0, 8, borderStyle);
        setCellValue(firstRow, 0, borderStyle, "申请时间");
        setCellValue(firstRow, 1, borderStyle, "申请人ID");
        setCellValue(firstRow, 2, borderStyle, "申请人姓名");
        setCellValue(firstRow, 3, borderStyle, "申请人手机");
        setCellValue(firstRow, 4, borderStyle, "班级");
        setCellValue(firstRow, 5, borderStyle, "申请类型");
        setCellValue(firstRow, 6, borderStyle, "接收人ID");
        setCellValue(firstRow, 7, borderStyle, "接收人姓名");
        setCellValue(firstRow, 8, borderStyle, "接收人手机");

        int rowNum = 1;
        if (records != null && records.size() > 0) {
            for (Map<String, Object> record : records) {
                String createTime = String.valueOf(record.get("CREATE_TIME"));
                String applicantId = String.valueOf(record.get("applicantId"));
                String applicantName = String.valueOf(record.get("APPLICANT_NAME"));
                String applicantMobile = String.valueOf(record.get("APPLICANT_MOBILE"));
                String clazzInfo = String.valueOf(record.get("CLASS_INFO"));
                String respondentId = String.valueOf(record.get("RESPONDENT_ID"));
                String respondentName = String.valueOf(record.get("RESPONDENT_NAME"));
                String respondentMobile = String.valueOf(record.get("RESPONDENT_MOBILE"));
                String alterationType = String.valueOf(record.get("ALTERATION_TYPE"));
                ClazzTeacherAlterationType ctaType = ClazzTeacherAlterationType.valueOf(alterationType);
                String typeName;
                switch (ctaType) {
                    case REPLACE:
                        typeName = "接管班级";
                        break;
                    case TRANSFER:
                        typeName = "申请转让";
                        break;
                    case LINK:
                        typeName = "关联班级";
                        break;
                    default:
                        typeName = "未知类型";
                        break;
                }
                HSSFRow row = createRow(hssfSheet, rowNum++, 8, borderStyle);
                setCellValue(row, 0, borderStyle, createTime);
                setCellValue(row, 1, borderStyle, applicantId);
                setCellValue(row, 2, borderStyle, applicantName);
                setCellValue(row, 3, borderStyle, applicantMobile);
                setCellValue(row, 4, borderStyle, clazzInfo);
                setCellValue(row, 5, borderStyle, typeName);
                setCellValue(row, 6, borderStyle, respondentId);
                setCellValue(row, 7, borderStyle, respondentName);
                setCellValue(row, 8, borderStyle, respondentMobile);
                // 每写一行Excel，记录一条日志，记录是哪位管理员去获取了老师的手机号信息
                // By Wyc 2016-01-19
                addAdminLog(getCurrentAdminUser().getAdminUserName() + " 老师信息，老师ID：" + applicantId, Long.parseLong(applicantId));
                addAdminLog(getCurrentAdminUser().getAdminUserName() + " 老师信息，老师ID：" + respondentId, Long.parseLong(respondentId));
            }
        }
        return hssfWorkbook;
    }

    public static HSSFRow createRow(HSSFSheet sheet, int rowNum, int column, CellStyle style) {
        HSSFRow row = sheet.createRow(rowNum);
        for (int i = 0; i <= column; i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(style);
        }

        return row;
    }

    public static void setCellValue(HSSFRow row, int column, CellStyle style, String value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        cell.setCellStyle(style);
        cell.setCellValue(value);
    }

    private String formalizeClazzName(String clazzLevel, String clazzName, EduSystemType eduSystemType) {
        int jie = ClassJieHelper.fromClazzLevel(ClazzLevel.parse(Integer.valueOf(clazzLevel)));
        int level = ClassJieHelper.toClazzLevel(jie, eduSystemType).getLevel();
        return level + "年级" + clazzName;
    }

    private String formalizeClazzName(Clazz clazz) {
        if (clazz == null) {
            return null;
        }
        String gradeInfo = String.valueOf(clazz.getClassLevel()) + "年级";
        String classInfo = String.valueOf(clazz.getClassName());
        return StringUtils.join(gradeInfo, classInfo, "(", String.valueOf(clazz.getId()), ")");
    }

    /**
     * Enhancement #19528 (1/5) 过滤掉申请人为假老师的显示
     */
    private List<Map<String, Object>> queryRecordsFilterFakeApplicant(String recordSql, Map paramMap) {
        List<Map<String, Object>> records = utopiaSql.withSql(recordSql).useParams(paramMap).queryAll();
        if (CollectionUtils.isEmpty(records)) {
            return records;
        }
        Set<Long> applicantIds = records.stream().map(r -> SafeConverter.toLong(r.get("applicantId"))).collect(Collectors.toSet());
        List<CrmTeacherSummary> teacherSummaries = crmTeacherSummaryService.getTeacherSummaryListByTeacherIds(applicantIds);
        Set<Long> fakeApplicantIds = teacherSummaries.stream()
                .filter(t -> SafeConverter.toBoolean(t.getFakeTeacher()) && CrmTeacherFakeValidationType.MANUAL_VALIDATION.getName().equals(t.getValidationType()))
                .map(CrmTeacherSummary::getTeacherId).collect(Collectors.toSet());
        return records.stream()
                .filter(r -> !fakeApplicantIds.contains(SafeConverter.toLong(r.get("applicantId"))))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> filterDuplicateSchool(List<Map<String, Object>> records) {
        if (CollectionUtils.isEmpty(records)) {
            return records;
        }
        List<Map<String, Object>> newRecords = new ArrayList<>();
        Set<Object> schoolIdSet = new HashSet<>();
        records.forEach(record -> {
            if (!schoolIdSet.contains(record.get("schoolId"))) {
                schoolIdSet.add(record.get("schoolId"));
                Map<String, Object> info = new HashMap<>();
                info.put("schoolId", record.get("schoolId"));
                info.put("schoolName", record.get("schoolName"));
                newRecords.add(info);
            }
        });
        return newRecords;
    }

    private List<Map<String, Object>> getAlterationData(List<Map<String, Object>> records) {

        Set<Long> clazzIdSet = new HashSet<>(); // 班级Id集合
        Set<Long> teacherIdSet = new HashSet<>(); // 老师Id集合

        // 查询结果根据 applicantId 分组
        Map<Long, List<Map<String, Object>>> tid_records_map = new HashMap<>(); // 教师Id和换班记录Map
        // 返回值
        List<Map<String, Object>> result = new ArrayList<>();

        records.forEach(record -> {
            Long respondentId = ConversionUtils.toLong(record.get("respondentId"));
            clazzIdSet.add(ConversionUtils.toLong(record.get("clazzId")));
            teacherIdSet.add(ConversionUtils.toLong(record.get("applicantId")));
            teacherIdSet.add(ConversionUtils.toLong(record.get("respondentId")));

            if (tid_records_map.containsKey(respondentId)) {
                tid_records_map.get(respondentId).add(record);
            } else {
                List<Map<String, Object>> list = new ArrayList<>();
                list.add(record);
                tid_records_map.put(respondentId, list);
            }
        });

        Map<Long, Clazz> clazzList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIdSet)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        // 如果班级列表空了，表示当前学校下不存在班级了(可能是班级被删除了)
        // 下面的步骤已经没有意义, 直接返回一个空结果
        if (MapUtils.isEmpty(clazzList)) {
            return null;
        }

        Map<Long, Teacher> teacherList = teacherLoaderClient.loadTeachers(teacherIdSet);
        for (Long respondentId : tid_records_map.keySet()) {
            Map<String, Object> teacherMap = new HashMap<>();
            // Applicant Information
            Teacher respondent = teacherList.get(respondentId);
            teacherMap.put("respondentId", respondentId);
            teacherMap.put("respondentName", respondent.getProfile().getRealname());
//            AuthenticatedMobile applicantMobile = sensitiveUserDataServiceClient.loadUserAuthenticationMobile(applicantId, "ClazzTeacherAlteration.RespondentMobile");
//            teacherMap.put("applicantMobile", applicantMobile == null ? null : applicantMobile.getMobile());
            teacherMap.put("respondentAuth", respondent.getAuthenticationState());

            List<Map<String, Object>> recordList = new ArrayList<>();
            for (Map<String, Object> record : tid_records_map.get(respondentId)) {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("recordId", record.get("id"));

                // Clazz Information
                Long clazzId = ConversionUtils.toLong(record.get("clazzId"));
                recordMap.put("clazzId", clazzId);
                Clazz clazzInfo = clazzList.get(clazzId);
                // avoid NPE, 如果班级信息为空，跳过该条记录
                if (clazzInfo == null) {
                    continue;
                } else {
                    String clazzLevel = clazzInfo.getClassLevel();
                    String clazzName = clazzInfo.getClassName();
                    EduSystemType eduSystemType = clazzInfo.getEduSystem();
                    recordMap.put("clazzName", formalizeClazzName(clazzLevel, clazzName, eduSystemType));
                }
                // Alteration Type Information
                String type = ConversionUtils.toString(record.get("type"));
                recordMap.put("type", type);
                String typeName = "未知类型";
                if (StringUtils.equals(type, ClazzTeacherAlterationType.TRANSFER.name())) {
                    typeName = "转让班级";
                } else if (StringUtils.equals(type, ClazzTeacherAlterationType.REPLACE.name())) {
                    typeName = "申请接管";
                } else if (StringUtils.equals(type, ClazzTeacherAlterationType.LINK.name())) {
                    typeName = "申请关联";
                }
                recordMap.put("typeName", typeName);

                // Respondent Information
                Long applicantId = ConversionUtils.toLong(record.get("applicantId"));
                Teacher applicant = teacherList.get(applicantId);
                recordMap.put("applicantId", applicantId);
                recordMap.put("applicantName", applicant.getProfile().getRealname());
//                AuthenticatedMobile respondentMobile = sensitiveUserDataServiceClient.loadUserAuthenticationMobile(respondentId, "ClazzTeacherAlteration.RespondentMobile");
//                recordMap.put("respondentMobile", respondentMobile == null ? null : respondentMobile.getMobile());
                recordMap.put("applicantAuth", applicant.getAuthenticationState());
                recordList.add(recordMap);
            }

            teacherMap.put("records", recordList);
            teacherMap.put("feedbacks", new ArrayList<>());
            result.add(teacherMap);
        }
        return result;
    }

    private List<Map<String, Object>> mergeAlterRecords(Date startDate, Date endDate,
                                                        List<Map<String, Object>> yang,
                                                        List<Map<String, Object>> yin) {
        Map<String, Map<String, Object>> returnMap = new LinkedHashMap<>();

        // 初始化一个从结束日期到开始日期为Key的Map
        for (Date currentDate = endDate; hourDiff(currentDate, startDate) >= 12L; currentDate = calculateDateDay(currentDate, -1)) {
            String dateStr = dateToString(currentDate, DATE_FORMAT);
            Map<String, Object> dateMap = new HashMap<>();
            dateMap.put("recordDate", dateStr);
            dateMap.put("totalCnt", 0);

            dateMap.put("tranSucCnt", 0);
            dateMap.put("tranCanCnt", 0);
            dateMap.put("tranTotal", 0);
            dateMap.put("tranRatio", "0.00");

            dateMap.put("repSucCnt", 0);
            dateMap.put("repCanCnt", 0);
            dateMap.put("repTotal", 0);
            dateMap.put("repRatio", "0.00");

            dateMap.put("lnkSucCnt", 0);
            dateMap.put("lnkCanCnt", 0);
            dateMap.put("lnkTotal", 0);
            dateMap.put("lnkRatio", "0.00");

            dateMap.put("fakeCnt", 0);
            returnMap.put(dateStr, dateMap);
        }
        // 将第一个统计数据根据日期键值put进去
        DecimalFormat df = new DecimalFormat("#0.00");
        yang.forEach(record -> {
            String dateKey = SafeConverter.toString(record.get("recordDate"));
            if (returnMap.keySet().contains(dateKey)) {
                Map<String, Object> dateMap = returnMap.get(dateKey);
                int tranSucCnt = SafeConverter.toInt(record.get("tranSucCnt"), 0);
                int tranCanCnt = SafeConverter.toInt(record.get("tranCanCnt"), 0);
                int tranTotal = tranSucCnt + tranCanCnt;
                String tranRatio = tranTotal == 0 ? "0.00" : df.format(tranSucCnt * 1.0 / tranTotal * 100.0);

                int repSucCnt = SafeConverter.toInt(record.get("repSucCnt"), 0);
                int repCanCnt = SafeConverter.toInt(record.get("repCanCnt"), 0);
                int repTotal = repSucCnt + repCanCnt;
                String repRatio = repTotal == 0 ? "0.00" : df.format(repSucCnt * 1.0 / repTotal * 100.0);

                int lnkSucCnt = SafeConverter.toInt(record.get("lnkSucCnt"), 0);
                int lnkCanCnt = SafeConverter.toInt(record.get("lnkCanCnt"), 0);
                int lnkTotal = lnkSucCnt + lnkCanCnt;
                String lnkRatio = lnkTotal == 0 ? "0.00" : df.format(lnkSucCnt * 1.0 / lnkTotal * 100.0);

                int totalCnt = tranTotal + repTotal + lnkTotal;

                dateMap.put("totalCnt", totalCnt);

                dateMap.put("tranSucCnt", tranSucCnt);
                dateMap.put("tranCanCnt", tranCanCnt);
                dateMap.put("tranTotal", tranTotal);
                dateMap.put("tranRatio", tranRatio);

                dateMap.put("repSucCnt", repSucCnt);
                dateMap.put("repCanCnt", repCanCnt);
                dateMap.put("repTotal", repTotal);
                dateMap.put("repRatio", repRatio);

                dateMap.put("lnkSucCnt", lnkSucCnt);
                dateMap.put("lnkCanCnt", lnkCanCnt);
                dateMap.put("lnkTotal", lnkTotal);
                dateMap.put("lnkRatio", lnkRatio);
            }
        });
        // 将第二个统计数据根据日期键值put进去
        yin.forEach(record -> {
            String dateKey = SafeConverter.toString(record.get("recordDate"));
            if (returnMap.keySet().contains(dateKey)) {
                Map<String, Object> dateMap = returnMap.get(dateKey);
                dateMap.put("fakeCnt", SafeConverter.toInt(record.get("fakeCnt"), 0));
            }
        });

        return returnMap.values().stream().collect(Collectors.toList());
    }

    private Map<String, Date> defaultDateRange(String start, String end) {
        final Date defaultStart = calculateDateDay(new Date(), -30);
        final Date defaultEnd = calculateDateDay(new Date(), -3);
        Date startDate;
        Date endDate;
        if (StringUtils.isBlank(start) || stringToDate(start, FORMAT_SQL_DATE) == null) {
            startDate = defaultStart;
        } else {
            startDate = stringToDate(start, FORMAT_SQL_DATE);
        }

        if (StringUtils.isBlank(end) || stringToDate(start, FORMAT_SQL_DATE) == null) {
            endDate = defaultEnd;
        } else {
            endDate = stringToDate(end, FORMAT_SQL_DATE);
        }

        endDate = endDate.after(defaultEnd) ? defaultEnd : endDate;

        Map<String, Date> dateMap = new HashMap<>();
        startDate = DayRange.newInstance(startDate.getTime()).getStartDate();
        endDate = DayRange.newInstance(endDate.getTime()).getEndDate();
        dateMap.put("startDate", startDate);
        dateMap.put("endDate", endDate);
        return dateMap;
    }
}
