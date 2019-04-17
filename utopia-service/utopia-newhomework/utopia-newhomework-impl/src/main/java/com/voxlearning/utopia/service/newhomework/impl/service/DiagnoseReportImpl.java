package com.voxlearning.utopia.service.newhomework.impl.service;


import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.PropertiesUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.content.api.NewKnowledgePointLoader;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePoint;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.constant.IntelligentRecommendStudentStatus;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching.*;
import com.voxlearning.utopia.service.newhomework.api.service.DiagnoseReportService;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.HomeworkSelfStudyRefDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.SelfStudyHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.strategy.report.DiagnoseReportFactory;
import com.voxlearning.utopia.service.newhomework.impl.strategy.report.DiagnoseReportStrategy;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisVariant;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.video.MicroVideoTask;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.INTELLIGENT_TEACHING_CONFIGTYPE;
import static com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS;
import static com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType.ORAL_INTERVENTIONS;

@Named
@Service(interfaceClass = DiagnoseReportService.class)
@ExposeService(interfaceClass = DiagnoseReportService.class)
public class DiagnoseReportImpl extends SpringContainerSupport implements DiagnoseReportService {

    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject private QuestionLoaderClient questionLoaderClient;
    @Inject private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private HomeworkSelfStudyRefDao homeworkSelfStudyRefDao;
    @Inject private SelfStudyHomeworkResultDao selfStudyHomeworkResultDao;
    @Inject private SelfStudyHomeworkLoaderImpl selfStudyHomeworkLoader;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private DiagnoseReportFactory diagnoseReportFactory;
    @Inject private NewContentLoaderClient newContentLoaderClient;
    @Inject private SelfStudyHomeworkDao selfStudyHomeworkDao;
    @Inject private IntelDiagnosisClient intelDiagnosisClient;
    @Inject private NewKnowledgePointLoader newKnowledgePointLoader;

    @Inject private RaikouSDK raikouSDK;

    @Override
    public List<ClazzGroupInfoResp> fetchClazzInfo(Collection<Long> teacherIds) {
        // 老师分组
        Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(teacherIds, false);
        // 分组id
        List<Long> groupIds = new LinkedList<>();
        // 班级id
        List<Long> clazzIds = new LinkedList<>();
        // clazz id -> group id map
        Map<Long, List<Long>> clazzIdGroupIdMap = new HashMap<>();
        // 分组id -> 学科
        Map<Long, Subject> groupIdSubjectMap = new HashMap<>();

        teacherGroups.forEach((teacherId, groups) -> groups.forEach(group -> {
            if (group.isTeacherGroupRefStatusValid(teacherId)) {
                groupIds.add(group.getId());
                clazzIds.add(group.getClazzId());
                clazzIdGroupIdMap.computeIfAbsent(group.getClazzId(), k -> new ArrayList<>()).add(group.getId());
                groupIdSubjectMap.put(group.getId(), group.getSubject());
            }
        }));

        Map<Long, List<Long>> groupStudentIds = studentLoaderClient.loadGroupStudentIds(groupIds);
        List<Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .filter(e -> e.isPublicClazz() && !e.isTerminalClazz())
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());

        List<ClazzGroupInfoResp> clazzGroupInfoRespList = Lists.newArrayList();
        clazzs.stream().filter(clazz -> clazzIdGroupIdMap.containsKey(clazz.getId()))
                .forEach(c -> {
                    ClazzGroupInfoResp clazzGroupInfoResp = new ClazzGroupInfoResp();
                    clazzGroupInfoResp.setClazzId(c.getId());
                    clazzGroupInfoResp.setClazzName(c.formalizeClazzName());
                    List<Long> gids = clazzIdGroupIdMap.get(c.getId());
                    List<ClazzGroupInfoResp.GroupInfo> groupInfos = new LinkedList<>();
                    gids.forEach(gid -> {
                        if (CollectionUtils.isNotEmpty(groupStudentIds.get(gid))) {
                            Subject subject = groupIdSubjectMap.get(gid);
                            if (subject.equals(Subject.ENGLISH) || subject.equals(Subject.MATH)) {//FIXME 诊断报告目前只有数学和英语, 只显示老师所教数学英语班级
                                groupInfos.add(new ClazzGroupInfoResp.GroupInfo(gid, subject, subject.getValue()));
                            }
                        }
                    });
                    if (!groupInfos.isEmpty()) {
                        clazzGroupInfoResp.setGroupInfos(groupInfos);
                        clazzGroupInfoRespList.add(clazzGroupInfoResp);
                    }
                });
        return clazzGroupInfoRespList;
    }

    @Override
    public PageImpl<DiagnoseReportReportListResp> fetchReportList(Long groupId, Pageable pageable, Subject subject) {
        if (groupId == null || pageable == null || subject == null) {
            return new PageImpl<>(Collections.emptyList());
        }
        try {
            Page<NewHomework> homeworkPage = newHomeworkLoader.loadIncludeIntelligentTeachingGroupHomeworks(Collections.singleton(groupId), subject)
                    .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                    .toPage(pageable);
            List<NewHomework> newHomeworkList = homeworkPage.getContent();
            Map<String, Map<ObjectiveConfigType, Map<String, String>>> homeworkQuestionBoxIdMap = Maps.newLinkedHashMap();
            newHomeworkList.forEach(newHomework -> {
                Map<ObjectiveConfigType, Map<String, String>> configTypeMap = Maps.newLinkedHashMap();
                for (ObjectiveConfigType diagnoseConfigType : INTELLIGENT_TEACHING_CONFIGTYPE) {
                    Map<String, String> questionBoxMap = newHomework.findNewHomeworkQuestions(diagnoseConfigType)
                            .stream()
                            .collect(Collectors.toMap(NewHomeworkQuestion::getQuestionBoxId, n -> SafeConverter.toString(n.getQuestionBoxName(), "课时讲练测"), (key1, key2) -> key2));
                    if (MapUtils.isNotEmpty(questionBoxMap)) {
                        configTypeMap.put(diagnoseConfigType, questionBoxMap);
                        homeworkQuestionBoxIdMap.put(newHomework.getId(), configTypeMap);
                    }
                }
            });
            //<homeworkId, Set<NewHomeworkResult>>
            Map<String, Set<NewHomeworkResult>> homeworkResultMap = newHomeworkResultLoader.findByHomeworksForReport(newHomeworkList);
            List<DiagnoseReportReportListResp> results = Lists.newLinkedList();
            newHomeworkList.forEach(newHomework -> {
                DiagnoseReportReportListResp reportListResp = new DiagnoseReportReportListResp();
                reportListResp.setReportTitle(DateUtils.dateToString(newHomework.getCreateAt(), "MM月dd日"));
                Set<NewHomeworkResult> newHomeworkResults = homeworkResultMap.get(newHomework.getId());
                if (CollectionUtils.isEmpty(newHomeworkResults) || newHomeworkResults.stream().noneMatch(o -> o.isFinished()
                        && (o.getPractices().get(ObjectiveConfigType.INTELLIGENT_TEACHING) != null || o.getPractices().get(ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING) != null))) {
                    return;
                }

                Map<ObjectiveConfigType, Map<String, String>> configTypeMap = homeworkQuestionBoxIdMap.get(newHomework.getId());
                for (Map.Entry<ObjectiveConfigType, Map<String, String>> configTypeEntry : configTypeMap.entrySet()) {
                    ObjectiveConfigType configType = configTypeEntry.getKey();
                    double avgScore = newHomeworkResults.stream()
                            .filter(o -> o.isFinished() && o.getPractices().get(configType) != null)
                            .mapToDouble(p -> p.getPractices().get(configType).getScore()).average().orElse(0D);

                    List<DiagnoseReportReportListResp.QuestionBox> questionBoxes = Lists.newLinkedList();
                    configTypeEntry.getValue().forEach((questionBoxId, questionBoxName) -> {
                        DiagnoseReportReportListResp.QuestionBox questionBox = new DiagnoseReportReportListResp.QuestionBox(questionBoxId, questionBoxName,
                                UrlUtils.buildUrlQuery("/teacher/new/homework/intelligent/teaching/reportdetail.vpage",
                                        MapUtils.m("hid", newHomework.getId(), "objectiveConfigType", configType, "questionBoxId", questionBoxId)));
                        questionBoxes.add(questionBox);
                    });
                    reportListResp.getConfigTypes().add(new DiagnoseReportReportListResp.ConfigType(configType.name(), configType.getValue(), Math.round(avgScore), questionBoxes));
                }
                results.add(reportListResp);
            });
            return new PageImpl<>(results, pageable, homeworkPage.getTotalElements());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new PageImpl<>(Collections.emptyList());
        }
    }

    @Override
    public DiagnoseReportDetailResp fetchReportDetail(String hid, ObjectiveConfigType configType, String questionBoxId) {
        NewHomework newHomework = newHomeworkLoader.load(hid);
        if (newHomework == null || !newHomework.getIncludeIntelligentTeaching()) {
            return null;
        }
        List<NewHomeworkQuestion> questionBoxQuestions = newHomework.findNewHomeworkQuestions(configType).stream()
                .filter(o -> o.getQuestionBoxId().equals(questionBoxId)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(questionBoxQuestions)) {
            return null;
        }

        List<String> questionBoxQids = Lists.transform(questionBoxQuestions, NewHomeworkQuestion::getQuestionId);
        Map<String, NewQuestion> questionBoxNewQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionBoxQids);
        Map<Long, User> studentMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId()).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentMap.keySet(), Boolean.TRUE).values().stream()
                .filter(BaseHomeworkResult::isFinished)
                .collect(Collectors.toMap(BaseHomeworkResult::getUserId, Function.identity()));

        //原作业学生做题情况
        List<String> allProcessIds = Lists.newLinkedList();
        newHomeworkResultMap.values().forEach(newHomeworkResult -> allProcessIds.addAll(Maps.filterKeys(newHomeworkResult.findProcessAnswersMap(configType), questionBoxQids::contains).values()));
        Map<String, SubHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(allProcessIds);

        //订正作业学生做题情况
        Set<String> selfStudyHids = homeworkSelfStudyRefDao.loadSelfStudyHomeworkIds(newHomework.getId(), studentMap.values());
        Map<String, SelfStudyHomework> selfStudyHomeworkMap = selfStudyHomeworkLoader.loadSelfStudyHomework(selfStudyHids);
        Map<String, SelfStudyHomeworkResult> studyHomeworkResultMap = Maps.filterValues(selfStudyHomeworkResultDao.loads(selfStudyHids), SelfStudyHomeworkResult::isFinished);

        String courseId = questionBoxQuestions.get(0).getCourseId();
        //根据学科和作业形式确定策略
        DiagnoseReportStrategy strategy = diagnoseReportFactory.getDiagnoseReportStrategy(newHomework.getSubject(), configType);
        DiagnoseReportDetailResp diagnoseReportDetail = strategy.getDiagnoseReportDetail(courseId, questionBoxId, questionBoxQids, questionBoxNewQuestionMap, studentMap,
                newHomeworkResultMap, processResultMap, selfStudyHomeworkMap, studyHomeworkResultMap);

        //Fixme 数学老数据questionBoxName没有存,为空的话去查sectionName. 事实上线上应该不存在这种数据, 考虑可以不用特殊处理
        String questionBoxName = questionBoxQuestions.get(0).getQuestionBoxName();
        if (StringUtils.isBlank(questionBoxName) && Subject.MATH.equals(newHomework.getSubject())) {
            NewBookCatalog newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(questionBoxId);
            if (newBookCatalog != null) {
                questionBoxName = newBookCatalog.getName();
            }
        }
        if (StringUtils.isEmpty(questionBoxName)) {
            questionBoxName = "课时讲练测";
        }
        diagnoseReportDetail.setQuestionBoxName(questionBoxName);
        diagnoseReportDetail.setQuestionBoxId(questionBoxId);
        diagnoseReportDetail.setHid(newHomework.getId());
        return diagnoseReportDetail;
    }

    @Override
    public List<OralStudentQuestionResp> oralStudentQuestionDetail(String hid, ObjectiveConfigType configType, String questionId) {
        NewHomework newHomework = newHomeworkLoader.load(hid);
        if (newHomework == null || !newHomework.getIncludeIntelligentTeaching()) {
            return null;
        }

        Map<Long, User> studentMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId()).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentMap.keySet(), Boolean.TRUE).values().stream()
                .filter(BaseHomeworkResult::isFinished)
                .collect(Collectors.toMap(BaseHomeworkResult::getUserId, Function.identity()));

        //原作业学生做题情况
        List<String> allProcessIds = Lists.newLinkedList();
        newHomeworkResultMap.values().forEach(newHomeworkResult -> allProcessIds.addAll(Maps.filterKeys(newHomeworkResult.findProcessAnswersMap(configType), questionId::equals).values()));
        Map<String, SubHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(allProcessIds);

        //订正作业学生做题情况
        Set<String> selfStudyHids = homeworkSelfStudyRefDao.loadSelfStudyHomeworkIds(newHomework.getId(), studentMap.values());
        Map<String, SelfStudyHomework> selfStudyHomeworkMap = selfStudyHomeworkLoader.loadSelfStudyHomework(selfStudyHids);

        NewHomeworkQuestion question = newHomework.findNewHomeworkQuestions(configType).stream()
                .filter(o -> o.getQuestionId().equals(questionId)).findFirst().orElseThrow(() -> new IllegalStateException("数据异常"));

        //<questionId, Set<studentId>>
        Map<String, Set<Long>> questionStudentsMap = getQuestionUnGraspStudentsMap(selfStudyHomeworkMap, question.getQuestionBoxId());
        List<OralStudentQuestionResp> respList = Lists.newLinkedList();
        for (SubHomeworkProcessResult processResult : processResultMap.values()) {
            OralStudentQuestionResp resp = new OralStudentQuestionResp();
            Long userId = processResult.getUserId();
            resp.setStudentId(userId);
            resp.setStudentName(studentMap.get(userId).fetchRealnameIfBlankId());
            Set<Long> unGraspStudents = questionStudentsMap.get(processResult.getQuestionId());
            resp.setPreStatus(unGraspStudents != null && unGraspStudents.contains(userId) ? "未通过" : "通过");
            List<List<BaseHomeworkProcessResult.OralDetail>> oralDetails = processResult.getOralDetails();
            if (CollectionUtils.isNotEmpty(oralDetails) && CollectionUtils.isNotEmpty(oralDetails.get(0))) {
                BaseHomeworkProcessResult.OralDetail oralDetail = oralDetails.get(0).get(0);
                resp.setVoiceUrl(oralDetail.getAudio());
                respList.add(resp);
            }
        }
        return respList;
    }

    @Override
    public IntelligentTeachingRecommendResp fetchIntelligentTeachingRecommend(String homeworkId) {
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        List<User> groupStudents = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId());
        Set<String> selfStudyHomeworkIds = homeworkSelfStudyRefDao.loadSelfStudyHomeworkIds(newHomework.getId(), groupStudents);

        Map<Long, SelfStudyHomework> studyHomeworkMap = selfStudyHomeworkDao.loads(selfStudyHomeworkIds).values()
                .stream()
                .filter(o -> !Collections.disjoint(NewHomeworkConstants.COURSE_APP_CONFIGTYPE, o.findPracticeContents().keySet()))
                .collect(Collectors.toMap(SelfStudyHomework::getStudentId, Function.identity()));

        Map<String, SelfStudyHomeworkResult> selfStudyHomeworkResultMap = selfStudyHomeworkResultDao.loads(selfStudyHomeworkIds);
        Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), Lists.transform(groupStudents, User::getId), false);

        Set<String> courseIdSet = Sets.newHashSet();
        Set<String> videoIdSet = Sets.newHashSet();
        Map<String, Integer> courseStudentCountMap = Maps.newHashMap();
        IntelligentTeachingRecommendResp recommendResp = new IntelligentTeachingRecommendResp();
        int totalStudentCount = 0;
        for (User user : groupStudents) {
            IntelligentTeachingRecommendResp.StudentFinishDetail studentFinishDetail = new IntelligentTeachingRecommendResp.StudentFinishDetail();
            recommendResp.getStudentFinishDetails().add(studentFinishDetail);
            studentFinishDetail.setStudentId(user.getId());
            studentFinishDetail.setStudentName(user.fetchRealnameIfBlankId());

            NewHomeworkResult newHomeworkResult = newHomeworkResultMap.get(user.getId());
            if (newHomeworkResult == null || !newHomeworkResult.isFinished()) {
                studentFinishDetail.setStatus(IntelligentRecommendStudentStatus.UN_FINISHED_HOMEWORK.name());
                continue;
            }
            SelfStudyHomework selfStudyHomework = studyHomeworkMap.get(user.getId());
            if (selfStudyHomework == null) {
                studentFinishDetail.setStatus(IntelligentRecommendStudentStatus.UN_DETECTED.name());
                continue;
            }
            SelfStudyHomeworkResult selfStudyHomeworkResult = selfStudyHomeworkResultMap.get(selfStudyHomework.getId());
            if (selfStudyHomeworkResult == null || !selfStudyHomeworkResult.isFinished()) {
                studentFinishDetail.setStatus(IntelligentRecommendStudentStatus.UN_FINISHED_SELF.name());
            } else {
                studentFinishDetail.setStatus(IntelligentRecommendStudentStatus.SELF_STUDY.name());
            }

            Map<ObjectiveConfigType, Set<String>> configTypeCourseIdMap = selfStudyHomework.findConfigTypeCourseIdMap();
            int courseCount = 0;
            Set<String> courseIds = configTypeCourseIdMap.get(DIAGNOSTIC_INTERVENTIONS);
            if (CollectionUtils.isNotEmpty(courseIds)) {
                courseIds.remove("IDC_10200000199319"); //过滤对照课程
            }
            if (CollectionUtils.isNotEmpty(courseIds)) {
                courseIdSet.addAll(courseIds);
                courseCount += courseIds.size();
                courseIds.forEach(courseId -> {
                    Integer studentCount = courseStudentCountMap.getOrDefault(courseId, 0);
                    courseStudentCountMap.put(courseId, studentCount + 1);
                });
            }
            Set<String> videoIds = configTypeCourseIdMap.get(ORAL_INTERVENTIONS);
            if (CollectionUtils.isNotEmpty(videoIds)) {
                videoIdSet.addAll(videoIds);
                courseCount += videoIds.size();
                videoIds.forEach(videoId -> {
                    Integer studentCount = courseStudentCountMap.getOrDefault(videoId, 0);
                    courseStudentCountMap.put(videoId, studentCount + 1);
                });
            }
            //过滤对照课程
            if (courseCount == 0) {
                studentFinishDetail.setStatus(IntelligentRecommendStudentStatus.UN_DETECTED.name());
                continue;
            }
            studentFinishDetail.setCourseCount(courseCount);
            totalStudentCount++;
        }
        recommendResp.setTotalStudentCount(totalStudentCount);
        Map<String, IntelDiagnosisCourse> diagnosisCourseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(courseIdSet);
        Map<String, String> courseVariantNameMap = getCourseVariantNameMapByCourseIds(diagnosisCourseMap);

        // 计算讲练测课程没有变式, 取知识点名
        Map<String, String> courseKnowledgeMap = getCourseKnowledgePointNameMap(diagnosisCourseMap);

        Map<String, MicroVideoTask> microVideoTaskMap = intelDiagnosisClient.loadMicroVideoTaskByIdsIncludeDisabled(videoIdSet);
        for (String courseId : courseStudentCountMap.keySet()) {
            IntelligentTeachingRecommendResp.IntelligentTeachingSummary summary = new IntelligentTeachingRecommendResp.IntelligentTeachingSummary();
            String variantName = courseVariantNameMap.get(courseId);
            if (variantName == null) {
                MicroVideoTask microVideoTask = microVideoTaskMap.get(courseId);
                if (microVideoTask != null && StringUtils.isNotBlank(microVideoTask.getName())) {
                    variantName = microVideoTask.getName();
                } else {
                    variantName = courseKnowledgeMap.getOrDefault(courseId, "");
                }
            }
            summary.setCourseId(courseId);
            summary.setVariantName(variantName);
            summary.setStudentCount(courseStudentCountMap.get(courseId));
            recommendResp.getSummaryList().add(summary);
        }
        recommendResp.setVariantCount(recommendResp.getSummaryList().size());
        recommendResp.setHasRecommend(SafeConverter.toBoolean(newHomework.getRemindCorrection()));
        recommendResp.getStudentFinishDetails().sort(IntelligentTeachingRecommendResp.StudentFinishDetail::compareTo);
        return recommendResp;
    }

    /**
     * 计算讲练测轻交互课程ID对应知识点名称
     *
     * @param diagnosisCourseMap
     * @return <courseId, KPName>
     */
    @NotNull
    public Map<String, String> getCourseKnowledgePointNameMap(Map<String, IntelDiagnosisCourse> diagnosisCourseMap) {
        Map<String, String> courseKnowledgeIdMap = diagnosisCourseMap.values().stream()
                .filter(course -> course.getCategory() != null && "ORAL_CALCULATE".equals(course.getCategory()))
                .collect(Collectors.toMap(IntelDiagnosisCourse::getId, IntelDiagnosisCourse::getKnowledgePointId));
        Map<String, NewKnowledgePoint> knowledgePointMap = newKnowledgePointLoader.loadKnowledgePointsIncludeDeleted(courseKnowledgeIdMap.values());
        return MapUtils.transform(courseKnowledgeIdMap, kpId -> {
            NewKnowledgePoint newKnowledgePoint = knowledgePointMap.get(kpId);
            return newKnowledgePoint == null ? "" : newKnowledgePoint.getName();
        });
    }

    /**
     * 根据轻交互课程ID查询课程对应变式map
     *
     * @param diagnosisCourseMap
     * @return <courseId, variantName>
     */
    @NotNull
    public Map<String, String> getCourseVariantNameMapByCourseIds(Map<String, IntelDiagnosisCourse> diagnosisCourseMap) {
        Map<String, String> courseVariantIdMap = MapUtils.transform(diagnosisCourseMap, IntelDiagnosisCourse::getVariantId);
        Map<String, String> variantNameMap = getVariantNameMap(courseVariantIdMap.values());
        return Maps.transformValues(courseVariantIdMap, variantNameMap::get);
    }


    /**
     * 获取变式名称map
     *
     * @param variantIds 变式ID
     * @return <变式ID, 变式名称>
     */
    public Map<String, String> getVariantNameMap(Collection<String> variantIds) {
        if (CollectionUtils.isEmpty(variantIds)) {
            return Collections.emptyMap();
        }
        Map<String, IntelDiagnosisVariant> intelDiagnosisVariantMap = intelDiagnosisClient.loadIntelDiagnosisVariantByIdIncludeDisabled(variantIds);
        if (MapUtils.isNotEmpty(intelDiagnosisVariantMap)) {
            return MapUtils.transform(intelDiagnosisVariantMap, IntelDiagnosisVariant::getCoreMission);
        }
        return Collections.emptyMap();
    }

    /**
     * 获取口语讲练测题目未掌握学生IDs
     *
     * @param selfStudyHomeworkMap 指定作业已生成订正作业
     * @param questionBoxId        原作业题包ID
     * @return <questionId, Set<studentId>>
     */
    public Map<String, Set<Long>> getQuestionUnGraspStudentsMap(Map<String, SelfStudyHomework> selfStudyHomeworkMap, String questionBoxId) {
        if (MapUtils.isEmpty(selfStudyHomeworkMap) || questionBoxId == null) {
            return Collections.emptyMap();
        }
        Map<String, Set<Long>> questionStudentsMap = Maps.newHashMap();
        for (SelfStudyHomework studyHomework : selfStudyHomeworkMap.values()) {
            List<ErrorQuestion> errorQuestions = studyHomework.findQuestionBoxAppErrorQuestionsMap(ObjectiveConfigType.ORAL_INTERVENTIONS).get(questionBoxId);
            if (CollectionUtils.isNotEmpty(errorQuestions)) {
                for (ErrorQuestion errorQuestion : errorQuestions) {
                    Set<Long> students = questionStudentsMap.computeIfAbsent(errorQuestion.getErrorQuestionId(), k -> Sets.newHashSet());
                    students.add(studyHomework.getStudentId());
                }
            }
        }
        return questionStudentsMap;
    }

    /**
     * 作业讲练测作业形式统计(用于作业分享讲练测总结部分)
     *
     * @param homeworkId 原作业ID
     * @return {@link IntelligentTeachingReport}
     */
    @Override
    public IntelligentTeachingReport fetchIntelligentTeachingReport(String homeworkId) {
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null || newHomework.getIncludeIntelligentTeaching() == null || !newHomework.getIncludeIntelligentTeaching()) {
            return null;
        }

        Map<Long, User> studentMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId()).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        Set<String> selfStudyHids = homeworkSelfStudyRefDao.loadSelfStudyHomeworkIds(homeworkId, studentMap.values());
        Map<String, SelfStudyHomework> selfStudyHomeworkMap = selfStudyHomeworkLoader.loadSelfStudyHomework(selfStudyHids);
        if (MapUtils.isEmpty(selfStudyHomeworkMap)) {
            return null;
        }
        Map<String, SelfStudyHomeworkResult> hasCourseGraspStudyHomeworkResultMap = Maps.filterValues(selfStudyHomeworkResultDao.loads(selfStudyHids),
                r -> r != null && r.isFinished() && r.findAppAnswer(NewHomeworkConstants.COURSE_APP_CONFIGTYPE).values().stream().anyMatch(BaseHomeworkResultAppAnswer::isGrasp));

        Map<Long, SelfStudyHomework> studentSelfHomeworkMap = selfStudyHomeworkMap.values().stream().collect(Collectors.toMap(SelfStudyHomework::getStudentId, Function.identity()));
        Map<Long, List<SubHomeworkProcessResult>> userGraspProcessResultMap = getUserGraspSelfProcessResultMap(selfStudyHomeworkMap.values(), ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS);
        //命中课程的订正作业map
        Map<String, SelfStudyHomework> courseSelfStudyHomeworkMap = Maps.filterValues(selfStudyHomeworkMap, s -> s != null && !Collections.disjoint(s.findPracticeContents().keySet(), NewHomeworkConstants.COURSE_APP_CONFIGTYPE));
        if (MapUtils.isEmpty(courseSelfStudyHomeworkMap)) {
            return null;
        }
        IntelligentTeachingReport report = new IntelligentTeachingReport();
        report.setHitCourseStudentCount(courseSelfStudyHomeworkMap.size());
        report.setHasCourseGraspUserCount(hasCourseGraspStudyHomeworkResultMap.size());
        report.setAdvanceAccuracy(accuracy(hasCourseGraspStudyHomeworkResultMap.size(), courseSelfStudyHomeworkMap.size()));
        List<IntelligentTeachingReport.AdvanceStudent> advanceStudents = new LinkedList<>();
        for (SelfStudyHomeworkResult studyHomeworkResult : hasCourseGraspStudyHomeworkResultMap.values()) {
            Long userId = studyHomeworkResult.getUserId();
            //口语讲练测
            IntelligentTeachingReport.AdvanceStudent stuOralInfo = wrapCourseAdvanceStudent(studentMap, studyHomeworkResult, userId);
            if (stuOralInfo != null && stuOralInfo.getCount() > 0) {
                advanceStudents.add(stuOralInfo);
            }
            //重点讲练测
            if (MapUtils.isEmpty(userGraspProcessResultMap)) {
                continue;
            }
            IntelligentTeachingReport.AdvanceStudent questionInfo = wrapQuestionAdvanceStudent(studentMap, studentSelfHomeworkMap, userGraspProcessResultMap, studyHomeworkResult, userId);
            if (questionInfo != null && questionInfo.getCount() > 0) {
                advanceStudents.add(questionInfo);
            }
        }
        advanceStudents.sort(new IntelligentTeachingReport.AdvanceStudentComparator());
        report.setAdvanceStudents(advanceStudents);
        return report;
    }


    private IntelligentTeachingReport.AdvanceStudent wrapCourseAdvanceStudent(Map<Long, User> studentMap, SelfStudyHomeworkResult studyHomeworkResult, Long userId) {
        Map<String, BaseHomeworkResultAppAnswer> appAnswerMap = Maps.filterValues(studyHomeworkResult.findAppAnswer(ObjectiveConfigType.ORAL_INTERVENTIONS), BaseHomeworkResultAppAnswer::isGrasp);
        if (MapUtils.isEmpty(appAnswerMap)) {
            return null;
        }
        IntelligentTeachingReport.AdvanceStudent advanceStudent = new IntelligentTeachingReport.AdvanceStudent();
        advanceStudent.setStudentId(userId);
        advanceStudent.setStudentName(studentMap.get(userId).fetchRealnameIfBlankId());
        advanceStudent.setAdvanceType("ORAL");
        advanceStudent.setCount(appAnswerMap.size());
        return advanceStudent;
    }

    private IntelligentTeachingReport.AdvanceStudent wrapQuestionAdvanceStudent(Map<Long, User> studentMap, Map<Long, SelfStudyHomework> studentSelfHomeworkMap, Map<Long, List<SubHomeworkProcessResult>> userGraspProcessResultMap, SelfStudyHomeworkResult studyHomeworkResult, Long userId) {
        LinkedHashMap<String, BaseHomeworkResultAppAnswer> appAnswer = studyHomeworkResult.findAppAnswer(ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS);
        if (MapUtils.isEmpty(appAnswer) || CollectionUtils.isEmpty(userGraspProcessResultMap.get(userId))) {
            return null;
        }
        List<String> correctQuestionIds = Lists.transform(userGraspProcessResultMap.get(userId), SubHomeworkProcessResult::getQuestionId);
        int graspQuestionCount = 0;
        for (String courseId : appAnswer.keySet()) {
            graspQuestionCount += statCourseGraspPreQuestionCount(studentSelfHomeworkMap.get(userId), correctQuestionIds, courseId);
        }
        IntelligentTeachingReport.AdvanceStudent advanceStudent = new IntelligentTeachingReport.AdvanceStudent();
        advanceStudent.setStudentId(userId);
        advanceStudent.setStudentName(studentMap.get(userId).fetchRealnameIfBlankId());
        advanceStudent.setAdvanceType("QUESTION");
        advanceStudent.setCount(graspQuestionCount);
        return advanceStudent;
    }

    /**
     * 统计自学作业指定课程掌握(后测题全做对)对应前测题数量
     * 注: 重点讲练测DIAGNOSTIC_INTERVENTIONS 一种作业形式
     *
     * @param courseId 课程ID
     * @return
     */
    public int statCourseGraspPreQuestionCount(SelfStudyHomework selfStudyHomework, List<String> correctQuestionIds, String courseId) {
        List<NewHomeworkQuestion> questionBoxQuestions = selfStudyHomework.findCourseAppQuestionsMap(ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS).get(courseId);
        List<String> similarQids = Lists.transform(questionBoxQuestions, NewHomeworkQuestion::getQuestionId);

        //学生后测全部做对的课程对应的前测题目数
        if (CollectionUtils.isNotEmpty(correctQuestionIds) && correctQuestionIds.containsAll(similarQids)) {
            List<ErrorQuestion> questionBoxErrorQuestions = selfStudyHomework.findAppErrorQuestionsMap(ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS).get(courseId);
            if (CollectionUtils.isNotEmpty(questionBoxErrorQuestions)) {
                return questionBoxErrorQuestions.size();
            }
        }
        return 0;
    }

    /**
     * 计算正确率(四舍五入取整)
     *
     * @param preCount
     * @param totalCount
     * @return
     */
    int accuracy(long preCount, int totalCount) {
        return BigDecimal.valueOf(preCount * 100).divide(new BigDecimal(totalCount), 0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    /**
     * 查询指定作业形式学生掌握自学作业结果
     *
     * @param selfStudyHomeworkList
     * @return <studentId, 作业形式掌握做题结果list>
     */
    public Map<Long, List<SubHomeworkProcessResult>> getUserGraspSelfProcessResultMap(Collection<SelfStudyHomework> selfStudyHomeworkList, ObjectiveConfigType configType) {
        List<String> resultAnswerIds = Lists.newLinkedList();
        selfStudyHomeworkList.forEach(selfStudyHomework -> {
            NewHomework homework = new NewHomework();
            PropertiesUtils.copyProperties(homework, selfStudyHomework);
            List<String> resultAnswerId = newHomeworkResultLoader.initSubHomeworkResultAnswerIdsMap(homework, selfStudyHomework.getStudentId()).get(configType);
            if (CollectionUtils.isNotEmpty(resultAnswerId)) {
                resultAnswerIds.addAll(resultAnswerId);
            }
        });
        Map<String, SubHomeworkResultAnswer> selfHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(resultAnswerIds);
        List<String> allSelfProcessIds = selfHomeworkResultAnswerMap.values().stream().filter(a -> a.getProcessId() != null).map(SubHomeworkResultAnswer::getProcessId).collect(Collectors.toList());
        Map<String, SubHomeworkProcessResult> selfProcessResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(allSelfProcessIds);
        Map<String, SubHomeworkProcessResult> selfGraspProcessResultMap = Maps.filterValues(selfProcessResultMap, SubHomeworkProcessResult::getGrasp);
        return selfGraspProcessResultMap.values().stream().collect(Collectors.groupingBy(SubHomeworkProcessResult::getUserId));
    }

    /**
     * 统计学生作业即时干预纠正错题数
     *
     * @param newHomework 作业
     * @param studentId   学生ID
     * @return 纠正数据
     */
    @Override
    public long countInterventionGraspQuestion(NewHomework newHomework, Long studentId) {
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
        if (newHomeworkResult == null) {
            return 0;
        }
        Map<String, String> homeworkProcessIdsMap = newHomeworkResult.findHomeworkProcessIdsMap();
        Map<String, SubHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(homeworkProcessIdsMap.values());
        return processResultMap.values().stream().filter(p -> p.isIntervention() && p.getGrasp()).count();
    }

}
