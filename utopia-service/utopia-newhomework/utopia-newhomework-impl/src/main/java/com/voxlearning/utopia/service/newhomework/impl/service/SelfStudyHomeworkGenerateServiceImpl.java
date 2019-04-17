package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.athena.api.cuotizhenduan.entity.*;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.*;
import com.voxlearning.utopia.service.newhomework.api.service.SelfStudyHomeworkGenerateService;
import com.voxlearning.utopia.service.newhomework.impl.athena.SelfStudyRecomLoaderClient;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.NewHomeworkQueueServiceImpl;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.*;

/**
 * @author xuesong.zhang
 * @since 2017/2/11
 */
@Named
@Service(interfaceClass = SelfStudyHomeworkGenerateService.class)
@ExposeService(interfaceClass = SelfStudyHomeworkGenerateService.class)
public class SelfStudyHomeworkGenerateServiceImpl extends SpringContainerSupport implements SelfStudyHomeworkGenerateService {

    @Inject private RaikouSDK raikouSDK;
    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;
    @Inject private NewHomeworkQueueServiceImpl newHomeworkQueueService;
    @Inject private NewHomeworkResultServiceImpl newHomeworkResultService;
    @Inject private NewHomeworkServiceImpl newHomeworkService;
    @Inject private QuestionLoaderClient questionLoaderClient;
    @Inject private SelfStudyRecomLoaderClient selfStudyRecomLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;

    public void generateSelfStudyHomework(NewHomework homework, NewHomeworkResult homeworkResult) {
        Set<ObjectiveConfigType> objectiveConfigTypes = homeworkResult.getPractices().keySet();
        // 普通类型，普通tag, 指定作业形式才有错题订正
        if (!GenerateHomeworkTypes.contains(homework.getType()) || !GenerateHomeworkTags.contains(homework.getHomeworkTag()) || !CollectionUtils.containsAny(objectiveConfigTypes, GenerateSelfStudyHomeworkConfigTypes)) {
            return;
        }
        List<String> processIds = getProcessIdsByObjectiveConfigType(homeworkResult);
        Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(homeworkResult.getHomeworkId(), processIds);
        List<NewHomeworkProcessResult> wrongProcessResultList = processResultMap.values().stream()
                .filter(o -> (!ObjectiveConfigType.MENTAL_ARITHMETIC.equals(o.getObjectiveConfigType()) && Objects.equals(Boolean.FALSE, o.getGrasp()))
                        || (ObjectiveConfigType.MENTAL_ARITHMETIC.equals(o.getObjectiveConfigType()) && Objects.equals(Boolean.FALSE, o.getGrasp()) && CollectionUtils.isNotEmpty(o.getUserAnswers()) && o.getUserAnswers().get(0).stream().noneMatch(StringUtils::isBlank))// 过滤掉口算速算里面的未作答的数据
                        || ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING.equals(o.getObjectiveConfigType())
                        || ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.equals(o.getObjectiveConfigType()))
                .distinct().collect(Collectors.toList());

        //不同作业形式相同题目去重
        wrongProcessResultList = distinctProcessResult(wrongProcessResultList);
        if (CollectionUtils.isNotEmpty(wrongProcessResultList)) {
            SelfStudyHomeworkGenerateServiceImpl.HwSource hwSource = null;
            if (Subject.ENGLISH.equals(homework.getSubject())) {
                hwSource = selfStudyEnglishCorrectHomework(homeworkResult, wrongProcessResultList, getQuestionBoxIdMap(homework));
            } else if (Subject.CHINESE.equals(homework.getSubject())) {
                hwSource = selfStudyChineseCorrectHomework(homeworkResult, wrongProcessResultList);
            } else {
                hwSource = selfStudyMathCorrectHomework(homeworkResult, wrongProcessResultList, getQuestionBoxIdMap(homework));
            }

            if (hwSource != null) {
                // noinspection unchecked
                HomeworkSource homeworkSource = HomeworkSource.newInstance(JsonUtils.fromJson(JsonUtils.toJson(hwSource), Map.class));
                Teacher teacher = teacherLoaderClient.loadTeacher(homework.getTeacherId());
                newHomeworkService.assignHomework(teacher, homeworkSource, HomeworkSourceType.App, NewHomeworkType.selfstudy, HomeworkTag.Correct);

                //生成订正任务, 初始化SubHomeworkResultExtendedInfo订正状态
                newHomeworkResultService.initCorrectStatus(homeworkResult);
            }
        }
    }

    /**
     * 自学任务，语文订正
     *
     * @param homeworkResult    作业结果
     * @param processResultList 作业完成后，需要订正的作业类型的错题信息List
     */
    private HwSource selfStudyChineseCorrectHomework(NewHomeworkResult homeworkResult, List<NewHomeworkProcessResult> processResultList) {

        List<String> sourceQuestionIds = Lists.transform(processResultList, NewHomeworkProcessResult::getQuestionId);
        Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(sourceQuestionIds);

        List<QuestionItem> correctionsQuestionItems = new ArrayList<>();
        for (NewHomeworkProcessResult newHomeworkProcessResult : processResultList) {
            String sourceQuestionId = newHomeworkProcessResult.getQuestionId();

            QuestionItem item = new QuestionItem();
            item.setQuestionId(sourceQuestionId);
            item.setBookId(newHomeworkProcessResult.getBookId());
            item.setUnitId(newHomeworkProcessResult.getUnitId());
            NewQuestion newQuestion = newQuestionMap.get(sourceQuestionId);
            item.setDuration(newQuestion == null ? 10 : newQuestion.getSeconds());
            item.setSimilarQuestionId(sourceQuestionId);
            correctionsQuestionItems.add(item);
        }
        Map<String, List<QuestionItem>> objectiveConfigTypeQuestionMap = new LinkedHashMap<>();
        objectiveConfigTypeQuestionMap.put(ObjectiveConfigType.DIAGNOSTIC_CORRECTIONS.name(), correctionsQuestionItems);
        return buildHwSource(homeworkResult.getClazzGroupId(), homeworkResult.getUserId(), homeworkResult.getHomeworkId(), objectiveConfigTypeQuestionMap, homeworkResult.getSubject());
    }

    /**
     * 当前作业讲练测->题目ID和题包ID对应关系
     *
     * @return <questionId, questionBoxId>
     */
    private Map<String, String> getQuestionBoxIdMap(NewHomework homework) {
        List<NewHomeworkQuestion> newHomeworkQuestions = homework.findNewHomeworkQuestions(INTELLIGENT_TEACHING_CONFIGTYPE);
        return newHomeworkQuestions.stream().collect(Collectors.toMap(NewHomeworkQuestion::getQuestionId, NewHomeworkQuestion::getQuestionBoxId));
    }

    /**
     * 自学任务，数学订正
     *
     * @param homeworkResult    作业结果
     * @param processResultList 作业完成后，需要订正的作业类型的错题信息List
     * @param questionBoxIdMap
     */
    private HwSource selfStudyMathCorrectHomework(NewHomeworkResult homeworkResult, List<NewHomeworkProcessResult> processResultList, Map<String, String> questionBoxIdMap) {
        //调大数据接口命中课程任务
        StudentMathHomeworkDiagnosisPkg studentMathHomeworkDiagnosisPkg = getStudentMathHomeworkDiagnosisPkg(homeworkResult.getUserId(), processResultList, homeworkResult.getClazzGroupId(), homeworkResult.getHomeworkId());
        if (studentMathHomeworkDiagnosisPkg == null) {
            return null;
        }

        //后测题id列表
        List<String> similarDocIds = new ArrayList<>();
        //Map<原错题ID或者知识点ID（纸质拍照作业没有题id）, DiagnoseCourseResp>
        Map<String, MathDiagnosisPkg> diagnoseCourseRespMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(studentMathHomeworkDiagnosisPkg.getSyncDiagnosisPkgList())) {
            for (MathDiagnosisPkg mathDiagnosisPkg : studentMathHomeworkDiagnosisPkg.getSyncDiagnosisPkgList()) {
                mathDiagnosisPkg.setDiagnosisSource(NewHomeworkApp.DiagnosisSource.SyncDiagnosis);
                diagnoseCourseRespMap.put(mathDiagnosisPkg.getqId(), mathDiagnosisPkg);
                similarDocIds.addAll(mathDiagnosisPkg.getSimilarDocIds());
            }
        }
        if (CollectionUtils.isNotEmpty(studentMathHomeworkDiagnosisPkg.getCalcDiagnosisPkgList())) {
            for (MathDiagnosisPkg mathDiagnosisPkg : studentMathHomeworkDiagnosisPkg.getCalcDiagnosisPkgList()) {
                mathDiagnosisPkg.setDiagnosisSource(NewHomeworkApp.DiagnosisSource.CalcDiagnosis);
                diagnoseCourseRespMap.put(mathDiagnosisPkg.getqId(), mathDiagnosisPkg);
                similarDocIds.addAll(mathDiagnosisPkg.getSimilarDocIds());
            }
        }
        if (CollectionUtils.isNotEmpty(studentMathHomeworkDiagnosisPkg.getOcrDiagnosisPkgList())) {
            for (MathDiagnosisPkg mathDiagnosisPkg : studentMathHomeworkDiagnosisPkg.getOcrDiagnosisPkgList()) {
                mathDiagnosisPkg.setDiagnosisSource(NewHomeworkApp.DiagnosisSource.OcrDiagnosis);
                diagnoseCourseRespMap.put(mathDiagnosisPkg.getKpId(), mathDiagnosisPkg);
                similarDocIds.addAll(mathDiagnosisPkg.getSimilarDocIds());
            }
        }

        Map<String, NewQuestion> stringNewQuestionMap = questionLoaderClient.loadLatestQuestionByDocIds(similarDocIds);

        //诊断课程集(巩固课程&发音矫正)，推送内容包含诊断课程都是订正原题。纸质拍照特殊：没有推送课程直接忽略因为没有原题ID
        List<QuestionItem> interventionsQuestionItems = new ArrayList<>();
        //非诊断课程(订正错题),推送内容没有诊断课程都是订正原题。
        List<QuestionItem> correctionsQuestionItems = new ArrayList<>();
        for (NewHomeworkProcessResult newHomeworkProcessResult : processResultList) {
            //纸质拍照没有题id，订正内容是根据诊断公式对应的知识点推送订正课程以及后测题
            if (ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.equals(newHomeworkProcessResult.getObjectiveConfigType())) {
                if (newHomeworkProcessResult.getOcrMentalImageDetail() != null && newHomeworkProcessResult.getOcrMentalImageDetail().getOmads() != null && newHomeworkProcessResult.getOcrMentalImageDetail().getOmads().getItemPoints() != null) {
                    for (OcrMentalImageDetail.ItemPoint point : newHomeworkProcessResult.getOcrMentalImageDetail().getOmads().getItemPoints()) {
                        for (OcrMentalImageDetail.Point p : point.getPoints()) {
                            MathDiagnosisPkg mathDiagnosisPkg = diagnoseCourseRespMap.get(p.getPointId());
                            if (mathDiagnosisPkg == null) {
                                continue;
                            }
                            for (String similarDocId : mathDiagnosisPkg.getSimilarDocIds()) {
                                QuestionItem item = new QuestionItem();
                                item.setKpId(p.getPointId());
                                item.setErrorCause(p.getErrorCause());
                                item.setBookId(newHomeworkProcessResult.getBookId());
                                item.setUnitId(newHomeworkProcessResult.getUnitId());
                                item.setDiagnosisSource(mathDiagnosisPkg.getDiagnosisSource());
                                NewQuestion newQuestion = stringNewQuestionMap.get(similarDocId);
                                if (StringUtils.isNotBlank(mathDiagnosisPkg.getCourseId()) && newQuestion != null) {
                                    item.setDuration(newQuestion.getSeconds());
                                    item.setSimilarQuestionId(newQuestion.getId());
                                    item.setCourseId(mathDiagnosisPkg.getCourseId());
                                    item.setCourseOrder(mathDiagnosisPkg.getCourseOrder());
                                    interventionsQuestionItems.add(item);
                                }
                            }
                        }
                    }
                }
            } else {
                //重点讲练测&计算讲练测&口算速算&口语讲练测
                String sourceQuestionId = newHomeworkProcessResult.getQuestionId();
                MathDiagnosisPkg mathDiagnosisPkg = diagnoseCourseRespMap.get(sourceQuestionId);
                if (mathDiagnosisPkg == null) {
                    continue;
                }
                for (String similarDocId : mathDiagnosisPkg.getSimilarDocIds()) {
                    QuestionItem item = new QuestionItem();
                    item.setQuestionId(sourceQuestionId);
                    item.setBookId(newHomeworkProcessResult.getBookId());
                    item.setUnitId(newHomeworkProcessResult.getUnitId());
                    item.setDiagnosisSource(mathDiagnosisPkg.getDiagnosisSource());
                    NewQuestion newQuestion = stringNewQuestionMap.get(similarDocId);
                    if (newQuestion != null) {
                        item.setDuration(newQuestion.getSeconds());
                        item.setSimilarQuestionId(newQuestion.getId());
                        if (StringUtils.isNotBlank(mathDiagnosisPkg.getCourseId())) {
                            item.setCourseId(mathDiagnosisPkg.getCourseId());
                            item.setErrorCause(mathDiagnosisPkg.getErrorCause());
                            item.setCourseOrder(mathDiagnosisPkg.getCourseOrder());
                            item.setExperimentId(mathDiagnosisPkg.getExperimentId());
                            item.setExperimentGroupId(mathDiagnosisPkg.getExperimentGroupId());
                            item.setQuestionBoxId(questionBoxIdMap.get(sourceQuestionId));
                            interventionsQuestionItems.add(item);
                        } else {
                            correctionsQuestionItems.add(item);
                        }
                    }
                }
            }
        }
        Map<String, List<QuestionItem>> objectiveConfigTypeQuestionMap = new LinkedHashMap<>();
        objectiveConfigTypeQuestionMap.put(ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS.name(), interventionsQuestionItems);
        objectiveConfigTypeQuestionMap.put(ObjectiveConfigType.DIAGNOSTIC_CORRECTIONS.name(), correctionsQuestionItems);
        return buildHwSource(homeworkResult.getClazzGroupId(), homeworkResult.getUserId(), homeworkResult.getHomeworkId(), objectiveConfigTypeQuestionMap, homeworkResult.getSubject());
    }

    /**
     * 英语订正
     *
     * @param hwResult          作业结果
     * @param processResultList 作业完成后，需要订正的作业类型的错题信息List
     * @param questionBoxIdMap
     */
    private HwSource selfStudyEnglishCorrectHomework(NewHomeworkResult hwResult, List<NewHomeworkProcessResult> processResultList, Map<String, String> questionBoxIdMap) {
        StudentEnglishHomeworkDiagnosisPkg diagnosisPkg = getStudentEnglishHomeworkDiagnosisPkg(hwResult, processResultList);
        if (diagnosisPkg == null) {
            return null;
        }
        Map<String, List<QuestionItem>> objectiveConfigTypeQuestionMap = new LinkedHashMap<>();

        //重点讲练测
        List<EnglishSyncDiagnosisPkg> syncDiagnosisPkgList = diagnosisPkg.getSyncDiagnosisPkgList();
        if (CollectionUtils.isNotEmpty(syncDiagnosisPkgList)) {
            //Map<原错题ID, EnglishSyncDiagnosisPkg>
            Map<String, EnglishSyncDiagnosisPkg> syncCourseRespMap = syncDiagnosisPkgList.stream()
                    .filter(d -> CollectionUtils.isNotEmpty(d.getSimilarDocIds()) && StringUtils.isNotBlank(d.getQId()))
                    .collect(Collectors.toMap(EnglishSyncDiagnosisPkg::getQId, Function.identity()));

            List<String> similarDocIds = Lists.newLinkedList();
            syncDiagnosisPkgList.forEach(d -> similarDocIds.addAll(d.getSimilarDocIds()));
            Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadLatestQuestionByDocIds(similarDocIds);

            List<QuestionItem> interventionsQuestionItems = new ArrayList<>();
            List<QuestionItem> correctionsQuestionItems = new ArrayList<>();
            List<NewHomeworkProcessResult> syncProcessResult = processResultList.stream().filter(p -> !p.getObjectiveConfigType().equals(ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING)).collect(Collectors.toList());
            for (NewHomeworkProcessResult processResult : syncProcessResult) {
                EnglishSyncDiagnosisPkg syncDiagnosisPkg = syncCourseRespMap.get(processResult.getQuestionId());
                for (String similarDocId : syncDiagnosisPkg.getSimilarDocIds()) {
                    NewQuestion newQuestion = newQuestionMap.get(similarDocId);
                    if (newQuestion == null) {
                        continue;
                    }
                    QuestionItem item = new QuestionItem();
                    item.setQuestionId(processResult.getQuestionId());
                    item.setBookId(processResult.getBookId());
                    item.setUnitId(processResult.getUnitId());
                    item.setDuration(newQuestion.getSeconds());
                    item.setSimilarQuestionId(newQuestion.getId());
                    if (StringUtils.isNotBlank(syncDiagnosisPkg.getCourseId())) {
                        item.setCourseId(syncDiagnosisPkg.getCourseId());
                        item.setErrorCause(syncDiagnosisPkg.getErrorCause());
                        item.setCourseOrder(syncDiagnosisPkg.getCourseOrder());
                        item.setExperimentId(syncDiagnosisPkg.getExperimentId());
                        item.setExperimentGroupId(syncDiagnosisPkg.getExperimentGroupId());
                        item.setQuestionBoxId(questionBoxIdMap.get(processResult.getQuestionId()));
                        interventionsQuestionItems.add(item);
                    } else {
                        correctionsQuestionItems.add(item);
                    }
                }
            }
            objectiveConfigTypeQuestionMap.put(ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS.name(), interventionsQuestionItems);
            objectiveConfigTypeQuestionMap.put(ObjectiveConfigType.DIAGNOSTIC_CORRECTIONS.name(), correctionsQuestionItems);
        }

        //口语讲练测-发音矫正
        List<EnglishOralConfusedPkg> oralConfusedPkgList = diagnosisPkg.getOralConfusedPkgList();
        oralConfusedPkgList = oralConfusedPkgList.stream().filter(o -> CollectionUtils.isNotEmpty(o.getVideoIds()) && o.getQuestionId() != null).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(oralConfusedPkgList)) {
            Map<String, EnglishOralConfusedPkg> courseMap = oralConfusedPkgList.stream().collect(Collectors.toMap(EnglishOralConfusedPkg::getQuestionId, Function.identity()));
            List<QuestionItem> oralQuestionItems = new ArrayList<>();
            for (NewHomeworkProcessResult processResult : processResultList) {
                String sourceQuestionId = processResult.getQuestionId();
                QuestionItem item = new QuestionItem();
                item.setQuestionId(sourceQuestionId);
                item.setBookId(processResult.getBookId());
                item.setUnitId(processResult.getUnitId());
                item.setDuration(0);
                EnglishOralConfusedPkg englishOralConfusedPkg = courseMap.get(processResult.getQuestionId());
                if (englishOralConfusedPkg != null) {
                    for (String courseId : englishOralConfusedPkg.getVideoIds()) {
                        item.setCourseId(courseId);
                        item.setCourseOrder(0);//没有课程顺序
                        item.setQuestionBoxId(questionBoxIdMap.get(sourceQuestionId));
                        oralQuestionItems.add(item);
                    }
                }
            }
            objectiveConfigTypeQuestionMap.put(ObjectiveConfigType.ORAL_INTERVENTIONS.name(), oralQuestionItems);
        }

        return buildHwSource(hwResult.getClazzGroupId(), hwResult.getUserId(), hwResult.getHomeworkId(), objectiveConfigTypeQuestionMap, hwResult.getSubject());
    }

    private StudentEnglishHomeworkDiagnosisPkg getStudentEnglishHomeworkDiagnosisPkg(NewHomeworkResult homeworkResult, List<NewHomeworkProcessResult> processResultList) {
        Set<String> oralQuestionIds = processResultList.stream()
                .filter(p -> p.getObjectiveConfigType().equals(ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING))
                .map(NewHomeworkProcessResult::getQuestionId).collect(Collectors.toSet());

        NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(homeworkResult.getHomeworkId());
        Map<ObjectiveConfigType, Map<String, String>> configTypeQuestionUnitIdMap = newHomeworkBook.processUnitIdMap();
        Map<String, NewQuestion> oralQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(oralQuestionIds);
        List<EnglishOralQuestion> oralHomework = Lists.newLinkedList();
        List<EnglishSyncQuestion> syncHomework = Lists.newLinkedList();
        for (NewHomeworkProcessResult process : processResultList) {
            ObjectiveConfigType objectiveConfigType = process.getObjectiveConfigType();
            Map<String, String> questionUnitIdMap = configTypeQuestionUnitIdMap.get(objectiveConfigType);
            if (!ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING.equals(objectiveConfigType)) {
                EnglishSyncQuestion syncQuestion = new EnglishSyncQuestion();
                syncQuestion.setQuestionId(process.getQuestionId());
                syncQuestion.setUserAnswers(process.getUserAnswers());
                if (MapUtils.isNotEmpty(questionUnitIdMap)) {
                    syncQuestion.setBkcId(questionUnitIdMap.get(process.getQuestionId()));
                } else {
                    LogCollector.info("backend-general", MapUtils.map(
                            "env", RuntimeMode.getCurrentStage(),
                            "studentId", homeworkResult.getUserId(),
                            "subject", Subject.ENGLISH,
                            "homeworkId", homeworkResult.getHomeworkId(),
                            "newHomeworkBook", JsonUtils.toJson(newHomeworkBook),
                            "configTypeQuestionUnitIdMap", JsonUtils.toJson(configTypeQuestionUnitIdMap),
                            "objectiveConfigType", objectiveConfigType,
                            "processResultId", process.getId(),
                            "questionId", process.getQuestionId(),
                            "op", "SelfStudyEnglishCorrect_NewHomeworkBook"
                    ));
                }
                syncHomework.add(syncQuestion);
            } else if (CollectionUtils.isNotEmpty(process.getOralDetails()) && isContainsOralLabel(oralQuestionMap.get(process.getQuestionId()))) {
                List<BaseHomeworkProcessResult.OralDetail> oralDetails = process.getOralDetails().get(0);
                if (CollectionUtils.isNotEmpty(oralDetails)) {
                    BaseHomeworkProcessResult.OralDetail oralDetail = oralDetails.get(0);
                    OralDiagnoseResult oralDiagnose = oralDetail.getOralDiagnoseResult();

                    //易混音, sample和userText相同and标准分大于3分则不去命中课程
                    if (oralDiagnose == null || "PAIRS".equals(oralDiagnose.getDiagnoseType()) && oralDiagnose.getSample().equals(oralDiagnose.getUserText()) && oralDetail.getStandardScore() > 3) {
                        continue;
                    }
                    EnglishOralConfusedInfo oralConfusedInfo = new EnglishOralConfusedInfo();
                    oralConfusedInfo.setConfusedTag(oralDiagnose.getDiagnoseTag());
                    oralConfusedInfo.setConfusedType(oralDiagnose.getDiagnoseType());
                    oralConfusedInfo.setScore(oralDiagnose.getScore());

                    EnglishOralQuestion oralQuestion = new EnglishOralQuestion();
                    oralQuestion.setQuestionId(process.getQuestionId());
                    oralQuestion.setTotalScore(SafeConverter.toDouble(oralDetail.getStandardScore()));
                    oralQuestion.setConfusedInfos(Collections.singletonList(oralConfusedInfo));
                    oralHomework.add(oralQuestion);
                }
            }
        }
        try {
            return selfStudyRecomLoaderClient.getCuotizhenduanLoader().loadStudentEnglishHomeworkDiagnosisRecommend(homeworkResult.getUserId(), oralHomework, syncHomework);
        } catch (Exception e) {
            logger.error("SelfStudyEnglishCorrect method:CuotizhenduanLoader.loadStudentEnglishHomeworkDiagnosisRecommend, userId:{} Error:{}", homeworkResult.getUserId(), e.getMessage());
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", homeworkResult.getUserId(),
                    "mod1", Subject.ENGLISH,
                    "mod2", homeworkResult.getHomeworkId(),
                    "mod3", oralHomework,
                    "mod4", syncHomework,
                    "op", "SelfStudyEnglishCorrect"
            ));
            return null;
        }
    }

    /**
     * 判断题目是否包含易混音标签
     *
     * @param newQuestion
     * @return
     */
    private boolean isContainsOralLabel(NewQuestion newQuestion) {
        if (newQuestion == null) {
            return false;
        }
        try {
            return newQuestion.getContent()
                    .getSubContents()
                    .stream().anyMatch(s -> MapUtils.isNotEmpty(s.getExtras()) && StringUtils.isNotBlank(s.getExtras().get("correct_phoneme")));
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 获取允许生成错题订正的作业形式的processId
     *
     * @return processIds
     */
    private List<String> getProcessIdsByObjectiveConfigType(NewHomeworkResult homeworkResult) {
        List<String> processIdList = new ArrayList<>();
        if (homeworkResult.practices != null) {
            for (ObjectiveConfigType configType : homeworkResult.practices.keySet()) {
                if (GenerateSelfStudyHomeworkConfigTypes.contains(configType)) {
                    NewHomeworkResultAnswer answer = homeworkResult.getPractices().get(configType);
                    if (answer != null) {
                        if (ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.equals(configType)) {
                            processIdList.addAll(answer.getOcrMentalAnswers());
                        } else {
                            if (answer.processScore(configType) < 100) {
                                List<String> processIds = homeworkResult.findHomeworkProcessIdsByObjectiveConfigType(configType);
                                processIdList.addAll(processIds);
                            }
                        }
                    }
                }

            }
        }
        return processIdList;
    }

    /**
     * 根据题目ID对原作业结果去重
     *
     * @param processResultList
     * @return
     */
    private List<NewHomeworkProcessResult> distinctProcessResult(List<NewHomeworkProcessResult> processResultList) {
        List<NewHomeworkProcessResult> processResultSet = Lists.newArrayList();
        Set<String> questionSet = new HashSet<>();
        for (NewHomeworkProcessResult processResult : processResultList) {
            String questionId = processResult.getQuestionId();
            //纸质拍照没有实际题id所以不用处理去重
            if (!questionSet.contains(questionId) || ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.equals(processResult.getObjectiveConfigType())) {
                processResultSet.add(processResult);
                questionSet.add(questionId);
            }
        }
        return processResultSet;
    }

    private StudentMathHomeworkDiagnosisPkg getStudentMathHomeworkDiagnosisPkg(Long studentId, List<NewHomeworkProcessResult> processResultList, Long clazzGroupId, String sourceHomeworkId) {
        List<DiagnoseCourseReq.SrcDocId> srcDocIds = new ArrayList<>();
        List<DiagnoseCourseReq.SrcDocId> calcDocIds = new ArrayList<>();
        List<DiagnoseCourseReq.OcrKpId> ocrKpIds = new ArrayList<>();
        Set<String> allIds = new HashSet<>();
        for (NewHomeworkProcessResult processResult : processResultList) {
            ObjectiveConfigType objectiveConfigType = processResult.getObjectiveConfigType();
            if (ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.equals(objectiveConfigType)) {
                if (processResult.getOcrMentalImageDetail() != null && processResult.getOcrMentalImageDetail().getOmads() != null && processResult.getOcrMentalImageDetail().getOmads().getItemPoints() != null) {
                    for (OcrMentalImageDetail.ItemPoint point : processResult.getOcrMentalImageDetail().getOmads().getItemPoints()) {
                        for (OcrMentalImageDetail.Point p : point.getPoints()) {
                            if (allIds.contains(p.getPointId())) continue;
                            ocrKpIds.add(new DiagnoseCourseReq.OcrKpId(p.getPointId()));
                            allIds.add(p.getPointId());
                        }
                    }
                }
            } else if (ObjectiveConfigType.CALC_INTELLIGENT_TEACHING.equals(objectiveConfigType) || ObjectiveConfigType.MENTAL_ARITHMETIC.equals(objectiveConfigType)) {
                if (allIds.contains(processResult.getQuestionId())) continue;
                calcDocIds.add(new DiagnoseCourseReq.SrcDocId(processResult.getUserAnswers(), processResult.getQuestionId(), StringUtils.isBlank(processResult.getSectionId()) ? "000" : processResult.getSectionId()));
                allIds.add(processResult.getQuestionId());
            } else {
                if (allIds.contains(processResult.getQuestionId())) continue;
                srcDocIds.add(new DiagnoseCourseReq.SrcDocId(processResult.getUserAnswers(), processResult.getQuestionId(), StringUtils.isBlank(processResult.getSectionId()) ? "000" : processResult.getSectionId()));
                allIds.add(processResult.getQuestionId());
            }
        }

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null || studentDetail.getClazz() == null) {
            logger.error("SelfStudyHomeworkGenerateServiceImpl.getDiagnoseCourseResp studentDetail data exception, studentId:{}", studentId);
            return null;
        }
        int grade = studentDetail.getClazz().getClazzLevel().getLevel();
        List<String> regions = Arrays.asList(String.valueOf(studentDetail.getRootRegionCode()), String.valueOf(studentDetail.getCityCode()), String.valueOf(studentDetail.getStudentSchoolRegionCode()));
        DiagnoseCourseReq courseReq = new DiagnoseCourseReq(studentId, srcDocIds, calcDocIds, ocrKpIds, grade, regions);
        String requestUrl = RuntimeMode.current().le(Mode.TEST) ? SELF_STUDY_ZHENDUAN_TEST_URL : SELF_STUDY_ZHENDUAN_PRODUCT_URL;
        if(RuntimeMode.current().equals(Mode.STAGING)){
            requestUrl = SELF_STUDY_ZHENDUAN_STAGING_URL;
        }
        String diagnoseCourseResp = null;
        try {
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                    .post(requestUrl)
                    .json(courseReq)
                    .contentType("application/json").socketTimeout(3 * 1000)
                    .execute();
            if (response == null || response.getStatusCode() != 200) {
                logger.error("SelfStudyMathCorrect invoke method: {} error, req:{} Error:{}", requestUrl, JsonUtils.toJson(courseReq), response);
                diagnoseCourseResp = selfStudyRecomLoaderClient.getCuotizhenduanLoader().loadZhenduanRecommendation(JsonUtils.toJson(courseReq));
            } else {
                diagnoseCourseResp = response.getResponseString();
            }

            Map<String, Object> responseMap = JsonUtils.fromJson(diagnoseCourseResp);
            if (MapUtils.isEmpty(responseMap) || !SafeConverter.toBoolean(responseMap.get("success"))) {
                logger.warn("SelfStudyHomework, BD return null. param:{}", JsonUtils.toJson(courseReq));
                kibana(studentId, clazzGroupId, sourceHomeworkId, courseReq, diagnoseCourseResp);
                return null;
            }
            StudentMathHomeworkDiagnosisPkg diagnosisPkg = new StudentMathHomeworkDiagnosisPkg();
            if (responseMap.get("interventionList") != null) {
                diagnosisPkg.setSyncDiagnosisPkgList(JsonUtils.fromJsonToList(JsonUtils.toJson(responseMap.get("interventionList")), MathDiagnosisPkg.class));
            }
            if (responseMap.get("calcList") != null) {
                diagnosisPkg.setCalcDiagnosisPkgList(JsonUtils.fromJsonToList(JsonUtils.toJson(responseMap.get("calcList")), MathDiagnosisPkg.class));
            }
            if (responseMap.get("ocrList") != null) {
                diagnosisPkg.setOcrDiagnosisPkgList(JsonUtils.fromJsonToList(JsonUtils.toJson(responseMap.get("ocrList")), MathDiagnosisPkg.class));
            }
            return diagnosisPkg;
        } catch (Exception e) {
            logger.error("SelfStudyMathCorrect method:CuotizhenduanLoader.loadZhenduanRecommendation, req:{} Error:{}", JsonUtils.toJson(courseReq), e.getMessage());
            kibana(studentId, clazzGroupId, sourceHomeworkId, courseReq, diagnoseCourseResp);
            return null;
        }
    }

    private void kibana(Long studentId, Long clazzGroupId, String sourceHomeworkId, DiagnoseCourseReq courseReq, String diagnoseCourseResp) {
        LogCollector.info("backend-general", MapUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "usertoken", studentId,
                "mod1", Subject.MATH,
                "mod2", JsonUtils.toJson(courseReq),
                "mod3", diagnoseCourseResp,
                "mod4", clazzGroupId,
                "mod5", sourceHomeworkId,
                "op", "SelfStudyMathCorrect"));
    }


    /**
     * 第一版单词巩固任务调用方，已废弃
     * 这个方法调用一定要淡定
     *
     * @param clazzGroupId 班组id
     * @param bookToKpMap  Map<bookId, Map<unitId, List<kpid>>>
     */
    public void selfStudyWordsIncreaseHomework(Long clazzGroupId, Map<String, Map<String, List<String>>> bookToKpMap) {
        List<Long> studentIds = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByGroupId(clazzGroupId)
                .stream()
                .map(GroupStudentTuple::getStudentId)
                .collect(Collectors.toList());
        List<Long> needJpushStudents = new ArrayList<>();
        for (Long studentId : studentIds) {
            // 生成作业
            needJpushStudents = generateWordsIncreaseHomework(clazzGroupId, studentId, bookToKpMap);
        }
        // 学生端jPush消息
        sendJpush(clazzGroupId, needJpushStudents);
    }

    /**
     * 按学生把消息发进kafka，下面那个方法生成作业
     */
    public void sendToKafkaSelfStudyWordsIncreaseHomework(Long clazzGroupId, Map<String, Map<String, List<String>>> bookToKpMap) {
        List<Long> studentIds = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByGroupId(clazzGroupId)
                .stream()
                .map(GroupStudentTuple::getStudentId)
                .collect(Collectors.toList());
        for (Long studentId : studentIds) {
            // 发去kafka了
            // 测试环境就直接调用吧，不要走kafka了
            if (RuntimeMode.isTest() || RuntimeMode.isDevelopment()) {
                generateWordsIncreaseHomework(clazzGroupId, studentId, bookToKpMap);
            }
            newHomeworkQueueService.saveSelfStudyWordsIncreaseHomework(clazzGroupId, studentId, bookToKpMap);
        }
    }

    /**
     * 第二版单词巩固任务，和上面的配套使用。
     * 上面的发kafka，这个消费。
     *
     * @param clazzGroupId 班组id
     * @param studentId    学生id
     * @param bookToKpMap  Map<bookId, Map<unitId, List<kpid>>>
     * @return 不知成功的学生ids
     */
    public List<Long> generateWordsIncreaseHomework(Long clazzGroupId, Long studentId, Map<String, Map<String, List<String>>> bookToKpMap) {
        List<Long> needJpushStudents = new ArrayList<>();
        List<QuestionItem> questionItems = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<String>>> entry : bookToKpMap.entrySet()) {
            String bookId = entry.getKey();
            Map<String, List<String>> unitToKpMap = entry.getValue();
//            Map<String, List<String>> kpToQuestionMap = utopiaPsrService.getSelfStudyRecomByKp(studentId, bookId, unitToKpMap, 2);
            Map<String, List<String>> kpToQuestionMap = selfStudyRecomLoaderClient.getUtopiaPsrLoader().getSelfStudyRecomByKp(studentId, bookId, unitToKpMap, 2);
            // 以上是预备数据
            if (MapUtils.isNotEmpty(kpToQuestionMap)) {
                List<String> questionIds = kpToQuestionMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
//                if (RuntimeMode.isTest() || RuntimeMode.isDevelopment()) {
//                    questionIds = Arrays.asList("Q_10308692577241-1", "Q_10308979921679-1", "Q_10306236377235-2", "Q_10308872003364-1", "Q_10308057574402-1", "Q_10308057575000-1");
//                }
                if (CollectionUtils.isNotEmpty(questionIds)) {
                    // 处理题量过大的情况，随机20道题
                    if (questionIds.size() > 20) {
                        Collections.shuffle(questionIds);
                        questionIds = new ArrayList<>(questionIds.subList(0, 20));
                    }
                    List<NewQuestion> questions = questionLoaderClient.loadQuestionsIncludeDisabledAsList(questionIds);
                    for (NewQuestion question : questions) {
                        QuestionItem item = new QuestionItem();
                        item.setBookId(bookId);
                        item.setQuestionId(question.getId());
                        item.setDuration(question.getSeconds());
                        item.setSimilarQuestionId(question.getId());
                        questionItems.add(item);
                    }
                } else {
                    LogCollector.info("backend-general", MapUtils.map(
                            "env", RuntimeMode.getCurrentStage(),
                            "usertoken", studentId,
                            "mod1", Subject.ENGLISH,
                            "mod2", JsonUtils.toJson(bookToKpMap),
                            "mod3", JsonUtils.toJson(kpToQuestionMap),
                            "mod4", clazzGroupId,
                            "mod5", bookId,
                            "mod6", "WordsIncrease",
                            "op", "SelfStudyMathMental"
                    ));
                }
            }
        }
        Map<String, List<QuestionItem>> objectiveConfigTypeQuestionMap = new HashMap<>();
        objectiveConfigTypeQuestionMap.put(ObjectiveConfigType.EXAM.name(), questionItems);
        // 以上是生成学生的作业题目数据
        HwSource hwSource = buildHwSource(clazzGroupId, studentId, "", objectiveConfigTypeQuestionMap, Subject.ENGLISH);

        Date d = new Date();
        WeekRange week = WeekRange.newInstance(d.getTime());
        String startDate = FastDateFormat.getInstance("MM月dd日").format(DateUtils.addDays(week.getStartDate(), -2));
        String endDate = FastDateFormat.getInstance("MM月dd日").format(DateUtils.addDays(week.getEndDate(), -2));
        String des = "本周重点词汇巩固（" + startDate + "~" + endDate + ")";
        hwSource.setDes(des);

        // noinspection unchecked
        HomeworkSource homeworkSource = HomeworkSource.newInstance(JsonUtils.fromJson(JsonUtils.toJson(hwSource), Map.class));
        // 有点挫，有点挫，有点挫
        Teacher teacher = new Teacher();
        teacher.setId(studentId);
        teacher.setSubject(Subject.ENGLISH);
        MapMessage mapMessage = newHomeworkService.assignHomework(teacher, homeworkSource, HomeworkSourceType.App, NewHomeworkType.selfstudy, HomeworkTag.WordsIncrease);
        if (mapMessage.isSuccess()) {
            // noinspection unchecked
            List<String> homeworkIds = (List<String>) mapMessage.get("homeworkIds");
            if (CollectionUtils.isEmpty(homeworkIds)) {
                logger.warn("SelfStudyHomework, WordsIncrease homework assign error, no homework id return.");
                return Collections.emptyList();
            }
            String homeworkId = homeworkIds.get(0);
            if (StringUtils.isNotBlank(homeworkId)) {
                needJpushStudents.add(studentId);
            }
        }
        return CollectionUtils.toLinkedList(needJpushStudents);
    }

    private void sendJpush(Long clazzGroupId, List<Long> needJpushStudents) {
        if (CollectionUtils.isNotEmpty(needJpushStudents)) {
            long sendTimeEpochMilli = System.currentTimeMillis() + (5 * 60 * 60 * 1000); // 4点跑任务，9点开发，延迟5个小时
            String content = "本周学习的重点英语词汇新鲜出炉啦，快点来巩固。完成任务得学分哦！";

            String link = "/view/mobile/student/wonderland/task?active=1&from=fairyland&refer=300007";
            Map<String, Object> extInfo = new HashMap<>();
            extInfo.put("link", link);
            extInfo.put("t", "h5");
            extInfo.put("key", "j");
            if (RuntimeMode.isTest()) {
                appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.STUDENT, needJpushStudents, extInfo);
            }

            List<Long> stagingGroup = Arrays.asList(923324L, 923537L, 1758685L, 1758686L);
            if ((RuntimeMode.isStaging() || RuntimeMode.isProduction()) && stagingGroup.contains(clazzGroupId)) {
                appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.STUDENT, needJpushStudents, extInfo);
            }
            appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.STUDENT, needJpushStudents, extInfo, sendTimeEpochMilli);
        }
    }

    private HwSource buildHwSource(Long groupId, Long studentId, String sourceHomeworkId, Map<String, List<QuestionItem>> objectiveConfigTypeQuestionMap, Subject subject) {
        int duration = 0;
        Map<String, Map<String, List<HwQuestion>>> practiceMap = new LinkedHashMap<>();
        Map<String, List<HwBook>> bookMap = new LinkedHashMap<>();
        for (Map.Entry<String, List<QuestionItem>> questionItemEntry : objectiveConfigTypeQuestionMap.entrySet()) {
            List<HwQuestion> questions = new LinkedList<>();
            Map<String, HwBook> hwBookMap = new LinkedHashMap<>();
            List<QuestionItem> questionItems = questionItemEntry.getValue();
            long courseCount = questionItems.stream().filter(q -> q.getCourseId() != null).count();
            duration += courseCount * COURSE_DEFAULT_DURATION;
            for (QuestionItem questionItem : questionItems) {
                HwQuestion hwQuestion = new HwQuestion();
                if (StringUtils.isNoneBlank(questionItem.getQuestionId())) {
                    hwQuestion.setQuestionId(questionItem.getQuestionId());
                }
                if (StringUtils.isNoneBlank(questionItem.getKpId())) {
                    hwQuestion.setKpId(questionItem.getKpId());
                }

                if (StringUtils.isNotBlank(questionItem.getSimilarQuestionId())) {
                    // 处理课本单元信息
                    String unitId = questionItem.getUnitId();
                    HwBook hwBook = hwBookMap.get(unitId);
                    if (hwBook == null) {
                        hwBook = new HwBook(questionItem.getBookId(), unitId);
                        hwBookMap.put(unitId, hwBook);
                    }
                    hwBook.getIncludeQuestions().add(questionItem.getSimilarQuestionId());
                    hwQuestion.setSimilarQuestionId(questionItem.getSimilarQuestionId());
                }
                duration += questionItem.getDuration();
                hwQuestion.setSubmitWay(Collections.singletonList(Collections.singletonList(questionItem.isSubjective ? 1 : 0)));
                hwQuestion.setCourseId(questionItem.getCourseId());
                hwQuestion.setSeconds(questionItem.getDuration());
                hwQuestion.setCourseOrder(questionItem.getCourseOrder());
                hwQuestion.setErrorCause(questionItem.getErrorCause());
                hwQuestion.setExperimentGroupId(questionItem.getExperimentGroupId());
                hwQuestion.setExperimentId(questionItem.getExperimentId());
                hwQuestion.setQuestionBoxId(questionItem.getQuestionBoxId());
                hwQuestion.setDiagnosisSource(questionItem.getDiagnosisSource());
                questions.add(hwQuestion);
            }

            Map<String, List<HwQuestion>> examMap = new LinkedHashMap<>();
            examMap.put("questions", questions);
            practiceMap.put(questionItemEntry.getKey(), examMap);
            bookMap.put(questionItemEntry.getKey(), Lists.newLinkedList(hwBookMap.values()));
        }
        HwSource hwSource = new HwSource();
        hwSource.setHomeworkType(NewHomeworkType.selfstudy);
        hwSource.setSourceHomeworkId(sourceHomeworkId);
        hwSource.setStudentId(studentId);
        hwSource.setDes("");
        hwSource.setSubject(subject);
        hwSource.setRemark("");
        hwSource.setClazzGroupId(groupId);
        hwSource.setDuration(duration);
        hwSource.setPractices(practiceMap);
        hwSource.setBooks(bookMap);
        return hwSource;
    }

    @Getter
    @Setter
    private static class QuestionItem implements Serializable {
        private static final long serialVersionUID = 5086305474277175394L;
        private String questionId;              //原题ID
        private String kpId;                    //知识点ID  纸质拍照张弦诊断返回答错对应的知识点
        private String similarQuestionId;       //后测题ID
        private String bookId;
        private String unitId;
        private Integer duration;
        private Boolean isSubjective = false;
        private String courseId;                //课程ID
        private String errorCause;              //错因
        private Integer courseOrder;            //课程顺序
        private String experimentId;            //实验ID
        private String experimentGroupId;       //实验组ID
        private String questionBoxId;           //原作业题包ID(讲练测特有)
        private NewHomeworkApp.DiagnosisSource diagnosisSource; //诊断来源
    }

    @Getter
    @Setter
    private static class HwQuestion implements Serializable {
        private static final long serialVersionUID = -8275585216408839179L;
        private String questionId;             //原题ID
        private String kpId;                   //原错误知识点ID 纸质拍照专属
        private Integer seconds;
        private List<List<Integer>> submitWay;
        private String courseId;             //课程ID
        private String errorCause;           //错因
        private Integer courseOrder;         //课程顺序
        private String similarQuestionId;    //后测题ID
        private String experimentId;            //实验ID
        private String experimentGroupId;       //实验组ID
        private String questionBoxId;           //原作业题包ID(讲练测特有)
        private NewHomeworkApp.DiagnosisSource diagnosisSource; //诊断来源
    }

    @Getter
    @Setter
    private static class HwBook implements Serializable {
        private static final long serialVersionUID = -3414018585493965300L;
        private String bookId;
        private String bookName;
        private String unitId;
        private String unitName;
        private String sectionId;
        private String sectionName;
        private List<String> includeQuestions = new LinkedList<>();

        HwBook(String bookId, String unitId) {
            this.bookId = bookId;
            this.unitId = unitId;
        }
    }

    @Getter
    @Setter
    private static class HwSource implements Serializable {
        private static final long serialVersionUID = -4759632728140797430L;
        private NewHomeworkType homeworkType;
        private String des;
        private String startTime;
        private String endTime;
        private Long clazzGroupId;
        private Long studentId;
        private String sourceHomeworkId;
        private Subject subject;
        private String remark;
        private Integer duration;
        private Map<String, Map<String, List<HwQuestion>>> practices;
        private Map<String, List<HwBook>> books;
        private String prize;
    }


    @Getter
    @Setter
    @AllArgsConstructor
    private static class DiagnoseCourseReq implements Serializable {
        private static final long serialVersionUID = -8084755060786378078L;
        private Long studentId;
        private List<SrcDocId> srcDocIds; //同步习题&重点讲练测
        private List<SrcDocId> calcDocIds; //计算讲练测&口算速算
        private List<OcrKpId> ocrKpIds; //纸质拍照
        private Integer grade;//年级
        private List<String> regions;//地区, list按照省市区顺序放

        @Getter
        @Setter
        @AllArgsConstructor
        private static class SrcDocId implements Serializable {
            private static final long serialVersionUID = -6184982647899370300L;
            private List<List<String>> userAnswer;
            private String questionId; //docId
            private String sectionId;
        }

        @Getter
        @Setter
        @AllArgsConstructor
        private static class OcrKpId implements Serializable {
            private static final long serialVersionUID = -6184982647899370300L;
            private String kpId; //只是点id
        }
    }


    @Getter
    @Setter
    private static class StudentMathHomeworkDiagnosisPkg implements Serializable {
        private static final long serialVersionUID = 7347556422436206042L;
        /*同步诊断包*/
        private List<MathDiagnosisPkg> syncDiagnosisPkgList;
        /*计算讲练&口算速算*/
        private List<MathDiagnosisPkg> calcDiagnosisPkgList;
        /*纸质拍照*/
        private List<MathDiagnosisPkg> ocrDiagnosisPkgList;
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "qId")
    private static class MathDiagnosisPkg implements Serializable {
        private static final long serialVersionUID = 2994962827895397446L;
        private String qId; //原题ID
        private String kpId; //知识点ID
        private String errorCause;  //错因
        private String courseId;    // 如果没有相关课程推荐，则返回null
        private Integer courseOrder; //课程顺序-- int，从0开始计数. 注:如果没有相关课程推荐，则返回null
        private List<String> similarDocIds; //后测题  如果未命中课程，返回不带版本号的原题docId（后来支持多道后测题）
        //作业内实验相关字段(命中实验不为空)
        private String experimentId;//实验ID
        private String experimentGroupId;//实验组ID
        private NewHomeworkApp.DiagnosisSource diagnosisSource; //诊断来源

        //lombok生成的get方法json格式化有问题
        public String getqId() {
            return qId;
        }
    }
}
