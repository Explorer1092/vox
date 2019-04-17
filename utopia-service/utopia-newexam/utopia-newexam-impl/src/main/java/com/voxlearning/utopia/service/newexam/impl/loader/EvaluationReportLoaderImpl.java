package com.voxlearning.utopia.service.newexam.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.athena.api.IndependentMockRankService;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.mapper.ScoreCircleQueueCommand;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalogAncestor;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePoint;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewKnowledgePointLoaderClient;
import com.voxlearning.utopia.service.newexam.api.constant.SkillType;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamRegistration;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamResult;
import com.voxlearning.utopia.service.newexam.api.loader.EvaluationReportLoader;
import com.voxlearning.utopia.service.newexam.api.mapper.evaluation.report.*;
import com.voxlearning.utopia.service.newexam.api.utils.NewExamUtils;
import com.voxlearning.utopia.service.newexam.impl.consumer.cache.EvaluationParentCacheManager;
import com.voxlearning.utopia.service.newexam.impl.consumer.cache.EvaluationTeacherOpenReportCacheManager;
import com.voxlearning.utopia.service.newexam.impl.consumer.cache.EvaluationTeacherShareCacheManager;
import com.voxlearning.utopia.service.newexam.impl.queue.NewExamParentQueueProducer;
import com.voxlearning.utopia.service.newexam.impl.support.NewExamSpringBean;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.consumer.FeatureLoaderClient;
import com.voxlearning.utopia.service.question.consumer.SolutionMethodLoaderClient;
import com.voxlearning.utopia.service.question.consumer.TestMethodLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = EvaluationReportLoader.class)
@ExposeService(interfaceClass = EvaluationReportLoader.class)
public class EvaluationReportLoaderImpl extends NewExamSpringBean implements EvaluationReportLoader {

    @ImportService(interfaceClass = IndependentMockRankService.class)
    private IndependentMockRankService independentMockRankService;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private NewKnowledgePointLoaderClient newKnowledgePointLoaderClient;
    @Inject private TestMethodLoaderClient testMethodLoaderClient;
    @Inject private SolutionMethodLoaderClient solutionMethodLoaderClient;
    @Inject private FeatureLoaderClient featureLoaderClient;
    @Inject private NewContentLoaderClient newContentLoaderClient;
    @Inject private NewExamParentQueueProducer newExamParentQueueProducer;


    public MapMessage fetchNewExamSkillInfo(String newExamId) {
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("考试ID错误");
        }
        String paperId = newExam.obtainRandomPaperId();
        NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(paperId);
        if (newPaper == null) {
            return MapMessage.errorMessage("试卷不存在");
        }
        if (CollectionUtils.isEmpty(newPaper.getQuestions())) {
            return MapMessage.errorMessage("试卷不存在题目");
        }

        List<String> qids = newPaper.getQuestions()
                .stream()
                .map(XxBaseQuestion::getId)
                .collect(Collectors.toList());
        Map<String, NewQuestion> newQuestionMap = tikuStrategy.loadQuestionsIncludeDisabled(qids);

        Map<String, List<String>> kidToQids = new LinkedHashMap<>();
        for (NewQuestion newQuestion : newQuestionMap.values()) {
            if (CollectionUtils.isEmpty(newQuestion.getAbilities()))
                continue;
            for (EmbedAbility embedAbility : newQuestion.getAbilities()) {
                if (embedAbility.getStars() == null)
                    continue;
                if (embedAbility.getStars() == 0)
                    continue;
                if (StringUtils.isNotBlank(embedAbility.getName())) {
                    kidToQids.computeIfAbsent(embedAbility.getName(), o -> new LinkedList<>()).add(newQuestion.getId());
                }
            }
        }
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("kidToQids", kidToQids);
        return mapMessage;
    }


    public Map<String, KnowledgeNameBO> fetchKnowledgeName(NewPaper newPaper) {

        if (MapUtils.isNotEmpty(newPaper.getEvaluationInFos())) {
            Map<String, KnowledgeNameBO> knowledgeNameBOMap = new LinkedHashMap<>();
            Set<String> kpIds = new LinkedHashSet<>();
            Set<String> kpfIds = new LinkedHashSet<>();
            Set<String> tmIds = new LinkedHashSet<>();
            Set<String> smIds = new LinkedHashSet<>();
            for (Map.Entry<String, EvaluationInfo> entry : newPaper.getEvaluationInFos().entrySet()) {
                KnowledgeNameBO knowledgeNameBO = new KnowledgeNameBO();
                knowledgeNameBOMap.put(entry.getKey(), knowledgeNameBO);
                EvaluationInfo evaluationInfo = entry.getValue();
                if (CollectionUtils.isNotEmpty(evaluationInfo.getTargetList())) {
                    for (EmbedEvaluationTarget embedEvaluationTarget : evaluationInfo.getTargetList()) {
                        List<String> ids = new LinkedList<>();
                        if (StringUtils.isNotBlank(embedEvaluationTarget.getId())) {
                            ids.add(embedEvaluationTarget.getId());
                        }
                        if (CollectionUtils.isNotEmpty(embedEvaluationTarget.getFeatureIds())) {
                            ids.addAll(embedEvaluationTarget.getFeatureIds());
                        }
                        if (CollectionUtils.isNotEmpty(ids)) {
                            for (String id : ids) {
                                if (StringUtils.isBlank(id))
                                    continue;
                                if (id.startsWith("KPF")) {
                                    kpfIds.add(id);
                                    knowledgeNameBO.getKpModule().getKpfIds().add(id);
                                } else {
                                    if (id.startsWith("KP")) {
                                        kpIds.add(id);
                                        knowledgeNameBO.getKpModule().getKpIds().add(id);
                                    }
                                }
                                if (id.startsWith("SM")) {
                                    smIds.add(id);
                                    knowledgeNameBO.getSmTmModule().getSmIds().add(id);
                                }
                                if (id.startsWith("TM")) {
                                    tmIds.add(id);
                                    knowledgeNameBO.getSmTmModule().getTmIds().add(id);
                                }
                            }
                        }
                    }
                }
            }

            Map<String, NewKnowledgePoint> newKnowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePointsIncludeDeleted(kpIds);
            Map<String, TestMethod> testMethodMap = testMethodLoaderClient.loadTestMethodIncludeDisabled(tmIds);
            Map<String, SolutionMethod> solutionMethodMap = solutionMethodLoaderClient.loadSolutionMethodsIncludeDisabled(smIds);
            Map<String, KnowledgePointFeature> knowledgePointFeatureMap = featureLoaderClient.loadKnowledgePointFeatureIncludeDisabled(kpfIds);

            for (Map.Entry<String, KnowledgeNameBO> entry : knowledgeNameBOMap.entrySet()) {
                KnowledgeNameBO knowledgeNameBO = entry.getValue();
                KnowledgeNameBO.KpModule kpModule = knowledgeNameBO.getKpModule();
                if (!kpModule.getKpIds().isEmpty()) {
                    for (String kpId : kpModule.getKpIds()) {
                        if (newKnowledgePointMap.containsKey(kpId)) {
                            NewKnowledgePoint newKnowledgePoint = newKnowledgePointMap.get(kpId);
                            if (StringUtils.isNotBlank(newKnowledgePoint.getName())) {
                                kpModule.getKpNames().add(newKnowledgePoint.getName());
                            }
                        }
                    }
                }
                if (!kpModule.getKpfIds().isEmpty()) {
                    for (String kpfId : kpModule.getKpfIds()) {
                        if (knowledgePointFeatureMap.containsKey(kpfId)) {
                            KnowledgePointFeature newKnowledgePoint = knowledgePointFeatureMap.get(kpfId);
                            if (StringUtils.isNotBlank(newKnowledgePoint.getName())) {
                                kpModule.getKpfNames().add(newKnowledgePoint.getName());
                            }
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(kpModule.getKpfNames()) && CollectionUtils.isNotEmpty(kpModule.getKpNames())) {
                    String name = StringUtils.join(kpModule.getKpNames(), ",") + "[" + StringUtils.join(kpModule.getKpfNames(), ",") + "]";
                    kpModule.setName(name);
                } else {
                    if (CollectionUtils.isNotEmpty(kpModule.getKpfNames())) {
                        String name = StringUtils.join(kpModule.getKpfNames(), ",");
                        kpModule.setName(name);
                    } else if (CollectionUtils.isNotEmpty(kpModule.getKpNames())) {
                        String name = StringUtils.join(kpModule.getKpNames(), ",");
                        kpModule.setName(name);
                    }
                }
                KnowledgeNameBO.SmTmModule smTmModule = knowledgeNameBO.getSmTmModule();
                if (!smTmModule.getTmIds().isEmpty()) {
                    for (String tmId : smTmModule.getTmIds()) {
                        if (testMethodMap.containsKey(tmId)) {
                            TestMethod testMethod = testMethodMap.get(tmId);
                            if (StringUtils.isNotBlank(testMethod.getName())) {
                                smTmModule.getTmNames().add(testMethod.getName());
                            }
                        }
                    }
                }
                if (!smTmModule.getSmIds().isEmpty()) {
                    for (String smId : smTmModule.getSmIds()) {
                        if (solutionMethodMap.containsKey(smId)) {
                            SolutionMethod solutionMethod = solutionMethodMap.get(smId);
                            if (StringUtils.isNotBlank(solutionMethod.getName())) {
                                smTmModule.getSmNames().add(solutionMethod.getName());
                            }
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(smTmModule.getSmIds()) || CollectionUtils.isNotEmpty(smTmModule.getTmNames())) {
                    List<String> names = new LinkedList<>();
                    names.addAll(smTmModule.getTmNames());
                    names.addAll(smTmModule.getSmNames());
                    String name = StringUtils.join(names, ",");
                    smTmModule.setName(name);
                }
            }
            return knowledgeNameBOMap;
        }
        return Collections.emptyMap();
    }

//    public MapMessage fetchBigData(String newExamId) {
//        NewExam newExam = newExamLoaderClient.load(newExamId);
//        Long groupId = newExam.getGroupId();
//        String paperId = newExam.obtainRandomPaperId();
//        List<Teacher> teachers = teacherLoaderClient.loadGroupTeacher(groupId);
//        if (CollectionUtils.isEmpty(teachers)) {
//            return MapMessage.errorMessage("班级不存在老师");
//        }
//        Teacher targetTeacher = null;
//        for (Teacher t : teachers) {
//            if (Objects.equals(t.getSubject(), newExam.getSubject())) {
//                targetTeacher = t;
//                break;
//            }
//        }
//        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(targetTeacher.getId());
//        MapMessage mapMessage1 = independentMockRankService.IndependentMockCitySummary(paperId, teacherDetail.getCityCode());
//        return mapMessage1;
//    }

    @Override
    @SuppressWarnings("unchecked")
    public MapMessage fetchEvaluationReport(String newExamId, User user) {
        if (StringUtils.isBlank(newExamId)) {
            return MapMessage.errorMessage("newExamId参数有误");
        }
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("考试不存在");
        }
        Long groupId = newExam.getGroupId();
        if (groupId == null) {
            return MapMessage.errorMessage("考试没有groupId");
        }
        GroupMapper groupMapper = groupLoaderClient.loadGroup(groupId, false);
        if (groupMapper == null) {
            return MapMessage.errorMessage("班组不存在");
        }
        Clazz clazz = clazzLoaderClient.getRemoteReference().loadClazz(groupMapper.getClazzId());
        if (clazz == null) {
            return MapMessage.errorMessage("班级不存在");
        }

        List<Teacher> teachers = teacherLoaderClient.loadGroupTeacher(groupId);
        if (CollectionUtils.isEmpty(teachers)) {
            return MapMessage.errorMessage("班级不存在老师");
        }
        Teacher targetTeacher = null;
        for (Teacher t : teachers) {
            if (Objects.equals(t.getSubject(), newExam.getSubject())) {
                targetTeacher = t;
                break;
            }
        }
        if (targetTeacher == null) {
            return MapMessage.errorMessage("班级不存在老师");
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(targetTeacher.getId());
        if (teacherDetail == null) {
            return MapMessage.errorMessage("班级不存在老师");
        }
        if (teacherDetail.getCityCode() == null) {
            return MapMessage.errorMessage("老师不存在市归属");
        }
        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newExam.getGroupId())
                .stream()
                .collect(Collectors
                        .toMap(LongIdEntity::getId, Function.identity()));
        if (userMap.size() == 0) {
            return MapMessage.errorMessage("班级不存在学生");
        }
        String paperId = newExam.obtainRandomPaperId();
        NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(paperId);
        if (newPaper == null) {
            return MapMessage.errorMessage("试卷不存在");
        }
        if (newPaper.getQuestions() == null) {
            return MapMessage.errorMessage("试卷不存在题目");
        }
        List<String> qids = newPaper.getQuestions()
                .stream()
                .map(XxBaseQuestion::getId)
                .collect(Collectors.toList());
        Map<String, NewQuestion> newQuestionMap = tikuStrategy.loadQuestionsIncludeDisabled(qids);
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userMap.values().iterator().next().getId());
        boolean scoreRegionFlag = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
        //是否发布
        boolean issue = newExam.getResultIssueAt().before(new Date());
        //知识点
        KnowledgePart knowledgePart = new KnowledgePart();


        Map<String, List<String>> qidToKidsMap = new LinkedHashMap<>();
        Map<String, KnowledgePart.KnowledgePointBO> knowledgePointBOMap = new LinkedHashMap<>();
        Map<String, KnowledgeNameBO> knowledgeNameBOMap = fetchKnowledgeName(newPaper);
        if (MapUtils.isNotEmpty(newPaper.getEvaluationInFos())) {
            for (Map.Entry<String, EvaluationInfo> entry : newPaper.getEvaluationInFos().entrySet()) {
                KnowledgePart.KnowledgePointBO knowledgePointBO = new KnowledgePart.KnowledgePointBO();
                knowledgePointBO.setKid(entry.getKey());
                if (knowledgeNameBOMap.containsKey(entry.getKey())) {
                    KnowledgeNameBO knowledgeNameBO = knowledgeNameBOMap.get(entry.getKey());
                    knowledgePointBO.setKName(knowledgeNameBO.getKpModule().getName());
                    knowledgePointBO.setTName(knowledgeNameBO.getSmTmModule().getName());
                }
                knowledgePointBOMap.put(entry.getKey(), knowledgePointBO);
                EvaluationInfo embedEvaluationInfo = entry.getValue();
                if (embedEvaluationInfo != null) {
                    if (CollectionUtils.isNotEmpty(embedEvaluationInfo.getQuestionIds())) {
                        for (String qid : embedEvaluationInfo.getQuestionIds()) {
                            if (!newQuestionMap.containsKey(qid))
                                continue;
                            NewQuestion newQuestion = newQuestionMap.get(qid);
                            qidToKidsMap.computeIfAbsent(newQuestion.getDocId(), o -> new LinkedList<>()).add(entry.getKey());
                        }
                    }
                }
            }
        }
        List<KnowledgePart.KnowledgePoint> knowledgePoints = knowledgePointBOMap.values()
                .stream()
                .map(o -> {
                    KnowledgePart.KnowledgePoint k = new KnowledgePart.KnowledgePoint();
                    k.setKid(o.getKid());
                    k.setKName(o.getKName());
                    k.setTName(o.getTName());
                    return k;
                })
                .collect(Collectors.toList());
        knowledgePart.setKnowledgePoints(knowledgePoints);


        //技能
        SkillPart skillPart = new SkillPart();
        Map<String, List<SkillType>> qidToSkillIdMap = new LinkedHashMap<>();
        Map<SkillType, SkillPart.SkillBO> stringSkillBOMap = new LinkedHashMap<>();
        Set<SkillType> skillIds = new LinkedHashSet<>();
        if (newExam.getSubject() == Subject.MATH) {
            for (NewQuestion newQuestion : newQuestionMap.values()) {
                if (newQuestion.getAbilities() != null) {
                    for (EmbedAbility embedAbility : newQuestion.getAbilities()) {
                        if (embedAbility.getStars() == null)
                            continue;
                        if (embedAbility.getStars() == 0)
                            continue;
                        if (embedAbility.getName() == null)
                            continue;
                        SkillType skillType = SkillType.parse(embedAbility.getName());
                        if (skillType == null) {
                            continue;
                        }
                        qidToSkillIdMap.computeIfAbsent(newQuestion.getDocId(), o -> new LinkedList<>()).add(skillType);
                        if (!stringSkillBOMap.containsKey(skillType)) {
                            skillIds.add(skillType);
                            SkillPart.SkillBO skillBO = new SkillPart.SkillBO();
                            skillBO.setSkillId(skillType.name());
                            skillBO.setSkillName(skillType.getDesc());
                            stringSkillBOMap.put(skillType, skillBO);
                        }
                    }
                }
            }
            Set<SkillType> __skillIds = new LinkedHashSet<>();
            for (SkillType skillType : Arrays.asList(SkillType.SolvingArithmetic, SkillType.SpatialImagination, SkillType.DeductiveReasoning)) {
                if (skillIds.contains(skillType)) {
                    skillPart.getSkillName().add(skillType.getDesc());
                    SkillPart.Skill skill = new SkillPart.Skill();
                    skill.setSkillId(skillType.name());
                    skill.setSkillName(skillType.getDesc());
                    skillPart.getSkills().add(skill);
                    __skillIds.add(skillType);
                }
            }
            skillIds = __skillIds;
        }


        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        List<String> newExamRegistrationIds = userMap.keySet()
                .stream()
                .map(o -> new NewExamRegistration.ID(month, newExam.getSubject(), newExam.getId(), o.toString()).toString())
                .collect(Collectors.toList());
        Map<String, NewExamResult> newExamResultMap = newExamResultDao.loads(newExamRegistrationIds);
        List<String> processIds = newExamResultMap.values()
                .stream()
                .filter(o -> MapUtils.isNotEmpty(o.getAnswers()))
                .map(o -> o.getAnswers().values())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Map<String, NewExamProcessResult> newExamProcessResultMap = newExamProcessResultDao.loads(processIds);
        List<KnowledgePart.StudentReportRecordBO> studentReportRecordBOList = new LinkedList<>();
        int totalScore = 0;
        int finishNum = 0;
        int beginNum = 0;
        for (User u : userMap.values()) {
            KnowledgePart.StudentReportRecordBO student = new KnowledgePart.StudentReportRecordBO();
            studentReportRecordBOList.add(student);
            student.setSid(u.getId());
            student.setSName(u.fetchRealnameIfBlankId());
            String s = new NewExamRegistration.ID(month, newExam.getSubject(), newExam.getId(), u.getId().toString()).toString();
            if (newExamResultMap.containsKey(s)) {
                NewExamResult newExamResult = newExamResultMap.get(s);
                if (newExamResult.getScore() == null) {
                    continue;
                }
                beginNum++;
                student.setBegin(true);
                int score = new BigDecimal(newExamResult.processScore(newPaper.getTotalScore())).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                student.setScore(score);
                Date finishAt = newExamResult.getSubmitAt();
                totalScore += score;
                Long durationMilliseconds = newExamResult.getDurationMilliseconds();
                long duration = new BigDecimal(SafeConverter.toLong(durationMilliseconds)).divide(new BigDecimal(1000 * 60), 0, BigDecimal.ROUND_HALF_UP).longValue();
                student.setDuration(duration);
                if (finishAt != null) {
                    finishNum++;
                    student.setFinished(true);
                    student.setFinishAt(finishAt);
                }
            }
        }
        //知识模块--》学生完成情况
        {
            List<KnowledgePart.StudentReportRecord> studentReportRecords = studentReportRecordBOList.stream()
                    .sorted((o1, o2) -> {
                        int compare = Integer.compare(o2.getScore(), o1.getScore());
                        if (compare == 0) {
                            compare = Long.compare(o1.getDuration(), o2.getDuration());
                        }
                        return compare;
                    })
                    .map(o -> {
                        KnowledgePart.StudentReportRecord s = new KnowledgePart.StudentReportRecord();
                        s.setSid(o.getSid());
                        s.setBegin(o.isBegin());
                        s.setSName(o.getSName());
                        s.setScore(o.getScore());
                        s.setScoreLevel(NewExamUtils.changeRateToLevel(o.getScore()));
                        s.setFinished(o.isFinished());
                        if (s.isBegin()) {
                            s.setDuration(o.getDuration() + "分钟");
                        }
                        if (o.isFinished()) {
                            s.setFinishTime(DateUtils.dateToString(o.getFinishAt(), "MM-dd HH:mm"));
                        }
                        return s;
                    })
                    .collect(Collectors.toList());
            knowledgePart.setStudentReportRecords(studentReportRecords);
        }

        if (newExam.getSubject() == Subject.ENGLISH) {
            skillPart = null;
        } else {
            Map<Long, SkillPart.StudentSkillRecordBO> studentSkillRecordBOMap = new LinkedHashMap<>();
            for (User u : userMap.values()) {
                SkillPart.StudentSkillRecordBO student = new SkillPart.StudentSkillRecordBO();
                studentSkillRecordBOMap.put(u.getId(), student);
                student.setSid(u.getId());
                student.setSName(u.fetchRealnameIfBlankId());
                String s = new NewExamRegistration.ID(month, newExam.getSubject(), newExam.getId(), u.getId().toString()).toString();
                if (newExamResultMap.containsKey(s)) {
                    NewExamResult newExamResult = newExamResultMap.get(s);
                    if (newExamResult.getScore() != null) {
                        student.setBegin(true);
                        student.setFinished(newExamResult.getFinishAt() != null);
                        Map<SkillType, Integer> totalNumMap = new LinkedHashMap<>();
                        Map<SkillType, Integer> rightNumMap = new LinkedHashMap<>();
                        for (SkillType skillId : skillIds) {
                            totalNumMap.put(skillId, 0);
                            rightNumMap.put(skillId, 0);
                        }
                        student.setTotalNumMap(totalNumMap);
                        student.setRightNumMap(rightNumMap);
                    }
                }
            }
            //计算知识点的正确率、技能的正确率、技能各个学生的正确率
            for (NewExamProcessResult p : newExamProcessResultMap.values()) {
                String qid = p.getQuestionDocId();
                if (qidToSkillIdMap.containsKey(qid)) {
                    List<SkillType> _skillIds = qidToSkillIdMap.get(qid);
                    for (SkillType skillId : _skillIds) {
                        if (stringSkillBOMap.containsKey(skillId)) {
                            SkillPart.SkillBO skillBO = stringSkillBOMap.get(skillId);
                            skillBO.setTotalNum(1 + skillBO.getTotalNum());
                            if (SafeConverter.toBoolean(p.getGrasp())) {
                                skillBO.setRightNum(1 + skillBO.getRightNum());
                            }
                        }
                        if (studentSkillRecordBOMap.containsKey(p.getUserId())) {
                            SkillPart.StudentSkillRecordBO studentSkillRecordBO = studentSkillRecordBOMap.get(p.getUserId());
                            int total = studentSkillRecordBO.getTotalNumMap().get(skillId) + 1;
                            studentSkillRecordBO.getTotalNumMap().put(skillId, total);
                            if (SafeConverter.toBoolean(p.getGrasp())) {
                                int rightNum = studentSkillRecordBO.getRightNumMap().get(skillId) + 1;
                                studentSkillRecordBO.getRightNumMap().put(skillId, rightNum);
                            }
                        }
                    }
                }
            }
            //技能模块--》学生技能分析 后处理
            {
                Set<SkillType> finalSkillIds = skillIds;
                List<SkillPart.StudentSkillRecord> studentSkillRecords = studentSkillRecordBOMap.values()
                        .stream()
                        .map(o -> {
                            SkillPart.StudentSkillRecord s = new SkillPart.StudentSkillRecord();
                            s.setSid(o.getSid());
                            s.setSName(o.getSName());
                            s.setBegin(o.isBegin());
                            s.setFinished(o.isFinished());
                            for (SkillType skillId : finalSkillIds) {
                                Integer totalNum = SafeConverter.toInt(o.getTotalNumMap().get(skillId));
                                Integer rightNum = SafeConverter.toInt(o.getRightNumMap().get(skillId));
                                if (totalNum > 0) {
                                    Integer rate = new BigDecimal(rightNum * 100).divide(new BigDecimal(totalNum), 0, BigDecimal.ROUND_HALF_UP).intValue();
                                    s.getSkillValue().add(rate.toString());
                                    String level = NewExamUtils.changeRateToLevel(rate);
                                    s.getSkillLevelValue().add(level);
                                } else {
                                    s.getSkillValue().add("--");
                                    s.getSkillLevelValue().add("--");
                                }

                            }
                            return s;
                        })
                        .sorted((o1, o2) -> {
                            int compare = Boolean.compare(o2.isFinished(), o1.isFinished());
                            //未完成的
                            if (compare == 0 && (!o2.isFinished())) {
                                compare = Boolean.compare(o2.isBegin(), o1.isBegin());
                            }
                            return compare;
                        })
                        .collect(Collectors.toList());
                skillPart.setStudentSkillRecords(studentSkillRecords);
            }
        }
        //学习习惯
        LearningHabitsPart learningHabitsPart = new LearningHabitsPart();
        //班级维度统计信息

        if (issue) {
            //知识点模块、学习习惯模块
            {

                //知识模块--》整体得分情况
                {
                    if (beginNum > 0 && totalScore > 0) {
                        int clazzAvgScore = new BigDecimal(totalScore).divide(new BigDecimal(beginNum), 0, BigDecimal.ROUND_HALF_UP).intValue();
                        knowledgePart.setClazzAvgScore(clazzAvgScore);
                    }
                }

                //知识模块---》知识点得分分析
                {
                    //计算知识点的正确率、技能的正确率、技能各个学生的正确率
                    for (NewExamProcessResult p : newExamProcessResultMap.values()) {
                        String qid = p.getQuestionDocId();
                        if (qidToKidsMap.containsKey(qid)) {
                            List<String> kids = qidToKidsMap.get(qid);
                            for (String kid : kids) {
                                if (knowledgePointBOMap.containsKey(kid)) {
                                    KnowledgePart.KnowledgePointBO knowledgePointBO = knowledgePointBOMap.get(kid);
                                    knowledgePointBO.setTotalNum(1 + knowledgePointBO.getTotalNum());
                                    if (SafeConverter.toBoolean(p.getGrasp())) {
                                        knowledgePointBO.setRightNum(1 + knowledgePointBO.getRightNum());
                                    }
                                }
                            }
                        }
                    }
                    //知识模块--》知识点分析 后处理
                    {
                        Map<String, KnowledgePart.KnowledgePoint> knowledgePointMap = knowledgePart.getKnowledgePoints()
                                .stream()
                                .collect(Collectors.toMap(KnowledgePart.KnowledgePoint::getKid, Function.identity()));
                        knowledgePointBOMap.values()
                                .stream()
                                .filter(o -> knowledgePointMap.containsKey(o.getKid()))
                                .forEach(o -> {
                                    KnowledgePart.KnowledgePoint k = knowledgePointMap.get(o.getKid());
                                    if (o.getTotalNum() > 0 && o.getRightNum() > 0) {
                                        int rate = new BigDecimal(o.getRightNum() * 100).divide(new BigDecimal(o.getTotalNum()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                                        k.setClazzRightRate(rate);
                                    }
                                });
                        knowledgePart.getKnowledgePoints().sort((o1, o2) -> Integer.compare(o2.getClazzRightRate(), o1.getClazzRightRate()));
                    }
                }

                //学习习惯模块
                {
                    //班级完成率
                    if (finishNum > 0) {
                        int clazzFinishRate = new BigDecimal(finishNum * 100).divide(new BigDecimal(userMap.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                        learningHabitsPart.setClazzFinishRate(clazzFinishRate);
                    }
                }

            }


            //班级--技能模块
            //技能模块--》技能得分分析 分析
            if (skillPart != null) {

                Map<String, SkillPart.Skill> skillMap = skillPart.getSkills()
                        .stream()
                        .collect(Collectors.toMap(SkillPart.Skill::getSkillId, Function.identity()));
                stringSkillBOMap.values()
                        .stream()
                        .filter(o -> skillMap.containsKey(o.getSkillId()))
                        .forEach(o -> {
                            SkillPart.Skill skill = skillMap.get(o.getSkillId());
                            if (o.getTotalNum() > 0 && o.getRightNum() > 0) {
                                int rate = new BigDecimal(o.getRightNum() * 100).divide(new BigDecimal(o.getTotalNum()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                                skill.setClazzSkillRate(rate);
                            }
                        });

            }

        }
        MapMessage mapMessage1 = independentMockRankService.IndependentMockCitySummary(paperId, teacherDetail.getCityCode());
        //市区数据是否统计
        boolean flag = false;
        if (mapMessage1.isSuccess()) {
            if (mapMessage1.containsKey("dataMap")) {
                Map dataMap = (Map) mapMessage1.get("dataMap");
                if (dataMap != null) {
                    //知识模块
                    {
                        flag = SafeConverter.toBoolean(dataMap.get("flag"));
                        if (dataMap.containsKey("cityAvgScore")) {
                            knowledgePart.setCityAvgScore((int) SafeConverter.toDouble(dataMap.get("cityAvgScore")));
                        }
                        if (dataMap.containsKey("cityTopTenAvgScore")) {
                            knowledgePart.setCityTopTenAvgScore((int) SafeConverter.toDouble(dataMap.get("cityTopTenAvgScore")));
                        }
                        if (dataMap.containsKey("knowledgePart")) {
                            List<Map> _knowledgePoints = (List<Map>) dataMap.get("knowledgePart");
                            if (CollectionUtils.isNotEmpty(_knowledgePoints)) {
                                Map<String, KnowledgePart.KnowledgePoint> knowledgePointMap = knowledgePart.getKnowledgePoints()
                                        .stream()
                                        .collect(Collectors.toMap(KnowledgePart.KnowledgePoint::getKid, Function.identity()));
                                for (Map map : _knowledgePoints) {
                                    if (map.containsKey("kid") && map.containsKey("cityRightRate")) {
                                        String kid = SafeConverter.toString(map.get("kid"));
                                        if (knowledgePointMap.containsKey(kid)) {
                                            KnowledgePart.KnowledgePoint knowledgePoint = knowledgePointMap.get(kid);
                                            knowledgePoint.setCityRightRate((int) (100 * SafeConverter.toDouble(map.get("cityRightRate"))));
                                        }
                                    }
                                }
                            }
                        }
                    }

                    //技能模块
                    {
                        if (newExam.getSubject() == Subject.MATH && dataMap.containsKey("skills")) {
                            List<Map> skills = (List<Map>) dataMap.get("skills");
                            if (CollectionUtils.isNotEmpty(skills) && skillPart != null) {
                                Map<String, SkillPart.Skill> skillMap = skillPart.getSkills()
                                        .stream()
                                        .collect(Collectors.toMap(SkillPart.Skill::getSkillId, Function.identity()));
                                for (Map map : skills) {
                                    if (map.containsKey("skillId") && map.containsKey("citySkillRate")) {
                                        String skillId = (String) map.get("skillId");
                                        if (skillMap.containsKey(skillId)) {
                                            SkillPart.Skill skill = skillMap.get(skillId);
                                            skill.setCitySkillRate((int) (100 * SafeConverter.toDouble(map.get("citySkillRate"))));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //学习习惯模块
                    {
                        if (dataMap.containsKey("cityFinishRate")) {
                            learningHabitsPart.setCityFinishRate((int) (100 * SafeConverter.toDouble(dataMap.get("cityFinishRate"))));
                        }
                        if (dataMap.containsKey("cityToptenFinishRate")) {
                            learningHabitsPart.setCityTopTenFinishRate((int) (100 * SafeConverter.toDouble(dataMap.get("cityToptenFinishRate"))));
                        }
                    }
                }
            }
        } else {
            logger.error("fetch Evaluation Report failed of examId {}", newExamId);
        }
        //家长端查看，记录查看的家长ID
        if (user != null && user.isParent()) {
            EvaluationParentCacheManager evaluationParentCacheManager = newExamCacheClient.getEvaluationParentCacheManager();
            String cacheKey = evaluationParentCacheManager.getCacheKey(newExamId);
            Set<Long> pids = evaluationParentCacheManager.load(cacheKey);
            if (pids == null) {
                pids = new HashSet<>();
                pids.add(user.getId());
                evaluationParentCacheManager.add(cacheKey, pids);
            } else {
                if (!pids.contains(user.getId())) {
                    pids.add(user.getId());
                    evaluationParentCacheManager.add(cacheKey, pids);
                }
            }
        }
        //老师端在发布后查看，记录查看的老师ID
        if (user != null && user.isTeacher() && issue) {
            EvaluationTeacherOpenReportCacheManager evaluationTeacherOpenReportCacheManager = newExamCacheClient.getEvaluationTeacherOpenReportCacheManager();
            String cacheKey = evaluationTeacherOpenReportCacheManager.getCacheKey(newExamId);
            Set<Long> tids = evaluationTeacherOpenReportCacheManager.load(cacheKey);
            if (tids == null) {
                tids = new HashSet<>();
                tids.add(user.getId());
                evaluationTeacherOpenReportCacheManager.add(cacheKey, tids);
            } else if (!tids.contains(user.getId())) {
                tids.add(user.getId());
                evaluationTeacherOpenReportCacheManager.add(cacheKey, tids);
            }
        }
        //这份测验是否分享
        EvaluationTeacherShareCacheManager evaluationTeacherShareCacheManager = newExamCacheClient.getEvaluationTeacherShareCacheManager();
        String cacheKey = evaluationTeacherShareCacheManager.getCacheKey(newExamId);
        Integer value = evaluationTeacherShareCacheManager.load(cacheKey);
        boolean shared = value != null;
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("issue", issue)
                .add("flag", flag)
                .add("subject", newExam.getSubject())
                .add("subjectName", newExam.getSubject() != null ? newExam.getSubject().getValue() : "")
                .add("newExamName", newExam.getName())
                .add("clazzName", clazz.formalizeClazzName())
                .add("scoreRegionFlag", scoreRegionFlag)
                .add("learningHabitsPart", learningHabitsPart)
                .add("skillPart", skillPart)
                .add("knowledgePart", knowledgePart)
                .add("shared", shared)
                .add("issueTime", DateUtils.dateToString(newExam.getResultIssueAt(), "MM-dd HH:mm"));
        return mapMessage;
    }

    @Override
    public MapMessage shareEvaluationReportToJzt(Teacher teacher, String newExamId) {
        EvaluationTeacherShareCacheManager evaluationTeacherShareCacheManager = newExamCacheClient.getEvaluationTeacherShareCacheManager();
        String cacheKey = evaluationTeacherShareCacheManager.getCacheKey(newExamId);
        Integer value = evaluationTeacherShareCacheManager.load(cacheKey);
        if (value != null) {
            return MapMessage.errorMessage("今日已经分享");
        }

        evaluationTeacherShareCacheManager.add(cacheKey, 1);

        NewExam newExam = newExamLoaderClient.load(newExamId);

        if (newExam == null) {
            return MapMessage.errorMessage("考试不存在");
        }
        String paperId = newExam.obtainRandomPaperId();
        NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(paperId);
        if (newPaper == null) {
            return MapMessage.errorMessage("试卷不存在");
        }


        String iMContent = "家长好，" + SafeConverter.toString(newPaper.getTitle()) + "考试结果已出，请家长查看。";
        String teacherName = teacher.fetchRealname();
        String em_push_title = teacherName + "(" + teacher.getSubject().getValue() + ")" + "老师:" + iMContent;

        //发往Parent Provider
        ScoreCircleQueueCommand circleQueueCommand = new ScoreCircleQueueCommand();
        circleQueueCommand.setGroupId(newExam.getGroupId());
        circleQueueCommand.setContent(iMContent);
        circleQueueCommand.setCreateDate(new Date());
        circleQueueCommand.setGroupCircleType("EVALUATION");
        circleQueueCommand.setTypeId(newExamId);
        circleQueueCommand.setLinkUrl("/view/evaluation/report?examId=" + newExamId + "&isEvaluation=true");
        newExamParentQueueProducer.getProducer().produce(Message.newMessage().writeObject(circleQueueCommand));

        //新的极光push
        Map<String, Object> jpushExtInfo = new HashMap<>();
        jpushExtInfo.put("studentId", "");
        jpushExtInfo.put("s", ParentAppPushType.HOMEWORK_CHECK.name());
        jpushExtInfo.put("url", "/view/evaluation/report?examId=" + newExamId + "&isEvaluation=true");
        appMessageServiceClient.sendAppJpushMessageByTags(em_push_title,
                AppMessageSource.PARENT,
                Collections.singletonList(JpushUserTag.CLAZZ_GROUP_REFACTOR.generateTag(SafeConverter.toString(newExam.getGroupId()))),
                null,
                jpushExtInfo);
        //************** end 给班群发送消息 **************//
        return MapMessage.successMessage();
    }

    @Override
    public List<EvaluationAverScoreInfo> fetchAverScoreByExamIds(Long groupId, String unitId) {
        // 获取单元下的试卷
        String bookId = "";
        NewBookCatalog unit = newContentLoaderClient.loadBookCatalogByCatalogId(unitId);
        if (unit != null && CollectionUtils.isNotEmpty(unit.getAncestors())) {
            for (NewBookCatalogAncestor ancestor : unit.getAncestors()) {
                if (StringUtils.equalsIgnoreCase(ancestor.getNodeType(), BookCatalogType.BOOK.name())) {
                    bookId = ancestor.getId();
                    break;
                }
            }
        }
        Set<String> paperIds = new LinkedHashSet<>();
        if (StringUtils.isNotBlank(bookId)) {
            paperIds = paperLoaderClient.loadPaperAsListByNewBooKIdAndExtraTypes(bookId, Collections.singleton(NewPaper.ExtraQuestionType.EVALUATION))
                    .stream()
                    .filter(newPaper -> CollectionUtils.isNotEmpty(newPaper.getBooksNew()))
                    .filter(newPaper -> newPaper.getBooksNew().stream().anyMatch(book -> StringUtils.equals(book.getUnitId(), unitId)))
                    .map(NewPaper::getId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        if (CollectionUtils.isEmpty(paperIds)) {
            return Collections.emptyList();
        }

        Map<String, NewExam> paperIdToNewExamMap = new LinkedHashMap<>();
        List<String> newExamRegistrationIds = new LinkedList<>();
        List<User> users = studentLoaderClient.loadGroupStudents(groupId);
        List<NewExam> newExams = newExamLoaderClient.loadByGroupIds(Collections.singleton(groupId)).get(groupId);
        if (CollectionUtils.isEmpty(newExams)) {
            return Collections.emptyList();
        }
        Set<String> finalPaperIds = paperIds;
        if (CollectionUtils.isNotEmpty(newExams)) {
            newExams = newExams.stream()
                    .filter(newExam -> finalPaperIds.contains(newExam.obtainRandomPaperId()))
                    .sorted((o1, o2) -> Long.compare(o2.getCreatedAt().getTime(), o1.getCreatedAt().getTime()))
                    .collect(Collectors.toList());
            for (NewExam newExam : newExams) {
                String paperId = newExam.obtainRandomPaperId();
                if (!paperIdToNewExamMap.containsKey(paperId)) {
                    paperIdToNewExamMap.put(paperId, newExam);
                    String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
                    newExamRegistrationIds.addAll(users
                            .stream()
                            .map(o -> new NewExamRegistration.ID(month, newExam.getSubject(), newExam.getId(), o.getId().toString()).toString())
                            .collect(Collectors.toList()));
                }
            }
        }
        Map<String, EvaluationAverScoreInfo> evaluationAverScoreInfoMap = new LinkedHashMap<>();
        List<EvaluationAverScoreInfo> evaluationAverScoreInfos = new LinkedList<>();
        Map<String, NewPaper> paperMap = new LinkedHashMap<>();
        for (String paperId : paperIds) {
            //没有多个一次查询的接口，也没法写一次查询的接口
            NewPaper newPaper = paperLoaderClient.loadLatestPaperByDocId(paperId);
            if (newPaper == null)
                continue;
            paperMap.put(paperId, newPaper);
            EvaluationAverScoreInfo evaluationAverScoreInfo = new EvaluationAverScoreInfo();
            evaluationAverScoreInfo.setPaperId(paperId);
            evaluationAverScoreInfo.setPaperName(newPaper.getTitle());
            if (paperIdToNewExamMap.containsKey(paperId)) {
                evaluationAverScoreInfo.setNewExamId(paperIdToNewExamMap.get(paperId).getId());
                evaluationAverScoreInfo.setAssigned(true);
            }
            evaluationAverScoreInfo.setAssigned(paperIdToNewExamMap.containsKey(paperId));
            evaluationAverScoreInfoMap.put(paperId, evaluationAverScoreInfo);
            evaluationAverScoreInfos.add(evaluationAverScoreInfo);
        }
        Map<String, NewExamResult> newExamResultMap = newExamResultDao.loads(newExamRegistrationIds);

        for (NewExamResult newExamResult : newExamResultMap.values()) {
            if (newExamResult.getFinishAt() == null)
                continue;
            String paperId = newExamResult.getPaperId();
            if (!paperMap.containsKey(paperId))
                continue;
            NewPaper newPaper = paperMap.get(paperId);
            if (!evaluationAverScoreInfoMap.containsKey(paperId))
                continue;
            double score = newExamResult.processScore(SafeConverter.toInt(newPaper.getTotalScore()));
            EvaluationAverScoreInfo evaluationAverScoreInfo = evaluationAverScoreInfoMap.get(paperId);
            evaluationAverScoreInfo.setTotalNum(1 + evaluationAverScoreInfo.getTotalNum());
            int totalScore = new BigDecimal(score).setScale(0, BigDecimal.ROUND_HALF_UP).intValue() + evaluationAverScoreInfo.getTotalScore();
            evaluationAverScoreInfo.setTotalScore(totalScore);
        }
        evaluationAverScoreInfos.stream()
                .filter(EvaluationAverScoreInfo::isAssigned)
                .filter(o -> o.getTotalNum() > 0)
                .filter(o -> o.getTotalScore() > 0)
                .forEach(o -> {
                    int averScore = new BigDecimal(o.getTotalScore()).divide(new BigDecimal(o.getTotalNum()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                    o.setAverScore(averScore);
                });
        return evaluationAverScoreInfos;
    }
}
