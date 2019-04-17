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

package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.core.helper.VoiceEngineTypeUtils;
import com.voxlearning.utopia.mapper.ScoreCircleQueueCommand;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.content.api.entity.*;
import com.voxlearning.utopia.service.content.client.PracticeServiceClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.*;
import com.voxlearning.utopia.service.newhomework.api.entity.*;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.HomeworkSelfStudyRef;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkReport;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkReportQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.BaseVoiceRecommend;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.DubbingRecommend;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.ImageTextRecommend;
import com.voxlearning.utopia.service.newhomework.api.entity.wordspractice.ImageTextRhymeHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.CategoryClazzHandlerContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.CategoryHandlerContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.NewReadReciteAppPart;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.AppNewHomeworkStudentDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.OcrNewHomeworkStudentDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.TypePartContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.rate.QuestionDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.pc.StudentPersonalInfo;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.reading.WordRecognitionAndReadingDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.readrecitewithscore.ParagraphDetailed;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.wordteach.ChineseCharacterCultureModuleClazzData;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.wordteach.ImageTextRhymeModuleClazzData;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.wordteach.StudentHomeworkData;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching.IntelligentTeachingReport;
import com.voxlearning.utopia.service.newhomework.api.mapper.wordteach.StudentImageTextRhymeDetailMapper;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkReportService;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.consumer.cache.NoticeShareReportToJztCacheManager;
import com.voxlearning.utopia.service.newhomework.consumer.cache.UrgeNewHomeworkUnCorrectCacheManager;
import com.voxlearning.utopia.service.newhomework.consumer.cache.UrgeNewHomeworkUnFinishCacheManager;
import com.voxlearning.utopia.service.newhomework.impl.dao.NewHomeworkStudyMasterDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.NewHomeworkSyllableDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.OfflineHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.PictureBookPlusDubbingDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.HomeworkSelfStudyRefDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkReportDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.queue.NewHomeworkParentQueueProducer;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.report.HomeworkReportProcessor;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.report.ImageTextRecommendProcessor;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.report.VoiceRecommendProcessor;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.typeresult.NewHomeworkTypeResultFactory;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.typeresult.NewHomeworkTypeResultProcessTemplate;
import com.voxlearning.utopia.service.newhomework.impl.support.ImageTextRhymeStarCalculator;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkQuestionFileHelper;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.newhomework.impl.template.*;
import com.voxlearning.utopia.service.newhomework.impl.template.internal.report.ProcessNewHomeworkAnswerDetailBasicAppTemplate;
import com.voxlearning.utopia.service.newhomework.impl.template.internal.report.ProcessNewHomeworkAnswerDetailCommonTemplate;
import com.voxlearning.utopia.service.newhomework.impl.template.internal.report.ProcessNewHomeworkAnswerDetailNewReadReciteTemplate;
import com.voxlearning.utopia.service.newhomework.impl.template.internal.report.ProcessNewHomeworkAnswerDetailWordTeachTemplate;
import com.voxlearning.utopia.service.newhomework.impl.template.report.factory.AppObjectiveConfigTypeProcessorFactory;
import com.voxlearning.utopia.service.newhomework.impl.template.report.template.AppObjectiveConfigTypeProcessorTemplate;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.api.entity.stone.data.StoneBufferedData;
import com.voxlearning.utopia.service.question.api.entity.stone.data.oralpractice.*;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.api.entity.OfflineHomeworkSignRecord;
import com.voxlearning.utopia.service.vendor.consumer.JxtLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.*;

@Named
@Service(interfaceClass = NewHomeworkReportService.class)
@ExposeService(interfaceClass = NewHomeworkReportService.class)
public class NewHomeworkReportServiceImpl extends NewHomeworkSpringBean implements NewHomeworkReportService {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @Inject
    private ProcessAppDetailByCategoryIdFactory processAppDetailByCategoryIdFactory;
    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    private ProcessNewHomeworkAnswerDetailFactory processNewHomeworkAnswerDetailFactory;
    @Inject
    private NewHomeworkTypeResultFactory newHomeworkTypeResultFactory;
    @Inject
    private ProcessNewHomeworkAnswerDetailNewReadReciteTemplate processNewHomeworkAnswerDetailNewReadReciteTemplate;
    @Inject
    private ProcessNewHomeworkAnswerDetailBasicAppTemplate processNewHomeworkAnswerDetailBasicAppTemplate;
    @Inject
    private ProcessNewHomeworkAnswerDetailCommonTemplate processNewHomeworkAnswerDetailCommonTemplate;
    @Inject
    private ProcessNewHomeworkAnswerDetailWordTeachTemplate processNewHomeworkAnswerDetailWordTeachTemplate;
    @Inject
    private HomeworkReportProcessor homeworkReportProcessor;
    @Inject
    private NewHomeworkReportForParentFactory newHomeworkReportForParentFactory;
    @Inject
    private FetchStudentSemesterReportFactory fetchStudentSemesterReportFactory;
    @Inject
    private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject
    private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject
    private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;
    @Inject
    private HomeworkSelfStudyRefDao homeworkSelfStudyRefDao;
    @Inject
    private SelfStudyHomeworkReportDao selfStudyHomeworkReportDao;
    @Inject
    private PracticeServiceClient practiceServiceClient;
    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;
    @Inject
    private AppObjectiveConfigTypeProcessorFactory appObjectiveConfigTypeProcessorFactory;
    @Inject
    private VoiceRecommendProcessor voiceRecommendProcessor;
    @Inject
    private NewHomeworkStudyMasterDao newHomeworkStudyMasterDao;
    @Inject
    private SelfStudyHomeworkDao selfStudyHomeworkDao;
    @Inject
    private PictureBookPlusDubbingDao pictureBookPlusDubbingDao;
    @Inject
    private NewHomeworkParentQueueProducer newHomeworkParentQueueProducer;
    @Inject
    private DiagnoseReportImpl diagnoseReportService;
    @Inject
    protected NewHomeworkSyllableDao newHomeworkSyllableDao;
    @Inject
    private IntelDiagnosisClient intelDiagnosisClient;
    @Inject
    private ImageTextRecommendProcessor imageTextRecommendProcessor;
    @Inject
    private ImageTextRhymeStarCalculator imageTextRhymeStarCalculator;
    @Inject
    private OfflineHomeworkDao offlineHomeworkDao;
    @Inject
    private JxtLoaderClient jxtLoaderClient;
    @Inject
    private VendorServiceClient vendorServiceClient;

    @Inject private RaikouSDK raikouSDK;

    @Override
    public List<DisplayStudentHomeWorkHistoryMapper> loadStudentNewHomeworkHistory(StudentDetail student, Date startDate, Date endDate) {
        return homeworkReportProcessor.loadStudentNewHomeworkHistory(student, startDate, endDate);
    }

    public Page<DisplayStudentHomeWorkHistoryMapper> loadStudentNewHomeworkHistory(StudentDetail student, Date startDate, Date endDate, Pageable pageable) {
        return homeworkReportProcessor.loadStudentNewHomeworkHistory(student, startDate, endDate, pageable);
    }

    @Override
    public MapMessage loadMentalArithmeticChart(String homeworkId, Long studentId) {
        NewHomework newHomework = newHomeworkLoader.loadNewHomework(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业无效").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
        }
        // 判断本次是否是有奖励作业
        Boolean mentalAward = false;
        List<NewHomeworkPracticeContent> practices = newHomework.getPractices();
        if (CollectionUtils.isNotEmpty(practices)) {
            for (NewHomeworkPracticeContent practiceContent : practices) {
                if (practiceContent.getType() != null && practiceContent.getType().equals(ObjectiveConfigType.MENTAL_ARITHMETIC)) {
                    mentalAward = SafeConverter.toBoolean(practiceContent.getMentalAward());
                    break;
                }
            }
        }

        Map<String, Object> chartMap = new LinkedHashMap<>();
        chartMap.put("mentalAward", mentalAward);

        List<Map<String, Object>> studentCharts = new ArrayList<>();
        NewHomeworkStudyMaster studyMaster = newHomeworkStudyMasterDao.load(homeworkId);
        if (studyMaster != null) {
            List<Long> studentIds = studyMaster.getCalculationList();
            Map<Long, Student> studentDetails = studentLoaderClient.loadStudents(studentIds);
            Map<Long, NewHomeworkResult> homeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentIds, false);
            if (MapUtils.isNotEmpty(homeworkResultMap)) {
                for (Long sid : studentIds) {
                    Map<String, Object> studentMap = new LinkedHashMap<>();
                    NewHomeworkResult newHomeworkResult = homeworkResultMap.get(sid);
                    if (newHomeworkResult != null && MapUtils.isNotEmpty(newHomeworkResult.getPractices())) {
                        Integer score = 0;
                        Long duration = 0L;
                        Integer rank = studentIds.indexOf(sid) + 1;
                        MentalArithmeticCredit mac = MentalArithmeticCredit.of(rank);
                        Integer rankCredit = mac == null ? 0 : SafeConverter.toInt(mac.getCredit());
                        NewHomeworkResultAnswer resultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.MENTAL_ARITHMETIC);
                        if (resultAnswer != null) {
                            score = resultAnswer.processScore(ObjectiveConfigType.MENTAL_ARITHMETIC);
                            duration = resultAnswer.getDuration();
                        }
                        studentMap.put("rank", rank);
                        studentMap.put("studentId", sid);
                        studentMap.put("studentName", studentDetails.get(sid).fetchRealname());
                        studentMap.put("myself", sid.equals(studentId));
                        studentMap.put("score", score);
                        String durationStr = NewHomeworkUtils.handlerEnTime((int) (duration / 1000));
                        studentMap.put("duration", durationStr);
                        studentMap.put("rankCredit", rankCredit);
                    }
                    studentCharts.add(studentMap);
                }
            }
        }

        NewHomeworkResult homeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
        if (homeworkResult != null && homeworkResult.isFinished()) {
            Integer score = 0;
            Long duration = 0L;
            if (MapUtils.isNotEmpty(homeworkResult.getPractices())) {
                NewHomeworkResultAnswer resultAnswer = homeworkResult.getPractices().get(ObjectiveConfigType.MENTAL_ARITHMETIC);
                if (resultAnswer != null) {
                    score = resultAnswer.processScore(ObjectiveConfigType.MENTAL_ARITHMETIC);
                    duration = resultAnswer.getDuration();
                }
            }

            List<String> processIds = new ArrayList<>();
            NewHomeworkResultAnswer answer = homeworkResult.getPractices().get(ObjectiveConfigType.MENTAL_ARITHMETIC);
            if (answer != null && MapUtils.isNotEmpty(answer.getAnswers())) {
                processIds.addAll(answer.getAnswers().values());
            }

            Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(homeworkId, processIds);
            int rightQuestionNum = 0;
            if (MapUtils.isNotEmpty(processResultMap)) {
                for (NewHomeworkProcessResult processResult : processResultMap.values()) {
                    if (Boolean.TRUE.equals(processResult.getGrasp()))
                        ++rightQuestionNum;
                }
            }
            String durationStr = NewHomeworkUtils.handlerEnTime((int) (duration / 1000));
            chartMap.put(
                    "myInfo", MapUtils.m(
                            "score", score,
                            "duration", durationStr,
                            "rightQuestionNum", rightQuestionNum,
                            "totalCredit", SafeConverter.toInt(homeworkResult.getCredit()))
            );
        }
        if (CollectionUtils.isEmpty(studentCharts)) {
            chartMap.put("chartInfo", "<p>无人上榜</p>（温馨提示：可能是提交的人数少于5人，或提交的人都低于60分）");
        }
        if (!newHomework.isHomeworkChecked()) {
            chartMap.put("chartInfo", "榜单将在教师检查后公布~");
        }
        chartMap.put("studentCharts", studentCharts);
        return MapMessage.successMessage().add("data", chartMap);
    }


    @Override
    public MapMessage loadStudentNewHomeworkHistoryDetail(String homeworkId, Long studentId) {
        return homeworkReportProcessor.loadStudentNewHomeworkHistoryDetail(homeworkId, studentId);
    }

    @Override
    public List<Map<String, Object>> loadTeacherUncheckedHomeworkList(Teacher teacher) {
        return homeworkReportProcessor.loadTeacherUncheckedHomeworkList(teacher);
    }

    @Override
    public Page<Map<String, Object>> pageHomeworkReportListByGroupIds(Collection<Long> groupIds, Pageable pageable, Subject subject) {
        return homeworkReportProcessor.pageHomeworkReportListByGroupIds(groupIds, pageable, subject);
    }

    @Override
    public Page<Map<String, Object>> pageHomeworkReportListByGroupIds(Collection<Long> groupIds, Pageable pageable, Subject subject, Date begin, Date end) {
        return homeworkReportProcessor.pageHomeworkReportListByGroupIds(groupIds, pageable, subject, begin, end);
    }

    @Override
    public Page<Map<String, Object>> pageHomeworkReportListByGroupIdsAndHomeworkStatus(Collection<Long> groupIds, Pageable pageable, Subject subject, HomeworkStatus homeworkStatus) {
        return homeworkReportProcessor.pageHomeworkReportListByGroupIdsAndHomeworkStatus(groupIds, pageable, subject, homeworkStatus);
    }

    @Override
    public Page<Map<String, Object>> pageHomeworkReportListByGroupIdsAndHomeworkStatus(Collection<Long> groupIds, Pageable pageable, Collection<Subject> subjects, HomeworkStatus homeworkStatus) {
        return homeworkReportProcessor.pageHomeworkReportListByGroupIdsAndHomeworkStatus(groupIds, pageable, subjects, homeworkStatus);
    }

    @Override
    public MapMessage homeworkReportForStudent(Teacher teacher, String homeworkId, boolean isPcWay) {
        return homeworkReportProcessor.newHomeworkReportForStudent(teacher, homeworkId, isPcWay);
    }


    @Override
    public List<Map<String, Object>> homeworkReportForStudentInfo(String homeworkId) {
        return homeworkReportProcessor.homeworkReportForStudentInfo(homeworkId);
    }

    @Override
    public MapMessage reportDetailIndex(Teacher teacher, String homeworkId) {
        return homeworkReportProcessor.reportDetailIndex(teacher, homeworkId);
    }

    @Override
    public MapMessage fetchQuestionDetailPart(Teacher teacher, String homeworkId, String cdnBaseUrl) {
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            logger.warn("fetch Question Detail Part failed : hid {}", homeworkId);
            return MapMessage.errorMessage("作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEW_HOMEWORK_TYPE_NOT_EXIST);
        }
        if (CollectionUtils.isEmpty(newHomework.getPractices())) {
            logger.warn("fetch Question Detail Part failed : hid {}", homeworkId);
            return MapMessage.errorMessage("这份作业里面不存在习题").setErrorCode(ErrorCodeConstants.ERROR_CODE_MISS_HOMEWORK_PRACTICE);
        }
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), newHomework.getClazzGroupId())) {
            logger.warn("fetch Question Detail Part failed : hid {}", homeworkId);
            return MapMessage.errorMessage("没有权限查看此作业报告").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_PERMISSION);
        }
        GroupMapper group = raikouSystem.loadGroupDetail(newHomework.getClazzGroupId()).firstOrNull();
        if (group == null) {
            logger.warn("fetch Question Detail Part failed : hid {}", homeworkId);
            return MapMessage.errorMessage("班组不存在");
        }
        Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(group.getClazzId());
        if (clazz == null) {
            logger.warn("fetch Question Detail Part failed : hid {}", homeworkId);
            return MapMessage.errorMessage("班级不存在");
        }
        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                .stream()
                .collect(Collectors
                        .toMap(LongIdEntity::getId, Function.identity()));
        if (MapUtils.isEmpty(userMap)) {
            logger.warn("fetch Question Detail Part failed : hid {}", homeworkId);
            return MapMessage.errorMessage("班组不存在学生");
        }
        try {
            Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), false);
            //是否都是主观作业
            boolean allSubjective = true;
            List<ObjectiveConfigType> types = new LinkedList<>();
            //某人没完成学生各类型的显示数据
            for (NewHomeworkPracticeContent newHomeworkPracticeContent : newHomework.getPractices()) {
                if (newHomeworkPracticeContent.getType() != null) {
                    types.add(newHomeworkPracticeContent.getType());
                    if (NewHomeworkConstants.NOT_SHOW_SCORE_TYPE.contains(newHomeworkPracticeContent.getType()))
                        continue;
                    if (!newHomeworkPracticeContent.getType().isSubjective()) {
                        allSubjective = false;
                    }
                }
            }
            int finishedNum = 0;
            int totalScore = 0;
            boolean canMarking = false;
            //总分数和总时间
            for (User user : userMap.values()) {
                if (newHomeworkResultMap.containsKey(user.getId())) {
                    NewHomeworkResult newHomeworkResult = newHomeworkResultMap.get(user.getId());
                    if (newHomeworkResult.isFinished()) {
                        if (!newHomeworkResult.isCorrected()) {
                            canMarking = true;
                        }
                        finishedNum++;
                        Integer score = newHomeworkResult.processScore();
                        if (score != null) {
                            totalScore += score;
                        }
                    }
                }
            }

            MapMessage mapMessage = MapMessage.successMessage();
            //获取页面公用属性
            fetchPublicTabInfo(mapMessage,
                    newHomework,
                    false,
                    allSubjective,
                    userMap,
                    clazz,
                    finishedNum,
                    totalScore,
                    canMarking,
                    null,
                    null);

            //各个类型题目分析
            Map<ObjectiveConfigType, Object> typeDataResult = new LinkedHashMap<>();
            for (ObjectiveConfigType type : types) {
                ProcessNewHomeworkAnswerDetailTemplate template = processNewHomeworkAnswerDetailFactory.getTemplate(type);
                if (template == null)
                    continue;
                template.processQuestionPartTypeInfo(newHomeworkResultMap, newHomework, type, typeDataResult, cdnBaseUrl);
            }
            List<Object> typeReportList = types.stream()
                    .filter(typeDataResult::containsKey)
                    .map(typeDataResult::get)
                    .collect(Collectors.toList());
            mapMessage.put("typeReportList", typeReportList);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch Question Detail Part failed : hid {}", homeworkId, e);
            return MapMessage.errorMessage();
        }
    }


    //学生查看和题目查看公用信息，用一个方法
    private void fetchPublicTabInfo(MapMessage mapMessage,
                                    NewHomework newHomework,
                                    boolean showCorrect,
                                    boolean allSubjective,
                                    Map<Long, User> userMap,
                                    Clazz clazz,
                                    int finishedNum,
                                    int totalScore,
                                    boolean canMarking,
                                    Map<String, HomeworkSelfStudyRef> homeworkSelfStudyRefMap,
                                    Map<String, SelfStudyHomeworkReport> selfStudyHomeworkReportMap) {
        //平均分
        Integer avgScore = null;
        //都是主观作业的时候没有分数，这个地方等语文朗读背诵加上分数，又得改
        if (!allSubjective) {
            if (finishedNum == 0) {
                avgScore = 0;
            } else {
                avgScore = new BigDecimal(totalScore).divide(new BigDecimal(finishedNum), 0, BigDecimal.ROUND_HALF_UP).intValue();
            }
        }
        //订正：需要订正的人数，完成订正的人数
        Integer needCorrectNum = null;
        Integer finishCorrectNum = null;
        if (showCorrect) {
            needCorrectNum = homeworkSelfStudyRefMap.size();
            finishCorrectNum = selfStudyHomeworkReportMap.size();
        }
        mapMessage.add("subject", newHomework.getSubject());
        mapMessage.add("subjectName", newHomework.getSubject().getValue());
        mapMessage.add("clazzName", clazz.formalizeClazzName());
        mapMessage.add("createAt", DateUtils.dateToString(newHomework.getCreateAt(), "yyyy年MM月dd日") + (newHomework.isTermEnd() ? "期末复习" : ""));
        mapMessage.add("finishedUserNum", finishedNum);
        mapMessage.add("showCorrect", showCorrect);
        mapMessage.add("checked", SafeConverter.toBoolean(newHomework.getChecked()));
        mapMessage.add("totalUserNum", userMap.size());
        mapMessage.add("avgScore", avgScore);
        mapMessage.add("needCorrectNum", needCorrectNum);
        mapMessage.add("finishCorrectNum", finishCorrectNum);
        mapMessage.add("canMarking", canMarking);
    }


    @Override
    public MapMessage fetchStudentDetailPart(Teacher teacher, String homeworkId) {
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            logger.warn("fetch Student Detail Part failed : hid {}", homeworkId);
            return MapMessage.errorMessage("作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEW_HOMEWORK_TYPE_NOT_EXIST);
        }
        if (CollectionUtils.isEmpty(newHomework.getPractices())) {
            logger.warn("fetch Student Detail Part failed : hid {}", homeworkId);
            return MapMessage.errorMessage("这份作业里面不存在习题").setErrorCode(ErrorCodeConstants.ERROR_CODE_MISS_HOMEWORK_PRACTICE);
        }
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), newHomework.getClazzGroupId())) {
            logger.warn("fetch Student Detail Part failed : hid {}", homeworkId);
            return MapMessage.errorMessage("没有权限查看此作业报告").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_PERMISSION);
        }
        GroupMapper group = groupLoaderClient.loadGroup(newHomework.getClazzGroupId(), true);
        if (group == null) {
            logger.warn("fetch Student Detail Part failed : hid {}", homeworkId);
            return MapMessage.errorMessage("班组不存在");
        }
        Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(group.getClazzId());
        if (clazz == null) {
            logger.warn("fetch Student Detail Part failed : hid {}", homeworkId);
            return MapMessage.errorMessage("班级不存在");
        }
        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                .stream()
                .collect(Collectors
                        .toMap(LongIdEntity::getId, Function.identity()));
        if (MapUtils.isEmpty(userMap)) {
            logger.warn("fetch Student Detail Part failed : hid {}", homeworkId);
            return MapMessage.errorMessage("班组不存在学生");
        }
        try {
            Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), false);
            List<ObjectiveConfigType> types = new LinkedList<>();
            List<String> typeNames = new LinkedList<>();
            //是否都是主观作业
            boolean allSubjective = true;
            //某人没完成学生各类型的显示数据
            List<String> defaultTypeInformation = new LinkedList<>();
            for (NewHomeworkPracticeContent newHomeworkPracticeContent : newHomework.getPractices()) {
                ObjectiveConfigType type = newHomeworkPracticeContent.getType();
                if (type == null)
                    continue;
                types.add(newHomeworkPracticeContent.getType());
                typeNames.add(newHomeworkPracticeContent.getType().getValue());
                defaultTypeInformation.add("未完成");
                if (NewHomeworkConstants.NOT_SHOW_SCORE_TYPE.contains(newHomeworkPracticeContent.getType()))
                    continue;
                if (!newHomeworkPracticeContent.getType().isSubjective()) {
                    allSubjective = false;
                }
            }

            //****begin 获取订正的数据****//
            //根据配置取是否显示订正相关信息
            Map<String, HomeworkSelfStudyRef> homeworkSelfStudyRefMap = new LinkedHashMap<>();
            Map<String, SelfStudyHomeworkReport> selfStudyHomeworkReportMap = new LinkedHashMap<>();
            boolean showCorrect = fetchCorrectInfo(null, selfStudyHomeworkReportMap, homeworkSelfStudyRefMap, types, newHomework, newHomeworkResultMap);
            //****begin 获取订正的数据****//

            List<StudentPersonalInfo> studentPersonalInfos = new LinkedList<>();
            int finishedNum = 0;
            int totalScore = 0;
            //是否需要批阅
            boolean canMarking = false;
            for (User user : userMap.values()) {
                StudentPersonalInfo studentPersonalInfo = new StudentPersonalInfo();
                studentPersonalInfos.add(studentPersonalInfo);
                studentPersonalInfo.setUserId(user.getId());
                studentPersonalInfo.setUserName(user.fetchRealnameIfBlankId());
                NewHomeworkResult newHomeworkResult = newHomeworkResultMap.get(user.getId());
                //评语部分内容
                if (newHomeworkResult != null) {
                    studentPersonalInfo.setComment(newHomeworkResult.getComment());
                    studentPersonalInfo.setAudioComment(newHomeworkResult.getAudioComment());
                }
                //学生是否开始作业
                if (newHomeworkResult != null && newHomeworkResult.getPractices() != null) {
                    studentPersonalInfo.setBegin(true);
                    //是否完成
                    if (newHomeworkResult.isFinished()) {
                        finishedNum++;
                        studentPersonalInfo.setFinished(true);
                        //该学生是否未被批改，如果未被批改，说明老师有未批改学生：canMarking 是true
                        if (!newHomeworkResult.isCorrected()) {
                            canMarking = true;
                        }
                        studentPersonalInfo.setFinishTime(newHomeworkResult.getFinishAt().getTime());
                        studentPersonalInfo.setFinishTimeStr(DateUtils.dateToString(newHomeworkResult.getFinishAt(), "MM月dd日 HH:mm"));
                        studentPersonalInfo.setRepair(SafeConverter.toBoolean(newHomeworkResult.getRepair()));
                        if (!allSubjective) {
                            Integer score = newHomeworkResult.processScore();
                            //可能存在完成却并没有分数的情况：比如作业都是主观题，这时候分数显示--
                            if (score != null) {
                                studentPersonalInfo.setAvgScore(score);
                                studentPersonalInfo.setAvgScoreStr(SafeConverter.toString(score));
                                totalScore += score;
                            }
                        }
                        //先判断是否需要订正
                        if (showCorrect) {
                            //学生订正情况分析
                            String s = newHomework.getId() + "_" + user.getId();
                            if (homeworkSelfStudyRefMap.containsKey(s)) {
                                HomeworkSelfStudyRef homeworkSelfStudyRef = homeworkSelfStudyRefMap.get(s);
                                if (StringUtils.isBlank(homeworkSelfStudyRef.getSelfStudyId())) {
                                    studentPersonalInfo.setCorrectInfo("未订正");
                                } else {
                                    SelfStudyHomeworkReport selfStudyHomeworkReport = selfStudyHomeworkReportMap.get(homeworkSelfStudyRef.getSelfStudyId());
                                    if (selfStudyHomeworkReport != null) {
                                        String correctInfo = "100%";
                                        if (selfStudyHomeworkReport.getPractices() != null) {
                                            List<SelfStudyHomeworkReportQuestion> selfStudyHomeworkReportQuestions = new LinkedList<>();
                                            for (LinkedHashMap<String, SelfStudyHomeworkReportQuestion> sh : selfStudyHomeworkReport.getPractices().values()) {
                                                selfStudyHomeworkReportQuestions.addAll(sh.values());
                                            }
                                            if (selfStudyHomeworkReportQuestions.size() != 0) {
                                                long num = selfStudyHomeworkReportQuestions.stream()
                                                        .filter(o -> SafeConverter.toBoolean(o.getGrasp()))
                                                        .count();
                                                correctInfo = new BigDecimal(num * 100).divide(new BigDecimal(selfStudyHomeworkReportQuestions.size()), BigDecimal.ROUND_HALF_UP, 0).intValue() + "%";
                                            }
                                        }
                                        studentPersonalInfo.setCorrectInfo(correctInfo);
                                    } else {
                                        studentPersonalInfo.setCorrectInfo("未订正");
                                    }
                                }
                            } else {
                                studentPersonalInfo.setCorrectInfo("无需");
                            }
                        }
                    }
                    //时间比较特殊，需要各个类型加起来向上取整得到，分数却不需要，因为时间的单位是分钟，要确保按照类型查看和整个查看的时间和相同
                    int duration = 0;
                    for (ObjectiveConfigType type : types) {

                        if (newHomeworkResult.getPractices() != null && newHomeworkResult.getPractices().containsKey(type) && newHomeworkResult.getPractices().get(type).isFinished()) {
                            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);

                            //各个类型表格分数表示
                            ProcessNewHomeworkAnswerDetailTemplate template = processNewHomeworkAnswerDetailFactory.getTemplate(type);
                            if (template != null) {
                                studentPersonalInfo.getTypeInformation().add(template.processStudentPartTypeScore(newHomework, newHomeworkResultAnswer, type));
                            } else {
                                studentPersonalInfo.getTypeInformation().add("暂不支持");
                            }
                            if (type.equals(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC)) {//纸质口算练习不计算总时间
                                continue;
                            }
                            duration += new BigDecimal(SafeConverter.toInt(newHomeworkResultAnswer.processDuration())).divide(new BigDecimal(60), 0, BigDecimal.ROUND_UP).intValue();
                        } else {
                            studentPersonalInfo.getTypeInformation().add("未完成");
                        }
                    }
                    if (newHomeworkResult.isFinished() && types.size() == 1 && types.get(0).equals(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC)) {
                        studentPersonalInfo.setDuration(0);
                        studentPersonalInfo.setDurationStr("--");
                    } else if (newHomeworkResult.isFinished()) {
                        studentPersonalInfo.setDuration(duration);
                        studentPersonalInfo.setDurationStr(studentPersonalInfo.getDuration() + "分钟");
                    }
                } else {
                    //用公用默认defaultTypeInformation
                    studentPersonalInfo.setTypeInformation(defaultTypeInformation);
                }
            }
            MapMessage mapMessage = MapMessage.successMessage();
            //获取页面公用属性
            fetchPublicTabInfo(mapMessage,
                    newHomework,
                    showCorrect,
                    allSubjective,
                    userMap,
                    clazz,
                    finishedNum,
                    totalScore,
                    canMarking,
                    homeworkSelfStudyRefMap,
                    selfStudyHomeworkReportMap);
            //分数倒序,时间正序，完成时间正序
            studentPersonalInfos.sort((o1, o2) -> {
                int compare = Long.compare(o2.getAvgScore(), o1.getAvgScore());
                if (compare == 0) {
                    compare = Integer.compare(o1.getDuration(), o2.getDuration());
                    if (compare == 0) {
                        compare = Long.compare(o1.getFinishTime(), o2.getFinishTime());
                    }
                }
                return compare;
            });
            mapMessage.add("studentPersonalInfos", studentPersonalInfos);
            mapMessage.add("typeNames", typeNames);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch Student Detail Part failed : hid {}", homeworkId, e);
            return MapMessage.errorMessage("获取作业报告失败");
        }
    }


    /**
     * 处理数据展开
     *
     * @param homeworkId 作业ID
     * @param categoryId 练习类型id
     * @param lessonId   我是一个很奇怪的属性
     * @return 返回作业的Base_app类型的链接详情具体内容
     */
    @Override
    public MapMessage reportDetailsBaseApp(String homeworkId, String categoryId, String lessonId, ObjectiveConfigType objectiveConfigType) {
        try {
            MapMessage mapMessage = new MapMessage();
            NewHomework newHomework = newHomeworkLoader.load(homeworkId);
            if (Objects.isNull(newHomework)) {
                logger.warn("fetch report details basic app failed : hid {},categoryId {},lessonId {},objectiveConfigType {}", homeworkId, categoryId, lessonId, objectiveConfigType);
                return MapMessage.errorMessage("homework does not exist");
            }
            Long groupId = newHomework.getClazzGroupId();
            GroupMapper group = groupLoaderClient.loadGroup(groupId, false);
            if (group == null) {
                logger.warn("report Details BaseApp failed hid {},categoryId {},lessonId {}, objectiveConfigType {}", homeworkId, categoryId, lessonId, objectiveConfigType);
                return MapMessage.errorMessage("班组不存在");
            }
            Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(group.getClazzId());
            if (clazz == null) {
                logger.warn("report Details BaseApp failed hid {},categoryId {},lessonId {}, objectiveConfigType {}", homeworkId, categoryId, lessonId, objectiveConfigType);
                return MapMessage.errorMessage("班级不存在");
            }
            String clazzName;
            Date startDate;
            NewHomeworkApp target = null;
            List<NewHomeworkApp> apps = newHomework.findNewHomeworkApps(objectiveConfigType);
            List<String> questionIds = new LinkedList<>();
            for (NewHomeworkApp o : apps) {
                if (Objects.equals(SafeConverter.toString(o.getCategoryId()), categoryId)
                        && Objects.equals(o.getLessonId(), lessonId)
                        && CollectionUtils.isNotEmpty(o.getQuestions())) {
                    questionIds = o.fetchQuestions()
                            .stream()
                            .map(NewHomeworkQuestion::getQuestionId)
                            .collect(Collectors.toList());
                    target = o;
                    break;
                }
            }
            if (target == null) {
                logger.error("report Details BaseApp failed hid {},categoryId {},lessonId {}, objectiveConfigType {}", homeworkId, categoryId, lessonId, objectiveConfigType);
                return MapMessage.errorMessage("target is null:目标App不存在");
            }
            PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(target.getPracticeId());
            if (Objects.isNull(practiceType)) {
                logger.error("report Details BaseApp failed hid {},categoryId {},lessonId {}, objectiveConfigType {}", homeworkId, categoryId, lessonId, objectiveConfigType);
                return MapMessage.errorMessage("practiceType not exist");
            }
            mapMessage.add("questionInfoMapper", processNewHomeworkForBaseApp(target, newHomework, practiceType, questionIds, objectiveConfigType));
            clazzName = clazz.formalizeClazzName();
            startDate = newHomework.getStartTime();
            mapMessage.add("homeworkId", homeworkId);
            mapMessage.add("description", practiceType.getDescription());
            mapMessage.add("homeworkType", newHomework.getNewHomeworkType());
            mapMessage.add("needRecord", practiceType.getNeedRecord());
            mapMessage.add("categoryName", practiceType.getCategoryName());
            mapMessage.add("className", clazzName);
            mapMessage.add("startDate", DateUtils.dateToString(startDate, "yyyy年MM月dd日"));
            mapMessage.add("categoryId", practiceType.getCategoryId());
            mapMessage.setSuccess(true);
            return mapMessage;
        } catch (Exception e) {
            logger.error("report Details BaseApp failed hid {},categoryId {},lessonId {}, objectiveConfigType {}", homeworkId, categoryId, lessonId, objectiveConfigType, e);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage shareReport(Teacher teacher, String hid, List<VoiceRecommend.RecommendVoice> recommendVoiceList, List<VoiceRecommend.ReadReciteVoice> readReciteVoiceList,
                                  String shareList, List<BaseVoiceRecommend.DubbingWithScore> excellentDubbingStu, List<BaseVoiceRecommend.ImageText> imageTextList) {
        try {

            //************** begin 通过缓存是否存在数据 ，是否今天发送到班群过班群 **************//
            NoticeShareReportToJztCacheManager noticeShareReportToJztCacheManager = newHomeworkCacheService.getNoticeShareReportToJztCacheManager();
            String cacheKey = noticeShareReportToJztCacheManager.getCacheKey(hid);
            boolean share = noticeShareReportToJztCacheManager.load(cacheKey) != null;
            if (share) {
                return MapMessage.errorMessage("已分享到班群");
            }
            noticeShareReportToJztCacheManager.add(cacheKey, 1);
            //************** end 通过缓存是否存在数据 ，是否今天发送到班群过班群 **************//


            //************** begin 给班群发送消息 **************//

            NewHomework newHomework = newHomeworkLoader.load(hid);
            if (newHomework == null) {
                logger.error("teacher Push share Report Message failed : tid {}  ,hid {}", teacher.getId(), hid);
                return MapMessage.errorMessage();
            }

            Subject subject = newHomework.getSubject();
            // 基础练习、自然拼读、课文读背优秀录音推荐
            VoiceRecommend voiceRecommend = voiceRecommendDao.load(hid);
            List<User> userList = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId());
            if (CollectionUtils.isEmpty(userList)) {
                return MapMessage.errorMessage("该班级暂无学生");
            }
            Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(LongIdEntity::getId, Function.identity()));
            if (voiceRecommend == null) {
                boolean isVoiceBelongToClass;
                if (Subject.ENGLISH == subject && recommendVoiceList != null) {
                    isVoiceBelongToClass = recommendVoiceList.stream().allMatch(r -> userMap.containsKey(r.getStudentId()));
                    if (!isVoiceBelongToClass) {
                        return MapMessage.errorMessage("推荐学生不属于该班级");
                    }
                    newHomeworkService.submitVoiceRecommend(hid, recommendVoiceList, "");
                } else if (Subject.CHINESE == subject && readReciteVoiceList != null) {
                    isVoiceBelongToClass = readReciteVoiceList.stream().allMatch(r -> userMap.containsKey(r.getStudentId()));
                    if (!isVoiceBelongToClass) {
                        return MapMessage.errorMessage("推荐学生不属于该班级");
                    }
                    newHomeworkService.submitReadReciteVoiceRecommend(hid, readReciteVoiceList, "");
                }
            }
            // 趣味配音优秀配音推荐
            DubbingRecommend dubbingRecommend = dubbingRecommendDao.load(hid);
            if (dubbingRecommend == null && CollectionUtils.isNotEmpty(excellentDubbingStu)) {
                boolean isdubbingBelongToClass = excellentDubbingStu.stream().allMatch(r -> userMap.containsKey(r.getUserId()));
                if (!isdubbingBelongToClass) {
                    return MapMessage.errorMessage("推荐学生不属于该班级");
                }
                if (Subject.ENGLISH == subject) {
                    newHomeworkService.submitDubbingVoiceRecommend(hid, excellentDubbingStu, "");
                }
            }
            // 图文入韵优秀配音推荐
            ImageTextRecommend imageTextRecommend = imageTextRecommendDao.load(hid);
            if (imageTextRecommend == null && CollectionUtils.isNotEmpty(imageTextList)) {
                boolean belongToClazz = imageTextList.stream().allMatch(r -> userMap.containsKey(r.getStudentId()));
                if (!belongToClazz) {
                    return MapMessage.errorMessage("推荐学生不属于该班级");
                }
                if (Subject.CHINESE == subject) {
                    newHomeworkService.submitImageTextRecommend(hid, imageTextList);
                }
            }

            //发到Parent Provider 替换掉环信
            //新的群组消息ScoreCircle
            ScoreCircleQueueCommand circleQueueCommand = new ScoreCircleQueueCommand();
            circleQueueCommand.setGroupId(newHomework.getClazzGroupId());
            circleQueueCommand.setCreateDate(new Date());
            circleQueueCommand.setGroupCircleType("HOMEWORK_REPORT");
            circleQueueCommand.setTypeId(newHomework.getId());
            circleQueueCommand.setImgUrl("");
            String linkUrl = "/view/mobile/parent/homework/report_detail?tab=clazz&hid=" + newHomework.getId();
            if (NewHomeworkType.OCR == newHomework.getType()) {
                linkUrl = "/view/mobile/parent/ocrhomework/report";
            }
            circleQueueCommand.setLinkUrl(linkUrl);
            //正文
            circleQueueCommand.setContent("这次作业全班情况，包括优异的同学、待提高的同学，还有一些错题的情况……请家长阅读并重视！");
            ScoreCircleQueueCommand.ExtInfo circleExtInfo = new ScoreCircleQueueCommand.ExtInfo();
            NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(newHomework.toLocation().getId());
            String extContent = DateUtils.dateToString(newHomework.getStartTime(), "MM月dd日作业");
            String unitName = "";
            if (newHomeworkBook != null) {
                extContent += "，内容：" + StringUtils.join(newHomeworkBook.processUnitNameList(), ",");
                unitName = StringUtils.join(newHomeworkBook.processUnitNameList(), "，");
            }
            circleQueueCommand.setUnitName(unitName);
            circleExtInfo.setContent(extContent);
            circleExtInfo.setExtType("CONTENT");
            circleQueueCommand.setExtInfoList(Collections.singletonList(circleExtInfo));
            newHomeworkParentQueueProducer.getProducer().produce(Message.newMessage().writeObject(circleQueueCommand));

            String iMContent = "家长好，这次作业大部分同学按质按量完成了，但仍有部分学生存在不足，请家长务必配合催促";

            if (teacher == null) {
                return MapMessage.errorMessage();
            }
            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacher.getId());
            Long teacherId = mainTeacherId == null ? teacher.getId() : mainTeacherId;
            //这里才是取所有的学科
            Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
            List<Subject> subjectList = teacherLoaderClient.loadTeachers(relTeacherIds).values().stream().map(Teacher::getSubject).collect(Collectors.toList());
            List<String> subjectStrList = subjectList.stream().sorted(Comparator.comparingInt(Subject::getId)).map(Subject::getValue).collect(Collectors.toList());
            String subjectsStr = "（" + StringUtils.join(subjectStrList.toArray(), "，") + "）";

            String em_push_title = teacher.fetchRealnameIfBlankId() + subjectsStr + "：" + iMContent;

            //************** end 给班群发送消息 **************//

            //新的极光push
            Map<String, Object> jpushExtInfo = new HashMap<>();
            jpushExtInfo.put("studentId", "");
            jpushExtInfo.put("s", ParentAppPushType.HOMEWORK_DAILY_REPORT.name());
            jpushExtInfo.put("url", "/view/mobile/parent/homework/report_detail?tab=clazz&hid=" + newHomework.getId());
            if (NewHomeworkType.OCR == newHomework.getType()) {
                jpushExtInfo.put("url", UrlUtils.buildUrlQuery("/view/mobile/parent/ocrhomework/report", MapUtils.m(
                        "homeworkId", newHomework.getId(),
                        "subject", newHomework.getSubject(),
                        "objectiveConfigType", Subject.ENGLISH == newHomework.getSubject() ? ObjectiveConfigType.OCR_DICTATION : ObjectiveConfigType.OCR_MENTAL_ARITHMETIC
                )));
            }
            appMessageServiceClient.sendAppJpushMessageByTags(em_push_title,
                    AppMessageSource.PARENT,
                    Collections.singletonList(JpushUserTag.CLAZZ_GROUP_REFACTOR.generateTag(SafeConverter.toString(newHomework.getClazzGroupId()))),
                    null,
                    jpushExtInfo);

            // 发送广播
            Map<String, Object> map = new HashMap<>();
            map.put("messageType", NewHomeworkPublishMessageType.shareHomeworkReport);
            map.put("groupId", newHomework.getClazzGroupId());
            map.put("homeworkId", newHomework.getId());
            map.put("subject", newHomework.getSubject());
            map.put("teacherId", newHomework.getTeacherId());
            map.put("createAt", newHomework.getCreateAt().getTime());
            map.put("startTime", newHomework.getStartTime().getTime());
            map.put("endTime", newHomework.getEndTime().getTime());
            map.put("homeworkType", newHomework.getNewHomeworkType());
            map.put("homeworkTag", newHomework.getHomeworkTag());
            newHomeworkPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));

            if (StringUtils.isNotBlank(shareList)) {
                newHomeworkService.updateReportShareParts(hid, shareList);
            }

            return MapMessage.successMessage();
        } catch (Exception e) {
            logger.error("teacher Push share Report Message failed : tid {}  ,hid {}", teacher.getId(), hid);
            return MapMessage.errorMessage();
        }

    }


    private void urgePush(Teacher teacher, String homeworkId, Set<Long> userIds, boolean correct) {
        NewHomework newhomework = newHomeworkLoader.load(homeworkId);
        NewHomework.Location location = newhomework.toLocation();
        if (location == null || CollectionUtils.isEmpty(userIds)) {
            return;
        }

        // 消息跳转地址
        String link = UrlUtils.buildUrlQuery("/studentMobile/homework/app/skip.vpage", MapUtils.m("homeworkId", homeworkId));
        // 消息内容
        String content = correct ? teacher.respectfulName() + "提醒你快去订正错题，从错误中学习，收获更大的进步！" :
                teacher.respectfulName() + "提醒你完成" + DateUtils.dateToString(newhomework.getCreateAt(), "MM月dd日") + "的" + newhomework.getSubject().getValue() + "作业，天才出于勤奋，快去完成吧！";
        String title = correct ? "订正提醒" : "作业提醒";
        Integer s = correct ? StudentAppPushType.HOMEWORK_ERROR_QUESTION_PUSH.getType() : StudentAppPushType.HOMEWORK_HURRY_REMIND.getType();

        // 消息中心
        List<AppMessage> messages = new ArrayList<>();
        for (Long userId : userIds) {
            AppMessage message = new AppMessage();
            message.setUserId(userId);
            message.setMessageType(s);
            message.setTitle(title);
            message.setContent(content);
            message.setLinkUrl(link);
            message.setLinkType(1); // 站内的相对地址
            messages.add(message);
        }
        // 发送消息中心
        messages.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);

        //发送jpush消息
        Map<String, Object> extInfo = MiscUtils.m("link", link, "t", "h5", "key", "j", "s", s, "title", title);
        appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.STUDENT, new ArrayList<>(userIds), extInfo);
    }


    /**
     * 学生个人base_app 个人详情
     *
     * @param homeworkId 作业ID
     * @param categoryId 练习类型id
     * @param lessonId   我是一个很奇怪的属性
     * @param studentId  学生ID
     * @return 学生个人base_app 个人详情
     */
    @Override
    public MapMessage reportDetailsBaseApp(String homeworkId, String categoryId, String lessonId, Long studentId, ObjectiveConfigType objectiveConfigType) {
        MapMessage mapMessage = new MapMessage();
        String clazzName;
        Date startDate;
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            logger.warn("report Details BaseApp failed hid {},categoryId {},lessonId {}, objectiveConfigType {} , studentId {}", homeworkId, categoryId, lessonId, objectiveConfigType, studentId);
            return MapMessage.errorMessage("该作业不存在");
        }
        Long groupId = newHomework.getClazzGroupId();
        GroupMapper group = groupLoaderClient.loadGroup(groupId, false);
        if (group == null) {
            logger.warn("report Details BaseApp failed hid {},categoryId {},lessonId {}, objectiveConfigType {} , studentId {}", homeworkId, categoryId, lessonId, objectiveConfigType, studentId);
            return MapMessage.errorMessage("班组 不存在");
        }
        Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(group.getClazzId());
        if (clazz == null) {
            logger.warn("report Details BaseApp failed hid {},categoryId {},lessonId {}, objectiveConfigType {} , studentId {}", homeworkId, categoryId, lessonId, objectiveConfigType, studentId);
            return MapMessage.errorMessage("班级 不存在");
        }
        clazzName = clazz.formalizeClazzName();
        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                .stream()
                .collect(Collectors
                        .toMap(LongIdEntity::getId, Function.identity()));
        if (!userMap.containsKey(studentId)) {
            logger.warn("report Details BaseApp failed hid {},categoryId {},lessonId {}, objectiveConfigType {} , studentId {}", homeworkId, categoryId, lessonId, objectiveConfigType, studentId);
            return MapMessage.errorMessage("该学生不在该作业的班级");
        }
        List<NewHomeworkApp> apps = newHomework.findNewHomeworkApps(objectiveConfigType);
        if (CollectionUtils.isEmpty(apps)) {
            logger.warn("report Details BaseApp failed hid {},categoryId {},lessonId {}, objectiveConfigType {} , studentId {}", homeworkId, categoryId, lessonId, objectiveConfigType, studentId);
            return MapMessage.errorMessage("App 不存在");
        }
        NewHomeworkApp target = apps.stream()
                .filter(o -> Objects.equals(SafeConverter.toString(o.getCategoryId()), categoryId))
                .filter(o -> Objects.equals(o.getLessonId(), lessonId))
                .findFirst()
                .orElse(null);
        if (target == null) {
            logger.warn("report Details BaseApp failed hid {},categoryId {},lessonId {}, objectiveConfigType {} , studentId {}", homeworkId, categoryId, lessonId, objectiveConfigType, studentId);
            return MapMessage.errorMessage("App 不存在");
        }
        PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(target.getPracticeId());
        if (Objects.isNull(practiceType)) {
            logger.warn("report Details BaseApp failed hid {},categoryId {},lessonId {}, objectiveConfigType {} , studentId {}", homeworkId, categoryId, lessonId, objectiveConfigType, studentId);
            return MapMessage.errorMessage("practiceType is null");
        }
        NewHomework.Location location = newHomework.toLocation();
        String day = DayRange.newInstance(location.getCreateTime()).toString();
        NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, location.getSubject(), location.getId(), studentId.toString());
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loads(Collections.singletonList(id.toString()), true).get(id.toString());
        if (Objects.isNull(newHomeworkResult)
                || MapUtils.isEmpty(newHomeworkResult.getPractices())) {
            return MapMessage.errorMessage("学生还未开始写作业");
        }
        if (!newHomeworkResult.isFinished()) {
            return MapMessage.errorMessage("学生未完成写作业");
        }
        User user = userMap.get(studentId);
        startDate = newHomework.getStartTime();
        List<Map<String, Object>> value = internalProcessHomeworkAnswer(user, newHomework, newHomeworkResult, categoryId, lessonId, practiceType, target, objectiveConfigType);
        mapMessage.add("description", practiceType.getDescription());
        mapMessage.add("tongueTwister", Objects.equals(categoryId, SafeConverter.toString(NatureSpellingType.TONGUE_TWISTER.getCategoryId())));
        mapMessage.add("questionInfoMapper", value);
        mapMessage.add("needRecord", practiceType.getNeedRecord());
        mapMessage.add("userId", user.getId());
        mapMessage.add("userName", user.fetchRealname());
        mapMessage.add("homeworkId", homeworkId);
        mapMessage.add("className", clazzName);
        mapMessage.add("startDate", DateUtils.dateToString(startDate, "yyyy年MM月dd日"));
        mapMessage.add("categoryName", practiceType.getCategoryName());
        mapMessage.add("categoryId", practiceType.getCategoryId());
        mapMessage.add("showCorrect", newHomeworkResult.isFinished());
        mapMessage.add("showCorrectInfo", newHomeworkResult.isFinished() ? "" : "此学生尚未完成全部作业，暂时不能批改");
        mapMessage.add("homeworkType", newHomework.getNewHomeworkType());
        mapMessage.add("subject", newHomework.getSubject());
        mapMessage.add("teacherId", newHomework.getTeacherId());
        mapMessage.add("objectiveConfigType", objectiveConfigType);
        mapMessage.setSuccess(true);
        return mapMessage;

    }

    @Override
    public MapMessage examAndQuizDetailInfo(String questionId, String homeworkId, ObjectiveConfigType type) {
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("homework does not exist");
        }

        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        MapMessage mapMessage = processExamNewHomework(newHomework, questionId, type, contentTypeMap);
        //作业ID和类题
        mapMessage.putAll(
                MapUtils.m(
                        "homeworkId", homeworkId,
                        "questionData", Collections.emptyList()
                ));
        mapMessage.setSuccess(true);
        return mapMessage;
    }

    private MapMessage processExamNewHomework(NewHomework newHomework, String questionId, ObjectiveConfigType type, Map<Integer, NewContentType> contentTypeMap) {
        // 找到某个作业下的所有作业形式
        MapMessage mapMessage = new MapMessage();
        NewQuestion newQuestion = questionLoaderClient.loadQuestionIncludeDisabled(questionId);
        //题信息结构
        Map<String, Object> questionObject = MapUtils.m(
                "difficultyName", QuestionConstants.newDifficultyMap.get(newQuestion.getDifficultyInt()),
                "questionType", contentTypeMap.get(newQuestion.getContentTypeId()) != null ? contentTypeMap.get(newQuestion.getContentTypeId()).getName() : "无题型",
                "questionId", questionId,
                "submitWay", newQuestion.getSubmitWays(),
                "seconds", newQuestion.getSeconds()
        );

        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                .stream()
                .collect(Collectors
                        .toMap(LongIdEntity::getId, Function.identity()));
        Map<String, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), false)
                .values()
                .stream()
                .filter(o -> o.isFinishedOfObjectiveConfigType(type))
                .collect(Collectors.toMap(NewHomeworkResult::getId, Function.identity()));

        // 1.找到此作业下所有已完成该题的作业

        Map<ObjectiveConfigType, List<NewHomework.NewHomeworkQuestionObj>> objectiveConfigTypeListMap = newHomework.processSubHomeworkResultAnswerIds(Collections.singleton(type));
        Map<String, NewHomework.NewHomeworkQuestionObj> newHomeworkQuestionObjMap = objectiveConfigTypeListMap.getOrDefault(type, Collections.emptyList())
                .stream()
                .collect(Collectors.toMap(NewHomework.NewHomeworkQuestionObj::getQuestionId, Function.identity()));
        if (!newHomeworkQuestionObjMap.containsKey(questionId)) {
            logger.error("process Exam NewHomework failed : hid {},questionId {},type {}", newHomework.getId(), questionId, type);
            return MapMessage.errorMessage("作业不包含此题");
        }
        NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj = newHomeworkQuestionObjMap.get(questionId);
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        List<String> subHomeworkResultAnswerIds = new LinkedList<>();
        for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
            subHomeworkResultAnswerIds.add(newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId()));
        }
        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);


        int submitNum = 0;
        List<String> processIds = new LinkedList<>();
        for (SubHomeworkResultAnswer subHomeworkResultAnswer : subHomeworkResultAnswerMap.values()) {
            submitNum++;
            processIds.add(subHomeworkResultAnswer.getProcessId());
        }
        //提交人数
        mapMessage.add("submitNum", submitNum);
        if (submitNum == 0) {
            mapMessage.add("error", 0);
        } else {
            Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);
            if (type == ObjectiveConfigType.ORAL_PRACTICE) {
                //口语习题 平均分
                double totalScore = newHomeworkProcessResultMap.values()
                        .stream()
                        .mapToDouble(o -> SafeConverter.toDouble(o.getScore()))
                        .sum();
                mapMessage.put("averageScore",
                        new BigDecimal(totalScore)
                                .divide(new BigDecimal(submitNum), 0, BigDecimal.ROUND_HALF_UP)
                                .intValue());
            } else {
                //错题数
                int error = SafeConverter.toInt(newHomeworkProcessResultMap
                        .values()
                        .stream()
                        .filter(o -> !SafeConverter.toBoolean(o.getGrasp()))
                        .count());
                //错误的人数
                mapMessage.add("error", error);
            }
        }
        mapMessage.add("questionObject", questionObject);
        mapMessage.setSuccess(true);
        return mapMessage;
    }

    @Override
    //一个班级下面可能有多个班组
    public MapMessage fetchClazzInfo(List<String> homeworkIds) {
        Map<String, NewHomework> newHomeworkMap = newHomeworkLoader.loads(homeworkIds);
        //班组信息
        Subject subject = null;
        Set<Long> groupIds = new LinkedHashSet<>();
        Map<Long, String> groupIdToHomeworkIdMap = new LinkedHashMap<>();
        for (NewHomework newHomework : newHomeworkMap.values()) {
            subject = newHomework.getSubject();
            if (newHomework.getClazzGroupId() != null) {
                groupIds.add(newHomework.getClazzGroupId());
                groupIdToHomeworkIdMap.put(newHomework.getClazzGroupId(), newHomework.getId());
            }
        }

        Map<Long, GroupMapper> groupMap = groupLoaderClient.loadGroups(groupIds, true);
        //<clazzId, Set<groupId>>
        Map<Long, Set<Long>> clazzToG = groupMap.values().stream().collect(Collectors.groupingBy(GroupMapper::getClazzId, Collectors.mapping(GroupMapper::getId, Collectors.toSet())));
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzToG.keySet())
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        List<Clazz> clazzs = MapUtils.isEmpty(clazzMap) ? Collections.emptyList() : clazzMap.values()
                .stream()
                .filter(Objects::nonNull)
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());

        List<String> selfStudyIds = Lists.newLinkedList();
        newHomeworkMap.values().forEach(o -> {
            GroupMapper groupMapper = groupMap.get(o.getClazzGroupId());
            if (groupMapper != null && CollectionUtils.isNotEmpty(groupMapper.getStudents())) {
                Set<String> selfStudyHomeworkIds = homeworkSelfStudyRefDao.loadSelfStudyHomeworkIds(o.getId(), Lists.transform(groupMapper.getStudents(), GroupMapper.GroupUser::getId));
                selfStudyIds.addAll(selfStudyHomeworkIds);
            }
        });
        Map<String, SelfStudyHomework> studyHomeworkMap = selfStudyHomeworkDao.loads(selfStudyIds);
        //班组包含课程的订正任务
        Map<Long, List<SelfStudyHomework>> groupSelfStudyHomeworkMap = studyHomeworkMap.values().stream()
                .filter(o -> !Collections.disjoint(NewHomeworkConstants.COURSE_APP_CONFIGTYPE, o.findPracticeContents().keySet()))
                .collect(Collectors.groupingBy(SelfStudyHomework::getClazzGroupId));

        List<RecommendHomeworkClazzInfo> recommendHomeworkClazzInfos = new LinkedList<>();
        boolean hasRecommendHomework = false;
        for (Clazz clazz : clazzs) {
            for (Long gId : clazzToG.get(clazz.getId())) {
                RecommendHomeworkClazzInfo s = new RecommendHomeworkClazzInfo();
                s.setClazzName(clazz.formalizeClazzName());
                s.setGroupId(gId);
                s.setHomeworkId(groupIdToHomeworkIdMap.get(gId));
                boolean groupHasRecommendHomework = groupSelfStudyHomeworkMap.containsKey(gId);
                if (groupHasRecommendHomework) {
                    hasRecommendHomework = true;
                }
                s.setHasRecommendHomework(groupHasRecommendHomework);
                recommendHomeworkClazzInfos.add(s);
            }
        }
        if (recommendHomeworkClazzInfos.isEmpty()) {
            return MapMessage.errorMessage("班级不存在");
        }
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("clazzInfo", recommendHomeworkClazzInfos);
        mapMessage.add("subject", subject);
        mapMessage.add("hasRecommendHomework", hasRecommendHomework);//是否有需要推荐的讲练测巩固练习
        return mapMessage;
    }


    //处理作业的分享报告
    @Override
    public NewHomeworkShareReport processNewHomeworkShareReport(String newHomeworkId, User user1, String cdnUrl) {
        NewHomework newHomework = newHomeworkLoader.load(newHomeworkId);
        NewHomeworkShareReport newHomeworkShareReport = new NewHomeworkShareReport();
        if (newHomework == null) {
            return newHomeworkShareReport;
        }
        List<User> userList = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId());
        if (CollectionUtils.isEmpty(userList)) {
            return newHomeworkShareReport;
        }
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(LongIdEntity::getId, Function.identity()));
        Set<ObjectiveConfigType> objectiveConfigTypeSet = new HashSet<>();
        List<NewHomeworkPracticeContent> practices = newHomework.getPractices();
        if (CollectionUtils.isNotEmpty(practices)) {
            practices.forEach(e -> objectiveConfigTypeSet.add(e.getType()));
        }
        Boolean onlySubjective = true;
        //不需要分数的类型
        if (CollectionUtils.isNotEmpty(objectiveConfigTypeSet)) {
            for (ObjectiveConfigType objectiveConfigType : objectiveConfigTypeSet) {
                if (NewHomeworkConstants.NOT_SHOW_SCORE_TYPE.contains(objectiveConfigType))
                    continue;
                if (!objectiveConfigType.isSubjective()) {
                    onlySubjective = false;
                    break;
                }
            }
        }
        newHomeworkShareReport.setOnlySubjective(onlySubjective);

//        NewHomeworkStudyMaster studyMaster = newHomeworkStudyMasterDao.load(newHomeworkId);
        //是否包含口算
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.MENTAL_ARITHMETIC);
        boolean hasMental = target != null;

        //上海城市需要分数等级制度处理
        boolean needScoreLevel = false;
        StudentDetail student = studentLoaderClient.loadStudentDetail(userMap.values().iterator().next().getId());
        if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(student, "ShowScoreLevel", "WhiteList")) {
            needScoreLevel = Boolean.TRUE;
        }
        //老师分享微信群报告-配音资源外露&建议升级方案灰度配置C：默认、A：升级引导外加必备工具外露（弹窗）、B：优质配音资源
        String planType = "C";
        if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(student, "ShareReportPlanA", "WhiteList")) {
            planType = "A";
        } else if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(student, "ShareReportPlanB", "WhiteList")) {
            planType = "B";
        }
        newHomeworkShareReport.setPlanType(planType);

        //老师分享需要查询明细
        Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), false);
        int totalScore = 0;
        newHomeworkShareReport.setTotalNum(userList.size());
        int finishedNum = 0;
        //订正信息数据准备
        List<String> homeworkToSelfStudyIds = Collections.emptyList();
        if (NewHomeworkConstants.showWrongQuestionInfo(newHomework.getCreateAt(), RuntimeMode.getCurrentStage())
                && (NewHomeworkUtils.isSubHomework(newHomework.getId()) || NewHomeworkUtils.isShardHomework(newHomework.getId()))) {
            homeworkToSelfStudyIds = newHomeworkResultMap.values()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(o -> newHomework.getId() + "_" + o.getUserId()).collect(Collectors.toList());
        }
        Map<String, HomeworkSelfStudyRef> homeworkSelfStudyRefMap = homeworkSelfStudyRefDao.loads(homeworkToSelfStudyIds);
        List<String> selfStudyIds = homeworkSelfStudyRefMap.values()
                .stream()
                .filter(o -> StringUtils.isNotBlank(o.getSelfStudyId()))
                .map(HomeworkSelfStudyRef::getSelfStudyId)
                .collect(Collectors.toList());
        Map<String, SelfStudyHomeworkReport> studyHomeworkReportMap = selfStudyHomeworkReportDao.loads(selfStudyIds);
        //最高分
        int highScore = 0;
        Map<Long, NewHomeworkShareReport.StudentReport> studentReportMap = new LinkedHashMap<>();
        for (User user : userList) {
            NewHomeworkResult n = newHomeworkResultMap.get(user.getId());
            //完成的学生
            if (n != null && n.isFinished()) {
                int score = SafeConverter.toInt(n.processScore());
                totalScore += score;
                if (highScore < score) {
                    highScore = score;
                }
                String scoreStr = needScoreLevel ? ScoreLevel.processLevel(score).getLevel() : score + "分";
                finishedNum++;
                NewHomeworkShareReport.StudentReport studentReport = new NewHomeworkShareReport.StudentReport();
                studentReport.setScore(score);
                studentReport.setScoreStr(scoreStr);
                studentReport.setConsumingTime(SafeConverter.toLong(n.processDuration()));
                if (!userMap.containsKey(n.getUserId())) {
                    continue;
                }
                if (hasMental) {
                    NewHomeworkResultAnswer newHomeworkResultAnswer = n.getPractices().get(ObjectiveConfigType.MENTAL_ARITHMETIC);
                    if (newHomeworkResultAnswer != null) {
                        int mentalScore = SafeConverter.toInt(newHomeworkResultAnswer.processScore(ObjectiveConfigType.MENTAL_ARITHMETIC));
                        studentReport.setMentalScore(mentalScore);
                        String mentalScoreStr = needScoreLevel ? ScoreLevel.processLevel(mentalScore).getLevel() : mentalScore + "分";
                        studentReport.setMentalScoreStr(mentalScoreStr);
                        int mentalDuration = SafeConverter.toInt(newHomeworkResultAnswer.processDuration());
                        studentReport.setMentalDuration(mentalDuration);
                        studentReport.setMentalDurationStr(NewHomeworkUtils.handlerEnTime(mentalDuration));
                    }
                }
                studentReport.setSid(n.getUserId());
                studentReport.setName(user.fetchRealnameIfBlankId());
                studentReport.setImageUrl(NewHomeworkUtils.getUserAvatarImgUrl(cdnUrl, user.fetchImageUrl()));
                studentReport.setSubmitTime(n.getFinishAt());
                studentReport.setFinishAtStr(DateUtils.dateToString(n.getFinishAt(), "MM-dd HH:mm"));
                int zdDuration = SafeConverter.toInt((n.getFinishAt().getTime() - n.getUserStartAt().getTime()) / 1000 - n.processDuration());
                if (zdDuration < 0) {
                    zdDuration = 0;
                }
                studentReport.setZdDurationStr(NewHomeworkUtils.handlerEnTime(zdDuration));
                String s = newHomework.getId() + "_" + n.getUserId();
                if (homeworkSelfStudyRefMap.containsKey(s)) {
                    studentReport.setNeedCorrect(true);
                    HomeworkSelfStudyRef homeworkSelfStudyRef = homeworkSelfStudyRefMap.get(s);
                    if (StringUtils.isNotBlank(homeworkSelfStudyRef.getSelfStudyId())) {
                        if (studyHomeworkReportMap.containsKey(homeworkSelfStudyRef.getSelfStudyId())) {
                            studentReport.setFinishedCorrect(true);
                        }
                    }
                }
                //错题未订正
                if (studentReport.isNeedCorrect() && !studentReport.isFinishedCorrect()) {
                    newHomeworkShareReport.getCorrectPart().add(studentReport);
                }
                // 完成订正的人数
                if (studentReport.isFinishedCorrect()) {
                    newHomeworkShareReport.setFinishedCorrectedNum(newHomeworkShareReport.getFinishedCorrectedNum() + 1);
                }
//                //满分
//                if (studentReport.getScore() == 100) {
//                    newHomeworkShareReport.getFullMarksPart().add(studentReport);
//                }
//                //90分及以上
//                if (studentReport.getScore() >= 90 && studentReport.getScore() < 100) {
//                    newHomeworkShareReport.getAchievementPartTwo().add(studentReport);
//                }
                newHomeworkShareReport.getFinishedStudentReports().add(studentReport);
                studentReportMap.put(user.getId(), studentReport);
            } else {
                //未完成的学生
                NewHomeworkShareReport.StudentReport studentReport = new NewHomeworkShareReport.StudentReport();
                studentReport.setSid(user.getId());
                studentReport.setName(user.fetchRealname());
                studentReport.setImageUrl(NewHomeworkUtils.getUserAvatarImgUrl(cdnUrl, user.fetchImageUrl()));
//                newHomeworkShareReport.getUnFinishedStudentReports().add(studentReport);
                studentReportMap.put(user.getId(), studentReport);
            }
        }
        newHomeworkShareReport.setStudentReportMap(studentReportMap);
        newHomeworkShareReport.setNeedScoreLevel(needScoreLevel);
        if (needScoreLevel) {
            newHomeworkShareReport.setOverNinety("成绩A");
            newHomeworkShareReport.setFullMark("成绩A+");
            newHomeworkShareReport.setOnm("成绩A及以上");
        }

        newHomeworkShareReport.setHasMental(hasMental);
        newHomeworkShareReport.setHighScore(highScore);

        //取学科之星、口算之星、专注之星、积极之星
//        if (studyMaster != null) {
//            if (studyMaster.getExcellentList() != null) {
//                studyMaster.getExcellentList().stream().filter(e -> studentReportMap.get(e) != null).forEach(e -> newHomeworkShareReport.getAchievementPart().add(studentReportMap.get(e)));
//            }
//
//            if (studyMaster.getCalculationList() != null) {
//                List<Long> calculationList = studyMaster.getCalculationList();
//                if (studyMaster.getCalculationList().size() > 3) {
//                    calculationList = studyMaster.getCalculationList().subList(0, 3);
//                }
//                calculationList.stream().filter(e -> studentReportMap.get(e) != null).forEach(e -> newHomeworkShareReport.getMentalPart().add(studentReportMap.get(e)));
//            }
//
//            if (studyMaster.getFocusList() != null) {
//                studyMaster.getFocusList().stream().filter(e -> studentReportMap.get(e) != null).forEach(e -> newHomeworkShareReport.getFocusPart().add(studentReportMap.get(e)));
//            }
//
//            if (studyMaster.getPositiveList() != null) {
//                studyMaster.getPositiveList().stream().filter(e -> studentReportMap.get(e) != null).forEach(e -> newHomeworkShareReport.getPositivePart().add(studentReportMap.get(e)));
//            }
//        }

        //完成人数
        newHomeworkShareReport.setFinishedNum(finishedNum);
        if (finishedNum != 0) {
            newHomeworkShareReport.setAvgScore(new BigDecimal(totalScore).divide(new BigDecimal(finishedNum), BigDecimal.ROUND_HALF_UP).intValue());
        }
        //平均分和最高分
        String avgScoreStr = needScoreLevel ? ScoreLevel.processLevel(newHomeworkShareReport.getAvgScore()).getLevel() : newHomeworkShareReport.getAvgScore() + "分";
        newHomeworkShareReport.setAvgScoreStr(avgScoreStr);
        String highScoreStr = needScoreLevel ? ScoreLevel.processLevel(newHomeworkShareReport.getHighScore()).getLevel() : newHomeworkShareReport.getHighScore() + "分";
        newHomeworkShareReport.setHighScoreStr(highScoreStr);
        newHomeworkShareReport.setSuccess(true);
        //是否有录音推荐的类型
        boolean hasRecommendType = newHomework.getPractices().stream().anyMatch(content -> VoiceRecommendSupportedTypes.contains(content.getType()));
        //语音推荐
        if (hasRecommendType && newHomework.isHomeworkChecked()) {
            voiceRecommendProcessor.recommendExcellentVoices(userMap, newHomework, newHomeworkResultMap.values(), newHomeworkShareReport.getMapMessage(), cdnUrl);
        }

        //是否分享
        NoticeShareReportToJztCacheManager noticeShareReportToJztCacheManager = newHomeworkCacheService.getNoticeShareReportToJztCacheManager();
        String cacheKey = noticeShareReportToJztCacheManager.getCacheKey(newHomeworkId);
        boolean share = noticeShareReportToJztCacheManager.load(cacheKey) != null;
        newHomeworkShareReport.setShare(share);
        //老师信息
        if (newHomework.getTeacherId() != null) {
            Teacher teacher = teacherLoaderClient.loadTeacher(newHomework.getTeacherId());
            newHomeworkShareReport.setTeacherUrl(NewHomeworkUtils.getUserAvatarImgUrl(cdnUrl, teacher.fetchImageUrl()));
            newHomeworkShareReport.setTeacherId(teacher.getId());
            newHomeworkShareReport.setTeacherName(teacher.fetchRealname());
            newHomeworkShareReport.setTeacherShareMsg("分享本次作业的班级情况，希望咱们班一起加油哦！");
        }
        //学科信息
        newHomeworkShareReport.setHomeworkId(newHomework.getId());
        newHomeworkShareReport.setSubject(newHomework.getSubject());
        if (newHomeworkShareReport.getSubject() != null) {
            newHomeworkShareReport.setSubjectName(newHomeworkShareReport.getSubject().getValue());
        }
        //创建时间
        newHomeworkShareReport.setTime(newHomework.getCreateAt() != null ? DateUtils.dateToString(newHomework.getCreateAt(), "MM.dd") : "");
        //作业讲练测
        IntelligentTeachingReport teachingReport = diagnoseReport.fetchIntelligentTeachingReport(newHomeworkId);
        if (teachingReport != null) {
            newHomeworkShareReport.setIntelligentTeachingReport(teachingReport);
        }
        //优秀趣味配音
        boolean hasDubbingType = newHomework.getPractices().stream().anyMatch(content -> content.getType().equals(ObjectiveConfigType.DUBBING_WITH_SCORE));

        if (hasDubbingType && newHomework.isHomeworkChecked()) {
            dubbingWithScoreRecommendProcessor.recommendExcellentVoices(userMap, newHomework, newHomeworkResultMap.values(), newHomeworkShareReport.getMapMessage());
        }

        //图文入韵优秀跟读作品
        boolean hasWordTeachAndPractice = newHomework.getPractices().stream().anyMatch(content -> content.getType().equals(ObjectiveConfigType.WORD_TEACH_AND_PRACTICE));
        if (hasWordTeachAndPractice && newHomework.isHomeworkChecked()) {
            imageTextRecommendProcessor.recommendExcellentVoices(userMap, newHomework, newHomeworkResultMap.values(), newHomeworkShareReport.getMapMessage());
        }
        return newHomeworkShareReport;
    }

    /**
     * 获取班级的优秀配音
     *
     * @param newHomeworkId
     * @return
     */
    @Override
    public MapMessage getExcellentDubbingStudent(String newHomeworkId) {
        return homeworkReportProcessor.loadDubbingWithScoreVoiceList(newHomeworkId);
    }

    @Override
    public MapMessage loadNewHomeworkReportExamErrorRates(String homeworkId, Long teacherId, boolean isPcWay) {
        MapMessage mapMessage = new MapMessage();
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            logger.error("load NewHomework Report Exam Error Rate failed : hid {},tid {}", homeworkId, teacherId);
            return MapMessage.errorMessage("作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEW_HOMEWORK_TYPE_NOT_EXIST);
        }
        if (CollectionUtils.isEmpty(newHomework.getPractices())) {
            logger.error("load NewHomework Report Exam Error Rate failed : hid {},tid {}", homeworkId, teacherId);
            return MapMessage.errorMessage("这份作业里面不存在习题").setErrorCode(ErrorCodeConstants.ERROR_CODE_MISS_HOMEWORK_PRACTICE);
        }
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacherId, newHomework.getClazzGroupId())) {
            logger.error("load NewHomework Report Exam Error Rate failed : hid {},tid {}", homeworkId, teacherId);
            return MapMessage.errorMessage("没有权限查看此作业报告").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_PERMISSION);
        }
        Long groupId = newHomework.getClazzGroupId();
        GroupMapper group = groupLoaderClient.loadGroup(groupId, false);
        if (group == null) {
            logger.error("load NewHomework Report Exam Error Rate failed : hid {},tid {}", homeworkId, teacherId);
            return MapMessage.errorMessage("班组不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(group.getClazzId());
        if (clazz == null) {
            logger.error("load NewHomework Report Exam Error Rate failed : hid {},tid {}", homeworkId, teacherId);
            return MapMessage.errorMessage("班组不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        try {
            String clazzName = clazz.formalizeClazzName();
            Date startDate = newHomework.getStartTime();
            Map<String, String> objectiveConfigTypes = new LinkedHashMap<>();
            List<String> objectiveConfigTypeRanks = new LinkedList<>();
            Map<String, Object> questionInfoMapper;

            Map<Long, User> userMap = studentLoaderClient
                    .loadGroupStudents(newHomework.getClazzGroupId())
                    .stream()
                    .collect(Collectors
                            .toMap(LongIdEntity::getId, Function.identity()));
            Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), true);
            newHomeworkResultMap = newHomeworkResultMap.values()
                    .stream()
                    .filter(BaseHomeworkResult::isFinished)
                    .collect(Collectors.toMap(BaseHomeworkResult::getUserId, Function.identity()));
            if (MapUtils.isEmpty(newHomeworkResultMap)) {
                mapMessage.setSuccess(false);
                mapMessage.setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
                mapMessage.add("info", "该作业类型还没有学生完成哦！");
                return mapMessage;
            }
            questionInfoMapper = processNewHomework(userMap, newHomeworkResultMap, newHomework, isPcWay);

            boolean includeSubjective = false;
            List<ObjectiveConfigType> subjectTypes = Arrays.asList(ObjectiveConfigType.READ_RECITE, ObjectiveConfigType.NEW_READ_RECITE);
            for (NewHomeworkPracticeContent content : newHomework.getPractices()) {
                ObjectiveConfigType type = content.getType();
                objectiveConfigTypeRanks.add(type.name());
                objectiveConfigTypes.put(type.name(), type.getValue());
                if (subjectTypes.contains(type)) {
                    includeSubjective = true;
                }
            }
            mapMessage.putAll(
                    MapUtils.m(
                            "questionInfoMapper", questionInfoMapper,
                            "objectiveConfigTypes", objectiveConfigTypes,
                            "objectiveConfigTypeRanks", objectiveConfigTypeRanks,
                            "homeworkId", homeworkId,
                            "homeworkType", newHomework.getNewHomeworkType(),
                            "subject", newHomework.getSubject(),
                            "className", clazzName,
                            "startDate", DateUtils.dateToString(startDate, "yyyy年MM月dd日"),
                            "includeSubjective", includeSubjective,
                            "clazzGroupId", newHomework.getClazzGroupId()
                    ));
            mapMessage.setSuccess(true);
        } catch (Exception e) {
            logger.error("get report failed : of hid {} ,tid {}", homeworkId, teacherId, e);
            mapMessage.setSuccess(false).setInfo("获取报告，学生完成情况失败").setErrorCode(ErrorCodeConstants.ERROR_CODE_COMMON);
        }
        return mapMessage;
    }

    @Override
    public MapMessage loadNewHomeworkReportExamErrorRates(String homeworkId, Long studentId, Long teacherId) {
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEW_HOMEWORK_TYPE_NOT_EXIST);
        }
        if (CollectionUtils.isEmpty(newHomework.getPractices())) {
            return MapMessage.errorMessage("这份作业里面不存在习题").setErrorCode(ErrorCodeConstants.ERROR_CODE_MISS_HOMEWORK_PRACTICE);
        }
        Long groupId = newHomework.getClazzGroupId();
        GroupMapper group = groupLoaderClient.loadGroup(groupId, false);
        if (group == null) {
            return MapMessage.errorMessage("班组不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(group.getClazzId());
        if (clazz == null) {
            return MapMessage.errorMessage("班级不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        MapMessage mapMessage = new MapMessage();
        try {
            String clazzName;
            Date startDate;
            if (teacherId != null) {
                if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacherId, newHomework.getClazzGroupId())) {
                    return MapMessage.errorMessage("没有权限查看此作业报告").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_PERMISSION);
                }
            }
            Map<Long, User> userMap = studentLoaderClient
                    .loadGroupStudents(newHomework.getClazzGroupId())
                    .stream()
                    .collect(Collectors.toMap(LongIdEntity::getId, Function.identity()));
            if (!userMap.containsKey(studentId)) {
                return MapMessage.errorMessage("该学生不在该作业的班级").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_PERMISSION);
            }
            NewHomework.Location location = newHomework.toLocation();
            String day = DayRange.newInstance(location.getCreateTime()).toString();
            NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, location.getSubject(), location.getId(), studentId.toString());
            NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loads(Collections.singletonList(id.toString()), true).get(id.toString());
            if (newHomeworkResult == null
                    || MapUtils.isEmpty(newHomeworkResult.getPractices())) {
                return MapMessage.errorMessage("学生还未开始做练习").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
            }
            if (!newHomeworkResult.isFinished()) {
                return MapMessage.errorMessage("学生还未完成练习").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
            }
            User user = userMap.get(studentId);
            clazzName = clazz.formalizeClazzName();
            startDate = newHomework.getStartTime();
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
            mapMessage.add("clazzGroupId", newHomework.getClazzGroupId());
            mapMessage.add("className", clazzName);
            mapMessage.add("startDate", DateUtils.dateToString(startDate, "yyyy年MM月dd日"));
            mapMessage.add("showCorrect", true);
            mapMessage.add("showCorrectInfo", "");
            mapMessage.add("homeworkType", newHomework.getNewHomeworkType());
            mapMessage.add("subject", newHomework.getSubject());
            mapMessage.setSuccess(true);
        } catch (Exception e) {
            mapMessage.setSuccess(false).setInfo("获取报告，学生完成情况失败").setErrorCode(ErrorCodeConstants.ERROR_CODE_COMMON);
        }
        return mapMessage;
    }

    private Map<ObjectiveConfigType, Object> processNewHomework(User user, NewHomework newHomework, NewHomeworkResult newHomeworkResult) {
        // 处理学生的做题结果，最里面的map为NewHomeworkProcessResult的简版
        Map<ObjectiveConfigType, NewHomeworkPracticeContent> map = newHomework.findPracticeContents();
        List<String> allQuestionIds = newHomework.findAllQuestionIds();
        Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(allQuestionIds);
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        List<String> newHomeworkProcessIds = newHomeworkResult.findAllHomeworkProcessIds(true);
        Map<String, NewHomeworkProcessResult> allProcessResultMap = newHomeworkProcessResultLoader.loads(newHomeworkResult.getHomeworkId(), newHomeworkProcessIds);
        ReportPersonalRateContext reportRateContext =
                new ReportPersonalRateContext(user,
                        newHomeworkResult,
                        newHomework,
                        allQuestionMap,
                        allProcessResultMap,
                        contentTypeMap);
        for (Map.Entry<ObjectiveConfigType, NewHomeworkPracticeContent> entry : map.entrySet()) {
            ObjectiveConfigType key = entry.getKey();
            reportRateContext.setType(key);
            ProcessNewHomeworkAnswerDetailTemplate template = processNewHomeworkAnswerDetailFactory.getTemplate(key);
            if (template != null) {
                template.processNewHomeworkAnswerDetailPersonal(reportRateContext);
            }
        }
        return reportRateContext.getResultMap();
    }

    @Override
    public MapMessage personalWordRecognitionAndReading(String hid, String questionBoxId, Long sid) {
        NewHomework newHomework = newHomeworkLoader.load(hid);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业ID错误");
        }
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.WORD_RECOGNITION_AND_READING);
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
            return MapMessage.errorMessage("newHomework错误");
        }
        NewHomework.Location location = newHomework.toLocation();
        String day = DayRange.newInstance(location.getCreateTime()).toString();
        NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, location.getSubject(), location.getId(), sid.toString());
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loads(Collections.singletonList(id.toString()), true).get(id.toString());
        if (newHomeworkResult == null || MapUtils.isEmpty(newHomeworkResult.getPractices())) {
            return MapMessage.errorMessage("学生还未开始写作业").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        if (!newHomeworkResult.isFinished()) {
            return MapMessage.errorMessage("学生还未完成作业").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.WORD_RECOGNITION_AND_READING);
        if (newHomeworkResultAnswer == null || MapUtils.isEmpty(newHomeworkResultAnswer.getAppAnswers())) {
            return MapMessage.errorMessage("newHomeworkResult数据错误");
        }
        if (!newHomeworkResultAnswer.getAppAnswers().containsKey(questionBoxId)) {
            return MapMessage.errorMessage("newHomeworkResult数据错误");
        }
        NewHomeworkResultAppAnswer appAnswer = newHomeworkResultAnswer.getAppAnswers().get(questionBoxId);
        if (MapUtils.isEmpty(appAnswer.getAnswers())) {
            return MapMessage.errorMessage("newHomeworkResult数据错误");
        }
        List<WordRecognitionAndReadingDetail> detailList = Lists.newLinkedList();
        try {
            Map<String, NewHomeworkProcessResult> allProcessResultMap = newHomeworkProcessResultLoader.loads(newHomeworkResult.getHomeworkId(), appAnswer.getAnswers().values());
            List<String> allQuestionIds = targetApp.getQuestions().stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList());
            Map<String, NewQuestion> allNewQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(allQuestionIds);
            int order = 0;
            List<String> voicesList = Lists.newLinkedList();
            for (NewHomeworkQuestion newHomeworkQuestion : targetApp.getQuestions()) {
                if (!appAnswer.getAnswers().containsKey(newHomeworkQuestion.getQuestionId()))
                    continue;
                String processId = appAnswer.getAnswers().get(newHomeworkQuestion.getQuestionId());
                if (!allProcessResultMap.containsKey(processId))
                    continue;
                order++;
                WordRecognitionAndReadingDetail detail = new WordRecognitionAndReadingDetail();
                detail.setQuestionId(newHomeworkQuestion.getQuestionId());
                detail.setOrder(order);//取question中index作为顺序
                NewHomeworkProcessResult newHomeworkProcessResult = allProcessResultMap.get(processId);
                List<String> voices = CollectionUtils.isNotEmpty(newHomeworkProcessResult.getOralDetails()) ?
                        newHomeworkProcessResult
                                .getOralDetails()
                                .stream()
                                .flatMap(Collection::stream)
                                .map(o -> VoiceEngineTypeUtils.getAudioUrl(o.getAudio(), newHomeworkProcessResult.getVoiceEngineType()))
                                .collect(Collectors.toList()) :
                        Collections.emptyList();
                detail.setVoices(voices);
                detail.setStandard(SafeConverter.toBoolean(newHomeworkProcessResult.grasp));
                NewQuestion question = allNewQuestionMap.get(detail.getQuestionId());
                if (question != null
                        && question.getContent().getSubContents() != null
                        && question.getContent().getSubContents().size() > 0
                        && question.getContent().getSubContents().get(0).getExtras() != null) {
                    String pinyinMark = question.getContent().getSubContents().get(0).getExtras().get("wordContentPinyinMark");
                    String chineseWordContent = question.getContent().getSubContents().get(0).getExtras().get("chineseWordContent");
                    detail.setPinYinMark(pinyinMark);
                    detail.setChineseWordContent(chineseWordContent);
                }
                detail.setEngineName(newHomeworkProcessResult.getVoiceEngineType());
                newHomeworkProcessResult.getOralDetails()
                        .stream()
                        .flatMap(Collection::stream)
                        .findFirst()
                        .ifPresent(value ->
                                detail.setEngineScore(
                                        CollectionUtils.isNotEmpty(value.getSentences()) ?
                                                value.getSentences().get(0).getScore()
                                                : null));
                PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(newHomeworkProcessResult.getPracticeId());
                detail.setCategoryId(practiceType != null ? practiceType.getCategoryId() : 0L);
                voicesList.addAll(voices);
                detailList.add(detail);
            }
            MapMessage mapMessage = MapMessage.successMessage();
            double value = new BigDecimal(SafeConverter.toInt(appAnswer.getStandardNum()) * 100).divide(new BigDecimal(appAnswer.getAnswers().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            long appDuration = SafeConverter.toLong(appAnswer.processDuration());
            int duration = new BigDecimal(appDuration).divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP).intValue();
            mapMessage.add("duration", NewHomeworkUtils.handlerEnTime(duration));//总时长
            mapMessage.add("standard", value >= NewHomeworkConstants.WORD_RECOGNITION_AND_READING_STANDARD);//达标
            mapMessage.add("standardStr", SafeConverter.toInt(appAnswer.getStandardNum()) + "/" + appAnswer.getAnswers().size() + "字达标");
            mapMessage.add("voicesList", voicesList);
            mapMessage.add("detailList", detailList);
            mapMessage.add("homeworkId", hid);
            mapMessage.add("teacherId", newHomework.getTeacherId());
            mapMessage.add("subject", newHomework.getSubject());
            mapMessage.add("objectiveConfigType", ObjectiveConfigType.WORD_RECOGNITION_AND_READING);
            return mapMessage;

        } catch (Exception e) {
            logger.error("personal WordRecognitionAndReading sid {},hid {},questionBoxId {}", sid, hid, questionBoxId, e);
            return MapMessage.errorMessage("操作失败");
        }
    }

    @Override
    public MapMessage personalOcrMentalArithmetic(String hid, String ocrAnswers, Long sid) {
        NewHomework newHomework = newHomeworkLoader.load(hid);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业ID错误");
        }
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC);
        if (target == null) {
            return MapMessage.errorMessage("作业不存在该类型");
        }
        String[] ocrAnswerArr = ocrAnswers.split(",");
        if (ocrAnswerArr.length < 1) {
            return MapMessage.errorMessage("作业内容不存在");
        }
        List<String> ocrAnswerList = Arrays.asList(ocrAnswerArr);
        try {
            Map<String, SubHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(ocrAnswerList);
            if (MapUtils.isEmpty(processResultMap)) {
                return MapMessage.errorMessage("作业内容不存在");
            }
            List<OcrMentalImageDetail> ocrMentalImageDetails = processResultMap.values()
                    .stream()
                    .map(SubHomeworkProcessResult::getOcrMentalImageDetail)
                    .collect(Collectors.toList());
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("ocrMentalImageDetails", ocrMentalImageDetails);
            return mapMessage;
        } catch (Exception e) {
            logger.error("personal OcrMentalArithmetic sid {},hid {},ocrAnswers {}", sid, hid, ocrAnswers, e);
            return MapMessage.errorMessage("操作失败");
        }
    }

    public MapMessage personalReadReciteWithScore(String hid, String questionBoxId, Long sid) {
        NewHomework newHomework = newHomeworkLoader.load(hid);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业ID错误");
        }
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.READ_RECITE_WITH_SCORE);
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
            return MapMessage.errorMessage("newHomework错误");
        }
        NewHomework.Location location = newHomework.toLocation();
        String day = DayRange.newInstance(location.getCreateTime()).toString();
        NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, location.getSubject(), location.getId(), sid.toString());
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loads(Collections.singletonList(id.toString()), true).get(id.toString());
        if (newHomeworkResult == null || MapUtils.isEmpty(newHomeworkResult.getPractices())) {
            return MapMessage.errorMessage("学生还未开始写作业").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        if (!newHomeworkResult.isFinished()) {
            return MapMessage.errorMessage("学生还未完成作业").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.READ_RECITE_WITH_SCORE);
        if (newHomeworkResultAnswer == null || MapUtils.isEmpty(newHomeworkResultAnswer.getAppAnswers())) {
            return MapMessage.errorMessage("newHomeworkResult数据错误");
        }
        if (!newHomeworkResultAnswer.getAppAnswers().containsKey(questionBoxId)) {
            return MapMessage.errorMessage("newHomeworkResult数据错误");
        }
        NewHomeworkResultAppAnswer appAnswer = newHomeworkResultAnswer.getAppAnswers().get(questionBoxId);
        if (MapUtils.isEmpty(appAnswer.getAnswers())) {
            return MapMessage.errorMessage("newHomeworkResult数据错误");
        }
        try {
            Map<String, NewHomeworkProcessResult> allProcessResultMap = newHomeworkProcessResultLoader.loads(newHomeworkResult.getHomeworkId(), appAnswer.getAnswers().values());

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
                if (!appAnswer.getAnswers().containsKey(newHomeworkQuestion.getQuestionId()))
                    continue;
                String processId = appAnswer.getAnswers().get(newHomeworkQuestion.getQuestionId());
                if (!allProcessResultMap.containsKey(processId))
                    continue;
                NewHomeworkProcessResult newHomeworkProcessResult = allProcessResultMap.get(processId);
                ParagraphDetailed paragraphDetailed = new ParagraphDetailed();
                //设置语音
                paragraphDetailed.setVoices(CollectionUtils.isNotEmpty(newHomeworkProcessResult.getOralDetails()) ?
                        newHomeworkProcessResult
                                .getOralDetails()
                                .stream()
                                .flatMap(Collection::stream)
                                .map(o -> VoiceEngineTypeUtils.getAudioUrl(o.getAudio(), newHomeworkProcessResult.getVoiceEngineType()))
                                .collect(Collectors.toList()) :
                        Collections.emptyList());
                //设置是否达标
                paragraphDetailed.setStandard(SafeConverter.toBoolean(newHomeworkProcessResult.getGrasp()));
                if (paragraphDetailed.isStandard()) {
                    standardCount++;
                }
                int duration = new BigDecimal(SafeConverter.toLong(newHomeworkProcessResult.getDuration())).divide(new BigDecimal(1000), 0, BigDecimal.ROUND_HALF_UP).intValue();
                String durationStr = NewHomeworkUtils.handlerEnTime(duration);
                paragraphDetailed.setDuration(durationStr);
                paragraphDetailed.setQuestionId(newHomeworkQuestion.getQuestionId());
                if (qidToParagraph.containsKey(newHomeworkQuestion.getQuestionId())) {
                    paragraphDetailed.setParagraphOrder(qidToParagraph.get(newHomeworkQuestion.getQuestionId()));
                }
                //设置是否是重点自然段
                paragraphDetailed.setParagraphDifficultyType(SafeConverter.toBoolean(qidToDifficultyType.get(newHomeworkQuestion.getQuestionId())));
                if (CollectionUtils.isNotEmpty(newHomeworkProcessResult.getOralDetails())
                        && CollectionUtils.isNotEmpty(newHomeworkProcessResult.getOralDetails().get(0))
                        && newHomeworkProcessResult.getOralDetails().get(0).get(0) != null) {
                    paragraphDetailed.setSentences(newHomeworkProcessResult.getOralDetails().get(0).get(0).getSentences());
                } else {
                    paragraphDetailed.setSentences(Collections.emptyList());
                }
                paragraphDetailed.setVoiceEngineType(newHomeworkProcessResult.getVoiceEngineType());
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
    public MapMessage fetchPictureBookPlusDubbing(String dubbingId) {
        String[] split = dubbingId.split("__");
        if (split.length != 3) {
            return MapMessage.errorMessage("dubbingId 格式不正确");
        }
        long sid = SafeConverter.toLong(split[2]);
        if (sid <= 0) {
            return MapMessage.errorMessage("sid is error");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);
        if (studentDetail == null) {
            return MapMessage.errorMessage("学生不存在");
        }
        PictureBookPlus pictureBookPlus = pictureBookPlusServiceClient.loadByIds(Collections.singleton(split[1])).get(split[1]);
        if (pictureBookPlus == null) {
            return MapMessage.errorMessage("绘本不存在");
        }

        PictureBookPlusDubbing pictureBookPlusDubbing = pictureBookPlusDubbingDao.load(dubbingId);
        if (pictureBookPlusDubbing == null) {
            return MapMessage.errorMessage("视频已不存在");
        }
        String imageUrl = studentDetail.fetchImageUrl();
        String userName = studentDetail.fetchRealnameIfBlankId();
        AppOralScoreLevel scoreLevel = pictureBookPlusDubbing.getScoreLevel();
        String pictureName = pictureBookPlus.getEname();
        String coverUrl = pictureBookPlus.getCoverUrl();

        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("imageUrl", imageUrl)
                .add("userName", userName)
                .add("pictureName", pictureName)
                .add("coverUrl", coverUrl)
                .add("scoreLevel", scoreLevel != null ? scoreLevel.getDesc() : null)
                .add("userId", sid);
        return mapMessage;
    }


    /**
     * 留了个接口这个方法仅仅用于
     */
    public Map<String, Map<String, Object>> lessonDataForBasicApp(NewHomeworkResult newHomeworkResult, List<NewHomeworkApp> apps, Boolean flag, ObjectiveConfigType objectiveConfig) {
        return processNewHomeworkAnswerDetailBasicAppTemplate.lessonDataForBasicApp(newHomeworkResult, apps, flag, objectiveConfig);
    }


    @Override
    public JztReport buildNewHomeworkReportV1(NewHomeworkResult newHomeworkResult, User parent, NewHomework newHomework, StudentDetail studentDetail) {
        NewHomeworkReportForParentTemple newHomeworkReportForParentTemple = newHomeworkReportForParentFactory.getTemplate(newHomework.getSubject());
        return newHomeworkReportForParentTemple.newDoLoadNewHomeworkDetailForParent(newHomeworkResult, parent, studentDetail, newHomework);
    }

    @Override
    public MapMessage semesterChildren(User parent) {
        String defaultUrl = "upload/images/avatar/avatar_normal.gif";
        List<ChildrenFromParent> childrenFromParents = studentLoaderClient.loadParentStudents(parent.getId())
                .stream()
                .map(o -> {
                    ChildrenFromParent childrenFromParent = new ChildrenFromParent();
                    String imageUrl = o.fetchImageUrl();
                    if (StringUtils.isBlank(imageUrl)) {
                        imageUrl = defaultUrl;
                    } else {
                        imageUrl = "gridfs/" + imageUrl;
                    }
                    childrenFromParent.setImageUrl(imageUrl);
                    childrenFromParent.setStudentId(o.getId());
                    childrenFromParent.setStudentName(o.fetchRealname());
                    return childrenFromParent;
                })
                .collect(Collectors.toList());
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.set("childrenFromParents", childrenFromParents);
        return mapMessage;
    }

    @Override
    public SemesterReport semesterReport(Long studentId, String subject) {
        FetchStudentSemesterReportTemple fetchStudentSemesterReportTemple = fetchStudentSemesterReportFactory.getTemplate(Subject.of(subject));
        if (fetchStudentSemesterReportTemple == null) {
            return null;
        } else {
            return fetchStudentSemesterReportTemple.doFetchSemesterReport(studentId, subject);
        }
    }


    @Override
    public String fetchStudentNewestUnfinishedHomework(Long studentId, Collection<Long> groupIds) {
        if (studentId == null || groupIds == null) {
            logger.warn("参数错误");
            return null;
        }
        Date endTime = new Date();
        //获取班组获取作业ID
        List<NewHomework.Location> locations = newHomeworkLoader.loadNewHomeworksByClazzGroupIds(groupIds).values()
                .stream()
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(locations)) {
            return null;
        }
        //过滤掉补做和检查的作业，另外时间排序
        locations = locations.stream()
                .filter(o -> !o.isChecked())
                .filter(o -> o.getEndTime() > endTime.getTime())
                .filter(o -> o.getCreateTime() > NewHomeworkConstants.ALLOW_UPDATE_HOMEWORK_START_TIME.getTime())
                .sorted((o1, o2) -> Long.compare(o2.getStartTime(), o1.getStartTime()))
                .collect(Collectors.toList());
        //形成作业ID和作业中间结果的ID的Map
        Map<String, String> hidToResultId = locations.stream()
                .collect(Collectors
                        .toMap(NewHomework.Location::getId, location -> {
                            String day = DayRange.newInstance(location.getCreateTime()).toString();
                            SubHomeworkResult.ID id = new SubHomeworkResult.ID(day, location.getSubject(), location.getId(), studentId.toString());
                            return id.toString();
                        }));
        //查询中间数据
        Map<String, SubHomeworkResult> subHomeworkResultMap = newHomeworkResultLoader.loadSubHomeworkResults(hidToResultId.values());
        String homeworkId = null;
        //循环得到第一份未完成的作业ID
        for (NewHomework.Location location : locations) {
            String s = hidToResultId.get(location.getId());
            SubHomeworkResult subHomeworkResult = subHomeworkResultMap.get(s);
            if (subHomeworkResult == null || !subHomeworkResult.isFinished()) {
                homeworkId = location.getId();
                break;
            }
        }
        return homeworkId;
    }


    @Override
    public MapMessage fetchNewHomeworkCommonObjectiveConfigTypePart(Teacher teacher, String hid, ObjectiveConfigType objectiveConfigType, ObjectiveConfigTypeParameter parameter) {
        try {
            NewHomework newHomework = newHomeworkLoader.load(hid);
            if (newHomework == null) {
                logger.error("fetch NewHomework CommonObjectiveConfigType Part failed : hid {},type {},parameter {},tid {}", hid, objectiveConfigType, JsonUtils.toJson(parameter), teacher.getId());
                return MapMessage.errorMessage("作业不存在");
            }
            NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(objectiveConfigType);
            if (target == null) {
                logger.error("fetch NewHomework CommonObjectiveConfigType Part failed : hid {},type {},parameter {}", hid, objectiveConfigType, JsonUtils.toJson(parameter));
                return MapMessage.errorMessage("作业不包含" + objectiveConfigType.getValue());
            }
            if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), newHomework.getClazzGroupId())) {
                return MapMessage.errorMessage("没有权限查看此作业报告").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_PERMISSION);
            }
            //获取班级学生
            Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                    .stream()
                    .collect(Collectors
                            .toMap(LongIdEntity::getId, Function.identity()));

            //取学生作业中间结果数据（根据完成作业形式过滤）
            Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), false)
                    .values()
                    .stream()
                    .filter(o -> o.isFinishedOfObjectiveConfigType(objectiveConfigType))
                    .collect(Collectors.toMap(NewHomeworkResult::getUserId, Function.identity()));

            //数据环境准备
            ObjectiveConfigTypePartContext context = new ObjectiveConfigTypePartContext(teacher,
                    objectiveConfigType,
                    newHomeworkResultMap,
                    newHomework,
                    parameter,
                    userMap,
                    target);

            //模板处理
            ProcessNewHomeworkAnswerDetailTemplate template = processNewHomeworkAnswerDetailFactory.getTemplate(objectiveConfigType);
            if (template != null) {
                template.fetchNewHomeworkCommonObjectiveConfigTypePart(context);
                return context.getMapMessage();
            } else {
                logger.error("fetch NewHomework CommonObjectiveConfigType Part failed : hid {},type {},parameter {},tid {}", hid, objectiveConfigType, JsonUtils.toJson(parameter), teacher.getId());
                return MapMessage.errorMessage("模板缺失");
            }
        } catch (Exception e) {
            logger.error("fetch NewHomework CommonObjectiveConfigType Part failed : hid {},type {},parameter {},tid {}", hid, objectiveConfigType, JsonUtils.toJson(parameter), teacher.getId());
            return MapMessage.errorMessage();
        }
    }


    @Override
    public MapMessage fetchNewHomeworkSingleQuestionPart(Teacher teacher, String hid, ObjectiveConfigType objectiveConfigType, String qid, String stoneDataId) {
        NewHomework newHomework = newHomeworkLoader.load(hid);
        //****** begin 校验 ********//
        if (newHomework == null) {
            logger.error("fetch NewHomework Single QuestionP art failed : hid {},type {},qid {},tid {}", hid, objectiveConfigType, qid, teacher.getId());
            return MapMessage.errorMessage("作业不存在");
        }
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(objectiveConfigType);
        if (target == null) {
            logger.error("fetch NewHomework Single QuestionP art failed : hid {},type {},qid {},tid {}", hid, objectiveConfigType, qid, teacher.getId());
            return MapMessage.errorMessage("作业不包含" + objectiveConfigType.getValue());
        }
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), newHomework.getClazzGroupId())) {
            return MapMessage.errorMessage("没有权限查看此作业报告").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_PERMISSION);
        }
        //****** end 校验 ********//

        //****** begin 构件初始化数据****//
        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                .stream()
                .collect(Collectors
                        .toMap(LongIdEntity::getId, Function.identity()));
        Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), false)
                .values()
                .stream()
                .filter(o -> o.isFinishedOfObjectiveConfigType(objectiveConfigType))
                .collect(Collectors.toMap(NewHomeworkResult::getUserId, Function.identity()));
        ObjectiveConfigTypePartContext context = new ObjectiveConfigTypePartContext(teacher,
                objectiveConfigType,
                newHomeworkResultMap,
                newHomework,
                null,
                userMap,
                target);
        context.setQuestionId(qid);
        context.setStoneDataId(stoneDataId);
        //****** end 构件初始化数据****//

        ProcessNewHomeworkAnswerDetailTemplate template = processNewHomeworkAnswerDetailFactory.getTemplate(objectiveConfigType);
        if (template != null) {
            template.fetchNewHomeworkSingleQuestionPart(context);
            return context.getMapMessage();
        } else {
            logger.error("fetch NewHomework Single QuestionP art failed : hid {},type {},qid {},tid {}", hid, objectiveConfigType, qid, teacher.getId());
            return MapMessage.errorMessage("模板缺失");
        }
    }

    @Override
    public MapMessage fetchAppNewHomeworkUnCorrectStudentDetail(String hid, Teacher teacher) {
        try {
            NewHomework newHomework = newHomeworkLoader.load(hid);
            if (newHomework == null) {
                logger.error("fetch App NewHomework UnCorrect StudentDetail failed : hid {},tid {}", hid, teacher.getId());
                return MapMessage.errorMessage("作业不存在");
            }
            if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), newHomework.getClazzGroupId())) {
                return MapMessage.errorMessage("没有权限查看此作业报告").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_PERMISSION);
            }
            Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                    .stream()
                    .collect(Collectors
                            .toMap(LongIdEntity::getId, Function.identity()));
            Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), false);
            List<String> homeworkToSelfStudyIds = newHomeworkResultMap.values()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(o -> newHomework.getId() + "_" + o.getUserId()).collect(Collectors.toList());

            Map<String, HomeworkSelfStudyRef> refMap = (homeworkSelfStudyRefDao.loads(homeworkToSelfStudyIds));

            // 获取订正作业id，用于拿到订正报告
            List<String> selfStudyIds = refMap.values()
                    .stream()
                    .filter(o -> StringUtils.isNotBlank(o.getSelfStudyId()))
                    .map(HomeworkSelfStudyRef::getSelfStudyId)
                    .collect(Collectors.toList());

            Map<String, SelfStudyHomeworkReport> selfStudyHomeworkReportMap = selfStudyHomeworkReportDao.loads(selfStudyIds);
            List<Map<String, Object>> users = new LinkedList<>();
            for (NewHomeworkResult r : newHomeworkResultMap.values()) {
                if (!r.isFinished()) continue;
                boolean flag = true;
                String s = newHomework.getId() + "_" + r.getUserId();
                if (refMap.containsKey(s)) {
                    HomeworkSelfStudyRef homeworkSelfStudyRef = refMap.get(s);
                    flag = !StringUtils.isBlank(homeworkSelfStudyRef.getSelfStudyId()) && selfStudyHomeworkReportMap.containsKey(homeworkSelfStudyRef.getSelfStudyId());
                }
                if (!flag) {
                    users.add(MapUtils.m(
                            "userId", r.getUserId(),
                            "userName", userMap.containsKey(r.getUserId()) ? userMap.get(r.getUserId()).fetchRealname() : ""
                    ));
                }

            }

            UrgeNewHomeworkUnCorrectCacheManager urgeNewHomeworkUnCorrectCacheManager = newHomeworkCacheService.getUrgeNewHomeworkUnCorrectCacheManager();
            String cacheKey = urgeNewHomeworkUnCorrectCacheManager.getCacheKey(hid);
            boolean isCorrected = urgeNewHomeworkUnCorrectCacheManager.load(cacheKey) != null;

            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("users", users);
            mapMessage.add("isCorrected", isCorrected);

            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch App NewHomework UnCorrect StudentDetail failed : hid {},tid {}", hid, teacher.getId(), e);
            return MapMessage.errorMessage();
        }
    }


    @Override
    public MapMessage fetchAppNewHomeworkUnFinishStudentDetail(String hid, Teacher teacher) {
        try {
            NewHomework newHomework = newHomeworkLoader.load(hid);
            if (newHomework == null) {
                logger.error("fetch App NewHomework UnFinish StudentDetail failed : hid {},tid {}", hid, teacher.getId());
                return MapMessage.errorMessage("作业不存在");
            }
            if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), newHomework.getClazzGroupId())) {
                logger.error("fetch App NewHomework UnFinish StudentDetail failed : hid {},tid {}", hid, teacher.getId());
                return MapMessage.errorMessage("没有权限查看此作业报告").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_PERMISSION);
            }
            Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                    .stream()
                    .collect(Collectors
                            .toMap(LongIdEntity::getId, Function.identity()));
            Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), false);
            List<Map<String, Object>> users = new LinkedList<>();
            for (User u : userMap.values()) {
                boolean flag = false;
                if (newHomeworkResultMap.containsKey(u.getId())) {
                    flag = newHomeworkResultMap.get(u.getId()).isFinished();
                }
                if (!flag) {
                    users.add(MapUtils.m(
                            "userId", u.getId(),
                            "userName", u.fetchRealname()
                    ));
                }
            }
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("users", users);

            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch App NewHomework UnFinish StudentDetail failed : hid {},tid {}", hid, teacher.getId(), e);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage fetchAppNewHomeworkStudentDetailOpenTable(String hid, Teacher teacher) {
        try {
            NewHomework newHomework = newHomeworkLoader.load(hid);
            //********** begin 校验：（1）作业是否存在；（2）老师是否存在权限 //
            if (newHomework == null) {
                logger.error("fetch App NewHomework Student Detail OpenTable failed : hid {},tid {}", hid, teacher.getId());
                return MapMessage.errorMessage("作业不存在");
            }
            if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), newHomework.getClazzGroupId())) {
                logger.error("fetch App NewHomework Student Detail OpenTable failed : hid {},tid {}", hid, teacher.getId());
                return MapMessage.errorMessage("没有权限查看此作业报告").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_PERMISSION);
            }
            //********** end 校验：（1）作业是否存在；（2）老师是否存在权限 //


            //********** begin 初始化数据准备：（1）userMap；（2）newHomeworkResultMap （3）refMap （4）selfStudyHomeworkReportMap //
            Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                    .stream()
                    .collect(Collectors
                            .toMap(LongIdEntity::getId, Function.identity()));
            List<ObjectiveConfigType> types = newHomework.getPractices().stream().map(NewHomeworkPracticeContent::getType).collect(Collectors.toList());
            Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), false);
            Map<String, HomeworkSelfStudyRef> refMap = new LinkedHashMap<>();
            Map<String, SelfStudyHomeworkReport> selfStudyHomeworkReportMap = new LinkedHashMap<>();

            Map<String, SelfStudyHomework> studyHomeworkMap = new LinkedHashMap<>();
            boolean showCorrect = fetchCorrectInfo(studyHomeworkMap, selfStudyHomeworkReportMap, refMap, types, newHomework, newHomeworkResultMap);
            boolean allSubjective = types.stream()
                    .filter(o -> !NewHomeworkConstants.NOT_SHOW_SCORE_TYPE.contains(o))
                    .allMatch(ObjectiveConfigType::isSubjective);
            List<String> defaultTypeInfo = types.stream().map(o -> "-").collect(Collectors.toList());
            //********** end 初始化数据准备：（1）userMap；（2）newHomeworkResultMap （3）refMap （4）selfStudyHomeworkReportMap //

            //************* begin 数据初始化 AppNewHomeworkStudentDetail.StudentDetail 是返回数据结构，对于每个人的成绩 **************//
            List<AppNewHomeworkStudentDetail.StudentDetail> studentDetails = userMap.values()
                    .stream()
                    .map(o -> {
                        AppNewHomeworkStudentDetail.StudentDetail user = new AppNewHomeworkStudentDetail.StudentDetail();
                        user.setSid(o.getId());
                        user.setSname(o.fetchRealname());
                        user.setTypeInfo(defaultTypeInfo);
                        return user;
                    })
                    .collect(Collectors.toList());
            Map<Long, AppNewHomeworkStudentDetail.StudentDetail> detailMap = studentDetails.stream().collect(Collectors.toMap(AppNewHomeworkStudentDetail.StudentDetail::getSid, Function.identity()));
            //************* end 数据初始化 AppNewHomeworkStudentDetail.StudentDetail 是返回数据结构，对于每个人的成绩 **************//


            //************* begin 数据处理 AppNewHomeworkStudentDetail.StudentDetail 和 NewHomeworkResult 根据userId 一一对于，然后数据处理 **************//
            for (NewHomeworkResult r : newHomeworkResultMap.values()) {
                if (!detailMap.containsKey(r.getUserId())) continue;
                AppNewHomeworkStudentDetail.StudentDetail studentDetail = detailMap.get(r.getUserId());
                if (r.isFinished()) {
                    if (allSubjective) {
                        studentDetail.setScoreStr("--");
                    } else {
                        int score = SafeConverter.toInt(r.processScore());
                        studentDetail.setScore(score);
                        studentDetail.setScoreStr(score + "分");
                    }
                    long duration = new BigDecimal(SafeConverter.toLong(r.processDuration())).divide(new BigDecimal(60), 0, BigDecimal.ROUND_UP).longValue();
                    studentDetail.setDuration(duration);
                    studentDetail.setDurationStr(duration + "分钟");
                    boolean notShowDuration = types.stream().allMatch(o -> o.equals(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC));//只包含纸质口算
                    if (notShowDuration) {
                        studentDetail.setDuration(0);
                        studentDetail.setDurationStr("-");
                    }
                    studentDetail.setFinished(true);
                    studentDetail.setFinishAt(r.getFinishAt());
                    studentDetail.setFinishStr(DateUtils.dateToString(r.getFinishAt(), "MM月dd日 HH:mm"));
                    studentDetail.setRepair(r.getRepair());
                    if (showCorrect) {
                        String s = newHomework.getId() + "_" + r.getUserId();
                        if (refMap.containsKey(s) && studyHomeworkMap.containsKey(refMap.get(s).getSelfStudyId())) {
                            HomeworkSelfStudyRef homeworkSelfStudyRef = refMap.get(s);
                            SelfStudyHomework selfStudyHomework = studyHomeworkMap.get(refMap.get(s).getSelfStudyId());
                            studentDetail.setWrongNum(selfStudyHomework.findAllQuestionIds().size());
                            if (StringUtils.isBlank(homeworkSelfStudyRef.getSelfStudyId())) {
                                studentDetail.setCorrectInfo("未订正");
                                studentDetail.setCorrectRate("未订正");
                            } else {
                                SelfStudyHomeworkReport selfStudyHomeworkReport = selfStudyHomeworkReportMap.get(homeworkSelfStudyRef.getSelfStudyId());
                                if (selfStudyHomeworkReport != null) {
                                    String correctInfo = "100%";
                                    if (selfStudyHomeworkReport.getPractices() != null) {
                                        List<SelfStudyHomeworkReportQuestion> selfStudyHomeworkReportQuestions = new LinkedList<>();
                                        for (LinkedHashMap<String, SelfStudyHomeworkReportQuestion> sh : selfStudyHomeworkReport.getPractices().values()) {
                                            selfStudyHomeworkReportQuestions.addAll(sh.values());
                                        }
                                        if (selfStudyHomeworkReportQuestions.size() != 0) {
                                            long num = selfStudyHomeworkReportQuestions.stream()
                                                    .filter(o -> SafeConverter.toBoolean(o.getGrasp()))
                                                    .count();
                                            correctInfo = new BigDecimal(num * 100).divide(new BigDecimal(selfStudyHomeworkReportQuestions.size()), BigDecimal.ROUND_HALF_UP, 0).intValue() + "%";
                                        }
                                    }
                                    studentDetail.setCorrectInfo("已订正");
                                    studentDetail.setFinishCorrect(true);
                                    studentDetail.setCorrectRate(correctInfo);
                                } else {
                                    studentDetail.setCorrectInfo("未订正");
                                    studentDetail.setCorrectRate("未订正");
                                }
                            }
                        } else {
                            studentDetail.setCorrectInfo("无需订正");
                            studentDetail.setCorrectRate("无需订正");
                        }
                    }
                }
                List<String> typeInfos = new LinkedList<>();
                for (ObjectiveConfigType type : types) {
                    if (!r.isFinishedOfObjectiveConfigType(type)) {
                        typeInfos.add("-");
                    } else {
                        NewHomeworkResultAnswer newHomeworkResultAnswer = r.getPractices().get(type);
                        //各个类型表格分数表示
                        ProcessNewHomeworkAnswerDetailTemplate template = processNewHomeworkAnswerDetailFactory.getTemplate(type);
                        if (template != null) {
                            typeInfos.add(template.processStudentPartTypeScore(newHomework, newHomeworkResultAnswer, type));
                        } else {
                            typeInfos.add("暂不支持");
                        }
                    }
                    studentDetail.setTypeInfo(typeInfos);
                }
            }
            //************* end 数据处理 AppNewHomeworkStudentDetail.StudentDetail 和 NewHomeworkResult 根据userId 一一对于，然后数据处理 **************//


            //*** begin 排序：（1）分数降序；（2）用时升序；（3）完成时间 **********//
            studentDetails.sort((o1, o2) -> {
                int compare = Integer.compare(o2.getScore(), o1.getScore());
                if (compare != 0) {
                    return compare;
                }
                compare = Long.compare(o1.getDuration(), o2.getDuration());
                if (compare != 0) {
                    return compare;
                }
                long t1 = o1.getFinishAt() != null ? o1.getFinishAt().getTime() : Long.MAX_VALUE;
                long t2 = o2.getFinishAt() != null ? o2.getFinishAt().getTime() : Long.MAX_VALUE;
                return Long.compare(t1, t2);
            });

            //*** end 排序：（1）分数降序；（2）用时升序；（3）完成时间 **********//

            List<String> columnName = types.stream().map(ObjectiveConfigType::getValue).collect(Collectors.toList());
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("studentDetails", studentDetails);
            mapMessage.add("columnName", columnName);
            mapMessage.add("showCorrect", showCorrect);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch App NewHomework Student Detail OpenTable failed : hid {},tid {}", hid, teacher.getId(), e);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage fetchAppNewHomeworkTypeQuestion(String hid, Teacher teacher, String cdnBaseUrl) {

        try {
            NewHomework newHomework = newHomeworkLoader.load(hid);
            //********** begin 校验：（1）作业是否存在；（2）老师是否存在权限 //
            if (newHomework == null) {
                logger.error("fetch App NewHomework StudentDetail failed : hid {},tid {}", hid, teacher.getId());
                return MapMessage.errorMessage("作业不存在");
            }
            if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), newHomework.getClazzGroupId())) {
                logger.error("fetch App NewHomework StudentDetail failed : hid {},tid {}", hid, teacher.getId());
                return MapMessage.errorMessage("没有权限查看此作业报告").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_PERMISSION);
            }
            //********** end 校验：（1）作业是否存在；（2）老师是否存在权限 //

            //********
            Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                    .stream()
                    .collect(Collectors
                            .toMap(LongIdEntity::getId, Function.identity()));
            List<ObjectiveConfigType> types = newHomework.getPractices().stream().map(NewHomeworkPracticeContent::getType).collect(Collectors.toList());
            Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), false);


            //需要process的类型
            //用过滤的原因，新增类型的时候，宁可是多一种，而不要没有导致出问题
            Set<ObjectiveConfigType> needAnswer = new LinkedHashSet<>();
            if (CollectionUtils.isNotEmpty(newHomework.getPractices())) {
                needAnswer = newHomework.getPractices()
                        .stream()
                        .map(NewHomeworkPracticeContent::getType)
                        .filter(o -> !NewHomeworkConstants.GenerateQuestionPartHomeworkConfigTypesForReport.contains(o))
                        .collect(Collectors.toSet());
            }
            Map<ObjectiveConfigType, List<NewHomework.NewHomeworkQuestionObj>> objectiveConfigTypeListMap = newHomework.processSubHomeworkResultAnswerIds(needAnswer);
            String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
            List<String> subHomeworkResultAnswerIds = new LinkedList<>();
            for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
                if (MapUtils.isEmpty(newHomeworkResult.getPractices())) {
                    continue;
                }
                for (Map.Entry<ObjectiveConfigType, NewHomeworkResultAnswer> entry : newHomeworkResult.getPractices().entrySet()) {
                    if (!objectiveConfigTypeListMap.containsKey(entry.getKey()) || !entry.getValue().isFinished()) {
                        continue;
                    }
                    for (NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj : objectiveConfigTypeListMap.get(entry.getKey())) {
                        subHomeworkResultAnswerIds.add(newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId()));
                    }
                }
            }
            Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);
            List<String> processIds = subHomeworkResultAnswerMap.values().stream().map(SubHomeworkResultAnswer::getProcessId).collect(Collectors.toList());
            Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(hid, processIds);
            TypePartContext typePartContext = new TypePartContext(newHomework, newHomeworkResultMap, newHomeworkProcessResultMap, cdnBaseUrl);
            for (NewHomeworkPracticeContent content : newHomework.getPractices()) {
                AppObjectiveConfigTypeProcessorTemplate template = appObjectiveConfigTypeProcessorFactory.getTemplate(content.getType());
                typePartContext.setType(content.getType());
                template.fetchTypePart(typePartContext);
            }

            MapMessage mapMessage = MapMessage.successMessage();
            List<Object> data = types.stream().filter(typePartContext.getResult()::containsKey).map(typePartContext.getResult()::get).collect(Collectors.toList());
            String shareReportUrl = UrlUtils.buildUrlQuery("/view/reportv5/share",
                    MapUtils.m(
                            "subject", newHomework.getSubject(),
                            "homeworkIds", newHomework.getId()));
            mapMessage.add("shareReportUrl", shareReportUrl);
            mapMessage.add("checked", SafeConverter.toBoolean(newHomework.getChecked()));
            mapMessage.add("showCorrect", false);
            mapMessage.add("data", data);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch App NewHomework StudentDetail failed : hid {},tid {}", hid, teacher.getId(), e);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage fetchReadReciteQuestionBoxIdDetail(String hid, String questionBoxId, ObjectiveConfigType type) {
        try {
            NewHomework newHomework = newHomeworkLoader.load(hid);
            if (newHomework == null) {
                logger.error("fetch ReadRecite QuestionBoxId Detail failed : hid {}", hid);
                return MapMessage.errorMessage("作业不存在");
            }
            NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
            if (target == null) {
                logger.error("fetch ReadRecite QuestionBoxId Detail failed : hid {}", hid);
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
                logger.error("fetch ReadRecite QuestionBoxId Detail failed : hid {}", hid);
                return MapMessage.errorMessage("不存在对应朗读背诵部分" + questionBoxId);
            }
            Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(Collections.singleton(targetApp.getLessonId()));
            List<String> qids = targetApp.getQuestions().stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList());
            Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(qids);
            List<Long> chineseSentenceIds = questionMap.values()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(question -> CollectionUtils.isNotEmpty(question.getSentenceIds()))
                    .map(o -> o.getSentenceIds().get(0))
                    .collect(Collectors.toList());

            List<ChineseSentence> chineseSentences = chineseContentLoaderClient.loadChineseSentenceByIds(chineseSentenceIds);
            //构件自然段信息Map<Qid:自然段>
            Map<String, String> qidToParagraph = new LinkedHashMap<>();
            Map<String, Boolean> qidToDiffMap = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(chineseSentences)) {
                Map<Long, ChineseSentence> mapChineseSentences = chineseSentences.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(AbstractDatabaseEntity::getId, Function.identity()));
                for (NewQuestion newQuestion : questionMap.values()) {
                    if (CollectionUtils.isEmpty(newQuestion.getSentenceIds()))
                        continue;
                    List<Long> sentenceIds = newQuestion.getSentenceIds();
                    if (!mapChineseSentences.containsKey(sentenceIds.get(0)))
                        continue;
                    ChineseSentence chineseSentence = mapChineseSentences.get(sentenceIds.get(0));
                    qidToParagraph.put(newHomework.getId(), SafeConverter.toString(chineseSentence.getParagraph(), ""));
                    qidToDiffMap.put(newQuestion.getId(), chineseSentence.getReciteParagraph());
                }
            }
            NewReadReciteAppPart newReadReciteAppPart = new NewReadReciteAppPart();
            if (newBookCatalogMap.containsKey(targetApp.getLessonId())) {
                newReadReciteAppPart.setParagraphName(newBookCatalogMap.get(targetApp.getLessonId()).getName());
            }
            newReadReciteAppPart.setQuestionBoxType(targetApp.getQuestionBoxType());
            for (String qid : qids) {
                if (!questionMap.containsKey(qid)) continue;
                NewReadReciteAppPart.ParagraphDetailed paragraphDetailed = new NewReadReciteAppPart.ParagraphDetailed();
                newReadReciteAppPart.getParagraphDetaileds().add(paragraphDetailed);
                paragraphDetailed.setQuestionId(qid);
                if (qidToParagraph.containsKey(qid)) {
                    paragraphDetailed.setParagraphOrder(qidToParagraph.get(qid));

                }
                paragraphDetailed.setParagraphDifficultyType(SafeConverter.toBoolean(qidToDiffMap.get(qid)));
            }
            newReadReciteAppPart.setQuestionBoxType(targetApp.getQuestionBoxType());
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("newReadReciteAppPart", newReadReciteAppPart);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch ReadRecite QuestionBoxId Detail failed : hid {}", hid, e);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage urgeNewHomework(String hid, Teacher teacher, Set<Long> sids, boolean isCorrect) {
        NewHomework newHomework = newHomeworkLoader.load(hid);
        if (newHomework == null) {
            logger.error("urge NewHomework failed : hid {}, tid {}, sids {],isCorrect", hid, teacher.getId(), sids, isCorrect);
            return MapMessage.errorMessage();
        }
        //isCorrect 是否是要催促订正

        if (isCorrect) {
            //********** begin 判断是否催促订正了 ***********//
            UrgeNewHomeworkUnCorrectCacheManager urgeNewHomeworkUnCorrectCacheManager = newHomeworkCacheService.getUrgeNewHomeworkUnCorrectCacheManager();
            String cacheKey = urgeNewHomeworkUnCorrectCacheManager.getCacheKey(hid);
            if (urgeNewHomeworkUnCorrectCacheManager.load(cacheKey) != null) {
                return MapMessage.errorMessage("每天只能提醒一次哦~");
            }
            urgeNewHomeworkUnCorrectCacheManager.add(cacheKey, 1);
            //********** end 判断是否催促订正了 ***********//

            //发送消息
//            urgeToCorrectNewHomework(teacher, sids, newHomework);
        } else {
            //********** begin 判断是否催促作业了 ***********//
            UrgeNewHomeworkUnFinishCacheManager urgeNewHomeworkUnFinishCacheManager = newHomeworkCacheService.getUrgeNewHomeworkUnFinishCacheManager();
            String cacheKey = urgeNewHomeworkUnFinishCacheManager.getCacheKey(hid);
            if (urgeNewHomeworkUnFinishCacheManager.load(cacheKey) != null) {
                return MapMessage.errorMessage("每天只能提醒一次哦~");
            }
            urgeNewHomeworkUnFinishCacheManager.add(cacheKey, 1);
            //********** end 判断是否催促作业了 ***********//

            //发送消息
//            urgeToFinishNewHomework(teacher, sids, newHomework);

        }

        //催促push消息
        urgePush(teacher, hid, sids, isCorrect);

        return MapMessage.successMessage();
    }


    //订正相关信息的提取
    //studyHomeworkMap == null 的时候不去查询数据，因为只有一处用到
    private boolean fetchCorrectInfo(Map<String, SelfStudyHomework> studyHomeworkMap, Map<String, SelfStudyHomeworkReport> selfStudyHomeworkReportMap, Map<String, HomeworkSelfStudyRef> refMap, List<ObjectiveConfigType> types, NewHomework newHomework, Map<Long, NewHomeworkResult> newHomeworkResultMap) {
        boolean showCorrect = false;
        boolean isConfigTrue = true;

        try {
            String config = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_TEACHER.name(), "GET_CORRECT_FUN_IS_SHOW");
            isConfigTrue = ConversionUtils.toBool(config);
        } catch (IllegalArgumentException e) {
            logger.info("CommonConfigLoaderClient GET_CORRECT_FUN_IS_SHOW : e{}", e.getMessage());
        }
        if (CollectionUtils.containsAny(GenerateSelfStudyHomeworkConfigTypes, types)
                && NewHomeworkConstants.showWrongQuestionInfo(newHomework.getCreateAt(), RuntimeMode.getCurrentStage())
                && (NewHomeworkUtils.isSubHomework(newHomework.getId()) || NewHomeworkUtils.isShardHomework(newHomework.getId()))
                && NeedSelfStudyHomeworkSubjects.contains(newHomework.getSubject())
                && isConfigTrue
        ) {
            showCorrect = true;
        }
        if (showCorrect) {
            List<String> homeworkToSelfStudyIds = newHomeworkResultMap.values()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(o -> newHomework.getId() + "_" + o.getUserId()).collect(Collectors.toList());

            refMap.putAll(homeworkSelfStudyRefDao.loads(homeworkToSelfStudyIds));

            // 获取订正作业id，用于拿到订正报告
            List<String> selfStudyIds = refMap.values()
                    .stream()
                    .filter(o -> StringUtils.isNotBlank(o.getSelfStudyId()))
                    .map(HomeworkSelfStudyRef::getSelfStudyId)
                    .collect(Collectors.toList());

            selfStudyHomeworkReportMap.putAll(selfStudyHomeworkReportDao.loads(selfStudyIds));
            if (studyHomeworkMap != null) {
                studyHomeworkMap.putAll(selfStudyHomeworkDao.loads(selfStudyIds));
            }

        }
        return showCorrect;
    }


    @Override
    @SuppressWarnings("unchecked")
    public MapMessage fetchAppNewHomeworkStudentDetail(String hid, Teacher teacher) {
        try {
            NewHomework newHomework = newHomeworkLoader.load(hid);
            //******* begin 校验：（1）作业是否存在；（2）老师是否有权限********//
            if (newHomework == null) {
                logger.error("fetch App NewHomework StudentDetail failed : hid {},tid {}", hid, teacher.getId());
                return MapMessage.errorMessage("作业不存在");
            }
            if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), newHomework.getClazzGroupId())) {
                return MapMessage.errorMessage("没有权限查看此作业报告").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_PERMISSION);
            }
            //******* end 校验：（1）作业是否存在；（2）老师是否有权限********//

            //********* begin 数据初始化 ************//
            Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                    .stream()
                    .collect(Collectors
                            .toMap(LongIdEntity::getId, Function.identity()));
            List<ObjectiveConfigType> types = newHomework.getPractices().stream().map(NewHomeworkPracticeContent::getType).collect(Collectors.toList());
            Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), false);
            Map<String, HomeworkSelfStudyRef> refMap = new LinkedHashMap<>();
            Map<String, SelfStudyHomeworkReport> selfStudyHomeworkReportMap = new LinkedHashMap<>();
            //DUBBING 和 READ_RECITE_WITH_SCORE 是两个特殊的类型不需要显示分数，也不需要批改
            //是否有需要批改的
            //新读背，趣味配音不需要批改
            boolean hasSubjective = types.stream()
                    .filter(o -> !NewHomeworkConstants.NOT_SHOW_SCORE_TYPE.contains(o))
                    .anyMatch(ObjectiveConfigType::isSubjective);
            //是否显示分数和平均分
            //新读背，趣味配音不需要显示分数
            boolean allSubjective = types.stream()
                    .filter(o -> !NewHomeworkConstants.NOT_SHOW_SCORE_TYPE.contains(o))
                    .allMatch(ObjectiveConfigType::isSubjective);
            boolean showCorrect = fetchCorrectInfo(null, selfStudyHomeworkReportMap, refMap, types, newHomework, newHomeworkResultMap);

            //********* end 数据初始化 ************//

            //***********  begin AppNewHomeworkStudentDetail 是返回数据的结构：对每个人的数据进行数据构件 *************//
            AppNewHomeworkStudentDetail appNewHomeworkStudentDetail = new AppNewHomeworkStudentDetail();
            for (User u : userMap.values()) {
                AppNewHomeworkStudentDetail.StudentDetail studentDetail = new AppNewHomeworkStudentDetail.StudentDetail();
                studentDetail.setSid(u.getId());
                studentDetail.setSname(u.fetchRealname());
                appNewHomeworkStudentDetail.getStudentDetails().add(studentDetail);
                NewHomeworkResult r = newHomeworkResultMap.get(u.getId());
                if (r != null && r.isFinished()) {
                    if (hasSubjective) {
                        if (!r.isCorrected()) {
                            appNewHomeworkStudentDetail.setUnMarkNum(1 + appNewHomeworkStudentDetail.getUnMarkNum());
                        }
                    }
                    if (allSubjective) {
                        studentDetail.setScoreStr("--");
                    } else {
                        int score = SafeConverter.toInt(r.processScore());
                        studentDetail.setScore(score);
                        studentDetail.setScoreStr(score + "分");
                        appNewHomeworkStudentDetail.setTotalScore(appNewHomeworkStudentDetail.getTotalScore() + score);

                    }
                    long duration = new BigDecimal(SafeConverter.toLong(r.processDuration())).divide(new BigDecimal(60), 0, BigDecimal.ROUND_UP).longValue();
                    studentDetail.setDuration(duration);
                    studentDetail.setDurationStr(duration + "分钟");
                    boolean notShowDuration = types.stream().allMatch(o -> o.equals(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC));//只包含纸质口算
                    if (notShowDuration) {
                        duration = 0;
                        studentDetail.setDurationStr("-");
                    }
                    appNewHomeworkStudentDetail.setFinishedNum(1 + appNewHomeworkStudentDetail.getFinishedNum());
                    appNewHomeworkStudentDetail.setTotalDuration(duration + appNewHomeworkStudentDetail.getTotalDuration());
                    studentDetail.setFinished(true);
                    studentDetail.setFinishAt(r.getFinishAt());
                    studentDetail.setRepair(r.getRepair());
                    String personReportUrl = UrlUtils.buildUrlQuery("/view/reportv5/student/questionsdetail",
                            MapUtils.m(
                                    "subject", newHomework.getSubject(),
                                    "homeworkId", newHomework.getId(),
                                    "studentId", u.getId()));
                    studentDetail.setPersonReportUrl(personReportUrl);
                    if (showCorrect) {
                        String s = newHomework.getId() + "_" + r.getUserId();
                        if (refMap.containsKey(s)) {
                            studentDetail.setNeedCorrect(true);
                            HomeworkSelfStudyRef homeworkSelfStudyRef = refMap.get(s);
                            if (StringUtils.isBlank(homeworkSelfStudyRef.getSelfStudyId())) {
                                studentDetail.setCorrectInfo("未订正");
                            } else {
                                SelfStudyHomeworkReport selfStudyHomeworkReport = selfStudyHomeworkReportMap.get(homeworkSelfStudyRef.getSelfStudyId());
                                if (selfStudyHomeworkReport != null) {
                                    studentDetail.setFinishCorrect(true);
                                    String correctInfo = "100%";
                                    if (selfStudyHomeworkReport.getPractices() != null) {
                                        List<SelfStudyHomeworkReportQuestion> selfStudyHomeworkReportQuestions = new LinkedList<>();
                                        for (LinkedHashMap<String, SelfStudyHomeworkReportQuestion> sh : selfStudyHomeworkReport.getPractices().values()) {
                                            selfStudyHomeworkReportQuestions.addAll(sh.values());
                                        }
                                        if (selfStudyHomeworkReportQuestions.size() != 0) {
                                            long num = selfStudyHomeworkReportQuestions.stream()
                                                    .filter(o -> SafeConverter.toBoolean(o.getGrasp()))
                                                    .count();
                                            correctInfo = new BigDecimal(num * 100).divide(new BigDecimal(selfStudyHomeworkReportQuestions.size()), BigDecimal.ROUND_HALF_UP, 0).intValue() + "%";
                                        }
                                    }
                                    studentDetail.setCorrectInfo(correctInfo);
                                } else {
                                    studentDetail.setCorrectInfo("未订正");
                                }
                            }
                        } else {
                            studentDetail.setNeedCorrect(false);
                            studentDetail.setCorrectInfo("无需订正");
                        }
                    }
                }
            }

            //***********  end AppNewHomeworkStudentDetail 是返回数据的结构：对每个人的数据进行数据构件 *************//

            //******* begin 是否催促没有完成作业的 ****//
            UrgeNewHomeworkUnFinishCacheManager urgeNewHomeworkUnFinishCacheManager = this.newHomeworkCacheService.getUrgeNewHomeworkUnFinishCacheManager();
            String key = urgeNewHomeworkUnFinishCacheManager.getCacheKey(newHomework.getId());
            Integer value = urgeNewHomeworkUnFinishCacheManager.load(key);
            appNewHomeworkStudentDetail.setShowUrgeFinishHomework(Objects.isNull(value));
            //******* end 是否催促没有完成作业的 ****//

            if (showCorrect) {
                //******* begin 是否催促没有完成订正的 ****//
                UrgeNewHomeworkUnCorrectCacheManager urgeNewHomeworkUnCorrectCacheManager = this.newHomeworkCacheService.getUrgeNewHomeworkUnCorrectCacheManager();
                key = urgeNewHomeworkUnCorrectCacheManager.getCacheKey(newHomework.getId());
                value = urgeNewHomeworkUnCorrectCacheManager.load(key);
                appNewHomeworkStudentDetail.setShowUrgeCorrectHomework(Objects.isNull(value));
                //******* end 是否催促没有完成订正的 ****//

                appNewHomeworkStudentDetail.setFinishCorrectNum(selfStudyHomeworkReportMap.size());
                appNewHomeworkStudentDetail.setUnfinishedCorrectNum(refMap.size() - appNewHomeworkStudentDetail.getFinishCorrectNum());
            }

            appNewHomeworkStudentDetail.setShowCorrect(showCorrect);
            //部分结果数据处理:排序，平均分，是否显示催促未完成，未订正
            appNewHomeworkStudentDetail.handlerResult(userMap, newHomework, allSubjective);

            // 做题习惯
            if (SafeConverter.toBoolean(newHomework.getIncludeIntelligentTeaching()) && newHomework.getSubject() == Subject.MATH) {
                String diagnosisHabitUrl = NewHomeworkConstants.DIAGNOSIS_HABIT_TEST_URL;
                if (RuntimeMode.isStaging()) {
                    diagnosisHabitUrl = NewHomeworkConstants.DIAGNOSIS_HABIT_STAGING_URL;
                } else if (RuntimeMode.isProduction()) {
                    diagnosisHabitUrl = NewHomeworkConstants.DIAGNOSIS_HABIT_PRODUCT_URL;
                }
                String url = StringUtils.formatMessage(diagnosisHabitUrl + "homework/report/diagnosis/habit/{}/{}", newHomework.getClazzGroupId(), newHomework.getId());
                AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                        .get(url)
                        .socketTimeout(500)
                        .execute();
                if (response != null && response.getStatusCode() == 200) {
                    Map<String, Object> map = JsonUtils.fromJson(response.getResponseString());
                    if (MapUtils.isNotEmpty(map)) {
                        List<Map<String, Object>> diagnosis = (List<Map<String, Object>>) map.get("diagnosis");
                        if (CollectionUtils.isNotEmpty(diagnosis)) {
                            appNewHomeworkStudentDetail.setDiagnosisHabits(diagnosis);
                        }
                    }
                } else {
                    logger.error("调用:{}失败, response: {}",
                            url,
                            response != null ? response.getResponseString() : "");
                }
            }

            // ************* begin 一些地址传送 ********//
            String correctUrl = UrlUtils.buildUrlQuery("/view/report/correct",
                    MapUtils.m(
                            "homeworkId", newHomework.getId(),
                            "subject", newHomework.getSubject(),
                            "homeworkType", newHomework.getNewHomeworkType()
                    ));
            String detailUrl = UrlUtils.buildUrlQuery("/view/reportv5/studentresult",
                    MapUtils.m(
                            "subject", newHomework.getSubject(),
                            "homeworkId", newHomework.getId()));
            String shareReportUrl = UrlUtils.buildUrlQuery("/view/reportv5/share",
                    MapUtils.m(
                            "subject", newHomework.getSubject(),
                            "homeworkIds", newHomework.getId()));
            String checkHomeworkUrl = UrlUtils.buildUrlQuery("/view/reportv5/rearrange",
                    MapUtils.m(
                            "subject", newHomework.getSubject(),
                            "homeworkId", newHomework.getId()));

            appNewHomeworkStudentDetail.setCheckHomeworkUrl(checkHomeworkUrl);
            appNewHomeworkStudentDetail.setShareReportUrl(shareReportUrl);
            appNewHomeworkStudentDetail.setDetailUrl(detailUrl);
            appNewHomeworkStudentDetail.setCorrectUrl(correctUrl);
            // ************* end 一些地址传送 ********//

            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("appNewHomeworkStudentDetail", appNewHomeworkStudentDetail);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch App NewHomework StudentDetail failed : hid {},tid {}", hid, teacher.getId(), e);
            return MapMessage.errorMessage();
        }
    }

    /**
     * 个人详情base_app
     *
     * @param newHomework       作业实体类
     * @param newHomeworkResult 学生作业结果类
     * @param categoryId        练习类型id
     * @param lessonId          我是一个很奇怪的属性
     * @return 返回个人base_app详情
     */
    private List<Map<String, Object>> internalProcessHomeworkAnswer(User user, NewHomework newHomework, NewHomeworkResult newHomeworkResult, String categoryId, String lessonId, PracticeType practiceType, NewHomeworkApp newHomeworkApp, ObjectiveConfigType objectiveConfigType) {

        // 取出base_app类型,
        if (Objects.isNull(newHomeworkApp) ||
                CollectionUtils.isEmpty(newHomeworkApp.getQuestions())) {
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
        ProcessAppDetailByCategoryIdTemplate template = processAppDetailByCategoryIdFactory.getTemplate(natureSpellingType);
        if (template == null) {
            return Collections.emptyList();
        }
        Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), newHomeworkResult.findHomeworkProcessIdsForBaseAppByCategoryIdAndLessonId(categoryId, lessonId, objectiveConfigType));
        Map<String, NewHomeworkProcessResult> dataInfo = processResultMap
                .values()
                .stream()
                .collect(Collectors
                        .toMap(NewHomeworkProcessResult::getQuestionId, Function.identity()));
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


    /**
     * 处理base_app这类的作业详情链接展开数据
     *
     * @param newHomework 作业
     * @return base_app 类型链接详情具体内容作业详情
     */

    private List<Map<String, Object>> processNewHomeworkForBaseApp(NewHomeworkApp targetApp, NewHomework newHomework, PracticeType practiceType, List<String> questionIds, ObjectiveConfigType objectiveConfigType) {

        if (CollectionUtils.isEmpty(questionIds)) {
            return Collections.emptyList();
        }

        //Map qIds to NewHomeworkProcessResult
        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                .stream()
                .collect(Collectors
                        .toMap(LongIdEntity::getId, Function.identity()));
        Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), false);


        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        List<String> subHomeworkResultAnswerIds = new LinkedList<>();
        for (NewHomeworkQuestion newHomeworkQuestion : targetApp.fetchQuestions()) {
            NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomework.NewHomeworkQuestionObj(newHomework.getId(), objectiveConfigType, Arrays.asList(SafeConverter.toString(targetApp.getCategoryId()), targetApp.getLessonId()), newHomeworkQuestion.getQuestionId());
            for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
                subHomeworkResultAnswerIds.add(newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId()));
            }
        }
        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);
        List<String> newHomeworkProcessResultIds = subHomeworkResultAnswerMap.values().stream().map(SubHomeworkResultAnswer::getProcessId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(newHomeworkProcessResultIds)) {
            return Collections.emptyList();
        }
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), newHomeworkProcessResultIds);

        Map<String, List<NewHomeworkProcessResult>> dataInfo = new LinkedHashMap<>();

        for (NewHomeworkProcessResult n : newHomeworkProcessResultMap.values()) {
            if (!userMap.containsKey(n.getUserId()))
                continue;
            if (dataInfo.containsKey(n.getQuestionId())) {
                List<NewHomeworkProcessResult> m = dataInfo.get(n.getQuestionId());
                m.add(n);
                dataInfo.put(n.getQuestionId(), m);
            } else {
                List<NewHomeworkProcessResult> m = new LinkedList<>();
                m.add(n);
                dataInfo.put(n.getQuestionId(), m);
            }
        }

        NatureSpellingType natureSpellingType = NatureSpellingType.of(targetApp.getCategoryId());
        ProcessAppDetailByCategoryIdTemplate template = processAppDetailByCategoryIdFactory.getTemplate(natureSpellingType);
        if (template == null) {
            return Collections.emptyList();
        }
        Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        List<Long> sentenceIds = newQuestionMap
                .values()
                .stream()
                .filter(o -> CollectionUtils.isNotEmpty(o.getSentenceIds()))
                .map(NewQuestion::getSentenceIds)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Map<Long, Sentence> sentenceMap = englishContentLoaderClient.loadEnglishSentences(sentenceIds);
        CategoryClazzHandlerContext categoryClazzHandlerContext = new CategoryClazzHandlerContext(dataInfo,
                newQuestionMap,
                sentenceMap,
                userMap,
                practiceType);
        template.processClazzCategory(categoryClazzHandlerContext);
        return categoryClazzHandlerContext.getResult();
    }

    /**
     * 处理作业报告
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> processNewHomework(Map<Long, User> userMap, Map<Long, NewHomeworkResult> newHomeworkResultMap, NewHomework newHomework, boolean isPcWay) {
        List<String> questionIds = newHomework.findAllQuestionIds();
        Map<String, NewQuestion> allNewQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        // 2. 遍历每种作业形式下的题目
        List<String> newHomeworkProcessResultIds = newHomeworkResultMap
                .values()
                .stream()
                .map(o -> o.findAllHomeworkProcessIds(true))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.
                loads(newHomework.getId(), newHomeworkProcessResultIds);

        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        // 遍历每种作业形式下的报告
        ReportRateContext reportRateContext = new ReportRateContext(allNewQuestionMap,
                newHomeworkProcessResultMap,
                newHomeworkResultMap,
                userMap,
                newHomework,
                isPcWay,
                contentTypeMap
        );
        for (NewHomeworkPracticeContent content : newHomework.getPractices()) {
            ObjectiveConfigType type = content.getType();
            reportRateContext.setType(type);
            reportRateContext.setQuestions(content.processNewHomeworkQuestion(true)
                    .stream()
                    .map(NewHomeworkQuestion::getQuestionId)
                    .collect(Collectors.toList()));
            ProcessNewHomeworkAnswerDetailTemplate template = processNewHomeworkAnswerDetailFactory.getTemplate(type);
            if (template != null) {
                template.processNewHomeworkAnswerDetail(reportRateContext);
            }
        }
        return reportRateContext.getResult();
    }


    @Override
    public MapMessage loadNewHomeworkNeedCorrect(String homeworkId, Long teacherId) {
        MapMessage mapMessage = new MapMessage();
        String clazzName = "";
        Date startDate;
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacherId, newHomework.getClazzGroupId())) {
            return MapMessage.errorMessage("没有权限查看此作业报告");
        }
        Long groupId = newHomework.getClazzGroupId();
        GroupMapper group = groupLoaderClient.loadGroup(groupId, false);
        startDate = newHomework.getStartTime();
        mapMessage.add("questionInfoMapper", processNewHomeworkNeedCorrect(homeworkId));
        if (group != null) {
            Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(group.getClazzId());
            if (clazz != null) {
                clazzName = clazz.formalizeClazzName();
            }
        }
        mapMessage.add("homeworkId", homeworkId);
        mapMessage.add("className", clazzName);
        mapMessage.add("startDate", DateUtils.dateToString(startDate, "yyyy年MM月dd日"));
        mapMessage.setSuccess(true);
        return mapMessage;
    }

    /**
     * "去批改"-待批改作业报告
     *
     * @param homeworkId 作业id
     * @return map
     */
    private Map<String, Object> processNewHomeworkNeedCorrect(String homeworkId) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 每种作业形式下需要批改的题目
        Map<ObjectiveConfigType, List<String>> needCorrectQuestionMap = new LinkedHashMap<>();
        // 找到某个作业下的所有作业形式
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (Objects.equals(Boolean.TRUE, newHomework.getIncludeSubjective())) {
            // 本次作业中包含需要主观作答的题目
            if (CollectionUtils.isNotEmpty(newHomework.getPractices())) {
                LinkedHashMap<ObjectiveConfigType, List<NewHomeworkQuestion>> tempMap = newHomework.findIncludeSubjectiveQuestions();
                for (Map.Entry<ObjectiveConfigType, List<NewHomeworkQuestion>> entry : tempMap.entrySet()) {
                    ObjectiveConfigType key = entry.getKey();
                    List<String> values = entry
                            .getValue()
                            .stream()
                            .map(NewHomeworkQuestion::getQuestionId)
                            .collect(Collectors.toList());
                    needCorrectQuestionMap.put(key, values);
                }
            }
        } else {
            return result;
        }

        Map<Long, User> userMap = studentLoaderClient
                .loadGroupStudents(newHomework.getClazzGroupId())
                .stream()
                .collect(Collectors
                        .toMap(LongIdEntity::getId, Function.identity()));
        // 1.找到此作业下所有已完成的作业
        Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), true);

        // 2. 遍历每种作业形式下的题目
        List<String> newHomeworkProcessResultIds = new ArrayList<>();
        for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
            if (newHomeworkResult != null && MapUtils.isNotEmpty(newHomeworkResult.getPractices())) {
                for (Map.Entry<ObjectiveConfigType, NewHomeworkResultAnswer> entry : newHomeworkResult.getPractices().entrySet()) {
                    if (entry.getKey().isSubjective()) {
                        newHomeworkProcessResultIds.addAll(entry.getValue().processAnswers().values());
                    }
                }
            }
        }
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(homeworkId, newHomeworkProcessResultIds);
        // 过滤结果中review和correction为null，并且showPics不为空数组
        Map<ObjectiveConfigType, List<NewHomeworkProcessResult>> tempMap = newHomeworkProcessResultMap.values()
                .stream()
                .collect(Collectors.groupingBy(NewHomeworkProcessResult::getObjectiveConfigType));
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        List<String> questionIds = newHomework.findAllQuestionIds();
        Map<String, NewQuestion> allNewQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        // 遍历每种作业形式下的报告
        for (Map.Entry<ObjectiveConfigType, List<String>> entry : needCorrectQuestionMap.entrySet()) {
            if (entry.getKey() == ObjectiveConfigType.NEW_READ_RECITE) {
                ReportRateContext reportRateContext = new ReportRateContext(allNewQuestionMap,
                        newHomeworkProcessResultMap,
                        newHomeworkResultMap,
                        userMap,
                        newHomework,
                        false,
                        contentTypeMap
                );
                reportRateContext.setType(ObjectiveConfigType.NEW_READ_RECITE);
                processNewHomeworkAnswerDetailNewReadReciteTemplate.processNewHomeworkAnswerDetail(reportRateContext);
                result.putAll(reportRateContext.getResult());
            } else {
                String key = entry.getKey().name();
                List<QuestionDetail> values = new ArrayList<>();
                if (MapUtils.isNotEmpty(tempMap)) {
                    if (tempMap.containsKey(entry.getKey())) {
                        values = processNewHomeworkAnswerDetailCommonTemplate.newInternalProcessHomeworkAnswerDetail(userMap, allNewQuestionMap, contentTypeMap, newHomework, entry.getKey(), entry.getValue(), tempMap.get(entry.getKey()));
                    }
                }
                result.put(key, values);
            }
        }
        return result;
    }


    @Override
    public MapMessage homeworkForObjectiveConfigTypeResult(String homeworkId, ObjectiveConfigType objectiveConfigType, StudentDetail studentDetail) {
        try {
            NewHomework newHomework = newHomeworkLoader.load(homeworkId);
            if (null == newHomework) {
                return MapMessage.errorMessage().setInfo("作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
            if (objectiveConfigType == null) {
                return MapMessage.errorMessage().setInfo("作类型不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_CONTENT_IS_NULL);
            }
            NewHomework.Location location = newHomework.toLocation();
            String day = DayRange.newInstance(location.getCreateTime()).toString();
            NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, location.getSubject(), location.getId(), studentDetail.getId().toString());
            NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loads(Collections.singletonList(id.toString()), true).get(id.toString());
            if (newHomeworkResult == null) {
                return MapMessage.errorMessage().setInfo("newHomeworkResult is null").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_RESULT_NOT_EXIST);
            }
            NewHomeworkTypeResultProcessTemplate typeResultTemplate = newHomeworkTypeResultFactory.getTemplate(objectiveConfigType.getNewHomeworkTypeResultProcessTemp());
            if (typeResultTemplate == null) {
                return MapMessage.errorMessage("作业中间结果页模板不存在！");
            }
            return typeResultTemplate.processHomeworkTypeResult(objectiveConfigType, newHomeworkResult);
        } catch (Exception ex) {
            logger.error("homeworkForObjectiveConfigTypeResult error", ex);
            return MapMessage.errorMessage("获取作业中间结果页异常");
        }
    }

    @Override
    public MapMessage vacationHomeworkForObjectiveConfigTypeResult(String homeworkId, ObjectiveConfigType objectiveConfigType) {
        try {
            VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.load(homeworkId);
            if (vacationHomeworkResult == null) {
                return MapMessage.errorMessage().setInfo("vacationHomeworkResult is null").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_RESULT_NOT_EXIST);
            }
            switch (objectiveConfigType) {
                case BASIC_APP:
                case NATURAL_SPELLING:
                    return vacationHomeworkForBasicAppResult(objectiveConfigType, vacationHomeworkResult);
                case READING:
                    return vacationHomeworkForReadingResult(objectiveConfigType, vacationHomeworkResult);
                case LEVEL_READINGS:
                    return vacationHomeworkForLevelReadingResult(objectiveConfigType, vacationHomeworkResult);
                case DUBBING:
                    return vacationHomeworkForDubbingResult(objectiveConfigType, vacationHomeworkResult);
                case DUBBING_WITH_SCORE:
                    return vacationHomeworkForDubbingWithScoreResult(objectiveConfigType, vacationHomeworkResult);
                case KEY_POINTS:
                    return vacationHomeworkForKeyPointResult(objectiveConfigType, vacationHomeworkResult);
                case NEW_READ_RECITE:
                    return vacationHomeworkForReadReciteResult(objectiveConfigType, vacationHomeworkResult);
                case READ_RECITE_WITH_SCORE:
                    return vacationHomeworkForReadReciteWithScoreResult(objectiveConfigType, vacationHomeworkResult);
                case PHOTO_OBJECTIVE:
                    return vacationHomeworkForSubjectivityResult(objectiveConfigType, vacationHomeworkResult);
                case VOICE_OBJECTIVE:
                    return vacationHomeworkForSubjectivityResult(objectiveConfigType, vacationHomeworkResult);
                default:
                    return vacationHomeworkForExamResult(objectiveConfigType, vacationHomeworkResult);
            }
        } catch (Exception ex) {
            logger.error("Failed to load vacationHomeworkForObjectiveConfigTypeResult, homeworkId:{}, objectiveConfigType:{}", homeworkId, objectiveConfigType, ex);
            return MapMessage.errorMessage("获取作业中间结果异常");
        }
    }

    @Override
    public MapMessage personalReadingDetail(String homeworkId, Long studentId, String readingId, Long teacherId, ObjectiveConfigType type) {
        MapMessage mapMessage = new MapMessage();
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        User u = studentLoaderClient.loadStudent(studentId);
        if (Objects.isNull(newHomework)) {
            return MapMessage.errorMessage("homework does not exist");
        }
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
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


        if (teacherId != null) {//null 时候是学生端
            if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacherId, newHomework.getClazzGroupId())) {
                return MapMessage.errorMessage("老师权限错误");
            }
        }
        Map<Long, User> userMap = studentLoaderClient
                .loadGroupStudents(newHomework.getClazzGroupId())
                .stream()
                .collect(Collectors
                        .toMap(LongIdEntity::getId, Function.identity()));
        if (!userMap.containsKey(studentId)) {
            return MapMessage.errorMessage("该学生不在该作业的班级");
        }

        NewHomework.Location location = newHomework.toLocation();
        String day = DayRange.newInstance(location.getCreateTime()).toString();
        NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, location.getSubject(), location.getId(), studentId.toString());
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loads(Collections.singletonList(id.toString()), true).get(id.toString());
        if (newHomeworkResult == null) {
            return MapMessage.errorMessage();
        }
        if (MapUtils.isEmpty(newHomeworkResult.getPractices()) || !newHomeworkResult.getPractices().containsKey(type)) {
            return MapMessage.errorMessage("学生未完成该类型作业" + type);
        }
        NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
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
            Map<String, PictureBookPlus> pictureBookMap = pictureBookPlusServiceClient.loadByIds(Collections.singleton(readingId));
            readingName = pictureBookMap.get(readingId) != null ? pictureBookMap.get(readingId).getEname() : "";
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
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(homeworkId, newHomeworkProcessResultIds);
        Map<String, NewQuestion> questionsMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        List<Map<String, Object>> oralQuestions = new LinkedList<>();
        Map<String, Object> exercisesInfo = new LinkedHashMap<>();

        //是否包含口语题
        if (MapUtils.isNotEmpty(newHomeworkResultAppAnswer.getOralAnswers())) {
            LinkedHashMap<String, String> oralAnswers = newHomeworkResultAppAnswer.getOralAnswers();
            oralAnswers.forEach((key, value) -> {
                NewHomeworkProcessResult newHomeworkProcessResult = newHomeworkProcessResultMap.get(value);
                NewQuestion newQuestion = questionsMap.get(key);
                if (newHomeworkProcessResult != null && newQuestion != null) {
                    AppOralScoreLevel appOralScoreLevel = newHomeworkProcessResult.getAppOralScoreLevel();
                    double score = appOralScoreLevel != null ? appOralScoreLevel.getScore() : 0;
                    List<NewQuestionsSubContents> subContents = newQuestion.getContent().getSubContents();
                    int i = 0;
                    for (NewQuestionsSubContents newQuestionsSubContents : subContents) {
                        if (newQuestionsSubContents.getOralDict() != null
                                && CollectionUtils.isNotEmpty(newQuestionsSubContents.getOralDict().getOptions())
                                && newHomeworkProcessResult.getOralDetails().size() >= (i + 1)) {
                            List<NewHomeworkProcessResult.OralDetail> oralDetails = newHomeworkProcessResult.getOralDetails().get(i);
                            List<NewQuestionOralDictOptions> options = newQuestionsSubContents.getOralDict().getOptions();
                            int j = 0;
                            for (NewQuestionOralDictOptions newQuestionOralDictOptions : options) {
                                if (oralDetails.size() >= (j + 1)) {
                                    NewHomeworkProcessResult.OralDetail oralDetail = oralDetails.get(j);
                                    String voiceUrl = oralDetail.getAudio();
                                    VoiceEngineType voiceEngineType = newHomeworkProcessResult.getVoiceEngineType();
                                    voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                                    oralQuestions.add(MapUtils.m(
                                            "text", newQuestionOralDictOptions.getText(),
                                            "audio", voiceUrl,
                                            "score", score
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
            List<Map<String, Object>> exercisesQuestionsInfo = new LinkedList<>();
            for (Map.Entry<String, String> entry : answers.entrySet()) {
                String value = entry.getValue();
                NewHomeworkProcessResult newHomeworkProcessResult = newHomeworkProcessResultMap.get(value);
                NewQuestion newQuestion = questionsMap.get(entry.getKey());
                if (newHomeworkProcessResult == null || newQuestion == null) continue;
                List<NewQuestionsSubContents> subContents = newQuestion.getContent().getSubContents();
                List<List<String>> standardAnswers = subContents
                        .stream()
                        .map(o -> o.getAnswerList(newHomework.getSubject()))
                        .collect(Collectors.toList());

                if (Objects.equals(newHomeworkProcessResult.getGrasp(), Boolean.TRUE)) {
                    rightNum++;
                }
                exercisesQuestionsInfo.add(MapUtils.m(
                        "questionId", entry.getKey(),
                        "userAnswers", NewHomeworkUtils.pressAnswer(subContents, newHomeworkProcessResult.getUserAnswers()),
                        "standardAnswers", NewHomeworkUtils.pressAnswer(subContents, standardAnswers),
                        "difficultyName", QuestionConstants.newDifficultyMap.get(newQuestion.getDifficultyInt()),
                        "questionType", contentTypeMap.get(newQuestion.getContentTypeId()) != null ? contentTypeMap.get(newQuestion.getContentTypeId()).getName() : "无题型"
                ));
            }
            exercisesInfo.put("rightNum", rightNum);
            exercisesInfo.put("totalExercises", totalExercises);
            exercisesInfo.put("exercisesQuestionInfo", exercisesQuestionsInfo);
        }
        String dubbingId = newHomeworkResultAppAnswer.getDubbingId();
        AppOralScoreLevel appOralScoreLevel = newHomeworkResultAppAnswer.getDubbingScoreLevel();
        mapMessage.add("dubbingId", targetApp.containsDubbing() ? dubbingId : null);
        mapMessage.add("dubbingScoreLevel", appOralScoreLevel != null && targetApp.containsDubbing() ? appOralScoreLevel.getDesc() : null);
        mapMessage.add("exercisesInfo", exercisesInfo);
        mapMessage.add("oralQuestions", oralQuestions);
        mapMessage.add("readingName", readingName);
        mapMessage.add("studentName", u != null ? u.fetchRealname() : "");
        mapMessage.add("homeworkType", newHomework.getNewHomeworkType());
        mapMessage.add("subject", newHomework.getSubject());
        mapMessage.setSuccess(true);
        return mapMessage;
    }

    @Override
    public MapMessage personalDubbingDetail(String homeworkId, Long studentId, String dubbingId, Long teacherId) {
        try {
            NewHomework newHomework = newHomeworkLoader.load(homeworkId);
            if (newHomework == null) {
                return MapMessage.errorMessage("作业不存在");
            }
            NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.DUBBING);
            if (target == null) {
                return MapMessage.errorMessage("作业不包含" + ObjectiveConfigType.DUBBING.getValue());
            }
            if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacherId, newHomework.getClazzGroupId())) {
                return MapMessage.errorMessage("没有权限查看此作业报告").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_PERMISSION);
            }
            //获取班级学生
            Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                    .stream()
                    .collect(Collectors.toMap(LongIdEntity::getId, Function.identity()));
            if (MapUtils.isNotEmpty(userMap) && !userMap.containsKey(studentId)) {
                return MapMessage.errorMessage("该学生不在该作业的班级");
            }
            NewHomework.Location location = newHomework.toLocation();
            String day = DayRange.newInstance(location.getCreateTime()).toString();
            NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, location.getSubject(), location.getId(), studentId.toString());
            NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loads(Collections.singletonList(id.toString()), true).get(id.toString());
            String duration = "";
            String studentVideoUrl = "";
            boolean syntheticSuccess = true;
            if (newHomeworkResult != null
                    && MapUtils.isNotEmpty(newHomeworkResult.getPractices())
                    && newHomeworkResult.getPractices().get(ObjectiveConfigType.DUBBING) != null) {
                NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.DUBBING);
                LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = newHomeworkResultAnswer.getAppAnswers();
                NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = appAnswers.get(dubbingId);
                if (newHomeworkResultAppAnswer != null) {
                    String hyid = new DubbingSyntheticHistory.ID(homeworkId, studentId, dubbingId).toString();
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
                    "studentId", studentId,
                    "studentName", userMap.get(studentId) == null ? "" : userMap.get(studentId).fetchRealname(),
                    "duration", duration,
                    "syntheticSuccess", syntheticSuccess,
                    "studentVideoUrl", studentVideoUrl);
            return MapMessage.successMessage().add("content", result);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage personalDubbingWithScoreDetail(String homeworkId, Long studentId, String dubbingId, Long teacherId) {
        try {
            NewHomework newHomework = newHomeworkLoader.load(homeworkId);
            if (newHomework == null) {
                return MapMessage.errorMessage("作业不存在");
            }
            NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.DUBBING_WITH_SCORE);
            if (target == null) {
                return MapMessage.errorMessage("作业不包含" + ObjectiveConfigType.DUBBING_WITH_SCORE.getValue());
            }
            if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacherId, newHomework.getClazzGroupId())) {
                return MapMessage.errorMessage("没有权限查看此作业报告").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_PERMISSION);
            }
            Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(Collections.singleton(dubbingId));
            if (MapUtils.isEmpty(dubbingMap) || dubbingMap.get(dubbingId) == null) {
                return MapMessage.errorMessage("配音不存在");
            }
            //获取班级学生
            Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                    .stream()
                    .collect(Collectors.toMap(LongIdEntity::getId, Function.identity()));
            if (MapUtils.isNotEmpty(userMap) && !userMap.containsKey(studentId)) {
                return MapMessage.errorMessage("该学生不在该作业的班级");
            }
            NewHomework.Location location = newHomework.toLocation();
            String day = DayRange.newInstance(location.getCreateTime()).toString();
            NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, location.getSubject(), location.getId(), studentId.toString());
            NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loads(Collections.singletonList(id.toString()), true).get(id.toString());
            String duration;
            String studentVideoUrl;
            boolean syntheticSuccess = true;
            if (newHomeworkResult == null
                    || MapUtils.isEmpty(newHomeworkResult.getPractices())
                    || newHomeworkResult.getPractices().get(ObjectiveConfigType.DUBBING_WITH_SCORE) == null
                    || MapUtils.isEmpty(newHomeworkResult.getPractices().get(ObjectiveConfigType.DUBBING_WITH_SCORE).getAppAnswers())
            ) {
                return MapMessage.errorMessage("不存在该学生的配音记录");
            }
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.DUBBING_WITH_SCORE);
            LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = newHomeworkResultAnswer.getAppAnswers();
            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = appAnswers.get(dubbingId);
            if (newHomeworkResultAppAnswer == null) {
                return MapMessage.errorMessage("不存在该学生的配音记录");
            }
            String hyid = new DubbingSyntheticHistory.ID(homeworkId, studentId, dubbingId).toString();
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
            int score = new BigDecimal(SafeConverter.toDouble(newHomeworkResultAppAnswer.getScore())).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            Map<String, Object> result = MapUtils.m(
                    "dubbingId", dubbingId,
                    "dubbingName", dubbingMap.get(dubbingId).getVideoName(),
                    "score", score + "分",
                    "duration", duration,
                    "syntheticSuccess", syntheticSuccess,
                    "studentVideoUrl", studentVideoUrl);
            return MapMessage.successMessage().add("content", result);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage personalOralCommunicationDetail(String homeworkId, Long studentId, String stoneId, Long teacherId) {
        try {
            NewHomework newHomework = newHomeworkLoader.load(homeworkId);
            if (newHomework == null) {
                return MapMessage.errorMessage("作业不存在");
            }
            NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.ORAL_COMMUNICATION);
            if (target == null) {
                return MapMessage.errorMessage("作业不包含" + ObjectiveConfigType.ORAL_COMMUNICATION.getValue());
            }
            if (teacherId != null && !teacherLoaderClient.hasRelTeacherTeachingGroup(teacherId, newHomework.getClazzGroupId())) {
                return MapMessage.errorMessage("没有权限查看此作业报告").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_PERMISSION);
            }
            //获取班级学生
            Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                    .stream()
                    .collect(Collectors.toMap(LongIdEntity::getId, Function.identity()));
            if (MapUtils.isNotEmpty(userMap) && !userMap.containsKey(studentId)) {
                return MapMessage.errorMessage("该学生不在该作业的班级");
            }
            List<StoneBufferedData> stoneBufferedDataList = stoneDataLoaderClient.getStoneBufferedDataList(Collections.singleton(stoneId));
            if (CollectionUtils.isEmpty(stoneBufferedDataList)) {
                return MapMessage.errorMessage("未找到该题包");
            }
            StoneBufferedData stoneBufferedData = stoneBufferedDataList.get(0);
            NewHomework.Location location = newHomework.toLocation();
            String day = DayRange.newInstance(location.getCreateTime()).toString();
            NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, location.getSubject(), location.getId(), studentId.toString());
            NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loads(Collections.singletonList(id.toString()), true).get(id.toString());
            if (newHomeworkResult == null
                    || MapUtils.isEmpty(newHomeworkResult.getPractices())
                    || newHomeworkResult.getPractices().get(ObjectiveConfigType.ORAL_COMMUNICATION) == null
                    || MapUtils.isEmpty(newHomeworkResult.getPractices().get(ObjectiveConfigType.ORAL_COMMUNICATION).getAppAnswers())
            ) {
                return MapMessage.errorMessage("不存在该学生的口语交际练习记录");
            }
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.ORAL_COMMUNICATION);
            LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = newHomeworkResultAnswer.getAppAnswers();
            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = appAnswers.get(stoneId);
            if (newHomeworkResultAppAnswer == null) {
                return MapMessage.errorMessage("不存在该学生的口语交际练习记录");
            }
            String duration;
            int time = new BigDecimal(SafeConverter.toLong(newHomeworkResultAppAnswer.processDuration()))
                    .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                    .intValue();
            duration = NewHomeworkUtils.handlerEnTime(time);
            int score = new BigDecimal(SafeConverter.toDouble(newHomeworkResultAppAnswer.getScore())).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            String topicName = "";
            List<Map<String, Object>> studentRecords = Lists.newArrayList();
            List<String> subHomeworkResultAnswerIds = new LinkedList<>();
            Map<String, SubHomeworkProcessResult> homeworkProcessResultMap;
            List<String> dialogList = Lists.newArrayList();
            if (stoneBufferedData.getOralPracticeConversion() != null) {
                topicName = StringUtils.isNotEmpty(stoneBufferedData.getOralPracticeConversion().getTopicTrans())
                        ? stoneBufferedData.getOralPracticeConversion().getTopicTrans()
                        : stoneBufferedData.getOralPracticeConversion().getTopicName();
                dialogList = stoneBufferedData.getOralPracticeConversion().getTopics()
                        .stream()
                        .filter(t -> CollectionUtils.isNotEmpty(t.getContents()))
                        .map(Topic::getContents)
                        .flatMap(List::stream)
                        .filter(c -> CollectionUtils.isNotEmpty(c.getDialogs()))
                        .map(OralContent::getDialogs)
                        .flatMap(List::stream)
                        .filter(Objects::nonNull)
                        .filter(d -> SafeConverter.toInt(d.getRequiredAnswer()) == 1)
                        .peek(d -> {
                            Map<String, Object> dialogItemMap = Maps.newLinkedHashMap();
                            dialogItemMap.put("uuid", d.getUuid());
                            dialogItemMap.put("questionContent", d.getSentence());
                            studentRecords.add(dialogItemMap);
                        })
                        .map(Dialog::getUuid)
                        .collect(Collectors.toList());
            }
            if (stoneBufferedData.getInteractiveVideo() != null) {
                topicName = StringUtils.isNotEmpty(stoneBufferedData.getInteractiveVideo().getTopicTrans())
                        ? stoneBufferedData.getInteractiveVideo().getTopicTrans()
                        : stoneBufferedData.getInteractiveVideo().getTopicName();
                dialogList = stoneBufferedData.getInteractiveVideo().getContents()
                        .stream()
                        .filter(v -> "record".equals(v.getContentType()))
                        .peek(d -> {
                            Map<String, Object> dialogItemMap = Maps.newLinkedHashMap();
                            dialogItemMap.put("uuid", d.getUuid());
                            dialogItemMap.put("questionContent", d.getContentText());
                            studentRecords.add(dialogItemMap);
                        })
                        .map(VideoContent::getUuid)
                        .collect(Collectors.toList());
            }
            if (stoneBufferedData.getInteractivePictureBook() != null) {
                topicName = StringUtils.isNotEmpty(stoneBufferedData.getInteractivePictureBook().getTopicTrans())
                        ? stoneBufferedData.getInteractivePictureBook().getTopicTrans()
                        : stoneBufferedData.getInteractivePictureBook().getTopicName();
                dialogList = stoneBufferedData.getInteractivePictureBook().getPages().stream()
                        .filter(p -> CollectionUtils.isNotEmpty(p.getSections()))
                        .map(InteractivePictureBook.Page::getSections)
                        .flatMap(List::stream)
                        .filter(s -> CollectionUtils.isNotEmpty(s.getQuestions()))
                        .map(InteractivePictureBook.Section::getQuestions)
                        .flatMap(List::stream)
                        .filter(q -> "record".equals(q.getContentType()))
                        .peek(d -> {
                            Map<String, Object> dialogItemMap = Maps.newLinkedHashMap();
                            dialogItemMap.put("uuid", d.getUuid());
                            dialogItemMap.put("questionContent", d.getContentText());
                            studentRecords.add(dialogItemMap);
                        })
                        .map(InteractivePictureBook.Question::getUuid)
                        .collect(Collectors.toList());
            }
            if (CollectionUtils.isEmpty(dialogList)) {
                return MapMessage.errorMessage("不存在该学生的口语交际练习记录");
            }
            dialogList.forEach(d -> {
                SubHomeworkResultAnswer.ID aid = new SubHomeworkResultAnswer.ID();
                aid.setDay(day);
                aid.setHid(newHomework.getId());
                aid.setJoinKeys(Collections.singleton(stoneId));
                aid.setType(ObjectiveConfigType.ORAL_COMMUNICATION);
                aid.setUserId(SafeConverter.toString(studentId));
                aid.setQuestionId(d);
                subHomeworkResultAnswerIds.add(aid.toString());
            });

            if (CollectionUtils.isEmpty(subHomeworkResultAnswerIds)) {
                return MapMessage.errorMessage("不存在该学生的口语交际练习记录");
            }
            Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);
            if (MapUtils.isEmpty(subHomeworkResultAnswerMap)) {
                return MapMessage.errorMessage("不存在该学生的口语交际练习记录");
            }
            List<String> newHomeworkProcessResultIds = subHomeworkResultAnswerMap.values()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(SubHomeworkResultAnswer::getProcessId)
                    .collect(Collectors.toList());
            Map<String, SubHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(newHomeworkProcessResultIds);
            if (MapUtils.isEmpty(newHomeworkProcessResultMap)) {
                return MapMessage.errorMessage("不存在该学生的口语交际练习记录");
            }
            homeworkProcessResultMap = newHomeworkProcessResultMap.values()
                    .stream()
                    .collect(Collectors.toMap(SubHomeworkProcessResult::getDialogId, Function.identity()));
            Iterator<Map<String, Object>> iterator = studentRecords.iterator();
            while (iterator.hasNext()) {
                Map<String, Object> studentRecord = iterator.next();
                String uuid = SafeConverter.toString(studentRecord.get("uuid"));
                if (homeworkProcessResultMap.get(uuid) == null) {
                    iterator.remove();
                    continue;
                }
                studentRecord.put("studentAudio", CollectionUtils.isNotEmpty(homeworkProcessResultMap.get(uuid).getOralDetails().get(0))
                        && homeworkProcessResultMap.get(uuid).getOralDetails().get(0).get(0) != null
                        ? homeworkProcessResultMap.get(uuid).getOralDetails().get(0).get(0).getAudio() : "");
                studentRecord.put("score", homeworkProcessResultMap.get(uuid).getScore());
            }
            Map<String, Object> result = MapUtils.m("stoneId", stoneId,
                    "topicName", topicName,
                    "duration", duration,
                    "score", score,
                    "studentRecords", studentRecords
            );
            return MapMessage.successMessage().add("content", result);
        } catch (Exception e) {
            logger.error("personalOralCommunicationDetail error ", e);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage studentDubbingWithScoreDetail(String homeworkId, Long studentId, String dubbingId) {
        try {
            NewHomework newHomework = newHomeworkLoader.load(homeworkId);
            if (newHomework == null) {
                return MapMessage.errorMessage("作业不存在");
            }
            NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.DUBBING_WITH_SCORE);
            if (target == null) {
                return MapMessage.errorMessage("作业不包含" + ObjectiveConfigType.DUBBING_WITH_SCORE.getValue());
            }
            Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(Collections.singleton(dubbingId));
            if (MapUtils.isEmpty(dubbingMap) || dubbingMap.get(dubbingId) == null) {
                return MapMessage.errorMessage("配音不存在");
            }
            //获取班级学生
            Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                    .stream()
                    .collect(Collectors.toMap(LongIdEntity::getId, Function.identity()));
            if (MapUtils.isNotEmpty(userMap) && !userMap.containsKey(studentId)) {
                return MapMessage.errorMessage("该学生不在该作业的班级");
            }
            NewHomework.Location location = newHomework.toLocation();
            String day = DayRange.newInstance(location.getCreateTime()).toString();
            NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, location.getSubject(), location.getId(), studentId.toString());
            NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loads(Collections.singletonList(id.toString()), true).get(id.toString());
            String duration;
            Double scoreResult;
            String studentVideoUrl;
            boolean syntheticSuccess = true;
            if (newHomeworkResult == null
                    || MapUtils.isEmpty(newHomeworkResult.getPractices())
                    || newHomeworkResult.getPractices().get(ObjectiveConfigType.DUBBING_WITH_SCORE) == null) {
                return MapMessage.errorMessage("不存在该学生的配音记录");
            }
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.DUBBING_WITH_SCORE);
            LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = newHomeworkResultAnswer.getAppAnswers();
            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = appAnswers.get(dubbingId);
            if (newHomeworkResultAppAnswer == null) {
                return MapMessage.errorMessage("不存在该学生的配音记录");
            }
            // 该作业形式的总体得分(8分制)
            Map<String, SubHomeworkProcessResult> homeworkProcessResultMap;
            List<String> subHomeworkResultAnswerIds = new LinkedList<>();
            Map<String, String> dubbingQuestionMap = Maps.newHashMap();
            newHomeworkResultAppAnswer.getAnswers().keySet().forEach(q -> dubbingQuestionMap.put(q, dubbingId));
            for (Map.Entry<String, String> entry : dubbingQuestionMap.entrySet()) {
                NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomework.NewHomeworkQuestionObj(newHomework.getId(), ObjectiveConfigType.DUBBING_WITH_SCORE, Collections.singletonList(entry.getValue()), entry.getKey());
                subHomeworkResultAnswerIds.add(newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, studentId));
            }
            Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);
            if (MapUtils.isEmpty(subHomeworkResultAnswerMap)) {
                return MapMessage.errorMessage("不存在该学生的配音记录");
            }
            List<String> newHomeworkProcessResultIds = subHomeworkResultAnswerMap.values()
                    .stream()
                    .map(SubHomeworkResultAnswer::getProcessId)
                    .collect(Collectors.toList());
            Map<String, SubHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(newHomeworkProcessResultIds);
            if (MapUtils.isEmpty(newHomeworkProcessResultMap)) {
                return MapMessage.errorMessage("不存在该学生的配音记录");
            }
            homeworkProcessResultMap = newHomeworkProcessResultMap.values()
                    .stream()
                    .collect(Collectors.toMap(SubHomeworkProcessResult::getQuestionId, Function.identity()));
            // 计算8分制分数
            List<SubHomeworkProcessResult> subHomeworkProcessResultList = new ArrayList<>();
            Set<String> questionIds = newHomeworkResultAppAnswer.getAnswers().keySet();
            for (String questionId : questionIds) {
                SubHomeworkProcessResult subHomeworkProcessResult = homeworkProcessResultMap.get(questionId);
                if (subHomeworkProcessResult != null) {
                    subHomeworkProcessResultList.add(subHomeworkProcessResult);
                }
            }
            if (CollectionUtils.isEmpty(subHomeworkProcessResultList)) {
                return MapMessage.errorMessage("不存在该学生的配音记录");
            }
            double totalScore = subHomeworkProcessResultList.stream().mapToDouble(processResult -> SafeConverter.toDouble(processResult.getActualScore())).sum();
            scoreResult = SafeConverter.toDouble(Math.floor(totalScore / subHomeworkProcessResultList.size()));
            int time = new BigDecimal(SafeConverter.toLong(newHomeworkResultAppAnswer.processDuration()))
                    .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                    .intValue();
            duration = NewHomeworkUtils.handlerEnTime(time);
            String hyid = new DubbingSyntheticHistory.ID(homeworkId, studentId, dubbingId).toString();
            Map<String, DubbingSyntheticHistory> dubbingSyntheticHistoryMap = newHomeworkLoader.loadDubbingSyntheticHistories(Collections.singleton(hyid));
            if (dubbingSyntheticHistoryMap.containsKey(hyid)) {
                DubbingSyntheticHistory dubbingSyntheticHistory = dubbingSyntheticHistoryMap.get(hyid);
                syntheticSuccess = SafeConverter.toBoolean(dubbingSyntheticHistory.isSyntheticSuccess(newHomework.getCreateAt()));
            }
            studentVideoUrl = newHomeworkResultAppAnswer.getVideoUrl();
            //判断该配音是否是欢快歌曲类型   欢快歌曲类型ID:DC_10300000140166
            DubbingCategory dubbingCategory = dubbingLoaderClient.loadDubbingCategoriesByIds(Collections.singleton(dubbingMap.get(dubbingId).getCategoryId())).get(dubbingMap.get(dubbingId).getCategoryId());
            boolean isHappySong = dubbingCategory != null && Objects.equals("DC_10300000140166", dubbingCategory.getParentId());
            Map<String, Object> result = MapUtils.m(
                    "userName", userMap.get(studentId).fetchRealname(),
                    "imageUrl", userMap.get(studentId).fetchImageUrl(),
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

    @Override
    public MapMessage loadEnglishHomeworkVoiceList(String homeworkId) {
        return homeworkReportProcessor.loadEnglishHomeworkVoiceList(homeworkId);
    }

    private MapMessage vacationHomeworkForBasicAppResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_RESULT_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appResult = resultAnswer.getAppAnswers();
        List<String> processResultIds = new ArrayList<>();
        List<String> lessonIds = new ArrayList<>();
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            processResultIds.addAll(nraa.getAnswers().values());
            lessonIds.add(nraa.getLessonId());
        }

        Map<String, VacationHomeworkProcessResult> nhprMap = vacationHomeworkProcessResultDao.loads(processResultIds);
        Map<String, List<Map<String, Object>>> lessonCategorysMap = new HashMap<>();
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            String lessonId = nraa.getLessonId();
            PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(SafeConverter.toLong(nraa.getPracticeId()));
            int errorCount = 0;
            int rightCount = 0;
            boolean finished = nraa.isFinished();
            for (String processResultId : nraa.getAnswers().values()) {
                VacationHomeworkProcessResult nhpr = nhprMap.get(processResultId);
                if (nhpr == null) continue;
                if (practiceType.getNeedRecord()) {
                    if ((nhpr.getAppOralScoreLevel() != null && !AppOralScoreLevel.D.equals(nhpr.getAppOralScoreLevel())) || (nhpr.getAppOralScoreLevel() == null && nhpr.getScore() >= 40)) {
                        rightCount++;
                    } else {
                        errorCount++;
                    }
                } else {
                    if (SafeConverter.toBoolean(nhpr.getGrasp())) {
                        rightCount++;
                    } else {
                        errorCount++;
                    }
                }
            }
            List<Map<String, Object>> categorys = lessonCategorysMap.get(lessonId);
            if (CollectionUtils.isEmpty(categorys)) {
                categorys = new ArrayList<>();
            }
            categorys.add(MapUtils.m(
                    "catetoryName", practiceType.getCategoryName(),
                    "needRecord", practiceType.getNeedRecord(),
                    "rightCount", rightCount,
                    "errorCount", errorCount,
                    "finished", finished));
            lessonCategorysMap.put(lessonId, categorys);
        }
        Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        Map<String, List<String>> unitLessonsMap = new LinkedHashMap<>();
        Map<String, String> lessonUnitMap = NewHomeworkUtils.handleLessonIdToUnitId(lessonMap);
        for (Map.Entry<String, String> lessonUnitEntry : lessonUnitMap.entrySet()) {
            String unitId = lessonUnitEntry.getValue();
            List<String> lids = unitLessonsMap.get(unitId);
            if (CollectionUtils.isEmpty(lids)) {
                lids = new ArrayList<>();
            }
            lids.add(lessonUnitEntry.getKey());
            unitLessonsMap.put(unitId, lids);
        }
        Map<String, NewBookCatalog> unitMap = newContentLoaderClient.loadBookCatalogByCatalogIds(unitLessonsMap.keySet());
        List<Map<String, Object>> results = new ArrayList<>();

        handleUnitLessonsMap(unitLessonsMap,
                unitMap,
                lessonMap,
                lessonCategorysMap,
                results);

        return MapMessage.successMessage().add("datas", results);
    }

    private void handleUnitLessonsMap(Map<String, List<String>> unitLessonsMap,
                                      Map<String, NewBookCatalog> unitMap,
                                      Map<String, NewBookCatalog> lessonMap,
                                      Map<String, List<Map<String, Object>>> lessonCategorysMap,
                                      List<Map<String, Object>> results) {
        for (Map.Entry<String, List<String>> unitLessonEntry : unitLessonsMap.entrySet()) {
            NewBookCatalog unit = unitMap.get(unitLessonEntry.getKey());
            List<Map<String, Object>> lessonObjs = new ArrayList<>();
            if (unit == null) continue;
            for (String lessonId : unitLessonEntry.getValue()) {
                NewBookCatalog lesson = lessonMap.get(lessonId);
                List<Map<String, Object>> categorys = lessonCategorysMap.get(lessonId);
                if (lesson == null || categorys == null) continue;
                lessonObjs.add(MapUtils.m("lessonName", lesson.getAlias(),
                        "categorys", categorys));
            }
            if (CollectionUtils.isNotEmpty(lessonObjs)) {
                results.add(MapUtils.m("unitName", unit.getAlias(),
                        "lessons", lessonObjs));
            }
        }


    }

    /**
     * 绘本阅读
     *
     * @param objectiveConfigType
     * @param baseHomeworkResult
     * @return
     */
    private MapMessage vacationHomeworkForReadingResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_RESULT_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appResult = resultAnswer.getAppAnswers();
        List<String> processResultIds = new ArrayList<>();
        List<String> readingIds = new ArrayList<>();
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            processResultIds.addAll(nraa.getAnswers().values());
            readingIds.add(nraa.getPictureBookId());
        }
        Map<String, VacationHomeworkProcessResult> nhprMap = vacationHomeworkProcessResultDao.loads(processResultIds);
        Map<String, PictureBook> readingMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(readingIds);
        List<Map<String, Object>> results = new ArrayList<>();
        int totalRightCount = 0;
        int totalErrorCount = 0;
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            PictureBook reading = readingMap.get(nraa.getPictureBookId());
            int errorCount = 0;
            int rightCount = 0;
            for (String processResultId : nraa.getAnswers().values()) {
                VacationHomeworkProcessResult nhpr = nhprMap.get(processResultId);
                if (nhpr == null) continue;
                if (SafeConverter.toBoolean(nhpr.getGrasp())) {
                    rightCount++;
                    totalRightCount++;
                } else {
                    errorCount++;
                    totalErrorCount++;
                }
            }
            results.add(MapUtils.m("readingName", reading.getName(),
                    "rightCount", rightCount,
                    "errorCount", errorCount));
        }
        return MapMessage.successMessage().add("datas", results)
                .add("rightCount", totalRightCount)
                .add("errorCount", totalErrorCount);
    }

    /**
     * 新版绘本阅读
     *
     * @param objectiveConfigType
     * @param baseHomeworkResult
     * @return
     */
    private MapMessage vacationHomeworkForLevelReadingResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appResult = resultAnswer.getAppAnswers();
        List<String> processResultIds = new ArrayList<>();
        List<String> readingIds = new ArrayList<>();
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            if (MapUtils.isNotEmpty(nraa.getAnswers())) {
                processResultIds.addAll(nraa.getAnswers().values());
            }
            readingIds.add(nraa.getPictureBookId());
        }
        Map<String, VacationHomeworkProcessResult> nhprMap = vacationHomeworkProcessResultDao.loads(processResultIds);
        Map<String, PictureBookPlus> readingMap = pictureBookPlusServiceClient.loadByIds(readingIds);
        List<Map<String, Object>> results = new ArrayList<>();
        int totalRightCount = 0;
        int totalErrorCount = 0;
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            PictureBookPlus reading = readingMap.get(nraa.getPictureBookId());
            if (reading != null) {
                int errorCount = 0;
                int rightCount = 0;
                for (String processResultId : nraa.getAnswers().values()) {
                    VacationHomeworkProcessResult nhpr = nhprMap.get(processResultId);
                    if (nhpr == null) {
                        continue;
                    }
                    if (SafeConverter.toBoolean(nhpr.getGrasp())) {
                        rightCount++;
                        totalRightCount++;
                    } else {
                        errorCount++;
                        totalErrorCount++;
                    }
                }
                results.add(MapUtils.m("readingName", reading.getEname(),
                        "rightCount", rightCount,
                        "errorCount", errorCount));
            }
        }
        return MapMessage.successMessage().add("datas", results)
                .add("rightCount", totalRightCount)
                .add("errorCount", totalErrorCount);
    }

    private MapMessage vacationHomeworkForDubbingResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEW_HOMEWORK_TYPE_NOT_EXIST);
        }
        VacationHomework vacationHomework = vacationHomeworkDao.load(baseHomeworkResult.getHomeworkId());
        if (vacationHomework == null) {
            return MapMessage.errorMessage().setInfo("假期作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = resultAnswer.getAppAnswers();
        List<String> dubbingIds = appAnswers.values()
                .stream()
                .map(NewHomeworkResultAppAnswer::getDubbingId)
                .collect(Collectors.toList());
        Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(dubbingIds);

        // 生成合成配音ids
        String homeworkId = baseHomeworkResult.getHomeworkId();
        Long studentId = baseHomeworkResult.getUserId();
        List<String> ids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dubbingIds)) {
            for (String dubbingId : dubbingIds) {
                ids.add(new DubbingSyntheticHistory.ID(homeworkId, studentId, dubbingId).toString());
            }
        }
        Map<String, DubbingSyntheticHistory> dubbingSyntheticHistoryMap = newHomeworkLoader.loadDubbingSyntheticHistories(ids);

        List<Map<String, Object>> results = new ArrayList<>();
        for (NewHomeworkResultAppAnswer appAnswer : appAnswers.values()) {
            String dubbingId = appAnswer.getDubbingId();
            Dubbing dubbing = dubbingMap.get(dubbingId);
            String id = new DubbingSyntheticHistory.ID(homeworkId, studentId, dubbingId).toString();
            DubbingSyntheticHistory dubbingSyntheticHistory = dubbingSyntheticHistoryMap.get(id);
            // 合成配音是否成功
            Boolean synthetic = dubbingSyntheticHistory == null || SafeConverter.toBoolean(dubbingSyntheticHistory.isSyntheticSuccess(vacationHomework.getCreateAt()));
            results.add(MapUtils.m(
                    "videoName", dubbing != null ? dubbing.getVideoName() : "",
                    "videoUrl", appAnswer.getVideoUrl(),
                    "synthetic", synthetic));
        }
        return MapMessage.successMessage().add("datas", results);
    }

    /**
     * 新版趣味配音
     *
     * @param objectiveConfigType
     * @param baseHomeworkResult
     * @return
     */
    private MapMessage vacationHomeworkForDubbingWithScoreResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }
        VacationHomework vacationHomework = vacationHomeworkDao.load(baseHomeworkResult.getHomeworkId());
        if (vacationHomework == null) {
            return MapMessage.errorMessage().setInfo("假期作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = resultAnswer.getAppAnswers();
        List<String> dubbingIds = appAnswers.values()
                .stream()
                .map(NewHomeworkResultAppAnswer::getDubbingId)
                .collect(Collectors.toList());
        Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(dubbingIds);

        // 生成合成配音ids
        String homeworkId = baseHomeworkResult.getHomeworkId();
        Long studentId = baseHomeworkResult.getUserId();
        List<String> ids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dubbingIds)) {
            for (String dubbingId : dubbingIds) {
                ids.add(new DubbingSyntheticHistory.ID(homeworkId, studentId, dubbingId).toString());
            }
        }
        Map<String, DubbingSyntheticHistory> dubbingSyntheticHistoryMap = newHomeworkLoader.loadDubbingSyntheticHistories(ids);

        List<Map<String, Object>> results = new ArrayList<>();
        for (NewHomeworkResultAppAnswer appAnswer : appAnswers.values()) {
            String dubbingId = appAnswer.getDubbingId();
            Dubbing dubbing = dubbingMap.get(dubbingId);
            String id = new DubbingSyntheticHistory.ID(homeworkId, studentId, dubbingId).toString();
            DubbingSyntheticHistory dubbingSyntheticHistory = dubbingSyntheticHistoryMap.get(id);
            // 合成配音是否成功
            Boolean synthetic = dubbingSyntheticHistory == null || SafeConverter.toBoolean(dubbingSyntheticHistory.isSyntheticSuccess(vacationHomework.getCreateAt()));
            results.add(MapUtils.m(
                    "videoName", dubbing != null ? dubbing.getVideoName() : "",
                    "videoUrl", appAnswer.getVideoUrl(),
                    "synthetic", synthetic));
        }
        return MapMessage.successMessage().add("datas", results);
    }

    /**
     * 课文读背
     *
     * @param objectiveConfigType
     * @param baseHomeworkResult
     * @return
     */
    private MapMessage vacationHomeworkForReadReciteResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_RESULT_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appResult = resultAnswer.getAppAnswers();
        List<String> processResultIds = new ArrayList<>();
        List<String> lessonIds = new ArrayList<>();
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            processResultIds.addAll(nraa.getAnswers().values());
            lessonIds.add(nraa.getLessonId());
        }
        Map<String, VacationHomeworkProcessResult> nhprMap = vacationHomeworkProcessResultDao.loads(processResultIds);
        Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        List<Map<String, Object>> readList = new ArrayList<>();
        List<Map<String, Object>> reciteList = new ArrayList<>();
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            Map<String, Object> map = new HashMap<>();
            NewBookCatalog newBookCatalog = lessonMap.get(nraa.getLessonId());
            String lessonName = "";
            if (newBookCatalog != null) {
                lessonName = newBookCatalog.getName();
            }
            String correction;
            if (nraa.getCorrection() != null) {
                correction = nraa.getCorrection().getDescription();
            } else if (nraa.getReview() != null) {
                correction = "阅";
            } else {
                correction = null;
            }
            List<String> audios = new LinkedList<>();

            //将音频的播放顺序按照学生答题录音时的顺序展示
            List<String> processIds = nraa.getAnswers()
                    .values()
                    .stream()
                    .sorted(String::compareTo)
                    .collect(Collectors.toList());
            for (String processResultId : processIds) {
                VacationHomeworkProcessResult nhpr = nhprMap.get(processResultId);
                if (nhpr == null) continue;
                if (CollectionUtils.isNotEmpty(nhpr.getFiles())) {
                    audios.addAll(nhpr
                            .getFiles()
                            .stream()
                            .flatMap(Collection::stream)
                            .map(NewHomeworkQuestionFileHelper::getFileUrl)
                            .collect(Collectors.toList()));
                }
            }
            map.put("lessonName", lessonName);
            map.put("correction", correction);
            map.put("audios", audios);
            if (QuestionBoxType.READ.equals(nraa.getQuestionBoxType())) {
                readList.add(map);
            } else {
                reciteList.add(map);
            }
        }
        return MapMessage.successMessage()
                .add("readList", readList)
                .add("reciteList", reciteList);
    }

    /**
     * 新版课文读背(引擎打分)
     *
     * @param objectiveConfigType
     * @param baseHomeworkResult
     * @return
     */
    public MapMessage vacationHomeworkForReadReciteWithScoreResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);

        // 总的达标段落数
        Integer totalStandardNum = 0;
        // 总的段落数
        Integer totalQuestionNum = 0;
        List<String> processResultIds = new ArrayList<>();
        Set<String> lessonIds = new HashSet<>();
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = new LinkedHashMap<>();
        if (resultAnswer != null && MapUtils.isNotEmpty(resultAnswer.getAppAnswers())) {
            appAnswers = resultAnswer.getAppAnswers();
            if (MapUtils.isNotEmpty(appAnswers)) {
                for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : appAnswers.entrySet()) {
                    NewHomeworkResultAppAnswer resultAppAnswer = entry.getValue();
                    if (resultAppAnswer != null) {
                        totalStandardNum += SafeConverter.toInt(resultAppAnswer.getStandardNum());
                        totalQuestionNum += SafeConverter.toInt(resultAppAnswer.getAppQuestionNum());

                        if (MapUtils.isEmpty(resultAppAnswer.getAnswers())) {
                            logger.error("processHomeworkTypeResult READ_RECITE_WITH_SCORE error, homeworkId:{}, studentId:{}",
                                    baseHomeworkResult.getHomeworkId(),
                                    baseHomeworkResult.getUserId());
                        } else {
                            processResultIds.addAll(resultAppAnswer.getAnswers().values());
                        }
                        lessonIds.add(resultAppAnswer.getLessonId());
                    }
                }
            }
        }
        String standardInfo = totalStandardNum + "/" + totalQuestionNum + "个段落达标";

        Map<String, VacationHomeworkProcessResult> nhprMap = vacationHomeworkProcessResultDao.loads(processResultIds);
        Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);

        List<Map> readList = new ArrayList<>();
        List<Map> reciteList = new ArrayList<>();
        for (NewHomeworkResultAppAnswer nraa : appAnswers.values()) {
            Map<String, Object> map = new HashMap<>();
            NewBookCatalog newBookCatalog = lessonMap.get(nraa.getLessonId());
            // 判断达标
            int standardNum = SafeConverter.toInt(nraa.getStandardNum());
            int appQuestionNum = SafeConverter.toInt(nraa.getAppQuestionNum());
            String standard = standardNum + "/" + appQuestionNum + " 段落达标";

            List<String> audios = new LinkedList<>();

            //将音频的播放顺序按照学生答题录音时的顺序展示
            List<String> processIds = nraa.getAnswers()
                    .values()
                    .stream()
                    .sorted(String::compareTo)
                    .collect(Collectors.toList());
            for (String processResultId : processIds) {
                VacationHomeworkProcessResult nhpr = nhprMap.get(processResultId);
                if (nhpr == null) {
                    continue;
                }
                if (CollectionUtils.isNotEmpty(nhpr.getOralDetails())) {
                    audios.addAll(nhpr
                            .getOralDetails()
                            .stream()
                            .flatMap(Collection::stream)
                            .map(BaseHomeworkProcessResult.OralDetail::getAudio)
                            .collect(Collectors.toList()));
                }
            }
            map.put("lessonId", nraa.getLessonId());
            map.put("lessonName", newBookCatalog == null ? "" : newBookCatalog.getName());
            map.put("standard", standard);
            map.put("audios", audios);
            if (QuestionBoxType.READ.equals(nraa.getQuestionBoxType())) {
                readList.add(map);
            } else {
                reciteList.add(map);
            }
        }
        return MapMessage.successMessage()
                .add("standardInfo", standardInfo)
                .add("readList", readList)
                .add("reciteList", reciteList);
    }

    private MapMessage vacationHomeworkForKeyPointResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_RESULT_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appResult = resultAnswer.getAppAnswers();
        List<String> processResultIds = new ArrayList<>();
        List<String> videoIds = new ArrayList<>();
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            processResultIds.addAll(nraa.getAnswers().values());
            videoIds.add(nraa.getVideoId());
        }
        Map<String, VacationHomeworkProcessResult> nhprMap = vacationHomeworkProcessResultDao.loads(processResultIds);
        Map<String, Video> videoMap = videoLoaderClient.loadVideoIncludeDisabled(videoIds);
        List<Map<String, Object>> results = new ArrayList<>();
        int totalRightCount = 0;
        int totalErrorCount = 0;
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            Video video = videoMap.get(nraa.getVideoId());
            int errorCount = 0;
            int rightCount = 0;
            for (String processResultId : nraa.getAnswers().values()) {
                VacationHomeworkProcessResult nhpr = nhprMap.get(processResultId);
                if (nhpr == null) continue;
                if (SafeConverter.toBoolean(nhpr.getGrasp())) {
                    rightCount++;
                    totalRightCount++;
                } else {
                    errorCount++;
                    totalErrorCount++;
                }

            }
            results.add(MapUtils.m("videoName", video != null ? video.getVideoName() : "未知名字",
                    "rightCount", rightCount,
                    "errorCount", errorCount));
        }
        return MapMessage.successMessage().add("datas", results)
                .add("rightCount", totalRightCount)
                .add("errorCount", totalErrorCount);
    }

    /**
     * 同步习题
     *
     * @param objectiveConfigType
     * @param baseHomeworkResult
     * @return
     */
    private MapMessage vacationHomeworkForExamResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        VacationHomework vacationHomework = vacationHomeworkDao.load(baseHomeworkResult.homeworkId);
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_RESULT_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        Collection<String> processResultIds = resultAnswer.getAnswers().values();
        Map<String, VacationHomeworkProcessResult> nhprMap = vacationHomeworkProcessResultDao.loads(processResultIds);
        List<Map<String, Object>> results = new ArrayList<>();
        int rightCount = 0;
        int errorCount = 0;
        for (String processResultId : resultAnswer.getAnswers().values()) {

            VacationHomeworkProcessResult nhpr = nhprMap.get(processResultId);
            if (nhpr == null) continue;
            if (SafeConverter.toBoolean(nhpr.getGrasp())) {
                rightCount++;
            } else {
                errorCount++;
            }
            results.add(MapUtils.m(
                    "questionId", nhpr.getQuestionId(),
                    "score", new BigDecimal(SafeConverter.toDouble(nhpr.getScore())).setScale(0, BigDecimal.ROUND_HALF_UP).intValue(),
                    "right", SafeConverter.toBoolean(nhpr.getGrasp()),
                    "userAnswers", nhpr.getUserAnswers()));
        }

        MapMessage mapMessage = MapMessage.successMessage()
                .add("datas", results)
                .add("rightCount", rightCount)
                .add("errorCount", errorCount)
                .add("duration", resultAnswer.getDuration());

        String durationStr = "";    // 用时
        Integer timeLimit = 0;      // 限时
        Boolean mentalAward = false;// 奖励
        if (ObjectiveConfigType.MENTAL_ARITHMETIC.equals(objectiveConfigType)) {
            // 用时
            Long duration = 0L;
            if (resultAnswer.getDuration() != null) {
                duration = SafeConverter.toLong(resultAnswer.processDuration());
            }
            if (duration > 0) {
                long minute = duration / 60;
                long second = duration % 60;
                if (minute == 0) {
                    durationStr = second + "\"";
                } else {
                    durationStr = minute + "'" + second + "\"";
                }
            }

            // 是否限时及奖励
            List<NewHomeworkPracticeContent> practices = vacationHomework.getPractices();
            if (CollectionUtils.isNotEmpty(practices)) {
                for (NewHomeworkPracticeContent practiceContent : practices) {
                    if (practiceContent.getType().equals(objectiveConfigType)) {
                        timeLimit = practiceContent.getTimeLimit() != null ? practiceContent.getTimeLimit().getTime() : 0;
                        mentalAward = practiceContent.getMentalAward() != null ? practiceContent.getMentalAward() : false;
                    }
                }
            }
            mapMessage.add("durationStr", durationStr)
                    .add("score", new BigDecimal(SafeConverter.toDouble(resultAnswer.getScore())).setScale(0, BigDecimal.ROUND_HALF_UP).intValue())
                    .add("credit", baseHomeworkResult.getCredit())
                    .add("timeLimit", timeLimit)
                    .add("mentalAward", mentalAward);
        }

        return mapMessage;
    }

    private MapMessage vacationHomeworkForSubjectivityResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_RESULT_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        Collection<String> processResultIds = resultAnswer.getAnswers().values();
        Map<String, VacationHomeworkProcessResult> nhprMap = vacationHomeworkProcessResultDao.loads(processResultIds);
        List<Map<String, Object>> results = new ArrayList<>();
        for (String processResultId : resultAnswer.getAnswers().values()) {
            VacationHomeworkProcessResult nhpr = nhprMap.get(processResultId);
            results.add(
                    MapUtils.m("questionId", nhpr.getQuestionId(),
                            "review", nhpr.getReview() != null && nhpr.getReview(),
                            "correction", nhpr.getCorrection() != null ? nhpr.getCorrection().getDescription() : ""));
        }
        return MapMessage.successMessage().add("datas", results);
    }

    @Override
    public JztStudentHomeworkReport loadJztStudentHomeworkReport(NewHomeworkResult homeworkResult, NewHomework newHomework, StudentDetail studentDetail) {
        String homeworkId = newHomework.getId();
        NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(homeworkId);

        JztStudentHomeworkReport studentHomeworkReport = new JztStudentHomeworkReport();
        studentHomeworkReport.setChecked(newHomework.getChecked());
        studentHomeworkReport.setExpired(newHomework.getEndTime().before(new Date()));
        studentHomeworkReport.setTeacherId(newHomework.getTeacherId());
        studentHomeworkReport.setEndDate(DateUtils.dateToString(newHomework.getEndTime(), "MM月dd日 HH:mm"));
        studentHomeworkReport.setPlanDuration(new BigDecimal(newHomework.getDuration()).divide(new BigDecimal(60), 0, BigDecimal.ROUND_UP).intValue() + "分钟");
        studentHomeworkReport.setHomeworkName(DateUtils.dateToString(newHomework.getCreateAt(), "MM月dd日") + newHomework.getSubject().getValue() + "作业");
        boolean scoreRegionFlag = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
        studentHomeworkReport.setSubject(newHomework.getSubject().name());
        studentHomeworkReport.setUnitNameSet(newHomeworkBook.processUnitNameList());
        String bookId = newHomeworkBook.processBookId();
        studentHomeworkReport.setBookId(bookId);

        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = Maps.newLinkedHashMap();
        if (homeworkResult != null && homeworkResult.getPractices() != null) {
            practices = homeworkResult.getPractices();
        }

        Set<ObjectiveConfigType> objectiveConfigTypes = newHomework.getPractices().stream().map(NewHomeworkPracticeContent::getType).collect(Collectors.toSet());
        if (objectiveConfigTypes.contains(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC)) {
            studentHomeworkReport.setHasOcrMentalArithmetic(true);
        }

        // 各作业形式完成详情
        for (NewHomeworkPracticeContent content : newHomework.getPractices()) {
            NewHomeworkResultAnswer resultAnswer = practices.get(content.getType());
            JztHomeworkReport.ScoreStatus typeScoreStatus = JztHomeworkReport.ScoreStatus.process(scoreRegionFlag, resultAnswer != null && resultAnswer.isFinished(), NOT_SHOW_SCORE_TYPE.contains(content.getType()));
            //计算分数显示
            String scoreDesc = calculateScoreDesc(content, resultAnswer, content.getType(), typeScoreStatus);
            studentHomeworkReport.getObjectiveConfigTypeDetails().add(new JztStudentHomeworkReport.ObjectiveConfigTypeDetail(content.getType().name(), content.getType().getValue(), scoreDesc, typeScoreStatus));
        }

        JztHomeworkReport.ScoreStatus scoreStatus = JztHomeworkReport.ScoreStatus.process(scoreRegionFlag, homeworkResult != null && homeworkResult.isFinished(), NOT_SHOW_SCORE_TYPE.containsAll(objectiveConfigTypes));
        studentHomeworkReport.setScoreStatus(scoreStatus);
        if (JztHomeworkReport.ScoreStatus.UN_FINISHED.equals(scoreStatus)) {
            return studentHomeworkReport;
        }

        Integer homeworkScore = Objects.requireNonNull(homeworkResult).processScore();
        studentHomeworkReport.setScore(JztStudentHomeworkReport.ScoreStatus.LEVEL.equals(scoreStatus) ? ScoreLevel.processLevel(homeworkScore).getLevel() : SafeConverter.toString(homeworkScore));

        // 错题数
        //英语-纸质听写从图片识别结果中获取
        long ocrDictationErrorCount = 0;
        if (objectiveConfigTypes.contains(ObjectiveConfigType.OCR_DICTATION)) {
            List<String> ocrDictationProcessIds = practices.get(ObjectiveConfigType.OCR_DICTATION).getOcrDictationAnswers();
            Map<String, NewHomeworkProcessResult> ocrDictationProcessResultMap = newHomeworkProcessResultLoader.loads(homeworkId, ocrDictationProcessIds);
            ocrDictationErrorCount = ocrDictationProcessResultMap.values().stream()
                    .filter(p -> p != null
                            && p.getOcrDictationImageDetail() != null
                            && CollectionUtils.isNotEmpty(p.getOcrDictationImageDetail().getForms())
                    )
                    .map(NewHomeworkProcessResult::getOcrDictationImageDetail)
                    .map(OcrMentalImageDetail::getForms)
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .filter(f -> Objects.equals(0, f.getJudge()))
                    .filter(NewHomeworkUtils.distinctByKey(OcrMentalImageDetail.Form::getText))
                    .count();
        }
        //计算其他题型的错题
        List<String> allHomeworkProcessIds = homeworkResult.findAllHomeworkProcessIds(Boolean.TRUE);
        Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(homeworkId, allHomeworkProcessIds);
        List<NewHomeworkProcessResult> errorProcessResults = processResultMap.values()
                .stream()
                .filter(p -> NewHomeworkConstants.GenerateSelfStudyHomeworkConfigTypes.contains(p.getObjectiveConfigType()) && !p.getGrasp())
                .collect(Collectors.toList());
        studentHomeworkReport.setErrorQuestionCount(errorProcessResults.size() + SafeConverter.toInt(ocrDictationErrorCount));
        studentHomeworkReport.setDuration(homeworkResult.processDuration());

        // 错题订正模块
        studentHomeworkReport.setCorrectDetail(getCorrectDetail(homeworkResult, newHomework, errorProcessResults));
        // 语文薄弱巩固模块(未达标课文朗读, 课文背诵)
        studentHomeworkReport.setReadRecite(handleReadRecite(homeworkResult, processResultMap));
        // 英语,语文薄弱巩固模块
        if (Subject.ENGLISH.equals(newHomework.getSubject()) || Subject.CHINESE.equals(newHomework.getSubject())) {
            JztStudentHomeworkReport.SyllableModule syllableModule = syllableModule(newHomework, homeworkResult, processResultMap);
            studentHomeworkReport.setSyllableModule(syllableModule);
            studentHomeworkReport.setSubstandardSyllableCount(syllableModule.getWeakCount());
            syllableModule.setBookId(bookId);
        }
        return studentHomeworkReport;
    }

    private String calculateScoreDesc(NewHomeworkPracticeContent content, NewHomeworkResultAnswer resultAnswer, ObjectiveConfigType type, JztHomeworkReport.ScoreStatus typeScoreStatus) {
        String scoreDesc = "未完成";
        if (!JztHomeworkReport.ScoreStatus.UN_FINISHED.equals(typeScoreStatus)) {
            if (ObjectiveConfigType.WORD_RECOGNITION_AND_READING.equals(content.getType())) {
                scoreDesc = calculateNotScoreDesc(resultAnswer, NewHomeworkConstants.WORD_RECOGNITION_AND_READING_STANDARD);
            } else if (ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(content.getType())) {
                scoreDesc = calculateNotScoreDesc(resultAnswer, NewHomeworkConstants.READ_RECITE_STANDARD);
            } else if (JztHomeworkReport.ScoreStatus.NOT_SCORE.equals(typeScoreStatus)) {
                scoreDesc = "已完成";
            } else {
                Integer score = resultAnswer.processScore(type);
                scoreDesc = JztStudentHomeworkReport.ScoreStatus.LEVEL.equals(typeScoreStatus) ? ScoreLevel.processLevel(score).getLevel() : SafeConverter.toString(score);
            }
        }
        return scoreDesc;
    }

    private String calculateNotScoreDesc(NewHomeworkResultAnswer resultAnswer, int wordRecognitionAndReadingStandard) {
        long wordRecognitionReadingStandardCount = resultAnswer.getAppAnswers().values().stream().filter(appAnswer -> {
            double value = new BigDecimal(SafeConverter.toInt(appAnswer.getStandardNum()) * 100).divide(new BigDecimal(appAnswer.getAnswers().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            return value > wordRecognitionAndReadingStandard;
        }).count();

        if (wordRecognitionReadingStandardCount == 0) {
            return "未达标";
        }
        if (wordRecognitionReadingStandardCount == resultAnswer.getAppAnswers().size()) {
            return "达标";
        } else {
            return StringUtils.join(wordRecognitionReadingStandardCount, "/", resultAnswer.getAppAnswers().size(), "达标");
        }
    }

    /**
     * 口语音频
     */
    private JztStudentHomeworkReport.SyllableModule syllableModule(NewHomework newHomework, NewHomeworkResult newHomeworkResult, Map<String, NewHomeworkProcessResult> allNewHomeworkProcessResultMap) {

        JztStudentHomeworkReport.SyllableModule syllableModule = new JztStudentHomeworkReport.SyllableModule();
        if (newHomeworkResult.getPractices().containsKey(ObjectiveConfigType.BASIC_APP)) {
            Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = Maps.filterValues(allNewHomeworkProcessResultMap, p -> p != null && ObjectiveConfigType.BASIC_APP.equals(p.getObjectiveConfigType()));
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.BASIC_APP);
            LinkedHashMap<String, NewHomeworkResultAppAnswer> apps = newHomeworkResultAnswer != null ? newHomeworkResultAnswer.getAppAnswers() : new LinkedHashMap<>();
            List<String> homeworkSyllableIds = new LinkedList<>();
            boolean allPc = true;
            boolean hasVoice = false;
            for (NewHomeworkResultAppAnswer app : apps.values()) {
                PracticeType practiceType = practiceLoaderClient.loadPractice(app.getPracticeId());
                if (practiceType.getNeedRecord()) {
                    hasVoice = true;
                    if (app.getAnswers() == null) {
                        continue;
                    }
                    for (String processId : app.getAnswers().values()) {
                        NewHomeworkProcessResult processResult = newHomeworkProcessResultMap.get(processId);
                        if (processResult == null) {
                            continue;
                        }

                        String voiceUrl = CollectionUtils.isEmpty(processResult.getOralDetails()) ||
                                CollectionUtils.isEmpty(processResult.getOralDetails().get(0)) ||
                                StringUtils.isBlank(processResult.getOralDetails().get(0).get(0).getAudio()) ?
                                null :
                                processResult.getOralDetails().get(0).get(0).getAudio();
                        voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, processResult.getVoiceEngineType());
                        if (processResult.getAppOralScoreLevel() == AppOralScoreLevel.D || processResult.getAppOralScoreLevel() == AppOralScoreLevel.C) {
                            String month = DateUtils.dateToString(newHomework.getCreateAt(), "yyyyMMdd");
                            String homeworkSyllableId = VoiceEngineTypeUtils.handleAudioUrl(month, voiceUrl, processResult.getVoiceEngineType(), processResult.getHomeworkId(), processResult.getUserId());
                            if (homeworkSyllableId != null) {
                                homeworkSyllableIds.add(homeworkSyllableId);
                            }
                        }
                        if (processResult.getClientType() != null && (!processResult.getClientType().equals("pc"))) {
                            allPc = false;
                        }
                    }
                }
            }

            syllableModule.setAllPc(allPc);
            syllableModule.setHasVoice(hasVoice);
            Map<String, NewHomeworkSyllable> newHomeworkSyllableMap = newHomeworkSyllableDao.loads(homeworkSyllableIds);
            PronunciationRecord pronunciationRecord = new PronunciationRecord(newHomeworkSyllableMap.values());//处理得到数据
            syllableModule.setWords(pronunciationRecord.getWords());
            syllableModule.setWeakCount(pronunciationRecord.getCount());
            syllableModule.setLines(pronunciationRecord.getLines());
            syllableModule.setUnitAndSentenceList(pronunciationRecord.getUnitAndSentenceList());
        }
        syllableModule.setVoiceFlag(true);
        return syllableModule;
    }

    /**
     * 家长通学生作业报告->语文薄弱巩固模块
     */
    private List<Map<String, Object>> handleReadRecite(NewHomeworkResult newHomeworkResult, Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap) {
        if (!newHomeworkResult.getPractices().containsKey(ObjectiveConfigType.READ_RECITE_WITH_SCORE)) {
            return Collections.emptyList();
        }
        NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.READ_RECITE_WITH_SCORE);
        List<Map<String, Object>> readReciteList = new LinkedList<>();
        if (newHomeworkResultAnswer.getAppAnswers() != null) {
            List<Map<String, Object>> readList = new LinkedList<>();
            List<Map<String, Object>> reciteList = new LinkedList<>();
            for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                if (MapUtils.isEmpty(appAnswer.getAnswers()) || appAnswer.getQuestionBoxType() == null) {
                    continue;
                }
                Map<String, Object> basicData = new LinkedHashMap<>();
                double value = new BigDecimal(SafeConverter.toInt(appAnswer.getStandardNum()) * 100).divide(new BigDecimal(appAnswer.getAnswers().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                if (value > NewHomeworkConstants.READ_RECITE_STANDARD) {
                    continue;
                }
                basicData.put("standard", false);
                basicData.put("questionBoxTypeName", appAnswer.getQuestionBoxType().getName());
                List<String> voices = new LinkedList<>();
                basicData.put("voices", voices);
                for (String pid : appAnswer.getAnswers().values()) {
                    NewHomeworkProcessResult processResult = newHomeworkProcessResultMap.get(pid);
                    if (processResult != null && CollectionUtils.isNotEmpty(processResult.getOralDetails())) {
                        voices.addAll(processResult.getOralDetails()
                                .stream()
                                .flatMap(Collection::stream)
                                .filter(Objects::nonNull)
                                .map(o -> VoiceEngineTypeUtils.getAudioUrl(o.getAudio(), processResult.getVoiceEngineType()))
                                .filter(StringUtils::isNotBlank)
                                .collect(Collectors.toList()));
                    }
                }
                if (appAnswer.getQuestionBoxType() == QuestionBoxType.READ) {
                    readList.add(basicData);
                } else {
                    reciteList.add(basicData);
                }
            }
            readReciteList.addAll(readList);
            readReciteList.addAll(reciteList);
        }
        return readReciteList;
    }

    /**
     * 家长通学生作业报告->订正模块
     */
    private JztStudentHomeworkReport.CorrectDetail getCorrectDetail(NewHomeworkResult newHomeworkResult, NewHomework newHomework, List<NewHomeworkProcessResult> errorProcessResults) {
        JztStudentHomeworkReport.CorrectDetail correctDetail = new JztStudentHomeworkReport.CorrectDetail();
        HomeworkCorrectStatus correctStatus = newHomeworkResultService.getHomeworkCorrectStatus(newHomework, newHomeworkResult);
        correctDetail.setCorrectStatus(correctStatus);
        if (!HomeworkCorrectStatus.WITHOUT_CORRECT.equals(correctStatus)) {
            Set<String> selfStudyHomeworkIds = homeworkSelfStudyRefDao.loadSelfStudyHomeworkIds(Collections.singletonList(newHomework.getId()), newHomeworkResult.getUserId());
            ArrayList<String> selfStudyHomeworkIdList = Lists.newArrayList(selfStudyHomeworkIds);
            SelfStudyHomework selfStudyHomework = selfStudyHomeworkDao.load(selfStudyHomeworkIdList.get(0));

            List<NewHomeworkApp> newHomeworkApps = selfStudyHomework.findNewHomeworkApps(ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS);
            String setCorrectDesc = StringUtils.join("本次", errorProcessResults.size(), "道错题涉及主要知识点如下：");
            List<String> knowledgeList = new LinkedList<>();
            if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
                Set<String> courseIds = newHomeworkApps.stream().map(NewHomeworkApp::getCourseId).collect(Collectors.toSet());
                Map<String, IntelDiagnosisCourse> diagnosisCourseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(courseIds);
                Map<String, String> courseVariantNameMap = diagnoseReportService.getCourseVariantNameMapByCourseIds(diagnosisCourseMap);
                Map<String, String> courseKnowledgeMap = diagnoseReportService.getCourseKnowledgePointNameMap(diagnosisCourseMap);
                knowledgeList.addAll(courseVariantNameMap.values());
                knowledgeList.addAll(courseKnowledgeMap.values());
            } else {
                List<String> allQuestionIds = selfStudyHomework.findAllQuestionIds();
                Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestions(allQuestionIds);
                Set<String> knowledgePointIds = questionMap.values().stream().flatMap(o -> o.mainKnowledgePointList().stream()).collect(Collectors.toSet());
                Map<String, NewKnowledgePoint> knowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePointsIncludeDeleted(knowledgePointIds);
                knowledgeList = knowledgePointMap.values().stream().map(NewKnowledgePoint::getName).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(knowledgeList)) {
                    setCorrectDesc = StringUtils.join("本次", errorProcessResults.size(), "道错题集中在以下的作业中：");
                    knowledgeList = errorProcessResults.stream().map(p -> p.getObjectiveConfigType().getValue()).collect(Collectors.toList());
                }
            }
            knowledgeList = knowledgeList.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
            correctDetail.setKnowledgeList(knowledgeList);
            correctDetail.setCorrectDesc(setCorrectDesc);
        }
        return correctDetail;
    }


    @Override
    public JztClazzHomeworkReport loadJztClazzHomeworkReport(NewHomework newHomework, StudentDetail studentDetail, String cdnUrl) {
        NewHomework.Location location = newHomework.toLocation();
        JztClazzHomeworkReport clazzReport = new JztClazzHomeworkReport();
        //处理作业基础信息
        NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(newHomework.getId());
        clazzReport.setEndDate(DateUtils.dateToString(newHomework.getEndTime(), "MM月dd日 HH:mm"));
        clazzReport.setPlanDuration(new BigDecimal(newHomework.getDuration()).divide(new BigDecimal(60), 0, BigDecimal.ROUND_UP).intValue() + "分钟");
        clazzReport.setHomeworkName(DateUtils.dateToString(newHomework.getEndTime(), "MM月dd日") + newHomework.getSubject().getValue() + "作业");
        clazzReport.setSubject(newHomework.getSubject().getValue());
        clazzReport.setUnitNameSet(newHomeworkBook.processUnitNameList());
        //处理班级成绩
        boolean haveScore = false;
        for (ObjectiveConfigType objectiveConfigType : newHomework.findPracticeContents().keySet()) {
            if (!NOT_SHOW_SCORE_TYPE.contains(objectiveConfigType)) {
                haveScore = true;
                break;
            }
        }
        List<Long> userIds = studentLoaderClient.loadGroupStudentIds(newHomework.getClazzGroupId());
        if (haveScore) {
            Map<Long, NewHomeworkResult> homeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(location, userIds, false);
            Integer totalScore = 0;
            Integer totalCount = 0;
            Integer maxScore = 0;
            for (NewHomeworkResult result : homeworkResultMap.values()) {
                if (result.isFinished()) {
                    Integer score = result.processScore();
                    totalScore += score;
                    totalCount++;
                    if (score > maxScore) {
                        maxScore = score;
                    }
                }
            }
            if (totalCount > 0) {
                clazzReport.setMaxScore(maxScore);
                clazzReport.setMaxScoreLevel(ScoreLevel.processLevel(maxScore).getLevel());
                Integer avgScore = new BigDecimal(totalScore).divide(new BigDecimal(totalCount), 0, BigDecimal.ROUND_HALF_UP).intValue();
                clazzReport.setAvgScore(avgScore);
                clazzReport.setAvgScoreLebel(ScoreLevel.processLevel(avgScore).getLevel());
            }
            boolean isHitScoreLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
            clazzReport.setScoreStatus(isHitScoreLevel ? JztHomeworkReport.ScoreStatus.LEVEL : JztHomeworkReport.ScoreStatus.SCORE);
        } else {
            NewAccomplishment newAccomplishment = newAccomplishmentLoader.loadNewAccomplishment(location);
            int finishCount = 0;
            if (newAccomplishment != null && newAccomplishment.getDetails() != null) {
                finishCount = newAccomplishment.getDetails().size();
            }
            clazzReport.setFinishCount(finishCount);
            clazzReport.setUserCount(userIds.size());
            clazzReport.setScoreStatus(JztHomeworkReport.ScoreStatus.NOT_SCORE);
        }
        //处理老师分享内容
        List<String> shareList = newHomework.getReportShareParts();
        if (newHomework.isHomeworkChecked() && newHomework.getAdditions() != null && CollectionUtils.isNotEmpty(shareList)) {
            NewHomeworkShareReport shareReport = processNewHomeworkShareReport(location.getId(), null, cdnUrl);
            clazzReport.setReportStatus(JztClazzHomeworkReport.ReportStatus.shared);
            clazzReport.setShareReport(shareReport);
            clazzReport.setShareList(shareList);
        } else if (!newHomework.isHomeworkChecked()) {
            clazzReport.setReportStatus(JztClazzHomeworkReport.ReportStatus.unterminated);
        } else {
            clazzReport.setReportStatus(JztClazzHomeworkReport.ReportStatus.unshared);
        }
        return clazzReport;
    }

    @Override
    public JztHomeworkNotice loadJztHomeworkNotice(NewHomework newHomework, StudentDetail studentDetail, String cdnUrl, Long parentId) {
        NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(newHomework.getId());

        JztHomeworkNotice jztHomeworkNotice = new JztHomeworkNotice();

        String bookCoverImageUrl = "";
        String bookId = newHomeworkBook != null ? newHomeworkBook.processBookId() : "";
        if (StringUtils.isNotBlank(bookId)) {
            NewBookProfile bookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
            if (bookProfile != null) {
                bookCoverImageUrl = NewHomeworkUtils.compressBookImg(bookProfile.getImgUrl());
            }
        }

        jztHomeworkNotice.setSubject(newHomework.getSubject().name());
        jztHomeworkNotice.setHomeworkName(DateUtils.dateToString(newHomework.getEndTime(), "MM月dd日 ") + newHomework.getSubject().getValue() + "作业");
        jztHomeworkNotice.setBookCoverImageUrl(bookCoverImageUrl);
        jztHomeworkNotice.setUnitNameSet(newHomeworkBook != null ? newHomeworkBook.processUnitNameList() : Collections.emptySet());
        jztHomeworkNotice.setPlanDuration(new BigDecimal(newHomework.getDuration()).divide(new BigDecimal(60), 0, BigDecimal.ROUND_UP).intValue() + "分钟");
        int questionCount = newHomework.findAllQuestionIds().size();
        String questionCountStr = questionCount + "道题";
        if (newHomework.findPracticeContents().containsKey(ObjectiveConfigType.ORAL_COMMUNICATION)) {
            questionCount += newHomework.findNewHomeworkApps(ObjectiveConfigType.ORAL_COMMUNICATION).size();
            questionCountStr = questionCount + "道题";
        }
        if (newHomework.findPracticeContents().containsKey(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC)) {
            if (questionCount == 0) {
                questionCount = 1;
                questionCountStr = "1份";
            } else {
                questionCount += 1;
                questionCountStr = questionCount + "道题";
            }
        }
        jztHomeworkNotice.setQuestionCount(questionCount);
        jztHomeworkNotice.setQuestionCountStr(questionCountStr);
        jztHomeworkNotice.setEndDate(DateUtils.dateToString(newHomework.getEndTime(), "MM月dd日 HH:mm"));


        List<User> students = studentLoaderClient.loadGroupStudents(Collections.singleton(newHomework.getClazzGroupId())).get(newHomework.getClazzGroupId());
        Map<Long, User> studentMap = students.stream().collect(Collectors.toMap(User::getId, Function.identity()));

        Map<Long, NewHomeworkResult> homeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentMap.keySet(), false);
        NewHomeworkResult newHomeworkResult = homeworkResultMap.get(studentDetail.getId());

        Map<ObjectiveConfigType, NewHomeworkResultAnswer> answerMap = newHomeworkResult != null && newHomeworkResult.getPractices() != null ? newHomeworkResult.getPractices() : Collections.emptyMap();
        List<JztHomeworkNotice.PracticeContent> practiceContents = new ArrayList<>();
        List<NewHomeworkPracticeContent> homeworkPractices = newHomework.getPractices();
        for (NewHomeworkPracticeContent practiceContent : homeworkPractices) {
            JztHomeworkNotice.PracticeContent practice = new JztHomeworkNotice.PracticeContent();
            ObjectiveConfigType objectiveConfigType = practiceContent.getType();
            practice.setObjectiveConfigType(objectiveConfigType);
            practice.setObjectiveConfigTypeName(objectiveConfigType.getValue());
            practice.setSummary(processPracticeSummary(practiceContent, newHomework.getSubject()));
            NewHomeworkResultAnswer answer = answerMap.get(objectiveConfigType);
            practice.setFinished(answer != null && answer.isFinished());
            practiceContents.add(practice);
        }
        jztHomeworkNotice.setPractices(practiceContents);

        // 线下作业内容
        jztHomeworkNotice.setOfflineHomework(getOfflineHomework(newHomework, studentDetail, parentId));

        List<JztHomeworkNotice.FinishedStudent> finishedStudents = new ArrayList<>();
        List<NewHomeworkResult> toSort = new ArrayList<>();
        for (NewHomeworkResult result : homeworkResultMap.values()) {
            if (result.isFinished()) {
                toSort.add(result);
            }
        }
        toSort.sort(Comparator.comparing(BaseHomeworkResult::getFinishAt));
        for (NewHomeworkResult result : toSort) {
            Long studentId = result.getUserId();
            User user = studentMap.get(studentId);
            JztHomeworkNotice.FinishedStudent finishedStudent = new JztHomeworkNotice.FinishedStudent();
            finishedStudent.setStudentId(result.getUserId());
            finishedStudent.setStudentName(user != null ? user.fetchRealname() : "");
            finishedStudent.setAvatarImgUrl(NewHomeworkUtils.getUserAvatarImgUrl(cdnUrl, user != null ? user.fetchImageUrl() : ""));
            finishedStudent.setFinishAt(DateUtils.dateToString(result.getFinishAt(), "MM-dd HH:mm"));
            finishedStudents.add(finishedStudent);
        }
        jztHomeworkNotice.setFinishedStudents(finishedStudents);
        return jztHomeworkNotice;
    }

    private JztHomeworkNotice.OfflineHomework getOfflineHomework(NewHomework newHomework, StudentDetail studentDetail, Long parentId) {
        Map<String, OfflineHomework> offlineHomeworkMap = offlineHomeworkDao.loadByNewHomeworkIds(Collections.singleton(newHomework.getId()));
        OfflineHomework offlineHomework = offlineHomeworkMap.get(newHomework.getId());
        if (offlineHomework != null) {
            Map<String, List<OfflineHomeworkSignRecord>> offlineHomeworkSignMap = jxtLoaderClient.getSignRecordByOfflineHomeworkIds(Collections.singleton(offlineHomework.getId()));
            List<OfflineHomeworkSignRecord> homeworkSignRecords = offlineHomeworkSignMap.get(offlineHomework.getId());
            boolean hadSign = CollectionUtils.isNotEmpty(homeworkSignRecords) && homeworkSignRecords.stream().anyMatch(p -> Objects.equals(p.getParentId(), parentId) && Objects.equals(p.getStudentId(), studentDetail.getId()));
            JztHomeworkNotice.OfflineHomework offline = new JztHomeworkNotice.OfflineHomework();
            offline.setOhid(offlineHomework.getId());
            offline.setNeedSign(offlineHomework.getNeedSign());
            offline.setHadSign(hadSign);
            List<String> offlineHomeworkContentList = offlineHomework.getPractices().stream()
                    .map(OfflineHomeworkPracticeContent::toString)
                    .collect(Collectors.toList());
            offline.setOfflineHomeworkContents(offlineHomeworkContentList);
            offline.setSignReadContent(StringUtils.join(studentDetail.fetchRealnameIfBlankId(), "已完成今天的", newHomework.getSubject().getValue(), "练习"));
            return offline;
        }
        return null;
    }

    private String processPracticeSummary(NewHomeworkPracticeContent practiceContent, Subject subject) {
        String summary;
        ObjectiveConfigType type = practiceContent.getType();
        switch (type) {
            case BASIC_APP:
            case NATURAL_SPELLING:
            case KEY_POINTS:
                summary = "共" + practiceContent.getApps().size() + "题";
                break;
            case LEVEL_READINGS:
                summary = "共" + practiceContent.getApps().size() + "本" + (Subject.CHINESE == subject ? "（趣味绘本听、看、练）" : "");
                break;
            case DUBBING:
            case DUBBING_WITH_SCORE:
                summary = "共" + practiceContent.getApps().size() + "集";
                break;
            case ORAL_COMMUNICATION:
                summary = "共" + practiceContent.getApps().size() + "个主题";
                break;
            case OCR_MENTAL_ARITHMETIC:
                summary = practiceContent.getWorkBookName() + " " + practiceContent.getHomeworkDetail();
                break;
            case WORD_RECOGNITION_AND_READING:
                summary = "共" + practiceContent.getApps().size() + "篇" + "（通过读音、笔顺、字义等认识生字）";
                break;
            case READ_RECITE:
            case READ_RECITE_WITH_SCORE:
                summary = "共" + practiceContent.getApps().size() + "篇" + "（智能语音打分，提升读背效果）";
                break;
            case BASIC_KNOWLEDGE:
                summary = "共" + practiceContent.getQuestions().size() + "题" + "（通过字词、修辞等同步习题巩固基础）";
                break;
            case CHINESE_READING:
                summary = "共" + practiceContent.getQuestions().size() + "题" + "（课内同步的阅读理解练习）";
                break;
            case UNIT_QUIZ:
                summary = "共" + practiceContent.getQuestions().size() + "题" + (Subject.CHINESE == subject ? "（精选试卷，复习专练）" : "");
                break;
            case WORD_TEACH_AND_PRACTICE:
                summary = "共" + practiceContent.getApps().size() + "题包" + "（在句中语感和文化延伸中深入学习汉字）";
                break;
            default:
                summary = "共" + practiceContent.getQuestions().size() + "题";
                break;
        }
        return summary;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapMessage loadDiagnosisHabitDetail(String homeworkId) {
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        List<Map<String, Object>> diagnosisHabitDetail = new ArrayList<>();
        String diagnosisHabitUrl = NewHomeworkConstants.DIAGNOSIS_HABIT_TEST_URL;
        if (RuntimeMode.isStaging()) {
            diagnosisHabitUrl = NewHomeworkConstants.DIAGNOSIS_HABIT_STAGING_URL;
        } else if (RuntimeMode.isProduction()) {
            diagnosisHabitUrl = NewHomeworkConstants.DIAGNOSIS_HABIT_PRODUCT_URL;
        }
        // 调第一个接口先拿到症状列表
        String url = StringUtils.formatMessage(diagnosisHabitUrl + "homework/report/diagnosis/habit/{}/{}", newHomework.getClazzGroupId(), homeworkId);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                .get(url)
                .socketTimeout(500)
                .execute();
        List<Map<String, Object>> diagnosis = Collections.emptyList();
        if (response != null && response.getStatusCode() == 200) {
            Map<String, Object> map = JsonUtils.fromJson(response.getResponseString());
            if (MapUtils.isNotEmpty(map)) {
                diagnosis = (List<Map<String, Object>>) map.get("diagnosis");
            }
        } else {
            logger.error("调用:{}失败, response: {}",
                    url,
                    response != null ? response.getResponseString() : "");
        }
        Map<String, List<Map<String, Object>>> diagnosisHabitDetailMap = new HashMap<>();
        // 对每一个症状通过接口拿到详情数据
        if (CollectionUtils.isNotEmpty(diagnosis)) {
            for (Map<String, Object> diagnosisMapper : diagnosis) {
                String category = SafeConverter.toString(diagnosisMapper.get("category"));
                String symptomId = SafeConverter.toString(diagnosisMapper.get("symptom_id"));
                String requestUrl = StringUtils.formatMessage(diagnosisHabitUrl + "homework/report/diagnosis/symptom/{}/{}/{}", symptomId, newHomework.getClazzGroupId(), homeworkId);
                AlpsHttpResponse alpsHttpResponse = HttpRequestExecutor.defaultInstance()
                        .get(requestUrl)
                        .socketTimeout(100)
                        .execute();
                if (alpsHttpResponse != null && alpsHttpResponse.getStatusCode() == 200) {
                    Map<String, Object> map = JsonUtils.fromJson(alpsHttpResponse.getResponseString());
                    if (MapUtils.isNotEmpty(map)) {
                        List<Object> patients = (List<Object>) map.get("patients");
                        if (CollectionUtils.isNotEmpty(patients)) {
                            List<Long> studentIds = patients.stream().map(SafeConverter::toLong).collect(Collectors.toList());
                            Map<Long, Student> studentMap = studentLoaderClient.loadStudents(studentIds);
                            map.put("students", studentMap.values().stream().map(User::fetchRealname).collect(Collectors.toList()));
                            map.remove("patients");
                            map.remove("group_id");
                            map.remove("homework_id");
                            map.remove("symptom_id");
                        }
                        diagnosisHabitDetailMap.computeIfAbsent(category, k -> new ArrayList<>()).add(map);
                    }
                } else {
                    logger.error("调用:{}失败, response: {}",
                            requestUrl,
                            response != null ? response.getResponseString() : "");
                }
            }
        }
        if (MapUtils.isNotEmpty(diagnosisHabitDetailMap)) {
            diagnosisHabitDetailMap.forEach((k, v) -> diagnosisHabitDetail.add(MapUtils.m("category", k, "symptoms", v)));
        }
        return MapMessage.successMessage().add("diagnosisHabitDetail", diagnosisHabitDetail);
    }

    @Override
    public MapMessage clazzWordTeachModuleDetail(Long teacherId, String hid, String stoneId, WordTeachModuleType wordTeachModuleType) {
        try {
            NewHomework newHomework = newHomeworkLoader.load(hid);
            if (newHomework == null) {
                return MapMessage.errorMessage("作业不存在");
            }
            ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.WORD_TEACH_AND_PRACTICE;
            NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(objectiveConfigType);
            if (target == null) {
                return MapMessage.errorMessage("作业不包含" + objectiveConfigType.getValue());
            }
            if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacherId, newHomework.getClazzGroupId())) {
                return MapMessage.errorMessage("没有权限查看此作业报告").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_PERMISSION);
            }
            // 该题包原始信息
            NewHomeworkApp newHomeworkApp = new NewHomeworkApp();
            for (NewHomeworkApp app : target.getApps()) {
                if (app.getStoneDataId().equals(stoneId)) {
                    newHomeworkApp = app;
                }
            }
            // 班级用户信息
            Map<Long, User> userMap = studentLoaderClient
                    .loadGroupStudents(newHomework.getClazzGroupId())
                    .stream()
                    .collect(Collectors
                            .toMap(LongIdEntity::getId, Function.identity()));
            Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), true);

            // 完成学生ID信息
            List<Long> studentIds = new ArrayList<>();
            // 字词训练 processResultId
            List<String> wordExerciseProcessResultIds = new ArrayList<>();
            // 图文入韵 processResultId
            List<String> imageTextRhymeProcessResultIds = new ArrayList<>();
            for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
                if (MapUtils.isNotEmpty(newHomeworkResult.getPractices())) {
                    for (Map.Entry<ObjectiveConfigType, NewHomeworkResultAnswer> entry : newHomeworkResult.getPractices().entrySet()) {
                        if (!entry.getKey().equals(objectiveConfigType)) {
                            continue;
                        }
                        NewHomeworkResultAppAnswer appAnswer = entry.getValue().getAppAnswers().get(stoneId);
                        if (appAnswer == null) {
                            continue;
                        }
                        if (!appAnswer.isFinished()) {
                            continue;
                        }
                        studentIds.add(newHomeworkResult.getUserId());
                        if (MapUtils.isNotEmpty(appAnswer.getAnswers())) {
                            wordExerciseProcessResultIds.addAll(appAnswer.getAnswers().values());
                        }
                        if (MapUtils.isNotEmpty(appAnswer.getImageTextRhymeAnswers())) {
                            imageTextRhymeProcessResultIds.addAll(appAnswer.getImageTextRhymeAnswers().values());
                        }
                    }
                }
            }

            MapMessage mapMessage = new MapMessage();
            // 字词训练模块
            if (WordTeachModuleType.WORDEXERCISE.equals(wordTeachModuleType)) {
                LinkedList<String> questionIds = new LinkedList<>();
                if (CollectionUtils.isNotEmpty(newHomeworkApp.getWordExerciseQuestions())) {
                    List<NewHomeworkQuestion> wordExerciseQuestions = newHomeworkApp.getWordExerciseQuestions();
                    questionIds.addAll(wordExerciseQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList()));
                }
                Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(hid, wordExerciseProcessResultIds);
                List<NewHomeworkProcessResult> newHomeworkProcessResults = new ArrayList<>(newHomeworkProcessResultMap.values());
                Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
                Map<String, NewQuestion> allNewQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
                List<QuestionDetail> values = processNewHomeworkAnswerDetailWordTeachTemplate.newInternalProcessHomeworkAnswerDetail(userMap, allNewQuestionMap, contentTypeMap, newHomework, objectiveConfigType, questionIds, newHomeworkProcessResults);
                mapMessage.add("questions", values);
            }

            // 完成学生相关信息
            Map<Long, StudentHomeworkData> studentHomeworkDataMap = new HashMap<>();
            for (Long studentId : studentIds) {
                if (userMap.get(studentId) != null) {
                    User user = userMap.get(studentId);
                    StudentHomeworkData studentHomeworkData = new StudentHomeworkData();
                    studentHomeworkData.setStudentId(user.getId());
                    studentHomeworkData.setStudentName(user.fetchRealname());
                    studentHomeworkData.setFinished(true);
                    studentHomeworkDataMap.put(user.getId(), studentHomeworkData);
                }
            }

            // 图文入韵模块
            if (WordTeachModuleType.IMAGETEXTRHYME.equals(wordTeachModuleType)) {
                List<ImageTextRhymeHomework> imageTextRhymeQuestions = newHomeworkApp.getImageTextRhymeQuestions();
                List<ImageTextRhymeModuleClazzData.ImageTextRhymeChapterData> imageTextRhymeChapterDatas = new ArrayList<>();
                // processResult信息
                Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(hid, imageTextRhymeProcessResultIds);
                // 篇章信息
                for (ImageTextRhymeHomework imageTextRhymeQuestion : imageTextRhymeQuestions) {
                    ImageTextRhymeModuleClazzData.ImageTextRhymeChapterData imageTextRhymeChapterData = new ImageTextRhymeModuleClazzData.ImageTextRhymeChapterData();
                    imageTextRhymeChapterData.setChapterId(imageTextRhymeQuestion.getChapterId());
                    imageTextRhymeChapterData.setTitle(imageTextRhymeQuestion.getTitle());
                    // 该篇章下题目ID
                    Set<String> questionIds = imageTextRhymeQuestion.getChapterQuestions().stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toSet());
                    // 学生得分信息
                    Map<Long, Double> studentScoreMap = new HashMap<>();
                    for (NewHomeworkProcessResult processResult : newHomeworkProcessResultMap.values()) {
                        if (!questionIds.contains(processResult.getQuestionId())) {
                            continue;
                        }
                        Long studentId = processResult.getUserId();
                        if (studentHomeworkDataMap.get(studentId) == null) {
                            continue;
                        }
                        double score = processResult.getActualScore();
                        if (studentScoreMap.get(studentId) == null) {
                            studentScoreMap.put(studentId, score);
                        } else {
                            double beforeScore = studentScoreMap.get(studentId);
                            studentScoreMap.put(studentId, processResult.getActualScore() + beforeScore);
                        }
                    }

                    List<StudentHomeworkData> studentHomeworkDatas = new ArrayList<>();
                    for (Map.Entry<Long, Double> entry : studentScoreMap.entrySet()) {
                        if (userMap.get(entry.getKey()) != null) {
                            User user = userMap.get(entry.getKey());
                            StudentHomeworkData studentHomeworkData = new StudentHomeworkData();
                            studentHomeworkData.setStudentId(user.getId());
                            studentHomeworkData.setStudentName(user.fetchRealname());
                            studentHomeworkData.setFinished(true);
                            int finaScore = new BigDecimal(entry.getValue()).divide(new BigDecimal(imageTextRhymeQuestion.getChapterQuestions().size()), 2, BigDecimal.ROUND_DOWN).intValue();
                            studentHomeworkData.setScore(imageTextRhymeStarCalculator.calculateImageTextRhymeStar(finaScore));  //这里跟前端之前约定为星级
                            studentHomeworkData.setStar(imageTextRhymeStarCalculator.calculateImageTextRhymeStar(finaScore));
                            studentHomeworkData.setFlashvarsUrl(UrlUtils.buildUrlQuery("/exam/flash/student/imagetextrhyme/detail" + Constants.AntiHijackExt,
                                    MiscUtils.m("homeworkId", hid, "studentId", entry.getKey(), "stoneDataId", stoneId, "chapterId", imageTextRhymeQuestion.getChapterId())));
                            studentHomeworkDatas.add(studentHomeworkData);
                        }
                    }
                    imageTextRhymeChapterData.setStudentHomeworkDatas(studentHomeworkDatas);
                    imageTextRhymeChapterDatas.add(imageTextRhymeChapterData);
                }
                mapMessage.add("result", imageTextRhymeChapterDatas);
            }

            // 汉字文化模块
            if (WordTeachModuleType.CHINESECHARACTERCULTURE.equals(wordTeachModuleType)) {
                List<String> chineseCharacterCultureCourseIds = newHomeworkApp.getChineseCharacterCultureCourseIds();
                // 课程信息
                Map<String, IntelDiagnosisCourse> courseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(chineseCharacterCultureCourseIds);
                List<ChineseCharacterCultureModuleClazzData.ChineseCharacterCultureCourseData> courseDatas = new ArrayList<>();
                for (String courseId : chineseCharacterCultureCourseIds) {
                    ChineseCharacterCultureModuleClazzData.ChineseCharacterCultureCourseData courseData = new ChineseCharacterCultureModuleClazzData.ChineseCharacterCultureCourseData();
                    courseData.setCourseId(courseId);
                    courseData.setTitle(courseMap.get(courseId) != null ? courseMap.get(courseId).getName() : "");
                    courseData.setStudentHomeworkDatas(new ArrayList<>(studentHomeworkDataMap.values()));
                    courseDatas.add(courseData);
                }
                mapMessage.add("result", courseDatas);
            }
            mapMessage.add("clazzGroupId", newHomework.getClazzGroupId());
            mapMessage.setSuccess(true);
            return mapMessage;
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage studentImageTextRhymeDetail(String homeworkId, Long studentId, String stoneDataId, String chapterId) {
        try {
            NewHomework newHomework = newHomeworkLoader.load(homeworkId);
            if (newHomework == null) {
                return MapMessage.errorMessage("作业不存在");
            }
            ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.WORD_TEACH_AND_PRACTICE;
            NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(objectiveConfigType);
            if (target == null) {
                return MapMessage.errorMessage("作业不包含" + objectiveConfigType.getValue());
            }
            // 作业原始信息
            NewHomeworkApp newHomeworkApp = new NewHomeworkApp();
            for (NewHomeworkApp app : target.getApps()) {
                if (app.getStoneDataId().equals(stoneDataId)) {
                    newHomeworkApp = app;
                }
            }

            // 所传篇章原始信息
            ImageTextRhymeHomework imageTextRhymeHomework = new ImageTextRhymeHomework();
            List<ImageTextRhymeHomework> imageTextRhymeHomeworks = newHomeworkApp.getImageTextRhymeQuestions();
            if (CollectionUtils.isNotEmpty(imageTextRhymeHomeworks)) {
                for (ImageTextRhymeHomework imageTextHomework : imageTextRhymeHomeworks) {
                    if (imageTextHomework.getChapterId().equals(chapterId)) {
                        imageTextRhymeHomework = imageTextHomework;
                    }
                }
            }
            Set<String> questionIds = imageTextRhymeHomework.getChapterQuestions()
                    .stream()
                    .map(NewHomeworkQuestion::getQuestionId)
                    .collect(Collectors.toSet());
            Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
            NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
            List<String> processIds = newHomeworkResult.findHomeworkProcessIdsForWordTeachByStoneDataId(stoneDataId, objectiveConfigType, WordTeachModuleType.IMAGETEXTRHYME);
            if (CollectionUtils.isEmpty(processIds)) {
                return MapMessage.errorMessage("学生尚未完成作业");
            }
            Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);

            int score = 0;
            Map<String, NewHomeworkProcessResult> questionIdProcessResultMap = new HashMap<>();
            for (NewHomeworkProcessResult newHomeworkProcessResult : processResultMap.values()) {
                if (questionIds.contains(newHomeworkProcessResult.getQuestionId()) && WordTeachModuleType.IMAGETEXTRHYME.equals(newHomeworkProcessResult.getWordTeachModuleType())) {
                    questionIds.remove(newHomeworkProcessResult.getQuestionDocId());
                    score += newHomeworkProcessResult.getActualScore();
                    questionIdProcessResultMap.put(newHomeworkProcessResult.getQuestionId(), newHomeworkProcessResult);
                }
            }

            List<StudentImageTextRhymeDetailMapper.StudentImageTextRhymeQuestionInfo> studentImageTextRhymeQuestionInfoList = new LinkedList<>();
            for (String questionId : questionIds) {
                StudentImageTextRhymeDetailMapper.StudentImageTextRhymeQuestionInfo studentQuestionInfo = new StudentImageTextRhymeDetailMapper.StudentImageTextRhymeQuestionInfo();
                studentQuestionInfo.setQuestionId(questionId);
                studentQuestionInfo.setCoverPic(questionMap.get(questionId).getCoverPic());
                // 答题语音详情
                NewHomeworkProcessResult newHomeworkProcessResult = questionIdProcessResultMap.get(questionId);
                LinkedList<StudentImageTextRhymeDetailMapper.QuestionContentAudioUrl> contentAudioUrls = new LinkedList<>();
                for (BaseHomeworkProcessResult.OralDetail oralDetail : newHomeworkProcessResult.getOralDetails().get(0)) {
                    StudentImageTextRhymeDetailMapper.QuestionContentAudioUrl questionContentAudioUrl = new StudentImageTextRhymeDetailMapper.QuestionContentAudioUrl();
                    questionContentAudioUrl.setAudioUrl(oralDetail.getAudio());
                    questionContentAudioUrl.setContent(oralDetail.getSentences().get(0).getSample());
                    contentAudioUrls.add(questionContentAudioUrl);
                }
                studentQuestionInfo.setContentAudioUrls(contentAudioUrls);
                studentImageTextRhymeQuestionInfoList.add(studentQuestionInfo);
            }
            int finalScore = new BigDecimal(score).divide(new BigDecimal(questionIdProcessResultMap.size()), 2, BigDecimal.ROUND_DOWN).intValue();
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            StudentImageTextRhymeDetailMapper studentImageTextRhymeDetail = new StudentImageTextRhymeDetailMapper();
            studentImageTextRhymeDetail.setStudentId(studentId);
            studentImageTextRhymeDetail.setStudentName(studentDetail.fetchRealname());
            studentImageTextRhymeDetail.setChapterId(imageTextRhymeHomework.getChapterId());
            studentImageTextRhymeDetail.setTitle(imageTextRhymeHomework.getTitle());
            studentImageTextRhymeDetail.setImageUrl(imageTextRhymeHomework.getImageUrl() != null ? imageTextRhymeHomework.getImageUrl() : NewHomeworkConstants.WORD_TEACH_IMAGE_TEXT_RHYME_DEFAULT_IMG);
            studentImageTextRhymeDetail.setStar(imageTextRhymeStarCalculator.calculateImageTextRhymeStar(finalScore));
            studentImageTextRhymeDetail.setStudentImageTextRhymeQuestionInfoList(studentImageTextRhymeQuestionInfoList);
            return MapMessage.successMessage().add("result", studentImageTextRhymeDetail);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage fetchOcrHomeworkStudentDetail(String hid, Teacher teacher) {
        try {
            NewHomework newHomework = newHomeworkLoader.load(hid);
            //******* begin 校验：（1）作业是否存在；（2）老师是否有权限********//
            if (newHomework == null) {
                logger.error("fetch Ocr NewHomework StudentDetail failed : hid {},tid {}", hid, teacher.getId());
                return MapMessage.errorMessage("作业不存在");
            }
            if (!NewHomeworkType.OCR.equals(newHomework.getType())) {
                return MapMessage.errorMessage("非纸质作业");
            }
            if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), newHomework.getClazzGroupId())) {
                return MapMessage.errorMessage("没有权限查看此作业报告").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_PERMISSION);
            }
            //******* end 校验：（1）作业是否存在；（2）老师是否有权限********//

            //********* begin 数据初始化 ************//
            Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                    .stream()
                    .collect(Collectors
                            .toMap(LongIdEntity::getId, Function.identity()));
            Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), false);

            OcrNewHomeworkStudentDetail ocrNewHomeworkStudentDetail = new OcrNewHomeworkStudentDetail();
            for (User u : userMap.values()) {
                NewHomeworkResult r = newHomeworkResultMap.get(u.getId());
                if (r != null && r.isFinished()) {
                    int score = SafeConverter.toInt(r.ocrHomeworkProcessScore());
                    ocrNewHomeworkStudentDetail.setTotalScore(ocrNewHomeworkStudentDetail.getTotalScore() + score);
                    long duration = new BigDecimal(SafeConverter.toLong(r.processDuration())).divide(new BigDecimal(60), 0, BigDecimal.ROUND_UP).longValue();
                    ocrNewHomeworkStudentDetail.setFinishedNum(1 + ocrNewHomeworkStudentDetail.getFinishedNum());
                    ocrNewHomeworkStudentDetail.setTotalDuration(duration + ocrNewHomeworkStudentDetail.getTotalDuration());
                }
            }

            //******* begin 是否催促没有完成作业的 ****//
            UrgeNewHomeworkUnFinishCacheManager urgeNewHomeworkUnFinishCacheManager = this.newHomeworkCacheService.getUrgeNewHomeworkUnFinishCacheManager();
            String key = urgeNewHomeworkUnFinishCacheManager.getCacheKey(newHomework.getId());
            Integer value = urgeNewHomeworkUnFinishCacheManager.load(key);
            ocrNewHomeworkStudentDetail.setShowUrgeFinishHomework(Objects.isNull(value));
            //******* end 是否催促没有完成作业的 ****//

            // OTO纸质作业作业形式
            ObjectiveConfigType objectiveConfigType = null;
            if (Subject.MATH.equals(newHomework.getSubject())) {
                objectiveConfigType = ObjectiveConfigType.OCR_MENTAL_ARITHMETIC;
            } else if (Subject.ENGLISH.equals(newHomework.getSubject())) {
                objectiveConfigType = ObjectiveConfigType.OCR_DICTATION;
            }

            NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(objectiveConfigType);
            //取学生作业中间结果数据（根据完成作业形式过滤）
            Map<Long, NewHomeworkResult> ocrNewHomeworkResultMap = new HashMap<>();
            for (Map.Entry<Long, NewHomeworkResult> entry : newHomeworkResultMap.entrySet()) {
                NewHomeworkResult newHomeworkResult = entry.getValue();
                if (newHomeworkResult.isFinishedOfObjectiveConfigType(objectiveConfigType)) {
                    ocrNewHomeworkResultMap.put(entry.getKey(), entry.getValue());
                }
            }
            //参考模板处理：数据环境准备
            ObjectiveConfigTypePartContext context = new ObjectiveConfigTypePartContext(teacher,
                    objectiveConfigType,
                    ocrNewHomeworkResultMap,
                    newHomework,
                    null,
                    userMap,
                    target);
            ocrNewHomeworkStudentDetail.setOcrHomeworkStudentDetails(fetchOcrHomeworkStudentDetail(context));
            if (MapUtils.isNotEmpty(ocrNewHomeworkResultMap)) {
                //数学：易错知识点&常见错因
                if (Subject.MATH.equals(newHomework.getSubject())) {
                    ocrNewHomeworkStudentDetail.setWrongQuestionParts(processOcrHomeworkMathWrongQuestionInfo(hid, ocrNewHomeworkResultMap));
                }
                //英语：易拼错单词
                if (Subject.ENGLISH.equals(newHomework.getSubject())) {
                    ocrNewHomeworkStudentDetail.setWrongQuestionParts(processOcrHomeworkEnglishWrongQuestionInfo(newHomework, ocrNewHomeworkResultMap));
                }
            }
            ocrNewHomeworkStudentDetail.handlerResult(userMap, newHomework);
            // ************* begin 一些地址传送 ********//
            String correctUrl = UrlUtils.buildUrlQuery("/view/report/correct",
                    MapUtils.m(
                            "homeworkId", newHomework.getId(),
                            "subject", newHomework.getSubject(),
                            "homeworkType", newHomework.getNewHomeworkType()
                    ));
            String detailUrl = UrlUtils.buildUrlQuery("/view/reportv5/studentresult",
                    MapUtils.m(
                            "subject", newHomework.getSubject(),
                            "homeworkId", newHomework.getId()));
            String shareReportUrl = UrlUtils.buildUrlQuery("/view/ocrhomeworkreport/share",
                    MapUtils.m(
                            "subject", newHomework.getSubject(),
                            "homeworkIds", newHomework.getId()));
            String checkHomeworkUrl = UrlUtils.buildUrlQuery("/view/reportv5/rearrange",
                    MapUtils.m(
                            "subject", newHomework.getSubject(),
                            "homeworkId", newHomework.getId()));

            ocrNewHomeworkStudentDetail.setCheckHomeworkUrl(checkHomeworkUrl);
            ocrNewHomeworkStudentDetail.setShareReportUrl(shareReportUrl);
            ocrNewHomeworkStudentDetail.setDetailUrl(detailUrl);
            ocrNewHomeworkStudentDetail.setCorrectUrl(correctUrl);
            // ************* end 一些地址传送 ********//

            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("ocrNewHomeworkStudentDetail", ocrNewHomeworkStudentDetail);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch Ocr NewHomework StudentDetail failed : hid {},tid {}", hid, teacher.getId(), e);
            return MapMessage.errorMessage();
        }
    }

    private List<OcrNewHomeworkStudentDetail.OcrStudentDetail> fetchOcrHomeworkStudentDetail(ObjectiveConfigTypePartContext context) {
        Map<Long, User> userMap = context.getUserMap();
        Map<Long, NewHomeworkResult> resultMap = context.getNewHomeworkResultMap();
        List<OcrNewHomeworkStudentDetail.OcrStudentDetail> ocrStudentDetailList = Lists.newArrayList();
        ObjectiveConfigType objectiveConfigType = context.getType();
        for (User user : userMap.values()) {
            OcrNewHomeworkStudentDetail.OcrStudentDetail student = new OcrNewHomeworkStudentDetail.OcrStudentDetail();
            student.setUserId(user.getId());
            student.setUserName(user.fetchRealnameIfBlankId());
            if (resultMap.containsKey(user.getId())) {
                NewHomeworkResult newHomeworkResult = resultMap.get(user.getId());
                NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(objectiveConfigType);
                student.setScore(newHomeworkResultAnswer.processScore(context.getType()));
                if (ObjectiveConfigType.OCR_DICTATION.equals(objectiveConfigType)) { //英语单词听写
                    student.setIdentifyCount(newHomeworkResultAnswer.getOcrDictationQuestionCount());
                    student.setErrorCount(newHomeworkResultAnswer.getOcrDictationQuestionCount() - newHomeworkResultAnswer.getOcrDictationCorrectQuestionCount());
                } else {    //数学纸质口算
                    student.setIdentifyCount(newHomeworkResultAnswer.getOcrMentalQuestionCount());
                    student.setErrorCount(newHomeworkResultAnswer.getOcrMentalQuestionCount() - newHomeworkResultAnswer.getOcrMentalCorrectQuestionCount());
                }
                student.setManualCorrect(newHomeworkResultAnswer.isCorrected());
                student.setFinished(newHomeworkResultAnswer.isFinished());
                student.setRepair(newHomeworkResult.getRepair());
                student.setPersonReportUrl(UrlUtils.buildUrlQuery("/view/ocrhomeworkreport/student/questionsdetail",
                        MapUtils.m("homeworkId", context.getNewHomework().getId(),
                                "studentId", user.getId(),
                                "objectiveConfigType", context.getType())));
            }
            ocrStudentDetailList.add(student);
        }
        ocrStudentDetailList.sort(OcrNewHomeworkStudentDetail.OcrStudentDetail.comparator);
        return ocrStudentDetailList;
    }

    /**
     * 纸质作业
     * 数学：易错知识点&常见错因
     *
     * @param homeworkId
     * @param newHomeworkResultMap
     * @return
     */
    private List<OcrNewHomeworkStudentDetail.WrongQuestionPart> processOcrHomeworkMathWrongQuestionInfo(String homeworkId, Map<Long, NewHomeworkResult> newHomeworkResultMap) {
        // processResultIds
        List<String> processResultIds = new ArrayList<>();
        for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
            LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = newHomeworkResult.getPractices();
            for (NewHomeworkResultAnswer newHomeworkResultAnswer : practices.values()) {
                processResultIds.addAll(newHomeworkResultAnswer.getOcrMentalAnswers());
            }
        }
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(homeworkId, processResultIds);

        //Map<知识点ID，Set<学生ID>>
        Map<String, Set<Long>> pointStudentMap = new HashMap<>();
        //Map<知识点ID，知识点名称>
        Map<String, String> pointNameMap = new HashMap<>();
        //Map<错因, Set<学生ID>>
        Map<String, Set<Long>> kpSymptomsStudentMap = new HashMap<>();
        for (NewHomeworkProcessResult processResult : newHomeworkProcessResultMap.values()) {
            OcrMentalImageDetail ocrMentalImageDetail = processResult.getOcrMentalImageDetail();
            if (ocrMentalImageDetail == null) {
                continue;
            }
            // 知识点
            OcrMentalImageDetail.OcrMentalArithmeticDiagnosis omads = ocrMentalImageDetail.getOmads();
            if (omads != null && CollectionUtils.isNotEmpty(omads.getItemPoints())) {
                List<OcrMentalImageDetail.ItemPoint> itemPoints = omads.getItemPoints();
                if (CollectionUtils.isEmpty(itemPoints)) {
                    continue;
                }
                for (OcrMentalImageDetail.ItemPoint itemPoint : itemPoints) {
                    List<OcrMentalImageDetail.Point> points = itemPoint.getPoints();
                    if (CollectionUtils.isEmpty(points)) {
                        continue;
                    }
                    for (OcrMentalImageDetail.Point point : points) {
                        pointNameMap.put(point.getPointId(), point.getPointName());
                        if (CollectionUtils.isNotEmpty(pointStudentMap.get(point.getPointId()))) {
                            Set<Long> studentIds = pointStudentMap.get(point.getPointId());
                            studentIds.add(processResult.getUserId());
                            pointStudentMap.put(point.getPointId(), studentIds);
                        } else {
                            Set<Long> studentIds = new HashSet<>();
                            studentIds.add(processResult.getUserId());
                            pointStudentMap.put(point.getPointId(), studentIds);
                        }
                    }
                }
            }
            //错因 -> Set<学生ID>
            if (MapUtils.isNotEmpty(ocrMentalImageDetail.getKpSymptoms())) {
                for (Map.Entry<String, List<String>> entry : ocrMentalImageDetail.getKpSymptoms().entrySet()) {
                    String kpSymptoms = String.join("", entry.getValue());
                    if (CollectionUtils.isNotEmpty(kpSymptomsStudentMap.get(kpSymptoms))) {
                        Set<Long> studentIds = kpSymptomsStudentMap.get(kpSymptoms);
                        studentIds.add(processResult.getUserId());
                        kpSymptomsStudentMap.put(kpSymptoms, studentIds);
                    } else {
                        Set<Long> studentIds = new HashSet<>();
                        studentIds.add(processResult.getUserId());
                        kpSymptomsStudentMap.put(kpSymptoms, studentIds);
                    }
                }
            }
        }

        List<OcrNewHomeworkStudentDetail.WrongQuestionPart> wrongQuestionParts = new LinkedList<>();
        //易错知识点
        OcrNewHomeworkStudentDetail.WrongQuestionPart wrongPointInfo = new OcrNewHomeworkStudentDetail.WrongQuestionPart();
        List<OcrNewHomeworkStudentDetail.WrongQuestionInfo> wrongPointInfos = new LinkedList<>();
        for (Map.Entry<String, Set<Long>> entry : pointStudentMap.entrySet()) {
            String pointId = entry.getKey();
            OcrNewHomeworkStudentDetail.WrongQuestionInfo wrongQuestionInfo = new OcrNewHomeworkStudentDetail.WrongQuestionInfo();
            wrongQuestionInfo.setPointId(pointId);
            wrongQuestionInfo.setWrongInfo(pointNameMap.get(pointId));
            wrongQuestionInfo.setStudentNum(entry.getValue().size());
            wrongPointInfos.add(wrongQuestionInfo);
        }
        if (CollectionUtils.isNotEmpty(wrongPointInfos)) {
            wrongPointInfo.setTypeName("易错知识点");
            wrongPointInfo.setWrongQuestionInfos(wrongPointInfos);
            wrongQuestionParts.add(wrongPointInfo);
        }

        //常见错因
        OcrNewHomeworkStudentDetail.WrongQuestionPart commonSymptomsInfo = new OcrNewHomeworkStudentDetail.WrongQuestionPart();
        List<OcrNewHomeworkStudentDetail.WrongQuestionInfo> commonSymptomsInfos = new LinkedList<>();
        for (Map.Entry<String, Set<Long>> entry : kpSymptomsStudentMap.entrySet()) {
            OcrNewHomeworkStudentDetail.WrongQuestionInfo wrongQuestionInfo = new OcrNewHomeworkStudentDetail.WrongQuestionInfo();
            wrongQuestionInfo.setWrongInfo(entry.getKey());
            wrongQuestionInfo.setStudentNum(entry.getValue().size());
            commonSymptomsInfos.add(wrongQuestionInfo);
        }
        if (CollectionUtils.isNotEmpty(commonSymptomsInfos)) {
            commonSymptomsInfo.setTypeName("常见错因");
            commonSymptomsInfo.setWrongQuestionInfos(commonSymptomsInfos);
            wrongQuestionParts.add(commonSymptomsInfo);
        }
        return wrongQuestionParts;
    }

    /**
     * 纸质作业
     * 英语：易拼错单词
     *
     * @param newHomework
     * @param newHomeworkResultMap
     * @return
     */
    private List<OcrNewHomeworkStudentDetail.WrongQuestionPart> processOcrHomeworkEnglishWrongQuestionInfo(NewHomework newHomework, Map<Long, NewHomeworkResult> newHomeworkResultMap) {
        List<OcrNewHomeworkStudentDetail.WrongQuestionPart> wrongQuestionParts = new LinkedList<>();
        //原题目信息(英文单词)
        List<NewHomeworkQuestion> newHomeworkQuestions = newHomework.findNewHomeworkQuestions(ObjectiveConfigType.OCR_DICTATION);
        List<String> questionIds = newHomeworkQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList());
        Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        List<String> lessonIds = newHomeworkQuestions
                .stream()
                .map(NewHomeworkQuestion::getQuestionBoxId)
                .distinct()
                .collect(Collectors.toList());
        if (MapUtils.isEmpty(allQuestionMap) || CollectionUtils.isEmpty(lessonIds)) {
            return wrongQuestionParts;
        }
        List<Long> choosedSentencesList = allQuestionMap.values()
                .stream()
                .map(NewQuestion::getSentenceIds)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Map<String, List<Sentence>> lessonSentenceMap = newEnglishContentLoaderClient.loadEnglishLessonSentences(lessonIds);
        if (CollectionUtils.isEmpty(choosedSentencesList) || MapUtils.isEmpty(lessonSentenceMap)) {
            return wrongQuestionParts;
        }
        List<Sentence> sentenceList = lessonSentenceMap.values()
                .stream()
                .flatMap(List::stream)
                .filter(s -> choosedSentencesList.contains(s.getId()))
                .collect(Collectors.toList());
        Map<Long, Sentence> sentenceMap = sentenceList
                .stream()
                .collect(Collectors.toMap(Sentence::getId, Function.identity()));

        // processResultIds
        List<String> processResultIds = new ArrayList<>();
        for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
            LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = newHomeworkResult.getPractices();
            for (NewHomeworkResultAnswer newHomeworkResultAnswer : practices.values()) {
                processResultIds.addAll(newHomeworkResultAnswer.getOcrDictationAnswers());
            }
        }
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processResultIds);

        //Map<英语单词，Set<学生ID>>  答题正确
        Map<String, Set<Long>> questionStudentMap = new HashMap<>();
        for (NewHomeworkProcessResult processResult : newHomeworkProcessResultMap.values()) {
            OcrMentalImageDetail ocrMentalImageDetail = processResult.getOcrDictationImageDetail();
            if (ocrMentalImageDetail == null) {
                continue;
            }
            List<OcrMentalImageDetail.Form> forms = ocrMentalImageDetail.getForms();
            if (CollectionUtils.isEmpty(forms)) {
                continue;
            }
            forms.stream()
                    .filter(Objects::nonNull)
                    .filter(f -> Objects.equals(1, f.getJudge()))
                    .filter(NewHomeworkUtils.distinctByKey(OcrMentalImageDetail.Form::getText))
                    .forEach(f -> {
                        if (CollectionUtils.isNotEmpty(questionStudentMap.get(f.getText()))) {
                            Set<Long> studentIds = questionStudentMap.get(f.getText());
                            studentIds.add(processResult.getUserId());
                            questionStudentMap.put(f.getText(), studentIds);
                        } else {
                            Set<Long> studentIds = new HashSet<>();
                            studentIds.add(processResult.getUserId());
                            questionStudentMap.put(f.getText(), studentIds);
                        }
                    });
        }

        List<OcrNewHomeworkStudentDetail.WrongQuestionInfo> mispronouncedWords = new LinkedList<>();
        for (Long sentenceId : choosedSentencesList) {
            Sentence sentence = sentenceMap.get(sentenceId);
            if (sentence != null) {
                OcrNewHomeworkStudentDetail.WrongQuestionInfo wrongQuestionInfo = new OcrNewHomeworkStudentDetail.WrongQuestionInfo();
                if (questionStudentMap.keySet().contains(sentence.getEnText())) {
                    if (newHomeworkResultMap.size() == questionStudentMap.get(sentence.getEnText()).size()) {
                        continue;
                    }
                    wrongQuestionInfo.setWrongInfo(sentence.getEnText());
                    wrongQuestionInfo.setStudentNum(newHomeworkResultMap.size() - questionStudentMap.get(sentence.getEnText()).size());
                } else {
                    wrongQuestionInfo.setWrongInfo(sentence.getEnText());
                    wrongQuestionInfo.setStudentNum(newHomeworkResultMap.size());
                }
                mispronouncedWords.add(wrongQuestionInfo);
            }
        }
        OcrNewHomeworkStudentDetail.WrongQuestionPart wrongQuestionPart = new OcrNewHomeworkStudentDetail.WrongQuestionPart();
        if (CollectionUtils.isNotEmpty(mispronouncedWords)) {
            wrongQuestionPart.setTypeName("易拼错单词");
            wrongQuestionPart.setWrongQuestionInfos(mispronouncedWords);
            wrongQuestionParts.add(wrongQuestionPart);
        }
        return wrongQuestionParts;
    }

    @Override
    public OcrHomeworkShareReport processOcrHomeworkShareReport(String newHomeworkId, User user, String cdnUrl) {
        NewHomework newHomework = newHomeworkLoader.load(newHomeworkId);
        OcrHomeworkShareReport ocrHomeworkShareReport = new OcrHomeworkShareReport();
        if (newHomework == null) {
            return ocrHomeworkShareReport;
        }

        List<User> userList = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId());
        if (CollectionUtils.isEmpty(userList)) {
            return ocrHomeworkShareReport;
        }
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(LongIdEntity::getId, Function.identity()));
        //老师分享需要查询明细
        Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), false);
        if (MapUtils.isEmpty(newHomeworkResultMap)) {
            return ocrHomeworkShareReport;
        }

        OcrHomeworkShareReport.FinishedStudent finishedStudent = new OcrHomeworkShareReport.FinishedStudent();
        Set<String> studentNames = new HashSet<>();
        int finishedNum = 0;
        for (User student : userList) {
            NewHomeworkResult n = newHomeworkResultMap.get(student.getId());
            //完成的学生
            if (n != null && n.isFinished()) {
                finishedNum++;
                String studentName = student.fetchRealnameIfBlankId();
                studentNames.add(studentName);
            }
        }
        if (finishedNum != 0) {
            finishedStudent.setTypeName("优秀学生");
            finishedStudent.setFinishedStudentNum(finishedNum);
            finishedStudent.setStudentNames(studentNames);
            ocrHomeworkShareReport.setFinishedStudent(finishedStudent);
        }

        //是否分享
        NoticeShareReportToJztCacheManager noticeShareReportToJztCacheManager = newHomeworkCacheService.getNoticeShareReportToJztCacheManager();
        String cacheKey = noticeShareReportToJztCacheManager.getCacheKey(newHomeworkId);
        boolean share = noticeShareReportToJztCacheManager.load(cacheKey) != null;
        ocrHomeworkShareReport.setShare(share);
        ocrHomeworkShareReport.setHomeworkDate(newHomework.getCreateAt() != null ? DateUtils.dateToString(newHomework.getCreateAt(), "MM月dd日") : "");

        //老师信息
        if (newHomework.getTeacherId() != null) {
            Teacher teacher = teacherLoaderClient.loadTeacher(newHomework.getTeacherId());
            ocrHomeworkShareReport.setTeacherUrl(NewHomeworkUtils.getUserAvatarImgUrl(cdnUrl, teacher.fetchImageUrl()));
            ocrHomeworkShareReport.setTeacherId(teacher.getId());
            ocrHomeworkShareReport.setTeacherName(teacher.fetchRealname());
            ocrHomeworkShareReport.setTeacherShareMsg("我刚检查了纸质作业，完成情况如下。家长有需要的可查看自己孩子的完成情况。");
        }
        ocrHomeworkShareReport.setHomeworkId(newHomework.getId());
        GroupMapper group = groupLoaderClient.loadGroup(newHomework.getClazzGroupId(), true);
        Long clazzId = group == null ? null : group.getClazzId();
        Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId);
        ocrHomeworkShareReport.setClazzGroupId(newHomework.getClazzGroupId());
        ocrHomeworkShareReport.setClazzGroupName(clazz != null ? clazz.formalizeClazzName() : "");
        //家长一起学使用人数
        Map<Long, List<StudentParentRef>> studentParentMap = studentLoaderClient.loadStudentParentRefs(userMap.keySet());
        int useYQXNum = 0;
        for (List<StudentParentRef> studentParentRefs : studentParentMap.values()) {
            if (CollectionUtils.isEmpty(studentParentRefs)) {
                continue;
            }
            useYQXNum++;
        }
        ocrHomeworkShareReport.setUseYQXNum(useYQXNum);
        // OTO纸质作业作业形式
        ObjectiveConfigType objectiveConfigType = null;
        if (Subject.MATH.equals(newHomework.getSubject())) {
            objectiveConfigType = ObjectiveConfigType.OCR_MENTAL_ARITHMETIC;
        } else if (Subject.ENGLISH.equals(newHomework.getSubject())) {
            objectiveConfigType = ObjectiveConfigType.OCR_DICTATION;
        }
        //取学生作业中间结果数据（根据完成作业形式过滤）
        Map<Long, NewHomeworkResult> ocrNewHomeworkResultMap = new HashMap<>();
        for (Map.Entry<Long, NewHomeworkResult> entry : newHomeworkResultMap.entrySet()) {
            NewHomeworkResult newHomeworkResult = entry.getValue();
            if (newHomeworkResult.isFinishedOfObjectiveConfigType(objectiveConfigType)) {
                ocrNewHomeworkResultMap.put(entry.getKey(), entry.getValue());
            }
        }
        if (MapUtils.isNotEmpty(ocrNewHomeworkResultMap)) {
            //数学：易错知识点&常见错因
            if (Subject.MATH.equals(newHomework.getSubject())) {
                ocrHomeworkShareReport.setWrongQuestionParts(processOcrHomeworkMathWrongQuestionInfo(newHomeworkId, ocrNewHomeworkResultMap));
            }
            //英语：易拼错单词
            if (Subject.ENGLISH.equals(newHomework.getSubject())) {
                ocrHomeworkShareReport.setWrongQuestionParts(processOcrHomeworkEnglishWrongQuestionInfo(newHomework, ocrNewHomeworkResultMap));
            }
        }

        return ocrHomeworkShareReport;
    }

    @Override
    public MapMessage loadOcrHomeworkDetail(List<String> homeworkIds) {
        Map<String, NewHomework> newHomeworkMap = newHomeworkLoader.loads(homeworkIds);
        if (MapUtils.isEmpty(newHomeworkMap)) {
            return MapMessage.errorMessage("作业id错误");
        }
        for (NewHomework newHomework : newHomeworkMap.values()) {
            if (NewHomeworkType.OCR != newHomework.getNewHomeworkType()) {
                return MapMessage.errorMessage("非纸质作业");
            }
        }
        List<Long> groupIds = newHomeworkMap.values().stream().map(NewHomework::getClazzGroupId).collect(Collectors.toList());
        Map<Long, GroupMapper> groupMapperMap = groupLoaderClient.loadGroups(groupIds, true);
        Set<Long> studentIds = groupMapperMap.values().stream()
                .map(GroupMapper::getStudents)
                .flatMap(Collection::stream)
                .map(GroupMapper.GroupUser::getId)
                .collect(Collectors.toSet());
        Map<Long, Set<Long>> studentBindAppParentMap = vendorServiceClient.studentBindAppParentMap(new ArrayList<>(studentIds));
        Set<Long> clazzIds = groupMapperMap.values().stream()
                .map(GroupMapper::getClazzId)
                .collect(Collectors.toSet());
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        List<Map<String, Object>> clazzList = new ArrayList<>();
        for (GroupMapper groupMapper : groupMapperMap.values()) {
            Clazz clazz = clazzMap.get(groupMapper.getClazzId());
            if (clazz == null) {
                continue;
            }
            List<GroupMapper.GroupUser> students = groupMapper.getStudents();
            long parentCount = students.stream()
                    .map(GroupMapper.GroupUser::getId)
                    .filter(studentId -> CollectionUtils.isNotEmpty(studentBindAppParentMap.get(studentId)))
                    .count();
            clazzList.add(MapUtils.m("clazzName", clazz.formalizeClazzName(), "parentCount", parentCount));
        }
        NewHomework newHomework = newHomeworkMap.values().iterator().next();
        String homeworkDate = DateUtils.dateToString(newHomework.getCreateAt(), "MM.dd");
        String shareHomeworkDate = DateUtils.dateToString(newHomework.getCreateAt(), "MM月dd日");
        String endDate = DateUtils.dateToString(newHomework.getEndTime(), "yyyy-MM-dd HH:mm");
        Map<ObjectiveConfigType, NewHomeworkPracticeContent> newHomeworkPracticeContents = newHomework.findPracticeContents();
        String submitWay = "";
        Map<String, Object> practices = Collections.emptyMap();
        if (newHomeworkPracticeContents.containsKey(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC)) {
            NewHomeworkPracticeContent newHomeworkPracticeContent = newHomeworkPracticeContents.get(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC);
            String workBookName = newHomeworkPracticeContent.getWorkBookName();
            String homeworkDetail = newHomeworkPracticeContent.getHomeworkDetail();
            String[] bookNames = StringUtils.split(workBookName, NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_SEPARATOR);
            String[] homeworkDetails = StringUtils.split(homeworkDetail, NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_SEPARATOR);
            List<String> bookNameList = Arrays.asList(bookNames);
            List<String> homeworkDetailList = Arrays.asList(homeworkDetails);
            int length = Integer.max(bookNameList.size(), homeworkDetailList.size());
            List<Map<String, Object>> contents = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                contents.add(MapUtils.m("bookName", bookNameList.get(i), "homeworkDetail", homeworkDetailList.get(i)));
            }
            submitWay = "完成教辅 打开“一起小学”app拍照提交";
            practices = MapUtils.m("type", ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.name(), "typeName", ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.getValue(), "contents", contents);
        } else if (newHomeworkPracticeContents.containsKey(ObjectiveConfigType.OCR_DICTATION)) {
            NewHomeworkPracticeContent newHomeworkPracticeContent = newHomeworkPracticeContents.get(ObjectiveConfigType.OCR_DICTATION);
            List<NewHomeworkQuestion> newHomeworkQuestions = newHomeworkPracticeContent.getQuestions();
//            Set<String> lessonIds = newHomeworkQuestions.stream()
//                    .map(NewHomeworkQuestion::getQuestionBoxId)
//                    .collect(Collectors.toCollection(LinkedHashSet::new));
//            Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
//            List<String> lessonNames = lessonMap.values().stream().map(NewBookCatalog::getAlias).collect(Collectors.toList());
            List<String> lessonNames = Collections.singletonList("请打开“一起小学”app播放单词");
            Set<String> questionIds = newHomeworkQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toCollection(LinkedHashSet::new));
            Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
            Set<Long> sentenceIds = new LinkedHashSet<>();
            for (NewQuestion question : questionMap.values()) {
                if (CollectionUtils.isNotEmpty(question.getSentenceIds())) {
                    sentenceIds.add(question.getSentenceIds().get(0));
                }
            }
            Map<Long, Sentence> sentenceMap = englishContentLoaderClient.loadEnglishSentences(sentenceIds);
            List<String> sentenceNames = sentenceMap.values().stream().map(Sentence::getEnText).collect(Collectors.toList());
            submitWay = "打开“一起小学”app播放音频 线上听写，拍照提交";
            practices = MapUtils.m("type", ObjectiveConfigType.OCR_DICTATION.name(), "typeName", "单词听写", "lessons", lessonNames, "sentences", sentenceNames);
        }
        return MapMessage.successMessage()
                .add("shareHomeworkDate", shareHomeworkDate)
                .add("homeworkDate", homeworkDate)
                .add("endDate", endDate)
                .add("submitWay", submitWay)
                .add("clazzList", clazzList)
                .add("practices", practices)
                .add("subject", newHomework.getSubject())
                .add("subjectName", newHomework.getSubject().getValue())
                .add("hasShareParent", false);
    }
}
