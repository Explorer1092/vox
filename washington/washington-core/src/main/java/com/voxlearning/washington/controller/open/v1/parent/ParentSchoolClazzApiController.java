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

package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.SchoolType;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.ClazzConstants;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.RES_DIFFERENT_CLAZZ_MSG;

/**
 * @author shiwe.liao
 * @since 2016-7-21
 */
@Controller
@Slf4j
@RequestMapping(value = "/v1/parent/school_clazz/")
public class ParentSchoolClazzApiController extends AbstractParentApiController {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private GlobalTagServiceClient globalTagServiceClient;

    @RequestMapping(value = "get_teacher_clazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage getTeacherClazzList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_TEACHER_ID, "老师ID");
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_TEACHER_ID, REQ_STUDENT_ID);
        } catch (IllegalVendorUserException e) {
            return failMessage(e);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        String teacherToken = getRequestString(REQ_TEACHER_ID);
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        MapMessage message = studentSystemClazzServiceClient.joinClazz_findClazzInfo(teacherToken, studentId, Collections.singleton(Ktwelve.PRIMARY_SCHOOL));
        if (!message.isSuccess()) {
            return failMessage(message.getInfo());
        }

        Teacher teacher = (Teacher) message.get("teacher");
        if (teacher == null) {
            return failMessage(RES_TEACHER_NUMBER_ERROR_MSG);
        }

        if (teacherLoaderClient.isFakeTeacher(teacher.getId())) {
            return failMessage(RES_RESULT_TEACHER_STATUS_ERROR_MSG);
        }

        List<Map<String, Object>> clazzList = (List<Map<String, Object>>) message.get("clazzList");
        if (clazzList.isEmpty()) {
            return failMessage(RES_TEACHER_CLAZZ_ERROR_MSG);
        }
        List<Long> clazzIds = new ArrayList<>();
        for (Map<String, Object> clazzMap : clazzList) {
            clazzMap.remove("schoolName");
            clazzMap.remove("teachers");
            clazzMap.remove("creatorType");
            clazzMap.put(RES_CLAZZ_ID, clazzMap.get("clazzId"));
            clazzMap.put(RES_CLAZZ_NAME, clazzMap.get("clazzName"));
            clazzMap.put(RES_KTWELVE, clazzMap.get("ktwelve"));
            clazzMap.remove("clazzId");
            clazzMap.remove("clazzName");
            clazzIds.add(SafeConverter.toLong(clazzMap.get(RES_CLAZZ_ID)));
        }

        AlpsFuture<School> schoolFuture = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacher.getId());

        // 查找班级是否学生人员是否已满,把状态返回
        Map<Long, GroupMapper> groupMapperMap = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzIds(teacher.getId(), clazzIds, false);
        List<Long> groupIds = groupMapperMap.values().stream().map(GroupMapper::getId).collect(Collectors.toList());
        Map<Long, List<GroupStudentTuple>> refs = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByGroupIds(groupIds)
                .stream()
                .collect(Collectors.groupingBy(GroupStudentTuple::getGroupId));

        School school = schoolFuture.getUninterruptibly();

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
            if (Objects.equals(Boolean.TRUE, mapper.getFreeJoin()) && inQuota) {
                clazzMap.put(RES_CLAZZ_STATUS, true);
            } else {
                clazzMap.put(RES_CLAZZ_STATUS, false);
            }
        }

        resultMap.add(RES_CLAZZ_LIST, message.get("clazzList"));
        resultMap.add(RES_TEACHER_ID, teacher.getId());
        resultMap.add(RES_TEACHER_NAME, teacher.fetchRealname());
        resultMap.add(RES_SUBJECT, teacher.getSubject() == null ? "" : teacher.getSubject().getValue());
        resultMap.add(RES_KTWELVE, teacher.getKtwelve());
        resultMap.add(RES_SCHOOL_NAME, school == null ? "" : school.getCname());
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "joinsystemclazz.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage joinSystemClazz() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CLAZZ_ID, "班级ID");
            validateRequired(REQ_TEACHER_ID, "老师ID");
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateDigitNumber(REQ_CLAZZ_ID, "班级ID");
            validateDigitNumber(REQ_TEACHER_ID, "老师ID");
            validateDigitNumber(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_CLAZZ_ID, REQ_TEACHER_ID, REQ_STUDENT_ID);
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

        Long studentId = getRequestLong(REQ_STUDENT_ID);
        Student student = studentLoaderClient.loadStudent(studentId);

        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
        Long teacherId = getRequestLong(REQ_TEACHER_ID);
        return joinClazz(student, clazzId, teacherId, false);
    }

    /**
     * 通过区id获取学校
     *
     * @return MapMessage
     */
    @RequestMapping(value = "get_school_by_region.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getSchoolByRegionCode() {
        try {
            validateRequired(REQ_REGION_CODE, "区域code");
            validateRequest(REQ_REGION_CODE);
        } catch (IllegalVendorUserException ue) {
            return failMessage(ue);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        Integer regionCode = getRequestInt(REQ_REGION_CODE);

        List<School> schoolKtwelveList = loadAreaSchools(Collections.singleton(regionCode), SchoolLevel.safeParse(Ktwelve.PRIMARY_SCHOOL.getLevel()));
        List<Map<String, Object>> resSchoolList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(schoolKtwelveList)) {
            schoolKtwelveList.stream().filter(p -> StringUtils.isNotBlank(p.getShortName())).forEach(school -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put(RES_SCHOOL_ID, school.getId());
                map.put(RES_SCHOOL_NAME, school.getShortName());
                map.put(RES_SCHOOL_LEVEL, school.getLevel());
                resSchoolList.add(map);
            });
        }
        return successMessage().add(RES_SCHOOL_LIST, resSchoolList);
    }


    //=======================
    // private method
    //=======================
    @SuppressWarnings("unchecked")
    private MapMessage joinClazz(Student student, Long clazzId, Long teacherId, boolean forceLink) {
        // 2016.1.31 学生滥用换班功能刷学霸学豆的处理
        Boolean isValid = studentSystemClazzServiceClient.isValidClazzJoinRequest(teacherId, student.getId());
        if (!isValid) {
            return failMessage("你最近换班次数异常，已被系统禁止换班！");
        }

        // 加入班级
        MapMessage message = studentSystemClazzServiceClient.studentJoinClazz(student.getId(), teacherId, clazzId, forceLink, OperationSourceType.app);
        if (!message.isSuccess()) {
            return failMessage(getJoinClazzErrorMsg(message));
        }

        //给原老师发送app消息 退出班级
        //2016-08-19 有了message 服务,加入班级时,加入班级退出班级消息可以从user服务底层方法studentJoinClazz里面发,这里就不用再发了。
//        if (message.get("exitInfo") != null) {
//            List<Map<String, Object>> exitMapList = (List<Map<String, Object>>) message.get("exitInfo");
//            sendStudentExitOrJoinClazzTeacherAppMessage(exitMapList, "exit");
//        }


//        Clazz clazz = clazzLoaderClient.loadClazz(clazzId);
//        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
//
//        //给新班级老师发加入班级app消息
//        Map<String, Object> map = new HashMap<>();
//        map.put("teacherId", teacher.getId());
//        map.put("clazzName", clazz.formalizeClazzName());
//        String dateStr = DateUtils.dateToString(new Date(), "yyyy年MM月dd日");
//        map.put("dateStr", dateStr);
//        map.put("studentName", StringUtils.defaultString(student.fetchRealname()));
//        sendStudentExitOrJoinClazzTeacherAppMessage(Collections.singletonList(map), "join");
        return successMessage();
    }


    private String getJoinClazzErrorMsg(Map message) {
        Object errorType = message.get("type");
        if (errorType != null) {
            if (Objects.equals(errorType, "ABOVE_QUOTA")) {
                return RES_RESULT_ABOVE_CLAZZ_QUOTA_MSG;
            } else if (Objects.equals(errorType, "ALREADY_IN_CLASS")) {
                return RES_RESULT_ALREADY_IN_CLASS_MSG;
            } else if (Objects.equals(errorType, "CHEATING_TEACHER")) {
                return "此老师使用异常，你不能添加Ta为老师！";
            } else if (Objects.equals(errorType, "DIFFERENT_CLAZZ")) {
                return RES_DIFFERENT_CLAZZ_MSG;
            } else if (Objects.equals(errorType, "NO_SUCH_CLASS")) {
                return "找不到对应班级";
            }
        }
        return RES_RESULT_JOIN_CLAZZ_ERROR_MSG;
    }

    private List<School> loadAreaSchools(Collection<Integer> regionCodes, SchoolLevel schoolLevel) {
        return raikouSystem.querySchoolLocations(regionCodes)
                .enabled()
                .waitingSuccess()
                .level(schoolLevel)
                .filter(s -> s.getType() != SchoolType.TRAINING.getType() && s.getType() != SchoolType.CONFIDENTIAL.getType())
                .transform()
                .asList();
    }
}
