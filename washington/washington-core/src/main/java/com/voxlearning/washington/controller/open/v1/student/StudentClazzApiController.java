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

package com.voxlearning.washington.controller.open.v1.student;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.utopia.api.constant.AppAuditAccounts;
import com.voxlearning.utopia.api.constant.ClazzConstants;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;
import com.voxlearning.washington.controller.open.AbstractStudentApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import com.voxlearning.washington.support.SessionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType.*;
import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.RES_RESULT_CANCEL_BUTTON_TEXT;

/**
 * Student clazz api controller
 * Created by Shuai Huan on 2015/8/10.
 * 接口定义wiki请见 http://wiki.17zuoye.net/pages/viewpage.action?pageId=11501847
 */
@Controller
@RequestMapping(value = "/v1/student/clazz")
@SuppressWarnings("unchecked")
public class StudentClazzApiController extends AbstractStudentApiController {

    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private GlobalTagServiceClient globalTagServiceClient;
    @Inject private GroupLoaderClient groupLoaderClient;
    @Inject private MessageLoaderClient messageLoaderClient;
    @Inject private NewKuailexueLoaderClient newKuailexueLoaderClient;
    @Inject private NewKuailexueServiceClient newKuailexueServiceClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;

    @Inject private RaikouSDK raikouSDK;

    /**
     * wiki参考  http://wiki.17zuoye.net/pages/viewpage.action?pageId=31196931
     */
    @RequestMapping(value = "/checkclazzinfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage checkClazzInfo() {
        MapMessage resultMap = new MapMessage();
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        boolean sidExists = false; // 这个标志是用来区分是注册还是换班，{false = 注册; true = 换班}
        try {
            // 又做个恶心的兼容，大于等于1.7.1版本的app不用验证sessionkey
            validateRequired(REQ_ID, "ID");

            String sessionKey = getRequestString(REQ_SESSION_KEY);

            if (StringUtils.isEmpty(sessionKey)) {
                if (StringUtils.isNotEmpty(getRequestString(REQ_STUDENT_ID))) {
                    sidExists = true;
                    validateDigitNumber(REQ_STUDENT_ID, "学生id");
                    validateRequestNoSessionKey(REQ_ID, REQ_STUDENT_ID);
                } else {
                    validateRequestNoSessionKey(REQ_ID);
                }
            } else {
                if (StringUtils.isNotEmpty(getRequestString(REQ_STUDENT_ID))) {
                    sidExists = true;
                    validateDigitNumber(REQ_STUDENT_ID, "学生id");
                    validateRequest(REQ_ID, REQ_STUDENT_ID);
                } else {
                    validateRequest(REQ_ID);
                }
            }

        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String id = getRequestString(REQ_ID);

        // 读取老师信息
        TeacherDetail studentTeacher = null;
        if (MobileRule.isMobile(id)) {// 输入的是手机号
            UserAuthentication ua = userLoaderClient.loadMobileAuthentication(id, UserType.TEACHER);
            if (ua != null) {
                studentTeacher = teacherLoaderClient.loadTeacherDetail(ua.getId());
            }
        } else {// 输入为老师id
            studentTeacher = teacherLoaderClient.loadTeacherDetail(SafeConverter.toLong(id));
        }

        if (studentTeacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_TEACHER_NUMBER_ERROR_MSG);
            return resultMap;
        }

        // FIXME 西安交通大学附属中学（30636，属于名校的初中部）, 临时禁止登陆和注册
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(studentTeacher.getId());
        if (AppAuditAccounts.isForbiddenStudentRegisterSchool(teacherDetail.getTeacherSchoolId())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "暂不支持登录和注册!");
            return resultMap;
        }

        // 学生端APP低于2.8.4版本时不允许注册和登录学前
        if (isAndroidRequest(getRequest()) && studentTeacher.isInfantTeacher() && VersionUtil.compareVersion(getRequestString(REQ_APP_NATIVE_VERSION), "2.8.4.0") < 0) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNSUPPORT_KETWELVE_INFANT);
            return resultMap;
        }

        String appKey = getRequestString(REQ_APP_KEY);
        boolean kuailexueChoiceFlag = false; // FIXME 这个字段是用来控制要不要填写填涂号的 以及 控制后续要不要调校验klx学生姓名接口
        boolean requireKlxScanNumber = false;   // 新添加一个标志用来控制填涂号是不是必填

        // Feature #46231 老师包班&学生注册打包优化 Update:2017-06-01
        StudentDetail studentDetail = getCurrentStudentDetail();
        //只是学生端返回检验信息
        // 学生APP
        if (StringUtils.equals("17Student", appKey)) {
            // 以下是注册逻辑的校验
            // 注册逻辑为：如果不是小学老师， 检查用户APP的版本必须 大于 2.7.10.0
            if (!sidExists) {
                if (!studentTeacher.isPrimarySchool()) {
                    if (VersionUtil.compareVersion(ver, "2.7.10.0") < 0) {
                        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                        resultMap.add(RES_MESSAGE, RES_RESULT_UNSUPPORT_ANSWER_EXAM);
                        return resultMap;
                    }
                    // 初中or高中的老师 2018-8-15 中学app上线之后, 注册才走这个
                    if (StringUtils.isEmpty(getRequestString(REQ_SESSION_KEY))) {
                        if (juniorStudentAppOnlineTime() && (studentTeacher.isSeniorTeacher() || studentTeacher.isJuniorTeacher())) {
                            resultMap.add(RES_RESULT, RES_RESULT_BUTTON_CODE);
                            resultMap.add(RES_MESSAGE, RES_RESULT_JUNIOR_MESSAGE);
                            resultMap.add(RES_OK_BUTTON, RES_RESULT_OK_BUTTON_TEXT);
                            resultMap.add(RES_OK_BUTTON_ACTION, RES_RESULT_JUNIOR_LINK);
                            resultMap.add(RES_CANCEL_BUTTON, RES_RESULT_CANCEL_BUTTON_TEXT);
                            return resultMap;
                        }
                    }
                    kuailexueChoiceFlag = true;
                }
            }

            // 注册逻辑校验完

            // 以下是换班逻辑校验
            else {
                long reqStudentId = getRequestLong(REQ_STUDENT_ID);
                StudentDetail tempStudentDetail = studentLoaderClient.loadStudentDetail(reqStudentId);
                // 初高中学生 && 小学毕业生
                if (tempStudentDetail != null && tempStudentDetail.getClazz() != null && (tempStudentDetail.getClazz().isTerminalClazz() || !tempStudentDetail.isPrimaryStudent())) {
                    //该老师与学生现在学校不一致 or 该学生目前无阅卷机号
//                    StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(reqStudentId);
                    KlxStudent klxStudent = newKuailexueLoaderClient.loadKlxStudentBy17Id(reqStudentId);
                    School teacherSchool = asyncTeacherServiceClient.getAsyncTeacherService().loadTeacherSchool(studentTeacher.getId()).getUninterruptibly();
                    boolean isSameSchool = tempStudentDetail.getClazz() != null && teacherSchool != null && (Objects.equals(tempStudentDetail.getClazz().getSchoolId(), teacherSchool.getId()));
                    boolean hasScanNumber = klxStudent != null && StringUtils.isNotBlank(klxStudent.getScanNumber());
                    if (!isSameSchool || !hasScanNumber) {
                        if (VersionUtil.compareVersion(ver, "2.7.10.0") < 0) {
                            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                            resultMap.add(RES_MESSAGE, RES_RESULT_UNSUPPORT_ANSWER_EXAM);
                            return resultMap;
                        }
                        kuailexueChoiceFlag = true;
                    }
                }
                // 无 group 学生
                else if (tempStudentDetail != null && tempStudentDetail.getClazz() == null) {
                    // 如果老师是初高中老师，检查用户APP的版本必须 大于 2.7.10.0
                    if (!studentTeacher.isPrimarySchool()) {
                        if (VersionUtil.compareVersion(ver, "2.7.10.0") < 0) {
                            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                            resultMap.add(RES_MESSAGE, RES_RESULT_UNSUPPORT_ANSWER_EXAM);
                            return resultMap;
                        }
                        kuailexueChoiceFlag = true;
                    }
                }
            }
            // 换班逻辑校验完

            // 修正是否要填填涂号标志
            // 如果老师是初中英语老师或者初中语文老师 而且 版本小于 2.8.4, 填涂号不填
            if (kuailexueChoiceFlag) {
                if (studentTeacher.isJuniorEnglishOrChineseTeacher()) {
                    if (VersionUtil.compareVersion(ver, "2.8.4.0") < 0) {
                        kuailexueChoiceFlag = false;
                    }
                } else {
                    // 除了初中英语语文老师，需要必填
                    requireKlxScanNumber = true;
                }
                // 修正学前不需要快乐学标志
                if (studentTeacher.isInfantTeacher()) {
                    kuailexueChoiceFlag = false;
                }
                // 学校如果没有开通了阅卷机权限，也不同填涂号
//                School teacherSchool = asyncTeacherServiceClient.getAsyncTeacherService().loadTeacherSchool(studentTeacher.getId()).getUninterruptibly();
                if (studentTeacher.getTeacherSchoolId() != null) {
                    SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(studentTeacher.getTeacherSchoolId()).getUninterruptibly();
                    if (schoolExtInfo == null || !schoolExtInfo.isScanMachineFlag()) {
                        kuailexueChoiceFlag = false;
                    }
                }
            }
        } else if (StringUtils.equals("17JuniorStu", appKey)) {
            // 中学APP
            if (studentTeacher.isPrimarySchool()) {
                if (!sidExists) {
                    resultMap.add(RES_RESULT, RES_RESULT_BUTTON_CODE);
                    resultMap.add(RES_OK_BUTTON, RES_RESULT_OK_BUTTON_TEXT);
                    resultMap.add(RES_OK_BUTTON_ACTION, RES_RESULT_PRIMARY_LINK);
                    resultMap.add(RES_MESSAGE, RES_RESULT_PRIMARY_MESSAGE);
                    resultMap.add(RES_CANCEL_BUTTON, RES_RESULT_CANCEL_BUTTON_TEXT);
                } else {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_PRIMARY_MESSAGE2);
                }
                return resultMap;
            }

            if (studentTeacher.getTeacherSchoolId() != null) {
                SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(studentTeacher.getTeacherSchoolId()).getUninterruptibly();
                if (schoolExtInfo != null && schoolExtInfo.isScanMachineFlag()) {
                    kuailexueChoiceFlag = true;
                }
            }

            if (kuailexueChoiceFlag) {
                if (studentTeacher.isKLXTeacher()) {
                    requireKlxScanNumber = true;
                }
            }
        }

        // 小学毕业班的学生可以换到小学和初中。其他情况一律限制只能换到当前学制
        Set<Ktwelve> allowedKtwelves;
        if (studentDetail == null || studentDetail.getClazz() == null) {
            allowedKtwelves = new HashSet<>(Arrays.asList(Ktwelve.PRIMARY_SCHOOL, Ktwelve.JUNIOR_SCHOOL, Ktwelve.SENIOR_SCHOOL, Ktwelve.INFANT));
        } else if (studentDetail.getClazz().isTerminalClazz() && studentDetail.isPrimaryStudent()) {  // 小学毕业
            allowedKtwelves = new HashSet<>(Arrays.asList(Ktwelve.PRIMARY_SCHOOL, Ktwelve.JUNIOR_SCHOOL));
        } else if (studentDetail.isPrimaryStudent() && studentDetail.getClazzLevel() == ClazzLevel.SIXTH_GRADE && isPreTerminalPeriod()) {
            allowedKtwelves = new HashSet<>(Arrays.asList(Ktwelve.PRIMARY_SCHOOL, Ktwelve.JUNIOR_SCHOOL));
        } else if (studentDetail.getClazz().isTerminalClazz() && studentDetail.isJuniorStudent()) {
            allowedKtwelves = new HashSet<>(Arrays.asList(Ktwelve.JUNIOR_SCHOOL, Ktwelve.SENIOR_SCHOOL));
        } else if (studentDetail.getClazz().isTerminalClazz() && studentDetail.isInfantStudent()) {
            allowedKtwelves = new HashSet<>(Arrays.asList(Ktwelve.INFANT, Ktwelve.PRIMARY_SCHOOL));
        } else {
            allowedKtwelves = Collections.singleton(studentDetail.getClazz().getEduSystem().getKtwelve());
        }

        Long studentId = studentDetail == null ? null : studentDetail.getId(); // 这个 studentId 貌似很鸡肋啊...

        // 学生加入班级时,获得老师所有班级
        MapMessage message = studentSystemClazzServiceClient.joinClazz_findClazzInfo(id, studentId, allowedKtwelves);
        if (!message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, message.getInfo());
            return resultMap;
        }

        Teacher teacher = (Teacher) message.get("teacher");
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_TEACHER_NUMBER_ERROR_MSG);
            return resultMap;
        }

        if (teacherLoaderClient.isFakeTeacher(teacher.getId())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_TEACHER_STATUS_ERROR_MSG);
            return resultMap;
        }

        List<Map<String, Object>> clazzList = (List<Map<String, Object>>) message.get("clazzList");
        if (clazzList.isEmpty()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_TEACHER_CLAZZ_ERROR_MSG);
            return resultMap;
        }
        List<Long> clazzIds = new ArrayList<>();
        for (Map<String, Object> clazzMap : clazzList) {
            clazzMap.remove("schoolName");
            clazzMap.remove("teachers");
            clazzMap.remove("creatorType");
            clazzMap.put(RES_CLAZZ_ID, clazzMap.remove("clazzId"));
            clazzMap.put(RES_CLAZZ_NAME, clazzMap.remove("clazzName"));
            clazzMap.put(RES_KTWELVE, clazzMap.get("ktwelve"));
            clazzIds.add(SafeConverter.toLong(clazzMap.get(RES_CLAZZ_ID)));
        }

        School school = asyncTeacherServiceClient.getAsyncTeacherService().loadTeacherSchool(teacher.getId())
                .getUninterruptibly();

        // 查找班级是否学生人员是否已满,把状态返回
        Map<Long, GroupMapper> groupMapperMap = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzIds(teacher.getId(), clazzIds, false);
        //支持包班制,如果这个老师有多学科,则查出子账号所有的group....
        if (teacher.getSubjects() != null && teacher.getSubjects().size() > 1) {
            List<Long> teacherRefIds = teacherLoaderClient.loadSubTeacherIds(teacher.getId());
            for (Long t : teacherRefIds) {
                Map<Long, GroupMapper> groupMapperMap1 = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzIds(t, clazzIds, false);
                if (MapUtils.isNotEmpty(groupMapperMap1)) {
                    if (MapUtils.isEmpty(groupMapperMap))
                        groupMapperMap = new HashMap<>();
                    groupMapperMap.putAll(groupMapperMap1);
                }
            }
        }
        List<Long> groupIds = groupMapperMap.values().stream().map(GroupMapper::getId).collect(Collectors.toList());
        Map<Long, List<GroupStudentTuple>> refs = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByGroupIds(groupIds)
                .stream()
                .collect(Collectors.groupingBy(GroupStudentTuple::getGroupId));
        for (Map<String, Object> clazzMap : clazzList) {
            Long clazzId = SafeConverter.toLong(clazzMap.get(RES_CLAZZ_ID));
            GroupMapper mapper = groupMapperMap.get(clazzId);
            if (mapper == null) {
                clazzMap.put(RES_CLAZZ_STATUS, false);
                continue;
            }
            List<GroupStudentTuple> studentRefs = refs.get(mapper.getId());
            if (CollectionUtils.isEmpty(studentRefs)) {
                clazzMap.put(RES_CLAZZ_STATUS, true);
                continue;
            }

            // FIXME 这块有脏数据，有班级的老师但不在任意一个学校里。
            boolean inQuota = true;
            if (school != null) {
                inQuota = studentRefs.size() < globalTagServiceClient.getGlobalTagBuffer()
                        .loadSchoolMaxClassCapacity(school.getId(), ClazzConstants.MAX_CLAZZ_CAPACITY);
            }
            boolean clazzStatus = Boolean.TRUE.equals(mapper.getFreeJoin()) && inQuota;
            clazzMap.put(RES_CLAZZ_STATUS, clazzStatus);
        }

        resultMap.add(RES_KUAILEXUE_CHOICE, kuailexueChoiceFlag);
        resultMap.add(RES_REQUIRE_KLX_SCAN_NUMBER, requireKlxScanNumber);
        resultMap.add(RES_CLAZZ_LIST, message.get("clazzList"));
        resultMap.add(RES_TEACHER_ID, teacher.getId());
        resultMap.add(RES_TEACHER_NAME, teacher.fetchRealname());
        resultMap.add(RES_SUBJECT, teacher.getSubject() == null ? "" : teacher.getSubject().getValue());
        resultMap.add(RES_KTWELVE, teacher.getKtwelve());
        resultMap.add(RES_SCHOOL_NAME, school == null ? "" : school.getCname());
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/joinsystemclazz.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage joinSystemClazz() {
        MapMessage resultMap = new MapMessage();
        boolean scanNumberExists = false;
        try {
            validateRequired(REQ_CLAZZ_ID, "班级ID");
            validateRequired(REQ_TEACHER_ID, "老师ID");
            validateDigitNumber(REQ_CLAZZ_ID, "班级ID");
            validateDigitNumber(REQ_TEACHER_ID, "老师ID");
            if (StringUtils.isNotEmpty(getRequestString(REQ_SCAN_NUMBER))) { //阅卷机号前端可能传,也可能不传
                validateDigitNumber(REQ_SCAN_NUMBER, "阅卷机填涂号");
                validateRequest(REQ_CLAZZ_ID, REQ_TEACHER_ID, REQ_SCAN_NUMBER);
                scanNumberExists = true;
            } else {
                validateRequest(REQ_CLAZZ_ID, REQ_TEACHER_ID);
            }
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Student student = getCurrentStudent();

        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
        Long teacherId = getRequestLong(REQ_TEACHER_ID);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        String scanNumber = getRequestString(REQ_SCAN_NUMBER);
        String name = student.fetchRealname();

        // 先做一下数据检查吧，省的出错了
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher.isSeniorTeacher() || teacher.isJuniorTeacher()) {
            // 有填涂号的，该填涂号已经有对应的17作业学生ID了，无填涂号的，重名
            MapMessage checkResult = newKuailexueServiceClient.checkStudentJoinTeacherGroup(clazzId, teacherId, student.getId(), scanNumber);
            if (!checkResult.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, checkResult.getInfo());
                return resultMap;
            }
        }

        resultMap = joinClazz(student, clazzId, teacherId, false);
        if (!resultMap.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            return resultMap;
        }
        if (teacher.isJuniorTeacher() || teacher.isSeniorTeacher()) {
            // 学校如果没有开通了阅卷机权限，也不同填涂号
            SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(teacher.getTeacherSchoolId()).getUninterruptibly();
            if (schoolExtInfo == null || !schoolExtInfo.isScanMachineFlag()) {
                scanNumberExists = false;
            }
            if (scanNumberExists) {
                newKuailexueServiceClient.linkKlxStudentByScanNumber(clazzId, teacherId, student.getId(), scanNumber);
            } else {
                newKuailexueServiceClient.joinKlxClazz(clazzId, teacherId, student.getId());
            }
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/changesystemclazz.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage changeSystemClazz() {
        MapMessage resultMap = new MapMessage();
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        try {
            validateRequired(REQ_CLAZZ_ID, "班级ID");
            validateRequired(REQ_TEACHER_ID, "老师ID");
            validateDigitNumber(REQ_CLAZZ_ID, "班级ID");
            validateDigitNumber(REQ_TEACHER_ID, "老师ID");
            validateRequired(REQ_VERIFY_CODE, "验证码");
            if (VersionUtil.compareVersion(ver, "2.0.0.0") < 0) {
                validateRequest(REQ_CLAZZ_ID, REQ_TEACHER_ID, REQ_USER_CODE, REQ_VERIFY_CODE);
            } else {
                validateRequest(REQ_CLAZZ_ID, REQ_TEACHER_ID, REQ_VERIFY_CODE);
            }

        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        StudentDetail student = getCurrentStudentDetail();

        // check验证码
        String code = getRequestString(REQ_VERIFY_CODE);

        // 原逻辑：
        // 发送验证码的时候，根据当前登录的学生id，找到家长手机号，如果有就发送了; 如果没有，就用学生自己绑定的手机号; 这里不知道当时用了哪个手机号发送的验证码，而且会有发送时间差; 所以需要两个手机号按次序分别确认
        // 新逻辑，redmine 31437
        // 发送验证码的时候，根据当前登录的学生id，找到学生绑定手机号，如果有就发送了; 如果没有，就用家长的手机号; 这里不知道当时用了哪个手机号发送的验证码，而且会有发送时间差; 所以需要两个手机号按次序分别确认
        String userMobile = sensitiveUserDataServiceClient.loadUserMobile(student.getId());

        MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(userMobile, code, SmsType.APP_STUDENT_CHANGE_CLAZZ_VERIFY_MOBILE.name());
        if (!validateResult.isSuccess()) {
            userMobile = getParentMobileByStudentId(student.getId());
            validateResult = smsServiceClient.getSmsService().verifyValidateCode(userMobile, code, SmsType.APP_STUDENT_CHANGE_CLAZZ_VERIFY_MOBILE.name());
            if (!validateResult.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_VERIFY_CODE_ERROR_MSG);
                return resultMap;
            }
        }

        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
        Long teacherId = getRequestLong(REQ_TEACHER_ID);

        //快乐学学生换班后默认关联group下无17id的用户名和阅卷机填涂号
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_TEACHER_NUMBER_ERROR_MSG);
            return resultMap;
        }

        // 如果学生没绑定手机，就将此手机号绑定学生
        UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(student.getId());
        if (userAuthentication == null || StringUtils.isBlank(userAuthentication.getSensitiveMobile())) {
            userServiceClient.activateUserMobile(student.getId(), userMobile);
        }

        // 学校如果没有开通了阅卷机权限，也不同填涂号
        boolean needScanNumber = false;
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(teacher.getTeacherSchoolId())
                .getUninterruptibly();
        if (schoolExtInfo != null && schoolExtInfo.isScanMachineFlag()) {
            needScanNumber = true;
        }

        if (teacher.isJuniorEnglishOrChineseTeacher()) {
            if (VersionUtil.compareVersion(ver, "2.8.4.0") <= 0) {
                // 走到这一步，表示肯定是异校换班，或者没有填涂号，如果有填涂号，换班之后清除学生之前的填涂号
                KlxStudent klxStudent = newKuailexueLoaderClient.loadKlxStudentBy17Id(student.getId());
                if (klxStudent != null && StringUtils.isNotBlank(klxStudent.getScanNumber())) {
                    Long schoolId = student.getClazz() != null ? null : student.getClazz().getSchoolId();
                    if (schoolId != null) {
                        newKuailexueServiceClient.removeScanNumberFromSchool(schoolId, klxStudent.getScanNumber());
                    }
                }
            }
        }

        // 绑定新的填涂号
        if (needScanNumber && (teacher.isKLXTeacher() || teacher.isJuniorMathTeacher())) {
            newKuailexueServiceClient.linkDefaultKlxStudent(clazzId, teacherId, student.getId());
        }

        resultMap = joinClazz(student, clazzId, teacherId, true);
        if (!resultMap.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            return resultMap;
        }

        // 加入班级之后, 更新一下学生和组之间的关联
        if (needScanNumber && (teacher.isKLXTeacher() || teacher.isJuniorMathTeacher())) {
            newKuailexueServiceClient.joinKlxClazz(clazzId, teacherId, student.getId());
        }


        //换班成功,给原班级老师发离开班级app消息,给新班级老师发加入班级app消息
        //此处可能有坑,底层方法(user-provider)会给pc发消息,但由于user不应该依赖vendor,故只能在操作入口加给老师app发消息
        //sendStudentJoinClazzTeacherAppMessage(student.fetchRealname(),);
        //sendStudentExitClazzTeacherAppMessage(student.fetchRealname(),teacherId,)

        // 换班前后学制不同，直接踢出去重新登录账号
        if (resultMap.get(RES_KTWELVE) instanceof Ktwelve) {
            if (student.getClazz() == null || student.getClazz().getEduSystem() == null
                    || student.getClazz().getEduSystem().getKtwelve() != resultMap.get(RES_KTWELVE)) {

                // 需要把sessionkey过期,同一个账户多端登录,无法全部登出.
                vendorServiceClient.expireSessionKey(getApiRequestApp().getAppKey(),
                        student.getId(),
                        SessionUtils.generateSessionKey(CommonConfiguration.getInstance().getSessionEncryptKey(), student.getId()));
                resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
                resultMap.add(RES_MESSAGE, "成功升学，请重新登录");
                return resultMap;
            }
        }

        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(student.getId());
        userServiceRecord.setOperatorId(SafeConverter.toString(student.getId()));
        userServiceRecord.setOperationType(UserServiceRecordOperationType.班组信息变更.name());
        userServiceRecord.setOperationContent("学生变更班组");
        userServiceRecord.setComments("学生[" + student.getId() + "]加入班组，老师ID[" + teacherId + "]，班组ID[" + clazzId + "]操作端[app]");
        userServiceClient.saveUserServiceRecord(userServiceRecord);

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    private MapMessage joinClazz(Student student, Long clazzId, Long teacherId, boolean forceLink) {
        MapMessage resultMap = new MapMessage();

        // 2016.1.31 学生滥用换班功能刷学霸学豆的处理
        Boolean isValid = studentSystemClazzServiceClient.isValidClazzJoinRequest(teacherId, student.getId());
        if (!isValid) {
            resultMap.setSuccess(false);
            resultMap.add(RES_MESSAGE, "你最近换班次数异常，已被系统禁止换班！");
            return resultMap;
        }

        // 加入班级 由于存在账号再使用的情况，所以这里还要判断一下学生是否已经在老师的组里面了
        MapMessage message = studentSystemClazzServiceClient.studentJoinClazz(student.getId(), teacherId, clazzId, forceLink, OperationSourceType.app);
        if (!message.isSuccess()) {
            resultMap.setSuccess(false);
            resultMap.add(RES_MESSAGE, getJoinClazzErrorMsg(message));
            return resultMap;
        }

        Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId);

        // 移动端需要返回学生是中学还是小学
        resultMap.add(RES_KTWELVE, clazz.getEduSystem().getKtwelve());
        resultMap.setSuccess(true);
        return resultMap;
    }

    private String getJoinClazzErrorMsg(Map message) {
        String errorType = SafeConverter.toString(message.get("type"));
        if (errorType != null) {
            switch (errorType) {
                case "NO_STUDENT_FOUND":
                    return RES_RESULT_NO_STUDENT_FOUND_MSG;
                case "ABOVE_QUOTA":
                    return RES_RESULT_ABOVE_CLAZZ_QUOTA_MSG;
                case "ALREADY_IN_CLASS":
                    return RES_RESULT_ALREADY_IN_CLASS_MSG;
                case "CHEATING_TEACHER":
                    return RES_RESULT_TEACHER_ACCOUNT_UNUSUAL_MSG;
                case "DIFFERENT_CLAZZ":
                    return RES_DIFFERENT_CLAZZ_MSG;
                case "NO_SUCH_CLASS":
                    return RES_RESULT_NO_CLASS_FOUND_MSG;
                case "NO_TEACHER_FOUND":
                    return RES_RESULT_NO_TEACHER_FOUND_MSG;
                case "CLASS_FREE_JOIN_CLOSED":
                    return RES_RESULT_JOIN_CLAZZ_ERROR_MSG;
                case "MULTI_TEACHER_ONE_SUBJECT":
                    return RES_ALREADY_IN_CLAZZ_MSG;
            }
        }

        logger.warn("student join clazz failed, message is {}", JsonUtils.toJson(message));
        return RES_RESULT_JOIN_CLAZZ_ERROR_MSG;
    }

    @RequestMapping(value = "/getstudentteacher.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getStudentTeacher() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Long studentId = getCurrentStudent().getId();
        List<Map<String, Object>> teacherList = new ArrayList<>();

        //TODO 需要抽象方法
        Set<Long> groupIds = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByStudentId(studentId)
                .stream()
                .map(GroupStudentTuple::getGroupId)
                .collect(Collectors.toSet());
        Set<Long> clazzIds = groupLoaderClient.getGroupLoader()
                .loadGroups(groupIds)
                .getUninterruptibly()
                .values()
                .stream()
                .map(Group::getClazzId)
                .collect(Collectors.toSet());
        List<ClazzTeacher> clazzTeachers = userAggregationLoaderClient.loadStudentTeachersBySystemClazzIds(clazzIds, studentId).values()
                .stream().flatMap(Collection::stream).collect(Collectors.toList());
        for (ClazzTeacher clazzTeacher : clazzTeachers) {
            Map<String, Object> teacherMap = new HashMap<>();
            teacherMap.put(RES_TEACHER_NAME, clazzTeacher.getTeacher().fetchRealname());
            teacherMap.put(RES_TEACHER_ID, clazzTeacher.getTeacher().getId());
            teacherMap.put(RES_SUBJECT, clazzTeacher.getTeacher().getSubject() == null ? "" : clazzTeacher.getTeacher().getSubject().getValue());
            teacherList.add(teacherMap);
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_TEACHER_LIST, teacherList);
        return resultMap;
    }

    @RequestMapping(value = "/getstudentclassmates.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getStudentClassmates() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        StudentDetail studentDetail = getCurrentStudentDetail();
        if (studentDetail.getClazz() == null) {
            resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
            resultMap.add(RES_MESSAGE, RES_STUDENT_NO_CLAZZ_MSG);
            return resultMap;
        }

        List<Map<String, Object>> classmateList = new ArrayList<>();
        // TODO 底层重构
        List<User> classmates = userAggregationLoaderClient.loadLinkedClassmatesForSystemClazz(studentDetail.getId());
        //先把自己放在列表第一个
        Map<String, Object> currentStudent = new HashMap<>();
        currentStudent.put(RES_USER_ID, studentDetail.getId());
        currentStudent.put(RES_REAL_NAME, studentDetail.fetchRealname());
        classmateList.add(currentStudent);

        classmates.forEach(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put(RES_USER_ID, e.getId());
            map.put(RES_REAL_NAME, getStudentName(e));
            classmateList.add(map);
        });
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_USER_LIST, classmateList);
        return resultMap;
    }

    /**
     * 减少客户端请求次数，搞个聚合接口
     */
    @RequestMapping(value = "/getstudentteachersandclassmates.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getStudentTeachersAndClassmates() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        StudentDetail studentDetail = getCurrentStudentDetail();
        Long studentId = studentDetail.getId();

        // 取老师列表
        List<Map<String, Object>> teacherList = new ArrayList<>();
        Set<Long> groupIds = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByStudentId(studentId)
                .stream()
                .map(GroupStudentTuple::getGroupId)
                .collect(Collectors.toSet());
        Set<Long> clazzIds = groupLoaderClient.getGroupLoader()
                .loadGroups(groupIds)
                .getUninterruptibly()
                .values()
                .stream()
                .map(Group::getClazzId)
                .collect(Collectors.toSet());
        List<ClazzTeacher> clazzTeachers = userAggregationLoaderClient.loadStudentTeachersBySystemClazzIds(clazzIds, studentId).values()
                .stream().flatMap(Collection::stream).collect(Collectors.toList());
        for (ClazzTeacher clazzTeacher : clazzTeachers) {
            Map<String, Object> teacherMap = new HashMap<>();
            teacherMap.put(RES_TEACHER_NAME, clazzTeacher.getTeacher().fetchRealname());
            teacherMap.put(RES_TEACHER_ID, clazzTeacher.getTeacher().getId());
            teacherMap.put(RES_SUBJECT, clazzTeacher.getTeacher().getSubject() == null ? "" : clazzTeacher.getTeacher().getSubject().getValue());
            teacherMap.put(RES_AVATAR_URL, getUserAvatarImgUrl(clazzTeacher.getTeacher().fetchImageUrl()));
            teacherList.add(teacherMap);
        }
        resultMap.add(RES_TEACHER_LIST, teacherList);

        // 取同班同学列表
        List<Map<String, Object>> classmateList = new ArrayList<>();
        List<User> classmates = userAggregationLoaderClient.loadLinkedClassmatesForSystemClazz(studentDetail.getId());
        // 同班同学的总数
        //列表加了自己。这个数量要+1
        resultMap.add(RES_TOTAL_COUNT, classmates.size() + 1);

        // 只取前三个同学
        if (classmates.size() > 3) {
            classmates = classmates.subList(0, 3);
        }
        classmates.forEach(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put(RES_USER_ID, e.getId());
            map.put(RES_REAL_NAME, getStudentName(e));
            classmateList.add(map);
        });
        resultMap.add(RES_USER_LIST, classmateList);

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }


    @RequestMapping(value = "/checkduplicatename.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage checkName() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequestNoSessionKey(REQ_CLAZZ_ID, REQ_TEACHER_ID, REQ_REAL_NAME);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
        String realName = getRequestString(REQ_REAL_NAME);

        return checkDuplicateName(realName, clazzId);
    }

    @RequestMapping(value = "/validatejoinclazz.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage validate() {
        MapMessage resultMap = new MapMessage();
        // 这个方法在登录/未登录状态下都可能调用，所以不验证sessionkey
        try {
            validateRequestNoSessionKey(REQ_CLAZZ_ID, REQ_TEACHER_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
        Long teacherId = getRequestLong(REQ_TEACHER_ID);

        MapMessage validateMsg = clazzServiceClient.validateJoinSystemClazz(teacherId, clazzId);
        if (validateMsg.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, getJoinClazzErrorMsg(validateMsg));
        }
        return resultMap;
    }

    /**
     * 班级红点提醒
     */
    @RequestMapping(value = "/remind.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage remind() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        //since v2.7.0 班级空间红点提醒数量
        //未读提醒
        List<AppMessage.Location> locationList = messageLoaderClient.getMessageLoader().loadAppMessageLocations(currentUserId());
        long count = locationList.stream()
                .filter(m -> LIKE_REMIND.getType() == SafeConverter.toInt(m.getMessageType())
                        || ACHIEVEMENT_HEAD_LINE_REMIND.getType() == SafeConverter.toInt(m.getMessageType())
                        || ACHIEVEMENT_WALL_HEAD_LINE_REMIND.getType() == SafeConverter.toInt(m.getMessageType())
                        || BIRTHDAY_BLESS_HEAD_LINE_REMIND.getType() == SafeConverter.toInt(m.getMessageType())
                        || ACHIEVEMENT_ENCOURAGE_HEAD_LINE_REMIND.getType() == SafeConverter.toInt(m.getMessageType()))
                .filter(m -> null == m.getViewed() || !m.getViewed()).count();
        if (count > 0) {
            Map<String, Object> unreadMap = new HashMap<>();
            unreadMap.put(RES_CLAZZ_UNREAD_REMIND_TYPE, 1); //0红点　1红点+数字
            unreadMap.put(RES_CLAZZ_UNREAD_REMIND_COUNT, count);
            resultMap.put(RES_CLAZZ_UNREAD_REMIND, unreadMap);
        }

        resultMap.put(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * 学生注册, 判断姓名填涂号和group的匹配
     */
    @RequestMapping(value = "/checkklxstudentregisterinfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage checkKlxStudentRegisterInfo() {
        MapMessage resultMap = new MapMessage();
        //未登录状态下都可能调用，所以不验证sessionkey
        try {
            validateRequired(REQ_REAL_NAME, "姓名");
            validateRequired(REQ_CLAZZ_ID, "班级");
            validateDigitNumber(REQ_CLAZZ_ID, "班级");
            validateRequired(REQ_TEACHER_ID, "老师");
            validateDigitNumber(REQ_TEACHER_ID, "老师");
            validateRequestNoSessionKey(REQ_REAL_NAME, REQ_SCAN_NUMBER, REQ_CLAZZ_ID, REQ_TEACHER_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        //业务处理
        String name = getRequestString(REQ_REAL_NAME);
        String scanNumber = getRequestString(REQ_SCAN_NUMBER);
        Long teacherId = getRequestLong(REQ_TEACHER_ID);
        Long clazzId = getRequestLong(REQ_CLAZZ_ID);

        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null || teacher.isDisabledTrue()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_TEACHER_NUMBER_ERROR_MSG);
            return resultMap;
        }
        // 校验填涂号
        boolean noNeedCheck = false;
        try {
            String appKey = getRequestString(REQ_APP_KEY);
            if (StringUtils.equals("17Student", appKey)) {
                // 此时是填涂号是选填的
                if (teacher.isJuniorEnglishOrChineseTeacher() && VersionUtil.compareVersion(getRequestString(REQ_APP_NATIVE_VERSION), "2.8.4.0") > 0) {
                    if (StringUtils.isNotBlank(scanNumber)) {
                        validateDigitNumber(REQ_SCAN_NUMBER, "阅卷机填涂号");
                    } else {
                        noNeedCheck = true;
                    }
                } else {
                    validateRequired(REQ_SCAN_NUMBER, "阅卷机填涂号");
                    validateDigitNumber(REQ_SCAN_NUMBER, "阅卷机填涂号");
                }
            } else if (StringUtils.equals("17JuniorStu", appKey)) {
                if (teacher.isKLXTeacher()) {
                    validateRequired(REQ_SCAN_NUMBER, "阅卷机填涂号");
                    validateDigitNumber(REQ_SCAN_NUMBER, "阅卷机填涂号");
                } else {
                    if (StringUtils.isNotBlank(scanNumber)) {
                        validateDigitNumber(REQ_SCAN_NUMBER, "阅卷机填涂号");
                    } else {
                        noNeedCheck = true;
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        //检查重名
        resultMap = checkDuplicateName(name, clazzId);
        if (!Objects.equals(resultMap.get(RES_USER_ID), 0L)) {
            return resultMap;
        }

        // 此时不用校验名字是否匹配
        if (noNeedCheck) {
            resultMap = new MapMessage();
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        return checkKlxStudentInfo(name, scanNumber, teacherId, clazzId, false);
    }

    /**
     * 学生换班, 判断姓名填涂号和group的匹配
     */
    @RequestMapping(value = "/checkklxstudentscannumber.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage checkKlxStudentScanNumber() {
        MapMessage resultMap = new MapMessage();

        //登录状态下验证sessionkey
        try {
            validateRequired(REQ_CLAZZ_ID, "班级");
            validateRequired(REQ_TEACHER_ID, "老师");
            validateDigitNumber(REQ_CLAZZ_ID, "班级");
            validateDigitNumber(REQ_TEACHER_ID, "老师");
            validateRequest(REQ_SCAN_NUMBER, REQ_CLAZZ_ID, REQ_TEACHER_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        //业务处理
        Student student = getCurrentStudent();
        String name = "";
        if (student.getProfile() != null) {
            name = student.getProfile().getRealname();
        }

        String scanNumber = getRequestString(REQ_SCAN_NUMBER); // 填涂号是选填的，当版本
        Long teacherId = getRequestLong(REQ_TEACHER_ID);
        Long clazzId = getRequestLong(REQ_CLAZZ_ID);

        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null || teacher.isDisabledTrue()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_TEACHER_NUMBER_ERROR_MSG);
            return resultMap;
        }

        // 校验填涂号
        boolean noNeedCheck = false;
        try {
            // 此时是填涂号是选填的
            if (teacher.isJuniorEnglishOrChineseTeacher() && VersionUtil.compareVersion(getRequestString(REQ_APP_NATIVE_VERSION), "2.8.4.0") > 0) {
                if (StringUtils.isNotBlank(scanNumber)) {
                    validateDigitNumber(REQ_SCAN_NUMBER, "阅卷机填涂号");
                } else {
                    noNeedCheck = true;
                }
            } else {
                validateRequired(REQ_SCAN_NUMBER, "阅卷机填涂号");
                validateDigitNumber(REQ_SCAN_NUMBER, "阅卷机填涂号");
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // 此时不用校验名字是否匹配
        if (noNeedCheck) {
            resultMap = new MapMessage();
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        return checkKlxStudentInfo(name, scanNumber, teacherId, clazzId, true);
    }

    /**
     * Feature #46231 老师包班&学生注册打包优化 Update:2017-06-01
     * 在这里加入判断
     */
    private MapMessage checkKlxStudentInfo(String name, String scanNumber, Long teacherId, Long clazzId, boolean isLogin) {
        MapMessage resultMap = new MapMessage();

        // Feature #46231 老师包班&学生注册打包优化 Update:2017-06-01
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null || teacher.isDisabledTrue()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_TEACHER_NUMBER_ERROR_MSG);
            return resultMap;
        }

        MapMessage mapMessage = newKuailexueLoaderClient.checkNameAndScanNumber(name, scanNumber, teacherId, clazzId);
        if (!mapMessage.isSuccess()) {
            String errorFlag = (String) mapMessage.get("errorflag");
            if (StringUtils.equals(errorFlag, "nameNotExist")) {
                if (isLogin) {//快乐学学生换班时的提示
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_INPUT_NAME_NOTIN_CLAZZ);
                    return resultMap;
                } else {//注册时的提示
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_NAME_NOTIN_CLAZZ);
                    return resultMap;
                }
            } else if (StringUtils.equals(errorFlag, "scanNumberNotExist")) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_SCANNUMBER_NOTIN_CLAZZ);
                return resultMap;
            } else if (StringUtils.equals(errorFlag, "nameScanNumberNotMatch")) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_NAME_SCANNUMBER_NOT_MATCH);
                return resultMap;
            } else if (StringUtils.equals(errorFlag, "scanNumberUsed")) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_SCANNUMBER_USED);
                return resultMap;
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_SCANNUMBER_CHECK);
                return resultMap;
            }
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    //注册时校验 学生姓名是否重复，如果重复则返回已有学生的id
    private MapMessage checkDuplicateName(String realName, Long clazzId) {
        MapMessage resultMap = new MapMessage();

        if (badWordCheckerClient.containsUserNameBadWord(realName)) {
            return failMessage("输入的姓名信息不合适哦\n有疑问请联系客服：\n400-160-1717");
        }

        Long duplicateUserId = 0L;
        boolean hasLogin = false;

        List<Long> studentIds = asyncGroupServiceClient.getAsyncGroupService()
                .findStudentIdsByClazzId(clazzId);
        if (CollectionUtils.isNotEmpty(studentIds)) {
            List<User> sameNameList = userLoaderClient.loadUsers(studentIds).values().stream()
                    .filter(s -> StringUtils.equals(s.fetchRealname(), realName.trim()))
                    .sorted((o1, o2) -> Long.compare(o2.getCreateTime().getTime(), o1.getCreateTime().getTime()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sameNameList)) {
                User user = userLoginServiceClient.getUserLoginService().findSameNameUsedUser(sameNameList).getUninterruptibly();
                if (user != null) {
                    duplicateUserId = user.getId();
                    hasLogin = true;
                }
            }
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_DUPLICATE_REALNAME, duplicateUserId != 0);
        resultMap.add(RES_HAS_LOGIN, hasLogin);
        resultMap.add(RES_USER_ID, duplicateUserId);
        return resultMap;
    }

}
