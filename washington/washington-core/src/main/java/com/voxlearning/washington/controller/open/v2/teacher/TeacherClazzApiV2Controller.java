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

package com.voxlearning.washington.controller.open.v2.teacher;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.ClazzType;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.core.helper.ClassJieHelper;
import com.voxlearning.utopia.data.NeonatalClazz;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzIntegralPool;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorSchoolRef;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.TeacherSystemClazzService;
import com.voxlearning.utopia.service.user.api.constants.ClazzCreateSourceType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.ClassMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.ClazzServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.SUCCESS;
import static com.voxlearning.utopia.api.constant.PopupCategory.LOWER_RIGHT;
import static com.voxlearning.utopia.api.constant.Subjects.ALL_SUBJECTS;
import static com.voxlearning.utopia.api.constant.Subjects.BASIC_SUBJECTS;
import static com.voxlearning.utopia.service.user.api.constants.GroupType.WALKING_GROUP;
import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.*;

/**
 * @author fugui.chang
 * @since 2016/11/15
 */
@Controller
@RequestMapping(value = "/v2/teacher")
@Slf4j
public class TeacherClazzApiV2Controller extends AbstractTeacherApiController {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private ClazzIntegralServiceClient clazzIntegralServiceClient;
    @Inject private FlowerServiceClient flowerServiceClient;
    @Inject private ClazzServiceClient clazzServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "findclazzinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage findClazzInfo() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_JSON, "参数");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateEnum(REQ_SUBJECT, "学科", Subject.CHINESE.name(), Subject.MATH.name(), Subject.ENGLISH.name());
                validateRequest(REQ_SUBJECT, REQ_JSON);
            } else
                validateRequest(REQ_JSON);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Map<String, Object> clazzMap = parseJSON2Map(getRequestString(REQ_JSON));

        //支持包班制
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SUBJECT_ERROR_MSG);
            return resultMap;
        }

        Set<Long> clazzIdSet = new LinkedHashSet<>();
        clazzMap.forEach((k, v) -> {
            List<Object> levelClazzs = (List<Object>) v;
            levelClazzs.forEach(lc -> {
                Map<String, Object> m = (Map<String, Object>) lc;
                List<TeacherClazzApiV2Controller.SystemClazzInfo> clazzs = JsonUtils.fromJsonToList(JsonUtils.toJson(m.get("clazzs")), TeacherClazzApiV2Controller.SystemClazzInfo.class);
                clazzs.forEach(c -> clazzIdSet.add(c.getId()));
            });
        });

        //与 /v1/teacher/findclazzinfo.vpage 的差异是forceAppUsed为true
        MapMessage message = teacherSystemClazzInfoServiceClient.getNewAddAndAdjustClazzs(teacher.getId(), clazzIdSet, ClazzCreateSourceType.app, true);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add("newClazzs", message.get("newClazzs"));
        resultMap.add("adjustClazzs", message.get("adjustClazzs"));
        return resultMap;
    }

    @NoArgsConstructor
    @RequiredArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class SystemClazzInfo {
        @Getter
        @Setter
        @NonNull
        Long id;
        @Getter
        @Setter
        @NonNull
        String name;
        @Getter
        @Setter
        Boolean checked;
    }

    private Map<String, Object> parseJSON2Map(String jsonStr) {
        Map<String, Object> map = new HashMap<>();
        //最外层解析
        com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(jsonStr);
        for (Object k : json.keySet()) {
            Object v = json.get(k);
            //如果内层还是数组的话，继续解析
            if (v instanceof JSONArray) {
                List<Map<String, Object>> list = new ArrayList<>();
                ((JSONArray) v).forEach(p -> {
                    list.add(parseJSON2Map(p.toString()));
                });
                map.put(k.toString(), list);
            } else {
                map.put(k.toString(), v);
            }
        }
        return map;
    }

    /**
     * 老师的班级详情空间
     *
     * @return
     */
    @RequestMapping(value = "/clazz/detail.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage teacherClazzDetail() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_GROUP_ID, "分组id");
            validateRequest(REQ_GROUP_ID);
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Long groupId = getRequestLong(REQ_GROUP_ID);

        Teacher curTeacher = getCurrentTeacher();

        //开始验证老师分组是否正确
        Boolean groupRight = teacherLoaderClient.hasRelTeacherTeachingGroup(curTeacher.getId(), groupId);
        if (!groupRight) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_TEACHER_GROUP_ERROR);
            return resultMap;
        }

        GroupMapper groupMapper = deprecatedGroupLoaderClient.loadGroup(groupId, true);

        if (groupMapper == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_TEACHER_GROUP_ERROR);
            return resultMap;
        }

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(groupMapper.getClazzId());
        if (clazz == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_TEACHER_GROUP_ERROR);
            return resultMap;
        }

        resultMap.add(RES_GROUP_ID, groupMapper.getId());
        resultMap.add(RES_CLAZZ_ID, clazz.getId());
        resultMap.add(RES_CLAZZ_NAME, clazz.formalizeClazzName());
        resultMap.add(RES_CLAZZ_LEVEL, clazz.getClazzLevel().getLevel());
        resultMap.add(RES_CLAZZ_TYPE, clazz.getClazzType().getType());
        resultMap.add(RES_CLAZZ_SIZE, groupMapper.getStudents().size());
        resultMap.add(RES_GROUP_FREE_JOIN, groupMapper.getFreeJoin());
        resultMap.add(RES_CLAZZ_SHOW_RANK, clazz.needShowRank());

        //该班级已添加的老师信息
        List<TeacherSystemClazzService.CanAddSubjectStatus> canAddSubjects = teacherSystemClazzServiceClient.findCanAddSubject(curTeacher.getId(), clazz.getId());
        List<Map<String, Object>> teacherMapList = new ArrayList<>();
        canAddSubjects.forEach(e -> {
            Map<String, Object> map = new LinkedHashMap<>();

            map.put(RES_COULD_ADD_TEACHER, e.isCanAdd());
            if (e.getStatus() == 0) {
                map.put(RES_COULD_NOT_ADD_TEXT, "已添加");
            } else if (e.getStatus() == 2) {
                map.put(RES_COULD_NOT_ADD_TEXT, "等待对方同意");
            }
            map.put(RES_TEACHER_ID, e.getTeacherId() == null ? 0L : e.getTeacherId());
            map.put(RES_TEACHER_NAME, e.getTeacherName() != null ? e.getTeacherName() : "");
            map.put(RES_SUBJECT, e.getSubject().name());
            map.put(RES_SUBJECT_KEY, e.getSubject().getKey());
            map.put(RES_SUBJECT_NAME, e.getSubject().getValue());
            map.put(RES_AUTH_STATE, e.getAuthState() == SUCCESS.getState());
            Teacher tempTeacher = teacherLoaderClient.loadTeacher(e.getTeacherId());
            if (tempTeacher != null) {
                map.put(RES_TEACHER_AVATAR, getUserAvatarImgUrl(tempTeacher));
            } else {
                map.put(RES_TEACHER_AVATAR, "");
            }
            teacherMapList.add(map);
        });
        List<Map<String, Object>> finalTeacherMapList = teacherMapList.stream()
                .sorted(((o1, o2) -> Integer.compare(SafeConverter.toInt(o1.get(RES_SUBJECT_KEY)), SafeConverter.toInt(o2.get(RES_SUBJECT_KEY)))))
                .collect(Collectors.toList());

        resultMap.add(RES_TEACHER_LIST, finalTeacherMapList);

        List<Map<String, Object>> studentList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(groupMapper.getStudents())) {
            groupMapper.getStudents().forEach(t -> {
                Map<String, Object> studentMap = new LinkedHashMap<>();
                studentMap.put(RES_STUDENT_ID, t.getId());
                studentMap.put(RES_STUDENT_NAME, t.getName());
                studentList.add(studentMap);
            });
        }

        // 加一个汉字排序
        Collator compator = Collator.getInstance(java.util.Locale.CHINA);
        Collections.sort(studentList, (o1, o2) -> (compator.compare(SafeConverter.toString(o1.get(RES_STUDENT_NAME)), SafeConverter.toString(o2.get(RES_STUDENT_NAME)))));

        MonthRange range = MonthRange.current();
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_STUDENT_LIST, studentList);
        if (curTeacher.isPrimarySchool() || curTeacher.isInfantTeacher()) {
            Boolean multipleSubject = CollectionUtils.isNotEmpty(curTeacher.getSubjects()) && curTeacher.getSubjects().size() > 1;
            if (multipleSubject) {// 包班制老师 返回学科,返回每个学科的学豆数量 鲜花数量
                List<GroupMapper> teacherAllGroupInClazz = teacherLoaderClient.findTeacherAllGroupInClazz(clazz.getId(), curTeacher.getId());
                resultMap.add(RES_SUBJECT_LIST, toSubjectList(
                        teacherAllGroupInClazz.stream().map(GroupMapper::getSubject).collect(Collectors.toList()), true));
                Set<Long> groupIdSet = teacherAllGroupInClazz.stream().map(GroupMapper::getId).collect(Collectors.toSet());
                Map<Long, GroupMapper> groupMapperMap = teacherAllGroupInClazz.stream().distinct().collect(Collectors.toMap(GroupMapper::getId, Function.identity()));
                Map<Long, SmartClazzIntegralPool> integralPoolMap = clazzIntegralServiceClient.getClazzIntegralService()
                        .loadClazzIntegralPools(groupIdSet)
                        .getUninterruptibly();

                Map<Long, Teacher> groupSingleTeacherMap = teacherLoaderClient.loadGroupSingleTeacher(groupIdSet);

                Map<Long, List<Flower>> teacherFlowerListMap = flowerServiceClient.loadCurrentMonthTeachersFlowersInClazz(clazz.getId(),
                        groupSingleTeacherMap.values().stream().map(Teacher::getId).collect(Collectors.toSet()));

                List<Map<String, Object>> subjectIntegralFlowerMapList = new ArrayList<>();
                groupIdSet.forEach(gid -> {
                    GroupMapper groupMapper1 = groupMapperMap.get(gid);
                    if (groupMapper1 == null)
                        return;
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put(RES_SUBJECT_NAME, groupMapper1.getSubject().getValue());

                    SmartClazzIntegralPool smartClazzIntegralPool = integralPoolMap.get(gid);
                    map.put(RES_INTEGRAL_COUNT, smartClazzIntegralPool == null ? 0 : smartClazzIntegralPool.fetchTotalIntegral());

                    Teacher groupTeacher = groupSingleTeacherMap.get(gid);
                    if (groupTeacher != null) {
                        List<Flower> flowers = teacherFlowerListMap.get(groupTeacher.getId());
                        map.put(RES_FLOWER_COUNT, flowers == null ? 0 : flowers == null ? 0 : flowers.stream().filter(f -> f.getReceiverId() != null && Objects.equals(f.getReceiverId(), groupTeacher.getId()))
                                .filter(f -> range.contains(f.getCreateDatetime())).count());
                    }
//                    map.put(RES_FLOWER_EXCHANGE_URL, "http://www.test.17zuoye.net");
                    subjectIntegralFlowerMapList.add(map);
                });
                resultMap.add(RES_F_I_LIST, subjectIntegralFlowerMapList);

            } else { //非包班制单独处理
                SmartClazzIntegralPool smartClazzIntegralPool = clazzIntegralServiceClient.getClazzIntegralService()
                        .loadClazzIntegralPool(groupId)
                        .getUninterruptibly();
                resultMap.add(RES_INTEGRAL_COUNT, smartClazzIntegralPool == null ? 0 : smartClazzIntegralPool.fetchTotalIntegral());
                List<Flower> flowers = flowerServiceClient.loadCurrentMonthTeacherFlowersInClazz(clazz.getId(), curTeacher.getId());
                resultMap.add(RES_FLOWER_COUNT, flowers == null ? 0 : flowers.size());
//                resultMap.add(RES_FLOWER_EXCHANGE_URL, "http://www.test.17zuoye.net");
            }
        }

        return resultMap;
    }


    /**
     * 新建班级
     * <p>
     * 去重逻辑针对极算子, 其他平台慎调
     *
     * @return
     */
    @RequestMapping(value = "createClazz.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage createClazz() {
        MapMessage resultMap = new MapMessage();
        try {
            validateEnum(RES_CLAZZ_TYPE, "班级类型", ClazzType.PUBLIC.name(), ClazzType.WALKING.name());
            validateRequiredNumber(RES_SCHOOL_ID, "学校ID");
            validateRequiredNumber(RES_CLAZZ_LEVEL, "年级");
            validateNotNull(RES_CLAZZ_NAME, "班级名");
            validateEnum(RES_CLAZZ_EDUSYSTEM, "年制", EduSystemType.I4.name(), EduSystemType.P6.name(), EduSystemType.P5.name(),
                    EduSystemType.J4.name(), EduSystemType.J3.name(), EduSystemType.S4.name(), EduSystemType.S3.name());
            validateRequest(RES_CLAZZ_TYPE, RES_SCHOOL_ID, RES_CLAZZ_LEVEL, RES_CLAZZ_NAME, RES_CLAZZ_EDUSYSTEM);
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_NO_TEACHER_FOUND_MSG);
            return resultMap;
        }
        Long schoolId = getRequestLong(RES_SCHOOL_ID);
        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SCHOOL_INFO_ERROR_MSG);
            return resultMap;
        }

        // 老师学校校验
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
        if (!Objects.equals(teacherDetail.getTeacherSchoolId(), schoolId)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SCHOOL_TEACHER);
            return resultMap;
        }

        String clazzLevel = getRequestString(RES_CLAZZ_LEVEL);
        String clazzName = getRequestString(RES_CLAZZ_NAME);

        String clazzFullName = teacherDetail.getSubject().getValue() + clazzName + "班";

        // 同级的班级ID
        Collection<Long> sameLevelClazzId = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolId)
                .originalLocationsAsList()
                .stream()
                .filter(t -> (t.getJie() == ClassJieHelper.fromClazzLevel(ClazzLevel.parse(Integer.valueOf(clazzLevel)))) && !t.isDisabled())
                .map(Clazz.Location::getId)
                .collect(Collectors.toSet());

        //同级的班级
        Map<Long, Clazz> sameLevelClazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(sameLevelClazzId)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));

        //同级班级中同名的班级ID
        List<Long> sameNameClazzId = sameLevelClazz.values().stream().filter(i -> Objects.equals(i.getClassName(), clazzFullName))
                .map(LongIdEntity::getId).collect(Collectors.toList());

        Map<Long, List<GroupMapper>> sameClazzGroup = deprecatedGroupLoaderClient.loadClazzGroups(sameNameClazzId);
        List<GroupMapper> sameClazzAll = sameClazzGroup.values().stream().flatMap(Collection::stream).filter(groupMapper -> groupMapper.getGroupType() == WALKING_GROUP).collect(Collectors.toList());

        for (GroupMapper groupMapper : sameClazzAll) {
            if (groupMapper.getSubject() == teacherDetail.getSubject()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_CLAZZ_SCHOOL_DUPLICATE_NAME);
                return resultMap;
            }
        }

        ClassMapper mapper = new ClassMapper();
        mapper.setSchoolId(schoolId);
        mapper.setClassLevel(clazzLevel);
        mapper.setClazzName(clazzFullName);
        mapper.setEduSystem(getRequestString(RES_CLAZZ_EDUSYSTEM));
//        ClazzType clazzType = ClazzType.valueOf(getRequestString(RES_CLAZZ_TYPE));
        ClazzType clazzType = ClazzType.WALKING; // 极算来源, 强制走教学班逻辑
        MapMessage resultClasses;
        // 行政班
        if (ClazzType.PUBLIC == clazzType) {
            resultClasses = clazzServiceClient.createSystemClazz(Collections.singletonList(mapper));
        } else {
            resultClasses = clazzServiceClient.createWalkingClazz(teacher.getId(), Collections.singletonList(mapper));
        }

        if (resultClasses.isSuccess()) {
            List<NeonatalClazz> neonatalClazzs = JsonUtils.fromJsonToList(JsonUtils.toJson(resultClasses.get("neonatals")), NeonatalClazz.class);
            if (neonatalClazzs == null || neonatalClazzs.isEmpty()) {
                resultMap.add(RES_RESULT, RES_RESULT_ERROR);
                return resultMap;
            }
            NeonatalClazz neonatalClazz = neonatalClazzs.get(0);
            Long clazzId = neonatalClazz.getClazzId();

            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            if (clazz == null) {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
                return resultMap;
            }

            if (ClazzType.PUBLIC == clazzType) {
                // 老师加入班级
                clazzServiceClient.teacherJoinSystemClazz(teacher.getId(), clazzId, OperationSourceType.app);
            }

            GroupMapper groupMapper = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacher.getId(), clazzId, true);
            if (groupMapper == null) {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
                return resultMap;
            }

            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_GROUP_ID, groupMapper.getId());
            resultMap.add(RES_CLAZZ_ID, clazzId);
            resultMap.add(RES_SUBJECT, groupMapper.getSubject().name());
            resultMap.add(RES_CLAZZ_LEVEL, clazzLevel);
            resultMap.add(RES_CLAZZ_NAME, clazz.formalizeClazzName());

            return resultMap;
        }
        resultMap.add(RES_RESULT, RES_RESULT_ERROR);
        resultMap.add(RES_MESSAGE, resultClasses.getInfo());
        return resultMap;
    }

    /**
     * 老师退出班级
     *
     * @return MapMessage
     */
    @RequestMapping(value = "exitclazz.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage exitClazz() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_CLAZZ_ID, "班级ID");
            validateRequest(REQ_CLAZZ_ID);
        } catch (IllegalVendorUserException e) {
            resultMap.add(RES_RESULT, e.getCode());
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher currentTeacher = getCurrentTeacher();
        if (currentTeacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_NO_TEACHER_FOUND_MSG);
            return resultMap;
        }

        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
        MapMessage message = clazzServiceClient.teacherExitSystemClazz(currentTeacher.getId(), clazzId, true, OperationSourceType.app);

        if (message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_RESULT_HAS_CHANGED, true);
            resultMap.add(RES_CLAZZ_ID, clazzId);
            return resultMap;
        }
        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, RES_RESULT_EXIT_CLAZZ_ERROR);
        return resultMap;
    }

    /**
     * 不带班转校
     */
    @RequestMapping(value = "changeschoolwithoutclazz.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage changeSchoolWithoutClazz() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_SCHOOL_ID, "学校ID");
            validateRequiredNumber(REQ_ENFORCE, "是否强行执行");
            validateRequest(REQ_SCHOOL_ID, REQ_ENFORCE);
        } catch (IllegalVendorUserException e) {
            resultMap.add(RES_RESULT, e.getCode());
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher currentTeacher = getCurrentTeacher();
        if (currentTeacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_NO_TEACHER_FOUND_MSG);
            return resultMap;
        }
        Long teacherId = currentTeacher.getId();
        Long targetSchoolId = getRequestLong(REQ_SCHOOL_ID);
        int enforce = getRequestInt(REQ_ENFORCE, 0);

        // 判断目标学校是否存在
        School targetSchool = raikouSystem.loadSchool(targetSchoolId);
        if (targetSchool == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_TEACHER_SET_SCHOOL_FAILED);
            return resultMap;
        }

        // 检查老师名下是否还有班组
        List<GroupTeacherMapper> groupTeacherMappers = deprecatedGroupLoaderClient.loadTeacherGroups(teacherId, false);
        if (enforce != 1 && CollectionUtils.isNotEmpty(groupTeacherMappers)) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_RESULT_HAS_CHANGED, false);
            resultMap.add(RES_MESSAGE, "名下有班级，请先转让班级再转校");
            return resultMap;
        }

        // 中学学校存在开通学科的问题，需要检查老师所属学科，在目标学校中是否已经开通
        if (targetSchool.isMiddleSchool()) {
            SchoolExtInfo targetSchoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(targetSchoolId).getUninterruptibly();
            TeacherSubjectRef teacherSubjectRef = teacherLoaderClient.loadTeacherSubjectRef(teacherId);
            if (targetSchoolExtInfo == null) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, MessageFormat.format("需要转校的学校尚未开通{0}学科", teacherSubjectRef.getSubject().getValue()));
                return resultMap;
            }
            Set<Subject> targetSubjects = targetSchoolExtInfo.loadValidSubjects();
            if (teacherSubjectRef != null && !targetSubjects.contains(teacherSubjectRef.getSubject())) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, MessageFormat.format("需要转校的学校尚未开通{0}学科", teacherSubjectRef.getSubject().getValue()));
                return resultMap;
            }
        }

        // 自动检查作业
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        deprecatedGroupLoaderClient.loadTeacherGroups(teacherId, false).forEach(g -> {
            List<String> homeworkIds = newHomeworkLoaderClient.loadGroupHomeworks(g.getId(), g.getSubject()).originalLocationsAsList()
                    .stream()
                    .map(NewHomework.Location::getId)
                    .collect(Collectors.toList());
            String homeworkIdsStr = StringUtils.join(homeworkIds, ",");
            newHomeworkServiceClient.batchCheckHomework(teacher, homeworkIdsStr, HomeworkSourceType.App);
        });

        // 处理大使
        AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findAmbassadorSchoolRefs(teacherId)
                .stream()
                .findFirst()
                .orElse(null);
        if (ref != null) {
            // 如果是大使 直接辞任
            MapMessage message = businessTeacherServiceClient.resignationAmbassador(teacherLoaderClient.loadTeacherDetail(teacherId));
            if (!message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_TEACHER_SET_SCHOOL_FAILED);
                return resultMap;
            }
            // 给大使发右下角弹窗
            String content = "您已经转校，系统自动取消了您的大使身份。";
            userPopupServiceClient.createPopup(teacherId).content(content).type(PopupType.AMBASSADOR_NOTICE).category(LOWER_RIGHT).create();
        }

        // 比较学段，小、中、高不能互转
        School sourceSchool = asyncTeacherServiceClient.getAsyncTeacherService().loadTeacherSchool(teacherId).getUninterruptibly();
        if (sourceSchool != null) {
            if (Objects.equals(sourceSchool.getId(), targetSchoolId)) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, "修改前后的学校不能为同一学校");
                return resultMap;
            }
            if (sourceSchool.getLevel() == null || targetSchool.getLevel() == null || !Objects.equals(sourceSchool.getLevel(), targetSchool.getLevel())) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, "不允许跨学段转校");
                return resultMap;
            }
        }

        // 这回校验应该都完事儿了，可以转校
        MapMessage mapMessage = teacherSystemClazzServiceClient.changeTeacherSchoolNotCarryOldClazz(teacherId, targetSchoolId);
        if (!mapMessage.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_TEACHER_SET_SCHOOL_FAILED);
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_RESULT_HAS_CHANGED, true);
        resultMap.add(RES_SCHOOL_ID, targetSchool.getId());
        return resultMap;
    }

    /**
     * 老师修改学科
     * http://wiki.17zuoye.net/pages/viewpage.action?pageId=44845258
     * 搜转学科
     */
    @RequestMapping(value = "changesubject.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage changeSubject() {
        MapMessage resultMap = new MapMessage();
        try {
            validateNotNull(REQ_SUBJECT, "学科");
            validateRequest(REQ_SUBJECT);
        } catch (IllegalVendorUserException e) {
            resultMap.add(RES_RESULT, e.getCode());
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher currentTeacher = getCurrentTeacher();
        if (currentTeacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_NO_TEACHER_FOUND_MSG);
            return resultMap;
        }

        // 校验学科参数
        String subjectName = getRequestString(REQ_SUBJECT);
        Subject subject = Subject.ofWithUnknown(subjectName);
        if (subject == Subject.UNKNOWN) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOWN_SUBJECT_MSG);
            return resultMap;
        }

        // 校验包班制
        Set<Long> baos = teacherLoaderClient.loadRelTeacherIds(currentTeacher.getId());
        if (baos.size() > 1) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "包班制老师不允许修改学科");
            return resultMap;
        }

        // 校验学校
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(currentTeacher.getId())
                .getUninterruptibly();
        if (school == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "老师不在任何学校中");
            return resultMap;
        }

        // 校验转学科前后的学科，分中小学
        if (school.isPrimarySchool() && !BASIC_SUBJECTS.contains(subject)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, MessageFormat.format("抱歉，小学暂时不支持该学科: {0}", subject.getValue()));
            return resultMap;
        }

        if ((school.isMiddleSchool() || school.isSeniorSchool()) && !ALL_SUBJECTS.contains(subject)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, MessageFormat.format("抱歉，初高中暂时不支持该学科: {0}", subject.getValue()));
            return resultMap;
        }

        // 当前学校是否支持需要转的学科，仅对中学校验
        if (school.isMiddleSchool()) {
            SchoolExtInfo targetSchoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(school.getId()).getUninterruptibly();
            TeacherSubjectRef teacherSubjectRef = teacherLoaderClient.loadTeacherSubjectRef(currentTeacher.getId());
            if (targetSchoolExtInfo == null) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, MessageFormat.format("需要转校的学校尚未开通{0}学科", teacherSubjectRef.getSubject().getValue()));
                return resultMap;
            }
            Set<Subject> targetSubjects = targetSchoolExtInfo.loadValidSubjects();
            if (teacherSubjectRef != null && !targetSubjects.contains(teacherSubjectRef.getSubject())) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, MessageFormat.format("需要转校的学校尚未开通{0}学科", teacherSubjectRef.getSubject().getValue()));
                return resultMap;
            }
        }

        // 校验目标学科下，学生是否有目标学科的老师，by changyuan.liu
        // 1. 取teacher下所有group，
        Set<Long> teacherGroupIdSet = deprecatedGroupLoaderClient.loadTeacherGroups(currentTeacher.getId(), false)
                .stream()
                .map(GroupMapper::getId)
                .collect(Collectors.toSet());

        // 2. 取group下所有student，
        Set<Long> studentIds = studentLoaderClient.loadGroupStudentIds(teacherGroupIdSet)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        // 3. 取student的所有group,
        Set<Long> studentGroupIdSet = deprecatedGroupLoaderClient.loadStudentGroups(studentIds, false)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .map(GroupMapper::getId)
                .collect(Collectors.toSet());

        // 4. 从student中的group，去除teacher中的group
        Set<Long> groupIdSet = new HashSet<>(CollectionUtils.removeAll(studentGroupIdSet, teacherGroupIdSet));
        if (CollectionUtils.isEmpty(groupIdSet)) {
            groupIdSet = studentGroupIdSet;
        }

        // 5. 取group的teacher，
        Set<Teacher> teacherList = teacherLoaderClient.loadGroupTeacher(groupIdSet)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        // 6. 取teacher的subject是否包含目标学科
        teacherList.remove(currentTeacher);
        Set<Subject> subjects = teacherList
                .stream()
                .filter(Objects::nonNull)
                .map(Teacher::getSubjects)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        // 7.判断
        if (CollectionUtils.isNotEmpty(subjects) && subjects.contains(subject)) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_RESULT_HAS_CHANGED, false);
            resultMap.add(RES_MESSAGE, "您的学生中已有该学科的老师，无法更换为此学科");
            return resultMap;
        }

        // 终于都校验完了，转吧。
        MapMessage message = teacherSystemClazzServiceClient.changeTeacherSubject(currentTeacher.getId(), subject, SafeConverter.toString(currentTeacher.getId(), ""), OperationSourceType.app);
        // MapMessage message = teacherServiceClient.setTeacherSubject(currentTeacher.getId(), subject, currentTeacher.getKtwelve());
        if (!message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "修改学科失败，请联系客服");
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_RESULT_HAS_CHANGED, true);
        resultMap.add(RES_SUBJECT, subject);
        return resultMap;
    }

}
