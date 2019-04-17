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

package com.voxlearning.utopia.service.business.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFutureMap;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.business.api.MiscLoader;
import com.voxlearning.utopia.entity.activity.InterestingReport;
import com.voxlearning.utopia.entity.misc.*;
import com.voxlearning.utopia.mapper.UgcQuestionMapper;
import com.voxlearning.utopia.mapper.UgcRecordMapper;
import com.voxlearning.utopia.service.business.base.AbstractMiscLoader;
import com.voxlearning.utopia.service.business.impl.dao.InterestingReportDao;
import com.voxlearning.utopia.service.finance.client.IntegralActivityRuleServiceClient;
import com.voxlearning.utopia.service.finance.client.IntegralActivityServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.ugc.client.UgcServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserAggregationLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = MiscLoader.class)
@ExposeService(interfaceClass = MiscLoader.class)
public class MiscLoaderImpl extends AbstractMiscLoader {

    @Inject private RaikouSystem raikouSystem;

    @Inject private AsyncUserServiceClient asyncUserServiceClient;
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private IntegralActivityRuleServiceClient integralActivityRuleServiceClient;
    @Inject private IntegralActivityServiceClient integralActivityServiceClient;
    @Inject private InterestingReportDao interestingReportDao;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UgcServiceClient ugcServiceClient;
    @Inject private UserAggregationLoaderClient userAggregationLoaderClient;

    // ========================================================================
    // 积分活动相关的方法 By Wyc 2016-01-15
    // ========================================================================
    @Override
    @Deprecated
    public List<IntegralActivity> loadAllIntegralActivities() {
        return integralActivityServiceClient.getIntegralActivityService()
                .loadAllIntegralActivitiesFromDB()
                .getUninterruptibly()
                .stream()
                .filter(e -> !e.isDisabledTrue())
                .sorted((o1, o2) -> {
                    long u1 = o1.fetchUpdateTimestamp();
                    long u2 = o2.fetchUpdateTimestamp();
                    return Long.compare(u2, u1);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Deprecated
    public Page<IntegralActivity> loadIntegralActivityPage(Pageable pageable, Integer department, Integer status) {
        List<IntegralActivity> candidate = loadAllIntegralActivities().stream()
                .filter(e -> {
                    int d = SafeConverter.toInt(department);
                    return d == 0 || Objects.equals(d, e.getDepartment());
                })
                .filter(e -> {
                    int s = SafeConverter.toInt(status);
                    return s == 0 || Objects.equals(s, e.getStatus());
                })
                .collect(Collectors.toList());
        return PageableUtils.listToPage(candidate, pageable);
    }

    @Override
    @Deprecated
    public IntegralActivity loadIntegralActivityById(Long activityId) {
        return integralActivityServiceClient.getIntegralActivityService()
                .loadIntegralActivityFromDB(activityId)
                .getUninterruptibly();
    }

    @Override
    @Deprecated
    public Map<String, Object> loadIntegralActivityDetail(Long activityId) {
        Map<String, Object> activityDetails = new HashMap<>();
        IntegralActivity activity = loadIntegralActivityById(activityId);
        List<IntegralActivityRule> activityRules = integralActivityRuleServiceClient.getIntegralActivityRuleBuffer()
                .loadAllEnabled()
                .stream()
                .filter(e -> Objects.equals(activityId, e.getActivityId()))
                .collect(Collectors.toList());
        activityDetails.put("activity", activity);
        activityDetails.put("rules", activityRules);
        return activityDetails;
    }

    @Override
    public boolean checkIntegralType(Long ruleId, Integer type) {
        // Criteria criteria = Criteria.where("DISABLED").is(false).and("ID").ne(ruleId);
        Set<Integer> typeSet = integralActivityRuleServiceClient.getIntegralActivityRuleBuffer()
                .loadAllEnabled()
                .stream()
                .filter(e -> !Objects.equals(ruleId, e.getId()))
                .map(IntegralActivityRule::getIntegralType)
                .distinct()
                .collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(typeSet)) {
            return typeSet.contains(type);
        } else return false;
    }


    /**
     * 新的UGC支持同一个用户同时有多个有效的UGC收集活动 在所有没有回答的活动中，随机取一个返回
     *
     * @param user
     * @return
     */
    @Override
    public UgcRecordMapper loadEnableUserUgcRecord(User user) {
        List<UgcRecord> records = ugcServiceClient.getUgcService()
                .findUgcRecordsByUserType(user.fetchUserType())
                .getUninterruptibly();
        if (CollectionUtils.isEmpty(records)) {
            return null;
        }

        // 加入status状态过滤, 方便测试
        if (RuntimeMode.isProduction()) {
            records = records.stream().filter(UgcRecord::getPublished).collect(Collectors.toList());
        }

        // 先过滤出所有有效的活动
        Date now = new Date();
        records = records.stream().filter(r -> r.getStartDate() != null && r.getStartDate().before(now)
                && r.getEndDate() != null && r.getEndDate().after(now))
                .filter(r -> !r.getDisabled()).collect(Collectors.toList());

        if (user.isTeacher()) {
            TeacherDetail detail = teacherLoaderClient.loadTeacherDetail(user.getId());
            if (!detail.isSchoolAmbassador()) {
                records = records.stream().filter(r -> !SafeConverter.toBoolean(r.getAmbassadorOnly())).collect(Collectors.toList());
            }
        }

        if (CollectionUtils.isEmpty(records)) {
            return null;
        }

        List<UgcRecordMapper> mappers = new ArrayList<>();
        List<UgcRecord> validRecords = new ArrayList<>();
        // 过滤UGC类型
        for (UgcRecord record : records) {
            switch (record.getCodeType()) {
                case ALL:
                    // 全部 不做处理
                    validRecords.add(record);
                    break;
                case REGION:
                    // 过滤用户学校REGION
                    School school = asyncUserServiceClient.getAsyncUserService()
                            .loadUserSchool(user)
                            .getUninterruptibly();
                    if (school == null || school.getRegionCode() == null) {
                        break;
                    }

                    ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
                    if (region == null) {
                        break;
                    }

                    List<UgcRecordCodeRef> regionRefs = ugcServiceClient.getUgcService()
                            .findUgcRecordCodeRefsByRecordId(record.getId())
                            .getUninterruptibly();
                    if (CollectionUtils.isEmpty(regionRefs)) {
                        break;
                    }

                    List<Integer> regionList = regionRefs.stream().map(ref -> SafeConverter.toInt(ref.getCode()))
                            .collect(Collectors.toList());
                    if (regionList.contains(region.getCountyCode())
                            || regionList.contains(region.getCityCode())
                            || regionList.contains(region.getProvinceCode())) {
                        validRecords.add(record);
                    }
                    break;
                case SCHOOL:
                    // 过滤用户学校
                    School userSchool = asyncUserServiceClient.getAsyncUserService()
                            .loadUserSchool(user)
                            .getUninterruptibly();
                    if (userSchool == null) {
                        break;
                    }

                    List<UgcRecordCodeRef> schoolRefs = ugcServiceClient.getUgcService()
                            .findUgcRecordCodeRefsByRecordId(record.getId())
                            .getUninterruptibly();
                    if (CollectionUtils.isEmpty(schoolRefs)) {
                        break;
                    }

                    List<Long> schoolList = schoolRefs.stream().map(ref -> SafeConverter.toLong(ref.getCode()))
                            .collect(Collectors.toList());
                    if (schoolList.contains(userSchool.getId())) {
                        validRecords.add(record);
                    }

                    break;
                case USER:
                    // 暂时不支持此类型
                    records.remove(record);
                    break;
                case GROUP:
                    if (user.fetchUserType() == UserType.STUDENT) {
                        List<UgcRecordCodeRef> ugcRecordCodeRefList = ugcServiceClient.getUgcService()
                                .findUgcRecordCodeRefsByRecordId(record.getId())
                                .getUninterruptibly();
                        if (CollectionUtils.isNotEmpty(ugcRecordCodeRefList)) {
                            List<Long> groupIds = ugcRecordCodeRefList.stream().map(UgcRecordCodeRef::getCode).map(SafeConverter::toLong).collect(Collectors.toList());
                            Map<Long, List<GroupStudentTuple>> groupStudentRefs = raikouSystem.findGroupStudentRefs(groupIds).asGroupGroup();
                            if (MapUtils.isNotEmpty(groupStudentRefs)) {
                                Set<Long> studentIds = new HashSet<>();
                                groupStudentRefs.values().forEach(groupStudentRefsList -> {
                                    Set<Long> tempStudentIds = groupStudentRefsList.stream()
                                            .map(GroupStudentTuple::getStudentId)
                                            .collect(Collectors.toSet());
                                    studentIds.addAll(tempStudentIds);
                                });
                                if (studentIds.contains(user.getId())) {
                                    validRecords.add(record);
                                }
                            }
                        }
                    }
                    break;
                default:
            }
        }
        if (CollectionUtils.isEmpty(validRecords)) {
            return null;
        }

        for (UgcRecord record : validRecords) {
            // 这里对所有活动做出过滤， 条件：用户是否回答了全部问题
            List<UgcRecordQuestionsRef> questionsRefs = ugcServiceClient.getUgcService()
                    .findUgcRecordQuestionsRefsByRecordId(record.getId())
                    .getUninterruptibly();
            if (CollectionUtils.isNotEmpty(questionsRefs)) {
                // 本次活动勾选所有题目ID
                List<Long> unAnswerQuestionIds = questionsRefs.stream().map(ref -> SafeConverter.toLong(ref.getQuestionId())).collect(Collectors.toList());
                // 用户本次活动全部答案
                List<UgcAnswers> answers = ugcServiceClient.getUgcService()
                        .findUgcAnswersByRecordIdAndUserId(record.getId(), user.getId())
                        .getUninterruptibly();
                if (CollectionUtils.isNotEmpty(answers)) {
                    List<Long> answerIds = answers.stream().map(UgcAnswers::getQuestionId).collect(Collectors.toList());
                    unAnswerQuestionIds = unAnswerQuestionIds.stream().filter(a -> !answerIds.contains(a)).collect(Collectors.toList());
                }
                if (CollectionUtils.isNotEmpty(unAnswerQuestionIds)) {
                    UgcRecordMapper mapper = new UgcRecordMapper();
                    mapper.setRecordId(record.getId());
                    mapper.setRecordName(record.getName());
                    AlpsFutureMap<Long, UgcQuestions> futureMap = new AlpsFutureMap<>();
                    for (Long id : unAnswerQuestionIds) {
                        futureMap.put(id, ugcServiceClient.getUgcService().loadUgcQuestions(id));
                    }
                    Collection<UgcQuestions> questions = futureMap.regularize().values();
                    List<UgcQuestionMapper> questionMappers = new ArrayList<>();
                    for (UgcQuestions question : questions) {
                        UgcQuestionMapper qm = question.toMapper();
                        String questionName = ugcQuestionReplaceKeyWord(qm.getQuestionName(), user);
                        if (StringUtils.isNotBlank(questionName)) {
                            qm.setQuestionName(questionName);
                            questionMappers.add(qm);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(questionMappers)) {
                        mapper.setUnAnswerQuestions(questionMappers);
                        mappers.add(mapper);
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(mappers)) {
            return null;
        }
        // 随机取一个
        Collections.shuffle(mappers);
        return MiscUtils.firstElement(mappers);
    }

    @Override
    public UgcRecordMapper loadEnableUserUgcRecordByRecordId(User user, Long recordId) {
        if (recordId == null) {
            return null;
        }
        UgcRecord record = ugcServiceClient.getUgcService().loadUgcRecord(recordId).getUninterruptibly();
        if (record == null) {
            return null;
        }
        // 角色过滤
        if (user.fetchUserType() != record.getUserType()) {
            return null;
        }
        // 过滤学生是否毕业班
        if (user.fetchUserType() == UserType.STUDENT) {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(user.getId());
            if (clazz == null || clazz.isTerminalClazz()) {
                return null;
            }
        }
        // 先过滤出所有有效的活动
        Date now = new Date();
        if (record.getStartDate() == null || record.getEndDate() == null || record.getStartDate().after(now)
                || record.getEndDate().before(now) || record.getDisabled()) {
            return null;
        }
        switch (record.getCodeType()) {
            case ALL:
                // 全部 不做处理
                break;
            case REGION:
                // 过滤用户学校REGION
                School school = asyncUserServiceClient.getAsyncUserService()
                        .loadUserSchool(user)
                        .getUninterruptibly();
                List<UgcRecordCodeRef> regionRefs = ugcServiceClient.getUgcService()
                        .findUgcRecordCodeRefsByRecordId(record.getId())
                        .getUninterruptibly();
                if (CollectionUtils.isEmpty(regionRefs)) {
                    return null;
                } else {
                    List<Integer> codesList = regionRefs.stream()
                            .map(ref -> SafeConverter.toInt(ref.getCode()))
                            .collect(Collectors.toList());
                    if (!codesList.contains(school.getRegionCode())) {
                        return null;
                    }
                }
                break;
            case SCHOOL:
                // 过滤用户学校
                School userSchool = asyncUserServiceClient.getAsyncUserService()
                        .loadUserSchool(user)
                        .getUninterruptibly();
                List<UgcRecordCodeRef> schoolRefs = ugcServiceClient.getUgcService()
                        .findUgcRecordCodeRefsByRecordId(record.getId())
                        .getUninterruptibly();
                if (CollectionUtils.isEmpty(schoolRefs)) {
                    return null;
                } else {
                    List<Long> codesList = schoolRefs.stream()
                            .map(ref -> SafeConverter.toLong(ref.getCode()))
                            .collect(Collectors.toList());
                    if (!codesList.contains(userSchool.getId())) {
                        return null;
                    }
                }
                break;
            case USER:
                // 暂时不支持此类型
                return null;
            default:
        }
        // 这里活动做出过滤， 条件：用户是否回答了全部问题
        List<UgcRecordQuestionsRef> questionsRefs = ugcServiceClient.getUgcService()
                .findUgcRecordQuestionsRefsByRecordId(record.getId())
                .getUninterruptibly();
        if (CollectionUtils.isNotEmpty(questionsRefs)) {
            // 本次活动勾选所有题目ID
            List<Long> unAnswerQuestionIds = questionsRefs.stream().map(ref -> SafeConverter.toLong(ref.getQuestionId())).collect(Collectors.toList());
            // 用户本次活动全部答案
            List<UgcAnswers> answers = ugcServiceClient.getUgcService()
                    .findUgcAnswersByRecordIdAndUserId(record.getId(), user.getId())
                    .getUninterruptibly();
            if (CollectionUtils.isNotEmpty(answers)) {
                List<Long> answerIds = answers.stream().map(UgcAnswers::getQuestionId).collect(Collectors.toList());
                unAnswerQuestionIds = unAnswerQuestionIds.stream().filter(a -> !answerIds.contains(a)).collect(Collectors.toList());
            }
            if (CollectionUtils.isNotEmpty(unAnswerQuestionIds)) {
                UgcRecordMapper mapper = new UgcRecordMapper();
                mapper.setUserName(user.fetchRealname());
                mapper.setUserId(user.getId());
                mapper.setRecordId(record.getId());
                mapper.setRecordName(record.getName());
                AlpsFutureMap<Long, UgcQuestions> futureMap = new AlpsFutureMap<>();
                for (Long id : unAnswerQuestionIds) {
                    futureMap.put(id, ugcServiceClient.getUgcService().loadUgcQuestions(id));
                }
                Collection<UgcQuestions> questions = futureMap.regularize().values();
                List<UgcQuestionMapper> questionMappers = new ArrayList<>();
                for (UgcQuestions question : questions) {
                    UgcQuestionMapper qm = question.toMapper();
                    String questionName = ugcQuestionReplaceKeyWord(qm.getQuestionName(), user);
                    if (StringUtils.isNotBlank(questionName)) {
                        qm.setQuestionName(questionName);
                        questionMappers.add(qm);
                    }
                }
                if (CollectionUtils.isEmpty(questionMappers)) {
                    return null;
                }
                mapper.setUnAnswerQuestions(questionMappers);
                return mapper;
            }
        }
        return null;
    }

    private String ugcQuestionReplaceKeyWord(String questionName, User user) {
        // 目前关键字只有学生用户班级
        if (user.fetchUserType() == UserType.STUDENT) {
            String key = "#CLAZZLEVEL#";
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(user.getId());
            questionName = StringUtils.replace(questionName, key, clazz == null ? "" : clazz.getClazzLevel().getDescription());
            // #ENAME# 英语老师名字 #MNAME# 数学老师名字  #CNAME# 语文老师名字， 没有的话 本到题不显示， 直接返回空
            key = "#ENAME#";
            List<ClazzTeacher> teachers = userAggregationLoaderClient.loadStudentTeachers(user.getId());
            ClazzTeacher teacher = teachers.stream().filter(ct -> ct.getTeacher() != null && ct.getTeacher().getSubject() != null
                    && ct.getTeacher().getSubject() == Subject.ENGLISH).findAny().orElse(null);
            if (questionName.contains(key) && teacher == null) {
                return null;
            }
            questionName = StringUtils.replace(questionName, key, teacher == null ? "" : teacher.getTeacher().fetchRealname());
            key = "#MNAME#";
            teacher = teachers.stream().filter(ct -> ct.getTeacher() != null && ct.getTeacher().getSubject() != null
                    && ct.getTeacher().getSubject() == Subject.MATH).findAny().orElse(null);
            if (questionName.contains(key) && teacher == null) {
                return null;
            }
            questionName = StringUtils.replace(questionName, key, teacher == null ? "" : teacher.getTeacher().fetchRealname());
            key = "#CNAME#";
            teacher = teachers.stream().filter(ct -> ct.getTeacher() != null && ct.getTeacher().getSubject() != null
                    && ct.getTeacher().getSubject() == Subject.CHINESE).findAny().orElse(null);
            if (questionName.contains(key) && teacher == null) {
                return null;
            }
            questionName = StringUtils.replace(questionName, key, teacher == null ? "" : teacher.getTeacher().fetchRealname());
            return questionName;
        }

        return questionName;
    }

    @Override
    public InterestingReport loadUserInterestingReport(Long userId) {
        return interestingReportDao.load(userId);
    }

}
