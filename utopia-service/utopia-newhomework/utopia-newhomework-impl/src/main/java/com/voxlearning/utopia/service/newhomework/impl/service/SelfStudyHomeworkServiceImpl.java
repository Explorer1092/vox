package com.voxlearning.utopia.service.newhomework.impl.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.MongoWriteException;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.PropertiesUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalogAncestor;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePoint;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.client.PracticeServiceClient;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewKnowledgePointLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.QuestionCorrectStatus;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.FinishSelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.SelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.SelfStudyRotReport;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkReport;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.SelfStudyHomeworkInfoMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.SelfStudyHomeworkDoResp;
import com.voxlearning.utopia.service.newhomework.api.service.SelfStudyHomeworkService;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkBookDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkReportDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.pubsub.InterventionPublisher;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy.SelfStudyHomeworkResultProcessor;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy.finish.FinishSelfStudyHomeworkProcessor;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisErrorFactor;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.video.MicroVideoTask;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.DETAIL_COURSE_NAME;

/**
 * 自学作业生成
 *
 * @author xuesong.zhang
 * @since 2017/1/19
 */
@Named
@ExposeService(interfaceClass = SelfStudyHomeworkService.class)
public class SelfStudyHomeworkServiceImpl extends SpringContainerSupport implements SelfStudyHomeworkService {
    @Inject private SelfStudyHomeworkDao selfStudyHomeworkDao;
    @Inject private SelfStudyHomeworkBookDao selfStudyHomeworkBookDao;
    @Inject private SelfStudyHomeworkResultDao selfStudyHomeworkResultDao;
    @Inject private SelfStudyHomeworkReportDao selfStudyHomeworkReportDao;
    @Inject private SelfStudyHomeworkResultProcessor selfStudyHomeworkResultProcessor;
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;
    @Inject private FinishSelfStudyHomeworkProcessor finishSelfStudyHomeworkProcessor;
    @Inject private PracticeServiceClient practiceServiceClient;
    @Inject private NewContentLoaderClient newContentLoaderClient;
    @Inject private IntelDiagnosisClient intelDiagnosisClient;
    @Inject private InterventionPublisher interventionPublisher;
    @Inject private NewKnowledgePointLoaderClient newKnowledgePointLoaderClient;

    @Override
    public void insertSelfStudyHomework(SelfStudyHomework entity) {
        if (entity != null) {
            try {
                selfStudyHomeworkDao.insert(entity);
            } catch (MongoWriteException ignored) {
            }
        }
    }

    @Override
    public void insertSelfStudyHomeworks(Collection<SelfStudyHomework> entities) {
        if (CollectionUtils.isNotEmpty(entities)) {
            try {
                selfStudyHomeworkDao.inserts(entities);
            } catch (MongoBulkWriteException ignored) {
            }
        }
    }

    @Override
    public void insertSelfStudyHomeworkBook(SelfStudyHomeworkBook entity) {
        if (entity != null) {
            try {
                selfStudyHomeworkBookDao.insert(entity);
            } catch (MongoWriteException ignored) {
            }
        }
    }

    @Override
    public void insertSelfStudyHomeworkBooks(Collection<SelfStudyHomeworkBook> entities) {
        if (CollectionUtils.isNotEmpty(entities)) {
            try {
                selfStudyHomeworkBookDao.inserts(entities);
            } catch (MongoBulkWriteException ignored) {
            }
        }
    }

    @Override
    public void insertSelfStudyHomeworkResult(SelfStudyHomeworkResult entity) {
        if (entity != null) {
            try {
                selfStudyHomeworkResultDao.insert(entity);
            } catch (MongoWriteException ignored) {
            }
        }
    }

    @Override
    public void insertSelfStudyHomeworkResults(Collection<SelfStudyHomeworkResult> entities) {
        if (CollectionUtils.isNotEmpty(entities)) {
            try {
                selfStudyHomeworkResultDao.inserts(entities);
            } catch (MongoBulkWriteException ignored) {
            }
        }
    }

    @Override
    public void insertSelfStudyHomeworkReport(SelfStudyHomeworkReport entity) {
        if (entity != null) {
            try {
                selfStudyHomeworkReportDao.insert(entity);
            } catch (MongoWriteException ignored) {
            }
        }
    }

    @Override
    public void insertSelfStudyHomeworkReports(Collection<SelfStudyHomeworkReport> entities) {
        if (CollectionUtils.isNotEmpty(entities)) {
            try {
                selfStudyHomeworkReportDao.inserts(entities);
            } catch (MongoBulkWriteException ignored) {
            }
        }
    }

    @Override
    public Map<String, Object> generateIndexData(String homeworkId, Long studentId) {
        if (StringUtils.isBlank(homeworkId) || null == studentId) {
            return Collections.emptyMap();
        }
        SelfStudyHomework homework = selfStudyHomeworkDao.load(homeworkId);
        if (null == homework) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("homeworkId", homework.getId());
        result.put("homeworkType", homework.getNewHomeworkType());
        result.put("homeworkTag", homework.getHomeworkTag());
        NewHomework sourceHomework = newHomeworkLoader.load(homework.getSourceHomeworkId());
        Date homeworkStartDate = sourceHomework == null ? new Date() : sourceHomework.getStartTime() == null ? new Date() : sourceHomework.getStartTime();
        result.put("homeworkName", DateUtils.dateToString(homeworkStartDate, "MM月dd日") + homework.getSubject().getValue() + "作业订正");
        result.put("subject", homework.getSubject());

        SelfStudyHomeworkResult homeworkResult = selfStudyHomeworkResultDao.load(homework.getId());
        LinkedHashMap<ObjectiveConfigType, BaseHomeworkResultAnswer> doPractices = new LinkedHashMap<>();
        if (homeworkResult != null && MapUtils.isNotEmpty(homeworkResult.getPractices())) {
            doPractices = homeworkResult.getPractices();
        }

        int undoPracticesCount = 0, totalQuestionCount = 0, doTotalQuestionCount = 0, errorsCount = 0, courseCount = 0;
        boolean needFinish = false;
        Integer questionIndex = 0;
        Map<ObjectiveConfigType, SelfStudyHomeworkInfoMapper> infoMapper = statisticSelfStudyInfo(homework, homeworkResult);
        Map<ObjectiveConfigType, NewHomeworkPracticeContent> practiceMap = homework.findPracticeContents();

        List<Map<String, Object>> practiceInfos = Lists.newLinkedList();
        for (ObjectiveConfigType objectiveConfigType : practiceMap.keySet()) {
            BaseHomeworkResultAnswer baseHomeworkResultAnswer = doPractices.get(objectiveConfigType);
            NewHomeworkPracticeContent newHomeworkPracticeContent = practiceMap.get(objectiveConfigType);
            SelfStudyHomeworkInfoMapper mapper = infoMapper.getOrDefault(objectiveConfigType, null);
            int doCount = 0, questionCount = 0;
            List<SubHomeworkProcessResult> processResults = Lists.newArrayList();
            if (mapper != null) {
                doCount = mapper.getDoCount();
                processResults = mapper.getQuestionProcess();
            }

            List<Map<String, Object>> taskList = Lists.newLinkedList();

            if (CollectionUtils.isNotEmpty(newHomeworkPracticeContent.getApps())) {
                List<NewHomeworkApp> apps = new ArrayList<>();
                List<String> kpIds = new ArrayList<>();
                for(NewHomeworkApp app : newHomeworkPracticeContent.getApps()){
                    if(StringUtils.isNotEmpty(app.getCourseId()) && app.getCourseOrder() != null){
                        apps.add(app);
                        if(app.getErrorKpoints() != null){
                            for(ErrorKpoint kp : app.getErrorKpoints())
                            kpIds.add(kp.getErrorKpId());
                        }
                    }
                }
                apps = apps.stream().sorted(Comparator.comparing(NewHomeworkApp::getCourseOrder)).collect(Collectors.toList());

                courseCount += apps.size();
                if (ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS.equals(objectiveConfigType)) {
                    Map<String, IntelDiagnosisCourse> intelDiagnosisCourseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(Lists.transform(apps, NewHomeworkApp::getCourseId));
                    Map<String, NewKnowledgePoint> newKnowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePointsIncludeDeleted(kpIds);
                    Map<String, List<SubHomeworkProcessResult>> courseProcessMap = processResults.stream().collect(Collectors.groupingBy(SubHomeworkProcessResult::getCourseId));
                    for (NewHomeworkApp courseApp : apps) {
                        List<NewHomeworkQuestion> questions = courseApp.getQuestions();
                        List<ErrorQuestion> errorQuestions = courseApp.getErrorQuestions();
                        if (CollectionUtils.isNotEmpty(questions)) {
                            questionCount += questions.size();
                            List<String> similarQids = Lists.transform(questions, NewHomeworkQuestion::getQuestionId);
                            List<SubHomeworkProcessResult> doProcessResult = courseProcessMap.get(courseApp.getCourseId());

                            String qTitle = "错题" + (errorQuestions != null ? errorQuestions.size() : 0) + "道";
                            List<String> kpNames = new ArrayList<>();
                            if(CollectionUtils.isNotEmpty(courseApp.getErrorKpoints())){
                                for(ErrorKpoint kpoint : courseApp.getErrorKpoints()){
                                    NewKnowledgePoint kp = newKnowledgePointMap.get(kpoint.getErrorKpId());
                                    if(kp != null){
                                        kpNames.add(kp.getName());
                                    }
                                }
                                if(CollectionUtils.isNotEmpty(kpNames)){
                                    qTitle = "错误：" + StringUtils.join(kpNames, ",");
                                }
                            }
                            IntelDiagnosisCourse course = intelDiagnosisCourseMap.get(courseApp.getCourseId());
                            if(course != null){
                                Map<String, Object> taskMap = MapUtils.m("qTitle", qTitle,
                                        "courseId", courseApp.getCourseId(),
                                        "diagnosisSource", courseApp.getDiagnosisSource(),
                                        "courseName", StringUtils.isBlank(course.getName()) ? DETAIL_COURSE_NAME : course.getName(),
                                        "category", course.getCategory(),
                                        "status", CollectionUtils.isEmpty(doProcessResult) ? QuestionCorrectStatus.TODO : doProcessResult.size() == similarQids.size() ? QuestionCorrectStatus.FINISH : QuestionCorrectStatus.DOING,
                                        "correctCount", doProcessResult == null ? 0 : doProcessResult.stream().filter(SubHomeworkProcessResult::getGrasp).count(),
                                        "wrongCount", doProcessResult == null ? 0 : doProcessResult.stream().filter(o -> !o.getGrasp()).count(),
                                        "similarQids", similarQids);//一个课程对应多道错题, 也有多道后测题
                                taskList.add(taskMap);
                            }
                        }
                    }
                } else if (ObjectiveConfigType.ORAL_INTERVENTIONS.equals(objectiveConfigType)) {
                    Map<String, String> videoCourseName = getVideoCourseName(Lists.transform(apps, NewHomeworkApp::getCourseId));
                    questionCount += apps.size();
                    for (NewHomeworkApp courseApp : apps) {
                        String courseId = courseApp.getCourseId();
                        QuestionCorrectStatus status = QuestionCorrectStatus.TODO;
                        if (homeworkResult != null) {
                            LinkedHashMap<String, BaseHomeworkResultAppAnswer> appAnswer = homeworkResult.findAppAnswer(objectiveConfigType);
                            status = appAnswer.get(courseId) == null ? QuestionCorrectStatus.TODO : appAnswer.get(courseId).isGrasp() ? QuestionCorrectStatus.CORRECT : QuestionCorrectStatus.WRONG;
                        }
                        Map<String, Object> taskMap = MapUtils.m(
                                "courseId", courseId,
                                "diagnosisSource", courseApp.getDiagnosisSource(),
                                "courseName", videoCourseName.getOrDefault(courseId, ""),
                                "status", status);
                        taskList.add(taskMap);
                    }
                }
            } else if (CollectionUtils.isNotEmpty(newHomeworkPracticeContent.getQuestions())){
                Map<String, SubHomeworkProcessResult> questionProcessMap = processResults.stream().collect(Collectors.toMap(SubHomeworkProcessResult::getQuestionId, t -> t, (k1, k2) -> k1));
                List<NewHomeworkQuestion> questions = newHomeworkPracticeContent.getQuestions();
                if (CollectionUtils.isNotEmpty(questions)) {
                    errorsCount = questions.size();
                    questionCount += errorsCount;
                    for (NewHomeworkQuestion question : questions) {
                        SubHomeworkProcessResult subHomeworkProcessResult = questionProcessMap.get(question.getQuestionId());
                        QuestionCorrectStatus correctStatus = subHomeworkProcessResult == null ? QuestionCorrectStatus.TODO : (subHomeworkProcessResult.getGrasp() ? QuestionCorrectStatus.CORRECT : QuestionCorrectStatus.WRONG);
                        Map<String, Object> taskMap = MapUtils.m("qTitle", StringUtils.join("第", ++questionIndex, "题"),
                                "qIds", Collections.singletonList(question.getQuestionId()),
                                "status", correctStatus,
                                "similarQid", question.getQuestionId());
                        taskList.add(taskMap);
                    }
                }
            }
            Map<String, Object> practiceInfo = MapUtils.m(
                    "objectiveConfigType", objectiveConfigType,
                    "objectiveConfigTypeName", objectiveConfigType.getValue(),
                    "taskList", taskList,
                    "doHomeworkUrl", UrlUtils.buildUrlQuery("/student/selfstudy/homework/do.vpage", MapUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType, "sid", studentId)),
                    "finished", baseHomeworkResultAnswer != null && baseHomeworkResultAnswer.getFinishAt() != null);
            practiceInfos.add(practiceInfo);
            totalQuestionCount += questionCount;
            doTotalQuestionCount += doCount;
            if (baseHomeworkResultAnswer == null || doCount < questionCount) {
                undoPracticesCount++;
            }
            // 所有题都已做完，但缺少finishAt
            if ((questionCount == doCount && (baseHomeworkResultAnswer == null || !baseHomeworkResultAnswer.isFinished()))) {
                needFinish = true;
            }
        }

        result.put("courseCount", courseCount);
        result.put("errorsCount", errorsCount);
        boolean finished = homeworkResult != null && homeworkResult.isFinished();
        result.put("finished", finished);
        result.put("practices", practiceInfos);
        //总进度=已完成题目总数/作业题目总数
        result.put("finishingRate", new BigDecimal(doTotalQuestionCount * 100).divide(new BigDecimal(totalQuestionCount), 0, BigDecimal.ROUND_HALF_UP).intValue());
        selfStudyHomeworkResultDao.initSelfStudyHomeworkResult(homework.toLocation());
        if ((undoPracticesCount == 0 && !finished) || needFinish) {
            finishHomework(homework, homeworkResult, studentId);
        }
        return result;
    }


    private Map<String, String> getVideoCourseName(Collection<String> courseIds) {
        Map<String, MicroVideoTask> microVideoTaskMap = intelDiagnosisClient.loadMicroVideoTaskByIdsIncludeDisabled(courseIds);
        if (MapUtils.isNotEmpty(microVideoTaskMap)) {
            return microVideoTaskMap.values().stream().collect(Collectors.toMap(MicroVideoTask::getId, o -> StringUtils.isBlank(o.getName()) ? DETAIL_COURSE_NAME : o.getName()));
        }
        return Collections.emptyMap();
    }

    @Override
    public MapMessage homeworkForObjectiveConfigTypeResult(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId) {
        SelfStudyHomework homework = selfStudyHomeworkDao.load(homeworkId);
        if (null == homework) {
            return MapMessage.errorMessage().setInfo("作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
        }
        if (objectiveConfigType == null) {
            return MapMessage.errorMessage().setInfo("作类型不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_CONTENT_IS_NULL);
        }
        SelfStudyHomeworkResult homeworkResult = selfStudyHomeworkResultDao.load(homeworkId);
        if (homeworkResult == null) {
            return MapMessage.errorMessage().setInfo("SelfStudyHomeworkResult is null").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_FINISHED);
        }

        NewHomework newHomework = new NewHomework();
        PropertiesUtils.copyProperties(newHomework, homework);
        Map<ObjectiveConfigType, List<String>> answerIdsMap = newHomeworkResultLoader.initSubHomeworkResultAnswerIdsMap(newHomework, studentId);

        if (StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.BASIC_APP.name())) {
            return homeworkForBasicAppResult(objectiveConfigType, homeworkResult, answerIdsMap);
        } else {
            return homeworkForExamResult(newHomework, objectiveConfigType, homeworkResult, answerIdsMap);
        }
    }

    @Override
    public Map<String, Object> loadHomeworkQuestions(String homeworkId, ObjectiveConfigType objectiveConfigType, String courseId) {
        if (StringUtils.isBlank(homeworkId) || objectiveConfigType == null) {
            return Collections.emptyMap();
        }
        SelfStudyHomework homework = selfStudyHomeworkDao.load(homeworkId);
        if (homework == null) {
            return Collections.emptyMap();
        }
        Map<String, NewHomeworkBookInfo> bookInfoMap = getBookInfo(homeworkId, objectiveConfigType);

        Map<String, Map<String, Object>> examUnitMap = new LinkedHashMap<>();
        Set<String> eids = new LinkedHashSet<>();
        Set<String> sQids = new LinkedHashSet<>();
        int normalTime = 0;
        if (objectiveConfigType.equals(ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS)) {
            NewHomeworkPracticeContent homeworkPracticeContent = homework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(objectiveConfigType);
            if (homeworkPracticeContent != null && CollectionUtils.isNotEmpty(homeworkPracticeContent.getApps())) {
                NewHomeworkApp newHomeworkApp = homeworkPracticeContent.getApps().stream().filter(p -> p.getCourseId().equals(courseId)).findFirst().orElse(null);
                if (newHomeworkApp == null) {
                    return Collections.emptyMap();
                }
                Map<String, IntelDiagnosisCourse> intelDiagnosisCourseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(Collections.singleton(newHomeworkApp.getCourseId()));
                IntelDiagnosisCourse course = intelDiagnosisCourseMap.get(newHomeworkApp.getCourseId());
                String courseName = course != null ? (StringUtils.isBlank(course.getName()) ? DETAIL_COURSE_NAME : course.getName() ) : "";
                String advice = StringUtils.join("认真学习1-3分钟课程《", courseName, "》，并完成后测题。");

                List<ErrorQuestion> errorQuestionList = newHomeworkApp.getErrorQuestions();
                Map<String, String> errorCauseQuestionMap = this.getErrorCauseMap(errorQuestionList);

                //原作业错题
                List<Map<String, String>> sourceQuestions = Lists.newLinkedList();
                if (CollectionUtils.isNotEmpty(errorQuestionList)) {
                    for (ErrorQuestion errorQuestion : errorQuestionList) {
                        Map<String, String> sourceQuestionMap = Maps.newLinkedHashMap();
                        String sourceQuestionId = errorQuestion.getErrorQuestionId();
                        sourceQuestionMap.put("sourceQuestionId", sourceQuestionId);
                        sourceQuestionMap.put("errorCause", errorCauseQuestionMap.getOrDefault(sourceQuestionId, NewHomeworkConstants.THE_DEFAULT_ERROR_CAUSE));
                        sourceQuestionMap.put("advice", advice);
                        sourceQuestions.add(sourceQuestionMap);
                        eids.add(sourceQuestionId);
                    }
                }

                //自学作业要做的题(类题)
                List<Map<String, Object>> similarQuestions = Lists.newLinkedList();
                for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkApp.getQuestions()) {
                    normalTime += newHomeworkQuestion.getSeconds();
                    NewHomeworkBookInfo bookInfo = getBookInfo(bookInfoMap, newHomeworkQuestion);
                    similarQuestions.add(MapUtils.m("bookId", bookInfo.getBookId(),
                            "unitId", bookInfo.getUnitId(),
                            "lessonId", bookInfo.getLessonId(),
                            "unitGroupId", bookInfo.getUnitGroupId(),
                            "sectionId", bookInfo.getSectionId(),
                            "similarQuestionId", newHomeworkQuestion.getQuestionId()));
                    sQids.add(newHomeworkQuestion.getQuestionId());
                }
                examUnitMap.put(newHomeworkApp.getCourseId(), MapUtils.m("sourceQuestions", sourceQuestions,
                        "similarQuestions", similarQuestions,
                        "expGroupId", newHomeworkApp.getExperimentGroupId(),
                        "expId", newHomeworkApp.getExperimentId()));
            }
        } else {
            List<NewHomeworkQuestion> newHomeworkQuestions = homework.findNewHomeworkQuestions(objectiveConfigType);
            for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkQuestions) {
                normalTime += newHomeworkQuestion.getSeconds();

                String sourceQuestionId = newHomeworkQuestion.getQuestionId();
                NewHomeworkBookInfo bookInfo = getBookInfo(bookInfoMap, newHomeworkQuestion);
                examUnitMap.put(sourceQuestionId, MapUtils.m("bookId", bookInfo.getBookId(),
                        "unitId", bookInfo.getUnitId(),
                        "lessonId", bookInfo.getLessonId(),
                        "unitGroupId", bookInfo.getUnitGroupId(),
                        "sectionId", bookInfo.getSectionId(),
                        "similarQuestionId", sourceQuestionId));
                eids.add(sourceQuestionId);
                sQids.add(sourceQuestionId);
            }
        }
        return MapUtils.m("examUnitMap", examUnitMap,
                "normalTime", normalTime,
                "eids", eids,
                "sQids", sQids,
                "homeworkType", homework.getNewHomeworkType(),
                "homeworkTag", homework.getHomeworkTag());
    }

    private Map<String, NewHomeworkBookInfo> getBookInfo(String homeworkId, ObjectiveConfigType objectiveConfigType) {
        Map<String, NewHomeworkBookInfo> bookInfoMap = new HashMap<>();
        SelfStudyHomeworkBook homeworkBook = selfStudyHomeworkBookDao.load(homeworkId);
        if (homeworkBook != null) {
            List<NewHomeworkBookInfo> newHomeworkBookInfos = homeworkBook.getPractices().get(objectiveConfigType);
            if (newHomeworkBookInfos != null) {
                for (NewHomeworkBookInfo info : newHomeworkBookInfos) {
                    if (CollectionUtils.isNotEmpty(info.getQuestions())) {
                        for (String qid : info.getQuestions()) {
                            bookInfoMap.put(qid, info);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(info.getPapers())) {
                        for (String pid : info.getPapers()) {
                            bookInfoMap.put(pid, info);
                        }
                    }
                }
            }
        }
        return bookInfoMap;
    }

    private NewHomeworkBookInfo getBookInfo(Map<String, NewHomeworkBookInfo> bookInfoMap, NewHomeworkQuestion newHomeworkQuestion) {
        NewHomeworkBookInfo bookInfo;
        if (StringUtils.isNoneBlank(newHomeworkQuestion.getPaperId())) {
            bookInfo = bookInfoMap.getOrDefault(newHomeworkQuestion.getPaperId(), new NewHomeworkBookInfo());
        } else {
            bookInfo = bookInfoMap.getOrDefault(newHomeworkQuestion.getQuestionId(), new NewHomeworkBookInfo());
        }
        return bookInfo;
    }

    private Map<String, String> getErrorCauseMap(List<ErrorQuestion> errorQuestionList) {
        if(errorQuestionList != null){
            List<String> errorCauseIds = Lists.transform(errorQuestionList, ErrorQuestion::getErrorCauseId);
            Map<String, IntelDiagnosisErrorFactor> intelDiagnosisErrorFactorMap = intelDiagnosisClient.loadErrorFactorsByIdIncludeDisabled(errorCauseIds);
            Map<String, String> errorCauseMap = MapUtils.transform(intelDiagnosisErrorFactorMap, IntelDiagnosisErrorFactor::getDescription);
            return errorQuestionList.stream()
                    .collect(Collectors.toMap(ErrorQuestion::getErrorQuestionId,
                            e -> errorCauseMap.get(e.getErrorCauseId()) == null ? NewHomeworkConstants.THE_DEFAULT_ERROR_CAUSE : errorCauseMap.get(e.getErrorCauseId())));
        }else {
            return Collections.emptyMap();
        }

    }

    @Override
    public Map<String, Object> loadHomeworkQuestionsAnswer(String homeworkId, Long studentId, ObjectiveConfigType objectiveConfigType, String courseId) {
        if (StringUtils.isBlank(homeworkId) || studentId == null || objectiveConfigType == null) {
            return Collections.emptyMap();
        }
        SelfStudyHomework homework = selfStudyHomeworkDao.load(homeworkId);
        if (homework == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> questionAnswerMap = new HashMap<>();
        // 如果是订正, 且为巩固学习的，下面需要取到当时作业中做错的题的作答信息
        if (HomeworkTag.Correct.equals(homework.getHomeworkTag()) && ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS.equals(objectiveConfigType)) {
            String sourceHomeworkId = homework.getSourceHomeworkId();
            NewHomework newHomework = newHomeworkLoader.load(sourceHomeworkId);
            NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);

            Map<String, Object> wrongQuestionAnswerMap = new HashMap<>();
            //巩固学习一道后测题对应多道原题
            List<ErrorQuestion> errorQuestions = homework.findAppErrorQuestionsMap(objectiveConfigType).get(courseId);
            if (CollectionUtils.isNotEmpty(errorQuestions)) {
                //只查询做错的题的做题记录
                Map<String, String> homeworkProcessIdsMap = newHomeworkResult.findHomeworkProcessIdsMap();
                List<String> sourceQuestionIds = Lists.transform(errorQuestions, ErrorQuestion::getErrorQuestionId);
                Collection<String> sourceWrongProcessIds = Maps.filterKeys(homeworkProcessIdsMap, sourceQuestionIds::contains).values();

                // 做错的qid,对应的做题详情
                Map<String, NewHomeworkProcessResult> wrongQuestionProcessResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), sourceWrongProcessIds)
                        .values()
                        .stream()
                        .collect(Collectors.toMap(NewHomeworkProcessResult::getQuestionId, Function.identity(), (v1, v2) -> v2, LinkedHashMap::new));

                errorQuestions.forEach(e -> {
                    String sourceQId = e.getErrorQuestionId();// 原作业错题
                    NewHomeworkProcessResult processResult = wrongQuestionProcessResultMap.getOrDefault(sourceQId, null);
                    if (processResult != null) {
                        Map<String, Object> value = MapUtils.m(
                                "sourceQuestion", sourceQId,
                                "files", processResult.getFiles(),
                                "subMaster", processResult.getSubGrasp(),
                                "master", processResult.getGrasp(),
                                "userAnswers", processResult.getUserAnswers(),
                                "fullScore", processResult.getStandardScore(),
                                "score", processResult.getScore()
                        );
                        wrongQuestionAnswerMap.put(sourceQId, value);
                    }
                });
            }
            questionAnswerMap.put("sourceQuestionAnswers", wrongQuestionAnswerMap);
        }

        if (!selfStudyHomeworkResultDao.exists(homeworkId)) {
            return questionAnswerMap;
        }

        NewHomework selfStudyHomework = new NewHomework();
        PropertiesUtils.copyProperties(selfStudyHomework, homework);
        Map<ObjectiveConfigType, List<String>> answerProcessIdsMap = newHomeworkResultLoader.initSubHomeworkResultAnswerIdsMap(selfStudyHomework, studentId);
        Collection<String> processIds = newHomeworkResultLoader.loadSubHomeworkResultAnswers(answerProcessIdsMap.get(objectiveConfigType))
                .values()
                .stream()
                .map(SubHomeworkResultAnswer::getProcessId)
                .collect(Collectors.toSet());

        Map<String, SubHomeworkProcessResult> subHomeworkProcessResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(processIds);
        List<SubHomeworkProcessResult> processResults = subHomeworkProcessResultMap.values().stream().filter(p -> StringUtils.isBlank(courseId) || courseId.equals(p.getCourseId())).collect(Collectors.toList());

        Map<String, Object> doQuestionAnswerMap = new HashMap<>();
        for (SubHomeworkProcessResult processResult : processResults) {
            Map<String, Object> value = MapUtils.m(
                    "files", processResult.getFiles(),
                    "subMaster", processResult.getSubGrasp(),
                    "master", processResult.getGrasp(),
                    "userAnswers", processResult.getUserAnswers(),
                    "fullScore", processResult.getStandardScore(),
                    "score", processResult.getScore()
            );
            doQuestionAnswerMap.put(processResult.getQuestionId(), value);
        }
        // 非DIAGNOSTIC_INTERVENTIONS作业类型直接返回
        if (!ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS.equals(objectiveConfigType)) {
            return doQuestionAnswerMap;
        }
        questionAnswerMap.put("doQuestionAnswers", doQuestionAnswerMap);
        return questionAnswerMap;
    }

    @Override
    public MapMessage processorHomeworkResult(SelfStudyHomeworkContext homeworkResultContext) {
        try {
            SelfStudyHomeworkContext context = selfStudyHomeworkResultProcessor.process(homeworkResultContext);
            return context.transform().add("result", context.getResult());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @Override
    public List<SelfStudyHomeworkDoResp> fetchIntelDiagnosisCourse(String homeworkId, ObjectiveConfigType objectiveConfigType) {
        SelfStudyHomework homework = selfStudyHomeworkDao.load(homeworkId);
        if (homework == null) {
            return Collections.emptyList();
        }

        SelfStudyHomeworkResult studyHomeworkResult = selfStudyHomeworkResultDao.load(homeworkId);
        LinkedHashMap<ObjectiveConfigType, BaseHomeworkResultAnswer> practices = studyHomeworkResult.getPractices();
        LinkedHashMap<String, BaseHomeworkResultAppAnswer> appAnswers = Maps.newLinkedHashMap();
        if (MapUtils.isNotEmpty(practices) && practices.get(objectiveConfigType) != null) {
            appAnswers = practices.get(objectiveConfigType).getAppAnswers();
        }

        NewHomeworkPracticeContent homeworkPracticeContent = homework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(objectiveConfigType);
        if (homeworkPracticeContent == null) {
            return Collections.emptyList();
        }
        List<NewHomeworkApp> apps = homeworkPracticeContent.getApps();
        List<SelfStudyHomeworkDoResp> courseList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(apps)) {
            List<String> courseIds = Lists.transform(apps, NewHomeworkApp::getCourseId);
            ImmutableMap<String, NewHomeworkApp> courseAppMap = Maps.uniqueIndex(apps, NewHomeworkApp::getCourseId);
            if (CollectionUtils.isNotEmpty(courseIds)) {
                for (String courseId : courseIds) {
                    SelfStudyHomeworkDoResp resp = new SelfStudyHomeworkDoResp();
                    resp.setId(courseId);
                    if (ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS.equals(objectiveConfigType)) {
                        resp.setQuestionUrl(UrlUtils.buildUrlQuery("/student/selfstudy/homework/questions" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "courseId", courseId, "sid", homework.getStudentId())));
                        resp.setCompletedUrl(UrlUtils.buildUrlQuery("/student/selfstudy/homework/questions/answer" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "courseId", courseId, "sid", homework.getStudentId())));
                    }
                    resp.setFinished(appAnswers.containsKey(courseId));

                    //实验打点用
                    NewHomeworkApp newHomeworkApp = courseAppMap.get(courseId);
                    resp.setExpGroupId(newHomeworkApp.getExperimentGroupId());
                    resp.setExpId(newHomeworkApp.getExperimentId());
                    courseList.add(resp);
                }
            }
        }
        return courseList;
    }

    @Override
    public void selfStudyRotReport(SelfStudyRotReport selfStudyRotReport) {
        interventionPublisher.getCourseInterventionProducer().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(selfStudyRotReport)));
    }


    /**
     * 用于返回每种作业形式下错题订正的信息
     */
    private Map<ObjectiveConfigType, SelfStudyHomeworkInfoMapper> statisticSelfStudyInfo(SelfStudyHomework selfStudyHomework, SelfStudyHomeworkResult homeworkResult) {
        NewHomework newHomework = new NewHomework();
        PropertiesUtils.copyProperties(newHomework, selfStudyHomework);
        Map<ObjectiveConfigType, List<String>> subHomeworkResultAnswerIdsMap = newHomeworkResultLoader.initSubHomeworkResultAnswerIdsMap(newHomework, selfStudyHomework.getStudentId());

        Set<String> subHomeworkResultAnswerIds = subHomeworkResultAnswerIdsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        Set<String> processIds = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds)
                .values()
                .stream()
                .map(SubHomeworkResultAnswer::getProcessId)
                .collect(Collectors.toSet());

        // 实际做题结果
        Map<ObjectiveConfigType, List<SubHomeworkProcessResult>> processResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(processIds)
                .values()
                .stream()
                .collect(Collectors.groupingBy(SubHomeworkProcessResult::getObjectiveConfigType));

        Map<ObjectiveConfigType, SelfStudyHomeworkInfoMapper> result = new HashMap<>();
        for (ObjectiveConfigType objectiveConfigType : processResultMap.keySet()) {
            List<SubHomeworkProcessResult> results = processResultMap.get(objectiveConfigType);
            if (CollectionUtils.isNotEmpty(results)) {
                SelfStudyHomeworkInfoMapper mapper = new SelfStudyHomeworkInfoMapper();
                mapper.setQuestionProcess(results);
                mapper.setDoCount(results.size());
                result.put(objectiveConfigType, mapper);
            }
        }

        if (homeworkResult != null) {
            LinkedHashMap<String, BaseHomeworkResultAppAnswer> appAnswer = homeworkResult.findAppAnswer(ObjectiveConfigType.ORAL_INTERVENTIONS);
            if (MapUtils.isNotEmpty(appAnswer)) {
                SelfStudyHomeworkInfoMapper mapper = new SelfStudyHomeworkInfoMapper();
                mapper.setDoCount(appAnswer.size());
                result.put(ObjectiveConfigType.ORAL_INTERVENTIONS, mapper);
            }
        }
        return result;
    }


    private MapMessage homeworkForBasicAppResult(ObjectiveConfigType objectiveConfigType, SelfStudyHomeworkResult selfStudyHomeworkResult, Map<ObjectiveConfigType, List<String>> answerIdsMap) {
        LinkedHashMap<ObjectiveConfigType, BaseHomeworkResultAnswer> resultMap = selfStudyHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_FINISHED);
        }
        BaseHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        LinkedHashMap<String, BaseHomeworkResultAppAnswer> appResult = resultAnswer.getAppAnswers();

        List<String> processResultIds = newHomeworkResultLoader.loadSubHomeworkResultAnswers(answerIdsMap.get(objectiveConfigType))
                .values()
                .stream()
                .map(SubHomeworkResultAnswer::getProcessId)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(processResultIds)) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_FINISHED);
        }

        List<String> lessonIds = new ArrayList<>();
        for (BaseHomeworkResultAppAnswer nraa : appResult.values()) {
            lessonIds.add(nraa.getLessonId());
        }

        Map<String, List<String>> unitLessonsMap = new LinkedHashMap<>();
        Map<String, String> lessonUnitMap = handle(lessonIds);
        for (String lessonId : lessonUnitMap.keySet()) {
            String unitId = lessonUnitMap.get(lessonId);
            List<String> lids = unitLessonsMap.get(unitId);
            if (CollectionUtils.isEmpty(lids)) {
                lids = new ArrayList<>();
            }
            lids.add(lessonId);
            unitLessonsMap.put(unitId, lids);
        }

        Map<String, SubHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(processResultIds);

        Map<String, List<Map<String, Object>>> lessonCategoryMap = new HashMap<>();
        for (BaseHomeworkResultAppAnswer nraa : appResult.values()) {
            String lessonId = nraa.getLessonId();
            PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(SafeConverter.toLong(nraa.getPracticeId()));
            int errorCount = 0;
            int rightCount = 0;

            for (SubHomeworkProcessResult processResult : processResultMap.values()) {
                if (practiceType.getNeedRecord()) {
                    if ((processResult.getAppOralScoreLevel() != null && !AppOralScoreLevel.D.equals(processResult.getAppOralScoreLevel()))
                            || (processResult.getAppOralScoreLevel() == null && processResult.getScore() >= 40))
                        rightCount++;
                    else {
                        errorCount++;
                    }
                } else {
                    if (SafeConverter.toBoolean(processResult.getGrasp())) {
                        rightCount++;
                    } else {
                        errorCount++;
                    }
                }
            }

            List<Map<String, Object>> categories = lessonCategoryMap.get(lessonId);
            if (CollectionUtils.isEmpty(categories)) {
                categories = new ArrayList<>();
            }
            categories.add(MapUtils.m("categoryName", practiceType.getCategoryName(),
                    "needRecord", practiceType.getNeedRecord(),
                    "rightCount", rightCount,
                    "errorCount", errorCount));
            lessonCategoryMap.put(lessonId, categories);
        }
        Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        Map<String, NewBookCatalog> unitMap = newContentLoaderClient.loadBookCatalogByCatalogIds(unitLessonsMap.keySet());
        List<Map<String, Object>> results = new ArrayList<>();
        for (String unitId : unitLessonsMap.keySet()) {
            NewBookCatalog unit = unitMap.get(unitId);
            List<Map<String, Object>> lessonObjs = new ArrayList<>();
            if (unit == null) continue;
            for (String lessonId : unitLessonsMap.get(unitId)) {
                NewBookCatalog lesson = lessonMap.get(lessonId);
                List<Map<String, Object>> categorys = lessonCategoryMap.get(lessonId);
                if (lesson == null || categorys == null) continue;
                lessonObjs.add(MapUtils.m("lessonName", lesson.getAlias(),
                        "categorys", categorys));
            }
            if (CollectionUtils.isNotEmpty(lessonObjs)) {
                results.add(MapUtils.m("unitName", unit.getAlias(),
                        "lessons", lessonObjs));
            }
        }
        return MapMessage.successMessage().add("datas", results);
    }

    private MapMessage homeworkForExamResult(NewHomework homework, ObjectiveConfigType objectiveConfigType, SelfStudyHomeworkResult selfStudyHomeworkResult, Map<ObjectiveConfigType, List<String>> answerIdsMap) {
        LinkedHashMap<ObjectiveConfigType, BaseHomeworkResultAnswer> resultMap = selfStudyHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_FINISHED);
        }
        BaseHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);

        Map<String, String> questionIdToProcessIdMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(answerIdsMap.get(objectiveConfigType))
                .values()
                .stream()
                .collect(Collectors.toMap(o -> o.parseID().getQuestionId(), SubHomeworkResultAnswer::getProcessId));

        if (CollectionUtils.isEmpty(questionIdToProcessIdMap.values())) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_FINISHED);
        }

        List<NewHomeworkQuestion> homeworkQuestions = homework.findNewHomeworkQuestions(objectiveConfigType);
        Map<String, SubHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(questionIdToProcessIdMap.values());
        List<Map<String, Object>> results = new ArrayList<>();
        int rightCount = 0;
        int errorCount = 0;

        for (NewHomeworkQuestion homeworkQuestion : homeworkQuestions) {
            SubHomeworkProcessResult processResult = processResultMap.get(questionIdToProcessIdMap.get(homeworkQuestion.getQuestionId()));
            if (SafeConverter.toBoolean(processResult.getGrasp())) {
                rightCount++;
            } else {
                errorCount++;
            }
            results.add(MapUtils.m(
                    "questionId", processResult.getQuestionId(),
                    "score", new BigDecimal(SafeConverter.toDouble(processResult.getScore())).setScale(0, BigDecimal.ROUND_HALF_UP).intValue(),
                    "right", SafeConverter.toBoolean(processResult.getGrasp())));
        }
        return MapMessage.successMessage().add("datas", results).add("rightCount", rightCount).add("errorCount", errorCount).add("duration", resultAnswer.getDuration());
    }

    private Map<String, String> handle(List<String> ids) {
        Map<String, NewBookCatalog> ms = newContentLoaderClient.loadBookCatalogByCatalogIds(ids);
        Map<String, String> data = new LinkedHashMap<>();
        for (String a : ids) {
            NewBookCatalog newBookCatalog = ms.get(a);
            if (newBookCatalog == null) {
                UtopiaRuntimeException ex = new UtopiaRuntimeException("BookCatalog exception");
                logger.error("BookCatalog of the " + a + " is null", ex);
            } else {
                List<NewBookCatalogAncestor> l = newBookCatalog.getAncestors();
                Map<String, NewBookCatalogAncestor> m = l
                        .stream()
                        .collect(Collectors
                                .toMap(NewBookCatalogAncestor::getNodeType, Function.identity()));
                if (m.get("UNIT") != null) {
                    data.put(a, m.get("UNIT").getId());
                }
            }
        }
        return data;
    }

    private void finishHomework(SelfStudyHomework homework, SelfStudyHomeworkResult homeworkResult, Long studentId) {
        if (homeworkResult == null) {
            return;
        }

        LinkedHashMap<ObjectiveConfigType, NewHomeworkPracticeContent> homeworkPractice = homework.findPracticeContents();
        LinkedHashMap<ObjectiveConfigType, BaseHomeworkResultAnswer> homeworkResultPractices = homeworkResult.getPractices();

        for (ObjectiveConfigType type : homeworkPractice.keySet()) {

            //发音矫正没有后测题, 不存在丢数据情况,故跳过
            if (type.equals(ObjectiveConfigType.ORAL_INTERVENTIONS)) {
                continue;
            }
            if (ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS.equals(type)) {
                for (NewHomeworkApp newHomeworkApp : homeworkPractice.get(type).getApps()) {
                    String courseId = newHomeworkApp.getCourseId();

                    if (homeworkResultPractices == null || homeworkResultPractices.get(type) == null) {
                        doFinishHomework(homework, studentId, type, courseId);
                        return;//错误数据是小概率事件, 一次只修复一个课程
                    } else if (!homeworkResultPractices.get(type).isFinished()) {
                        LinkedHashMap<String, BaseHomeworkResultAppAnswer> appAnswers = homeworkResultPractices.get(type).getAppAnswers();

                        if (appAnswers == null || !appAnswers.containsKey(courseId)) {
                            doFinishHomework(homework, studentId, type, courseId);
                            return;//错误数据是小概率事件, 一次只修复一个课程, 坏的太多就多刷新几次
                        } else {
                            appAnswers.values().stream()
                                    .filter(o -> !o.isFinished())
                                    .findFirst()
                                    .ifPresent(appAnswer -> doFinishHomework(homework, studentId, type, appAnswer.getCourseId()));
                        }
                    }
                }
            } else {
                doFinishHomework(homework, studentId, type, null);
            }
        }
    }

    private void doFinishHomework(SelfStudyHomework homework, Long studentId, ObjectiveConfigType type, String courseId) {
        FinishSelfStudyHomeworkContext ctx = new FinishSelfStudyHomeworkContext();
        ctx.setUserId(studentId);
        ctx.setHomeworkId(homework.getId());
        ctx.setSelfStudyHomework(homework);
        ctx.setObjectiveConfigType(type);
        ctx.setAppChameleonId(courseId);
        finishSelfStudyHomeworkProcessor.process(ctx);
        LogCollector.info("backend-general", MapUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "usertoken", studentId,
                "mod1", homework.getId(),
                "mod2", type,
                "mod3", courseId,
                "op", "selfStudyDoFinishHomework"));
    }
}
