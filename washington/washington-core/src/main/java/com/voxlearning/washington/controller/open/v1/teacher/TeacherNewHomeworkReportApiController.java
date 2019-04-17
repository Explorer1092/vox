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

package com.voxlearning.washington.controller.open.v1.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkStatus;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.CorrectHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.washington.constant.TeacherReportType;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import com.voxlearning.washington.net.message.exam.CorrectQuestionRequest;
import com.voxlearning.washington.net.message.exam.SaveCorrectQuestionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST;
import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.REQ_HOMEWORK_ID;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.*;

/**
 * 新体系老师作业报告
 *
 * @author xuesong.zhang
 * @since 2016-04-08
 */
@Controller
@RequestMapping(value = "/v1/teacher/new/report")
@Slf4j
public class TeacherNewHomeworkReportApiController extends AbstractTeacherApiController {

    @Inject private RaikouSDK raikouSDK;

    @Inject
    private DeprecatedGroupLoaderClient groupLoaderClient;

    /**
     * 报告顶部-Tab列表
     */
    @RequestMapping(value = "typelist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage typeList() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_TEACHER_NO_SUBJECT_MSG);
            return resultMap;
        }

        try {
            List<Subject> subjects = teacher.getSubjects();
            List<Map<String, Object>> reportTypeList = new LinkedList<>();
            for (TeacherReportType teacherReportType : TeacherReportType.values()) {
                Map<String, Object> reportTypeMap = new HashMap<>();
                reportTypeMap.put("reportType", teacherReportType);
                reportTypeMap.put("reportTypeName", teacherReportType.getName());
                reportTypeList.add(reportTypeMap);
            }

            if (!subjects.contains(Subject.MATH)) {
                reportTypeList = reportTypeList.stream().filter(r -> !r.get("reportType").equals(TeacherReportType.UNIT_TEST_REPORT)).collect(Collectors.toList());
            }
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(REQ_REPORT_TYPE_LIST, reportTypeList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_TEACHER_NO_SUBJECT_MSG);
        }
        return resultMap;
    }



    /**
     * 获取老师当前所教班组信息
     * xuesong.zhang
     */
    @RequestMapping(value = "teacherclazzlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage teacherClazzList() {
        MapMessage resultMap = new MapMessage();

        try {
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT);
            } else {
                validateRequest();
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher teacher = getCurrentTeacherBySubject();
        String subjectStr = getRequestString(REQ_SUBJECT);

        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }

        try {
            MapMessage message;
            if (StringUtils.isEmpty(subjectStr)) {
                // 表示读取全部学科的班级
                message = newHomeworkContentServiceClient.loadTeachersClazzList(teacherLoaderClient.loadRelTeacherIds(teacher.getId()), Collections.singleton(NewHomeworkType.Normal), false);
            } else {
                message = newHomeworkContentServiceClient.loadTeacherClazzList(teacher, Collections.singleton(NewHomeworkType.Normal), false);
            }
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_CLAZZ_LIST, message.get("clazzList"));
                List<Map<String, Object>> homeworkStatusList = new ArrayList<>();
                for (HomeworkStatus homeworkStatus : HomeworkStatus.values()) {
                    homeworkStatusList.add(MapUtils.m("status", homeworkStatus, "desc", homeworkStatus.getDesc()));
                }
                resultMap.add("homework_status_list", homeworkStatusList);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
        }
        return resultMap;
    }

    /**
     * 首页获取待检查作业列表
     */
    @RequestMapping(value = "uncheckedhomeworklist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadUncheckedHomeworkList() {
        MapMessage resultMap = new MapMessage();
        try {
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT);
            } else {
                validateRequest();
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }

        try {
            List<Map<String, Object>> homeworkList = newHomeworkReportServiceClient.loadTeacherUncheckedHomeworkList(teacher);
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_NEWHOMEWORK_LIST, homeworkList);
            String domain = getWebRequestContext().getWebAppBaseUrl();
            resultMap.add(RES_DOMAIN, domain);
            resultMap.add(RES_REPORT_URL, RES_REPORT_URL_VALUE);
            return resultMap;
        } catch (Exception e) {
            logger.error(getClass().getName() + e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
    }

    @RequestMapping(value = "fetchtypestudent.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchAppNewHomeworkStudentDetail() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            validateRequest(REQ_HOMEWORK_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        Teacher teacher = getCurrentTeacherBySubject();

        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        MapMessage mapMessage = newHomeworkReportServiceClient.fetchAppNewHomeworkStudentDetail(homeworkId, teacher);
        if (mapMessage.isSuccess()) {
            mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mapMessage.add(RES_MESSAGE, mapMessage.getInfo());
        }
        return mapMessage;
    }

    @RequestMapping(value = "urgenewhomework.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage urgeNewHomework() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_STUDENT_LIST, "学生IDS");
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            validateRequired(REQ_IS_CORRECT, "是否是订正");
            validateRequest(REQ_STUDENT_LIST, REQ_HOMEWORK_ID, REQ_IS_CORRECT);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        boolean isCorrect = getRequestBool(REQ_IS_CORRECT);
        List<String> strUserIds = Arrays.asList(StringUtils.split(getRequestString(REQ_STUDENT_LIST), ","));
        Set<Long> userIds = strUserIds.stream().map(SafeConverter::toLong).collect(Collectors.toSet());
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        MapMessage mapMessage = newHomeworkReportServiceClient.urgeNewHomework(homeworkId, teacher, userIds, isCorrect);
        if (mapMessage.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, mapMessage.getInfo());
        }
        return resultMap;
    }


    @RequestMapping(value = "fetchunfinishstudent.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchAppNewHomeworkUnFinishStudentDetail() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            validateRequest(REQ_HOMEWORK_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        Teacher teacher = getCurrentTeacherBySubject();

        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        MapMessage mapMessage = newHomeworkReportServiceClient.fetchAppNewHomeworkUnFinishStudentDetail(homeworkId, teacher);
        if (mapMessage.isSuccess()) {
            mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mapMessage.add(RES_MESSAGE, mapMessage.getInfo());
        }
        return mapMessage;
    }


    @RequestMapping(value = "fetchuncorrectstudent.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchAppNewHomeworkUnCorrectStudentDetail() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            validateRequest(REQ_HOMEWORK_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        Teacher teacher = getCurrentTeacherBySubject();

        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        MapMessage mapMessage = newHomeworkReportServiceClient.fetchAppNewHomeworkUnCorrectStudentDetail(homeworkId, teacher);


        if (mapMessage.isSuccess()) {
            mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mapMessage.add(RES_MESSAGE, mapMessage.getInfo());
        }
        return mapMessage;
    }


    @RequestMapping(value = "fetchtypequestion.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchTypeQuestion() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            validateRequest(REQ_HOMEWORK_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        Teacher teacher = getCurrentTeacherBySubject();

        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        MapMessage mapMessage = newHomeworkReportServiceClient.fetchAppNewHomeworkTypeQuestion(homeworkId, teacher, getCdnBaseUrlAvatarWithSep());
        if (mapMessage.isSuccess()) {
            mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mapMessage.add(RES_MESSAGE, mapMessage.getInfo());
        }
        return mapMessage;
    }

    /**
     * 根据所选班级，出现的作业列表
     * xuesong.zhang
     */
    @RequestMapping(value = "homeworklist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage homeworkList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_PAGE_NUMBER, "页数");
            validateRequired(REQ_HOMEWORK_STATUS, "作业状态");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_CLAZZ_GROUP_IDS, REQ_PAGE_NUMBER, REQ_HOMEWORK_STATUS);
            } else {
                validateRequest(REQ_CLAZZ_GROUP_IDS, REQ_PAGE_NUMBER, REQ_HOMEWORK_STATUS);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String groupIds = getRequestString(REQ_CLAZZ_GROUP_IDS);
        String homeworkStatus = getRequestString(REQ_HOMEWORK_STATUS);
        Integer currentPage = getRequestInt(REQ_PAGE_NUMBER, 1);

        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }

        try {
            String domain = getWebRequestContext().getWebAppBaseUrl();

            Pageable pageable = new PageRequest(currentPage - 1, 10);
            if (StringUtils.isBlank(groupIds)) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_NEWHOMEWORK_LIST, PageableUtils.listToPage(Collections.emptyList(), pageable));
                resultMap.add(RES_DOMAIN, domain);
                resultMap.add(RES_REPORT_URL, RES_REPORT_URL_VALUE);
                resultMap.add(RES_OFFLINEHOMEWORK_URL, RES_OFFLINEHOMEWORK_URL_VALUE);
                resultMap.add(RES_OFFLINEHOMEWORK_DETAIL_URL, RES_OFFLINEHOMEWORK_DETAIL_URL_VALUE);
                return resultMap;
            }

            Set<Long> tids = teacherLoaderClient.loadRelTeacherIds(teacher.getId());

            //获取老师下全部的班组：包含包班制,用于基础必过复习，查询需要全部的班组
            Set<Long> teacherGroupIds = raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient()
                    .findByTeacherIds(tids)
                    .stream()
                    .map(GroupTeacherTuple::getGroupId)
                    .collect(Collectors.toSet());

            List<Long> clazzGroupIds = new ArrayList<>();
            for (String id : groupIds.split(",")) {
                Long groupId = SafeConverter.toLong(id);
                if (!teacherGroupIds.contains(groupId)) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, "没有班组:{" + id + "}操作权限");
                    return resultMap;
                }
                clazzGroupIds.add(groupId);
            }


            // 获取期末复习基础必过部分数据（每个学科一条）
            //Set<Subject> termReviewSubject = new HashSet<>();
            //Map<Long, List<BasicReviewHomeworkPackage>> brhMap = basicReviewHomeworkLoaderClient.loadBasicReviewHomeworkPackageByClazzGroupIds(teacherGroupIds);
            //for (List<BasicReviewHomeworkPackage> brhs : brhMap.values()) {
            //    for (BasicReviewHomeworkPackage brh : brhs) {
            //        termReviewSubject.add(brh.getSubject());
            //    }
            //}
            //List<Map<String, Object>> termReviewHomeworks = new ArrayList<>();
            //boolean expired = new Date().after(NewHomeworkConstants.BASIC_REVIEW_END_DATE);
            //String termReviewStatus = expired ? "已到期" : "进行中";
            //if (termReviewSubject.contains(Subject.ENGLISH)) {
            //    termReviewHomeworks.add(MapUtils.m("name", "基础必过：英语报告", "status", termReviewStatus, "subject", Subject.ENGLISH, "expired", expired));
            //}
            //if (termReviewSubject.contains(Subject.MATH)) {
            //    termReviewHomeworks.add(MapUtils.m("name", "基础必过：数学报告", "status", termReviewStatus, "subject", Subject.MATH, "expired", expired));
            //}
            //if (termReviewSubject.contains(Subject.CHINESE)) {
            //    termReviewHomeworks.add(MapUtils.m("name", "基础必过：语文报告", "status", termReviewStatus, "subject", Subject.CHINESE, "expired", expired));
            //}
            HomeworkStatus status = HomeworkStatus.of(homeworkStatus);
            Page<Map<String, Object>> page;
            page = newHomeworkReportServiceClient.pageHomeworkReportListByGroupIdsAndHomeworkStatus(clazzGroupIds, pageable, teacher.getSubject(), status);
            resultMap.add(RES_NEWHOMEWORK_LIST, page);
            //resultMap.add(RES_NEWHOMEWORK_TERMREVIEW_LIST, termReviewHomeworks);
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_DOMAIN, domain);
            resultMap.add(RES_HOMEWORK_SHARE_URL, "/view/reportv5/share");
            resultMap.add(RES_HOMEWORK_NEED_SHARE, true);
            resultMap.add(RES_REPORT_URL, RES_REPORT_URL_VALUE);
            resultMap.add(RES_OFFLINEHOMEWORK_URL, RES_OFFLINEHOMEWORK_URL_VALUE);
            resultMap.add(RES_OFFLINEHOMEWORK_DETAIL_URL, RES_OFFLINEHOMEWORK_DETAIL_URL_VALUE);
            return resultMap;
        } catch (Exception e) {
            logger.error(getClass().getName() + e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
    }

    /**
     * 检查作业
     * xuesong.zhang
     */
    @RequestMapping(value = "homeworkcheck.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage homeworkCheck() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_HOMEWORK_ID);
            } else {
                validateRequest(REQ_HOMEWORK_ID);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        NewHomework homework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (homework == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_HOMEWORK_NOT_EXIST);
            return resultMap;
        }

        // 权限检查
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(currentUserId(), homework.getClazzGroupId())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }

        String homeworkShareUrl;
        if (!NewHomeworkType.OCR.equals(homework.getNewHomeworkType())) {
            if (homework.getIncludeIntelligentTeaching() != null && homework.getIncludeIntelligentTeaching() && homework.getCreateAt().after(NewHomeworkConstants.REMIND_CORRECTION_START_DATE)) {
                homeworkShareUrl = UrlUtils.buildUrlQuery("/view/mobile/teacher/junior/similarrecommend/individualization_consolidate.vpage",
                        MapUtils.m("homeworkIds", homework.getId()));
            } else {
                homeworkShareUrl = UrlUtils.buildUrlQuery("/view/reportv5/share",
                        MapUtils.m("homeworkIds", homework.getId()));
            }
        } else {
            homeworkShareUrl = UrlUtils.buildUrlQuery("/view/ocrhomeworkreport/share",
                    MapUtils.m(
                            "subject", homework.getSubject(),
                            "homeworkIds", homework.getId()));
        }

        try {
            MapMessage message = newHomeworkServiceClient.checkHomework(teacher, homeworkId, HomeworkSourceType.App);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_MESSAGE, RES_RESULT_CHECK_SUCCESS_MSG);
                resultMap.add(RES_HOMEWORK_SHARE_URL, homeworkShareUrl);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
            return resultMap;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_DATA_PROCESSING_MSG);
            return resultMap;
        }
    }

    /**
     * 检查作业
     * xuesong.zhang
     */
    @RequestMapping(value = "batchcheck.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage homeworkBatchCheck() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_IDS, "作业IDS");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_HOMEWORK_IDS);
            } else {
                validateRequest(REQ_HOMEWORK_IDS);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String homeworkIds = getRequestString(REQ_HOMEWORK_IDS);
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }

        List<String> hids = new ArrayList<>();
        for (String hid : homeworkIds.split(",")) {
            hids.add(hid);
        }
        Map<String, NewHomework> homeworkMap = newHomeworkLoaderClient.loadNewHomeworks(hids);
        for (NewHomework newHomework : homeworkMap.values()) {
            if (newHomework == null) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_HOMEWORK_NOT_EXIST);
                return resultMap;
            }
            // 权限检查
            if (!teacherLoaderClient.hasRelTeacherTeachingGroup(currentUserId(), newHomework.getClazzGroupId())) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
                return resultMap;
            }
        }
        List<String> correctHids = new ArrayList<>();
        List<String> normalHids = new ArrayList<>();
        for (NewHomework newHomework : homeworkMap.values()) {
            if (newHomework.getIncludeIntelligentTeaching() != null && newHomework.getIncludeIntelligentTeaching() && newHomework.getCreateAt().after(NewHomeworkConstants.REMIND_CORRECTION_START_DATE)) {
                correctHids.add(newHomework.getId());
            } else {
                normalHids.add(newHomework.getId());
            }
        }

        String homeworkShareUrl;
        if (CollectionUtils.isNotEmpty(correctHids)) {
            homeworkShareUrl = UrlUtils.buildUrlQuery("/view/mobile/teacher/junior/similarrecommend/individualization_consolidate.vpage",
                    MapUtils.m("homeworkIds", StringUtils.join(correctHids, ",")));
        } else {
            homeworkShareUrl = UrlUtils.buildUrlQuery("/view/reportv5/share",
                    MapUtils.m("homeworkIds", StringUtils.join(normalHids, ",")));
        }

        try {
            MapMessage message = newHomeworkServiceClient.batchCheckHomework(teacher, homeworkIds, HomeworkSourceType.App);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_MESSAGE, RES_RESULT_CHECK_SUCCESS_MSG);
                resultMap.add(RES_HOMEWORK_SHARE_URL, homeworkShareUrl);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
            return resultMap;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_DATA_PROCESSING_MSG);
            return resultMap;
        }
    }

    /**
     * 调整作业入口数据
     * xuesong.zhang
     */
    @RequestMapping(value = "adjust/index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage adjustHomeworkInfo() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            validateRequest(REQ_HOMEWORK_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        try {
            Teacher teacher = getCurrentTeacher();
            if (teacher == null) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
                return resultMap;
            }
            NewHomework homework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
            if (homework == null) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_HOMEWORK_NOT_EXIST);
                return resultMap;
            }
            // 权限检查
            if (!teacherLoaderClient.hasRelTeacherTeachingGroup(currentUserId(), homework.getClazzGroupId())) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
                return resultMap;
            }

            List<Map<String, Object>> practices = homework.getPractices().stream().map(hc -> MapUtils.m("objective_config_type", hc.getType().getValue(),
                    "question_count", ObjectiveConfigType.OCR_MENTAL_ARITHMETIC == hc.getType() ? 1 : (hc.getQuestions() != null ? hc.getQuestions().size() : hc.getApps().size()))).collect(Collectors.toList());
            resultMap.add("practices", practices);
            resultMap.add(RES_HOMEWORK_PAST_DUE, homework.getDuration());
            resultMap.add(RES_HOMEWORK_START_DATE, DateUtils.dateToString(homework.getStartTime(), DateUtils.FORMAT_SQL_DATE));
            resultMap.add(RES_HOMEWORK_END_DATE, DateUtils.dateToString(homework.getEndTime()));
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        return resultMap;
    }

    /**
     * 调整作业提交数据
     * xuesong.zhang
     */
    @RequestMapping(value = "homeworkadjust.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage homeworkAdjust() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            // 为了方便数据传输，还是用long吧
            validateRequiredNumber(REQ_HOMEWORK_ENDTIME, "作业结束时间");
            validateRequest(REQ_HOMEWORK_ID, REQ_HOMEWORK_ENDTIME);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        Long endTime = getRequestLong(REQ_HOMEWORK_ENDTIME);
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }

        try {
            Date endDate = new Date(endTime);
            long currentTime = new Date().getTime();
            if (endDate.getTime() < currentTime) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_HOMEWORK_ENDDATE_ERROR_MSG);
                return resultMap;
            }
            MapMessage message = newHomeworkServiceClient.adjustHomework(teacher.getId(), homeworkId, endDate);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_MESSAGE, RES_RESULT_ADJUST_SUCCESS_MSG);
                resultMap.putAll(message);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
            return resultMap;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
    }

    /**
     * 删除作业
     * xuesong.zhang
     */
    @RequestMapping(value = "homeworkdelete.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage homeworkDelete() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            validateRequest(REQ_HOMEWORK_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }

        MapMessage message = newHomeworkServiceClient.deleteHomework(teacher.getId(), homeworkId);
        if (message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_MESSAGE, RES_RESULT_DELETE_SUCCESS_MSG);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, message.getInfo());
        }
        return resultMap;
    }

    /**
     * 已做和未做作业的学生信息
     * xuesong.zhang
     */
    @RequestMapping(value = "homeworkfinishinfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage homeworkFinishInfo() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            validateRequest(REQ_HOMEWORK_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);

        if (newHomework == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_HOMEWORK_NOT_EXIST);
            return resultMap;
        }
        // 权限检查
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(currentUserId(), newHomework.getClazzGroupId())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }

        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId()).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        Map<String, List<Map<String, Object>>> result = newHomeworkResultLoaderClient.homeworkCommentAndIntegralInfo(userMap, newHomework);
        String version = getRequestString(REQ_APP_NATIVE_VERSION);

        if (result.containsKey("doUser")) {
            List<Map<String, Object>> doUser = result.get("doUser");
            if (CollectionUtils.isNotEmpty(doUser)) {
                doUser.forEach(o -> o.put("imageUrl", getUserAvatarImgUrl(SafeConverter.toString(o.get("imageUrl"), ""))));
            }
        }
        if (result.containsKey("undoUser")) {
            List<Map<String, Object>> undoUser = result.get("undoUser");
            if (CollectionUtils.isNotEmpty(undoUser)) {
                undoUser.forEach(o -> o.put("imageUrl", getUserAvatarImgUrl(SafeConverter.toString(o.get("imageUrl"), ""))));
            }
        }

        int subjectiveCount = 0;
        for (NewHomeworkPracticeContent practice : newHomework.getPractices()) {
            if (NewHomeworkConstants.NOT_SHOW_SCORE_TYPE.contains(practice.getType())) {
                subjectiveCount++;
            }
        }
        /**
         * 1.4.7之前的版本返给前端格式，和1.4.7及以后的版本返回的格式不一致因为要支持删除老师自定义评语，所以返回评语模版要告诉前端那些可以删
         * 新增老师自定义评语模版，哎这功能上了，下了，现在又要上反复沟通后，产品还是执意要上，无语了。
         */
        if (StringUtils.isBlank(version) || VersionUtil.compareVersion(version, "1.4.7") < 0) {
            resultMap.put(RES_HOMEWORK_COMMENT_TEMPLATE, NewHomeworkConstants.commentTemplate(teacher.getSubject()));
            resultMap.put(RES_HOMEWORK_DO_USER, result.get("doUser"));
            resultMap.put(RES_HOMEWORK_UNDO_USER, result.get("undoUser"));
            resultMap.put(RES_HOMEWORK_PART1_USER, result.get("list90to100"));
            resultMap.put(RES_HOMEWORK_PART2_USER, result.get("list80to89"));
            resultMap.put(RES_HOMEWORK_PART3_USER, result.get("list60to79"));
            resultMap.put(RES_HOMEWORK_PART4_USER, result.get("list60"));
            resultMap.put(RES_HOMEWORK_FINISHED_USER, result.get("finisheds"));
            resultMap.put(RES_HOMEWORK_UNFINISHED_USER, result.get("unfinisheds"));
        } else {
            List<String> commentTemplates = NewHomeworkConstants.commentTemplate(teacher.getSubject());
            List<String> userComments = newHomeworkCacheServiceClient.getNewHomeworkCacheService().teacherNewHomeworkCommentLibraryManager_find(teacher.getId());
            List<Map<String, Object>> comments = new ArrayList<>();
            userComments.forEach(c -> comments.add(MapUtils.m("comment", c, "deletable", true)));
            commentTemplates.forEach(c -> comments.add(MapUtils.m("comment", c, "deletable", false)));
            resultMap.put(RES_HOMEWORK_COMMENT_TEMPLATE, comments);
            resultMap.put(RES_HOMEWORK_DO_USER, result.get("doUser"));
            resultMap.put(RES_HOMEWORK_UNDO_USER, result.get("undoUser"));
            resultMap.put(RES_HOMEWORK_PART0_USER, result.get("list100"));
            resultMap.put(RES_HOMEWORK_PART1_USER, result.get("list90to99"));
            resultMap.put(RES_HOMEWORK_PART2_USER, result.get("list80to89"));
            resultMap.put(RES_HOMEWORK_PART3_USER, result.get("list60to79"));
            resultMap.put(RES_HOMEWORK_PART4_USER, result.get("list60"));
            resultMap.put(RES_HOMEWORK_FINISHED_USER, result.get("finisheds"));
            resultMap.put(RES_HOMEWORK_UNFINISHED_USER, result.get("unfinisheds"));
            List<Map<String, Object>> resultUser = new LinkedList<>();
            if (subjectiveCount != newHomework.getPractices().size()) {
                resultUser.add(MapUtils.m(
                        "tabName", "100分",
                        "users", result.get("list100")
                                .stream()
                                .sorted((o1, o2) -> Integer.compare(SafeConverter.toInt(o2.get("score")), SafeConverter.toInt(o1.get("score"))))
                                .collect(Collectors.toList())
                ));
                resultUser.add(MapUtils.m(
                        "tabName", "90-99分",
                        "users", result.get("list90to99")
                                .stream()
                                .sorted((o1, o2) -> Integer.compare(SafeConverter.toInt(o2.get("score")), SafeConverter.toInt(o1.get("score"))))
                                .collect(Collectors.toList())
                ));
                resultUser.add(MapUtils.m(
                        "tabName", "80-89分",
                        "users", result.get("list80to89")
                                .stream()
                                .sorted((o1, o2) -> Integer.compare(SafeConverter.toInt(o2.get("score")), SafeConverter.toInt(o1.get("score"))))
                                .collect(Collectors.toList())
                ));
                resultUser.add(MapUtils.m(
                        "tabName", "60-79分",
                        "users", result.get("list60to79")
                                .stream()
                                .sorted((o1, o2) -> Integer.compare(SafeConverter.toInt(o2.get("score")), SafeConverter.toInt(o1.get("score"))))
                                .collect(Collectors.toList())
                ));
                resultUser.add(MapUtils.m(
                        "tabName", "<60分",
                        "users", result.get("list60")
                                .stream()
                                .sorted((o1, o2) -> Integer.compare(SafeConverter.toInt(o2.get("score")), SafeConverter.toInt(o1.get("score"))))
                                .collect(Collectors.toList())
                ));
                resultUser.add(MapUtils.m(
                        "tabName", "未完成",
                        "users", result.get("unfinisheds")
                ));
            } else {
                resultUser.add(MapUtils.m(
                        "tabName", "已完成",
                        "users", result.get("finisheds")
                ));
                resultUser.add(MapUtils.m(
                        "tabName", "未完成",
                        "users", result.get("unfinisheds")
                ));
            }


            resultMap.put(RES_HOMEWORK_RESULT_USERS, resultUser);
        }


        resultMap.add(RES_HOMEWORK_ONLY_SUBJECTIVE, subjectiveCount == newHomework.getPractices().size());
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }


    /**
     * 写评语（批评和单评在一起）
     * xuesong.zhang
     */
    @RequestMapping(value = "homeworkwritecomment.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage homeworkWriteComment() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_COMMENT, "评论内容");
            validateRequired(REQ_STUDENT_LIST, "学生IDS");
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            validateRequest(REQ_HOMEWORK_COMMENT, REQ_STUDENT_LIST, REQ_HOMEWORK_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String comment = getRequestString(REQ_HOMEWORK_COMMENT);
        if (comment.length() > 100) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_COMMENT_TOO_LONG_MSG);
            return resultMap;
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        List<String> strUserIds = Arrays.asList(StringUtils.split(getRequestString(REQ_STUDENT_LIST), ","));
        Set<Long> userIds = strUserIds.stream().map(SafeConverter::toLong).collect(Collectors.toSet());
        if (StringUtils.isAnyBlank(comment, homeworkId) || CollectionUtils.isEmpty(userIds)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_COMMENT_FAILE_MSG);
            return resultMap;
        }
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        MapMessage message = newHomeworkServiceClient.batchSaveNewHomeworkComment(teacher, homeworkId, userIds, comment, null);
        if (message.isSuccess()) {
            List<String> commentTemplates = NewHomeworkConstants.commentTemplate(teacher.getSubject());
            if (!commentTemplates.contains(comment)) {
                newHomeworkCacheServiceClient.getNewHomeworkCacheService().teacherNewHomeworkCommentLibraryManager_addComment(teacher.getId(), comment);
            }
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, message.getInfo());
        }
        return resultMap;
    }


    /**
     * 老师写（文字评语、音频评语）同时奖励学豆
     */
    @RequestMapping(value = "writecommentaddintegral.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage writeCommentAddIntegral() {
        MapMessage resultMap = new MapMessage();
        String comment = getRequestString(REQ_HOMEWORK_COMMENT);
        String audioComment = getRequestString(REQ_HOMEWORK_AUDIO_COMMENT);
        String json = getRequestString(REQ_INTEGRAL_USER_JSON);
        //***** begin needComment 是否需要评语 ***********/
        boolean needComment = true;
        boolean needIntegral = true;
        if (StringUtils.isBlank(comment) && StringUtils.isBlank(audioComment)) {
            needComment = false;
        }

        if (StringUtils.isBlank(json)) {
            needIntegral = false;
        }

        try {
            validateRequired(REQ_HOMEWORK_ID, "作业id");
            //其余几个参数可以为空字符串
            validateRequest(
                    REQ_HOMEWORK_COMMENT,
                    REQ_HOMEWORK_AUDIO_COMMENT,
                    REQ_HOMEWORK_ID,
                    REQ_STUDENT_LIST,
                    REQ_INTEGRAL_USER_JSON
            );
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        if (comment.length() > 100) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_COMMENT_TOO_LONG_MSG);
            return resultMap;
        }

        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        try {
            List<String> strUserIds = Arrays.asList(StringUtils.split(getRequestString(REQ_STUDENT_LIST), ","));
            Set<Long> userIds = strUserIds.stream().map(SafeConverter::toLong).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(userIds)
                    || StringUtils.isBlank(homeworkId)
                    || (StringUtils.isBlank(comment) && StringUtils.isBlank(audioComment))) {
                needComment = false;
            }
            //***** end needComment 是否需要评语 ***********/
            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
            if (newHomework == null) {
                resultMap.add(RES_RESULT, ERROR_CODE_HOMEWORK_NOT_EXIST);
                resultMap.add(RES_MESSAGE, RES_RESULT_HOMEWORK_NOT_EXIST);
                return resultMap;
            }
            if (newHomework.getClazzGroupId() == null) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, "作业不存在班组");
                return resultMap;
            }


            GroupMapper group = groupLoaderClient.loadGroup(newHomework.getClazzGroupId(), true);
            if (group == null) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, "班组不存在");
                return resultMap;
            }
            Long clazzId = group.getClazzId();
            Teacher teacher = getCurrentTeacherBySubject(newHomework.getSubject());
            if (teacher == null) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
                return resultMap;
            }
            if (!(needComment || needIntegral)) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, "请填写奖励或评语");
                return resultMap;
            }

            //***** begin needComment 评语部分 ***********/
            if (needComment) {
                resultMap = new MapMessage();
                MapMessage message = newHomeworkServiceClient.batchSaveNewHomeworkComment(
                        teacher,
                        homeworkId,
                        userIds,
                        comment,
                        audioComment
                );
                if (message.isSuccess()) {
                    List<String> commentTemplates = NewHomeworkConstants.commentTemplate(teacher.getSubject());
                    if (!commentTemplates.contains(comment)) {
                        newHomeworkCacheServiceClient.getNewHomeworkCacheService().teacherNewHomeworkCommentLibraryManager_addComment(teacher.getId(), comment);
                    }
                    resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                } else {
                    resultMap.add(RES_MESSAGE, message.getInfo());
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    return resultMap;
                }
            }
            //***** end needComment 评语部分 ***********/
            //***** begin  发奖励 ***********/
            if (needIntegral) {
                resultMap = new MapMessage();
                Map<String, Object> jsonMap = new LinkedHashMap<>();
                List<Map> details = JsonUtils.fromJsonToList(json, Map.class);
                if (details == null) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, "参数错误");
                    return resultMap;
                }
                jsonMap.put("details", details);
                jsonMap.put("homeworkId", homeworkId);
                jsonMap.put("subject", newHomework.getSubject());
                jsonMap.put("clazzId", clazzId);
                if (!jsonMap.containsKey("subject")) {
                    jsonMap.put("subject", teacher.getSubject().name());
                }
                MapMessage message = atomicLockManager.wrapAtomic(newHomeworkServiceClient)
                        .keys("TeacherNewHomeworkApiController::batchSendIntegral", teacher.getId())
                        .proxy()
                        .batchRewardStudentIntegral(teacher.getId(), jsonMap);
                if (message.isSuccess()) {
                    resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                } else {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, message.getInfo());
                }
            }
            //***** end  发奖励 ***********/
            return resultMap;
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_DATA_PROCESSING_MSG);
                return resultMap;
            }
            logger.error(getClass().getName() + ex.getMessage(), ex);
            return resultMap;
        }
    }

    /**
     * 删除老师自定义评语
     */
    @RequestMapping(value = "removehomeworkcomment.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage removeHomeworkComment() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_COMMENT, "评论内容");
            validateRequest(REQ_HOMEWORK_COMMENT);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String comment = getRequestString(REQ_HOMEWORK_COMMENT);
        if (comment.length() > 100) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_COMMENT_TOO_LONG_MSG);
            return resultMap;
        }

        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        Boolean removeComment = newHomeworkCacheServiceClient.getNewHomeworkCacheService().teacherNewHomeworkCommentLibraryManager_removeComment(teacher.getId(), comment);
        if (removeComment) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "此评语不在你的自定义评语列表里面");
        }
        return resultMap;
    }

    /**
     * 发学豆
     * xuesong.zhang
     */
    @RequestMapping(value = "batchsendintegral.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage batchSendIntegral() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_INTEGRAL_USER_JSON, "发学豆json");
            validateRequest(REQ_INTEGRAL_USER_JSON);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // json格式：{"clazzId":"1063319","details":[{"studentId":333894383,"count":5}]}
        String json = getRequestString(REQ_INTEGRAL_USER_JSON);
        try {
            Map<String, Object> jsonMap = JsonUtils.fromJson(json);
            Teacher teacher = getCurrentTeacherBySubject(Subject.safeParse(SafeConverter.toString(jsonMap.get("subject"))));
            if (teacher == null) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
                return resultMap;
            }

            if (!jsonMap.containsKey("subject")) {
                jsonMap.put("subject", teacher.getSubject().name());
            }

            MapMessage message = atomicLockManager.wrapAtomic(newHomeworkServiceClient)
                    .keys("TeacherNewHomeworkApiController::batchSendIntegral", teacher.getId())
                    .proxy()
                    .batchRewardStudentIntegral(teacher.getId(), jsonMap);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
            return resultMap;
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_DATA_PROCESSING_MSG);
                return resultMap;
            }
            logger.error(getClass().getName() + ex.getMessage(), ex);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_OPERATION_FAILED_MSG);
            return resultMap;
        }
    }

    /**
     * 批改作业-去批改
     * 这里只显示某次作业中尚未批改部分的题目及主观作答信息
     * xuesong.zhang
     */
    @RequestMapping(value = "needcorrect.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage needCorrect() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            validateRequest(REQ_HOMEWORK_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);

        if (newHomework == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_HOMEWORK_NOT_EXIST);
            return resultMap;
        }
        // 权限检查
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), newHomework.getClazzGroupId())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }

        MapMessage message = newHomeworkReportServiceClient.loadNewHomeworkNeedCorrect(homeworkId, teacher.getId());
        if (message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, message.getInfo());
            return resultMap;
        }
        return resultMap;
    }

    /**
     * 批改作业-批改数据提交
     * xuesong.zhang
     */
    @RequestMapping(value = "batchcorrectquestion.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage batchCorrectQuestion() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CORRECT_JSON, "批改数据json");
            validateRequest(REQ_CORRECT_JSON);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        String data = getRequestString(REQ_CORRECT_JSON);
        SaveCorrectQuestionRequest reqObj = JsonUtils.fromJson(data, SaveCorrectQuestionRequest.class);
        if (reqObj == null || StringUtils.isBlank(reqObj.getHomeworkId())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_OPERATION_FAILED_MSG);
            return resultMap;
        }

        NewHomework homework = newHomeworkLoaderClient.loadNewHomework(reqObj.getHomeworkId());
        if (homework == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_HOMEWORK_NOT_EXIST);
            return resultMap;
        }
        if (homework.getCreateAt() != null && homework.getCreateAt().before(NewHomeworkConstants.ALLOW_UPDATE_HOMEWORK_START_TIME)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_HOMEWORK_NOT_ALLOW_CORRECT);
            return resultMap;
        }

        if (Objects.equals(Boolean.TRUE, reqObj.getIsBatch())) {
            // 批量
            newHomeworkServiceClient.batchSaveHomeworkCorrect(reqObj.getHomeworkId(), teacher.getId());
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_MESSAGE, RES_RESULT_CORRECT_SUCCESS_MSG);
            return resultMap;
        } else {
            // 非批量
            List<CorrectQuestionRequest> corrections = reqObj.getCorrections();
            List<CorrectQuestionRequest> returnInfo = new ArrayList<>();
            for (CorrectQuestionRequest obj : corrections) {
                Boolean b = saveCorrectQuestionResult(obj.getUserId(), reqObj.getHomeworkId(), reqObj.getType(), reqObj.getQuestionId(), obj, reqObj.getIsBatch());
                if (Objects.equals(Boolean.TRUE, b)) {
                    returnInfo.add(obj);
                }
            }
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_MESSAGE, RES_RESULT_CORRECT_SUCCESS_MSG);
            resultMap.add(RES_QUESTION_INFOS, returnInfo);
            return resultMap;
        }
    }

    private Boolean saveCorrectQuestionResult(Long studentId, String homeworkId, ObjectiveConfigType type, String questionId, CorrectQuestionRequest correct, Boolean isBatch) {
        CorrectHomeworkContext context = new CorrectHomeworkContext();
        context.setStudentId(studentId);
        if (type == ObjectiveConfigType.NEW_READ_RECITE) {
            context.setQuestionBoxId(questionId);
        }
        context.setHomeworkId(homeworkId);
        context.setQuestionId(questionId);
        context.setReview(correct.getReview());
        context.setType(type);
        context.setCorrectType(correct.getCorrectType());
        context.setCorrection(correct.getCorrection());
        context.setTeacherMark(correct.getTeacherMark());
        context.setIsBatch(isBatch);
        return newHomeworkProcessResultLoaderClient.updateCorrection(context);
    }

    /**
     * 诊断做题习惯详情
     */
    @RequestMapping(value = "diagnosis/habit/detail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage diagnosisHabitDetail() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            validateRequest(REQ_HOMEWORK_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);

        if (newHomework == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_HOMEWORK_NOT_EXIST);
            return resultMap;
        }

        MapMessage message = newHomeworkReportServiceClient.loadDiagnosisHabitDetail(homeworkId);
        if (message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_DIAGNOSIS_HABIT_DETAIL, message.get("diagnosisHabitDetail"));
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, message.getInfo());
            return resultMap;
        }
        return resultMap;
    }

    /**
     * 纸质作业-报告详情
     *
     * @return
     */
    @RequestMapping(value = "fetch/ocrhomework/studentdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ocrHomeworkStudentDetail() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业ID");
            validateRequest(REQ_HOMEWORK_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        Teacher teacher = getCurrentTeacherBySubject();

        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        MapMessage mapMessage = newHomeworkReportServiceClient.fetchOcrHomeworkStudentDetail(homeworkId, teacher);
        if (mapMessage.isSuccess()) {
            mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mapMessage.add(RES_MESSAGE, mapMessage.getInfo());
        }
        return mapMessage;
    }
}
