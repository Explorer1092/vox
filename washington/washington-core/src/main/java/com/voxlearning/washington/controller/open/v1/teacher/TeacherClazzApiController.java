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

package com.voxlearning.washington.controller.open.v1.teacher;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonObjectMapper;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomGenerator;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.core.helper.ClassJieHelper;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzIntegralPool;
import com.voxlearning.utopia.service.advertisement.client.UserAdvertisementServiceClient;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.ClazzService;
import com.voxlearning.utopia.service.user.api.TeacherAlterationService;
import com.voxlearning.utopia.service.user.api.TeacherSystemClazzService;
import com.voxlearning.utopia.service.user.api.TeacherSystemClazzService.TeacherToDoList;
import com.voxlearning.utopia.service.user.api.constants.ClazzCreateSourceType;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationType;
import com.voxlearning.utopia.service.user.api.constants.UserBehaviorType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.entities.log.UserOperatorType;
import com.voxlearning.utopia.service.user.api.mappers.*;
import com.voxlearning.utopia.service.user.api.mappers.clazz.DefaultVirtualClazz;
import com.voxlearning.utopia.service.user.client.*;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import com.voxlearning.washington.support.SessionUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.SUCCESS;
import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.RES_GROUP_LIST;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.*;


/**
 * 老师班级相关API类
 * Created by Alex on 15-1-7.
 */
@Controller
@RequestMapping(value = "/v1/teacher")
@Slf4j
public class TeacherClazzApiController extends AbstractTeacherApiController {

    @Inject private RaikouSystem raikouSystem;

    @Inject private AsyncUserServiceClient asyncUserServiceClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;


    public static Set<Subject> SUPPORT_SUBJECT_SET = new LinkedHashSet<>();

    static {
        SUPPORT_SUBJECT_SET.add(Subject.ENGLISH);
        SUPPORT_SUBJECT_SET.add(Subject.MATH);
        SUPPORT_SUBJECT_SET.add(Subject.CHINESE);
    }

    @Inject private RaikouSDK raikouSDK;

    @Inject private UserAdvertisementServiceClient userAdvertisementServiceClient;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;
    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private ClazzIntegralServiceClient clazzIntegralServiceClient;
    @Inject private FlowerServiceClient flowerServiceClient;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private UserSmsServiceClient userSmsServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;

    @Deprecated
    @RequestMapping(value = "/clazzbook.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getClazzBook() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CLAZZ_ID, "班级ID");
            validateRequest(REQ_CLAZZ_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, RES_RESULT_SUSPEND_MSG);
        return resultMap;

    }

    @Deprecated
    @RequestMapping(value = "/clazzbook/change.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage changeClazzBook() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CLAZZ_ID, "班级ID");
            validateRequired(REQ_BOOK_ID, "教材ID");
            validateRequest(REQ_CLAZZ_ID, REQ_BOOK_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, RES_RESULT_SUSPEND_MSG);
        return resultMap;

    }

    /**
     * 通过学校id获取班级列表
     *
     * @return
     */
    @RequestMapping(value = "/school/clazz/list.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage clazzListBySchool() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SCHOOL_ID, "学校ID");
            validateRequired(REQ_CLAZZ_TYPE, "班级类型");
            validateRequest(REQ_SCHOOL_ID, REQ_CLAZZ_TYPE);
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

        // 读取的班级类型，行政班、教学班
        ClazzType clazzType = ClazzType.parse(getRequestInt(REQ_CLAZZ_TYPE));

        // 学校id
        Long schoolId = getRequestLong(REQ_SCHOOL_ID);
        if (schoolId == 0L) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            schoolId = teacherDetail.getTeacherSchoolId();
        }
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        EduSystemType eduSystem = EduSystemType.of(
                schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly()
        );
        if (eduSystem == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "学校学制异常，请联系客服调整");
            return resultMap;
        }
        List<Map<String, Object>> clazzLevelList = new ArrayList<>();

        String[] levels = eduSystem.getCandidateClazzLevel().split(",");
        if (ClazzType.WALKING.equals(clazzType)) {
            // 教学班
            SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                    .loadSchoolExtInfo(schoolId)
                    .getUninterruptibly();
            for (String j : levels) {
                ClazzLevel clazzLevel = ClazzLevel.parse(SafeConverter.toInt(j));

                Map<String, Object> level = new LinkedHashMap<>();
                List<Map<String, Object>> clazzMapList = new LinkedList<>();

                if (schoolExtInfo == null
                        || MapUtils.isEmpty(schoolExtInfo.getWalkingClazzs())
                        || schoolExtInfo.getWalkingClazzs().get(clazzLevel) == null
                        || schoolExtInfo.getWalkingClazzs().get(clazzLevel).get(teacher.getSubject()) == null) {
                    for (int i = 0; i < 8; i++) {
                        Map<String, Object> clazzMap = new LinkedHashMap<>();
                        String clazzName = teacher.getSubject().getValue() + (i + 1) + "班";
                        clazzMap.put(RES_CLAZZ_ID, 0);
                        clazzMap.put(RES_CLAZZ_NAME, clazzName);
                        clazzMap.put(RES_CLAZZ_LEVEL, clazzLevel.getLevel());
                        clazzMap.put(RES_CLAZZ_LEVEL_NAME, clazzLevel.getDescription());
                        clazzMap.put(RES_CLAZZ_TYPE, ClazzType.WALKING.getType());
                        clazzMapList.add(clazzMap);
                    }
                } else {// 根据编制信息填充
                    Set<String> walkingClazzNames = schoolExtInfo.getWalkingClazzs().get(clazzLevel).get(teacher.getSubject());
                    List<String> names = walkingClazzNames.stream().sorted(String::compareTo).collect(Collectors.toList());
                    for (String name : names) {
                        Map<String, Object> clazzMap = new LinkedHashMap<>();
                        clazzMap.put(RES_CLAZZ_ID, 0);
                        clazzMap.put(RES_CLAZZ_NAME, teacher.getSubject().getValue() + name);
                        clazzMap.put(RES_CLAZZ_LEVEL, clazzLevel.getLevel());
                        clazzMap.put(RES_CLAZZ_LEVEL_NAME, clazzLevel.getDescription());
                        clazzMap.put(RES_CLAZZ_TYPE, ClazzType.WALKING.getType());
                        clazzMapList.add(clazzMap);
                    }
                }
                level.put(RES_CLAZZ_LEVEL, clazzLevel.getLevel());
                level.put(RES_CLAZZ_LEVEL_NAME, clazzLevel.getDescription());
                level.put(RES_CLAZZ_LIST, clazzMapList);
                clazzLevelList.add(level);
            }
        } else {
            // 班级数，学前10，其他20
            int clazzCount = 20;

            ClazzLevel maxClazzLevel = ClazzLevel.parse(SafeConverter.toInt(levels[levels.length - 1]));
            ClazzLevel minClazzLevel = ClazzLevel.parse(SafeConverter.toInt(levels[0]));
//            if (teacher.isInfantTeacher()) {
//                maxClazzLevel = ClazzLevel.INFANT_FOURTH;
//                minClazzLevel = ClazzLevel.INFANT_FIRST;
//                clazzCount = 10;
//            } else if (teacher.isJuniorTeacher()) {
//                maxClazzLevel = ClazzLevel.NINTH_GRADE;
//                minClazzLevel = ClazzLevel.SIXTH_GRADE;
//            }

            final int maxJie = ClassJieHelper.fromClazzLevel(minClazzLevel);
            final int minJie = ClassJieHelper.fromClazzLevel(maxClazzLevel);

            List<Clazz> clazzs = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadSchoolClazzs(schoolId)
                    .enabled()
                    .clazzType(ClazzType.PUBLIC)
                    .filter(e -> e.getJie() >= minJie && e.getJie() <= maxJie)
                    .toList();

            if (teacher.isInfantTeacher()) {  // 学前倒着排
                for (int j = maxClazzLevel.getLevel(); j >= minClazzLevel.getLevel(); j--) {
                    clazzLevelList.add(generateClazzLevelInfo(j, clazzCount, clazzs));
                }
            } else {
                for (int j = minClazzLevel.getLevel(); j <= maxClazzLevel.getLevel(); j++) {
                    clazzLevelList.add(generateClazzLevelInfo(j, clazzCount, clazzs));
                }
            }
        }
//        else {
//            // 行政班
//            int maxJie = teacher.isPrimarySchool() ? ClassJieHelper.fromClazzLevel(ClazzLevel.FIRST_GRADE)
//                    : ClassJieHelper.fromClazzLevel(ClazzLevel.SIXTH_GRADE);
//            int minJie = teacher.isPrimarySchool() ? ClassJieHelper.fromClazzLevel(ClazzLevel.SIXTH_GRADE)
//                    : ClassJieHelper.fromClazzLevel(ClazzLevel.NINTH_GRADE);
//
//            List<Clazz> clazzs = teacherSystemClazzServiceClient.loadSystemClazzsInfoByTeacherIdAndSchoolId(teacher.getId(), schoolId, minJie, maxJie);
////            List<Clazz> clazzs = clazzLoaderClient.loadSchoolClazzs(schoolId)
////                    .enabled()
////                    .clazzType(ClazzType.PUBLIC)
////                    .filter(e -> e.getJie() >= minJie && e.getJie() <= maxJie)
////                    .toList();
//
//            if (CollectionUtils.isNotEmpty(clazzs)) {
//                clazzs.stream()
////                        .filter(c -> !c.isTerminalClazz()) // 五四制支持，再过滤一下毕业班
////                        .sorted(new Clazz.ClazzLevelAndNameComparator())
//                        .map(clazz -> {
//                            Map<String, Object> clazzMap = new HashMap<>();
//                            clazzMap.put(RES_CLAZZ_ID, clazz.getId());
//                            clazzMap.put(RES_CLAZZ_NAME, clazz.getClassName());
//                            clazzMap.put(RES_CLAZZ_LEVEL, clazz.getClazzLevel().getLevel());
//                            clazzMap.put(RES_CLAZZ_LEVEL_NAME, clazz.getClazzLevel().getDescription());
//                            clazzMap.put(RES_CLAZZ_TYPE, clazz.getClazzType().getType());
//                            return clazzMap;
//                        })
//                        .collect(Collectors.groupingBy(e -> (int) e.get(RES_CLAZZ_LEVEL)))
//                        .forEach((l, cl) -> {
//                            Map<String, Object> level = new LinkedHashMap<>();
//                            level.put(RES_CLAZZ_LEVEL, l);
//                            level.put(RES_CLAZZ_LEVEL_NAME, ClazzLevel.parse(l).getDescription());
//                            level.put(RES_CLAZZ_LIST, cl);
//                            clazzLevelList.add(level);
//                        });
//            }
//        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap.add(RES_CLAZZ_LEVEl_LIST, clazzLevelList);
    }

    private Map<String, Object> generateClazzLevelInfo(int levelValue, int clazzCount, List<Clazz> clazzs) {
        Map<String, Object> level = new LinkedHashMap<>();

        ClazzLevel clazzLevel = ClazzLevel.parse(levelValue);

        List<Map<String, Object>> clazzMapList = new LinkedList<>();
        for (int i = 0; i < clazzCount; i++) {
            Map<String, Object> clazzMap = new LinkedHashMap<>();
            String clazzName = (i + 1) + "班";
            Clazz clazz = clazzs.stream()
                    .filter(p -> p.getClazzLevel().getLevel() == clazzLevel.getLevel())
                    .filter(p -> Objects.equals(p.getClassName(), clazzName))
                    .findFirst().orElse(null);
            if (clazz == null) {
                DefaultVirtualClazz vc = new DefaultVirtualClazz(i, ClassJieHelper.fromClazzLevel(clazzLevel), clazzName);
                clazzMap.put(RES_CLAZZ_ID, vc.generateVirtualClazzId());
            } else {
                clazzMap.put(RES_CLAZZ_ID, clazz.getId());
            }
            clazzMap.put(RES_CLAZZ_NAME, clazzName);
            clazzMap.put(RES_CLAZZ_LEVEL, clazzLevel.getLevel());
            clazzMap.put(RES_CLAZZ_LEVEL_NAME, clazzLevel.getDescription());
            clazzMap.put(RES_CLAZZ_TYPE, ClazzType.PUBLIC.getType());
            clazzMapList.add(clazzMap);
        }

        // 添加系统非正规命名班级
        List<Clazz> speicalClazzList = clazzs.stream()
                .filter(p -> p.getClazzLevel() == clazzLevel)
                .filter(p -> StringUtils.isNoneBlank(p.getClassName()) && !p.getClassName().matches("[1-9]班") && !p.getClassName().matches("1[0-9]班") && !Objects.equals(p.getClassName(), "20班"))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(speicalClazzList)) {
            for (Clazz specialClazz : speicalClazzList) {
                Map<String, Object> clazzMap = new LinkedHashMap<>();
                clazzMap.put(RES_CLAZZ_ID, specialClazz.getId());
                clazzMap.put(RES_CLAZZ_NAME, specialClazz.getClassName());
                clazzMap.put(RES_CLAZZ_LEVEL, clazzLevel.getLevel());
                clazzMap.put(RES_CLAZZ_LEVEL_NAME, clazzLevel.getDescription());
                clazzMap.put(RES_CLAZZ_TYPE, ClazzType.PUBLIC.getType());
                clazzMapList.add(clazzMap);
            }
        }

        level.put(RES_CLAZZ_LEVEL, clazzLevel.getLevel());
        level.put(RES_CLAZZ_LEVEL_NAME, clazzLevel.getDescription());
        level.put(RES_CLAZZ_LIST, clazzMapList);

        return level;
    }

    /**
     * 老师创建班级
     * 接口无用.
     *
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/clazz/create.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage createclazz() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CLAZZ_ID, "班级id");
            validateRequest(REQ_CLAZZ_ID);
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, RES_RESULT_SUSPEND_MSG);
        return resultMap;
    }


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
                List<SystemClazzInfo> clazzs = JsonUtils.fromJsonToList(JsonUtils.toJson(m.get("clazzs")), SystemClazzInfo.class);
                clazzs.forEach(c -> clazzIdSet.add(c.getId()));
            });
        });

        //TODO 封装返回
        MapMessage message = teacherSystemClazzInfoServiceClient.getNewAddAndAdjustClazzs(teacher.getId(), clazzIdSet, ClazzCreateSourceType.app, false);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add("newClazzs", message.get("newClazzs"));
        resultMap.add("adjustClazzs", message.get("adjustClazzs"));
        return resultMap;
    }

    /**
     * 老师调整班级(行政班教学班添加删除,申请关联行政班)
     *
     * @return
     */
    @RequestMapping(value = "/clazz/adjust.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage adjustclazz() throws IOException {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_JSON, "参数");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateEnum(REQ_SUBJECT, "学科", Subject.CHINESE.name(), Subject.MATH.name(), Subject.ENGLISH.name());
                validateRequest(REQ_SUBJECT, REQ_JSON);
            } else
                validateRequest(REQ_JSON);
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        //支持包班制
        Teacher curTeacher = getCurrentTeacherBySubject();
        if (curTeacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SUBJECT_ERROR_MSG);
            return resultMap;
        }

        //判断是否为假老师
        if (teacherLoaderClient.isFakeTeacher(curTeacher.getId())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_FAKE_TEACHER_MSG);
            return resultMap;
        }

        // 检查教务老师
//        MapMessage checkMsg = specialTeacherServiceClient.checkExistAffairTeacher(curTeacher, "学校已有教务老师，请联系教务{}老师进行班级调整");
//        if (!checkMsg.isSuccess()) {
//            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            resultMap.add(RES_MESSAGE, checkMsg.getInfo());
//            return resultMap;
//        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(getCurrentTeacher().getTeacherSchoolId()).getUninterruptibly();
        if (schoolExtInfo != null && schoolExtInfo.fetchAdjustClazz()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "您不可自主调整班级或学生，如有疑问请联系一起客服");
            return resultMap;
        }


        ObjectMapper mapper = JsonObjectMapper.OBJECT_MAPPER;
        JsonNode rootNode = mapper.readTree(getRequestString(REQ_JSON));
        CollectionType LongListType = mapper.getTypeFactory().constructCollectionType(List.class, Long.class);

        List<Long> adjustClazzIds = new LinkedList<>();

        JsonNode tempgroupNode = rootNode.get("takeovergroups");
        if (tempgroupNode != null && tempgroupNode.size() > 0) {
            Set<Long> tempGroupIds = new HashSet<>();
            for (JsonNode childNode : tempgroupNode) {
                Long groupId = childNode.asLong();
                tempGroupIds.add(groupId);
            }
            List<Long> tempClazzIds = deprecatedGroupLoaderClient.loadGroups(tempGroupIds, false).values().stream().map(GroupMapper::getClazzId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(tempClazzIds)) {
                adjustClazzIds.addAll(tempClazzIds); //避免加入后又在后面的逻辑中被删了 原因是clazzServiceClient.teacherAdjustSystemClazzs逻辑中根据adjustClazzIds去除不在其中的班级
            }
            MapMessage msg = groupServiceClient.joinTeacherIntoGroups(curTeacher.getId(), tempGroupIds);
            if (!msg.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, msg.getInfo());
                return resultMap;
            }
        }


        // 处理加入班级已有资源请求
        JsonNode node = rootNode.get("newClazzs");
        if (node != null) {
            for (JsonNode childNode : node) {
                Long clazzId = childNode.get("clazzId").asLong();
                adjustClazzIds.add(clazzId);// 对于发送请求的加入班级，老师先进班，请求是另外一回事
                List<Long> teacherIds = new LinkedList<>();
                for (JsonNode groupNode : childNode.get("groups")) {
                    if (groupNode.get("teachers") != null) {
                        for (JsonNode teacherNode : groupNode.get("teachers")) {
                            teacherIds.add(teacherNode.get("id").asLong());
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(teacherIds)) {
                    MapMessage msg = handleTeacherRequestStudentResource(curTeacher, clazzId, teacherIds);
                    if (!msg.isSuccess()) {
                        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                        resultMap.add(RES_MESSAGE, msg.getInfo());
                        return resultMap;
                    }
                }
            }
        }

        node = rootNode.get("adjustClazzs");
        adjustClazzIds.addAll(mapper.readValue(node.traverse(), LongListType));

        node = rootNode.get("adjustWalkingClazzs");
        Map<String, List<Map<String, Object>>> map = mapper.convertValue(node, Map.class);

        // 处理直接加入班级请求
        MapMessage message = MapMessage.successMessage();
        if (CollectionUtils.isNotEmpty(adjustClazzIds) || MapUtils.isNotEmpty(map)) {// 当教学班有班级时，允许退出所有行政班
            message = clazzServiceClient.teacherAdjustSystemClazzs(curTeacher.getId(), adjustClazzIds, OperationSourceType.app);
        }

        // 处理直接加入教学班级
        if (curTeacher.isJuniorTeacher()) {
            if (message.isSuccess() && (MapUtils.isNotEmpty(map) || CollectionUtils.isNotEmpty(adjustClazzIds))) {// 当行政班不为空时，允许退出所有教学班级
                message = clazzServiceClient.teacherAdjustWalkingCLazz(curTeacher.getId(), map);
            }
        }

        // 无论如何清一下缓存
        asyncUserServiceClient.getAsyncUserService().evictUserCache(curTeacher.getId()).awaitUninterruptibly();

        if (message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, message.getInfo());
        }

        return resultMap;
    }

    /**
     * 老师创建班级
     * 此接口木有用....
     *
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/group/create.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage createGroup() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CLAZZ_ID, "班级id");
            validateRequest(REQ_CLAZZ_ID);
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

        String clazzIdStrs = getRequestString(REQ_CLAZZ_ID);
        try {
            MapMessage result = null;
            for (String clazzIdStr : clazzIdStrs.split(",")) {
                Long clazzId = Long.parseLong(clazzIdStr);
                result = groupServiceClient.createTeacherGroup(teacher.getId(), clazzId, null);
                if (!result.isSuccess()) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_CREATE_CLAZZ_ERROR);
                    return resultMap;
                }

            }
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_CREATE_CLAZZ_ERROR);
            return resultMap;
        }

        return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
    }


    /**
     * 老师的班级列表
     * 支持包班制了
     * 如果有学科,
     * a. 如果该老师有副账号,则返回对应学科的班级列表.
     * b. 如果该老师没有副账号,他么是错误情况,如果学科对的上还好,对不上就忽略...
     * 如果没有学科
     * a. 如果该老师有副账号,则返回副账号所有学科的班级,班级要带上这个老师所有的学科
     * b. 如果该老师没有副账号,则返回这个老师的这个学科的班级,不用带这个老师的学科.
     *
     * @return
     */
    @RequestMapping(value = "/clazz/list.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage teacherClazzList() {
        MapMessage resultMap = new MapMessage();
        try {
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateEnum(REQ_SUBJECT, "学科", Subject.CHINESE.name(), Subject.MATH.name(), Subject.ENGLISH.name());
                validateRequest(REQ_SUBJECT);
            } else {
                validateRequest();
            }
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        Teacher curTeacher = getCurrentTeacher();

        boolean isMultiSubjectTeacher = teacherLoaderClient.loadRelTeacherIds(curTeacher.getId()).size() > 1;

        List<GroupClazzMapper> groupClazzMappers = teacherSystemClazzServiceClient.loadTeacherAllGroupsData(curTeacher.getId());

        List<Map<String, Object>> groupListRes = new ArrayList<>();

        Set<Long> handledClazzIds = new HashSet<>();
        for (GroupClazzMapper groupClazzMapper : groupClazzMappers) {
            Long clazzId = groupClazzMapper.getClazzId();
            if (handledClazzIds.contains(clazzId))
                continue;

            Map<String, Object> groupMap = new LinkedHashMap<>();
            groupMap.put(RES_GROUP_ID, groupClazzMapper.getGroupId());
            groupMap.put(RES_CLAZZ_ID, groupClazzMapper.getClazzId());
            groupMap.put(RES_CLAZZ_NAME, groupClazzMapper.getClazzNameWithoutGrade());
            groupMap.put(RES_CLAZZ_LEVEL, SafeConverter.toInt(groupClazzMapper.getClazzLevel()));
            groupMap.put(RES_CLAZZ_LEVEL_NAME, ClazzLevel.parse(SafeConverter.toInt(groupClazzMapper.getClazzLevel())).getDescription());
            groupMap.put(RES_CLAZZ_TYPE, ClazzType.valueOf(groupClazzMapper.getClazzType()).getType());
            groupMap.put(RES_CLAZZ_SIZE, groupClazzMapper.getStudentCount());
            groupMap.put(RES_GROUP_FREE_JOIN, groupClazzMapper.isFreeJoin());
            if (isMultiSubjectTeacher) {
                groupMap.put(RES_SUBJECT_LIST, toSubjectList(groupClazzMapper.getMultiSubjects(), true));
                if (curTeacher.isPrimarySchool() || curTeacher.isInfantTeacher()) {
                    List<GroupMapper> teacherAllGroupInClazz = teacherLoaderClient.findTeacherAllGroupInClazz(groupClazzMapper.getClazzId(), curTeacher.getId());
                    Set<Long> groupIdSet = teacherAllGroupInClazz.stream().map(GroupMapper::getId).collect(Collectors.toSet());
                    Map<Long, SmartClazzIntegralPool> integralPoolMap = clazzIntegralServiceClient.getClazzIntegralService()
                            .loadClazzIntegralPools(groupIdSet)
                            .getUninterruptibly();
                    groupMap.put(RES_INTEGRAL_COUNT, integralPoolMap.values().stream().mapToInt(SmartClazzIntegralPool::fetchTotalIntegral).sum());
                }
            } else {
                if (curTeacher.isPrimarySchool() || curTeacher.isInfantTeacher()) {
                    SmartClazzIntegralPool smartClazzIntegralPool = clazzIntegralServiceClient.getClazzIntegralService()
                            .loadClazzIntegralPool(groupClazzMapper.getGroupId())
                            .getUninterruptibly();
                    groupMap.put(RES_INTEGRAL_COUNT, smartClazzIntegralPool == null ? 0 : smartClazzIntegralPool.fetchTotalIntegral());
                }
            }
            groupListRes.add(groupMap);
            handledClazzIds.add(clazzId);
        }

        Collections.sort(groupListRes, (o1, o2) -> {
            int cl1 = SafeConverter.toInt(o1.get(RES_CLAZZ_LEVEL));
            int cl2 = SafeConverter.toInt(o2.get(RES_CLAZZ_LEVEL));
            if (cl2 - cl1 != 0) {
                if (curTeacher.isInfantTeacher()) {
                    return cl2 - cl1;
                } else {
                    return cl1 - cl2;
                }
            } else {
                String cn1 = SafeConverter.toString(o1.get(RES_CLAZZ_NAME)).replace("班", "").trim();
                String cn2 = SafeConverter.toString(o2.get(RES_CLAZZ_NAME)).replace("班", "").trim();
                return Integer.compare(SafeConverter.toInt(cn1, 99), SafeConverter.toInt(cn2, 99));
            }
        });

        resultMap.add(RES_GROUP_LIST, groupListRes);

        return resultMap;
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

        // 老师列表
        resultMap.add(RES_CLAZZ_TEACHER_LIST, getClazzTeachers(clazz.getId(), curTeacher.getId()));

        List<Map<String, Object>> studentList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(groupMapper.getStudents())) {
            groupMapper.getStudents().forEach(t -> {
                Map<String, Object> studentMap = new LinkedHashMap<>();
                studentMap.put(RES_STUDENT_ID, t.getId());
                studentMap.put(RES_STUDENT_NAME, t.getName());
                studentList.add(studentMap);
            });
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_STUDENT_LIST, studentList);
        MonthRange range = MonthRange.current();
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
                        map.put(RES_FLOWER_COUNT, flowers == null ? 0 : flowers.stream().filter(f -> f.getReceiverId() != null && Objects.equals(f.getReceiverId(), groupTeacher.getId()))
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

    // 获取班级的老师列表
    private List<Map<String, Object>> getClazzTeachers(Long clazzId, Long curTeacherId) {
        List<Map<String, Object>> teacherList = new ArrayList<>();
        // 老师列表
        List<ClazzTeacher> teachers = teacherLoaderClient.loadClazzTeachers(clazzId);
        if (CollectionUtils.isEmpty(teachers)) {
            return teacherList;
        }

        // 包班制
        List<Long> curTeacherIds = new ArrayList<>();
        CollectionUtils.addNonNullElement(curTeacherIds, teacherLoaderClient.loadMainTeacherId(curTeacherId));
        curTeacherIds.addAll(teacherLoaderClient.loadSubTeacherIds(curTeacherId));
        curTeacherIds.add(curTeacherId);

        for (ClazzTeacher clazzTeacher : teachers) {
            if (clazzTeacher.getTeacher() == null || clazzTeacher.getTeacher().isDisabledTrue()) {
                continue;
            }

            if (curTeacherIds.contains(clazzTeacher.getTeacher().getId())) {
                continue;
            }

            // 过滤掉假老师
            CrmTeacherSummary teacherSummary = crmSummaryLoaderClient.loadTeacherSummary(clazzTeacher.getTeacher().getId());
            if (teacherSummary != null && !teacherSummary.getManualFakeTeacher()) {
                continue;
            }

            Map<String, Object> teacherInfo = new LinkedHashMap<>();
            teacherInfo.put("teacherName", clazzTeacher.getTeacher().fetchRealname());
            if (clazzTeacher.getTeacher().getSubject() != null) {
                teacherInfo.put("teacherSubject", clazzTeacher.getTeacher().getSubject().name());
                teacherInfo.put("teacherSubjectName", clazzTeacher.getTeacher().getSubject().getValue());
            }
            teacherInfo.put("authStatus", clazzTeacher.getTeacher().fetchCertificationState().getDescription());
            teacherInfo.put("teacherAvatar", getUserAvatarImgUrl(clazzTeacher.getTeacher()));
            teacherList.add(teacherInfo);
        }

        return teacherList;
    }

    /**
     * 老师的班级空间点学生的详情
     *
     * @return
     */
    @RequestMapping(value = "/clazz/student/detail.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage teacherClazzStudentDetail() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_GROUP_ID, "分组id");
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequest(REQ_GROUP_ID, REQ_STUDENT_ID);
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
        Long studentId = getRequestLong(REQ_STUDENT_ID);

        Teacher curTeacher = getCurrentTeacher();

        //开始验证老师分组是否正确
        Boolean groupRight = teacherLoaderClient.hasRelTeacherTeachingGroup(curTeacher.getId(), groupId);
        if (!groupRight) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_TEACHER_GROUP_ERROR);
            return resultMap;
        }


        //验证学生是否属于该组
        Boolean studentRight = validateGroupStudent(groupId, studentId);
        if (!studentRight) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
            return resultMap;
        }

        User student = raikouSystem.loadUser(studentId);
        if (student == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
            return resultMap;
        }
        String mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(student.getId());
        if (StringUtils.isEmpty(mobile)) {
            resultMap.add(RES_STUDENT_MOBILE, "");
        } else {
            resultMap.add(RES_STUDENT_MOBILE, mobile);
        }
        resultMap.add(RES_STUDENT_NAME, student.fetchRealname());
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }


    /**
     * 设置班级是否允许学生加入
     *
     * @return
     */
    @RequestMapping(value = "/clazz/free_join.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage setClazzAllowIn() {

        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_GROUP_ID, "分组id");
            validateRequired(REQ_GROUP_FREE_JOIN, "是否可加入");
            validateEnum(REQ_GROUP_FREE_JOIN, "是否可加入", "true", "false");
            validateRequest(REQ_GROUP_ID, REQ_GROUP_FREE_JOIN);
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
        Boolean freeJoin = getRequestBool(REQ_GROUP_FREE_JOIN);
        GroupMapper groupMapper = deprecatedGroupLoaderClient.loadGroup(groupId, true);
        MapMessage mapMessage = clazzServiceClient.updateSystemClazzFreeJoin(curTeacher.getId(), groupMapper.getClazzId(), freeJoin);
        if (mapMessage.isSuccess()) {
            return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_FREE_JOIN_ERROR);
            return resultMap;
        }

    }

    /**
     * 设置是否显示班级排行榜
     *
     * @return
     */
    @RequestMapping(value = "/clazz/set_show_rank.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage setShowRank() {

        MapMessage resultMap = new MapMessage();
        return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
    }


    /**
     * 老师重置学生密码
     * 逻辑基本copy from pc端  com/voxlearning/ucenter/controller/teacher/TeacherClazzController.java
     *
     * @return
     */
    @RequestMapping(value = "/clazz/student/password/reset.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage resetStudentPassword() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_GROUP_ID, "分组id");
            validateRequired(REQ_STUDENT_ID, "学生id");
            if (!StringUtils.isEmpty(getRequestString(REQ_NEW_PASSWD)))
                validateRequest(REQ_GROUP_ID, REQ_STUDENT_ID, REQ_NEW_PASSWD);
            else
                validateRequest(REQ_GROUP_ID, REQ_STUDENT_ID);
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
        Long groupId = getRequestLong(REQ_GROUP_ID);
        //开始验证老师分组是否正确
        Boolean groupRight = teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), groupId);
        if (!groupRight) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_TEACHER_GROUP_ERROR);
            return resultMap;
        }

        Long studentId = getRequestLong(REQ_STUDENT_ID);
        //验证学生是否属于该组
        Boolean studentRight = validateGroupStudent(groupId, studentId);
        if (!studentRight) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
            return resultMap;
        }
        Long clazzId = deprecatedGroupLoaderClient.loadGroup(groupId, false).getClazzId();

        String password = getRequestString(REQ_NEW_PASSWD);
        String confirmPassword = password;
        // 如果学生未绑定手机，老师可以直接重置密码
        // 如果学生绑定了家长手机，则一天只能重置一次密码，且通过给手机发送随机密码的方式重置
        // 统一用一个接口并增加检查的原因是防止用户通过接口直接重置
        String authenticatedMobile = sensitiveUserDataServiceClient.showUserMobile(studentId, "resetstudentpassword", SafeConverter.toString(studentId));
        if (authenticatedMobile != null && (StringUtils.isNotBlank(authenticatedMobile))) {
            // 老师给学生重置密码行为，一天只能一次
            if (!asyncUserCacheServiceClient.getAsyncUserCacheService()
                    .TeacherResetBindedStudentPWCacheManager_canResetPw(teacher.getId(), studentId)
                    .getUninterruptibly()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_TEACHER_RESET_PASSWORD_OVER_LIMIT);
                return resultMap;
            }

            // 生成随机密码
            confirmPassword = password = RandomGenerator.generatePlainPassword();
        }

        // 修改密码
        try {
            MapMessage mesg = AtomicLockManager.instance().wrapAtomic(teacherServiceClient)
                    .keyPrefix(ClazzService.MANAGE_CLAZZ_PREFIX)
                    .keys(studentId)
                    .proxy()
                    .changeClazzStudentPassword(teacher, clazzId, studentId, password, confirmPassword);
            if (mesg.isSuccess()) {
                // 记录 UserServiceRecord
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(studentId);
                userServiceRecord.setOperatorId(teacher.getId().toString());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
                userServiceRecord.setOperationContent("老师重置学生密码");
                userServiceRecord.setComments("老师重置学生[" + studentId + "]密码，操作端[app]");
                userServiceRecord.setAdditions("refer:TeacherClazzController.resetStudentPassword");
                userServiceClient.saveUserServiceRecord(userServiceRecord);
            }

            // 重置密码后处理
            User student = raikouSystem.loadUser(studentId);
            // 如果学生修改密码，更新学生端sessionkey
            updateAppSessionKeyForStudent(studentId);
            asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                    .unflushable_clearUserBehaviorCount(UserBehaviorType.STUDENT_FORCE_RESET_PW, studentId)
                    .awaitUninterruptibly();


            // 如果是绑定手机的学生，则发送重置密码短信
            if (authenticatedMobile != null && (StringUtils.isNotBlank(authenticatedMobile))) {
                String smsPayload = StringUtils.formatMessage(
                        "{}同学好，老师正在帮你重置密码，请用新密码：{}登录做作业（如孩子在学校使用，请尽快将新密码转发给老师）",
                        student.fetchRealname(),
                        password
                );
                smsServiceClient.createSmsMessage(authenticatedMobile)
                        .content(smsPayload)
                        .type(SmsType.TEACHER_RESET_STUDENT_PASSWORD.name())
                        .send();
            }

            // 老师修改学生密码,需要强制学生修改密码
            asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                    .unflushable_setUserBehaviorCount(UserBehaviorType.STUDENT_FORCE_RESET_PW, studentId, 1L, 0)
                    .awaitUninterruptibly();

            return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } catch (DuplicatedOperationException ex) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_DUPLICATE_DEAL);
            return resultMap;
        }

    }

    /**
     * 修改密码后更新学生App的session key
     *
     * @param sid
     * @author changyuan.liu
     */
    private void updateAppSessionKeyForStudent(Long sid) {
        VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Student", sid);
        if (vendorAppsUserRef != null) {
            vendorServiceClient.expireSessionKey(
                    "17Student",
                    sid,
                    SessionUtils.generateSessionKey(commonConfiguration.getSessionEncryptKey(), sid));
        }
    }


    /**
     * 老师修改学生姓名
     *
     * @return
     */
    @RequestMapping(value = "/clazz/student/name/update.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage changeStudentName() {
        // Feature #54929
//        if (ForbidModifyNameAndPortrait.check()) {
//            return ForbidModifyNameAndPortrait.errorMessage;
//        }
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_GROUP_ID, "分组id");
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequiredLength(REQ_STUDENT_NAME, 2, 6, "学生姓名");
            validateRequest(REQ_GROUP_ID, REQ_STUDENT_ID, REQ_STUDENT_NAME);
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
        Long studentId = getRequestLong(REQ_STUDENT_ID);

        Teacher curTeacher = getCurrentTeacher();

        //开始验证老师分组是否正确
        Boolean groupRight = teacherLoaderClient.hasRelTeacherTeachingGroup(curTeacher.getId(), groupId);
        if (!groupRight) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_TEACHER_GROUP_ERROR);
            return resultMap;
        }


        //验证学生是否属于该组
        Boolean studentRight = validateGroupStudent(groupId, studentId);
        if (!studentRight) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
            return resultMap;
        }

        String studentNewName = getRequestString(REQ_STUDENT_NAME);
        if (badWordCheckerClient.containsUserNameBadWord(studentNewName)) {
            return failMessage("输入的姓名信息不合适哦\n有疑问请联系客服：\n400-160-1717");
        }

        // #36892 老师修改学生名字限制 非认证老师 && 同班有其他认证老师时不允许修改
        if (curTeacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
            GroupMapper group = deprecatedGroupLoaderClient.loadGroup(groupId, false);
            if (group != null) {
                List<ClazzTeacher> clazzTeachers = teacherLoaderClient.loadClazzTeachers(group.getClazzId());
                for (ClazzTeacher clazzTeacher : clazzTeachers) {
                    if (clazzTeacher.getTeacher() != null && clazzTeacher.getTeacher().fetchCertificationState() == AuthenticationState.SUCCESS) {
                        return failMessage("需要达到认证，才能进行修改哦");
                    }
                }
            }
        }
        User userOld = raikouSystem.loadUser(studentId);
        MapMessage mapMessage = userServiceClient.changeName(studentId, studentNewName);
        if (mapMessage.isSuccess()) {
            // update user name log
            User student = raikouSystem.loadUser(studentId);
            if (student != null) {
                LogCollector.info("backend-general", MiscUtils.map("usertoken", studentId,
                        "usertype", student.getUserType(),
                        "platform", getApiRequestApp().getAppKey(),
                        "version", getRequestString(REQ_APP_NATIVE_VERSION),
                        "op", "change user name",
                        "mod1", student.fetchRealname(),
                        "mod2", studentNewName,
                        "mod3", 0,
                        "mod4", curTeacher.getId()));
                // 记录 UserServiceRecord
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(student.getId());
                userServiceRecord.setOperatorId(curTeacher.getId().toString());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
                userServiceRecord.setOperationContent("老师修改学生姓名");
                userServiceRecord.setComments("老师[" + curTeacher.getId() + "]修改学生[" + student.getId() + userOld.fetchRealname() + "]姓名为[" + studentNewName + "]，操作端[app]");
                userServiceClient.saveUserServiceRecord(userServiceRecord);
            }
            return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, mapMessage.getInfo());
            return resultMap;
        }
    }


    /**
     * 老师删除组内学生
     *
     * @return
     */
    @RequestMapping(value = "/clazz/student/remove.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteStudent() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_GROUP_ID, "分组id");
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequest(REQ_GROUP_ID, REQ_STUDENT_ID);
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
        Long studentId = getRequestLong(REQ_STUDENT_ID);

        TeacherDetail curTeacher = getApiRequestTeacherDetail();

        // 检查教务老师
        // Feature #53519 开放初中英语、语文任课老师删除学生的权限
//        if (!curTeacher.isJuniorEnglishOrChineseTeacher()) {
//            MapMessage checkMsg = specialTeacherServiceClient.checkExistAffairTeacher(curTeacher, "学校已有教务老师，请联系教务{}老师删除学生");
//            if (!checkMsg.isSuccess()) {
//                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//                resultMap.add(RES_MESSAGE, checkMsg.getInfo());
//                return resultMap;
//            }
//        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(curTeacher.getTeacherSchoolId()).getUninterruptibly();
        if (schoolExtInfo != null && schoolExtInfo.fetchAdjustClazz()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "您不可自主调整班级或学生，如有疑问请联系一起客服");
            return resultMap;
        }

        //开始验证老师分组是否正确
        Boolean groupRight = teacherLoaderClient.hasRelTeacherTeachingGroup(curTeacher.getId(), groupId);
        if (!groupRight) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_TEACHER_GROUP_ERROR);
            return resultMap;
        }

        //验证学生是否属于该组
        Boolean studentRight = validateGroupStudent(groupId, studentId);
        if (!studentRight) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
            return resultMap;
        }
        GroupMapper gm = deprecatedGroupLoaderClient.loadGroup(groupId, false);
        Long clazzId = gm.getClazzId();
        User anotherTeacher = null;

        boolean isTeachingClazz = teacherLoaderClient.isTeachingClazz(curTeacher.getId(), clazzId);
        if (!isTeachingClazz) { //如果老师不在这个班级中
            Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(curTeacher.getId());
            for (Long tempTeacherId : relTeacherIds) {
                User tempTeacher = teacherLoaderClient.loadTeacherDetail(tempTeacherId);
                if (tempTeacher != null) {
                    if (teacherLoaderClient.isTeachingClazz(tempTeacher.getId(), clazzId)) {
                        anotherTeacher = tempTeacher;
                        break;
                    }
                }
            }
        }

        final User teacher;
        if (null == anotherTeacher) {
            teacher = getCurrentTeacher();
        } else {
            teacher = anotherTeacher;
        }

        Clazz c = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        MapMessage message = AtomicLockManager.instance().wrapAtomic(teacherServiceClient)
                .keyPrefix(ClazzService.MANAGE_CLAZZ_PREFIX)
                .keys(clazzId)
                .proxy()
                .deleteClazzStudent(teacher, c, studentId, null);

//        MapMessage mapMessage = groupServiceClient.removeStudents(Collections.singleton(studentId), groupId);
        if (message.isSuccess()) {
            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(studentId);
            userServiceRecord.setOperatorId(SafeConverter.toString(teacher.getId()));
            userServiceRecord.setOperationType(UserServiceRecordOperationType.班组信息变更.name());
            userServiceRecord.setOperationContent("老师删除班组学生");
            userServiceRecord.setComments("老师[" + teacher.getId() + "]删除班组学生[" + studentId + "]，操作端[app]");
            userServiceClient.saveUserServiceRecord(userServiceRecord);

            return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_REMOVE_STUDENT_ERROR);
            return resultMap;
        }

    }

    /**
     * 该班级已添加的老师列表
     * 添加老师选择
     *
     * @return
     */
    @RequestMapping(value = "/clazz/teacher/list.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage clazzTeacherList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_CLAZZ_ID, "班级id");
            validateRequest(REQ_CLAZZ_ID);
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Long clazzId = getRequestLong(REQ_CLAZZ_ID);

        Teacher teacher = getCurrentTeacher();

        List<TeacherSystemClazzService.CanAddSubjectStatus> canAddSubjects = teacherSystemClazzServiceClient.findCanAddSubject(teacher.getId(), clazzId);

        List<Map<String, Object>> teacherMapList = new ArrayList<>();
        canAddSubjects.forEach(e -> {
            Map<String, Object> map = new LinkedHashMap<>();

            map.put(RES_COULD_ADD_TEACHER, e.isCanAdd());
            if (e.getStatus() == 0) {
                map.put(RES_COULD_NOT_ADD_TEXT, "已添加");
            } else if (e.getStatus() == 2) {
                map.put(RES_COULD_NOT_ADD_TEXT, "等待对方同意");
            }
            map.put(RES_TEACHER_ID, e.getTeacherId());
            map.put(RES_TEACHER_NAME, e.getTeacherName() != null ? e.getTeacherName() : "");
            map.put(RES_SUBJECT, e.getSubject().name());
            map.put(RES_SUBJECT_KEY, e.getSubject().getKey());
            map.put(RES_SUBJECT_NAME, e.getSubject().getValue());
            map.put(RES_AUTH_STATE, e.getAuthState() == SUCCESS.getState());

            teacherMapList.add(map);
        });

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap.add(RES_TEACHER_LIST, teacherMapList);
    }

    /**
     * 班级添加老师 老师列表
     * 添加老师选择
     *
     * @return
     */
    @RequestMapping(value = "/clazz/add/teacher/list.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage clazzAddTeacherList() {

        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SUBJECT, "学科");
            validateEnum(REQ_SUBJECT, "学科", Subject.CHINESE.name(), Subject.MATH.name(), Subject.ENGLISH.name());
            validateRequest(REQ_SUBJECT);
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher curTeacher = getCurrentTeacher();

        //添加老师列表前判断当前老师是否为假
        if (teacherLoaderClient.isFakeTeacher(curTeacher.getId())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_FAKE_TEACHER_MSG);
            return resultMap;
        }

        // 判断老师学校
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(curTeacher.getId())
                .getUninterruptibly();
        if (null == school) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_TEACHER_NO_SCHOOL);
            return resultMap;
        }

        Subject subject = Subject.of(getRequestString(REQ_SUBJECT));

        List<Teacher> result = teacherAlterationServiceClient.getTeacherOfSpecificSubjectInTheSameSchool(curTeacher.getId(), subject, true);
        List<Teacher> teacherList = result.stream()
                .filter(p -> (p.fetchCertificationState() == SUCCESS || p.fetchCertificationState() == AuthenticationState.WAITING))
                .filter(p -> !teacherLoaderClient.isFakeTeacher(p.getId()))
                .sorted((o1, o2) -> Long.compare(o2.fetchCertificationState().getState(), o1.fetchCertificationState().getState()))
                .collect(Collectors.toList());

        List<Map<String, Object>> teacherMapList = new ArrayList<>();
        teacherList.forEach(p -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(RES_TEACHER_ID, p.getId());
            map.put(RES_TEACHER_NAME, p.fetchRealname());
            map.put(RES_AUTH_STATE, p.fetchCertificationState().equals(SUCCESS));
            teacherMapList.add(map);
        });

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap.add(RES_TEACHER_LIST, teacherMapList);
    }

    /**
     * 班级添加老师
     * 整段代码是copy from pc端 com/voxlearning/ucenter/controller/teacher/TeacherSystemClazzController.java
     * 包括调用的private方法和内部类
     *
     * @return
     */
    @RequestMapping(value = "/clazz/teacher/add.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage clazzAddTeacher() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_GROUP_ID, "分组id");
            validateRequiredNumber(REQ_TEACHER_ID, "老师id");
            if (StringUtils.isNotBlank(getRequestString(RES_SUBJECT))) {
                validateEnum(REQ_SUBJECT, "学科", Subject.CHINESE.name(), Subject.MATH.name(), Subject.ENGLISH.name());
                validateRequest(REQ_GROUP_ID, RES_TEACHER_ID, RES_SUBJECT);
            } else
                validateRequest(REQ_GROUP_ID, RES_TEACHER_ID);
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher curTeacher = getCurrentTeacher();

        // 添加老师列表前判断当前老师是否为假
        if (teacherLoaderClient.isFakeTeacher(curTeacher.getId())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_FAKE_TEACHER_MSG);
            return resultMap;
        }

        // 要添加的老师
        Long respondentId = getRequestLong(RES_TEACHER_ID);

        // 分组
        Long groupId = getRequestLong(REQ_GROUP_ID);
        GroupMapper groupMapper = deprecatedGroupLoaderClient.loadGroup(groupId, false);
        if (groupMapper == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_TEACHER_GROUP_ERROR);
            return resultMap;
        }

        // 班级id
        Long clazzId = groupMapper.getClazzId();

        String subjectStr = getRequestString(REQ_SUBJECT);
        Long teacherId = curTeacher.getId();
        //包班制老师,添加的班级可能不是主账号的班级,所以得取group的subject对应的子账号老师。
        if (groupMapper.getSubject() != curTeacher.getSubject()) {
            Long subjectTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(curTeacher.getId(), groupMapper.getSubject());
            if (subjectTeacherId != null)
                teacherId = subjectTeacherId;
            else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_TEACHER_GROUP_ERROR);
                return resultMap;
            }
        }

        // 添加老师操作
        MapMessage message;
        if (StringUtils.isNotBlank(subjectStr)) {
            Subject linkSubject = Subject.of(subjectStr);
            message = teacherAlterationServiceClient.sendLinkApplication(teacherId, respondentId, linkSubject, clazzId, OperationSourceType.app);
        } else {
            TeacherSubjectRef teacherSubjectRef = teacherLoaderClient.loadTeacherSubjectRef(respondentId);
            if (teacherSubjectRef == null) {
                resultMap.add(RES_MESSAGE, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_RESULT, RES_RESULT_TEACHER_GROUP_ERROR);
                return resultMap;
            }
            message = teacherAlterationServiceClient.sendLinkApplication(teacherId, respondentId, teacherSubjectRef.getSubject(), clazzId, OperationSourceType.app);
        }

        if (message.isSuccess()) {
            if (SafeConverter.toBoolean(message.get("added"))) {
                resultMap.add(RES_MESSAGE, RES_ADD_TEACHER_SUCCESS);
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                return resultMap;
            }

            if (message.containsKey("recordId")) {// 发送消息
                ApplicationMessageBuilder messageBuilder = new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("{}老师申请一起教{}",
                                applicant.getProfile().getRealname(),
                                clazz.formalizeClazzName());
                    }
                };
                sendAppMessageAndJpushForApplication(message, messageBuilder);
                sendApplicationMessageToRespondent(message, messageBuilder, true, true);
                Teacher respondent = (Teacher) message.get("respondent");
                resultMap.add(RES_MESSAGE, MessageFormat.format(RES_ADD_TEACHER_SEND_APPLICATION_SUCCESS, respondent.fetchRealname()));
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                return resultMap;
            }
        }

        resultMap.add(RES_MESSAGE, message.getInfo());
        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        return resultMap;
    }


    /**
     * 班级转让 老师列表
     *
     * @return
     */
    @RequestMapping(value = "/clazz/transfer/teacher/list.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage clazzTransferTeacherList() {
        MapMessage resultMap = new MapMessage();
        try {
            if (StringUtils.isNotBlank(getRequestString(RES_SUBJECT))) {
                validateEnum(REQ_SUBJECT, "学科", Subject.CHINESE.name(), Subject.MATH.name(), Subject.ENGLISH.name());
                validateRequest(REQ_SUBJECT);
            } else
                validateRequest();
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SUBJECT_ERROR_MSG);
            return resultMap;
        }

        Long teacherId = teacher.getId();

        //班级转让出现老师列表之前判断当前老师是否为假
        if (teacherLoaderClient.isFakeTeacher(teacherId)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_FAKE_TEACHER_MSG);
            return resultMap;
        }

        List<Teacher> list = teacherAlterationServiceClient.getTeacherOfSpecificSubjectInTheSameSchool(teacherId, teacher.getSubject(), true);
        List<Teacher> sortedList = list.stream()
                .filter(p -> p.fetchCertificationState() == SUCCESS || p.fetchCertificationState() == AuthenticationState.WAITING)
                .filter(p -> !teacherLoaderClient.isFakeTeacher(p.getId()))
                .sorted((o1, o2) -> Long.compare(o2.fetchCertificationState().getState(), o1.fetchCertificationState().getState())).collect(Collectors.toList());

        List<Map<String, Object>> teacherList = new ArrayList<>();
        sortedList.forEach(t -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(RES_TEACHER_ID, t.getId());
            map.put(RES_TEACHER_NAME, t.fetchRealname());
            map.put(RES_AUTH_STATE, t.fetchCertificationState().equals(SUCCESS));
            teacherList.add(map);
        });

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_TEACHER_LIST, teacherList);
        return resultMap;
    }

    /**
     * 班级转让(发申请)
     * 整段代码是copy from pc端 com/voxlearning/ucenter/controller/teacher/TeacherSystemClazzController.java
     * 包括调用的private方法和内部类
     * FIXME 应该把这逻辑抽象出来,pc和app公用一套.目前没时间抽了.呵呵
     *
     * @return
     */
    @RequestMapping(value = "/clazz/transfer.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage clazzTransfer() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_GROUP_ID, "分组id");
            validateRequiredNumber(REQ_TEACHER_ID, "老师id");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateEnum(REQ_SUBJECT, "学科", Subject.CHINESE.name(), Subject.MATH.name(), Subject.ENGLISH.name());
                validateRequest(REQ_GROUP_ID, RES_TEACHER_ID, REQ_SUBJECT);
            } else
                validateRequest(REQ_GROUP_ID, RES_TEACHER_ID);
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher applicant = getCurrentTeacherBySubject();
        if (applicant == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SUBJECT_ERROR_MSG);
            return resultMap;
        }

        long respondentId = getRequestLong(RES_TEACHER_ID);

        Long groupId = getRequestLong(REQ_GROUP_ID);
        GroupMapper groupMapper = deprecatedGroupLoaderClient.loadGroup(groupId, false);
        if (groupMapper == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_TEACHER_GROUP_ERROR);
            return resultMap;
        }
        Long clazzId = groupMapper.getClazzId();

        MapMessage message = teacherAlterationServiceClient.sendTransferApplication(applicant.getId(), respondentId, clazzId, OperationSourceType.app);
        if (!message.isSuccess() && !StringUtils.equals(message.getInfo(), TeacherAlterationService.DUP_OPERATION_ERR_MSG)
                && !StringUtils.equals(message.getInfo(), TeacherAlterationService.NO_STUDENT_GROUP_ERR_MSG)
                && !StringUtils.equals(message.getInfo(), TeacherAlterationService.UNUSUAL_APPLICATIONS_ERR_MSG)) {
            message = MapMessage.errorMessage("此申请发送失败，请重试！");
        }

        // 发送消息通知及弹窗
        if (message.isSuccess()) {// 表示发送了申请
            boolean sendMsg = SafeConverter.toBoolean(message.get("sendMsg"));

            if (sendMsg) {
                //同时发app消息和jpush
                ApplicationMessageBuilder messageBuilder = new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("{}老师申请转让{}给你",
                                applicant.getProfile().getRealname(),
                                clazz.formalizeClazzName());
                    }
                };
                sendAppMessageAndJpushForApplication(message, messageBuilder);
                sendApplicationMessageToRespondent(message, messageBuilder, true, true);
            }
            return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_TRANSFER_CLAZZ_ERROR);
            return resultMap;
        }
    }


    /**
     * 老师处理待办事项
     */
    @RequestMapping(value = "/clazz/application/deal.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage dealApplication() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_APPLICATION_ID, "申请id");
            validateRequired(REQ_APPLICATION_TYPE, "申请类型");
            validateEnum(REQ_APPLICATION_TYPE, "申请类型", ClazzTeacherAlterationType.LINK.name(),
                    ClazzTeacherAlterationType.TRANSFER.name(), ClazzTeacherAlterationType.REPLACE.name());
            validateRequired(REQ_APPLICATION_ACTION, "操作");
            validateEnum(REQ_APPLICATION_ACTION, "操作", "reject", "approve");
            validateRequest(REQ_APPLICATION_ID, REQ_APPLICATION_TYPE, REQ_APPLICATION_ACTION);
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
        Long respondentId = teacher.getId();

        String applicationTypeStr = getRequestString(REQ_APPLICATION_TYPE);
        ClazzTeacherAlterationType applicationType = ClazzTeacherAlterationType.valueOf(applicationTypeStr);
        String applicationAction = getRequestString(REQ_APPLICATION_ACTION);


        long recordId = getRequestLong(REQ_APPLICATION_ID);
        if (recordId == 0) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_APPLICATION_ID_ERROR);
            return resultMap;
        }
//        resultMap.add(RES_BANNER_LIST)


        MapMessage message = dealAllApplication(respondentId, applicationType, recordId, applicationAction);

//        switch (applicationType) {
//            case "LINK":
//                return dealLinkApplication(respondentId, recordId, applicationAction);
//            case "TRANSFER":
//                return dealTransferApplication(respondentId, recordId, applicationAction);
//            case "REPLACE":
//                return dealReplaceApplication(respondentId, recordId, applicationAction);
//            default:
//                return resultMap;
//        }
        if (message.isSuccess()) {
            return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, message.getInfo());
            return resultMap;
        }


    }

    private MapMessage dealAllApplication(Long respondentId, ClazzTeacherAlterationType applicationType, long recordId, String applicationAction) {


        //这里恶心了点,壳传过来的老师id是猪账号id,这里需要通过这个请求的respondent老师的学科,然后找这个主账号下面该学科的子账号id。再去处理。。。
        ClazzTeacherAlteration clazzTeacherAlteration = teacherLoaderClient.loadClazzTeacherAlteration(recordId);
        if (clazzTeacherAlteration == null)
            return MapMessage.errorMessage("请求错误");
        Long realRespondentId = clazzTeacherAlteration.getRespondentId();
        Teacher realRespondent = teacherLoaderClient.loadTeacher(realRespondentId);
        if (realRespondent == null)
            return MapMessage.errorMessage("请求对方错误");
        Subject respondentSubject = realRespondent.getSubject();
        if (respondentSubject == null)
            return MapMessage.errorMessage("请求对方学科错误");
        Long subjectTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(respondentId, respondentSubject);
        if (subjectTeacherId == null)
            return MapMessage.errorMessage("请求对方学科异常");
        respondentId = subjectTeacherId;

        ApplicationMessageBuilder messageBuilder = buildMessageBuilder(applicationType, applicationAction);
        switch (applicationAction) {
            case "reject":
                MapMessage message = rejectApplication(respondentId, recordId, applicationType);
                if (message.isSuccess()) {
                    sendApplicationMessageToApplicant(message, messageBuilder, false, false);
                    sendAppMessageForDealApplication(message, messageBuilder);
                }
                return message;
            case "approve":
                MapMessage message1 = teacherAlterationServiceClient.approveApplication(respondentId, recordId, applicationType, OperationSourceType.app);

                if (message1.isSuccess()) {
                    sendApplicationMessageToApplicant(message1, messageBuilder, false, true);
                    sendAppMessageForDealApplication(message1, messageBuilder);
                }
                return message1;
            default:
                return new MapMessage().setSuccess(true);
        }
    }

    private ApplicationMessageBuilder buildMessageBuilder(ClazzTeacherAlterationType applicationType, String applicationAction) {
        if (ClazzTeacherAlterationType.REPLACE.equals(applicationType)) {
            if (applicationAction.equals("approve"))
                return new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("您申请接管{}老师的{}的请求，已被{}老师接受",
                                respondent.getProfile().getRealname(),
                                clazz.formalizeClazzName(),
                                respondent.getProfile().getRealname());
                    }
                };
            if (applicationAction.equals("reject"))
                return new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("您申请接管{}老师的{}的请求，已被{}老师拒绝",
                                respondent.getProfile().getRealname(),
                                clazz.formalizeClazzName(),
                                respondent.getProfile().getRealname());
                    }
                };
        }
        if (ClazzTeacherAlterationType.TRANSFER.equals(applicationType)) {
            if (applicationAction.equals("approve"))
                return new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("您申请转让给{}老师的{}的请求，已被{}接受",
                                respondent.getProfile().getRealname(),
                                clazz.formalizeClazzName(),
                                respondent.getProfile().getRealname());
                    }
                };
            if (applicationAction.equals("reject"))
                return new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("您申请转让给{}老师的{}的请求，已被{}拒绝",
                                respondent.getProfile().getRealname(),
                                clazz.formalizeClazzName(),
                                respondent.getProfile().getRealname());
                    }
                };
        }
        if (ClazzTeacherAlterationType.LINK.equals(applicationType)) {
            if (applicationAction.equals("approve"))
                return new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("您申请和{}老师一起教{}的请求，已被{}老师接受",
                                respondent.getProfile().getRealname(),
                                clazz.formalizeClazzName(),
                                respondent.getProfile().getRealname());
                    }
                };
            if (applicationAction.equals("reject"))
                return new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("您申请和{}老师一起教{}的请求，已被{}老师拒绝",
                                respondent.getProfile().getRealname(),
                                clazz.formalizeClazzName(),
                                respondent.getProfile().getRealname());
                    }
                };
        }
        return null;
    }

    /**
     * 待办事项
     * 顺便带上 首页banner的广告
     * 还带上了二期的新手任务,mlgb
     *
     * @return
     */
    @RequestMapping(value = "/clazz/applications/list.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage receiveApplicationsList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
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

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        //获取首页banner
        final String slotId = "120101";
        List<AdMapper> adMapperList = new ArrayList<>();
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        String ua = getRequest().getHeader("User-Agent");
        String sys = getRequestString(REQ_SYS);
        List<NewAdMapper> newAdMappers = userAdvertisementServiceClient.getUserAdvertisementService()
                .loadNewAdvertisementData(teacher.getId(), slotId, sys, ver);
        Integer index = 0;
        for (NewAdMapper p : newAdMappers) {
            AdMapper ad = new AdMapper();
            ad.setImg(combineCdbUrl(p.getImg()));
            ad.setId(p.getId());
            ad.setResourceUrl(AdvertiseRedirectUtils.redirectUrl(ad.getId(), index, ver, sys, "", 0L));
            ad.setKey(p.getPriority().getLevel());
            adMapperList.add(ad);
            //曝光打点
            if (Boolean.TRUE.equals(p.getLogCollected())) {
                LogCollector.info("sys_new_ad_show_logs",
                        MiscUtils.map(
                                "user_id", teacher.getId(),
                                "env", RuntimeMode.getCurrentStage(),
                                "version", ver,
                                "aid", p.getId(),
                                "acode", SafeConverter.toString(p.getCode()),
                                "index", index,
                                "slotId", slotId,
                                "client_ip", getWebRequestContext().getRealRemoteAddress(),
                                "time", DateUtils.dateToString(new Date()),
                                "agent", ua,
                                "system", sys
                        ));
            }
            index++;
        }
        resultMap.add(RES_BANNER_LIST, adMapperList);

        // 老师首页教学工具
        List<AdMapper> teachingTools = new ArrayList<>();
        List<NewAdMapper> toolList = userAdvertisementServiceClient.getUserAdvertisementService()
                .loadNewAdvertisementData(teacher.getId(), "120102", getRequestString(REQ_SYS));
        // FIXME 这里有个坑， 1.4.9以下的只支持 http 和 SET_GOAL_HOMEWORK
        String appVersion = getRequestString(REQ_APP_NATIVE_VERSION);
        if (CollectionUtils.isNotEmpty(toolList) && VersionUtil.compareVersion(appVersion, "1.4.9.0") < 0) {
            toolList = toolList.stream()
                    .filter(p -> StringUtils.isNoneBlank(p.getUrl()) && (p.getUrl().startsWith("http") || p.getUrl().equals("SET_GOAL_HOMEWORK")))
                    .collect(Collectors.toList());
        }

        // 小学老师端1.5.3以上版本才能显示SET_GOAL_HOMEWORK
        if (teacher.isPrimarySchool() && CollectionUtils.isNotEmpty(toolList) && VersionUtil.compareVersion(appVersion, "1.5.3.0") < 0) {
            toolList = toolList.stream()
                    .filter(p -> StringUtils.isNoneBlank(p.getUrl()) && !p.getUrl().equals("SET_GOAL_HOMEWORK"))
                    .collect(Collectors.toList());
        }

        for (NewAdMapper p : toolList) {
            AdMapper ad = new AdMapper();
            ad.setImg(combineCdbUrl(p.getImg()));
            ad.setId(p.getId());
            ad.setResourceUrl(p.getUrl());
            ad.setKey(p.getPriority().getLevel());
            ad.setTitle(p.getName());
            ad.setDescription(p.getDescription());
            teachingTools.add(ad);
        }
        resultMap.add(RES_TEACHING_TOOLS, teachingTools);

        //新手任务
        resultMap.add(RES_FRESHMAN_TASK_LIST, freshManTask(teacher));


        TeacherToDoList data = teacherSystemClazzServiceClient
                .loadTeacherToDoList(teacher.getId());

        List<Map<String, Object>> appResultList = new ArrayList<>();


        if (data == null) {
            resultMap.add(RES_APPLICATION_LIST, new ArrayList<>());
            return resultMap;
        }
        Map<String, List<TeacherApplicationMapper>> applications = data.getReceivedApplications();
        if (MapUtils.isNotEmpty(applications)) {
            List<Map<String, Object>> applicationsList = new ArrayList<>();
            applications.keySet().forEach(key -> {
                List<TeacherApplicationMapper> appList = applications.get(key);
                if (!CollectionUtils.isEmpty(appList)) {
                    appList.forEach(app -> {
                        Map<String, Object> appMap = new LinkedHashMap<>();
                        appMap.put(RES_APPLICATION_ID, app.getId());
                        appMap.put(RES_APPLICATION_TYPE, app.getType());
                        appMap.put(RES_APPLICATION_CONTENT, generateApplicationContent(app));
                        appMap.put(RES_APPLICATION_APP_DATE, app.getDate().getTime());
                        appMap.put(RES_APPLICATION_CONFIRM_TEXT, generateApplicationConfirmText(app));
                        applicationsList.add(appMap);
                    });

                }
            });
            appResultList.addAll(applicationsList.stream().
                    sorted((o1, o2) -> Long.compare(Long.valueOf(o2.get(RES_APPLICATION_APP_DATE).toString()), Long.valueOf(o1.get(RES_APPLICATION_APP_DATE).toString()))).
                    collect(Collectors.toList()));
        }

        final Map<String, Map<String, List<TeacherApplicationMapper>>> clazzId2ApplicationMap = data.getSendApplications() == null ? new HashMap<>() : data.getSendApplications();

        List<GroupClazzMapper> exitClazzsList = data.getToBeTransferedClazzs();
        if (CollectionUtils.isNotEmpty(exitClazzsList)) {
            exitClazzsList.forEach(exitClazz -> {
                if (!clazzId2ApplicationMap.containsKey(String.valueOf(exitClazz.getClazzId()))) {
                    Map<String, Object> appMap = new LinkedHashMap<>();
                    appMap.put(RES_APPLICATION_ID, 0);
                    appMap.put(RES_APPLICATION_TYPE, "EXITCLAZZ");
                    appMap.put(RES_APPLICATION_CONTENT, generateExitClazzContent(exitClazz));
                    appMap.put(RES_APPLICATION_APP_DATE, 0L);
                    appMap.put(RES_EXITS_CLAZZ_ID, exitClazz.getClazzId());
                    appMap.put(RES_EXITS_GROUP_ID, exitClazz.getGroupId());
                    appMap.put(RES_EXITS_CLAZZ_NAME, exitClazz.getClazzName());
                    appMap.put(RES_SUBJECT, exitClazz.getGroupSubject().name());
                    appMap.put(RES_SUBJECT_NAME, exitClazz.getGroupSubject().getValue());
                    appResultList.add(appMap);
                }
            });
        }

        resultMap.add(RES_APPLICATION_LIST, appResultList);
        return resultMap;

    }

    public List<FreshManTask> freshManTask(Teacher teacher) {

        List<FreshManTask> freshmanTaskList = new ArrayList<>();
        FreshManTask notifyStudentTask = notifyStudentClazzTask(teacher);
        if (notifyStudentTask != null)
            freshmanTaskList.add(notifyStudentTask);


        FreshManTask rewardTask = rewardTaskAccording2Ktwelve(teacher);
        if (rewardTask != null)
            freshmanTaskList.add(rewardTask);

        return freshmanTaskList;
    }

    private FreshManTask rewardTaskAccording2Ktwelve(Teacher teacher) {
        if (teacher.isJuniorTeacher()) {
            return juniorTask(teacher);
        }

        return null;
    }

    private FreshManTask juniorTask(Teacher teacher) {
        if (juniorAuthRewardGrey(teacherLoaderClient.loadTeacherDetail(teacher.getId()))) {
            FreshManTask task = new FreshManTask();
            task.setTitle("新老师领话费");
            task.setContent("满足认证条件可领话费");
            task.setLabel("30元");
            task.setLinkUrl(juniorAuthRewardUrl);
            return task;
        } else
            return null;
    }

    /**
     * .通知学生加入班级 所任教班级人数少于5人.有几个给几个
     */
    private FreshManTask notifyStudentClazzTask(Teacher teacher) {

        //副标题文案为【XX年级YY班人数低于5人】（只有一个班级低于5人），【XX年级YY班等2个班人数低于5人】
        Teacher curTeacher = getCurrentTeacher();
        Set<Long> subTeacherIds = teacherLoaderClient.loadRelTeacherIds(curTeacher.getId());
        Map<Long, List<GroupTeacherMapper>> teacherGroupsMap = deprecatedGroupLoaderClient.loadTeacherGroups(subTeacherIds, true);

        List<GroupTeacherMapper> groupTeacherMappers = new LinkedList<>();
        for (Map.Entry<Long, List<GroupTeacherMapper>> entry : teacherGroupsMap.entrySet()) {
            Long teacherId = entry.getKey();
            List<GroupTeacherMapper> groupTeacherMapperList = entry.getValue();
            //过滤掉老师不再教的分组
            groupTeacherMappers.addAll(groupTeacherMapperList.stream().filter(t -> t.isTeacherGroupRefStatusValid(teacherId)).collect(Collectors.toList()));
        }

        if (CollectionUtils.isEmpty(groupTeacherMappers)) {
            return null;
        }

        List<Long> clazzIdList = groupTeacherMappers.stream().map(GroupTeacherMapper::getClazzId).collect(Collectors.toList());
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIdList)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));

        Long teacherSchoolId = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(curTeacher.getId())
                .getUninterruptibly()
                .getId();
        String clazzName = null;
        Set<Long> clazzIdSet = new HashSet<>();
        for (GroupTeacherMapper groupTeacherMapper : groupTeacherMappers) {
            Long clazzId = groupTeacherMapper.getClazzId();
            Clazz clazz = clazzMap.get(clazzId);
            if (clazz == null || clazz.isTerminalClazz() || !teacherSchoolId.equals(clazz.getSchoolId()))//过滤掉不属于该老师当前学校的班级.因为存在老师换学校的情况.
                continue;
            if (groupTeacherMapper.getStudents().size() > 5)
                continue;
            if (clazzIdSet.size() == 0) {
                clazzName = clazz.formalizeClazzName();
            }
            clazzIdSet.add(clazzId);
        }
        if (CollectionUtils.isEmpty(clazzIdSet))
            return null;
        String content = "";
        if (clazzIdSet.size() > 1) {
            content = clazzName + "等" + clazzIdSet.size() + "个班";
        }
        if (clazzIdSet.size() == 1) {
            content = clazzName;
        }
        content = content + "人数低于5人";

        FreshManTask task = new FreshManTask();
        task.setTitle("通知学生加入班级");
        task.setContent(content);
        task.setLinkUrl(fetchMainsiteUrlByCurrentSchema() + "/view/mobile/teacher/invite_student");
        return task;
    }

    /**
     * 生成退出班级提醒文案
     *
     * @return
     */
    private String generateExitClazzContent(GroupClazzMapper exitClazz) {
        String template = "你已经退出了{0}（{1}），{2}名学生还没有找到新{3}老师！";
        return MessageFormat.format(template, exitClazz.getClazzName(), exitClazz.getGroupSubject().getValue(), exitClazz.getStudentCount(), exitClazz.getGroupSubject().getValue());
    }

    /**
     * 根据申请类型生成申请文案
     *
     * @param app
     * @return
     */
    private String generateApplicationContent(TeacherApplicationMapper app) {
        String templateLink = "{0}老师{1}申请和你一起教{2}的学生";
        String templateReplace = "{0}老师{1}申请代替你在{2}教英语";
        String templateTransfer = "{0}老师{1}请你在{2}担任{3}老师";

        String content = "";
        switch (app.getType()) {
            case "LINK":
                content = MessageFormat.format(templateLink, app.getApplicantSubject().getValue(), app.getApplicantName(), app.getClazzName());
                break;
            case "TRANSFER":
                content = MessageFormat.format(templateTransfer, app.getApplicantSubject().getValue(), app.getApplicantName(), app.getClazzName(), app.getApplicantSubject().getValue());
                break;
            case "REPLACE":
                content = MessageFormat.format(templateReplace, app.getApplicantSubject().getValue(), app.getApplicantName(), app.getClazzName());
                break;
        }
        return content;
    }

    /**
     * 根据申请生成确认文案
     *
     * @param app
     * @return
     */
    private String generateApplicationConfirmText(TeacherApplicationMapper app) {
        String templateReplace = "允许后，你将不再担任该班{0}老师。确定？";
        String templateTransfer = "允许后，你将在这个班担任{0}老师。确定？";

        String content = "";
        switch (app.getType()) {
            case "LINK":
                content = "允许后，你们将一起教这个班。确定？";
                break;
            case "TRANSFER":
                content = MessageFormat.format(templateTransfer, app.getApplicantSubject().getValue());
                break;
            case "REPLACE":
                content = MessageFormat.format(templateReplace, app.getRespondentSubject().getValue());
                break;
        }
        return content;
    }

    /**
     * 给申请发送者发送消息提醒 同时发app消息和jpush
     *
     * @param message              申请消息
     * @param messageBuilder       发送消息生成器
     * @param appendCheckDetailBtn 是否在消息提醒中添加‘查看详情’按钮
     * @param needPopup            是否弹窗
     */
    private void sendApplicationMessageToRespondent(MapMessage message,
                                                    ApplicationMessageBuilder messageBuilder,
                                                    boolean appendCheckDetailBtn,
                                                    boolean needPopup) {
        try {
            Teacher applicant = (Teacher) message.get("applicant");
            Teacher respondent = (Teacher) message.get("respondent");
            Clazz clazz = (Clazz) message.get("clazz");
            String sendMsg = messageBuilder.buildMessage(applicant, respondent, clazz);

            doSendApplicationMessage(respondent, sendMsg, appendCheckDetailBtn, needPopup);

            Map<Long, List<UserWechatRef>> tid_refs_map = wechatLoaderClient.loadUserWechatRefs(Collections.singleton(respondent.getId()));
            List<UserWechatRef> refs = tid_refs_map.get(respondent.getId());
            if (CollectionUtils.isNotEmpty(refs)) {
                //发送微信模版消息，如果applicant绑定了微信的话
                doSendApplicationMessageByWechat(applicant, respondent);
            } else {
                //否则，发送短信提醒消息
                doSendApplicationMessageBySMS(applicant, respondent);
            }
        } catch (Exception ex) {
            // 申请其实已经成功了。。。记下log就好了吧
            log.error("Send application succeed but send message failed.", ex.getMessage(), ex);
        }
    }

    //发送微信模版消息通知
    private void doSendApplicationMessageByWechat(Teacher applicant, Teacher respondent) {
        // 查询微信
        Map<String, Object> extensionInfo = MiscUtils.m("applicantId", applicant.getId(),
                "applicantName", applicant.fetchRealname(),
                "respondentId", respondent.getId(),
                "respondentName", respondent.fetchRealname());
        wechatServiceClient.processWechatNotice(WechatNoticeProcessorType.TeacherClazzAlterationNotice,
                respondent.getId(), extensionInfo, WechatType.TEACHER);
    }

    //发送短信通知
    private void doSendApplicationMessageBySMS(Teacher applicant, Teacher respondent) {
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(respondent.getId());
        if (ua.isMobileAuthenticated()) {
            // 每个手机号，每天最多收到3个类似的班级调整的短信
            String phone = sensitiveUserDataServiceClient.loadUserMobile(ua.getId());
            if (smsServiceClient.getSmsService().canSendClazzManagement(phone).getUninterruptibly()) {
                String content = "您收到" + applicant.fetchRealname() + "老师的班级请求，点击链接立刻处理，" + "http://www.17zyw.cn/ABvANn";
                userSmsServiceClient.buildSms().to(ua)
                        .content(content)
                        .type(SmsType.CLAZZ_ALTERATION_NOTIFY)
                        .send();
            }
        }
    }

    private void doSendApplicationMessage(Teacher user,
                                          String message,
                                          boolean appendCheckDetailBtn,
                                          boolean needPopup) {
        // 需要加链接的
        if (appendCheckDetailBtn) {
            message = StringUtils.formatMessage(
                    message + " <a href=\"{}\" class=\"w-blue\" target=\"_blank\">【查看详情】</a>",
                    "/teacher/systemclazz/clazzindex.vpage"
            );
            // 发送站内信  带链接
            sendMessage(user, message);
        } else {
            // 发送站内信  不带链接
            sendMessage(user, message);
        }
        // 发右下角弹窗
        if (needPopup) {
            userPopupServiceClient.createPopup(user.getId())
                    .content(message)
                    .type(PopupType.TEACHER_ALTERATION_FOR_RESPONDENT)
                    .category(PopupCategory.LOWER_RIGHT)
                    .create();
        }
    }

    private void sendMessage(User receiver, String payload) {
        if (StringUtils.isBlank(payload)) {
            return;
        }
        payload = StringUtils.replace(payload, "老师老师", "老师");
        teacherLoaderClient.sendTeacherMessage(receiver.getId(), payload);
    }

    /**
     * 发送申请
     *
     * @param applicantId  申请人ID
     * @param clazzId      班级ID
     * @param respondentId 被申请人ID
     * @param type         申请类型
     * @param errMsg       出错信息
     * @return 成功返回map，包含以下基本信息：
     * {
     * "recordId": 申请记录ID
     * "applicant": 申请人信息
     * "respondent": 被申请人信息
     * "clazz": 班级信息
     * }
     * 失败返回error message
     */
    private MapMessage sendApplication(long applicantId,
                                       long clazzId,
                                       long respondentId,
                                       ClazzTeacherAlterationType type,
                                       String errMsg) {
        MapMessage msg = teacherAlterationServiceClient.sendApplication(applicantId, respondentId, clazzId, type, OperationSourceType
                .app);
        if (!msg.isSuccess() && !StringUtils.equals(msg.getInfo(), TeacherAlterationService.DUP_OPERATION_ERR_MSG)
                && !StringUtils.equals(msg.getInfo(), TeacherAlterationService.NO_STUDENT_GROUP_ERR_MSG)
                && !StringUtils.equals(msg.getInfo(), TeacherAlterationService.UNUSUAL_APPLICATIONS_ERR_MSG)) {
            msg = MapMessage.errorMessage(errMsg);
        }
        return msg;
    }

    /**
     * 给申请接受者发送消息提醒
     */
    private void sendApplicationMessageToApplicant(MapMessage message,
                                                   ApplicationMessageBuilder messageBuilder,
                                                   boolean appendCheckDetailBtn,
                                                   boolean needPopup) {
        try {
            Teacher applicant = (Teacher) message.get("applicant");
            Teacher respondent = (Teacher) message.get("respondent");
            Clazz clazz = (Clazz) message.get("clazz");
            String sendMsg = messageBuilder.buildMessage(applicant, respondent, clazz);
            //发送站内信
            doSendApplicationMessage(applicant, sendMsg, appendCheckDetailBtn, needPopup);
            //发送微信消息通知
            Map<String, Object> extensionInfo = MiscUtils.m("first", "班级申请通知",
                    "keyword1", "一起作业",
                    "keyword2", "您向" + respondent.fetchRealname() + "老师发出的班级请求已有结果",
                    "url", ProductConfig.get("wechat.url") + "/teacher/message/list.vpage?_from=wechatnotice");
            Map<Long, List<UserWechatRef>> userWechatRefs = wechatLoaderClient.loadUserWechatRefs(Collections.singleton(applicant.getId()), WechatType.TEACHER);
            List<UserWechatRef> refs = userWechatRefs.get(applicant.getId());
            if (CollectionUtils.isNotEmpty(refs)) {
                wechatServiceClient.processWechatNotice(
                        WechatNoticeProcessorType.TeacherOperationNotice, applicant.getId(), extensionInfo, WechatType.TEACHER);
            }
        } catch (Exception ex) {
            // 申请其实已经成功了。。。记下log就好了吧
            log.error("Send application succeed but send message failed.", ex.getMessage(), ex);
        }
    }

    /**
     * 拒绝申请
     *
     * @param respondentId 被申请人ID
     * @param recordId     申请ID
     * @param type         申请类型
     * @return 成功返回map，包含以下基本信息：
     * {
     * "recordId": 申请记录ID
     * "applicant": 申请人信息
     * "respondent": 被申请人信息
     * "clazz": 班级信息
     * }
     * 失败返回error message
     */
    private MapMessage rejectApplication(long respondentId,
                                         long recordId,
                                         ClazzTeacherAlterationType type) {
        MapMessage msg = teacherAlterationServiceClient.rejectApplication(respondentId, recordId, type, OperationSourceType.app);
        if (!msg.isSuccess() && !Objects.equals(msg.getInfo(), TeacherAlterationService.DUP_OPERATION_ERR_MSG)) {
            msg = MapMessage.errorMessage("拒绝申请失败");
        }
        return msg;
    }

    /**
     * 发班级申请时,给对方发送app消息,同时推送jpush.
     * 发link replace 以及transfer类申请.
     *
     * @param message
     * @param applicationMessageBuilder
     * @return
     */
    private void sendAppMessageAndJpushForApplication(MapMessage message, ApplicationMessageBuilder applicationMessageBuilder) {

        Teacher applicant = (Teacher) message.get("applicant");
        Teacher respondent = (Teacher) message.get("respondent");
        Clazz clazz = (Clazz) message.get("clazz");
        Long recordId = (Long) message.get("recordId");

        //TODO 新前端域名处理
        String messageContent = applicationMessageBuilder.buildMessage(applicant, respondent, clazz);

        String messageUrl = TeacherMessageType.getApplicationUrlTemplate() + recordId;
        AppMessage appMessage = new AppMessage();
        appMessage.setUserId(respondent.getId());
        appMessage.setLinkUrl(messageUrl);
        appMessage.setLinkType(1);
        appMessage.setMessageType(TeacherMessageType.APPLICATION.getType());
        appMessage.setContent(messageContent);
        appMessage.setTitle(TeacherMessageType.APPLICATION.getDescription());
        appMessage.setCreateTime(new Date().getTime());

        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(respondent.getId());
        if (mainTeacherId != null && mainTeacherId > 0L) {
            appMessage.setUserId(mainTeacherId);
        }

        messageCommandServiceClient.getMessageCommandService().createAppMessage(appMessage);

        List<Long> userIdList = new ArrayList<>();
        userIdList.add(respondent.getId());
        String key = "";
        if (Ktwelve.JUNIOR_SCHOOL.equals(respondent.getKtwelve()))
            key = "m";
        if (Ktwelve.PRIMARY_SCHOOL.equals(respondent.getKtwelve()))
            key = "j";
        if (Ktwelve.INFANT.equals(respondent.getKtwelve()))
            key = "i";
        Map<String, Object> extroInfo = MiscUtils.m("s", TeacherMessageType.APPLICATION.getType(), "key", key, "link", fetchMainsiteUrlByCurrentSchema() + messageUrl, "t", "h5");
        appMessageServiceClient.sendAppJpushMessageByIds(messageContent, AppMessageSource.JUNIOR_TEACHER, userIdList, extroInfo);

    }

    /**
     * 处理班级请求时,给申请者发app消息,不发jpush
     *
     * @param message
     * @param applicationMessageBuilder
     * @return
     */
    private void sendAppMessageForDealApplication(MapMessage message, ApplicationMessageBuilder applicationMessageBuilder) {

        Teacher applicant = (Teacher) message.get("applicant");
        Teacher respondent = (Teacher) message.get("respondent");
        Clazz clazz = (Clazz) message.get("clazz");

        String messageContent = applicationMessageBuilder.buildMessage(applicant, respondent, clazz);

        //TODO 新前端域名处理
        AppMessage appMessage = new AppMessage();
        appMessage.setUserId(applicant.getId());
        appMessage.setMessageType(TeacherMessageType.CLAZZNEWS.getType());
        appMessage.setContent(messageContent);
        appMessage.setTitle(TeacherMessageType.CLAZZNEWS.getDescription());

        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(applicant.getId());
        if (mainTeacherId != null && mainTeacherId > 0L) {
            appMessage.setUserId(mainTeacherId);
        }

        messageCommandServiceClient.getMessageCommandService().createAppMessage(appMessage);
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
     * 处理老师请求加入有学生资源的老师
     * 注意，如果老师需要加入班级，调用的是强行加入班级，从而可以跳过班级数量检查
     * 见redmine 28964
     *
     * @param curTeacher
     * @param clazzId
     * @param teacherIds
     * @return
     */
    private MapMessage handleTeacherRequestStudentResource(Teacher curTeacher, Long clazzId, List<Long> teacherIds) {
        Map<Long, Teacher> teachers = teacherLoaderClient.loadTeachers(teacherIds);
        Set<Subject> subjectSet = teachers.values().stream().map(Teacher::getSubject).collect(Collectors.toSet());

        // 如果teacherIds中包含curTeacher.getId()，表示老师想重新加回到组中(退班不退组)，把该班级id直接返回回去，让后面的逻辑处理
        if (teacherIds.contains(curTeacher.getId())) return MapMessage.successMessage();

        // 当无同科老师时，认证老师可以直接关联
        if (curTeacher.fetchCertificationState() == AuthenticationState.SUCCESS) {
            Teacher applicant = curTeacher;
            Teacher respondent = teachers.values().stream().filter(t -> t.getSubject() != applicant.getSubject()).findFirst().orElse(null);
            if (respondent != null) {
                // 处理老师当前的关联状况
                MapMessage result = clazzServiceClient.handleTeacherLinkOperation(applicant, respondent, clazzId, OperationSourceType.app);
                if (!result.isSuccess()) {
                    return result;
                }

                Map<Teacher, Teacher> replaceTeachers = (Map<Teacher, Teacher>) result.remove("replaceTeachers");
                Map<Teacher, Teacher> linkTeachers = (Map<Teacher, Teacher>) result.remove("linkTeachers");

                if (MapUtils.isNotEmpty(replaceTeachers) || MapUtils.isNotEmpty(linkTeachers)) {
                    boolean needSendApp = SafeConverter.toBoolean(result.remove("needSendApp"));
                    if (!needSendApp) {// 不需要发送请求的状况，直接接管
                        List<Long> addClazzTeacherIds = (List<Long>) result.remove("addClazzTeacherIds");
                        for (Long teacherId : addClazzTeacherIds) {
                            MapMessage message = clazzServiceClient.teacherJoinSystemClazzForce(teacherId, clazzId);
                            if (!message.isSuccess()) {
                                return message;
                            }
                        }
                        MapMessage m = MapMessage.successMessage();
                        for (Map.Entry<Teacher, Teacher> entry : replaceTeachers.entrySet()) {
                            Teacher fromT = entry.getKey();
                            Teacher toT = entry.getValue();
                            m = groupServiceClient.replaceTeacherGroupForReplace(fromT.getId(), toT.getId(), clazzId, toT.getId().toString(), UserOperatorType.TEACHER);
                        }
                        for (Map.Entry<Teacher, Teacher> entry : linkTeachers.entrySet()) {
                            Teacher fromT = entry.getKey();
                            Teacher toT = entry.getValue();
                            m = groupServiceClient.shareTeacherGroup(fromT.getId(),
                                    toT.getId(), fromT.getSubject(), toT.getSubject(), clazzId, curTeacher.getId().toString(), UserOperatorType.TEACHER);
                        }
                        if (m.isSuccess()) {// 已加入班级，不需要发送请求
                            return MapMessage.successMessage().add("join", true);
                        }
                    }
                }
            }

        }

        for (Teacher teacher : teachers.values()) {
            if (teacher.getSubject().equals(curTeacher.getSubject())) {// 同学科，进行接管申请
                MapMessage message = sendApplication(curTeacher.getId(), clazzId, teacher.getId(), ClazzTeacherAlterationType.REPLACE,
                        "向班级任课教师申请接管学生资源失败");
                // 发送消息通知及弹窗
                if (message.isSuccess() && message.containsKey("recordId")) {// 表示发送了申请
                    ApplicationMessageBuilder messageBuilder = new ApplicationMessageBuilder() {
                        @Override
                        String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                            return StringUtils.formatMessage("{}老师申请接管您任课的{}的学生资源",
                                    applicant.getProfile().getRealname(),
                                    clazz.formalizeClazzName());
                        }
                    };
                    sendAppMessageAndJpushForApplication(message, messageBuilder);
                    sendApplicationMessageToRespondent(message, messageBuilder, true, true);
                    return message;
                }
            } else {// 不同学科，判断是否有同学科的老师，没有发送关联申请
                // 或者老师为认证老师，则发送关联请求，一旦关联，之前的关联老师被接管
                if (teacher.getAuthenticationState() == SUCCESS.getState()
                        || !subjectSet.contains(curTeacher.getSubject())) {
                    MapMessage message = sendApplication(curTeacher.getId(), clazzId, teacher.getId(),
                            ClazzTeacherAlterationType.LINK, "向班级任课教师申请关联学生资源失败");

                    // 发送消息通知及弹窗
                    if (message.isSuccess() && message.containsKey("recordId")) {// 表示发送了申请
                        ApplicationMessageBuilder messageBuilder = new ApplicationMessageBuilder() {
                            @Override
                            String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                                return StringUtils.formatMessage("{}老师申请关联您任课的{}的学生资源",
                                        applicant.getProfile().getRealname(),
                                        clazz.formalizeClazzName());
                            }
                        };
                        sendAppMessageAndJpushForApplication(message, messageBuilder);
                        sendApplicationMessageToRespondent(message, messageBuilder, true, true);
                    } else {
                        return message;
                    }
                }
            }
        }
        return MapMessage.successMessage();
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

    /**
     * 新手任务model
     */
    @Data
    class FreshManTask {
        private String title;
        private String content;
        private String linkUrl;
        private String label;
        private Integer order;
        private String imgUrl;
    }

    /**
     * 申请消息生成器
     *
     * @author changyuan.liu
     */
    private abstract class ApplicationMessageBuilder {
        /**
         * 生成申请消息
         *
         * @param applicant  申请人
         * @param respondent 被申请人
         * @param clazz      班级
         * @return 申请消息
         */
        abstract String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz);
    }

}
