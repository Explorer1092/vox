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

package com.voxlearning.utopia.service.newexam.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.DateFormatUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.newexam.api.constant.NewExamConstants;
import com.voxlearning.utopia.service.newexam.api.constant.NewExamPublishMessageType;
import com.voxlearning.utopia.service.newexam.api.constant.NewExamStudentStatus;
import com.voxlearning.utopia.service.newexam.api.context.CorrectNewExamContext;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;
import com.voxlearning.utopia.service.newexam.api.entity.*;
import com.voxlearning.utopia.service.newexam.api.loader.NewExamRegistrationLoader;
import com.voxlearning.utopia.service.newexam.api.mapper.ResetScoreRequestParameter;
import com.voxlearning.utopia.service.newexam.api.mapper.UnitTestPaperInfo;
import com.voxlearning.utopia.service.newexam.api.mapper.report.NewExamThemeForSub;
import com.voxlearning.utopia.service.newexam.api.service.NewExamService;
import com.voxlearning.utopia.service.newexam.impl.dao.StudentExaminationAuthorityDao;
import com.voxlearning.utopia.service.newexam.impl.loader.NewExamResultLoaderImpl;
import com.voxlearning.utopia.service.newexam.impl.pubsub.NewExamPublisher;
import com.voxlearning.utopia.service.newexam.impl.queue.NewExamQueueProducer;
import com.voxlearning.utopia.service.newexam.impl.service.internal.report.NewExamReportProcessor;
import com.voxlearning.utopia.service.newexam.impl.service.internal.student.result.NewExamResultProcessor;
import com.voxlearning.utopia.service.newexam.impl.service.internal.student.work.DoNewExamProcess;
import com.voxlearning.utopia.service.newexam.impl.service.internal.teacher.correction.CorrectNewExamProcessor;
import com.voxlearning.utopia.service.newexam.impl.support.NewExamPaperHelper;
import com.voxlearning.utopia.service.newexam.impl.support.NewExamSpringBean;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.question.api.constant.NewExamStatus;
import com.voxlearning.utopia.service.question.api.constant.NewExamType;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.UserAnswerMapper;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.GroupType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.client.UserIntegralServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.*;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = NewExamService.class)
@ExposeService(interfaceClass = NewExamService.class)
public class NewExamServiceImpl extends NewExamSpringBean implements NewExamService {

    @Inject
    private RaikouSystem raikouSystem;
    @Inject
    private DoNewExamProcess doNewExamProcess;
    @Inject
    private NewExamResultProcessor newExamResultProcessor;
    @Inject
    private NewExamRegistrationLoader newExamRegistrationLoader;
    @Inject
    private CorrectNewExamProcessor correctNewExamProcessor;
    @Inject
    private NewExamReportProcessor newExamReportProcessor;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private StudentExaminationAuthorityDao studentExaminationAuthorityDao;
    @Inject
    private NewExamQueueProducer newExamQueueProducer;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private UserIntegralServiceClient userIntegralServiceClient;
    @Inject
    private NewExamPublisher newExamPublisher;
    @Inject
    private NewExamResultLoaderImpl newExamResultLoader;
    @Inject
    private RaikouSDK raikouSDK;
    @Inject
    private AsyncAvengerNewExamServiceImpl asyncAvengerNewExamService;

    private final static String CACHE_KEY_TEMPLATE = "TeacherIndependentExam_{}_{}";

    @Override
    public List<Map<String, Object>> loadExamsCanBeEntered(StudentDetail studentDetail, School school, ExRegion exRegion, Integer beforeExamStartMinutes) {
        if (studentDetail == null || school == null || exRegion == null) {
            return Collections.emptyList();
        }
        Long schoolId = school.getId();
        int clazzLevel = studentDetail.getClazzLevel().getLevel();
        Date currentDate = new Date();
        List<GroupMapper> groupMappers = groupLoaderClient.loadStudentGroups(Collections.singleton(studentDetail.getId()), false).get(studentDetail.getId());
        List<Long> groupIds = groupMappers.stream().map(GroupMapper::getId).collect(Collectors.toList());
        // 内容库接口获取当前时间能进入的考试
        List<NewExam> canBeEnteredExamList = newExamLoaderClient.loadExamList(null, schoolId, SchoolLevel.safeParse(school.getLevel()), exRegion, clazzLevel, null);

        //可能需要重考补考的考试
        List<NewExam> stopNewExam = new LinkedList<>();
        //正常考试
        List<NewExam> _canBeEnteredExamList = new LinkedList<>();
        for (NewExam newExam : canBeEnteredExamList) {
            if (newExam.getCorrectStopAt() == null || newExam.getExamStartAt() == null || newExam.getExamStopAt() == null) {
                continue;
            }
            //考试已经结束，但是批改时间没有结束
            if (newExam.getExamStopAt().before(currentDate) && newExam.getCorrectStopAt().after(currentDate)) {
                stopNewExam.add(newExam);
            }
            Date beforeNewExamDate = beforeExamStartMinutes != null ? DateUtils.addMinutes(currentDate, beforeExamStartMinutes) : currentDate;
            if (newExam.getExamStartAt().before(beforeNewExamDate) && newExam.getExamStopAt().after(currentDate)) {
                _canBeEnteredExamList.add(newExam);
            }
        }
        //能补考重考的ID
        Map<String, String> registrationIdMap = stopNewExam.stream()
                .collect(Collectors.toMap(NewExam::getId, newExam -> generateNewExamRegistrationId(studentDetail, newExam)));

        //补考重考的权限数据
        Map<String, StudentExaminationAuthority> stringStudentExaminationAuthorityMap = studentExaminationAuthorityDao.loads(registrationIdMap.values())
                .values()
                .stream()
                .filter(o -> !SafeConverter.toBoolean(o.getDisabled()))
                .collect(Collectors.toMap(StudentExaminationAuthority::getId, Function.identity()));
        _canBeEnteredExamList.addAll(
                stopNewExam
                        .stream()
                        .filter(newExam -> stringStudentExaminationAuthorityMap.containsKey(generateNewExamRegistrationId(studentDetail, newExam)))
                        .collect(Collectors.toList()));
        // 添加当前时间能进入的自主考试
        List<NewExam> independentExamList = newExamLoaderClient.loadEnterableIndependentExamList(groupIds, currentDate);
        if (CollectionUtils.isNotEmpty(independentExamList)) {
            _canBeEnteredExamList.addAll(independentExamList);
        }

        if (CollectionUtils.isNotEmpty(_canBeEnteredExamList)) {
            Set<String> registrationIdSet = _canBeEnteredExamList.stream()
                    .map(newExam -> generateNewExamRegistrationId(studentDetail, newExam))
                    .collect(Collectors.toSet());
            Map<String, NewExamRegistration> newExamRegistrationMap = newExamRegistrationLoader.loadByIds(registrationIdSet)
                    .values().stream().filter(nr -> !SafeConverter.toBoolean(nr.getBeenCanceled())).collect(Collectors.toMap(NewExamRegistration::getNewExamId, Function.identity()));
            Set<Subject> studentSubjects = loadStudentSubjects(studentDetail.getId());
            Map<String, String> examIdToPaperIdMap = _canBeEnteredExamList.stream()
                    .collect(Collectors.toMap(NewExam::getId, o -> o.fetchPaperId(studentDetail.getId())));
            Map<String, NewPaper> paperMap = loadNewPapersByDocIdsIncludeDisable(examIdToPaperIdMap.values());

            // 老师报名考试
            Map<String, NewExam> registerNewExamMap = getGroupRegisterNewExamMap(groupIds);

            return _canBeEnteredExamList.stream()
                    // 考试类型不允许为空
                    .filter(newExam -> newExam.getExamType() != null)
                    // 非报名考试||已经报名的报名考试
                    .filter(newExam -> NewExamType.apply != newExam.getExamType()
                            || (SafeConverter.toBoolean(newExam.getDirectional()) && newExamRegistrationMap.containsKey(newExam.getId()))
                            || (!SafeConverter.toBoolean(newExam.getDirectional()) && registerNewExamMap.containsKey(newExam.getId())))
                    .filter(newExam -> newExam.matchStudentSubject(studentSubjects))
                    // 未交卷
                    .filter(newExam -> newExamRegistrationMap.get(newExam.getId()) == null
                            || newExamRegistrationMap.get(newExam.getId()).getSubmitAt() == null)
                    .map(newExam -> {
                        StudentExaminationAuthority studentExaminationAuthority = null;
                        if (registrationIdMap.containsKey(newExam.getId()) && stringStudentExaminationAuthorityMap.containsKey(registrationIdMap.get(newExam.getId()))) {
                            studentExaminationAuthority = stringStudentExaminationAuthorityMap.get(registrationIdMap.get(newExam.getId()));
                        }
                        return convertNewExamToMap(studentExaminationAuthority, newExam, newExamRegistrationMap.get(newExam.getId()), paperMap.get(examIdToPaperIdMap.get(newExam.getId())));
                    })
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> loadExamsCanBeEnteredByStudentId(Long studentId) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail != null && studentDetail.getClazz() != null) {
            Clazz clazz = studentDetail.getClazz();
            School school = schoolLoaderClient.getSchoolLoader()
                    .loadSchool(clazz.getSchoolId())
                    .getUninterruptibly();
            if (school != null && school.getRegionCode() != null) {
                ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
                return loadExamsCanBeEntered(studentDetail, school, exRegion, null);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public MapMessage loadAllExams(StudentDetail studentDetail, School school, ExRegion exRegion) {
        List<Map<String, Object>> exams = Collections.emptyList();
        if (studentDetail == null || school == null || exRegion == null) {
            return MapMessage.errorMessage("获取数据失败");
        }

        List<NewExam> allNewExamList = newExamLoaderClient.loadAllExamList(null, school.getId(), SchoolLevel.safeParse(school.getLevel()), exRegion, studentDetail.getClazzLevel().getLevel());
        // 报名考试
        Set<String> registrationIdSet = allNewExamList.stream()
                .map(newExam -> generateNewExamRegistrationId(studentDetail, newExam))
                .collect(Collectors.toSet());
        Map<String, NewExamRegistration> newExamRegistrationMap = newExamRegistrationLoader.loadByIds(registrationIdSet)
                .values().stream().filter(nr -> !SafeConverter.toBoolean(nr.getBeenCanceled())).collect(Collectors.toMap(NewExamRegistration::getNewExamId, Function.identity()));

        List<NewExam> newExamList = allNewExamList.stream()
                .filter(newExam -> newExam.getExamType().equals(NewExamType.unify))
                .filter(newExam -> newExam.getExamStopAt() != null)
                .collect(Collectors.toList());

        List<GroupMapper> groupMappers = groupLoaderClient.loadStudentGroups(Collections.singleton(studentDetail.getId()), false).get(studentDetail.getId());
        List<Long> groupIds = groupMappers.stream().map(GroupMapper::getId).collect(Collectors.toList());
        // 教师报名考试
        Map<String, NewExam> registerNewExamMap = getGroupRegisterNewExamMap(groupIds);
        List<NewExam> applyNewExamList = allNewExamList.stream()
                .filter(newExam -> newExam.getExamType().equals(NewExamType.apply))
                .filter(newExam -> (SafeConverter.toBoolean(newExam.getDirectional()) && newExamRegistrationMap.containsKey(newExam.getId()))
                        || (!SafeConverter.toBoolean(newExam.getDirectional()) && registerNewExamMap.containsKey(newExam.getId())))
                .collect(Collectors.toList());
        newExamList.addAll(applyNewExamList);

        // 添加获取自主考试
        for (List<NewExam> newExams : newExamLoaderClient.loadByGroupIds(groupIds).values()) {
            newExamList.addAll(newExams);
        }

        if (CollectionUtils.isNotEmpty(newExamList)) {
            Date currentDate = new Date();
            //可能补考重考的试卷
            List<NewExam> stopNewExam = newExamList.stream()
                    .filter(newExam -> newExam.getExamStopAt() != null)
                    //考试结束
                    .filter(newExam -> newExam.getExamStopAt().before(currentDate))
                    .filter(newExam -> newExam.getCorrectStopAt() != null)
                    //批改时间没有结束
                    .filter(newExam -> newExam.getCorrectStopAt().after(currentDate))
                    .collect(Collectors.toList());
            Map<String, String> registrationIdMap = stopNewExam.stream()
                    .collect(Collectors.toMap(NewExam::getId, newExam -> generateNewExamRegistrationId(studentDetail, newExam)));
            Map<String, StudentExaminationAuthority> stringStudentExaminationAuthorityMap = studentExaminationAuthorityDao.loads(registrationIdMap.values())
                    .values()
                    .stream()
                    .filter(o -> !SafeConverter.toBoolean(o.getDisabled()))
                    .collect(Collectors.toMap(StudentExaminationAuthority::getId, Function.identity()));

            Map<String, String> examIdToPaperIdMap = newExamList.stream()
                    .collect(Collectors.toMap(NewExam::getId, o -> o.fetchPaperId(studentDetail.getId())));
            Map<String, NewPaper> paperMap = loadNewPapersByDocIdsIncludeDisable(examIdToPaperIdMap.values());

            exams = newExamList.stream()
                    .sorted(new NewExam.NewExamComparator())
                    .map(newExam -> {
                        StudentExaminationAuthority studentExaminationAuthority = null;
                        if (registrationIdMap.containsKey(newExam.getId()) && stringStudentExaminationAuthorityMap.containsKey(registrationIdMap.get(newExam.getId()))) {
                            studentExaminationAuthority = stringStudentExaminationAuthorityMap.get(registrationIdMap.get(newExam.getId()));
                        }
                        return convertNewExamToMap(studentExaminationAuthority, newExam, newExamRegistrationMap.get(newExam.getId()), paperMap.get(examIdToPaperIdMap.get(newExam.getId())));
                    })
                    .collect(Collectors.toList());
        }
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.put("newExamList", exams);
        return mapMessage;
    }

    /**
     * 获取老师已报名考试
     *
     * @param groupIds 班组ids
     * @return
     */
    private Map<String, NewExam> getGroupRegisterNewExamMap(List<Long> groupIds) {
        List<String> groupRegisterNewExamIds = groupExamRegistrationDao.loadByClazzGroupIds(groupIds)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .map(GroupExamRegistration::getNewExamId)
                .collect(Collectors.toList());
        return newExamLoaderClient.loads(groupRegisterNewExamIds);
    }

    @Override
    public MapMessage loadAllExamsByStudentId(Long studentId) {
        if (studentId == null) {
            return MapMessage.errorMessage("学生id不能为空");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return MapMessage.errorMessage("无效的学生id");
        }
        if (studentDetail.getClazz() == null) {
            return MapMessage.errorMessage("学生未加入任何班级，请先加入").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
        }
        Clazz clazz = studentDetail.getClazz();
        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(clazz.getSchoolId())
                .getUninterruptibly();
        if (school != null && school.getRegionCode() != null) {
            ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
            return loadAllExams(studentDetail, school, exRegion);
        }
        return MapMessage.errorMessage("学校不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_SCHOOL_NOT_EXIST);
    }

    @Override
    public MapMessage registerNewExam(StudentDetail studentDetail, School school, ExRegion exRegion, String newExamId, String clientType, String clientName) {
        if (studentDetail == null || school == null || exRegion == null) {
            return MapMessage.errorMessage("报名失败");
        }
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null || !Objects.equals(NewExamStatus.ONLINE, newExam.getStatus()) || newExam.getDeletedAt() != null) {
            return MapMessage.errorMessage("无效的考试id");
        }
        Date nowDate = new Date();
        if (!"crm".equals(clientName)) {
            if (nowDate.compareTo(newExam.getApplyStartAt()) < 0) {
                return MapMessage.errorMessage("报名未开始");
            }
            if (nowDate.compareTo(newExam.getApplyStopAt()) > 0) {
                return MapMessage.errorMessage("报名已结束");
            }
        }
        // 查询这个学生是否报过名
        String newExamRegistrationId = generateNewExamRegistrationId(studentDetail, newExam);
        NewExamRegistration newExamRegistration = newExamRegistrationLoader.loadById(newExamRegistrationId);
        if (newExamRegistration != null) {
            // 取消报名状态，更新这次报名的相关参数
            if (SafeConverter.toBoolean(newExamRegistration.getBeenCanceled())) {
                newExamRegistration.setBeenCanceled(false);
                newExamRegistration.setClientType(clientType);
                newExamRegistration.setClientName(clientName);
                newExamRegistration.setRegisterAt(nowDate);
                newExamRegistrationDao.update(newExamRegistrationId, newExamRegistration);
                return MapMessage.successMessage("报名成功");
            } else {
                return MapMessage.errorMessage("您已经报过名");
            }
        }
        // 获得学生所在的班组
        GroupMapper studentGroup = groupLoaderClient.loadStudentGroups(studentDetail.getId(), false)
                .stream()
                .filter(t -> t != null && t.getId() != null && Objects.equals(t.getSubject(), newExam.getSubject()))
                .filter(t -> GroupType.TEACHER_GROUP == t.getGroupType())
                .findFirst()
                .orElse(null);
        newExamRegistration = generateNewExamRegistration(studentDetail, school, exRegion, newExam, newExamRegistrationId, clientType, clientName);
        if (studentGroup != null) {
            newExamRegistration.setClazzGroupId(studentGroup.getId());
        }
        String paperId = newExam.fetchPaperId(studentDetail.getId());
        newExamRegistration.setPaperId(paperId);
        newExamRegistration.setRegisterAt(new Date());
        newExamRegistrationDao.insert(newExamRegistration);
        return MapMessage.successMessage("报名成功");
    }


    @Override
    public MapMessage unRegisterNewExam(StudentDetail studentDetail, School school, ExRegion exRegion, String newExamId, String clientType, String clientName) {
        if (studentDetail == null || school == null || exRegion == null) {
            return MapMessage.errorMessage("系统异常，请稍候重试");
        }
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null || !Objects.equals(NewExamStatus.ONLINE, newExam.getStatus()) || newExam.getDeletedAt() != null) {
            return MapMessage.errorMessage("无效的考试id");
        }
        String newExamRegistrationId = generateNewExamRegistrationId(studentDetail, newExam);
        NewExamRegistration newExamRegistration = newExamRegistrationLoader.loadById(newExamRegistrationId);
        if (newExamRegistration == null) {
            newExamRegistration = generateNewExamRegistration(studentDetail, school, exRegion, newExam, newExamRegistrationId, clientType, clientName);
            newExamRegistration.setBeenCanceled(true);
            newExamRegistration.setRegisterAt(new Date());
            newExamRegistrationDao.insert(newExamRegistration);
        } else {
            newExamRegistration.setBeenCanceled(true);
            newExamRegistration.setRegisterAt(new Date());
            newExamRegistrationDao.update(newExamRegistrationId, newExamRegistration);
        }
        return MapMessage.successMessage();
    }


    public MapMessage handlerStudentExaminationAuthority(Long sid, String newExamId, boolean makeUp) {
        if (sid == null || StringUtils.isBlank(newExamId)) {
            return MapMessage.errorMessage("参数有误");
        }
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("参数有误");
        }
        MapMessage mapMessage = loadAllExamsByStudentId(sid);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        List<Map<String, Object>> newExamList = (List<Map<String, Object>>) mapMessage.get("newExamList");

        if (newExamList == null) {
            return MapMessage.errorMessage("参数有误");
        }
        Set<Object> ids = newExamList.stream().map(o -> o.getOrDefault("id", null)).filter(Objects::nonNull).collect(Collectors.toSet());
        if (!ids.contains(newExamId)) {
            return MapMessage.errorMessage("学生不存在该考试权限");
        }
        Date currentTime = new Date();
        if (newExam.getCorrectStopAt().before(currentTime)) {
            return MapMessage.errorMessage("考试修改成绩时间已过");
        }
        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        String newExamRegistrationId = new NewExamRegistration.ID(month, newExam.getSubject(), newExam.getId(), sid.toString()).toString();
        //补考
        if (makeUp) {
            NewExamRegistration newExamRegistration = newExamRegistrationDao.load(newExamRegistrationId);
            NewExamResult newExamResult = newExamResultDao.load(newExamRegistrationId);
            if (newExamRegistration != null && newExamResult != null) {
                return MapMessage.errorMessage("学生参与了考试，需要重考");
            }
            if (newExam.getExamStopAt().after(new Date())) {
                return MapMessage.errorMessage("考试时间并未结束，学生可以进入可以，不需要申请补考");
            }
        } else {
            NewExamRegistration newExamRegistration = newExamRegistrationDao.load(newExamRegistrationId);
            NewExamResult newExamResult = newExamResultDao.load(newExamRegistrationId);
            if (newExamRegistration == null && newExamResult == null) {
                return MapMessage.errorMessage("学生没有参与了考试，需要补考");
            }

            if (newExamRegistration != null) {
                //特殊情况，特殊情况所以硬删数据
                newExamRegistrationDao.delete(newExamRegistrationId);
            }
            if (newExamResult != null) {
                //特殊情况，特殊情况所以硬删数据
                newExamResultDao.delete(newExamRegistrationId);
            }
        }
        StudentExaminationAuthority studentExaminationAuthority = studentExaminationAuthorityDao.load(newExamRegistrationId);
        if (studentExaminationAuthority == null) {
            studentExaminationAuthority = new StudentExaminationAuthority();
            studentExaminationAuthority.setId(newExamRegistrationId);
            studentExaminationAuthority.setCreateAt(currentTime);
            studentExaminationAuthority.setUpdateAt(currentTime);
            studentExaminationAuthorityDao.insert(studentExaminationAuthority);
        } else {
            if (SafeConverter.toBoolean(studentExaminationAuthority.getDisabled())) {
                studentExaminationAuthorityDao.updateStudentExaminationAuthorityDisabled(newExamRegistrationId, false);
            }
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage handlerStudentExaminationAuthority(Long sid, String newExamId) {
        if (sid == null || StringUtils.isBlank(newExamId)) {
            return MapMessage.errorMessage("参数有误");
        }
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("参数有误");
        }
        Date currentTime = new Date();
        if (newExam.getCorrectStopAt().before(currentTime)) {
            return MapMessage.errorMessage("考试修改成绩时间已过");
        }
        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        String newExamRegistrationId = new NewExamRegistration.ID(month, newExam.getSubject(), newExam.getId(), sid.toString()).toString();
        StudentExaminationAuthority studentExaminationAuthority = studentExaminationAuthorityDao.load(newExamRegistrationId);
        if (studentExaminationAuthority == null) {
            studentExaminationAuthority = new StudentExaminationAuthority();
            studentExaminationAuthority.setId(newExamRegistrationId);
            studentExaminationAuthority.setCreateAt(currentTime);
            studentExaminationAuthority.setUpdateAt(currentTime);
            studentExaminationAuthorityDao.insert(studentExaminationAuthority);
        } else {
            if (SafeConverter.toBoolean(studentExaminationAuthority.getDisabled())) {
                studentExaminationAuthorityDao.updateStudentExaminationAuthorityDisabled(newExamRegistrationId, false);
            }
        }
        NewExamRegistration newExamRegistration = newExamRegistrationDao.load(newExamRegistrationId);
        if (newExamRegistration != null) {
            //特殊情况，特殊情况所以硬删数据
            newExamRegistrationDao.delete(newExamRegistrationId);
        }
        NewExamResult newExamResult = newExamResultDao.load(newExamRegistrationId);
        if (newExamResult != null) {
            //特殊情况，特殊情况所以硬删数据
            newExamResultDao.delete(newExamRegistrationId);
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage loadQuestionAnswer(String newExamId, Long studentId, Boolean includeStandardAnswer) {
        return doNewExamProcess.questionAnswer(newExamId, studentId, includeStandardAnswer);
    }

    @Override
    public MapMessage index(String newExamId, Long studentId) {
        return doNewExamProcess.index(newExamId, studentId);
    }

    @Override
    public MapMessage enterExam(String newExamId, StudentDetail studentDetail, String cdnUrl, String clientType, String clientName) {
        try {
            return doNewExamProcess.enterExam(newExamId, studentDetail, cdnUrl, clientType, clientName);
        } catch (Exception ex) {
            logger.error("enter Exam failed :newExamId {}, sid {} ,clientType {},clientName{}", newExamId, studentDetail != null ? studentDetail.getId() : 0L, clientType, clientName, ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @Override
    public MapMessage viewExam(String newExamId, StudentDetail studentDetail, String cdnUrl) {
        try {
            return newExamReportProcessor.viewStudentNewexam(newExamId, studentDetail, cdnUrl);
        } catch (Exception ex) {
            logger.error("view Exam : newExamId {},sid {}", newExamId, studentDetail != null ? studentDetail.getId() : 0L, ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @Override
    public MapMessage processorNewExamResult(NewExamResultContext newExamResultContext) {
        NewExamResultContext context = newExamResultProcessor.process(newExamResultContext);
        return context.transform();
    }

    @Override
    public MapMessage submitNewExam(String newExamId, Long userId, String clientType, String clientName) {
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("考试不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_NOT_EXIST);
        }

        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        NewExamResult.ID id = new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), userId.toString());
        NewExamResult examResult = newExamResultDao.load(id.toString());
        if (examResult == null) {
            return MapMessage.errorMessage("没有参加过该考试").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_RESULT_NOT_EXIST);
        }
        if (examResult.getSubmitAt() != null) {
            return MapMessage.errorMessage("已交卷").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_IS_SUBMIT);
        }
        Date currentDate = new Date();
        Integer submitAfterMinutes = newExam.getSubmitAfterMinutes();
        if (newExam.getSubmitAfterMinutes() != null && currentDate.before(new Date(examResult.getCreateAt().getTime() + (newExam.getSubmitAfterMinutes() * 60 * 1000)))) {
            return MapMessage.errorMessage("进入考试后" + submitAfterMinutes + "分钟前禁止交卷").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_SUBMIT);
        }
        Long durationMilliseconds = new Date().getTime() - examResult.getFlightRecorderTime().getTime();
        Long totalDurationMilliseconds = SafeConverter.toLong(examResult.getDurationMilliseconds()) + durationMilliseconds;
        newExamRegistrationDao.submitNewExam(id.toString(), clientType, clientName, totalDurationMilliseconds);
        newExamResultDao.submitNewExam(id.toString(), clientType, clientName, totalDurationMilliseconds);
        StudentExaminationAuthority studentExaminationAuthority = studentExaminationAuthorityDao.load(id.toString());
        if (studentExaminationAuthority != null) {
            studentExaminationAuthorityDao.updateStudentExaminationAuthorityDisabled(id.toString(), true);
        }

        if (NewExamType.independent == newExam.getExamType()) {
            Student student = studentLoaderClient.loadStudent(userId);
            String studentName = student == null ? "" : student.fetchRealname();
            String content = StringUtils.formatMessage("家长好，你的孩子{}已完成{}，请查看考试结果。", studentName, newExam.getName());
            // 发送家长端push和系统通知
            List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(userId);
            if (CollectionUtils.isNotEmpty(studentParentRefs)) {
                String link = UrlUtils.buildUrlQuery("/view/mobile/student/junior/newexamv2/examdetail", MapUtils.m("newexam_id", newExam.getId(), "student_id", userId));
                List<Long> parentIdList = new ArrayList<>();
                Map<String, Object> extInfo = new HashMap<>();
                extInfo.put("tag", ParentMessageTag.报告.name());
                studentParentRefs.forEach(studentParentRef -> {
                    parentIdList.add(studentParentRef.getParentId());
                    AppMessage message = new AppMessage();
                    message.setUserId(studentParentRef.getParentId());
                    message.setMessageType(ParentMessageType.REMINDER.type);
                    message.setTitle("报告");
                    message.setContent(content);
                    message.setImageUrl("");
                    message.setLinkUrl(link);
                    message.setLinkType(0);
                    message.setIsTop(Boolean.FALSE);
                    message.setExtInfo(extInfo);
                    messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
                });
                Map<String, Object> extras = new HashMap<>();
                extras.put("studentId", userId);
                extras.put("tag", ParentMessageTag.报告.name());
                extras.put("url", link);
                extras.put("s", ParentAppPushType.NOTICE.name());
                appMessageServiceClient.sendAppJpushMessageByIds(
                        content,
                        AppMessageSource.PARENT,
                        parentIdList,
                        extras);
            }
            int studentIntegral = (int) SafeConverter.toDouble(examResult.getScore()) / 20;
            if (studentIntegral > 0) {
                String uniqueKey = newExam.getId() + "_" + userId;
                IntegralHistory studentIntegralHistory = new IntegralHistory(userId, IntegralType.TERM_END_EXAM_STUDENT_REWARD, studentIntegral);
                studentIntegralHistory.setComment("完成单元测评奖励学豆");
                studentIntegralHistory.setUniqueKey(uniqueKey);
                studentIntegralHistory.setRelationClassId(newExam.getGroupId());
                userIntegralServiceClient.getUserIntegralService().changeIntegral(studentIntegralHistory);
            }
            // 发送学生答题明细广播
            if (MapUtils.isNotEmpty(examResult.getAnswers())) {
                Map<String, NewExamProcessResult> processResultMap = newExamProcessResultDao.loads(examResult.getAnswers().values());
                Map<String, Object> map = new HashMap<>();
                map.put("messageType", NewExamPublishMessageType.submitIndependent);
                map.put("processResults", processResultMap.values());
                newExamPublisher.getStudentPublisher().publish(new Message().withPlainTextBody(JsonUtils.toJson(map)));
            }

            // (单元检测)发送学生试卷答题信息广播
            if (NewExamType.independent.equals(newExam.getExamType())) {
                Map<String, Object> map = new HashMap<>();
                map.put("messageType", NewExamPublishMessageType.studentIndependentResult);
                map.put("examResult", examResult);
                newExamPublisher.getStudentPublisher().publish(new Message().withPlainTextBody(JsonUtils.toJson(map)));
            }
        }

        //广播学生报名考试交卷消息
        if (NewExamType.apply.equals(newExam.getExamType())) {
            Map<String, Object> map = new HashMap<>();
            map.put("messageType", NewExamPublishMessageType.submitApply);
            map.put("newExamId", newExamId);
            map.put("studentId", userId);
            newExamPublisher.getStudentPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
        }
        return MapMessage.successMessage("交卷成功");
    }

    @Override
    public MapMessage crmSubmitNewExam(String newExamId, Long userId) {
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("考试不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_NOT_EXIST);
        }

        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        NewExamResult.ID id = new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), userId.toString());
        NewExamResult examResult = newExamResultDao.load(id.toString());
        if (examResult == null) {
            return MapMessage.errorMessage("没有参加过该考试").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_RESULT_NOT_EXIST);
        }
        if (examResult.getSubmitAt() != null) {
            return MapMessage.errorMessage("已交卷").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_IS_SUBMIT);
        }
        newExamResultDao.submitNewExam(examResult.getId(), "crm", "crm", null);
        newExamRegistrationDao.submitNewExam(examResult.getId(), "crm", "crm", null);

        return MapMessage.successMessage();
    }


    //TODO 题库数据
    private Map<String, NewPaper> loadNewPapersByDocIdsIncludeDisable(Collection<String> paperIds) {
        Map<String, NewPaper> map = new LinkedHashMap<>();
        for (String paperId : paperIds) {
            NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(paperId);
            if (newPaper != null) {
                map.put(newPaper.getDocId(), newPaper);
            }
        }
        return map;
    }

    @Override
    public MapMessage loadNewExamDetail(String newExamId, StudentDetail studentDetail) {
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("无效的考试id");
        }
        String registrationId = generateNewExamRegistrationId(studentDetail, newExam);
        NewExamRegistration newExamRegistration = newExamRegistrationLoader.loadById(registrationId);
        String paperDocId = NewExamPaperHelper.fetchPaperId(newExamRegistration, newExam, studentDetail.getId());
//        NewPaper newPaper = paperLoaderClient.loadNewPapersByDocIds0(Collections.singleton(paperDocId)).get(paperDocId);
        NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(paperDocId);
        Map<String, Object> newExamMap = convertNewExamToMap(null, newExam, newExamRegistration, newPaper);
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.putAll(newExamMap);
        return mapMessage;
    }

    private Map<String, Object> convertNewExamToMap(StudentExaminationAuthority studentExaminationAuthority, NewExam newExam, NewExamRegistration newExamRegistration, NewPaper newPaper) {
        if (newExam == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> newExamMap = JsonUtils.safeConvertObjectToMap(newExam);
        NewExamStudentStatus newExamStudentStatus = NewExamStudentStatus.EXPIRED;
        Date date = new Date();
        //考试结束，但是批改时间为结束 ,有重考补考权限，
        if (date.before(newExam.getCorrectStopAt()) && date.after(newExam.getExamStopAt()) && studentExaminationAuthority != null && (!SafeConverter.toBoolean(studentExaminationAuthority.getDisabled()))) {
            if (newExamRegistration == null) {
                newExamStudentStatus = NewExamStudentStatus.BEGIN;
            } else if (newExamRegistration.getSubmitAt() != null) {
                newExamStudentStatus = NewExamStudentStatus.END;
            } else {
                newExamStudentStatus = NewExamStudentStatus.CONTINUE;
            }
        } else {
            if (newExamRegistration != null && newExamRegistration.getSubmitAt() != null) {
                newExamStudentStatus = date.compareTo(newExam.getResultIssueAt()) < 0 ? NewExamStudentStatus.ISSUING : NewExamStudentStatus.END;
            } else {
                if (date.compareTo(newExam.getExamStartAt()) < 0) {
                    newExamStudentStatus = NewExamStudentStatus.BEGIN;
                } else if (date.compareTo(newExam.getExamStartAt()) >= 0 && date.compareTo(newExam.getExamStopAt()) <= 0) {
                    newExamStudentStatus = newExamRegistration == null || newExamRegistration.getStartAt() == null ? NewExamStudentStatus.BEGIN : NewExamStudentStatus.CONTINUE;
                } else if (date.compareTo(newExam.getExamStopAt()) > 0) {
                    if (newExamRegistration == null || newExamRegistration.getStartAt() == null) {
                        newExamStudentStatus = NewExamStudentStatus.ABSENT;
                    } else {
                        newExamStudentStatus = date.compareTo(newExam.getResultIssueAt()) < 0 ? NewExamStudentStatus.ISSUING : NewExamStudentStatus.END;
                    }
                }
            }
        }
        //是否允许学生查看成绩
        boolean notAllowViewScore = newExam.getStudentVisible() != null && newExam.getStudentVisible() == 0 && (newExamStudentStatus.equals(NewExamStudentStatus.ISSUING) || newExamStudentStatus.equals(NewExamStudentStatus.END));
        if (notAllowViewScore) {
            newExamStudentStatus = NewExamStudentStatus.NOT_ALLOW_VIEW_SCORE;
        }
        // NewExam实体里面没有的字段
        newExamMap.put("newExamStudentStatus", newExamStudentStatus);
        newExamMap.put("subject", newExam.processSubject());
        newExamMap.put("oldNewExam", newExam.isOldNewExam());
        newExamMap.put("subjectName", newExam.getSubject().getValue());
        Double score = 0D;
        if (newExamRegistration != null && newPaper != null) {
            score = newExamRegistration.processScore(SafeConverter.toInt(newPaper.getTotalScore()));
        }
        newExamMap.put("score", new BigDecimal(score).setScale(2, BigDecimal.ROUND_HALF_UP));

        double rate = 0;
        if (newPaper != null && newPaper.getTotalScore() != null && newPaper.getTotalScore() > 0) {
            rate = new BigDecimal(score * 100).divide(new BigDecimal(newPaper.getTotalScore()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        NewExam.EmbedRank embedRank = newExam.processScoreLevel(rate);
        newExamMap.put("gradeType", SafeConverter.toInt(newExam.getGradeType()));
        newExamMap.put("scoreLevel", embedRank == null ? "无等级" : embedRank.getRankName());
        Long remainTime = 0L;
        Long currentTime = System.currentTimeMillis() + 1000;
        String endDate = DateFormatUtils.format(newExam.getExamStopAt(), "MM-dd HH:mm");
        if (newExam.getExamStopAt().getTime() > currentTime) {
            remainTime = newExam.getExamStopAt().getTime() - currentTime;
        }
        newExamMap.put("endDate", endDate);
        newExamMap.put("remainTime", remainTime);
        newExamMap.put("fullScore", newPaper != null ? SafeConverter.toInt(newPaper.getTotalScore()) : 0);
        return newExamMap;
    }

    private NewExamRegistration generateNewExamRegistration(StudentDetail studentDetail,
                                                            School school,
                                                            ExRegion exRegion,
                                                            NewExam newExam,
                                                            String newExamRegistrationId,
                                                            String clientType,
                                                            String clientName) {
        NewExamRegistration newExamRegistration = new NewExamRegistration();
        newExamRegistration.setId(newExamRegistrationId);
        newExamRegistration.setNewExamId(newExam.getId());
        newExamRegistration.setSubject(Subject.fromSubjectId(newExam.getSubjectId()));
        newExamRegistration.setUserId(studentDetail.getId());
        newExamRegistration.setUserName(studentDetail.getProfile().getRealname());
        newExamRegistration.setExamType(newExam.getExamType());
        newExamRegistration.setBeenCanceled(false);
        newExamRegistration.setProvinceId(exRegion.getProvinceCode());
        newExamRegistration.setCityId(exRegion.getCityCode());
        newExamRegistration.setRegionId(exRegion.getCountyCode());
        newExamRegistration.setSchoolId(school.getId());
        newExamRegistration.setClazzLevel(studentDetail.getClazzLevel().getLevel());
        newExamRegistration.setSchoolLevel(SchoolLevel.safeParse(school.getLevel()));
        newExamRegistration.setClazzId(studentDetail.getClazzId());
        newExamRegistration.setClientType(clientType);
        newExamRegistration.setClientName(clientName);
        return newExamRegistration;
    }

    private String generateNewExamRegistrationId(StudentDetail studentDetail, NewExam newExam) {
        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        return new NewExamRegistration.ID(month, newExam.getSubject(), newExam.getId(), studentDetail.getId().toString()).toString();
    }

    private Set<Subject> loadStudentSubjects(Long studentId) {
        List<GroupMapper> groupMappers = groupLoaderClient.loadStudentGroups(studentId, false);
        if (CollectionUtils.isEmpty(groupMappers)) {
            return Collections.emptySet();
        }
        return groupMappers.stream()
                .map(GroupMapper::getSubject)
                .collect(Collectors.toSet());
    }

    @Override
    public MapMessage correctNewExam(CorrectNewExamContext correctNewExamContext) {
        CorrectNewExamContext context = correctNewExamProcessor.process(correctNewExamContext);
        return context.transform().setSuccess(context.isSuccessful() && CollectionUtils.isEmpty(context.getCorrectErrorUsers()))
                .add("correctErrorUsers", context.getCorrectErrorUsers())
                .add("correctSuccessUsers", context.getCorrectSuccessUsers());
    }

    @SuppressWarnings("unchecked")
    public MapMessage resetScore(Map<String, Object> pram) {
        String newExamId = (String) pram.get("newExamId");
        String questionDocId = (String) pram.get("questionDocId");
        List<List<String>> userAnswers = (List<List<String>>) pram.get("answer");
        List<List<String>> userErrorAnswers = (List<List<String>>) pram.get("errorAnswer");
        List<Long> studentIds = JsonUtils.fromJsonToList(JsonUtils.toJson(pram.get("studentIds")), Long.class);
        Boolean allUser = (Boolean) pram.get("allUser");
        NewExam newExam = newExamLoaderClient.load(newExamId);
        NewQuestion question = tikuStrategy.loadQuestionByDocId(questionDocId);
        List<String> newExamResultIds = newExamResultDao.findByNewExam(newExam);
        Map<String, NewExamResult> resultMap = newExamResultDao.loads(newExamResultIds);
        for (NewExamResult newExamResult : resultMap.values()) {
            if (studentIds != null && studentIds.contains(newExamResult.getUserId())) {
                processScore(newExam, newExamResult, question, userAnswers, userErrorAnswers);
            }
            if (allUser != null && allUser) {
                processScore(newExam, newExamResult, question, userAnswers, userErrorAnswers);
            }
        }
        return MapMessage.successMessage();
    }


    @Override
    @SuppressWarnings("unchecked")
    public MapMessage newResetScore(String param) {
        ResetScoreRequestParameter parameter = JsonUtils.fromJson(param, ResetScoreRequestParameter.class);
        if (parameter == null) {
            return MapMessage.errorMessage("parameter is error");
        }
        if (StringUtils.isBlank(parameter.getAnswerStr())) {
            return MapMessage.errorMessage("parameter is error");
        } else {
            List answer = JsonUtils.fromJson(parameter.getAnswerStr(), List.class);
            parameter.setAnswer(answer);
        }
        if (StringUtils.isBlank(parameter.getErrorAnswerStr())) {
            return MapMessage.errorMessage("parameter is error");
        } else {
            List errorAnswer = JsonUtils.fromJson(parameter.getErrorAnswerStr(), List.class);
            parameter.setErrorAnswer(errorAnswer);
        }
        if (!parameter.isAllUser()) {
            if (StringUtils.isBlank(parameter.getStudentIdsStr())) {
                return MapMessage.errorMessage("parameter is error");
            } else {
                Set<Long> studentIds = (Set<Long>) JsonUtils.fromJson(parameter.getStudentIdsStr(), List.class)
                        .stream()
                        .filter(Objects::nonNull)
                        .map(SafeConverter::toLong)
                        .collect(Collectors.toSet());
                parameter.setStudentIds(studentIds);
            }
        }
        if (parameter.getNewExamId() == null || parameter.getNewExamId().equals("NULL")) {
            return MapMessage.errorMessage("newExamId is null");
        }
        String newExamId = parameter.getNewExamId();
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("newExam is null");
        }
        String questionDocId = parameter.getQuestionDocId();
        if (questionDocId == null) {
            return MapMessage.errorMessage("questionDocId is null");
        }
        List<List<String>> errorAnswer = parameter.getErrorAnswer();
        if (errorAnswer == null) {
            return MapMessage.errorMessage("errorAnswer is null");
        }
        String errorAnswerStr = JsonUtils.toJson(errorAnswer);
        Set<Long> studentIds = parameter.getStudentIds();
        boolean allUser = parameter.isAllUser();
        if (!allUser && CollectionUtils.isEmpty(studentIds)) {
            return MapMessage.errorMessage();
        }
        if (StringUtils.isBlank(parameter.getPaperId())) {
            return MapMessage.errorMessage("paperId is null");
        }

        NewQuestion question = tikuStrategy.loadQuestionByDocId(questionDocId);
        List<List<String>> answer = parameter.getAnswer();
        List<String> newExamResultIds = newExamResultDao.findByNewExam(newExam);
        //获取对于可能学生的做题记录
        Map<Long, NewExamResult> resultMap = newExamResultDao.loads(newExamResultIds)
                .values()
                .stream()
                .filter(o -> allUser || studentIds.contains(o.getUserId()))
                .filter(o -> Objects.equals(parameter.getPaperId(), o.getPaperId()))
                .collect(Collectors.toMap(NewExamResult::getUserId, Function.identity()));
        //获取要求改变题目processIds
        List<String> processIds = resultMap.values()
                .stream()
                .filter(o -> MapUtils.isNotEmpty(o.getAnswers()))
                .filter(o -> o.getAnswers().containsKey(questionDocId))
                .map(o -> o.getAnswers().get(questionDocId))
                .collect(Collectors.toList());
        Map<String, NewExamProcessResult> newExamProcessResultMap = newExamProcessResultDao.loads(processIds);
        //获得符合该种答题的方式的NewExamProcessResult
        List<NewExamProcessResult> targetNewExamProcess = newExamProcessResultMap.values()
                .stream()
                .filter(o -> errorAnswerStr.equals(JsonUtils.toJson(o.getUserAnswers())))
                .collect(Collectors.toList());
        if (targetNewExamProcess.size() > 0) {
            NewExamProcessResult newExamProcessResult = targetNewExamProcess.get(0);
            UserAnswerMapper uam = new UserAnswerMapper(question.getId(), newExamProcessResult.getStandardScore(), answer);
            // 下面是为了输出日志用的
            uam.setUserAgent("crm修复分数");
            uam.setHomeworkId(newExamId);
            uam.setHomeworkType(StudyType.examination.name());
            targetNewExamProcess.forEach(o -> {
                uam.setUserId(o.getUserId());
                newProcessScore(question, uam, newExam, resultMap.get(o.getUserId()), o, answer);
            });
        }
        return MapMessage.successMessage();
    }


    public MapMessage resetOralQuestionScoreV2(String newExamId, String questionId, String docId, List<Long> userIds) {
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("newExamId 错误");
        }
        NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(docId);
        if (newPaper == null) {
            return MapMessage.errorMessage("newPaper 错误");
        }
        List<NewPaperQuestion> questions = newPaper.getQuestions();
        if (questions == null) {
            return MapMessage.errorMessage("newPaper 错误");
        }
        //获取对应小题的跨度分数
        NewPaperQuestion target = null;
        for (NewPaperQuestion q : questions) {
            if (Objects.equals(q.getId(), questionId)) {
                target = q;
                break;
            }

        }
        if (target == null) {
            return MapMessage.errorMessage("newPaper 错误");
        }
        float intervalScore = SafeConverter.toFloat(target.getIntervalScore());
        if (intervalScore <= 0) {
            return MapMessage.errorMessage("newPaper intervalScore错误");
        }


        NewQuestion newQuestion = tikuStrategy.loadQuestionIncludeDisabled(questionId);
        if (newQuestion == null)
            return MapMessage.errorMessage("questionId 错误");
        List<String> newExamResultIds = new LinkedList<>();
        //是否进行全部学生处理的分界线
        if (CollectionUtils.isNotEmpty(userIds)) {
            for (Long userId : userIds) {
                String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
                NewExamResult.ID id = new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), userId.toString());
                newExamResultIds.add(id.toString());
            }
        } else {
            newExamResultIds = newExamResultDao.findByNewExam(newExam);
        }
        if (CollectionUtils.isEmpty(newExamResultIds)) {
            return MapMessage.successMessage();
        }
        //过滤没做这个题学生
        Map<String, NewExamResult> newExamResultMap = newExamResultDao.loads(newExamResultIds)
                .values()
                .stream()
                .filter(o -> o.getAnswers() != null)
                .filter(o -> o.getAnswers().containsKey(newQuestion.getDocId()))
                .collect(Collectors.toMap(o -> o.getAnswers().get(newQuestion.getDocId()), Function.identity()));
        Map<String, NewExamProcessResult> newExamProcessResultMap = newExamProcessResultDao.loads(newExamResultMap.keySet());
        for (NewExamProcessResult newExamProcessResult : newExamProcessResultMap.values()) {
            if (!newExamResultMap.containsKey(newExamProcessResult.getId()))
                continue;
            double stdScore = SafeConverter.toDouble(newExamProcessResult.getStandardScore());
            List<List<NewExamProcessResult.OralDetail>> oralScoreDetails = newExamProcessResult.getOralDetails();
            if (oralScoreDetails == null)
                continue;
            Double subStdScore = new BigDecimal(stdScore).divide(new BigDecimal(oralScoreDetails.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            //实际每个小题语音打分得到分数
            List<Double> scoreResult = new ArrayList<>();
            for (List<NewExamProcessResult.OralDetail> oralDetails : oralScoreDetails) {
                int macScore = 0;
                int oralScoreDetailsSize = 0;
                for (NewExamProcessResult.OralDetail oralDetail : oralDetails) {
                    if (oralDetail != null && oralDetail.getMacScore() != null) {
                        macScore += oralDetail.getMacScore();
                        oralScoreDetailsSize++;
                    }
                }
                BigDecimal avgMacScore = new BigDecimal(0);
                if (macScore != 0 && oralScoreDetailsSize != 0) {
                    avgMacScore = new BigDecimal(macScore).divide(new BigDecimal(oralScoreDetailsSize), 0, BigDecimal.ROUND_HALF_UP);
                }
                BigDecimal bigDecimal = new BigDecimal(subStdScore);

                bigDecimal = bigDecimal.multiply(avgMacScore);
                double sysScore = bigDecimal.divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                //0四舍五入算法R
                //1向上取整算法
                sysScore = new BigDecimal(sysScore).divide(new BigDecimal(intervalScore), 0, BigDecimal.ROUND_HALF_UP).intValue() * intervalScore;
                //意外情况：当题库不给跨度分，或者跨度分有问题不可整除
                //意外情况：产品要求继续考试
                if (sysScore - subStdScore > 0) {
                    sysScore = subStdScore;
                }
                //两位小数处理
                sysScore = new BigDecimal(sysScore).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                scoreResult.add(sysScore);
            }
            //这个题用户每个小题的得分
            List<Double> subScoreList = newExamProcessResult.processSubScore();
            for (int i = 0; i < subScoreList.size(); i++) {
                //当小题分数
                if (Objects.equals(subScoreList.get(i), scoreResult.get(i)))
                    continue;
                CorrectNewExamContext correctNewExamContext = new CorrectNewExamContext();
                correctNewExamContext.setQuestionId(questionId);
                correctNewExamContext.setNewExamId(newExamId);
                correctNewExamContext.setSubId(i);
                Map<Long, Double> userScoreMap = new LinkedHashMap<>();
                correctNewExamContext.setUserScoreMap(userScoreMap);
                userScoreMap.put(newExamProcessResult.getUserId(), scoreResult.get(i));
                MapMessage mapMessage = correctNewExam(correctNewExamContext);
                if (mapMessage.isSuccess()) {
                    LogCollector.info("backend-general", MapUtils.map(
                            "env", RuntimeMode.getCurrentStage(),
                            "usertoken", newExamProcessResult.getUserId(),
                            "mod1", "考试ID：" + newExamId + " 题ID：" + questionId,//方便查询该题下面全部小题
                            "mod2", " 小题ID：" + i,//再到小题的维度
                            "mod3", newExamProcessResult.getId(),
                            "mod4", "原始分：" + subScoreList.get(i) + "，修改之后分数:" + scoreResult.get(i),//原本分数
                            "op", "newExam success resetScore"//成功标识
                    ));
                } else {
                    LogCollector.info("backend-general", MapUtils.map(
                            "env", RuntimeMode.getCurrentStage(),
                            "usertoken", newExamProcessResult.getUserId(),
                            "mod1", "考试ID：" + newExamId + " 题ID：" + questionId,//方便查询该题下面全部小题
                            "mod2", " 小题ID：" + i,//再到小题的维度
                            "mod3", newExamProcessResult.getId(),
                            "mod4", mapMessage.getErrorCode(),//
                            "op", "newExam failed resetScore"//失败标识
                    ));
                }

            }
        }
        return MapMessage.successMessage();
    }

    private void newProcessScore(NewQuestion newQuestion, UserAnswerMapper uam, NewExam newExam, NewExamResult newExamResult, NewExamProcessResult newExamProcessResult,
                                 List<List<String>> userAnswers) {
        QuestionScoreResult scoreResult = tikuStrategy.loadQuestionScoreResult(uam, null, newExam.getSchoolLevel());
        List<Double> subScore = new ArrayList<>(); // 作答区域的得分情况
        List<List<Boolean>> subGrasp = new ArrayList<>();   // 作答区域的掌握情况
        if (scoreResult != null) {
            scoreResult.getSubScoreResults().forEach(e -> {
                subScore.add(e.getScore());
                subGrasp.add(e.getIsRight());
            });
        }
        double score = SafeConverter.toDouble(scoreResult != null ? scoreResult.getTotalScore() : 0);
        //客观分
        double totalScore = score - SafeConverter.toDouble(newExamProcessResult.getScore()) + SafeConverter.toDouble(newExamResult.getScore());
        //批改分
        double correctScore = score - SafeConverter.toDouble(newExamProcessResult.getScore()) + SafeConverter.toDouble(newExamResult.getCorrectScore());
        //是否批改过
        boolean corrected = newExamResult.getCorrectScore() != null;

        NewExamProcessResult.ID id = new NewExamProcessResult.ID(newExam.getCreatedAt());
        newExamProcessResult.setId(id.toString());
        newExamProcessResult.setUserAnswers(userAnswers);
        newExamProcessResult.setQuestionId(newQuestion.getId());
        newExamProcessResult.setScore(score);
        newExamProcessResult.setGrasp(scoreResult != null && SafeConverter.toBoolean(scoreResult.getIsRight()));
        newExamProcessResult.setSubGrasp(subGrasp);
        newExamProcessResult.setSubScore(subScore);
        String processId = newExamProcessResultDao.insert(newExamProcessResult);
        newExamQueueProducer.sendSaveResultMessage(newExamProcessResult);
        newExamRegistrationDao.doNewExamResult(newExamResult.getId(), totalScore, newExamResult.getDurationMilliseconds(), newExamResult.getFinishAt() != null);
        newExamResultDao.doNewExam(newExamResult.getId(),
                newExamProcessResult.getQuestionDocId(),
                processId,
                totalScore,
                newExamResult.getDurationMilliseconds(),
                newExamResult.getFinishAt() != null);
        //批改过的才又走批改处理
        if (corrected) {
            newExamResultDao.correctNewExam(newExamResult.getId(), correctScore);
            newExamRegistrationDao.correctNewExam(newExamResult.getId(), correctScore);
        }
    }

    private void processScore(NewExam newExam, NewExamResult newExamResult, NewQuestion newQuestion, List<List<String>> userAnswers, List<List<String>> userErrorAnswers) {
        if (newExamResult.getAnswers() != null) {
            String resultProcessId = newExamResult.getAnswers().get(newQuestion.getDocId());
            NewExamProcessResult newExamProcessResult = newExamProcessResultDao.load(resultProcessId);
            if (newExamProcessResult != null) {
                List<List<String>> oldUserAnswer = newExamProcessResult.getUserAnswers();
                if (JsonUtils.toJson(oldUserAnswer).equals(JsonUtils.toJson(userErrorAnswers))) {
                    UserAnswerMapper uam = new UserAnswerMapper(newQuestion.getId(), newExamProcessResult.getStandardScore(), userAnswers);
                    // 下面是为了输出日志用的
                    uam.setUserAgent("crm修复分数");
                    uam.setUserId(newExamResult.getUserId());
                    uam.setHomeworkId(newExamResult.getNewExamId());
                    uam.setHomeworkType(StudyType.examination.name());
                    QuestionScoreResult scoreResult = tikuStrategy.loadQuestionScoreResult(uam, null, newExam.getSchoolLevel());
                    List<Double> subScore = new ArrayList<>(); // 作答区域的得分情况
                    List<List<Boolean>> subGrasp = new ArrayList<>();   // 作答区域的掌握情况
                    List<List<String>> standardAnswer = new ArrayList<>(); // 标准答案
                    scoreResult.getSubScoreResults().forEach(e -> {
                        subScore.add(e.getScore());
                        standardAnswer.add(e.getStandardAnswer());
                        subGrasp.add(e.getIsRight());
                    });
                    double score = scoreResult != null ? scoreResult.getTotalScore() : 0;

                    double oldScore = newExamProcessResult.getScore() != null ? newExamProcessResult.getScore() : 0;
                    score = score - oldScore;
                    double totalScore = newExamResult.getScore() + score;
                    NewExamProcessResult.ID id = new NewExamProcessResult.ID(newExam.getCreatedAt());
                    newExamProcessResult.setId(id.toString());
                    newExamProcessResult.setQuestionId(newQuestion.getId());
                    newExamProcessResult.setUserAnswers(userAnswers);
                    newExamProcessResult.setScore(scoreResult.getTotalScore());
                    newExamProcessResult.setGrasp(scoreResult.getIsRight());
                    newExamProcessResult.setSubGrasp(subGrasp);
                    newExamProcessResult.setSubScore(subScore);
                    String processId = newExamProcessResultDao.insert(newExamProcessResult);
                    newExamRegistrationDao.doNewExamResult(newExamResult.getId(), totalScore, newExamResult.getDurationMilliseconds(), newExamResult.getFinishAt() != null);
                    newExamResultDao.doNewExam(newExamResult.getId(),
                            newExamProcessResult.getQuestionDocId(),
                            processId,
                            totalScore,
                            newExamResult.getDurationMilliseconds(),
                            newExamResult.getFinishAt() != null);
                }
            }
        }
    }

    @Override
    public MapMessage loadPaperList(String bookId, Teacher teacher) {
        // 获取教材下的单元，排序用
        List<NewBookCatalog> unitList = newContentLoaderClient.loadChildren(Collections.singleton(bookId), BookCatalogType.UNIT).get(bookId);
        Map<String, Integer> unitIdRankMap = unitList.stream().collect(Collectors.toMap(NewBookCatalog::getId, NewBookCatalog::getRank));
        // 获取教材下的模考试卷
        List<NewPaper> paperList = paperLoaderClient.loadPaperAsListByNewBooKIdAndExtraTypes(bookId, Collections.singleton(NewPaper.ExtraQuestionType.EVALUATION));
        Set<String> cacheKeySet = paperList.stream()
                .map(paper -> StringUtils.formatMessage(CACHE_KEY_TEMPLATE, teacher.getId(), paper.getDocId()))
                .collect(Collectors.toSet());
        Map<String, CacheObject<Boolean>> cacheObjectMap = newExamCacheClient.cacheSystem.CBS.flushable.gets(cacheKeySet);
        List<Map<String, Object>> paperMapperList = paperList.stream()
                .sorted((p1, p2) -> {
                    int rank1 = 1;
                    int rank2 = 1;
                    if (CollectionUtils.isNotEmpty(p1.getBooksNew())) {
                        String unitId = p1.getBooksNew().get(0).getUnitId();
                        if (StringUtils.isNotBlank(unitId)) {
                            rank1 = SafeConverter.toInt(unitIdRankMap.get(unitId), 1);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(p2.getBooksNew())) {
                        String unitId = p2.getBooksNew().get(0).getUnitId();
                        if (StringUtils.isNotBlank(unitId)) {
                            rank2 = SafeConverter.toInt(unitIdRankMap.get(unitId), 1);
                        }
                    }
                    if (rank1 == rank2) {
                        return StringUtils.compare(p1.getTitle(), p2.getTitle());
                    }
                    return Integer.compare(rank1, rank2);
                })
                .map(paper -> {
                            String cacheKey = StringUtils.formatMessage(CACHE_KEY_TEMPLATE, teacher.getId(), paper.getDocId());
                            CacheObject<Boolean> cacheResult = cacheObjectMap.get(cacheKey);
                            return MapUtils.m("paperId", paper.getId(),
                                    "paperName", paper.getTitle(),
                                    "questionCount", paper.getTotalNum(),
                                    "minutes", paper.getExamTime(),
                                    "showAssigned", cacheResult != null && Objects.equals(cacheResult.getValue(), Boolean.TRUE));
                        }
                )
                .collect(Collectors.toList());
        return MapMessage.successMessage().add("paperList", paperMapperList);
    }

    @Override
    public MapMessage assignNewExam(Teacher teacher, Map<String, Object> source) {
        if (MapUtils.isEmpty(source)) {
            return MapMessage.errorMessage("测试内容错误");
        }
        String paperId = SafeConverter.toString(source.get("paperId"));
        if (StringUtils.isBlank(paperId)) {
            return MapMessage.errorMessage("试卷id错误");
        }
        NewPaper paper = paperLoaderClient.loadPaperIncludeDisabled(paperId);
        if (paper == null) {
            return MapMessage.errorMessage("试卷不存在");
        }
        Date startTime = SafeConverter.toDate(source.get("startTime"));
        if (startTime == null) {
            return MapMessage.errorMessage("测试开始时间错误");
        }
        Date endTime = SafeConverter.toDate(source.get("endTime"));
        Date now = new Date();
        if (endTime == null) {
            return MapMessage.errorMessage("测试结束时间错误");
        }
        if (endTime.before(now)) {
            return MapMessage.errorMessage("测试结束时间不能早于当前时间");
        }
        if (startTime.after(endTime)) {
            return MapMessage.errorMessage("测试开始时间不能晚于结束时间");
        }
        int durationMinutes = SafeConverter.toInt(source.get("durationMinutes"));
        if (durationMinutes <= 0) {
            return MapMessage.errorMessage("答题时长错误");
        }
        String[] groupIds = StringUtils.split(SafeConverter.toString(source.get("groupIds")), ",");
        Set<Long> groupIdSet = new LinkedHashSet<>();
        for (String groupIdStr : groupIds) {
            long groupId = SafeConverter.toLong(groupIdStr);
            if (groupId != 0) {
                groupIdSet.add(groupId);
            }
        }
        if (CollectionUtils.isEmpty(groupIdSet)) {
            return MapMessage.errorMessage("班组id错误");
        }
        Subject subject = Subject.fromSubjectId(paper.getSubjectId());
        Long realTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), subject);
        Teacher realTeacher = teacherLoaderClient.loadTeacher(realTeacherId);
        if (realTeacherId == null || realTeacher == null) {
            return MapMessage.errorMessage("账号信息错误");
        }
        List<GroupTeacherMapper> groupList = groupLoaderClient.loadTeacherGroups(realTeacherId, false);
        Set<Long> teacherGroupIds = groupList.stream()
                .map(GroupMapper::getId)
                .collect(Collectors.toSet());
        Map<Long, Subject> groupSubjectMap = groupList
                .stream()
                .collect(Collectors.toMap(GroupMapper::getId, GroupMapper::getSubject));
        for (Long groupId : groupIdSet) {
            if (!teacherGroupIds.contains(groupId)) {
                return MapMessage.errorMessage("没有班组" + groupId + "的操作权限");
            }
            if (subject != groupSubjectMap.get(groupId)) {
                return MapMessage.errorMessage("班组" + groupId + "学科与试卷学科不匹配");
            }
        }
        Long studentId = null;
        boolean showScoreLevel = false;
        Map<Long, List<Long>> groupStudentIds = studentLoaderClient.loadGroupStudentIds(groupIdSet);
        if (MapUtils.isNotEmpty(groupStudentIds)) {
            for (List<Long> studentIds : groupStudentIds.values()) {
                if (CollectionUtils.isNotEmpty(studentIds)) {
                    studentId = studentIds.iterator().next();
                    break;
                }
            }
        }
        if (studentId != null) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail != null) {
                showScoreLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
            }
        }
        Date currentDate = new Date();
        List<NewExam> newExams = new ArrayList<>();
        for (Long groupId : groupIdSet) {
            NewExam newExam = new NewExam();
            newExam.setName(paper.getTitle());
            newExam.setExamType(NewExamType.independent);

            NewExam.EmbedPaper embedPaper = new NewExam.EmbedPaper();
            embedPaper.setPaperId(paper.getDocId());
            embedPaper.setPaperName("A卷");
            newExam.setPapers(Collections.singletonList(embedPaper));

            newExam.setSubjectId(subject.getId());
            newExam.setCorrectStopAt(endTime);
            newExam.setExamStartAt(startTime);
            newExam.setExamStopAt(endTime);
            newExam.setResultIssueAt(endTime);
            newExam.setDurationMinutes(durationMinutes);
            newExam.setSubmitAfterMinutes(0);
            newExam.setStatus(NewExamStatus.ONLINE);
            newExam.setCreatedAt(currentDate);
            newExam.setUpdatedAt(currentDate);
            newExam.setTestCategory(2);
            newExam.setOralRepeatCount(-1);
            newExam.setGroupId(groupId);
            newExam.setTeacherId(realTeacherId);
            newExam.setGradeType(showScoreLevel ? 1 : 0);
            if (showScoreLevel) {
                newExam.setRanks(NewExam.EmbedRank.independentRanks);
            }
            newExams.add(newExam);
        }
        newExams = newExamLoaderClient.inserts(newExams);

        // 发送学生端jpush
        for (NewExam newExam : newExams) {
            String link = UrlUtils.buildUrlQuery("/studentMobile/homework/app/newexam.vpage", MapUtils.m("newExamId", newExam.getId()));
            List<Long> studentIds = groupStudentIds.get(newExam.getGroupId());
            String content = "单元考试已开始，请同学们按时完成考试！";
            Map<String, Object> extInfo = MapUtils.m("s", StudentAppPushType.NEW_EXAM_REMIND.getType(), "link", link, "t", "h5", "key", "j", "title", StudentAppPushType.NEW_EXAM_REMIND.getDescription());
            // 判断考试开始时间是否早于当前时间
            if (currentDate.before(newExam.getExamStartAt())) {
                appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.STUDENT, studentIds, extInfo, newExam.getExamStartAt().getTime());
            } else {
                appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.STUDENT, studentIds, extInfo);
            }
        }
        String cacheKey = StringUtils.formatMessage(CACHE_KEY_TEMPLATE, teacher.getId(), paper.getDocId());
        newExamCacheClient.cacheSystem.CBS.flushable.set(cacheKey, 0, true);
        // 发送家校群消息
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacher.getId());
        if (mainTeacherId == null) {
            mainTeacherId = teacher.getId();
        }
        for (NewExam newExam : newExams) {
            sendAssignMessageToChatGroup(newExam, mainTeacherId, realTeacher);
        }

        // 发送老师端报告jpush
        Map<Long, GroupMapper> groupMappers = groupLoaderClient.loadGroups(groupIdSet, false);
        Set<Long> clazzIdSet = groupMappers.values().stream().map(GroupMapper::getClazzId).collect(Collectors.toSet());
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIdSet)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        List<Long> relTeacherIds = new ArrayList<>(teacherLoaderClient.loadRelTeacherIds(teacher.getId()));
        for (NewExam newExam : newExams) {
            String link = UrlUtils.buildUrlQuery("/view/evaluation/report", MapUtils.m("examId", newExam.getId()));
            String clazzName = "";
            GroupMapper groupMapper = groupMappers.get(newExam.getGroupId());
            if (groupMapper != null) {
                Clazz clazz = clazzMap.get(groupMapper.getClazzId());
                if (clazz != null) {
                    clazzName = clazz.formalizeClazzName();
                }
            }
            String content = "老师好，已发布" + clazzName + newExam.getName() + "的测评报告，立即前往查看";
            Map<String, Object> extInfo = MapUtils.m("link", link, "key", "j", "t", "h5", "s", TeacherMessageType.ACTIVIY.getType());
            appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.PRIMARY_TEACHER, relTeacherIds, extInfo, newExam.getExamStopAt().getTime());
        }
        return MapMessage.successMessage();
    }

    private void sendAssignMessageToChatGroup(NewExam newExam, Long mainTeacherId, Teacher realTeacher) {
        String iMContent = StringUtils.formatMessage("家长好，我布置了{}，开始时间{}，请家长督促。", newExam.getName(), DateUtils.dateToString(newExam.getExamStartAt(), "MM月dd日HH:mm"));

        Long teacherId = mainTeacherId == null ? realTeacher.getId() : mainTeacherId;
        //这里才是取所有的学科
        Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        List<Subject> subjectList = teacherLoaderClient.loadTeachers(relTeacherIds).values().stream().map(Teacher::getSubject).collect(Collectors.toList());
        List<String> subjectStrList = subjectList.stream().sorted(Comparator.comparingInt(Subject::getId)).map(Subject::getValue).collect(Collectors.toList());
        String subjectsStr = "（" + StringUtils.join(subjectStrList.toArray(), "，") + "）";

        String em_push_title = realTeacher.fetchRealnameIfBlankId() + subjectsStr + "：" + iMContent;
        //新的push
        Map<String, Object> extras = new HashMap<>();
        extras.put("studentId", "");
        extras.put("tag", ParentMessageTag.通知.name());
        extras.put("s", ParentAppPushType.HOMEWORK_ASSIGN.name());
        appMessageServiceClient.sendAppJpushMessageByTags(
                em_push_title,
                AppMessageSource.PARENT,
                Collections.singletonList(JpushUserTag.CLAZZ_GROUP_REFACTOR.generateTag(SafeConverter.toString(newExam.getGroupId()))),
                null,
                extras);
    }

    @Override
    public MapMessage loadTeacherClazzListNew(Set<Long> teacherIds) {
        Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(teacherIds, true);
        Map<Long, Subject> groupIdSubjectMap = new HashMap<>();
        // clazz id -> group id map
        Map<Long, List<Long>> clazzIdGroupIdMap = new HashMap<>();
        teacherGroups.forEach((tId, groups) -> groups.forEach(group -> {
            if (group.isTeacherGroupRefStatusValid(tId) && CollectionUtils.isNotEmpty(group.getStudents())) {
                clazzIdGroupIdMap.computeIfAbsent(group.getClazzId(), k -> new ArrayList<>()).add(group.getId());
                groupIdSubjectMap.put(group.getId(), group.getSubject());
            }
        }));
        List<Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIdGroupIdMap.keySet())
                .stream()
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());
        boolean showSubject = teacherIds.size() > 1;
        List<Map<String, Object>> clazzList = new ArrayList<>();
        clazzs.stream()
                .filter(clazz -> clazzIdGroupIdMap.containsKey(clazz.getId()))
                .forEach(c -> {
                    List<Long> gids = clazzIdGroupIdMap.get(c.getId());
                    gids.forEach(gid -> {
                        String clazzName = c.formalizeClazzName();
                        if (showSubject) {
                            Subject subject = groupIdSubjectMap.get(gid);
                            clazzName = clazzName + "(" + subject.getValue() + ")";
                        }
                        Map<String, Object> clazzMapper = MapUtils.m(
                                "clazzId", c.getId(),
                                "clazzName", clazzName,
                                "groupId", gid);
                        clazzList.add(clazzMapper);
                    });
                });
        return MapMessage.successMessage().add("clazzList", clazzList);
    }


    @Override
    public MapMessage loadTeacherClazzList(Set<Long> teacherIds) {
        Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(teacherIds, true);
        Map<Long, Subject> groupIdSubjectMap = new HashMap<>();
        // clazz id -> group id map
        Map<Long, List<Long>> clazzIdGroupIdMap = new HashMap<>();
        teacherGroups.forEach((tId, groups) -> groups.forEach(group -> {
            if (group.isTeacherGroupRefStatusValid(tId) && CollectionUtils.isNotEmpty(group.getStudents())) {
                clazzIdGroupIdMap.computeIfAbsent(group.getClazzId(), k -> new ArrayList<>()).add(group.getId());
                groupIdSubjectMap.put(group.getId(), group.getSubject());
            }
        }));
        List<Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIdGroupIdMap.keySet())
                .stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());
        boolean showSubject = teacherIds.size() > 1;
        List<Map<String, Object>> clazzList = new ArrayList<>();
        clazzs.stream()
                .filter(clazz -> clazzIdGroupIdMap.containsKey(clazz.getId()))
                .forEach(c -> {
                    List<Long> gids = clazzIdGroupIdMap.get(c.getId());
                    gids.forEach(gid -> {
                        String clazzName = c.formalizeClazzName();
                        if (showSubject) {
                            Subject subject = groupIdSubjectMap.get(gid);
                            clazzName = clazzName + "(" + subject.getValue() + ")";
                        }
                        Map<String, Object> clazzMapper = MapUtils.m(
                                "clazzId", c.getId(),
                                "clazzName", clazzName,
                                "groupId", gid);
                        clazzList.add(clazzMapper);
                    });
                });
        return MapMessage.successMessage().add("clazzList", clazzList);
    }

    @Override
    public MapMessage deleteNewExam(Teacher teacher, String newExamId) {
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("测试不存在");
        }
        if (newExam.getExamType() != NewExamType.independent) {
            return MapMessage.errorMessage("不允许删除的测试");
        }
        if (newExam.getDeletedAt() != null || newExam.getStatus() != NewExamStatus.ONLINE) {
            return MapMessage.errorMessage("测试已经被删除");
        }
        Long groupId = newExam.getGroupId();
        boolean hasPermission = teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), groupId);
        if (!hasPermission) {
            return MapMessage.errorMessage("没有删除权限");
        }
        newExam.setStatus(NewExamStatus.OFFLINE);
        newExam.setDeletedAt(new Date());
        newExamLoaderClient.save(newExam);
        return MapMessage.successMessage();
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapMessage loadAppIndexData(Teacher teacher) {
        return MapMessage.errorMessage("功能已下线");
    }

    @Override
    public MapMessage shareIndependentReport(Teacher teacher, String newExamId) {
        return MapMessage.errorMessage("功能已下线");
    }

    @Override
    public MapMessage restoreData(List<String> newExamResultIds) {

        Map<String, NewExamResult> newExamResultMap = newExamResultDao.loads(newExamResultIds);
        //可能需要修改的学生数据
        List<NewExamResult> newExamResults = new LinkedList<>();
        //processIDs
        List<String> newExamProcessResultIds = new LinkedList<>();
        for (NewExamResult newExamResult : newExamResultMap.values()) {
            if (newExamResult == null || newExamResult.getCorrectScore() == null || MapUtils.isEmpty(newExamResult.getAnswers())) {
                continue;
            }
            newExamResults.add(newExamResult);
            newExamProcessResultIds.addAll(newExamResult.getAnswers().values());
        }
        Map<String, NewExamProcessResult> newExamProcessResultMap = newExamProcessResultDao.loads(newExamProcessResultIds);
        for (NewExamResult newExamResult : newExamResults) {
            double correctScore = 0;
            for (String processId : newExamResult.getAnswers().values()) {
                if (newExamProcessResultMap.containsKey(processId)) {
                    NewExamProcessResult n = newExamProcessResultMap.get(processId);
                    correctScore += n.processScore();
                }
            }
            //判断分数是否一致，不一致需要修改
            if (correctScore != newExamResult.getCorrectScore()) {
                newExamResultDao.correctNewExam(newExamResult.getId(), correctScore);
                newExamRegistrationDao.correctNewExam(newExamResult.getId(), correctScore);
                logger.info("fix correctScore" + newExamResult.getId());
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 查询某次考试学生人数
     *
     * @param newExamId 考试ID
     * @return 学生数
     */
    public Integer loadNewExamStudentCount(String newExamId) {
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return 0;
        }
        List<String> newExamRegistrationIdList = newExamRegistrationDao.findByNewExam(newExam);
        return newExamRegistrationIdList.size();
    }

    @Override
    public MapMessage resetNewExamResultScore(String newExamResultId, Double score, Double correctScore) {
        NewExamResult newExamResult = newExamResultDao.load(newExamResultId);
        if (newExamResult != null && newExamResult.getSubmitAt() != null) {
            newExamResult.setScore(score);
            newExamResult.setCorrectScore(correctScore);
            newExamResultDao.update(newExamResultId, newExamResult);
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage("考试结果不存在或未交卷");
    }

    @Override
    public MapMessage loadUnitTestPaperList(String unitId, Long teacherId) {
        //调用科学院接口，获取试卷IDs
        AlpsHttpResponse response = getUnitTestPaperIds(unitId, teacherId);
        List<UnitTestPaperInfo> unitTestPaperInfos = new LinkedList<>();
        if (response != null) {
            UnitTestPaperIdInfo unitTestPaperIdInfo = JsonUtils.fromJson(response.getResponseString(), UnitTestPaperIdInfo.class);
            if (unitTestPaperIdInfo != null) {
                List<String> classTestPids = (List<String>)unitTestPaperIdInfo.getData().get("class_test_pids");
                List<String> unitTestPids = (List<String>)unitTestPaperIdInfo.getData().get("unit_test_pids");
                List<String> paperIds = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(classTestPids)) {
                    paperIds.addAll(classTestPids);
                }
                if (CollectionUtils.isNotEmpty(unitTestPids)) {
                    paperIds.addAll(unitTestPids);
                }
                Map<String, NewPaper> paperMap = loadNewPapersByDocIdsIncludeDisable(paperIds);
                //For 计算试卷题目数量(试卷中可能存在复合题，全部题数需要算到小题)
                Set<String> qids = paperMap.values()
                        .stream()
                        .map(NewPaper::getQuestions)
                        .flatMap(Collection::stream)
                        .map(NewPaperQuestion::getId)
                        .collect(Collectors.toSet());
                Map<String, NewQuestion> newQuestionMap = tikuStrategy.loadQuestionsIncludeDisabled(qids);

                UnitTestPaperInfo unitTestPaperInfo;
                //单元测
                unitTestPaperInfo = new UnitTestPaperInfo();
                unitTestPaperInfo.setPaperTypeId(25);
                unitTestPaperInfo.setPaperType("单元检测");
                if (CollectionUtils.isNotEmpty(unitTestPids)) {
                    processUnitTestPaperInfos(unitTestPids, paperMap, unitTestPaperInfo, newQuestionMap);
                }
                unitTestPaperInfos.add(unitTestPaperInfo);

                //课时小测
                unitTestPaperInfo = new UnitTestPaperInfo();
                unitTestPaperInfo.setPaperTypeId(26);
                unitTestPaperInfo.setPaperType("课时小测");
                if (CollectionUtils.isNotEmpty(classTestPids)) {
                    //试卷需要按照绑定课时顺序排序
                    List<NewBookCatalog> sectionList = newContentLoaderClient.loadChildrenSingle(unitId, BookCatalogType.SECTION);
                    List<String> sortPids = new LinkedList<>();

                    //获取课时小测的paper信息，Map<String课时ID, List<String>试卷IDs>
                    Map<String, Set<String>> sectionPaperIdMap = new HashMap<>();
                    for (String classTestPid : classTestPids) {
                        NewPaper newPaper = paperMap.get(classTestPid);
                        if ( newPaper == null) {
                            continue;
                        }
                        NewPaperBookNew newPaperBookNew = newPaper.getBooksNew().iterator().next();
                        String sectionId = newPaperBookNew.getSectionId();
                        Set<String> pIds = sectionPaperIdMap.getOrDefault(sectionId, new HashSet<>());
                        pIds.add(classTestPid);
                        sectionPaperIdMap.put(sectionId, pIds);
                    }

                    if (CollectionUtils.isNotEmpty(sectionList)) {
                        sectionList.forEach(s -> {
                            if (CollectionUtils.isNotEmpty(sectionPaperIdMap.get(s.getId()))) {
                                sortPids.addAll(sectionPaperIdMap.get(s.getId()));
                            }
                        });
                    }
                    processUnitTestPaperInfos(sortPids, paperMap, unitTestPaperInfo, newQuestionMap);
                }
                unitTestPaperInfos.add(unitTestPaperInfo);
            }
        }
        return MapMessage.successMessage().add("unitTestPaperInfos", unitTestPaperInfos);
    }

    private void processUnitTestPaperInfos(List<String> unitTestPids, Map<String, NewPaper> paperMap, UnitTestPaperInfo unitTestPaperInfo, Map<String, NewQuestion> newQuestionMap) {
        List<UnitTestPaperInfo.PaperInfo> unitPaperInfos = new LinkedList<>();
        for (String unitTestPid : unitTestPids) {
            NewPaper newPaper = paperMap.get(unitTestPid);
            if (newPaper == null || newPaper.getExamTime() == null) {
                continue;
            }
            //题目数量计算
            int questionNum = 0;
            for (NewPaperParts parts : newPaper.getParts()) {
                if (CollectionUtils.isEmpty(parts.getQuestions())) {
                    continue;
                }
                for (NewPaperQuestion question : parts.getQuestions()) {
                    if (!newQuestionMap.containsKey(question.getId())) {
                        continue;
                    }
                    NewQuestion newQuestion = newQuestionMap.get(question.getId());
                    if (newQuestion.getContent() == null) {
                        continue;
                    }
                    if (CollectionUtils.isEmpty(newQuestion.getContent().getSubContents())) {
                        continue;
                    }
                    questionNum += newQuestion.getContent().getSubContents().size();
                }
            }

            UnitTestPaperInfo.PaperInfo paperInfo = new UnitTestPaperInfo.PaperInfo();
            paperInfo.setPaperId(newPaper.getId());
            paperInfo.setPaperName(newPaper.getTitle());
            paperInfo.setQuestionNum(questionNum);
            paperInfo.setPaperTime(newPaper.getExamTime() );
            paperInfo.setMinutes(newPaper.getExamTime() / 3);
            paperInfo.setDescription(newPaper.getDescription());
            paperInfo.setAuthor(newPaper.getAuthor());
            paperInfo.setDifficulty(newPaper.getDifficultyInt());
            paperInfo.setPreviewUrl(UrlUtils.buildUrlQuery("/view/newexamv2/assign_preview", MapUtils.m("paperId", newPaper.getId(),"subject", Subject.fromSubjectId(newPaper.getSubjectId()))));
            unitPaperInfos.add(paperInfo);
        }
        unitTestPaperInfo.setPapers(unitPaperInfos);
    }

    private AlpsHttpResponse getUnitTestPaperIds(String unitId, Long teacherId) {
        // 学习工程院：接口地址
        String requestUrl;
        if (RuntimeMode.current().le(Mode.TEST)) {
            requestUrl = NewExamConstants.UNIT_TEST_PAPER_IDS_TEST_URL;
        } else if(RuntimeMode.current().le(Mode.STAGING)) {
            requestUrl = NewExamConstants.UNIT_TEST_PAPER_IDS_STAGING_URL;
        } else {
            requestUrl = NewExamConstants.UNIT_TEST_PAPER_IDS_PRODUCT_URL;
        }
        String url = UrlUtils.buildUrlQuery(requestUrl, MapUtils.map("unit_id", unitId));
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                .get(url)
                .execute();

        if (response == null || response.getStatusCode() != 200) {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", teacherId,
                    "mod1", unitId,
                    "op", "unit test paper ids"
            ));
            logger.error("调用:{}失败, httpParams:{}, response: {}",
                    requestUrl,
                    unitId,
                    response != null ? response.getResponseString() : "");
            return null;
        }
        return response;
    }

    @Getter
    @Setter
    private static class UnitTestPaperIdInfo implements Serializable {
        private Map<String, Object> data;
    }

    @Override
    public MapMessage previewUnitTest(String paperId) {
        NewPaper newPaper = paperLoaderClient.loadPaperIncludeDisabled(paperId);
        if (newPaper == null) {
            return MapMessage.errorMessage("试卷不存在");
        }
        List<NewPaperParts> paperParts = newPaper.getParts();
        if (CollectionUtils.isEmpty(paperParts)) {
            return MapMessage.errorMessage("试卷信息错误");
        }
        Set<String> qids = newPaper.getQuestions()
                .stream()
                .map(XxBaseQuestion::getId)
                .collect(Collectors.toSet());
        Map<String, NewQuestion> newQuestionMap = tikuStrategy.loadQuestionsIncludeDisabled(qids);

        List<Map<String, Object>> modules = new ArrayList<>();
        int index = 1;//按照小题的顺序号
        for (NewPaperParts parts : newPaper.getParts()) {
            List<NewExamThemeForSub.SubQuestion> subQuestions = new LinkedList<>();
            //一个模块的对于数据结构
            if (CollectionUtils.isNotEmpty(parts.getQuestions())) {
                for (NewPaperQuestion question : parts.getQuestions()) {
                    if (newQuestionMap.containsKey(question.getId())) {
                        NewQuestion newQuestion = newQuestionMap.get(question.getId());
                        if (newQuestion.getContent() == null) {
                            continue;
                        }
                        if (CollectionUtils.isEmpty(newQuestion.getContent().getSubContents())) {
                            continue;
                        }
                        double standardScore = new BigDecimal(SafeConverter.toDouble(question.getScore(), 0.0))
                                .divide(new BigDecimal(newQuestion.getContent().getSubContents().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        int subIndex = 0;//题里面第几小题
                        for (NewQuestionsSubContents ignored : newQuestion.getContent().getSubContents()) {
                            //小题的数据结构
                            NewExamThemeForSub.SubQuestion subQuestion = new NewExamThemeForSub.SubQuestion();
                            subQuestion.setQid(question.getId());
                            subQuestion.setIndex(index);
                            subQuestion.setSubIndex(subIndex);
                            subQuestion.setStandardScore(standardScore);
                            subQuestions.add(subQuestion);
                            subIndex++;
                            index++;
                        }
                    }
                }
            }
            Map<String, Object> module = new HashMap<>();
            module.put("moduleName", parts.getTitle());
            module.put("description", parts.getDescription());
            module.put("questionCount", subQuestions.size());
            module.put("subQuestions", subQuestions);
            modules.add(module);
        }

        return MapMessage.successMessage()
                .add("paperId", newPaper.getId())
                .add("paperName", newPaper.getTitle())
                .add("questionCount", modules.stream().mapToInt(m -> SafeConverter.toInt(m.get("questionCount"))).sum())
                .add("minutes", newPaper.getExamTime() / 3)
                .add("paperTime", newPaper.getExamTime())
                .add("totalScore", newPaper.getTotalScore())
                .add("paperTypeId", newPaper.getPaperTypes().iterator().next())
                .add("paperType", newPaper.getPaperTypes().iterator().next() == 25 ? "单元检测" : "课时小测")
                .add("difficulty", newPaper.getDifficultyInt())
                .add("previewUrl", UrlUtils.buildUrlQuery("/view/newexamv2/assign_preview", MapUtils.m("paperId", newPaper.getId(),"subject", Subject.fromSubjectId(newPaper.getSubjectId()))))
                .add("description", newPaper.getDescription())
                .add("modules", modules);
    }

    @Override
    public MapMessage assignUnitTest(Teacher teacher, Map<String, Object> source) {
        if (MapUtils.isEmpty(source)) {
            return MapMessage.errorMessage("测试内容错误");
        }
        String[] paperIds = StringUtils.split(SafeConverter.toString(source.get("paperIds")), ",");
        Set<String> paperIdSet = new LinkedHashSet<>();
        for (String paperIdStr : paperIds) {
            String paperId = SafeConverter.toString(paperIdStr);
            if (StringUtils.isNotBlank(paperId)) {
                paperIdSet.add(paperId);
            }
        }
        if (CollectionUtils.isEmpty(paperIdSet)) {
            return MapMessage.errorMessage("试卷id为空");
        }
        Map<String, NewPaper> paperMap = paperLoaderClient.loadPaperAsMapIncludeDisabled(paperIdSet);
        if (MapUtils.isEmpty(paperMap)) {
            return MapMessage.errorMessage("试卷不存在");
        }
        if (paperMap.size() != paperIdSet.size()) {
            Set<String> nonExistPaperIds = new HashSet<>();
            for (String pId : paperIdSet) {
                if (paperMap.get(pId) == null) {
                    nonExistPaperIds.add(pId);
                }
            }
            if (CollectionUtils.isNotEmpty(nonExistPaperIds)) {
                return MapMessage.errorMessage(nonExistPaperIds.toString()+ "试卷不存在");
            }
        }

        Date startTime = SafeConverter.toDate(source.get("startTime"));
        if (startTime == null) {
            return MapMessage.errorMessage("检测开始时间错误");
        }
        Date endTime = SafeConverter.toDate(source.get("endTime"));
        Date currentDate = new Date();
        if (endTime == null) {
            return MapMessage.errorMessage("检测结束时间错误");
        }
        if (endTime.before(currentDate)) {
            return MapMessage.errorMessage("检测结束时间不能早于当前时间");
        }
        if (startTime.after(endTime)) {
            return MapMessage.errorMessage("检测开始时间不能晚于结束时间");
        }

        String[] groupIds = StringUtils.split(SafeConverter.toString(source.get("groupIds")), ",");
        Set<Long> groupIdSet = new LinkedHashSet<>();
        for (String groupIdStr : groupIds) {
            long groupId = SafeConverter.toLong(groupIdStr);
            if (groupId != 0) {
                groupIdSet.add(groupId);
            }
        }
        if (CollectionUtils.isEmpty(groupIdSet)) {
            return MapMessage.errorMessage("班组id错误");
        }
        Subject subject = Subject.MATH;
        Long realTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), subject);
        Teacher realTeacher = teacherLoaderClient.loadTeacher(realTeacherId);
        if (realTeacherId == null || realTeacher == null) {
            return MapMessage.errorMessage("账号信息错误");
        }
        List<GroupTeacherMapper> groupList = groupLoaderClient.loadTeacherGroups(realTeacherId, false);
        Set<Long> teacherGroupIds = groupList.stream()
                .map(GroupMapper::getId)
                .collect(Collectors.toSet());
        Map<Long, Subject> groupSubjectMap = groupList
                .stream()
                .collect(Collectors.toMap(GroupMapper::getId, GroupMapper::getSubject));
        for (Long groupId : groupIdSet) {
            if (!teacherGroupIds.contains(groupId)) {
                return MapMessage.errorMessage("没有班组" + groupId + "的操作权限");
            }
            if (subject != groupSubjectMap.get(groupId)) {
                return MapMessage.errorMessage("班组" + groupId + "学科与试卷学科不匹配");
            }
        }
        Long studentId = null;
        boolean showScoreLevel = false;
        Map<Long, List<Long>> groupStudentIds = studentLoaderClient.loadGroupStudentIds(groupIdSet);
        if (MapUtils.isNotEmpty(groupStudentIds)) {
            for (List<Long> studentIds : groupStudentIds.values()) {
                if (CollectionUtils.isNotEmpty(studentIds)) {
                    studentId = studentIds.iterator().next();
                    break;
                }
            }
        }
        if (studentId != null) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail != null) {
                showScoreLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
            }
        }

        List<NewExam> newExams = new ArrayList<>();
        for (Long groupId : groupIdSet) {
            for (NewPaper paper : paperMap.values()) {
                NewExam newExam = new NewExam();
                newExam.setName(paper.getTitle());
                newExam.setExamType(NewExamType.independent);

                NewExam.EmbedPaper embedPaper = new NewExam.EmbedPaper();
                embedPaper.setPaperId(paper.getDocId());
                embedPaper.setPaperName("A卷");
                newExam.setPapers(Collections.singletonList(embedPaper));
                newExam.setSubjectId(subject.getId());
                newExam.setCorrectStopAt(endTime);
                newExam.setExamStartAt(startTime);
                newExam.setExamStopAt(endTime);
                newExam.setResultIssueAt(endTime);
                newExam.setDurationMinutes(SafeConverter.toInt(paper.getExamTime()));
                newExam.setSubmitAfterMinutes(0);
                newExam.setStatus(NewExamStatus.ONLINE);
                newExam.setCreatedAt(currentDate);
                newExam.setUpdatedAt(currentDate);
                newExam.setTestCategory(2);
                newExam.setOralRepeatCount(-1);
                newExam.setGroupId(groupId);
                newExam.setTeacherId(realTeacherId);
                newExam.setGradeType(showScoreLevel ? 1 : 0);
                newExam.setSchoolLevel(SchoolLevel.JUNIOR);
                if (showScoreLevel) {
                    newExam.setRanks(NewExam.EmbedRank.defaultRanks);
                }
                newExams.add(newExam);
            }
        }
        newExams = newExamLoaderClient.inserts(newExams);
        // 发送广播
        assignUnitTestPublish(newExams, NewExamPublishMessageType.assignIndependent);
        // 发送学生端jpush
        sendUnitTestAssignMessageToStudent(teacher, paperMap, currentDate, groupStudentIds, newExams);
        // 发送家校群消息
        sendUnitTestAssignMessageToChatGroup(teacher, realTeacher, newExams);
        // 发送老师端报告jpush
        sendUnitTestAssignMessageToTeacher(teacher, groupIdSet, newExams);
        // 上报
        toAvenger(newExams);
        return MapMessage.successMessage().add("examIds", newExams.stream().map(NewExam::getId).collect(Collectors.toList()));
    }

    private void toAvenger(List<NewExam> NewExams) {
        for (NewExam newExam : NewExams) {
            asyncAvengerNewExamService.informNewExamToBigData(newExam);
        }
    }

    private void sendUnitTestAssignMessageToTeacher(Teacher teacher, Set<Long> groupIdSet, List<NewExam> newExams) {
        Map<Long, GroupMapper> groupMappers = groupLoaderClient.loadGroups(groupIdSet, false);
        Set<Long> clazzIdSet = groupMappers.values().stream().map(GroupMapper::getClazzId).collect(Collectors.toSet());
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIdSet)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        List<Long> relTeacherIds = new ArrayList<>(teacherLoaderClient.loadRelTeacherIds(teacher.getId()));
        for (NewExam newExam : newExams) {
            String link = UrlUtils.buildUrlQuery("/view/evaluation/report", MapUtils.m("examId", newExam.getId()));
            String clazzName = "";
            GroupMapper groupMapper = groupMappers.get(newExam.getGroupId());
            if (groupMapper != null) {
                Clazz clazz = clazzMap.get(groupMapper.getClazzId());
                if (clazz != null) {
                    clazzName = clazz.formalizeClazzName();
                }
            }
            String content = "老师好，已发布" + clazzName + newExam.getName() + "的测评报告，立即前往查看";
            Map<String, Object> extInfo = MapUtils.m("link", link, "key", "j", "t", "h5", "s", TeacherMessageType.ACTIVIY.getType());
            appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.PRIMARY_TEACHER, relTeacherIds, extInfo, newExam.getExamStopAt().getTime());
        }
    }

    private void sendUnitTestAssignMessageToChatGroup(Teacher teacher, Teacher realTeacher, List<NewExam> newExams) {
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacher.getId());
        if (mainTeacherId == null) {
            mainTeacherId = teacher.getId();
        }
        for (NewExam newExam : newExams) {
            sendAssignMessageToChatGroup(newExam, mainTeacherId, realTeacher);
        }
    }

    private void sendUnitTestAssignMessageToStudent(Teacher teacher, Map<String, NewPaper> paperMap, Date currentDate, Map<Long, List<Long>> groupStudentIds, List<NewExam> newExams) {
        for (NewExam newExam : newExams) {
            String link = UrlUtils.buildUrlQuery("/studentMobile/homework/app/newexam.vpage", MapUtils.m("newExamId", newExam.getId()));
            List<Long> studentIds = groupStudentIds.get(newExam.getGroupId());
            String content = "单元考试已开始，请同学们按时完成考试！";
            Map<String, Object> extInfo = MapUtils.m("s", StudentAppPushType.NEW_EXAM_REMIND.getType(), "link", link, "t", "h5", "key", "j", "title", StudentAppPushType.NEW_EXAM_REMIND.getDescription());
            // 判断考试开始时间是否早于当前时间
            if (currentDate.before(newExam.getExamStartAt())) {
                appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.STUDENT, studentIds, extInfo, newExam.getExamStartAt().getTime());
            } else {
                appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.STUDENT, studentIds, extInfo);
            }
        }
        for (NewPaper paper : paperMap.values()) {
            String cacheKey = StringUtils.formatMessage(CACHE_KEY_TEMPLATE, teacher.getId(), paper.getDocId());
            newExamCacheClient.cacheSystem.CBS.flushable.set(cacheKey, 0, true);
        }
    }

    private void assignUnitTestPublish(List<NewExam> newExams, NewExamPublishMessageType newExamPublishMessageType) {
        for (NewExam newExam : newExams) {
            Map<String, Object> map = new HashMap<>();
            map.put("messageType", newExamPublishMessageType);
            map.put("id", newExam.getId());
            map.put("name", newExam.getName());
            map.put("paperId", newExam.getPapers().iterator().next().getPaperId());
            map.put("examType", newExam.getExamType());
            map.put("regionLevel", newExam.getRegionLevel());
            map.put("regions", newExam.getRegions());
            map.put("schoolIds", newExam.getSchoolIds());
            map.put("subjectId", newExam.getSubjectId());
            map.put("correctStopAt", newExam.getCorrectStopAt());
            map.put("examStartAt", newExam.getExamStartAt());
            map.put("examStopAt", newExam.getExamStopAt());
            map.put("resultIssueAt", newExam.getResultIssueAt());
            map.put("durationMinutes", newExam.getDurationMinutes());
            map.put("submitAfterMinutes", newExam.getSubmitAfterMinutes());
            map.put("status", newExam.getStatus());
            map.put("createdAt", newExam.getCreatedAt());
            map.put("updatedAt", newExam.getUpdatedAt());
            map.put("deletedAt", newExam.getDeletedAt());
            map.put("agentId", newExam.getAgentId());
            map.put("agentCode", newExam.getAgentCode());
            map.put("agentName", newExam.getAgentName());
            map.put("fileUrl", newExam.getFileUrl());
            map.put("fileName", newExam.getFileName());
            newExamPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
        }
    }

    @Override
    public MapMessage adjustUnitTest(Long teacherId, String newExamId, Date end) {
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("考试不存在");
        }
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacherId, newExam.getGroupId())) {
            return MapMessage.errorMessage("您没有权限调整此考试");
        }
        if (newExam.getDeletedAt() != null || newExam.getStatus() != NewExamStatus.ONLINE) {
            return MapMessage.errorMessage("考试已经被删除");
        }

        try {
            newExam.setExamStopAt(end);
            newExam.setResultIssueAt(end);
            newExamLoaderClient.save(newExam);
            assignUnitTestPublish(Collections.singletonList(newExam), NewExamPublishMessageType.adjustIndependent);
            return MapMessage.successMessage("考试调整成功");
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @Override
    public MapMessage loadStudentUnitTestHistoryList(StudentDetail studentDetail) {
        List<GroupMapper>  groups = groupLoaderClient.loadStudentGroups(studentDetail.getId(), false);
        Set<Long> groupIds = groups.stream().map(GroupMapper::getId).collect(Collectors.toSet());
        Map<Long, List<NewExam>> newExamMap = newExamLoaderClient.loadByGroupIds(groupIds);
        //根据时间过滤历史数据:时间
        List<NewExam> newExamList = newExamMap.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(n -> n.getCreatedAt().after(NewExamConstants.UNIT_TEST_DUE_DATE))
                .collect(Collectors.toList());
        // 报名考试
        Set<String> registrationIdSet = newExamList.stream()
                .map(newExam -> generateNewExamRegistrationId(studentDetail, newExam))
                .collect(Collectors.toSet());
        Map<String, NewExamRegistration> newExamRegistrationMap = newExamRegistrationLoader.loadByIds(registrationIdSet)
                .values().stream().filter(nr -> !SafeConverter.toBoolean(nr.getBeenCanceled())).collect(Collectors.toMap(NewExamRegistration::getNewExamId, Function.identity()));

        Date currentDate = new Date();
        //可能补考重考的试卷
        List<NewExam> stopNewExam = newExamList.stream()
                .filter(newExam -> newExam.getExamStopAt() != null)
                //考试结束
                .filter(newExam -> newExam.getExamStopAt().before(currentDate))
                .filter(newExam -> newExam.getCorrectStopAt() != null)
                //批改时间没有结束
                .filter(newExam -> newExam.getCorrectStopAt().after(currentDate))
                .collect(Collectors.toList());
        Map<String, String> registrationIdMap = stopNewExam.stream()
                .collect(Collectors.toMap(NewExam::getId, newExam -> generateNewExamRegistrationId(studentDetail, newExam)));
        Map<String, StudentExaminationAuthority> stringStudentExaminationAuthorityMap = studentExaminationAuthorityDao.loads(registrationIdMap.values())
                .values()
                .stream()
                .filter(o -> !SafeConverter.toBoolean(o.getDisabled()))
                .collect(Collectors.toMap(StudentExaminationAuthority::getId, Function.identity()));

        Map<String, String> examIdToPaperIdMap = newExamList.stream()
                .collect(Collectors.toMap(NewExam::getId, o -> o.fetchPaperId(studentDetail.getId())));
        Map<String, NewPaper> paperMap = loadNewPapersByDocIdsIncludeDisable(examIdToPaperIdMap.values());
        List<Map<String, Object>> exams = newExamList.stream()
                .sorted(new NewExam.NewExamComparator())
                .map(newExam -> {
                    StudentExaminationAuthority studentExaminationAuthority = null;
                    if (registrationIdMap.containsKey(newExam.getId()) && stringStudentExaminationAuthorityMap.containsKey(registrationIdMap.get(newExam.getId()))) {
                        studentExaminationAuthority = stringStudentExaminationAuthorityMap.get(registrationIdMap.get(newExam.getId()));
                    }
                    return convertNewExamToMap(studentExaminationAuthority, newExam, newExamRegistrationMap.get(newExam.getId()), paperMap.get(examIdToPaperIdMap.get(newExam.getId())));
                })
                .collect(Collectors.toList());
        return MapMessage.successMessage().add("newExamList", exams);
    }

    @Override
    public MapMessage loadStudentIndexUnitTestList(StudentDetail studentDetail) {
        List<GroupMapper>  groups = groupLoaderClient.loadStudentGroups(studentDetail.getId(), false);
        Set<Long> groupIds = groups.stream().map(GroupMapper::getId).collect(Collectors.toSet());
        Map<Long, List<NewExam>> newExamGroupMap = newExamLoaderClient.loadByGroupIds(groupIds);
        Date currentDate = new Date();
        //根据时间过滤历史数据:时间
        List<NewExam> newExams = newExamGroupMap.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(n -> n.getCreatedAt().after(NewExamConstants.UNIT_TEST_DUE_DATE))
                .filter(n -> n.getExamStopAt().after(currentDate))
                .collect(Collectors.toList());

        List<String> newExamResultIds = new ArrayList<>();
        for (NewExam newExam : newExams) {
            String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
            NewExamResult.ID id = new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), studentDetail.getId().toString());
            newExamResultIds.add(id.toString());
        }
        Map<String, NewExamResult> newExamResultMap = newExamResultLoader.loadNewExamResults(newExamResultIds);
        List<NewExam> unSubmitExamList = newExams
                .stream()
                .filter(e -> {
                    //过滤掉已交卷的考试
                    String month = MonthRange.newInstance(e.getCreatedAt().getTime()).toString();
                    NewExamResult.ID id = new NewExamResult.ID(month, e.getSubject(), e.getId(), studentDetail.getId().toString());
                    NewExamResult newExamResult = newExamResultMap.get(id.toString());
                    return newExamResult == null || newExamResult.getSubmitAt() == null;
                })
                .collect(Collectors.toList());

        Set<String> paperIds = unSubmitExamList
                .stream()
                .map(NewExam::getPapers)
                .flatMap(Collection::stream)
                .map(NewExam.EmbedPaper::getPaperId)
                .collect(Collectors.toSet());
        Map<String, NewPaper> newPaperMap = loadNewPapersByDocIdsIncludeDisable(paperIds);

        //学生报名答题状态信息
        Set<String> registrationIdSet = unSubmitExamList.stream()
                .map(newExam -> generateNewExamRegistrationId(studentDetail, newExam))
                .collect(Collectors.toSet());
        Map<String, NewExamRegistration> newExamRegistrationMap = newExamRegistrationLoader.loadByIds(registrationIdSet)
                .values()
                .stream()
                .filter(nr -> !SafeConverter.toBoolean(nr.getBeenCanceled()))
                .collect(Collectors.toMap(NewExamRegistration::getNewExamId, Function.identity()));

        List<Map<String, Object>> unitTestList = new ArrayList<>();
        for (NewExam newExam : unSubmitExamList) {
            NewExam.EmbedPaper embedPaper = newExam.getPapers().iterator().next();
            NewPaper newPaper = newPaperMap.get(embedPaper.getPaperId());
            if (newPaper == null) {
                continue;
            }

            String status;
            NewExamRegistration newExamRegistration = newExamRegistrationMap.get(newExam.getId());
            if (newExam.getExamStartAt().after(currentDate)) {
                status = "undo";
            } else {
                status = "todo";
            }

            if (newExamRegistration != null) {
                //已经交卷的过滤
                if (newExamRegistration.getSubmitAt() != null) {
                    continue;
                }
                String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
                NewExamResult.ID id = new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), studentDetail.getId().toString());
                NewExamResult newExamResult = newExamResultMap.get(id.toString());
                if (newExamResult == null) {
                    status = "todo";
                } else {
                    status = "doing";
                }
            }

            Map<String, Object> contents = new HashMap<>();
            if (newPaper.getPaperTypes().contains(25)) {
                contents = MapUtils.m("paperType", 25,
                        "typeName", "单元检测",
                        "title", newPaper.getTitle(),
                        "examId", newExam.getId(),
                        "subject", Subject.fromSubjectId(newPaper.getSubjectId()),
                        "examStartAt", newExam.getExamStartAt().getTime(),
                        "examStopAt", newExam.getExamStopAt().getTime(),
                        "status", status,
                        "questionNum", newPaper.getQuestions().size());
            }
            if (newPaper.getPaperTypes().contains(26)) {
                contents = MapUtils.m("paperType", 26,
                        "typeName", "课时小测",
                        "title", newPaper.getTitle(),
                        "examId", newExam.getId(),
                        "subject", Subject.fromSubjectId(newPaper.getSubjectId()),
                        "examStartAt", newExam.getExamStartAt().getTime(),
                        "examStopAt", newExam.getExamStopAt().getTime(),
                        "status", status,
                        "questionNum", newPaper.getQuestions().size());
            }
            unitTestList.add(contents);
        }
        return MapMessage.successMessage().add("unitTestList", unitTestList);
    }
}