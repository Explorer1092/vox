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

package com.voxlearning.utopia.service.newhomework.impl.loader;


import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.core.helper.VoiceEngineTypeUtils;
import com.voxlearning.utopia.mapper.ScoreCircleQueueCommand;
import com.voxlearning.utopia.service.content.api.entity.*;
import com.voxlearning.utopia.service.content.client.PracticeServiceClient;
import com.voxlearning.utopia.service.content.consumer.ChineseContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.EnglishContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.PracticeLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.VacationHomeworkReportLoader;
import com.voxlearning.utopia.service.newhomework.api.constant.NatureSpellingType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSyntheticHistory;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.readrecitewithscore.ParagraphDetailed;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.vacationhomework.CategoryHandlerContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.vacationhomework.ReportRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkDetailCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkWinterPlanCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.report.ExercisesData;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.report.ExercisesQuestionData;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.report.VacationReportToSubject;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.consumer.cache.RemindStudentVacationProgressCacheManager;
import com.voxlearning.utopia.service.newhomework.consumer.cache.ShareVacationReportCacheManager;
import com.voxlearning.utopia.service.newhomework.consumer.cache.ShareWeiXinVacationReportCacheManager;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkPackageDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkProcessResultDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.pubsub.NewHomeworkPublisher;
import com.voxlearning.utopia.service.newhomework.impl.queue.NewHomeworkParentQueueProducer;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkCacheServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkContentServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkQuestionFileHelper;
import com.voxlearning.utopia.service.newhomework.impl.template.vacation.VacationProcessAppDetailByCategoryIdFactory;
import com.voxlearning.utopia.service.newhomework.impl.template.vacation.VacationProcessAppDetailByCategoryIdTemplate;
import com.voxlearning.utopia.service.newhomework.impl.template.vacation.VacationProcessNewHomeworkAnswerDetailFactory;
import com.voxlearning.utopia.service.newhomework.impl.template.vacation.VacationProcessNewHomeworkAnswerDetailTemplate;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.consumer.DubbingLoaderClient;
import com.voxlearning.utopia.service.question.consumer.PictureBookLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionContentTypeLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = VacationHomeworkReportLoader.class)
@ExposeService(interfaceClass = VacationHomeworkReportLoader.class)
public class VacationHomeworkReportLoaderImpl extends SpringContainerSupport implements VacationHomeworkReportLoader {

    @Inject private RaikouSystem raikouSystem;

    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private ChineseContentLoaderClient chineseContentLoaderClient;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private DubbingLoaderClient dubbingLoaderClient;
    @Inject private EnglishContentLoaderClient englishContentLoaderClient;
    @Inject private NewContentLoaderClient newContentLoaderClient;
    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;
    @Inject private NewHomeworkContentServiceImpl newHomeworkContentServiceImpl;
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private NewHomeworkParentQueueProducer newHomeworkParentQueueProducer;
    @Inject private NewHomeworkPublisher newHomeworkPublisher;
    @Inject private PictureBookLoaderClient pictureBookLoaderClient;
    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;
    @Inject private PracticeLoaderClient practiceLoaderClient;
    @Inject private PracticeServiceClient practiceServiceClient;
    @Inject private QuestionContentTypeLoaderClient questionContentTypeLoaderClient;
    @Inject private QuestionLoaderClient questionLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private VacationHomeworkCacheLoaderImpl vacationHomeworkCacheLoader;
    @Inject private VacationHomeworkDao vacationHomeworkDao;
    @Inject private VacationHomeworkLoaderImpl vacationHomeworkLoader;
    @Inject private VacationHomeworkPackageDao vacationHomeworkPackageDao;
    @Inject private VacationHomeworkProcessResultDao vacationHomeworkProcessResultDao;
    @Inject private VacationHomeworkResultDao vacationHomeworkResultDao;
    @Inject private VacationProcessAppDetailByCategoryIdFactory processAppDetailByCategoryIdFactory;
    @Inject private VacationProcessNewHomeworkAnswerDetailFactory vacationProcessNewHomeworkAnswerDetailFactory;

    @Inject private RaikouSDK raikouSDK;

    @Override
    public List<VacationReportForParent> loadVacationReportForParent(Long studentId) {
        try {
            Set<Long> groupIds = raikouSystem.loadStudentGroups(studentId)
                    .stream()
                    .filter(Objects::nonNull)
                    .map(Group::getId)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(groupIds)) {
                return Collections.emptyList();
            }
            //每一份寒假作业
            Map<Long, List<VacationHomeworkPackage.Location>> vacationHomeworkPackageLocationMap = vacationHomeworkPackageDao.loadVacationHomeworkPackageByClazzGroupIds(groupIds);

            return vacationHomeworkPackageLocationMap.values()
                    .stream()
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .map(o -> {
                        VacationHomeworkCacheMapper cacheMapper = vacationHomeworkCacheLoader.loadVacationHomeworkCacheMapper(o.getClazzGroupId(), studentId);
                        boolean begin = false;
                        boolean finish = false;
                        String endTime = null;
                        int finishedVacationHomework = 0;
                        if (cacheMapper != null) {
                            begin = true;
                            finish = cacheMapper.isFinished();
                            finishedVacationHomework = cacheMapper.getFinishPackageCount();
                        }
                        int totalHomeworkNum = SafeConverter.toInt(o.getPlannedDays());
                        if (o.getEndTime() != 0) {
                            endTime = DateUtils.dateToString(new Date(o.getEndTime()), "yyyy年MM月dd日 HH:mm");
                        }
                        VacationReportForParent vacationReportForParent = new VacationReportForParent();
                        vacationReportForParent.setSubject(o.getSubject());
                        vacationReportForParent.setTotalHomeworkNum(totalHomeworkNum);
                        vacationReportForParent.setBegin(begin);
                        vacationReportForParent.setFinish(finish);
                        vacationReportForParent.setLocation(o);
                        vacationReportForParent.setEndTime(endTime);
                        vacationReportForParent.setFinishedVacationHomework(finishedVacationHomework);
                        return vacationReportForParent;
                    })
                    .sorted((o1, o2) -> {
                        if (o1.getSubject() == Subject.MATH && o2.getSubject() == Subject.ENGLISH) {
                            return -1;
                        } else if (o2.getSubject() == Subject.MATH && o1.getSubject() == Subject.ENGLISH) {
                            return 1;
                        }
                        return 0;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("load VacationReport For Parent failed : sid {}", studentId, e);
            return Collections.emptyList();
        }
    }


    @Override
    public List<VacationReportToSubject> loadVacationReportToSubject(Long studentId) {
        if (SafeConverter.toLong(studentId) <= 0) {
            logger.error("load VacationReport To Subject failed: sid {}", studentId);
        }
        try {
            List<Group> groupMappers = raikouSystem.loadStudentGroups(studentId);
            Map<Long, Group> groupMap = new LinkedHashMap<>();
            Set<Long> groupIds = new LinkedHashSet<>();
            for (Group mapper : groupMappers) {
                groupMap.put(mapper.getId(), mapper);
                groupIds.add(mapper.getId());
            }
            Map<Long, List<VacationHomeworkPackage.Location>> vacationHomeworkPackageLocationMap = vacationHomeworkPackageDao.loadVacationHomeworkPackageByClazzGroupIds(groupIds);
            Map<Subject, VacationReportToSubject> vacationReportToSubjectMap = new LinkedHashMap<>();
            for (Long groupId : groupIds) {
                if (!groupMap.containsKey(groupId))
                    continue;
                Group groupMapper = groupMap.get(groupId);
                if (groupMapper.getSubject() == null)
                    continue;
                VacationReportToSubject vacationReportToSubject = new VacationReportToSubject();
                vacationReportToSubject.setSubject(groupMapper.getSubject());
                vacationReportToSubject.setSubjectName(groupMapper.getSubject().getValue());
                if (vacationHomeworkPackageLocationMap.containsKey(groupId)
                        && CollectionUtils.isNotEmpty(vacationHomeworkPackageLocationMap.get(groupId))) {
                    //有作业包情况
                    List<VacationHomeworkPackage.Location> locations = vacationHomeworkPackageLocationMap.get(groupId);
                    if (locations.size() < 1)
                        continue;
                    VacationHomeworkPackage.Location location = locations.get(0);
                    if (location.getSubject() == null) {
                        continue;
                    }
                    vacationReportToSubject.setPackageId(location.getId());
                    //
                    vacationReportToSubject.setHasJob(true);
                }
                vacationReportToSubjectMap.put(groupMapper.getSubject(), vacationReportToSubject);
            }
            //排序
            List<VacationReportToSubject> vacationReportToSubjects = new LinkedList<>();
            for (Subject subject : new Subject[]{Subject.ENGLISH, Subject.MATH, Subject.CHINESE}) {
                if (vacationReportToSubjectMap.containsKey(subject)) {
                    vacationReportToSubjects.add(vacationReportToSubjectMap.get(subject));
                }
            }
            return vacationReportToSubjects;
        } catch (Exception e) {
            logger.error("load VacationReport To Subject failed: sid {}", studentId, e);
            return Collections.emptyList();
        }
    }


    @Override
    public List<NewVacationHomeworkHistory> newVacationHomeworkHistory(Teacher teacher) {
        try {
            List<NewVacationHomeworkHistory> result = new LinkedList<>();
            Set<Long> groupIds = raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient()
                    .findByTeacherId(teacher.getId())
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(GroupTeacherTuple::isValidTrue)
                    .map(GroupTeacherTuple::getGroupId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(groupIds)) {
                Map<Long, List<VacationHomeworkPackage.Location>> vacationHomeworkPackageLocationMap = vacationHomeworkPackageDao.loadVacationHomeworkPackageByClazzGroupIds(groupIds);
                initVacationHistory(result, groupIds, vacationHomeworkPackageLocationMap);
            }
            return result;
        } catch (Exception e) {
            logger.error("new Vacation Homework History failed : tid {}", teacher.getId(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 组装假期作业列表
     *
     * @param result
     * @param groupIds
     * @param vacationHomeworkPackageLocationMap
     */
    private void initVacationHistory(List<NewVacationHomeworkHistory> result, Set<Long> groupIds, Map<Long, List<VacationHomeworkPackage.Location>> vacationHomeworkPackageLocationMap) {
        List<String> bookIds = vacationHomeworkPackageLocationMap.values()
                .stream()
                .flatMap(Collection::stream)
//                .filter(o -> !SafeConverter.toBoolean(o.getDisabled()))
                .map(VacationHomeworkPackage.Location::getBookId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        Map<String, NewBookProfile> newBookProfileMap = newContentLoaderClient.loadBooks(bookIds);
        //班组相关信息
        Map<Long, GroupMapper> groupMapperMap = groupLoaderClient.loadGroups(vacationHomeworkPackageLocationMap.keySet(), true);

        //班组对应班级ID，为了获取班级信息
        Map<Long, Long> groupIdToClazzIdMap = new LinkedHashMap<>();

        Set<Long> clazzIds = new LinkedHashSet<>();
        for (GroupMapper groupMapper : groupMapperMap.values()) {
            Long clazzId = groupMapper.getClazzId();
            clazzIds.add(groupMapper.getClazzId());
            groupIdToClazzIdMap.put(groupMapper.getId(), clazzId);
        }

        Map<Long, Clazz> classMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        Map<Long, List<User>> usersForGroup = studentLoaderClient.loadGroupStudents(vacationHomeworkPackageLocationMap.keySet());
        //各个班组对应学生寒假作业中间记录
        Map<Long, List<VacationHomeworkCacheMapper>> mapVacationHomeworkCacheMapper = groupIds
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Function.identity(), vacationHomeworkCacheLoader::loadVacationHomeworkCacheMappers));
        for (Long groupId : vacationHomeworkPackageLocationMap.keySet()) {
            if (!usersForGroup.containsKey(groupId))
                continue;
            if (!groupIdToClazzIdMap.containsKey(groupId))
                continue;
            if (!classMap.containsKey(groupIdToClazzIdMap.get(groupId)))
                continue;
            if (vacationHomeworkPackageLocationMap.get(groupId).size() == 0)
                continue;
            for (VacationHomeworkPackage.Location location : vacationHomeworkPackageLocationMap.get(groupId)) {
//                一个班组只有一份作业
//                VacationHomeworkPackage.Location location = vacationHomeworkPackageLocationMap.get(groupId).get(0);
                if (location.getStartTime() == 0)
                    continue;
                if (location.getEndTime() == 0)
                    continue;
                if (location.getSubject() == null)
                    continue;
                //总人数
                Integer totalNum = usersForGroup.get(groupId).size();
                Clazz clazz = classMap.get(groupIdToClazzIdMap.get(groupId));
                //boolean ableToDelete = nowDate.getTime() <= location.getStartTime();
                String startTime = DateUtils.dateToString(new Date(location.getStartTime()));
                Subject subject = location.getSubject();
                String subjectName = location.getSubject().getValue();
                int beginNum = 0;
                int finishNum = 0;
                if (mapVacationHomeworkCacheMapper.containsKey(location.getClazzGroupId())) {
                    List<VacationHomeworkCacheMapper> vacationHomeworkCacheMappers = mapVacationHomeworkCacheMapper.get(location.getClazzGroupId());
                    beginNum = vacationHomeworkCacheMappers.size();
                    if (CollectionUtils.isNotEmpty(vacationHomeworkCacheMappers)) {
                        finishNum = (int) vacationHomeworkCacheMappers.stream()
                                .filter(Objects::nonNull)
                                .filter(VacationHomeworkCacheMapper::isFinished)
                                .count();
                    }
                }
                String bookId = "";
                String bookName = "";
                if (StringUtils.isNotBlank(location.getBookId()) && newBookProfileMap.containsKey(location.getBookId())) {
                    NewBookProfile newBookProfile = newBookProfileMap.get(location.getBookId());
                    if (newBookProfile != null && StringUtils.isNotBlank(newBookProfile.getName())) {
                        bookId = location.getBookId();
                        bookName = newBookProfile.getName();
                    }
                }
                NewVacationHomeworkHistory newVacationHomeworkHistory = new NewVacationHomeworkHistory();
                newVacationHomeworkHistory.setSubjectName(subjectName);
                newVacationHomeworkHistory.setSubject(subject);
                newVacationHomeworkHistory.setAbleToDelete(true);
                newVacationHomeworkHistory.setCreateTime(DateUtils.dateToString(new Date(location.getCreateTime()), "yyyy-MM-dd HH:mm:ss"));
                newVacationHomeworkHistory.setEndTime(DateUtils.dateToString(new Date(location.getEndTime())));
                newVacationHomeworkHistory.setStartTime(startTime);
                newVacationHomeworkHistory.setBeginNum(beginNum);
                newVacationHomeworkHistory.setPackageId(location.getId());
                newVacationHomeworkHistory.setFinishNum(finishNum);
                newVacationHomeworkHistory.setTotalNum(totalNum);
                newVacationHomeworkHistory.setClassName(clazz.formalizeClazzName());
                newVacationHomeworkHistory.setBookId(bookId);
                newVacationHomeworkHistory.setBookName(bookName);
                newVacationHomeworkHistory.setDisabled(location.getDisabled());
                result.add(newVacationHomeworkHistory);
            }
        }
    }

    /**
     * 假期作业list历史(包含被删除的)
     * 仅供CRM使用！！！
     *
     * @param teacher
     * @return
     */
    @Override
    public List<NewVacationHomeworkHistory> allVacationHomeworkHistory(Teacher teacher) {
        try {
            List<NewVacationHomeworkHistory> result = new LinkedList<>();
            Set<Long> groupIds = raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient()
                    .findByTeacherId(teacher.getId())
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(GroupTeacherTuple::isValidTrue)
                    .map(GroupTeacherTuple::getGroupId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(groupIds)) {
                Map<Long, List<VacationHomeworkPackage.Location>> vacationHomeworkPackageLocationMap = vacationHomeworkPackageDao.loadAllVacationHomeworkPackageByClazzGroupIds(groupIds);
                initVacationHistory(result, groupIds, vacationHomeworkPackageLocationMap);
            }
            return result;
        } catch (Exception e) {
            logger.error("all New Vacation Homework History failed : tid {}", teacher.getId(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public MapMessage vacationReportDetailInformation(String homeworkId) {
        VacationHomework newHomework = vacationHomeworkDao.load(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEW_HOMEWORK_TYPE_NOT_EXIST);
        }
        if (CollectionUtils.isEmpty(newHomework.getPractices())) {
            return MapMessage.errorMessage("这份作业里面不存在习题").setErrorCode(ErrorCodeConstants.ERROR_CODE_MISS_HOMEWORK_PRACTICE);
        }
        Long groupId = newHomework.getClazzGroupId();
        Group group = raikouSDK.getClazzClient().getGroupLoaderClient()
                ._loadGroup(groupId).firstOrNull();
        if (group == null) {
            return MapMessage.errorMessage("班组不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(group.getClazzId());
        if (clazz == null) {
            return MapMessage.errorMessage("班级不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        VacationHomeworkResult newHomeworkResult = vacationHomeworkResultDao.load(homeworkId);
        if (newHomeworkResult == null || MapUtils.isEmpty(newHomeworkResult.getPractices())) {
            return MapMessage.errorMessage("学生还未开始写作业").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        if (!newHomeworkResult.isFinished()) {
            return MapMessage.errorMessage("学生还未完成作业").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        User user = userLoaderClient.loadUserIncludeDisabled(newHomeworkResult.getUserId());
        if (user == null) {
            return MapMessage.errorMessage("学生ID错误").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        MapMessage mapMessage = new MapMessage();
        try {
            String clazzName = clazz.formalizeClazzName();
            Map<ObjectiveConfigType, Object> questionInfoMapper = processNewHomework(user, newHomework, newHomeworkResult);
            Map<String, String> objectiveConfigTypes = new LinkedHashMap<>();
            List<String> objectiveConfigTypeRanks = new LinkedList<>();
            for (NewHomeworkPracticeContent content : newHomework.getPractices()) {
                ObjectiveConfigType type = content.getType();
                objectiveConfigTypeRanks.add(type.name());
                objectiveConfigTypes.put(type.name(), type.getValue());
            }
            mapMessage.add("questionInfoMapper", questionInfoMapper);
            mapMessage.add("objectiveConfigTypes", objectiveConfigTypes);
            mapMessage.add("objectiveConfigTypeRanks", objectiveConfigTypeRanks);
            mapMessage.add("userId", user.getId());
            mapMessage.add("userName", user.fetchRealname());
            mapMessage.add("homeworkId", homeworkId);
            mapMessage.add("className", clazzName);
            mapMessage.add("showCorrect", true);
            mapMessage.add("showCorrectInfo", "");
            mapMessage.add("packageId", newHomework.getPackageId());
            mapMessage.add("homeworkType", newHomework.getNewHomeworkType());
            mapMessage.add("subject", newHomework.getSubject());
            mapMessage.setSuccess(true);
        } catch (Exception e) {
            logger.error("vacation Report Detail Information failed : hid {}", homeworkId);
            mapMessage.setSuccess(false).setInfo("获取报告，学生完成情况失败").setErrorCode(ErrorCodeConstants.ERROR_CODE_COMMON);
        }
        return mapMessage;
    }


    @Override
    public MapMessage personalReadingDetail(String homeworkId, String readingId, ObjectiveConfigType type) {
        MapMessage mapMessage = new MapMessage();
        VacationHomework vacationHomework = vacationHomeworkDao.load(homeworkId);
        if (Objects.isNull(vacationHomework)) {
            return MapMessage.errorMessage("homework does not exist");
        }
        NewHomeworkPracticeContent target = vacationHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (target == null) {
            return MapMessage.errorMessage("作业不存在该类型");
        }
        NewHomeworkApp targetApp = null;
        if (CollectionUtils.isNotEmpty(target.getApps())) {
            for (NewHomeworkApp app : target.getApps()) {
                if (Objects.equals(app.getPictureBookId(), readingId)) {
                    targetApp = app;
                }
            }
        }
        if (targetApp == null) {
            return MapMessage.errorMessage("作业不存在该类型");
        }
        VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.load(homeworkId);
        if (Objects.isNull(vacationHomeworkResult)) {
            return MapMessage.errorMessage("vacationHomeworkResult does not exist");
        }
        User u = studentLoaderClient.loadStudent(vacationHomeworkResult.getUserId());
        NewHomeworkResultAnswer newHomeworkResultAnswer = vacationHomeworkResult.getPractices().get(type);
        if (newHomeworkResultAnswer == null) {
            return MapMessage.errorMessage("数据错误");
        }
        NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(readingId);
        if (newHomeworkResultAppAnswer == null) {
            return MapMessage.errorMessage("数据错误");
        }

        String readingName;
        if (type == ObjectiveConfigType.READING) {
            Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(Collections.singleton(readingId));
            readingName = pictureBookMap.get(readingId) != null ? pictureBookMap.get(readingId).getName() : "";
        } else {
            Map<String, PictureBookPlus> pictureBookPlusMap = pictureBookPlusServiceClient.loadByIds(Collections.singleton(readingId));
            readingName = pictureBookPlusMap.get(readingId) != null ? pictureBookPlusMap.get(readingId).getEname() : "";
        }

        List<String> newHomeworkProcessResultIds = new LinkedList<>();
        List<String> questionIds = new LinkedList<>();
        if (MapUtils.isNotEmpty(newHomeworkResultAppAnswer.getOralAnswers())) {
            newHomeworkProcessResultIds.addAll(newHomeworkResultAppAnswer.getOralAnswers().values());
            questionIds.addAll(newHomeworkResultAppAnswer.getOralAnswers().keySet());
        }
        if (MapUtils.isNotEmpty(newHomeworkResultAppAnswer.getAnswers())) {
            newHomeworkProcessResultIds.addAll(newHomeworkResultAppAnswer.getAnswers().values());
            questionIds.addAll(newHomeworkResultAppAnswer.getAnswers().keySet());
        }
        Map<String, VacationHomeworkProcessResult> newHomeworkProcessResultMap = vacationHomeworkProcessResultDao.loads(newHomeworkProcessResultIds);
        Map<String, NewQuestion> questionsMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        //List<OralQuestionData> oralQuestions = new LinkedList<>();
        List<Map<String, Object>> oralQuestions = new LinkedList<>();
        ExercisesData exercisesData = new ExercisesData();

        //是否包含口语题
        if (MapUtils.isNotEmpty(newHomeworkResultAppAnswer.getOralAnswers())) {
            LinkedHashMap<String, String> oralAnswers = newHomeworkResultAppAnswer.getOralAnswers();
            oralAnswers.forEach((key, value) -> {
                VacationHomeworkProcessResult newHomeworkProcessResult = newHomeworkProcessResultMap.get(value);
                if (newHomeworkProcessResult != null) {
                    NewQuestion newQuestion = questionsMap.get(key);
                    if (newQuestion == null || newQuestion.getContent() == null || newQuestion.getContent().getSubContents() == null) {
                        return;
                    }
                    AppOralScoreLevel appOralScoreLevel = newHomeworkProcessResult.getAppOralScoreLevel();
                    double score = appOralScoreLevel != null ? appOralScoreLevel.getScore() : 0;
                    List<NewQuestionsSubContents> subContents = newQuestion.getContent().getSubContents();
                    int i = 0;
                    for (NewQuestionsSubContents newQuestionsSubContents : subContents) {
                        if (newQuestionsSubContents.getOralDict() != null &&
                                CollectionUtils.isNotEmpty(newQuestionsSubContents.getOralDict().getOptions()) &&
                                newHomeworkProcessResult.getOralDetails().size() >= (i + 1)) {
                            List<BaseHomeworkProcessResult.OralDetail> oralDetails = newHomeworkProcessResult.getOralDetails().get(i);
                            List<NewQuestionOralDictOptions> options = newQuestionsSubContents.getOralDict().getOptions();
                            int j = 0;
                            for (NewQuestionOralDictOptions newQuestionOralDictOptions : options) {
                                if (oralDetails.size() >= (j + 1)) {
                                    BaseHomeworkProcessResult.OralDetail oralDetail = oralDetails.get(j);
                                    String voiceUrl = oralDetail.getAudio();
                                    VoiceEngineType voiceEngineType = newHomeworkProcessResult.getVoiceEngineType();
                                    voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                                    oralQuestions.add(MapUtils.m(
                                            "text", newQuestionOralDictOptions.getText(),
                                            "audio", voiceUrl,
                                            "score", score,
                                            "scoreLevel", ScoreLevel.processLevel((int) score).getLevel()
                                    ));
                                }
                                j++;
                            }
                        }
                        i++;
                    }
                }
            });
        }
        if (MapUtils.isNotEmpty(newHomeworkResultAppAnswer.getAnswers())) {
            LinkedHashMap<String, String> answers = newHomeworkResultAppAnswer.getAnswers();
            int totalExercises = answers.size();
            int rightNum = 0;
            for (String key : answers.keySet()) {
                String value = answers.get(key);
                VacationHomeworkProcessResult newHomeworkProcessResult = newHomeworkProcessResultMap.get(value);
                if (newHomeworkProcessResult == null) {
                    continue;
                }
                NewQuestion newQuestion = questionsMap.get(key);
                if (newQuestion == null || newQuestion.getContent() == null || newQuestion.getContent().getSubContents() == null) {
                    continue;
                }
                List<NewQuestionsSubContents> subContents = newQuestion.getContent().getSubContents();
                List<List<String>> standardAnswers = subContents
                        .stream()
                        .map(o -> o.getAnswerList(vacationHomework.getSubject()))
                        .collect(Collectors.toList());

                if (Objects.equals(Boolean.TRUE, newHomeworkProcessResult.getGrasp())) {
                    rightNum++;
                }
                ExercisesQuestionData exercisesQuestionData = new ExercisesQuestionData();
                exercisesData.getExercisesQuestionInfo().add(exercisesQuestionData);
                exercisesQuestionData.setQuestionId(key);
                exercisesQuestionData.setUserAnswers(NewHomeworkUtils.pressAnswer(subContents, newHomeworkProcessResult.getUserAnswers()));
                exercisesQuestionData.setStandardAnswers(NewHomeworkUtils.pressAnswer(subContents, standardAnswers));
                exercisesQuestionData.setDifficultyName(QuestionConstants.newDifficultyMap.get(newQuestion.getDifficultyInt()));
                exercisesQuestionData.setQuestionType(contentTypeMap.get(newQuestion.getContentTypeId()) != null ? contentTypeMap.get(newQuestion.getContentTypeId()).getName() : "无题型");
            }
            exercisesData.setRightNum(rightNum);
            exercisesData.setTotalExercises(totalExercises);
        }
        String dubbingId = newHomeworkResultAppAnswer.getDubbingId();
        AppOralScoreLevel appOralScoreLevel = newHomeworkResultAppAnswer.getDubbingScoreLevel();
        mapMessage.add("dubbingId", targetApp.containsDubbing() ? dubbingId : null);
        mapMessage.add("dubbingScoreLevel", appOralScoreLevel != null && targetApp.containsDubbing() ? appOralScoreLevel.getDesc() : null);
        mapMessage.add("exercisesInfo", exercisesData);
        mapMessage.add("oralQuestions", oralQuestions);
        mapMessage.add("readingName", readingName);
        mapMessage.add("studentName", u != null ? u.fetchRealname() : "");
        mapMessage.add("homeworkType", vacationHomework.getNewHomeworkType());
        mapMessage.add("subject", vacationHomework.getSubject());
        mapMessage.setSuccess(true);
        mapMessage.putAll(MapUtils.m(
                "questionUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions" + Constants.AntiHijackExt,
                        MapUtils.m(
                                "homeworkId", homeworkId,
                                "type", "")),
                "completedUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions/answer" + Constants.AntiHijackExt,
                        MapUtils.m(
                                "homeworkId", homeworkId,
                                "objectiveConfigType", ""))
        ));
        return mapMessage;
    }

    /**
     * 新版课文读背个人报告
     *
     * @param hid
     * @param questionBoxId
     * @param sid
     * @return
     */
    public MapMessage personalReadReciteWithScore(String hid, String questionBoxId, Long sid) {
        VacationHomework vacationHomework = vacationHomeworkDao.load(hid);
        if (vacationHomework == null) {
            return MapMessage.errorMessage("作业ID错误");
        }
        NewHomeworkPracticeContent target = vacationHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.READ_RECITE_WITH_SCORE);
        if (target == null) {
            return MapMessage.errorMessage("作业不存在该类型");
        }
        NewHomeworkApp targetApp = null;
        for (NewHomeworkApp app : target.getApps()) {
            if (Objects.equals(app.getQuestionBoxId(), questionBoxId)) {
                targetApp = app;
                break;
            }
        }
        if (targetApp == null) {
            return MapMessage.errorMessage("questionBoxId错误");
        }
        if (CollectionUtils.isEmpty(targetApp.getQuestions())) {
            return MapMessage.errorMessage("vacationHomework错误");
        }

        VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.load(vacationHomework.getId());
        if (vacationHomeworkResult == null || MapUtils.isEmpty(vacationHomeworkResult.getPractices())) {
            return MapMessage.errorMessage("学生还未开始写作业").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        if (!vacationHomeworkResult.isFinished()) {
            return MapMessage.errorMessage("学生还未完成作业").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        NewHomeworkResultAnswer newHomeworkResultAnswer = vacationHomeworkResult.getPractices().get(ObjectiveConfigType.READ_RECITE_WITH_SCORE);
        if (newHomeworkResultAnswer == null || MapUtils.isEmpty(newHomeworkResultAnswer.getAppAnswers())) {
            return MapMessage.errorMessage("vacationHomeworkResult数据错误");
        }
        if (!newHomeworkResultAnswer.getAppAnswers().containsKey(questionBoxId)) {
            return MapMessage.errorMessage("vacationHomeworkResult数据错误");
        }
        NewHomeworkResultAppAnswer appAnswer = newHomeworkResultAnswer.getAppAnswers().get(questionBoxId);
        if (MapUtils.isEmpty(appAnswer.getAnswers())) {
            return MapMessage.errorMessage("vacationHomeworkResult数据错误");
        }
        try {
            List<String> newHomeworkProcessIds = vacationHomeworkResult.findAllHomeworkProcessIds(true);
            Map<String, VacationHomeworkProcessResult> allProcessResultMap = vacationHomeworkProcessResultDao.loads(newHomeworkProcessIds);

            List<String> allQuestionIds = targetApp.getQuestions().stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList());
            Map<String, NewQuestion> allNewQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(allQuestionIds);
            //key ==》自然段段落编号
            Map<String, Integer> qidToParagraph = new LinkedHashMap<>();
            Map<String, Boolean> qidToDifficultyType = new LinkedHashMap<>();

            List<Long> chineseSentenceId = targetApp.getQuestions()
                    .stream()
                    .filter(o -> allNewQuestionMap.containsKey(o.getQuestionId()))
                    .map(o -> allNewQuestionMap.get(o.getQuestionId()))
                    .filter(Objects::nonNull)
                    .filter(question -> CollectionUtils.isNotEmpty(question.getSentenceIds()))
                    .map(o -> o.getSentenceIds().get(0))
                    .collect(Collectors.toList());
            List<ChineseSentence> chineseSentences = chineseContentLoaderClient.loadChineseSentenceByIds(new LinkedList<>(chineseSentenceId));

            if (CollectionUtils.isNotEmpty(chineseSentences)) {
                Map<Long, ChineseSentence> mapChineseSentences = chineseSentences.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(AbstractDatabaseEntity::getId, Function.identity()));
                for (NewHomeworkQuestion question : targetApp.getQuestions()) {
                    if (!allNewQuestionMap.containsKey(question.getQuestionId()))
                        continue;
                    NewQuestion newQuestion = allNewQuestionMap.get(question.getQuestionId());
                    if (CollectionUtils.isEmpty(newQuestion.getSentenceIds()))
                        continue;
                    Long sentenceId = newQuestion.getSentenceIds().get(0);
                    if (!mapChineseSentences.containsKey(sentenceId))
                        continue;
                    ChineseSentence chineseSentence = mapChineseSentences.get(sentenceId);
                    if (Objects.isNull(chineseSentence.getParagraph()))
                        continue;
                    qidToParagraph.put(question.getQuestionId(), chineseSentence.getParagraph());
                    qidToDifficultyType.put(question.getQuestionId(), chineseSentence.getReciteParagraph());
                }
            }
            int standardCount = 0;
            List<String> voices = new LinkedList<>();
            List<ParagraphDetailed> paragraphDetaileds = new LinkedList<>();
            for (NewHomeworkQuestion newHomeworkQuestion : targetApp.getQuestions()) {
                if (!appAnswer.getAnswers().containsKey(newHomeworkQuestion.getQuestionId())) {
                    continue;
                }
                String processId = appAnswer.getAnswers().get(newHomeworkQuestion.getQuestionId());
                if (!allProcessResultMap.containsKey(processId)) {
                    continue;
                }
                VacationHomeworkProcessResult vacationHomeworkProcessResult = allProcessResultMap.get(processId);
                ParagraphDetailed paragraphDetailed = new ParagraphDetailed();
                //设置语音
                paragraphDetailed.setVoices(CollectionUtils.isNotEmpty(vacationHomeworkProcessResult.getOralDetails()) ?
                        vacationHomeworkProcessResult
                                .getOralDetails()
                                .stream()
                                .flatMap(Collection::stream)
                                .map(o -> VoiceEngineTypeUtils.getAudioUrl(o.getAudio(), vacationHomeworkProcessResult.getVoiceEngineType()))
                                .collect(Collectors.toList()) :
                        Collections.emptyList());
                //设置是否达标
                paragraphDetailed.setStandard(SafeConverter.toBoolean(vacationHomeworkProcessResult.getGrasp()));
                if (paragraphDetailed.isStandard()) {
                    standardCount++;
                }
                int duration = new BigDecimal(SafeConverter.toLong(vacationHomeworkProcessResult.getDuration())).divide(new BigDecimal(1000), 0, BigDecimal.ROUND_HALF_UP).intValue();
                String durationStr = NewHomeworkUtils.handlerEnTime(duration);
                paragraphDetailed.setDuration(durationStr);
                paragraphDetailed.setQuestionId(newHomeworkQuestion.getQuestionId());
                if (qidToParagraph.containsKey(newHomeworkQuestion.getQuestionId())) {
                    paragraphDetailed.setParagraphOrder(qidToParagraph.get(newHomeworkQuestion.getQuestionId()));
                }
                //设置是否是重点自然段
                paragraphDetailed.setParagraphDifficultyType(SafeConverter.toBoolean(qidToDifficultyType.get(newHomeworkQuestion.getQuestionId())));
                voices.addAll(paragraphDetailed.getVoices());
                paragraphDetaileds.add(paragraphDetailed);
            }
            MapMessage mapMessage = MapMessage.successMessage();
            double value = new BigDecimal(SafeConverter.toInt(appAnswer.getStandardNum()) * 100).divide(new BigDecimal(appAnswer.getAnswers().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            long appDuration = SafeConverter.toLong(appAnswer.processDuration());
            int duration = new BigDecimal(appDuration).divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP).intValue();
            mapMessage.add("duration", NewHomeworkUtils.handlerEnTime(duration));
            mapMessage.add("standard", value >= NewHomeworkConstants.READ_RECITE_STANDARD);
            mapMessage.add("questionBoxType", targetApp.getQuestionBoxType());
            mapMessage.add("questionBoxTypeName", targetApp.getQuestionBoxType().getName());
            mapMessage.add("voices", voices);
            mapMessage.add("standardCount", standardCount);
            mapMessage.add("paragraphDetaileds", paragraphDetaileds);
            return mapMessage;
        } catch (Exception e) {
            logger.error("personal ReadReciteWithScore sid {},hid {},questionBoxId {}", sid, hid, questionBoxId, e);
            return MapMessage.errorMessage("操作失败");
        }
    }


    @Override
    public MapMessage personalDubbingDetail(String homeworkId, String dubbingId) {
        try {
            VacationHomework newHomework = vacationHomeworkDao.load(homeworkId);
            if (newHomework == null) {
                return MapMessage.errorMessage("作业不存在");
            }
            NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.DUBBING_WITH_SCORE);
            if (target == null) {
                return MapMessage.errorMessage("作业不包含" + ObjectiveConfigType.DUBBING_WITH_SCORE.getValue());
            }
            //获取班级学生
            VacationHomeworkResult newHomeworkResult = vacationHomeworkResultDao.load(newHomework.getId());
            if (newHomeworkResult == null) {
                return MapMessage.errorMessage("答案丢失");
            }
            if (!newHomeworkResult.isFinished()) {
                return MapMessage.errorMessage("作业未完成");
            }
            Student student = studentLoaderClient.loadStudent(newHomeworkResult.getUserId());
            if (student == null) {
                return MapMessage.errorMessage("学生ID错误");
            }
            String duration = "";
            String studentVideoUrl = "";
            boolean syntheticSuccess = true;
            if (MapUtils.isNotEmpty(newHomeworkResult.getPractices()) && newHomeworkResult.getPractices().containsKey(ObjectiveConfigType.DUBBING_WITH_SCORE)) {
                NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.DUBBING_WITH_SCORE);
                LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = newHomeworkResultAnswer.getAppAnswers();
                NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = appAnswers.get(dubbingId);
                if (newHomeworkResultAppAnswer != null) {
                    String hyid = new DubbingSyntheticHistory.ID(homeworkId, newHomeworkResult.getUserId(), dubbingId).toString();
                    Map<String, DubbingSyntheticHistory> dubbingSyntheticHistoryMap = newHomeworkLoader.loadDubbingSyntheticHistories(Collections.singleton(hyid));
                    if (dubbingSyntheticHistoryMap.containsKey(hyid)) {
                        DubbingSyntheticHistory dubbingSyntheticHistory = dubbingSyntheticHistoryMap.get(hyid);
                        syntheticSuccess = SafeConverter.toBoolean(dubbingSyntheticHistory.isSyntheticSuccess(newHomework.getCreateAt()));
                    }
                    studentVideoUrl = newHomeworkResultAppAnswer.getVideoUrl();
                    int time = new BigDecimal(SafeConverter.toLong(newHomeworkResultAppAnswer.processDuration()))
                            .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                            .intValue();
                    duration = NewHomeworkUtils.handlerEnTime(time);
                }
            }
            Map<String, Object> result = MapUtils.m(
                    "studentId", student.getId(),
                    "studentName", student.fetchRealnameIfBlankId(),
                    "duration", duration,
                    "syntheticSuccess", syntheticSuccess,
                    "studentVideoUrl", studentVideoUrl);
            return MapMessage.successMessage().add("content", result);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage reportDetailsBaseApp(String homeworkId, String categoryId, String lessonId, ObjectiveConfigType objectiveConfigType) {
        MapMessage mapMessage = new MapMessage();
        String clazzName;
        VacationHomework newHomework = vacationHomeworkDao.load(homeworkId);
        if (newHomework == null) {
            logger.warn("vacation report Details BaseApp failed hid {},categoryId {},lessonId {}, objectiveConfigType {} ", homeworkId, categoryId, lessonId, objectiveConfigType);
            return MapMessage.errorMessage("该作业不存在");
        }
        Long groupId = newHomework.getClazzGroupId();
        Group group = raikouSDK.getClazzClient()
                .getGroupLoaderClient()
                ._loadGroup(groupId)
                .firstOrNull();
        if (group == null) {
            logger.warn("vacation report Details BaseApp failed hid {},categoryId {},lessonId {}, objectiveConfigType {} ", homeworkId, categoryId, lessonId, objectiveConfigType);
            return MapMessage.errorMessage("班组 不存在");
        }
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                ._loadClazz(group.getClazzId())
                .firstOrNull();
        if (clazz == null) {
            logger.warn("vacation report Details BaseApp failed hid {},categoryId {},lessonId {}, objectiveConfigType {} ", homeworkId, categoryId, lessonId, objectiveConfigType);
            return MapMessage.errorMessage("班级 不存在");
        }
        clazzName = clazz.formalizeClazzName();
        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                .stream()
                .collect(Collectors
                        .toMap(LongIdEntity::getId, Function.identity()));

        List<NewHomeworkApp> apps = newHomework.findNewHomeworkApps(objectiveConfigType);
        if (CollectionUtils.isEmpty(apps)) {
            logger.warn("vacation report Details BaseApp failed hid {},categoryId {},lessonId {}, objectiveConfigType {} ", homeworkId, categoryId, lessonId, objectiveConfigType);
            return MapMessage.errorMessage("App 不存在");
        }
        NewHomeworkApp target = apps.stream()
                .filter(o -> Objects.equals(SafeConverter.toString(o.getCategoryId()), categoryId))
                .filter(o -> Objects.equals(o.getLessonId(), lessonId))
                .findFirst()
                .orElse(null);
        if (target == null) {
            logger.warn("vacation report Details BaseApp failed hid {},categoryId {},lessonId {}, objectiveConfigType {} ", homeworkId, categoryId, lessonId, objectiveConfigType);
            return MapMessage.errorMessage("App 不存在");
        }
        PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(target.getPracticeId());
        if (Objects.isNull(practiceType)) {
            logger.warn("vacation report Details BaseApp failed hid {},categoryId {},lessonId {}, objectiveConfigType {} ", homeworkId, categoryId, lessonId, objectiveConfigType);
            return MapMessage.errorMessage("practiceType is null");
        }
        VacationHomeworkResult newHomeworkResult = vacationHomeworkResultDao.load(homeworkId);
        if (Objects.isNull(newHomeworkResult)
                || MapUtils.isEmpty(newHomeworkResult.getPractices())) {
            return MapMessage.errorMessage("学生还未开始写作业");
        }
        if (!newHomeworkResult.isFinished()) {
            return MapMessage.errorMessage("学生未完成写作业");
        }
        if (!userMap.containsKey(newHomeworkResult.getUserId())) {
            return MapMessage.errorMessage("该学生不在该班组");
        }
        User user = userMap.get(newHomeworkResult.getUserId());
        List<Map<String, Object>> value = internalProcessHomeworkAnswer(user, newHomeworkResult, categoryId, lessonId, practiceType, target, objectiveConfigType);
        mapMessage.add("description", practiceType.getDescription());
        mapMessage.add("tongueTwister", Objects.equals(categoryId, SafeConverter.toString(NatureSpellingType.TONGUE_TWISTER.getCategoryId())));
        mapMessage.add("questionInfoMapper", value);
        mapMessage.add("needRecord", practiceType.getNeedRecord());
        mapMessage.add("userId", user.getId());
        mapMessage.add("userName", user.fetchRealname());
        mapMessage.add("homeworkId", homeworkId);
        mapMessage.add("className", clazzName);
        mapMessage.add("categoryName", practiceType.getCategoryName());
        mapMessage.add("showCorrect", newHomeworkResult.isFinished());
        mapMessage.add("showCorrectInfo", newHomeworkResult.isFinished() ? "" : "此学生尚未完成全部作业，暂时不能批改");
        mapMessage.add("homeworkType", newHomework.getNewHomeworkType());
        mapMessage.setSuccess(true);
        return mapMessage;
    }


    @Override
    public MapMessage packageReport(String packageId, User user, Long sid, Boolean fromJzt) {
        VacationHomeworkPackage vacationHomeworkPackage = vacationHomeworkPackageDao.load(packageId);
        if (vacationHomeworkPackage == null)
            return MapMessage.errorMessage(" VacationHomeworkPackage is null");
        if (SafeConverter.toBoolean(vacationHomeworkPackage.getDisabled())) {
            return MapMessage.errorMessage(" 作业已经删除");
        }
        VacationHomeworkWinterPlanCacheMapper vacationHomeworkWinterPlanCacheMapper = vacationHomeworkCacheLoader.loadVacationHomeworkWinterPlanCacheMapper(vacationHomeworkPackage.getBookId());
        if (vacationHomeworkWinterPlanCacheMapper == null)
            return MapMessage.errorMessage(" WinterPlan is null");
        Long clazzGroupId = vacationHomeworkPackage.getClazzGroupId();
        if (clazzGroupId == null)
            return MapMessage.errorMessage(" clazzGroupId is null");

        List<User> users = studentLoaderClient.loadGroupStudents(clazzGroupId);
        if (user != null && user.isParent() && fromJzt) {
            //sid是否这个班组下面
            boolean flag = false;
            Map<Long, User> userMap = new LinkedHashMap<>();
            for (User u : users) {
                if (Objects.equals(u.getId(), sid)) {
                    flag = true;
                    break;
                }
                userMap.put(u.getId(), u);
            }
            if (!flag) {
                List<User> users1 = studentLoaderClient.loadParentStudents(user.getId());
                List<Long> childrenIds = new LinkedList<>();
                for (User u : users1) {
                    if (userMap.containsKey(u.getId())) {
                        childrenIds.add(u.getId());
                    }
                }
                //多个孩子在班级里面的时候，随机取一个
                if (childrenIds.size() != 0) {
                    flag = true;
                    int v = (int) (Math.random() * childrenIds.size());
                    if (v == childrenIds.size()) {
                        v--;
                    }
                    sid = childrenIds.get(v);
                }
            }
            if (!flag) {
                LogCollector.info("backend-general", MapUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", sid,
                        "mod1", packageId,
                        "op", "vacationHomework report"
                ));
                return MapMessage.errorMessage("报告内容与当前选择的孩子帐号不符，请检查后重试~");
            }
        }

        if (CollectionUtils.isEmpty(users))
            return MapMessage.errorMessage(" users is empty");
        Group group = raikouSDK.getClazzClient()
                .getGroupLoaderClient()
                ._loadGroup(clazzGroupId)
                .firstOrNull();
        if (group == null)
            return MapMessage.errorMessage(" 班组 is null");
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                ._loadClazz(group.getClazzId())
                .firstOrNull();
        if (clazz == null)
            return MapMessage.errorMessage(" 班级 is null");
        try {
            Map<Long, VacationHomeworkCacheMapper> mapVacationHomeworkCacheMapper =
                    vacationHomeworkCacheLoader.loadVacationHomeworkCacheMappers(clazzGroupId).stream()
                            .filter(Objects::nonNull)
                            .filter(o -> o.getStudentId() != null)
                            .collect(Collectors.toMap(VacationHomeworkCacheMapper::getStudentId, Function.identity()));
            NewVacationHomeworkPackagePanorama newVacationHomeworkPackagePanorama = new NewVacationHomeworkPackagePanorama();
//            int totalHomeworkNum = vacationHomeworkWinterPlanCacheMapper.getDayPlan() != null ? vacationHomeworkWinterPlanCacheMapper.getDayPlan().size() : 0;

            // 分享渠道
            if (user != null) {
                TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
                if (teacherDetail != null) {
                    List<Integer> channels = newHomeworkContentServiceImpl.loadHomeworkReportShareChannel(teacherDetail);
                    newVacationHomeworkPackagePanorama.setChannels(channels);
                }
            }

            int totalHomeworkNum = SafeConverter.toInt(vacationHomeworkPackage.getPlannedDays());
            newVacationHomeworkPackagePanorama.setTotalStudentNum(users.size());
            newVacationHomeworkPackagePanorama.setPackageId(packageId);
            int finishedStudentNum = 0;
            int beginVacationHomeworkNum = 0;
            for (User u : users) {
                NewVacationHomeworkStudentPanorama newVacationHomeworkStudentPanorama = new NewVacationHomeworkStudentPanorama();
                String avatarUrl = u.fetchImageUrl();
                newVacationHomeworkStudentPanorama.setStudentName(u.fetchRealnameIfBlankId());
                newVacationHomeworkStudentPanorama.setTotalHomeworkNum(totalHomeworkNum);
                newVacationHomeworkStudentPanorama.setStudentId(u.getId());
                newVacationHomeworkStudentPanorama.setAvatarUrl(avatarUrl);
                int finishedHomeworkNum = 0;
                boolean beginPackage = true;
                int beginHomeworkNum = 0;
                VacationHomeworkCacheMapper vacationHomeworkCacheMapper = mapVacationHomeworkCacheMapper.get(u.getId());
                if (vacationHomeworkCacheMapper == null) {
                    beginPackage = false;
                } else {
                    beginVacationHomeworkNum++;
                    Map<String, VacationHomeworkDetailCacheMapper> homeworkDetail = vacationHomeworkCacheMapper.getHomeworkDetail();
                    Map<String, WinterDayPlan> dayPlanMap = vacationHomeworkWinterPlanCacheMapper.getDayPlan() != null ? vacationHomeworkWinterPlanCacheMapper.getDayPlan() : Collections.emptyMap();
                    if (MapUtils.isNotEmpty(homeworkDetail)) {
                        for (VacationHomeworkDetailCacheMapper v : homeworkDetail.values()) {
                            String key = v.getWeekRank() + "-" + v.getDayRank();
                            if (dayPlanMap.containsKey(key)) {
                                beginHomeworkNum++;
                                if (v.isFinished()) {
                                    if (newVacationHomeworkStudentPanorama.getEndTime() == null) {
                                        newVacationHomeworkStudentPanorama.setEndTime(v.getFinishAt());
                                    } else {
                                        if (v.getFinishAt().after(newVacationHomeworkStudentPanorama.getEndTime())) {
                                            newVacationHomeworkStudentPanorama.setEndTime(v.getFinishAt());
                                        }
                                    }
                                    finishedHomeworkNum++;
                                    newVacationHomeworkStudentPanorama.setAvgScore(v.getAvgScore());
                                }
                            }
                        }
                    }
                    if (vacationHomeworkCacheMapper.isFinished()) {
                        finishedStudentNum++;
                    }

                }
                newVacationHomeworkStudentPanorama.setBeginHomeworkNum(beginHomeworkNum);
                newVacationHomeworkStudentPanorama.setFinishedHomeworkNum(finishedHomeworkNum);
                newVacationHomeworkStudentPanorama.setBeginPackage(beginPackage);
                newVacationHomeworkPackagePanorama.getVacationHomeworkStudentPanoramas().add(newVacationHomeworkStudentPanorama);
            }
            newVacationHomeworkPackagePanorama.getVacationHomeworkStudentPanoramas()
                    .sort((o1, o2) -> {
                        int so = Integer.compare(o2.getFinishedHomeworkNum(), o1.getFinishedHomeworkNum());
                        if (so != 0) {
                            return so;
                        }
                        if (o2.getFinishedHomeworkNum() == 0 || o1.getFinishedHomeworkNum() == 0) {
                            return Collator.getInstance(Locale.CHINESE).compare(o1.getStudentName(), o1.getStudentName());
                        }
                        if (o2.getEndTime().before(o1.getEndTime())) {
                            return 1;
                        } else if (o2.getEndTime().equals(o1.getEndTime())) {
                            return Collator.getInstance(Locale.CHINESE).compare(o1.getStudentName(), o1.getStudentName());
                        } else {
                            return -1;
                        }
                    });

            //是否家长通今天发生消息了
            ShareVacationReportCacheManager shareVacationReportCacheManager = newHomeworkCacheService.getShareVacationReportCacheManager();
            String cacheKey = shareVacationReportCacheManager.getCacheKey(packageId);
            Integer cacheValue = shareVacationReportCacheManager.load(cacheKey);
            newVacationHomeworkPackagePanorama.setJztTodayHasShare(cacheValue != null);

            //是否今天微信和QQ分享了
            ShareWeiXinVacationReportCacheManager shareWeiXinVacationReportCacheManager = newHomeworkCacheService.getShareWeiXinVacationReportCacheManager();
            cacheKey = shareWeiXinVacationReportCacheManager.getCacheKey(packageId);
            cacheValue = shareWeiXinVacationReportCacheManager.load(cacheKey);
            newVacationHomeworkPackagePanorama.setWeiXinTodayHasShare(cacheValue != null);

            //今天是否提醒了学生
            RemindStudentVacationProgressCacheManager remindStudentVacationProgressCacheManager = newHomeworkCacheService.getRemindStudentVacationProgressCacheManager();
            cacheKey = remindStudentVacationProgressCacheManager.getCacheKey(packageId);
            cacheValue = remindStudentVacationProgressCacheManager.load(cacheKey);
            newVacationHomeworkPackagePanorama.setRemindStudent(cacheValue != null);

            Date currentTime = new Date();
            //是否可以删除和是否能够分享
//            boolean ableToDelete = currentTime.before(vacationHomeworkPackage.getStartTime());
            boolean ableToShare = currentTime.before(vacationHomeworkPackage.getStartTime());
            newVacationHomeworkPackagePanorama.setAbleToDelete(true);
            newVacationHomeworkPackagePanorama.setAbleToShare(!ableToShare);
            newVacationHomeworkPackagePanorama.setStartTime(vacationHomeworkPackage.getStartTime());
            newVacationHomeworkPackagePanorama.setEndTime(vacationHomeworkPackage.getEndTime());
            newVacationHomeworkPackagePanorama.setClazzName(clazz.formalizeClazzName());
            newVacationHomeworkPackagePanorama.setBeginVacationHomeworkNum(beginVacationHomeworkNum);
            newVacationHomeworkPackagePanorama.setFinishedStudentNum(finishedStudentNum);
            newVacationHomeworkPackagePanorama.setSubject(vacationHomeworkPackage.getSubject());
            newVacationHomeworkPackagePanorama.setSubjectName(vacationHomeworkPackage.getSubject().getValue());
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("vacationHomeworkPackagePanorama", newVacationHomeworkPackagePanorama);
            mapMessage.add("sid", sid);
            //是否可以抽奖
            //mapMessage.add("canReward", currentTime.before(NewHomeworkConstants.VH_LOTTERY_ENTRANCE_OFFLINE_DATE));
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch package Report failed : packageId {}", packageId, e);
            return MapMessage.errorMessage("接口错误");
        }
    }

    //crm
    @Override
    public Map<String, Object> studentVacationNewHomeworkDetail(String homeworkId) {
        VacationHomework newHomework = vacationHomeworkDao.load(homeworkId);
        Subject subject = newHomework.getSubject();
        List<Map<String, Object>> resultList = new LinkedList<>();

        VacationHomeworkResult result = vacationHomeworkResultDao.load(homeworkId);
        Map<String, String> oldQuestionIdToProcessId = new LinkedHashMap<>();
        Map<String, String> selectItemMap = new LinkedHashMap<>();
        selectItemMap.put("0.0", "全部");
        if (result != null) {
            LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = result.getPractices();
            List<String> qIds = newHomework.findAllQuestionIds();
            Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(qIds);
            if (MapUtils.isNotEmpty(practices)) {
                List<String> processIds = result.findAllHomeworkProcessIds(true);
                Map<String, VacationHomeworkProcessResult> processResultsMap = vacationHomeworkProcessResultDao
                        .loads(processIds);
                for (ObjectiveConfigType oct : practices.keySet()) {
                    if (oct == ObjectiveConfigType.READING) {
                        LinkedHashMap<String, NewHomeworkResultAppAnswer> apps = practices.get(oct).getAppAnswers();
                        List<String> pictureBookIds = apps.values()
                                .stream()
                                .map(NewHomeworkResultAppAnswer::getPictureBookId)
                                .collect(Collectors.toList());
                        Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(pictureBookIds);
                        if (MapUtils.isNotEmpty(apps)) {
                            for (NewHomeworkResultAppAnswer app : apps.values()) {
                                String selectItemKey = "1," + app.getPictureBookId();
                                PictureBook pictureBook = pictureBookMap.get(app.getPictureBookId());
                                selectItemMap.put(selectItemKey, oct.getValue() + ":" + pictureBook.getName());
                                String itemName = oct.getValue() + ":" + pictureBook.getName();
                                if (MapUtils.isNotEmpty(app.getAnswers())) {
                                    app.getAnswers()
                                            .values()
                                            .stream()
                                            .map(pid -> {
                                                VacationHomeworkProcessResult pr = processResultsMap.get(pid);
                                                NewQuestion newQuestion = questionMap.get(pr.getQuestionId());
                                                return translateNewHomeworkProcess(pr, newQuestion, selectItemKey, itemName, "", "", oct, oldQuestionIdToProcessId, processResultsMap);
                                            })
                                            .filter(Objects::nonNull)
                                            .forEach(resultList::add);
                                }
                                if (MapUtils.isNotEmpty(app.getOralAnswers())) {

                                    app.getOralAnswers()
                                            .values()
                                            .stream()
                                            .map(pid -> {
                                                VacationHomeworkProcessResult pr = processResultsMap.get(pid);
                                                NewQuestion newQuestion = questionMap.get(pr.getQuestionId());
                                                return translateNewHomeworkProcess(pr, newQuestion, selectItemKey, itemName, "", "", oct, oldQuestionIdToProcessId, processResultsMap);
                                            })
                                            .filter(Objects::nonNull)
                                            .forEach(resultList::add);

                                }
                            }
                        }
                    } else if (oct == ObjectiveConfigType.BASIC_APP || oct == ObjectiveConfigType.NATURAL_SPELLING) {

                        LinkedHashMap<String, NewHomeworkResultAppAnswer> apps = practices.get(oct).getAppAnswers();
                        List<String> lessons = apps
                                .values()
                                .stream()
                                .map(NewHomeworkResultAppAnswer::getLessonId)
                                .collect(Collectors.toList());
                        Map<String, NewBookCatalog> lessonNewBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogIds(lessons);
                        Set<String> unitsIds = new HashSet<>();
                        Map<String, String> lessonToUnit = new LinkedHashMap<>();
                        if (MapUtils.isNotEmpty(lessonNewBookCatalog)) {
                            lessonToUnit = lessonNewBookCatalog.values()
                                    .stream()
                                    .filter(n -> !CollectionUtils.isEmpty(n.getAncestors()))
                                    .collect(Collectors.toMap(NewBookCatalog::getId, n -> {
                                        List<NewBookCatalogAncestor> l = n.getAncestors();
                                        Map<String, NewBookCatalogAncestor> m = l
                                                .stream()
                                                .filter(v -> Objects.nonNull(v.getNodeType()))
                                                .collect(Collectors.toMap(NewBookCatalogAncestor::getNodeType, Function.identity()));
                                        if (m.containsKey("UNIT") && m.get("UNIT").getId() != null) {
                                            return m.get("UNIT").getId();
                                        }
                                        return "";
                                    }));
                            unitsIds.addAll(lessonToUnit.values());
                        }
                        Map<String, NewBookCatalog> units = newContentLoaderClient.loadBookCatalogByCatalogIds(unitsIds);
                        if (MapUtils.isNotEmpty(apps)) {
                            Map<String, String> finalLessonToUnit = lessonToUnit;
                            apps.values()
                                    .stream()
                                    .filter(app -> MapUtils.isNotEmpty(app.getAnswers()))
                                    .map(app -> {
                                        PracticeType practiceType = practiceLoaderClient.loadPractice(app.getPracticeId());
                                        String selectItemKey = "2," + app.getPracticeId();
                                        selectItemMap.put(selectItemKey, oct.getValue() + ":" + practiceType.getCategoryName());
                                        NewBookCatalog lesson = lessonNewBookCatalog.get(app.getLessonId());
                                        NewBookCatalog unit = units.get(finalLessonToUnit.get(app.getLessonId()));
                                        List<Map<String, Object>> re = new LinkedList<>();
                                        for (String pid : app.getAnswers().values()) {
                                            VacationHomeworkProcessResult pr = processResultsMap.get(pid);
                                            NewQuestion newQuestion = questionMap.get(pr.getQuestionId());
                                            Map<String, Object> mq = translateNewHomeworkProcess(pr, newQuestion, selectItemKey, oct.getValue() + ":" + practiceType.getCategoryName(), Objects.nonNull(unit) ? unit.getAlias() : "", Objects.nonNull(lesson) ? lesson.getAlias() : "", oct, oldQuestionIdToProcessId, processResultsMap);
                                            if (MapUtils.isNotEmpty(mq)) {
                                                re.add(mq);
                                            }
                                        }
                                        return re;
                                    })
                                    .flatMap(Collection::stream)
                                    .forEach(resultList::add);

                        }
                    } else {
                        List<String> pIds = result.findHomeworkProcessIdsByObjectiveConfigType(oct);
                        String selectItemKey = "3," + oct.getKey();
                        selectItemMap.put(selectItemKey, oct.getValue());
                        if (CollectionUtils.isNotEmpty(pIds)) {
                            pIds.stream()
                                    .map(pid -> {
                                        VacationHomeworkProcessResult pr = processResultsMap.get(pid);
                                        NewQuestion newQuestion = questionMap.get(pr.getQuestionId());
                                        return translateNewHomeworkProcess(pr, newQuestion, selectItemKey, oct.getValue(), "", "", oct, oldQuestionIdToProcessId, processResultsMap);
                                    })
                                    .filter(Objects::nonNull)
                                    .forEach(resultList::add);

                        }
                    }
                }
            }
        }
        User user = result != null && result.getUserId() != null ? userLoaderClient.loadUser(result.getUserId()) : null;
        resultList.sort(Comparator.comparingLong(o2 -> ((Date) o2.get("createTime")).getTime()));
        resultList.forEach(o -> o.put("createTime", DateUtils.dateToString((Date) o.get("createTime"), "yyyy-MM-dd HH:mm:ss")));
        return MapUtils.m(
                "stResultDetailList", resultList,
                "realName", user != null ? user.fetchRealname() : "",
                "resultList", JsonUtils.toJson(resultList),
                "selectItemKey", selectItemMap.keySet(),
                "selectItemValue", selectItemMap.values(),
                "homeworkId", homeworkId,
                "studentId", user != null ? user.getId() : "",
                "subject", subject
        );
    }

    /**
     * 学生个人报告
     *
     * @param packageId
     * @param studentId
     * @return
     */
    @Override
    public MapMessage studentPackageReport(String packageId, Long studentId) {
        User user = studentLoaderClient.loadStudent(studentId);
        VacationHomeworkPackage vacationHomeworkPackage = vacationHomeworkPackageDao.load(packageId);
        if (vacationHomeworkPackage == null) {
            return MapMessage.errorMessage("vacationHomeworkPackage is null").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        if (vacationHomeworkPackage.isDisabledTrue()) {
            return MapMessage.errorMessage("假期作业已删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        if (vacationHomeworkPackage.getBookId() == null) {
            return MapMessage.errorMessage("vacationHomeworkPackage's bookId is null").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        if (vacationHomeworkPackage.getClazzGroupId() == null) {
            return MapMessage.errorMessage("vacationHomeworkPackage's clazzGroupId is null").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        NewBookProfile book = newContentLoaderClient.loadBook(vacationHomeworkPackage.getBookId());
        if (book == null) {
            return MapMessage.errorMessage("课本不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_BOOK_NOT_EXIST);
        }
        Set<Long> userIds = studentLoaderClient.loadGroupStudents(vacationHomeworkPackage.getClazzGroupId())
                .stream()
                .filter(Objects::nonNull)
                .map(LongIdEntity::getId)
                .collect(Collectors.toSet());
        if (!userIds.contains(studentId)) {
            return MapMessage.errorMessage("该学生不属于这份假期作业的老师下").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }

        VacationHomeworkWinterPlanCacheMapper winterPlan = vacationHomeworkCacheLoader.loadVacationHomeworkWinterPlanCacheMapper(vacationHomeworkPackage.getBookId());
        if (winterPlan == null) {
            return MapMessage.errorMessage("课本计划不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_WINTER_PLAN_NOT_EXIST);
        }
        Map<String, NewVacationHomeworkStudentDetail> newVacationHomeworkStudentDetailMap = reportWithStudentToPackage(studentId, vacationHomeworkPackage);
        Map bookMap = MapUtils.m(
                "bookId", vacationHomeworkPackage.getBookId(),
                "name", book.getName(),
                "imgUrl", book.getImgUrl(),
                "clazzLevel", book.getClazzLevel(),
                "termType", book.getTermType(),
                "latestVersion", Objects.equals(1, book.getLatestVersion()),
                "subjectId", book.getSubjectId(),
                "seriesId", book.getSeriesId()
        );

        int finishedCount = 0;
        int totalCount = 0;
        int resultWeek = vacationHomeworkPackage.getPlannedDays() / 5;
        //初始化周计划
        List<Map> weekPlans = new ArrayList<>();
        if (MapUtils.isNotEmpty(winterPlan.getWeekPlan())) {
            for (WinterWeekPlan winterWeekPlan : winterPlan.getWeekPlan().values()) {
                if (winterWeekPlan.getWeekRank() > resultWeek) {
                    continue;
                }
                String weekRank = SafeConverter.toString(winterWeekPlan.getWeekRank());
                String title = "第" + NewHomeworkUtils.transferToChinese(SafeConverter.toString(winterWeekPlan.getWeekRank())) + "周";
                List<Map> dayPlans = new ArrayList<>();
                if (MapUtils.isNotEmpty(winterPlan.getWeekPlanDays()) && winterPlan.getWeekPlanDays().containsKey(weekRank)) {
                    List<String> weekDays = winterPlan.getWeekPlanDays().get(weekRank);
                    int i = 0;
                    for (String dayRank : weekDays) {
                        i++;
                        String weekDayPanKey = StringUtils.join(Arrays.asList(weekRank, dayRank), "-");
                        WinterDayPlan winterDayPlan = winterPlan.getDayPlan().get(weekDayPanKey);
                        if (winterDayPlan == null) continue;
                        String homeworkId = packageId + "-" + winterWeekPlan.getWeekRank() + "-" + winterDayPlan.getDayRank() + "-" + studentId;
                        NewVacationHomeworkStudentDetail newVacationHomeworkStudentDetail = new NewVacationHomeworkStudentDetail();
                        if (newVacationHomeworkStudentDetailMap.containsKey(homeworkId)) {
                            newVacationHomeworkStudentDetail = newVacationHomeworkStudentDetailMap.get(homeworkId);
                        } else {
                            newVacationHomeworkStudentDetail.setBegin(false);
                        }
                        boolean finished = newVacationHomeworkStudentDetail.isFinish();
                        String homeworkDetail = null;
                        String detailUrl = "";
                        if (finished) {
                            detailUrl =
                                    UrlUtils.buildUrlQuery("/view/vacationhomework/answerdetail",
                                            MapUtils.m(
                                                    "homeworkId", newVacationHomeworkStudentDetail.getHomeworkId()));
                            finishedCount++;
                            homeworkDetail = "平均分" + newVacationHomeworkStudentDetail.getScore() + ", 完成时间:" + DateUtils.dateToString(new Date(newVacationHomeworkStudentDetail.getFinishTime()), "yyyy年MM月dd日") + ", 用时" + SafeConverter.toLong(newVacationHomeworkStudentDetail.getDuration()) + "分钟";
                        } else {
                            homeworkId = "";
                        }

                        List<ObjectiveConfigType> types = newVacationHomeworkStudentDetail.getObjectiveConfigTypes();
                        dayPlans.add(MapUtils.m("desc", winterDayPlan.getDesc(),
                                "name", winterDayPlan.getName(),
                                "detailUrl", detailUrl,
                                "vacationHomeworkStudentDetail", newVacationHomeworkStudentDetail,
                                "dayRankStr", "Day" + i,
                                "finished", finished,
                                "includeDubbing", CollectionUtils.isNotEmpty(types) && (types.contains(ObjectiveConfigType.DUBBING) || types.contains(ObjectiveConfigType.DUBBING_WITH_SCORE)) && !finished,
                                "homeworkDetail", homeworkDetail,
                                "homeworkId", homeworkId,
                                "dayRank", winterDayPlan.getDayRank()));
                        totalCount++;
                    }
                }
                weekPlans.add(MapUtils.m("scope", winterWeekPlan.getName(),
                        "title", title,
                        "weekRank", winterWeekPlan.getWeekRank(),
                        "dayPlans", dayPlans));
            }

        }

        Map<Long, GroupMapper> groupMapperMap = groupLoaderClient.loadGroups(Collections.singleton(vacationHomeworkPackage.getClazzGroupId()), true);
        String clazzName = null;
        if (groupMapperMap.containsKey(vacationHomeworkPackage.getClazzGroupId())) {
            Long clazzId = groupMapperMap.get(vacationHomeworkPackage.getClazzGroupId()).getClazzId();
            if (clazzId != null) {
                Map<Long, Clazz> classMap = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazzs(Collections.singleton(clazzId))
                        .stream()
                        .collect(Collectors.toMap(Clazz::getId, Function.identity()));
                if (classMap.containsKey(clazzId)) {
                    Clazz clazz = classMap.get(clazzId);
                    clazzName = clazz.formalizeClazzName();
                }
            }
        }
        //初始化关卡状态
        Map<String, Object> levelState = vacationHomeworkLoader.levelState(studentId, packageId, finishedCount);
        return MapMessage.successMessage()
                .add("levelState", levelState)
                .add("time", DateUtils.dateToString(vacationHomeworkPackage.getStartTime(), "yyyy年MM月dd日") + " - " + DateUtils.dateToString(vacationHomeworkPackage.getEndTime(), "yyyy年MM月dd日"))
                .add("progress", finishedCount + "/" + totalCount)
                .add("finishedCount", finishedCount)
                .add("totalCount", totalCount)
                .add("book", bookMap)
                .add("subject", vacationHomeworkPackage.getSubject())
                .add("clazzName", clazzName)
                .add("studentName", user != null ? user.fetchRealname() : "")
                .add("weekPlans", weekPlans);
    }


    private Map<String, NewVacationHomeworkStudentDetail> reportWithStudentToPackage(Long studentId, VacationHomeworkPackage vacationHomeworkPackage) {
        //homeworkId --》
        Map<String, NewVacationHomeworkStudentDetail> result = new LinkedHashMap<>();
        VacationHomeworkCacheMapper vacationHomeworkCacheMapper = vacationHomeworkCacheLoader.loadVacationHomeworkCacheMapper(vacationHomeworkPackage.getClazzGroupId(), studentId);
        if (vacationHomeworkCacheMapper == null || MapUtils.isEmpty(vacationHomeworkCacheMapper.getHomeworkDetail()))
            return Collections.emptyMap();
        Map<String, VacationHomework> vacationHomeworkMap = vacationHomeworkDao.loads(vacationHomeworkCacheMapper.getHomeworkDetail().keySet());
        for (String homeworkId : vacationHomeworkCacheMapper.getHomeworkDetail().keySet()) {
            VacationHomeworkDetailCacheMapper v = vacationHomeworkCacheMapper.getHomeworkDetail().get(homeworkId);
            if (v != null && vacationHomeworkMap.containsKey(homeworkId) && vacationHomeworkMap.get(homeworkId).getPractices() != null) {
                VacationHomework vacationHomework = vacationHomeworkMap.get(homeworkId);
                NewVacationHomeworkStudentDetail newVacationHomeworkStudentDetail = new NewVacationHomeworkStudentDetail();
                newVacationHomeworkStudentDetail.setScore(SafeConverter.toInt(v.getAvgScore()));
                newVacationHomeworkStudentDetail.setScoreLevel(ScoreLevel.processLevel(v.getAvgScore()).getLevel());
                newVacationHomeworkStudentDetail.setDuration(new BigDecimal(SafeConverter.toInt(v.getDuration())).divide(new BigDecimal(60), BigDecimal.ROUND_UP).longValue());
                if (v.isFinished()) {
                    newVacationHomeworkStudentDetail.setFinishTime(v.getFinishAt().getTime());
                    newVacationHomeworkStudentDetail.setFinishAt(DateUtils.dateToString(v.getFinishAt(), DateUtils.FORMAT_SQL_DATE));
                    newVacationHomeworkStudentDetail.setCrmFinishAt(DateUtils.dateToString(v.getFinishAt()));
                    newVacationHomeworkStudentDetail.setFinish(true);
                    newVacationHomeworkStudentDetail.setRepair(vacationHomeworkPackage.getEndTime().before(v.getFinishAt()));
                } else {
                    newVacationHomeworkStudentDetail.setFinish(false);
                }
                newVacationHomeworkStudentDetail.setBegin(true);
                newVacationHomeworkStudentDetail.setHomeworkId(homeworkId);
                List<ObjectiveConfigType> objectiveConfigTypes = new LinkedList<>();
                List<String> objectiveConfigTypeCn = new LinkedList<>();
                for (NewHomeworkPracticeContent content : vacationHomework.getPractices()) {
                    objectiveConfigTypes.add(content.getType());
                    objectiveConfigTypeCn.add(content.getType().getValue());
                }


                newVacationHomeworkStudentDetail.setObjectiveConfigTypes(objectiveConfigTypes);
                newVacationHomeworkStudentDetail.setObjectiveConfigTypeCn(objectiveConfigTypeCn);
                result.put(homeworkId, newVacationHomeworkStudentDetail);
            }
        }
        return result;
    }

    private Map<String, Object> translateNewHomeworkProcess(VacationHomeworkProcessResult pr, NewQuestion newQuestion, String selectItemKey, String itemName, String unitName, String lessonName, ObjectiveConfigType oct, Map<String, String> oldQuestionIdToProcessId, Map<String, VacationHomeworkProcessResult> processResultsMap) {
        if (newQuestion == null || pr == null) {
            return null;
        }
        List<NewHomeworkQuestionFile> files = CollectionUtils.isNotEmpty(pr.getFiles()) ?
                pr.getFiles()
                        .stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()) :
                new LinkedList<>();
        List<BaseHomeworkProcessResult.OralDetail> oralDetails = CollectionUtils.isNotEmpty(pr.getOralDetails()) ?
                pr.getOralDetails()
                        .stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()) :
                new LinkedList<>();
        List<String> audioUrls = new LinkedList<>();
        List<String> audioInfo = new LinkedList<>();
        VoiceEngineType voiceEngineType = pr.getVoiceEngineType();
        handleOralDetail(audioUrls, audioInfo, oralDetails, voiceEngineType);
        List<String> fileNames = new LinkedList<>();
        List<String> relativeUrls = new LinkedList<>();
        for (NewHomeworkQuestionFile n : files) {
            fileNames.add(n.getFileName());
            relativeUrls.add(NewHomeworkQuestionFileHelper.getFileUrl(n));
        }
        long duration = new BigDecimal(pr.getDuration()).divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP).longValue();
        String sourceAnswer = "";
        if (CollectionUtils.isNotEmpty(newQuestion.getAnswers())) {
            sourceAnswer = StringUtils.join(
                    newQuestion
                            .getAnswers()
                            .stream()
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList()), ",");
        }
        String userAnswers = "";
        if (CollectionUtils.isNotEmpty(pr.getUserAnswers())) {
            userAnswers = StringUtils.join(
                    pr.getUserAnswers()
                            .stream()
                            .flatMap(Collection::stream)
                            .map(o -> StringUtils.isBlank(o) ? "未作答" : o)
                            .collect(Collectors.toList()),
                    ",");
        }
        String subGrasp = "";
        if (CollectionUtils.isNotEmpty(pr.getUserAnswers())) {
            subGrasp = StringUtils.join(pr
                            .getSubGrasp()
                            .stream()
                            .flatMap(Collection::stream)
                            .map(o -> Objects.equals(o, Boolean.TRUE) ? "对" : "错")
                            .collect(Collectors.toList()),
                    ",");
        }
        String correctNewHomeworkProcessInfo = oct == ObjectiveConfigType.EXAM &&
                oldQuestionIdToProcessId != null &&
                oldQuestionIdToProcessId.containsKey(pr.getQuestionId()) &&
                processResultsMap.containsKey(oldQuestionIdToProcessId.get(pr.getQuestionId())) ?
                JsonUtils.toJson(processResultsMap.get(oldQuestionIdToProcessId.get(pr.getQuestionId()))) : "";
        boolean isHaveCorrectNewHomeworkProcessInfo = StringUtils.isNotBlank(correctNewHomeworkProcessInfo);


        return MapUtils.m(
                "itemName", itemName,
                "qId", pr.getQuestionId(),
                "selectItemKey", selectItemKey,
                "createTime", pr.getCreateAt(),
                "unitName", unitName,
                "lessonName", lessonName,
                "standardScore", pr.getStandardScore(),
                "audioUrls", audioUrls,
                "sourceAnswer", sourceAnswer,
                "audioInfo", audioInfo,
                "isHaveCorrectNewHomeworkProcessInfo", isHaveCorrectNewHomeworkProcessInfo,
                "correctNewHomeworkProcessInfo", correctNewHomeworkProcessInfo,
                "score", pr.getScore(),
                "duration", (duration / 60 > 0 ? (duration / 60 + "分") : "") + duration % 60 + "秒",
                "grasp", SafeConverter.toBoolean(pr.getGrasp()) ? "是" : "否",
                "userAnswers", userAnswers,
                "subGrasp", subGrasp,
                "files", pr.getFiles(),
                "fileNames", fileNames,
                "relativeUrls", relativeUrls,
                "oralDetails", pr.getOralDetails(),
                "content", JsonUtils.toJson(pr),
                "clientName", pr.getClientName(),
                "clientType", pr.getClientType(),
                "appOralScoreLevel", pr.getAppOralScoreLevel() != null ? pr.getAppOralScoreLevel() : ""
        );
    }

    private void handleOralDetail(List<String> audioUrls, List<String> audioInfo, List<BaseHomeworkProcessResult.OralDetail> oralDetails, VoiceEngineType voiceEngineType) {
        for (BaseHomeworkProcessResult.OralDetail oralDetail : oralDetails) {
            String voiceUrl = oralDetail.getAudio();
            if (StringUtils.isNotEmpty(voiceUrl)) {
                if (voiceEngineType == VoiceEngineType.ChiVox) {
                    voiceUrl = "http://" + voiceUrl + ".mp3";
                }
                audioUrls.add(voiceUrl);
                audioInfo.add(oralDetail.getMacScore() + "/" + oralDetail.getFluency() + "/" + oralDetail.getIntegrity() + "/" + oralDetail.getPronunciation());
            }
        }
    }


    /**
     * 个人详情base_app
     *
     * @param categoryId 练习类型id
     * @param lessonId   我是一个很奇怪的属性
     * @return 返回个人base_app详情
     */
    private List<Map<String, Object>> internalProcessHomeworkAnswer(User user, VacationHomeworkResult vacationHomeworkResult, String categoryId, String lessonId, PracticeType practiceType, NewHomeworkApp newHomeworkApp, ObjectiveConfigType objectiveConfigType) {
        // 取出base_app类型,
        if (Objects.isNull(newHomeworkApp) || CollectionUtils.isEmpty(newHomeworkApp.getQuestions())) {
            return Collections.emptyList();
        }
        List<String> qIds = newHomeworkApp
                .getQuestions()
                .stream()
                .map(NewHomeworkQuestion::getQuestionId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(qIds)) {
            return Collections.emptyList();
        }
        NatureSpellingType natureSpellingType = NatureSpellingType.of(SafeConverter.toInt(categoryId));
        VacationProcessAppDetailByCategoryIdTemplate template = processAppDetailByCategoryIdFactory.getTemplate(natureSpellingType);
        if (template == null) {
            return Collections.emptyList();
        }
        Map<String, VacationHomeworkProcessResult> homeworkProcessResultMap = vacationHomeworkProcessResultDao.loads(vacationHomeworkResult.findHomeworkProcessIdsForBaseAppByCategoryIdAndLessonId(categoryId, lessonId, objectiveConfigType));
        Map<String, VacationHomeworkProcessResult> dataInfo = homeworkProcessResultMap
                .values()
                .stream()
                .collect(Collectors
                        .toMap(VacationHomeworkProcessResult::getQuestionId, Function.identity()));
        Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(qIds);
        List<Long> sentenceIds = newQuestionMap
                .values()
                .stream()
                .map(NewQuestion::getSentenceIds)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Map<Long, Sentence> sentenceMap = englishContentLoaderClient.loadEnglishSentences(sentenceIds);
        CategoryHandlerContext categoryHandlerContext = new CategoryHandlerContext(sentenceMap,
                newQuestionMap,
                dataInfo,
                qIds, practiceType, user);
        template.processPersonalCategory(categoryHandlerContext);
        return categoryHandlerContext.getResult();
    }

    private Map<ObjectiveConfigType, Object> processNewHomework(User user, VacationHomework vacationHomework, VacationHomeworkResult vacationHomeworkResult) {
        // 处理学生的做题结果，最里面的map为NewHomeworkProcessResult的简版
        Map<ObjectiveConfigType, NewHomeworkPracticeContent> map = vacationHomework.findPracticeContents();
        List<String> allQuestionIds = vacationHomework.findAllQuestionIds();
        Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(allQuestionIds);
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();

        List<String> vacationHomeworkProcessIds = vacationHomeworkResult.findAllHomeworkProcessIds(true);
        Map<String, VacationHomeworkProcessResult> vacationHomeworkProcessResultMap = vacationHomeworkProcessResultDao.loads(vacationHomeworkProcessIds);
        ReportRateContext reportRateContext =
                new ReportRateContext(user,
                        vacationHomeworkResult,
                        vacationHomework,
                        allQuestionMap,
                        vacationHomeworkProcessResultMap,
                        contentTypeMap);
        for (Map.Entry<ObjectiveConfigType, NewHomeworkPracticeContent> entry : map.entrySet()) {
            ObjectiveConfigType key = entry.getKey();
            reportRateContext.setType(key);
            VacationProcessNewHomeworkAnswerDetailTemplate template = vacationProcessNewHomeworkAnswerDetailFactory.getTemplate(key);
            if (template != null) {
                template.processNewHomeworkAnswerDetailPersonal(reportRateContext);
            }
        }
        return reportRateContext.getResultMap();
    }


    public MapMessage pushShareJztMsg(List<String> packageIds, Teacher teacher) {
        ShareVacationReportCacheManager shareVacationReportCacheManager = newHomeworkCacheService.getShareVacationReportCacheManager();
        Map<String, VacationHomeworkPackage> vacationHomeworkPackageMap = vacationHomeworkPackageDao.loads(packageIds);
        List<String> successPackages = new LinkedList<>();
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());

        //抽奖数目
        int rewardNum = 0;
        Date currentTime = new Date();
        for (VacationHomeworkPackage vacationHomeworkPackage : vacationHomeworkPackageMap.values()) {
            if (vacationHomeworkPackage.getStartTime().after(new Date()))
                continue;
            String cacheKey = shareVacationReportCacheManager.getCacheKey(vacationHomeworkPackage.getId());
            Integer cacheValue = shareVacationReportCacheManager.load(cacheKey);
            if (cacheValue != null)
                continue;
            String iMContent = "家长好，" + vacationHomeworkPackage.getSubject().getValue() + "假期作业已开始，有部分孩子进度落后，请家长督促";
            String urlLink = UrlUtils.buildUrlQuery("/view/mobile/common/vacationreport/clazzreport", MapUtils.m("packageId", vacationHomeworkPackage.getId()));

            //新的群组消息ScoreCircle
            ScoreCircleQueueCommand circleQueueCommand = new ScoreCircleQueueCommand();
            circleQueueCommand.setGroupId(vacationHomeworkPackage.getClazzGroupId());
            circleQueueCommand.setCreateDate(new Date());
            circleQueueCommand.setGroupCircleType("VACATION_HOMEWORK_REPORT");
            circleQueueCommand.setTypeId(vacationHomeworkPackage.getId());
            circleQueueCommand.setImgUrl("");
            circleQueueCommand.setLinkUrl(urlLink);
            circleQueueCommand.setContent(iMContent);
            newHomeworkParentQueueProducer.getProducer().produce(Message.newMessage().writeObject(circleQueueCommand));

            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacher.getId());
            Long teacherId = mainTeacherId == null ? teacher.getId() : mainTeacherId;
            //这里才是取所有的学科
            Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
            List<Subject> subjectList = teacherLoaderClient.loadTeachers(relTeacherIds).values().stream().map(Teacher::getSubject).collect(Collectors.toList());
            String subjectsStr = "（" + StringUtils.join(subjectList.stream().sorted(Comparator.comparingInt(Subject::getId)).map(Subject::getValue).toArray(), "，") + "）";
            String em_push_title = teacher.fetchRealnameIfBlankId() + subjectsStr + "：" + iMContent;

            //新的push
            Map<String, Object> extras = new HashMap<>();
            extras.put("studentId", "");
            extras.put("url", urlLink);
            extras.put("s", ParentAppPushType.REPORT.name());
            appMessageServiceClient.sendAppJpushMessageByTags(
                    em_push_title,
                    com.voxlearning.utopia.service.push.api.constant.AppMessageSource.PARENT,
                    Collections.singletonList(JpushUserTag.CLAZZ_GROUP_REFACTOR.generateTag(SafeConverter.toString(vacationHomeworkPackage.getClazzGroupId()))),
                    null,
                    extras);

            // 发送广播
            Map<String, Object> map = new HashMap<>();
            map.put("messageType", "share_progress");
            map.put("groupId", vacationHomeworkPackage.getClazzGroupId());
            map.put("homeworkId", vacationHomeworkPackage.getId());
            map.put("subject", vacationHomeworkPackage.getSubject());
            map.put("teacherId", teacher.getId());
            map.put("createAt", vacationHomeworkPackage.getCreateAt().getTime());
            map.put("startTime", vacationHomeworkPackage.getStartTime().getTime());
            map.put("endTime", vacationHomeworkPackage.getEndTime().getTime());
            map.put("homeworkType", NewHomeworkType.WinterVacation);
            newHomeworkPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));

            //抽奖截止时间之后，不可以在抽奖
            //if (currentTime.before(NewHomeworkConstants.VH_LOTTERY_ENTRANCE_OFFLINE_DATE)) {
            //    //添加抽奖奖励
            //    MapMessage mapMessage = campaignServiceClient.getCampaignService().addLotteryFreeChance(CampaignType.SUMMER_VOCATION_LOTTERY_2018, teacherDetail.getId(), 2);
            //    if (mapMessage.isSuccess()) {
            //        rewardNum = 2 + rewardNum;
            //    }
            //}
            shareVacationReportCacheManager.set(cacheKey, 1);
            successPackages.add(vacationHomeworkPackage.getId());
        }
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("rewardNum", rewardNum);
        mapMessage.add("successPackages", successPackages);
        return mapMessage;
    }

    @Override
    public MapMessage shareReportWeiXin(List<String> packageIds, Teacher teacher) {
        ShareWeiXinVacationReportCacheManager shareWeiXinVacationReportCacheManager = newHomeworkCacheService.getShareWeiXinVacationReportCacheManager();
        Map<String, VacationHomeworkPackage> vacationHomeworkPackageMap = vacationHomeworkPackageDao.loads(packageIds);
        List<String> successPackages = new LinkedList<>();
        //TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
        int rewardNum = 0;
        //Date currentTime = new Date();
        for (VacationHomeworkPackage vacationHomeworkPackage : vacationHomeworkPackageMap.values()) {
            if (vacationHomeworkPackage.getStartTime().after(new Date()))
                continue;
            String cacheKey = shareWeiXinVacationReportCacheManager.getCacheKey(vacationHomeworkPackage.getId());
            Integer cacheValue = shareWeiXinVacationReportCacheManager.load(cacheKey);
            if (cacheValue != null)
                continue;
            //if (currentTime.before(NewHomeworkConstants.VH_LOTTERY_ENTRANCE_OFFLINE_DATE)) {
            //    MapMessage mapMessage = campaignServiceClient.getCampaignService().addLotteryFreeChance(CampaignType.SUMMER_VOCATION_LOTTERY_2018, teacherDetail.getId(), 2);
            //    if (mapMessage.isSuccess()) {
            //        rewardNum = rewardNum + 2;
            //    }
            //}
            shareWeiXinVacationReportCacheManager.set(cacheKey, 1);
            successPackages.add(vacationHomeworkPackage.getId());
        }
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("successPackages", successPackages);
        mapMessage.add("rewardNum", rewardNum);
        return mapMessage;
    }

    @Override
    public MapMessage remindStudentMsg(String packageId, Teacher teacher) {
        VacationHomeworkPackage vacationHomeworkPackage = vacationHomeworkPackageDao.load(packageId);
        if (vacationHomeworkPackage.getStartTime().after(new Date())) {
            return MapMessage.errorMessage();
        }
        RemindStudentVacationProgressCacheManager remindStudentVacationProgressCacheManager = newHomeworkCacheService.getRemindStudentVacationProgressCacheManager();
        String cacheKey = remindStudentVacationProgressCacheManager.getCacheKey(vacationHomeworkPackage.getId());
        Integer cacheValue = remindStudentVacationProgressCacheManager.load(cacheKey);
        if (cacheValue != null) {
            return MapMessage.errorMessage("今日已提醒学生！");
        }
        remindStudentVacationProgressCacheManager.set(cacheKey, 1);

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
        String iMContent = teacherDetail.respectfulName() + "检查了假期作业进度，提醒你继续加油！";
        String urlLink = UrlUtils.buildUrlQuery("/studentMobile/homework/vacation/packagelist.vpage", MiscUtils.m("packageId", vacationHomeworkPackage.getId()));

        //学生端发送提醒消息
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("link", urlLink);
        extInfo.put("tag", ParentMessageTag.老师通知.name());
        extInfo.put("t", "h5");
        extInfo.put("key", "j");
        extInfo.put("s", StudentAppPushType.HOLIDAY_HOMEWORK_HURRY_REMIND.getType());
        Map<Long, VacationHomeworkCacheMapper> mapVacationHomeworkCacheMapper =
                vacationHomeworkCacheLoader.loadVacationHomeworkCacheMappers(vacationHomeworkPackage.getClazzGroupId()).stream()
                        .filter(Objects::nonNull)
                        .filter(o -> o.getStudentId() != null)
                        .collect(Collectors.toMap(VacationHomeworkCacheMapper::getStudentId, Function.identity()));
        List<GroupStudentTuple> tuples = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByGroupId(vacationHomeworkPackage.getClazzGroupId());
        if (CollectionUtils.isEmpty(tuples)) {
            return MapMessage.errorMessage("未查询到班组学生！");
        }
        List<Long> studentIds = tuples.stream()
                .map(GroupStudentTuple::getStudentId).collect(Collectors.toList());
        studentIds = studentIds.stream()
                .filter(
                        s -> !mapVacationHomeworkCacheMapper.containsKey(s)
                                || (mapVacationHomeworkCacheMapper.get(s) != null && !mapVacationHomeworkCacheMapper.get(s).isFinished()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(studentIds)) {
            appMessageServiceClient.sendAppJpushMessageByIds(iMContent, AppMessageSource.STUDENT, studentIds, extInfo);
        }

        // 发送广播
        Map<String, Object> map = new HashMap<>();
        map.put("messageType", "remind");
        map.put("groupId", vacationHomeworkPackage.getClazzGroupId());
        map.put("homeworkId", vacationHomeworkPackage.getId());
        map.put("subject", vacationHomeworkPackage.getSubject());
        map.put("teacherId", teacher.getId());
        map.put("createAt", vacationHomeworkPackage.getCreateAt().getTime());
        map.put("startTime", vacationHomeworkPackage.getStartTime().getTime());
        map.put("endTime", vacationHomeworkPackage.getEndTime().getTime());
        map.put("homeworkType", NewHomeworkType.WinterVacation);
        newHomeworkPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));

        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("successPackage", vacationHomeworkPackage.getId());
        return mapMessage;
    }

    @Override
    public MapMessage studentDubbingWithScoreDetail(String homeworkId, Long studentId, String dubbingId) {
        try {
            VacationHomework vacationHomework = vacationHomeworkDao.load(homeworkId);
            if (vacationHomework == null) {
                return MapMessage.errorMessage("作业不存在");
            }
            NewHomeworkPracticeContent target = vacationHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.DUBBING_WITH_SCORE);
            if (target == null) {
                return MapMessage.errorMessage("作业不包含" + ObjectiveConfigType.DUBBING_WITH_SCORE.getValue());
            }
            Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(Collections.singleton(dubbingId));
            if (MapUtils.isEmpty(dubbingMap) || dubbingMap.get(dubbingId) == null) {
                return MapMessage.errorMessage("配音不存在");
            }
            VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.load(homeworkId);
            String duration;
            double scoreResult;
            String studentVideoUrl;
            boolean syntheticSuccess = true;
            if (vacationHomeworkResult == null
                    || MapUtils.isEmpty(vacationHomeworkResult.getPractices())
                    || vacationHomeworkResult.getPractices().get(ObjectiveConfigType.DUBBING_WITH_SCORE) == null) {
                return MapMessage.errorMessage("不存在该学生的配音记录");
            }
            NewHomeworkResultAnswer newHomeworkResultAnswer = vacationHomeworkResult.getPractices().get(ObjectiveConfigType.DUBBING_WITH_SCORE);
            LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = newHomeworkResultAnswer.getAppAnswers();
            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = appAnswers.get(dubbingId);
            if (newHomeworkResultAppAnswer == null || MapUtils.isEmpty(newHomeworkResultAppAnswer.getAnswers())) {
                return MapMessage.errorMessage("不存在该学生的配音记录");
            }
            // 该作业形式的总体得分(8分制)
            List<String> vacationHomeworkProcessResultIds = new ArrayList<>(newHomeworkResultAppAnswer.getAnswers().values());
            Map<String, VacationHomeworkProcessResult> vacationHomeworkProcessResultMap = vacationHomeworkProcessResultDao.loads(vacationHomeworkProcessResultIds);
            if (MapUtils.isEmpty(vacationHomeworkProcessResultMap)) {
                return MapMessage.errorMessage("不存在该学生的配音记录");
            }
            Map<String, VacationHomeworkProcessResult> homeworkProcessResultMap = vacationHomeworkProcessResultMap.values()
                    .stream()
                    .collect(Collectors.toMap(VacationHomeworkProcessResult::getQuestionId, Function.identity()));
            // 计算8分制分数
            List<VacationHomeworkProcessResult> vacationHomeworkProcessResultList = new ArrayList<>();
            Set<String> questionIds = newHomeworkResultAppAnswer.getAnswers().keySet();
            for (String questionId : questionIds) {
                VacationHomeworkProcessResult vacationHomeworkProcessResult = homeworkProcessResultMap.get(questionId);
                if (vacationHomeworkProcessResult != null) {
                    vacationHomeworkProcessResultList.add(vacationHomeworkProcessResult);
                }
            }
            if (CollectionUtils.isEmpty(vacationHomeworkProcessResultList)) {
                return MapMessage.errorMessage("不存在该学生的配音记录");
            }
            double totalScore = vacationHomeworkProcessResultList.stream().mapToDouble(processResult -> SafeConverter.toDouble(processResult.getActualScore())).sum();
            scoreResult = SafeConverter.toDouble(Math.floor(totalScore / vacationHomeworkProcessResultList.size()));
            int time = new BigDecimal(SafeConverter.toLong(newHomeworkResultAppAnswer.processDuration()))
                    .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                    .intValue();
            duration = NewHomeworkUtils.handlerEnTime(time);
            String hyid = new DubbingSyntheticHistory.ID(homeworkId, studentId, dubbingId).toString();
            Map<String, DubbingSyntheticHistory> dubbingSyntheticHistoryMap = newHomeworkLoader.loadDubbingSyntheticHistories(Collections.singleton(hyid));
            if (dubbingSyntheticHistoryMap.containsKey(hyid)) {
                DubbingSyntheticHistory dubbingSyntheticHistory = dubbingSyntheticHistoryMap.get(hyid);
                syntheticSuccess = SafeConverter.toBoolean(dubbingSyntheticHistory.isSyntheticSuccess(vacationHomework.getCreateAt()));
            }
            studentVideoUrl = newHomeworkResultAppAnswer.getVideoUrl();
            //判断该配音是否是欢快歌曲类型   欢快歌曲类型ID:DC_10300000140166
            DubbingCategory dubbingCategory = dubbingLoaderClient.loadDubbingCategoriesByIds(Collections.singleton(dubbingMap.get(dubbingId).getCategoryId())).get(dubbingMap.get(dubbingId).getCategoryId());
            boolean isHappySong = dubbingCategory != null && Objects.equals("DC_10300000140166", dubbingCategory.getParentId());
            Student student = studentLoaderClient.loadStudent(studentId);
            Map<String, Object> result = MapUtils.m(
                    "userName", student != null ? student.fetchRealname() : "",
                    "imageUrl", student != null ? student.fetchImageUrl() : "",
                    "dubbingId", dubbingId,
                    "dubbingName", dubbingMap.get(dubbingId).getVideoName(),
                    "coverUrl", dubbingMap.get(dubbingId).getCoverUrl(),
                    "score", scoreResult,
                    "duration", duration,
                    "isHappySong", isHappySong,
                    "syntheticSuccess", syntheticSuccess,
                    "studentVideoUrl", studentVideoUrl);
            return MapMessage.successMessage().add("content", result);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
    }
}
