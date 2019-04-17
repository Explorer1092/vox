package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.support.LocationTransformer;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.core.helper.VoiceEngineTypeUtils;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.content.client.PracticeServiceClient;
import com.voxlearning.utopia.service.content.consumer.EnglishContentLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.DPLiveCastHomeworkReportLoader;
import com.voxlearning.utopia.service.newhomework.api.LiveCastHomeworkLocationLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSyntheticHistory;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.*;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkProcessResultDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.service.LiveCastGenerateDataServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkQuestionFileHelper;
import com.voxlearning.utopia.service.newhomework.impl.template.ProcessLiveCastHomeworkAnswerDetailFactory;
import com.voxlearning.utopia.service.newhomework.impl.template.ProcessLiveCastHomeworkAnswerDetailTemplate;
import com.voxlearning.utopia.service.newhomework.impl.template.StatisticsToObjectiveConfigTypeFactory;
import com.voxlearning.utopia.service.newhomework.impl.template.StatisticsToObjectiveConfigTypeTemple;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.api.mapper.QuestionMapper;
import com.voxlearning.utopia.service.question.consumer.PictureBookLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionContentTypeLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.ThirdPartyGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = DPLiveCastHomeworkReportLoader.class)
@ExposeService(interfaceClass = DPLiveCastHomeworkReportLoader.class)
public class DPLiveCastHomeworkReportLoaderImpl extends SpringContainerSupport implements DPLiveCastHomeworkReportLoader {

    @Inject
    private PictureBookPlusServiceClient pictureBookPlusServiceClient;
    @Inject
    private ThirdPartyGroupLoaderClient thirdPartyGroupLoaderClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private StatisticsToObjectiveConfigTypeFactory statisticsToObjectiveConfigTypeFactory;
    @Inject
    private QuestionLoaderClient questionLoaderClient;
    @Inject
    private QuestionContentTypeLoaderClient questionContentTypeLoaderClient;
    @Inject
    private ProcessLiveCastHomeworkAnswerDetailFactory processLiveCastHomeworkAnswerDetailFactory;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private EnglishContentLoaderClient englishContentLoaderClient;
    @Inject
    private PictureBookLoaderClient pictureBookLoaderClient;
    @Inject
    private LiveCastHomeworkResultDao liveCastHomeworkResultDao;
    @Inject
    private LiveCastHomeworkProcessResultDao liveCastHomeworkProcessResultDao;
    @Inject
    private LiveCastHomeworkDao liveCastHomeworkDao;
    @Inject
    private PracticeServiceClient practiceServiceClient;
    @Inject
    private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject
    private LiveCastGenerateDataServiceImpl liveCastGenerateDataService;

    @Override
    public MapMessage fetchSubjectiveQuestion(String hid, ObjectiveConfigType objectiveConfigType, String qid) {
        try {
            if (StringUtils.isBlank(hid) || objectiveConfigType == null || StringUtils.isBlank(qid)) {
                logger.warn("fetch subjective question failed:hid {},objectiveConfigType {},qid {}", hid, objectiveConfigType, qid);
                return MapMessage.errorMessage("参数错误");
            }
            LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(hid);
            if (liveCastHomework == null) {
                logger.warn("fetch subjective question failed:hid {},objectiveConfigType {},qid {}", hid, objectiveConfigType, qid);
                return MapMessage.errorMessage("作业不存在");
            }
            NewHomeworkPracticeContent target = liveCastHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(objectiveConfigType);
            if (target == null) {
                logger.warn("fetch subjective question failed:hid {},objectiveConfigType {},qid {}", hid, objectiveConfigType, qid);
                return MapMessage.errorMessage("作业不包含该类型");
            }
            List<NewHomeworkQuestion> questions = target.processNewHomeworkQuestion(true);
            if (CollectionUtils.isEmpty(questions)) {
                logger.warn("fetch subjective question failed:hid {},objectiveConfigType {},qid {}", hid, objectiveConfigType, qid);
                return MapMessage.errorMessage("作业不包含该题");
            }
            boolean match = questions.stream().anyMatch(o -> Objects.equals(o.getQuestionId(), qid));
            int questionScore = 100 / questions.size();
            if (!match) {
                logger.warn("fetch subjective question failed:hid {},objectiveConfigType {},qid {}", hid, objectiveConfigType, qid);
                return MapMessage.errorMessage("作业不包含该题");
            }
            if (liveCastHomework.getClazzGroupId() == null) {
                logger.warn("fetch subjective question failed:hid {},objectiveConfigType {},qid {}", hid, objectiveConfigType, qid);
                return MapMessage.errorMessage("作业基础数据不包含班组ID");
            }
            List<Long> useIds = thirdPartyGroupLoaderClient.loadGroupStudentIds(liveCastHomework.getClazzGroupId());
            Map<Long, User> userMap = userLoaderClient.loadUsers(useIds);
            LiveCastHomework.Location location = liveCastHomework.toLocation();
            String month = MonthRange.newInstance(location.getCreateTime()).toString();
            Map<Long, String> liveCastHomeworkResultIdMap = userMap.keySet()
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Function.identity(), o -> new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), o).toString()));
            Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap = liveCastHomeworkResultDao.loads(liveCastHomeworkResultIdMap.values());
            List<LiveCastHomeworkResult> liveCastHomeworkResults = liveCastHomeworkResultMap.values()
                    .stream()
                    .filter(o -> Objects.nonNull(o.getPractices()))
                    .filter(o -> o.getPractices().containsKey(objectiveConfigType))
                    .collect(Collectors.toList());
            List<String> liveCastHomeworkProcessResultIds = liveCastHomeworkResults.stream()
                    .map(o -> {
                        NewHomeworkResultAnswer newHomeworkResultAnswer = o.getPractices().get(objectiveConfigType);
                        LinkedHashMap<String, String> map = newHomeworkResultAnswer.processAnswers();
                        return map.getOrDefault(qid, null);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap = liveCastHomeworkProcessResultDao.loads(liveCastHomeworkProcessResultIds);
            LiveCastSubjectiveQuestion liveCastSubjectiveQuestion = new LiveCastSubjectiveQuestion();
            liveCastSubjectiveQuestion.setQid(qid);
            liveCastSubjectiveQuestion.setQuestionScore(questionScore);
            Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
            NewQuestion newQuestion = questionLoaderClient.loadQuestionIncludeDisabled(qid);
            if (newQuestion == null) {
                logger.warn("fetch subjective question failed:hid {},objectiveConfigType {},qid {}", hid, objectiveConfigType, qid);
                return MapMessage.errorMessage("题目不存在");
            }
            if (contentTypeMap.containsKey(newQuestion.getContentTypeId())) {
                String content = contentTypeMap.get(newQuestion.getContentTypeId()).getName();
                liveCastSubjectiveQuestion.setContentType(content);
            }
            liveCastSubjectiveQuestion.setDifficultyInt(newQuestion.getDifficultyInt());


            List<LiveCastSubjectiveQuestion.StudentSubjectiveQuestionInfo> subjectiveQuestionInfos = liveCastHomeworkProcessResultMap.values()
                    .stream()
                    .map(o -> {
                        LiveCastSubjectiveQuestion.StudentSubjectiveQuestionInfo studentSubjectiveQuestionInfo = new LiveCastSubjectiveQuestion.StudentSubjectiveQuestionInfo();
                        if (CollectionUtils.isNotEmpty(o.getFiles())) {
                            List<NewHomeworkQuestionFile> newHomeworkQuestionFiles = o.getFiles().get(0);
                            if (CollectionUtils.isNotEmpty(newHomeworkQuestionFiles)) {
                                for (NewHomeworkQuestionFile newHomeworkQuestionFile : newHomeworkQuestionFiles) {
                                    studentSubjectiveQuestionInfo.getUseAnswerPicture().add(NewHomeworkQuestionFileHelper.getFileUrl(newHomeworkQuestionFile));
                                }
                            }
                        }
                        if (CollectionUtils.isEmpty(studentSubjectiveQuestionInfo.getUseAnswerPicture())) return null;
                        studentSubjectiveQuestionInfo.setProcessId(o.getId());
                        studentSubjectiveQuestionInfo.setUseId(o.getUserId());
                        studentSubjectiveQuestionInfo.setUseName(userMap.containsKey(o.getUserId()) ? userMap.get(o.getUserId()).fetchRealname() : "");
                        studentSubjectiveQuestionInfo.setScore(SafeConverter.toDouble(o.getScore()));
                        studentSubjectiveQuestionInfo.setComment(o.getTeacherMark());
                        if (StringUtils.isNotBlank(o.getCorrectionImg())) {
                            for (String pic : StringUtils.split(o.getCorrectionImg(), ",")) {
                                studentSubjectiveQuestionInfo.getTeacherCorrectingPicture().add(pic);
                            }
                        }
                        return studentSubjectiveQuestionInfo;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            liveCastSubjectiveQuestion.setStudentSubjectiveQuestionInfos(subjectiveQuestionInfos);
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.set("liveCastSubjectiveQuestion", liveCastSubjectiveQuestion);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch subjective question failed:hid {},objectiveConfigType {},qid {}", hid, objectiveConfigType, qid, e);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public Boolean judgeLiveHomeworkSubjective(String hid) {
        if (StringUtils.isBlank(hid)) {
            logger.error("judge LiveHomework Subject hid {}", hid);
            return null;
        }
        LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(hid);
        if (liveCastHomework == null) {
            logger.error("judge LiveHomework Subject hid {}", hid);
            return null;
        }
        return liveCastHomework.getPractices().stream().anyMatch(o -> o.getType().isSubjective());
    }

    @Override
    public Boolean judgeLiveHomeworkObjective(String hid) {
        if (StringUtils.isBlank(hid)) {
            logger.error("judge LiveHomework Subject hid {}", hid);
            return null;
        }
        LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(hid);
        if (liveCastHomework == null) {
            logger.error("judge LiveHomework Subject hid {}", hid);
            return null;
        }
        return liveCastHomework.getPractices().stream().anyMatch(o -> !o.getType().isSubjective());
    }

    @Override
    public MapMessage loadExamQuestionByIds(String data) {
        try {
            Map<String, Object> map = JsonUtils.fromJson(data);
            if (map == null) {
                logger.error("flashReq is null, input: ", data);
                return MapMessage.errorMessage("参数错误");
            }
            if (!map.containsKey("ids")) {
                logger.error("flashReq is null, input: ", data);
                return MapMessage.errorMessage("参数错误");
            }
            Object ids = map.get("ids");
            if (ids instanceof Collection) {
                @SuppressWarnings("unchecked")
                Map<String, QuestionMapper> resultMap = questionLoaderClient.loadQuestionMapperByQids((Collection) ids, false, false, true);
                return MapMessage.successMessage().add("result", resultMap);
            } else {
                logger.error("flashReq is null, input: ", data);
                return MapMessage.errorMessage("参数错误");
            }
        } catch (Exception e) {
            logger.error("flashReq is null, input: ", data);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public Integer judgeLiveHomeworkType(String hid) {
        if (StringUtils.isBlank(hid)) {
            logger.error("judge LiveHomework Subject hid {}", hid);
            return null;
        }
        LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(hid);
        if (liveCastHomework == null) {
            logger.error("judge LiveHomework Subject hid {}", hid);
            return null;
        }
        boolean subjective = false;
        boolean flag = false;
        for (NewHomeworkPracticeContent newHomeworkPracticeContent : liveCastHomework.getPractices()) {
            if (newHomeworkPracticeContent.getType().isSubjective()) {
                subjective = true;
            } else {
                flag = true;
            }
        }
        if (subjective && flag) {
            return 3;
        } else {
            if (subjective) {
                return 1;
            } else {
                return 2;
            }
        }
    }

    @Override
    public MapMessage personalDubbingDetail(String homeworkId, Long studentId, String dubbingId, Long teacherId) {
        try {
            LiveCastHomework newHomework = liveCastHomeworkDao.load(homeworkId);
            if (newHomework == null) {
                return MapMessage.errorMessage("作业不存在");
            }
            NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.DUBBING);
            if (target == null) {
                return MapMessage.errorMessage("作业不包含" + ObjectiveConfigType.DUBBING.getValue());
            }
            //获取班级学生
            List<Long> useIds = thirdPartyGroupLoaderClient.loadGroupStudentIds(newHomework.getClazzGroupId());
            Map<Long, User> userMap = userLoaderClient.loadUsers(useIds);
            if (MapUtils.isNotEmpty(userMap) && !userMap.containsKey(studentId)) {
                return MapMessage.errorMessage("该学生不在该作业的班级");
            }
            LiveCastHomework.Location location = newHomework.toLocation();
            String month = MonthRange.newInstance(location.getCreateTime()).toString();
            LiveCastHomeworkResult.ID id = new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), studentId);
            LiveCastHomeworkResult newHomeworkResult = liveCastHomeworkResultDao.load(id.toString());
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


    // 1、初始化数据：livecasthomework、userMap、LiveCastHomeworkResultMap、LiveCastHomeworkProcessResultMap
    // 2、处理表格每个学生数据表示：processStudentReportBriefs
    // 3、作业每个类型的进行数据统计：processStatisticsToObjectiveConfigType（根据类型模板方式）
    @Override
    public LiveHomeworkReport obtainLiveHomeworkReport(String hid) {
        LiveHomeworkReport liveHomeworkReport = new LiveHomeworkReport();

        try {
            LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(hid);
            if (liveCastHomework == null) {
                logger.warn("liveCastHomework is null hid of {}", hid);
                liveHomeworkReport.setFailedInfo("作业不存在，请联系助教");
                return liveHomeworkReport;
            }
            boolean subjective = liveCastHomework.getPractices().stream().anyMatch(o -> o.getType().isSubjective());
            liveHomeworkReport.setSubjective(subjective);
            List<Long> useIds = thirdPartyGroupLoaderClient.loadGroupStudentIds(liveCastHomework.getClazzGroupId());
            Map<Long, User> userMap = userLoaderClient.loadUsers(useIds);
            LiveCastHomework.Location location = liveCastHomework.toLocation();
            String month = MonthRange.newInstance(location.getCreateTime()).toString();
            Map<Long, String> liveCastHomeworkResultIdMap = userMap.keySet()
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Function.identity(), o -> new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), o).toString()));
            Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap = liveCastHomeworkResultDao.loads(liveCastHomeworkResultIdMap.values());
            List<String> liveCastHomeworkProcessResultIds = liveCastHomeworkResultMap
                    .values()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(o -> o.findAllHomeworkProcessIds(true))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap = liveCastHomeworkProcessResultDao.loads(liveCastHomeworkProcessResultIds);
            LiveHomeworkReportContext liveHomeworkReportContext = new LiveHomeworkReportContext(liveCastHomework,
                    liveCastHomeworkResultMap,
                    liveCastHomeworkProcessResultMap,
                    userMap,
                    liveHomeworkReport,
                    liveCastHomeworkResultIdMap);
            List<String> allQuestionIds = liveCastHomework.findAllQuestionIds();
            Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(allQuestionIds);
            liveHomeworkReportContext.setNewQuestionMap(newQuestionMap);
            //处理学生表的数据
            liveHomeworkReportContext.processStudentReportBriefs();
            //处理学生批改情况
            processStuCorrectInfo(liveHomeworkReportContext);
            // 处理类型统计部分
            processStatisticsToObjectiveConfigType(liveHomeworkReportContext);
            liveHomeworkReport.setSuccess(true);
        } catch (Exception e) {
            logger.error("obtain LiveHomework Report failed : hid {}", hid, e);
            liveHomeworkReport.setFailedInfo(e.getMessage());
        }
        return liveHomeworkReport;
    }

    /**
     * 处理学生批改情况
     * @param liveHomeworkReportContext
     */
    private void processStuCorrectInfo(LiveHomeworkReportContext liveHomeworkReportContext) {
        if (liveHomeworkReportContext == null || liveHomeworkReportContext.getLiveHomeworkReport() == null) {
            return;
        }
        List<LiveHomeworkReport.StudentReportBrief> studentReportBriefs = liveHomeworkReportContext.getLiveHomeworkReport().getStudentReportBriefs();
        if (CollectionUtils.isEmpty(studentReportBriefs)) {
            return;
        }
        Map<Long, String> liveCastHomeworkResultIdMap = liveHomeworkReportContext.getLiveCastHomeworkResultIdMap();
        Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap = liveHomeworkReportContext.getLiveCastHomeworkResultMap();
        if (MapUtils.isEmpty(liveCastHomeworkResultMap)) {
            return;
        }
        //获取有订正的题目的processId
        List<String> allStuProcessIds = Lists.newArrayList();
        List<String> photoProcessIds = liveCastHomeworkResultMap.values().stream().map(r -> r.findHomeworkProcessIdsByObjectiveConfigType(ObjectiveConfigType.PHOTO_OBJECTIVE)).flatMap(List::stream).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(photoProcessIds)) {
            allStuProcessIds.addAll(photoProcessIds);
        }
        List<String> voiceObjectIds = liveCastHomeworkResultMap.values().stream().map(r -> r.findHomeworkProcessIdsByObjectiveConfigType(ObjectiveConfigType.VOICE_OBJECTIVE)).flatMap(List::stream).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(voiceObjectIds)) {
            allStuProcessIds.addAll(voiceObjectIds);
        }
        Map<String, LiveCastHomeworkProcessResult> allStuProcessResultMap = liveCastHomeworkProcessResultDao.loads(allStuProcessIds);
        if (MapUtils.isEmpty(allStuProcessResultMap)) {
            return;
        }
        for (LiveHomeworkReport.StudentReportBrief studentReportBrief : studentReportBriefs) {
            String homeworkresultId = liveCastHomeworkResultIdMap.get(studentReportBrief.getSid());
            if (StringUtils.isEmpty(homeworkresultId)) {
                continue;
            }
            LiveCastHomeworkResult liveCastHomeworkResult = liveCastHomeworkResultMap.get(homeworkresultId);
            if (liveCastHomeworkResult == null || liveCastHomeworkResult.getPractices() == null || !liveCastHomeworkResult.isFinished()) {
                continue;
            }
            //获取有订正的题目的processId
            List<String> newHomeworkProcessIds = Lists.newArrayList();
            List<String> stuPhotoIds = liveCastHomeworkResult.findHomeworkProcessIdsByObjectiveConfigType(ObjectiveConfigType.PHOTO_OBJECTIVE);
            if (CollectionUtils.isNotEmpty(stuPhotoIds)) {
                newHomeworkProcessIds.addAll(stuPhotoIds);
            }
            List<String> stuVoiceIds = liveCastHomeworkResult.findHomeworkProcessIdsByObjectiveConfigType(ObjectiveConfigType.VOICE_OBJECTIVE);
            if (CollectionUtils.isNotEmpty(stuVoiceIds)) {
                newHomeworkProcessIds.addAll(stuVoiceIds);
            }
            if (CollectionUtils.isEmpty(newHomeworkProcessIds)) {
                continue;
            }
            Map<String, LiveCastHomeworkProcessResult> allProcessResultMap = Maps.newLinkedHashMap();
            newHomeworkProcessIds.forEach(p -> {
                if (allStuProcessResultMap.get(p) != null) {
                    allProcessResultMap.put(p, allStuProcessResultMap.get(p));
                }
            });
            if (MapUtils.isEmpty(allProcessResultMap)) {
                continue;
            }
            List<LiveCastHomeworkProcessResult> subjectedResult = allProcessResultMap.values().stream()
                    .filter(p -> p.getObjectiveConfigType().equals(ObjectiveConfigType.PHOTO_OBJECTIVE)
                            || p.getObjectiveConfigType().equals(ObjectiveConfigType.VOICE_OBJECTIVE))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(subjectedResult)) {
                continue;
            }
            Map<String, LiveCastHomeworkProcessResult> questionProcessResultMap = subjectedResult.stream().collect(Collectors.toMap(LiveCastHomeworkProcessResult::getQuestionId, Function.identity()));
            List<String> questionIds = liveHomeworkReportContext.getLiveCastHomework().findAllQuestionIds();
            boolean isAllCorrected = subjectedResult.stream().allMatch(r -> SafeConverter.toBoolean(r.getReview()));
            studentReportBrief.setCorrected(isAllCorrected);
            List<Double> percentageInfo = new LinkedList<>();
            questionIds.forEach(q -> {
                if (questionProcessResultMap.get(q) != null && (questionProcessResultMap.get(q).getObjectiveConfigType().equals(ObjectiveConfigType.PHOTO_OBJECTIVE)
                        || questionProcessResultMap.get(q).getObjectiveConfigType().equals(ObjectiveConfigType.VOICE_OBJECTIVE))) {
                    percentageInfo.add(SafeConverter.toDouble(questionProcessResultMap.get(q).getPercentage()));
                }
            });
            studentReportBrief.setPercentageInfo(percentageInfo);
        }
    }

    @Override
    public Map<String, LiveCastHomework> obtainLiveHomeworkMap(List<String> hids) {
        return liveCastHomeworkDao.loads(hids);
    }

    private void processStatisticsToObjectiveConfigType(LiveHomeworkReportContext liveHomeworkReportContext) {
        LiveHomeworkReport liveHomeworkReport = liveHomeworkReportContext.getLiveHomeworkReport();
        for (ObjectiveConfigType type : liveHomeworkReportContext.getObjectiveConfigTypes()) {
            StatisticsToObjectiveConfigTypeTemple template = statisticsToObjectiveConfigTypeFactory.getTemplate(type);
            if (template != null) {
                LiveHomeworkReport.StatisticsToObjectiveConfigType statisticsToObjectiveConfigType = template.statisticsToObjectiveConfigType(liveHomeworkReportContext, type);
                if (statisticsToObjectiveConfigType == null) {
                    continue;
                }
                liveHomeworkReport.getStatisticsToObjectiveConfigTypes().add(statisticsToObjectiveConfigType);
            }
        }

    }

    //1、初始化数据useIdMap
    //2、根据groupIds查询locations
    //3、构件LiveCastHomeworkLocationLoader
    //4、liveCastHomeworkResultMap数据初始化
    //5、构件返回数据Page
    @Override
    public LiveHomeworkBriefPage pageHomeworkReportListByGroupIds(Collection<Long> groupIds,
                                                                  Integer page,
                                                                  Integer size) {
        try {
            Pageable pageable = new PageRequest(page, size);
            if (CollectionUtils.isEmpty(groupIds)) {
                logger.warn("page Homework Report List By GroupIds of groupIds is empty");
                return new LiveHomeworkBriefPage(new PageImpl<>(Collections.emptyList()));
            }
            Map<Long, List<User>> groupStudents = new LinkedHashMap<>();
            Map<Long, List<Long>> useIdMap = thirdPartyGroupLoaderClient.loadGroupStudentIds(groupIds);
            for (Map.Entry<Long, List<Long>> entry : useIdMap.entrySet()) {
                groupStudents.put(entry.getKey(), new LinkedList<>(userLoaderClient.loadUsers(entry.getValue()).values()));
            }
            Set<LiveCastHomework.Location> locations =
                    liveCastHomeworkDao.loadLiveCastHomeworkByClazzGroupIds(groupIds).values()
                            .stream()
                            .flatMap(List::stream)
                            .collect(Collectors.toSet());
            LocationTransformer<LiveCastHomework.Location, LiveCastHomework> transformer = candidate -> {
                List<String> idList = candidate.stream()
                        .map(LiveCastHomework.Location::getId)
                        .collect(Collectors.toList());
                Map<String, LiveCastHomework> map = liveCastHomeworkDao.loads(idList);
                return idList.stream()
                        .filter(map::containsKey)
                        .map(map::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            };
            LiveCastHomeworkLocationLoader liveCastHomeworkLocationLoader = new LiveCastHomeworkLocationLoader(transformer, locations);
            Page<LiveCastHomework> liveCastHomeworkPage = liveCastHomeworkLocationLoader
                    .sorted((o1, o2) -> {
                        long createAt1 = o1.getCreateTime();
                        long createAt2 = o2.getCreateTime();
                        return Long.compare(createAt2, createAt1);
                    }).toPage(pageable);
            List<LiveCastHomework> liveCastHomeworkList = liveCastHomeworkPage.getContent();
            Map<String, List<String>> liveCastHomeworkResultIdMap
                    = liveCastHomeworkList.stream()
                    .collect(
                            Collectors.toMap(LiveCastHomework::getId,
                                    o -> {
                                        LiveCastHomework.Location location = o.toLocation();
                                        String month = MonthRange.newInstance(location.getCreateTime()).toString();
                                        List<User> users = groupStudents.get(o.getClazzGroupId());
                                        if (CollectionUtils.isNotEmpty(users)) {
                                            return users.stream()
                                                    .map(t -> new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), t.getId()).toString())
                                                    .collect(Collectors.toList());
                                        } else {
                                            return Collections.emptyList();
                                        }
                                    }));
            List<String> liveCastHomeworkResultIds = liveCastHomeworkResultIdMap.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap = liveCastHomeworkResultDao.loads(liveCastHomeworkResultIds);
            List<LiveHomeworkBrief> liveHomeworkBriefs = new LinkedList<>();
            for (LiveCastHomework liveCastHomework : liveCastHomeworkList) {
                LiveHomeworkBrief liveHomeworkBrief = new LiveHomeworkBrief();
                liveHomeworkBrief.setHomeworkId(liveCastHomework.getId());
                liveHomeworkBrief.setClazzGroupId(liveCastHomework.getClazzGroupId());
                liveHomeworkBrief.setEndTime(liveCastHomework.getEndTime());
                liveHomeworkBrief.setStartTime(liveCastHomework.getStartTime());
                liveHomeworkBrief.setSubject(liveCastHomework.getSubject());
                List<User> users = groupStudents.get(liveCastHomework.getClazzGroupId());
                if (users != null) {
                    liveHomeworkBrief.setUserCount(users.size());
                    List<String> resultIds = liveCastHomeworkResultIdMap.get(liveCastHomework.getId());
                    if (resultIds != null) {
                        long count = resultIds.stream()
                                .filter(liveCastHomeworkResultMap::containsKey)
                                .filter(o -> liveCastHomeworkResultMap.get(o).isFinished())
                                .count();
                        liveHomeworkBrief.setFinishedCount(count);
                    }
                }
                liveHomeworkBriefs.add(liveHomeworkBrief);
            }
            return new LiveHomeworkBriefPage(new PageImpl<>(liveHomeworkBriefs, pageable, liveCastHomeworkPage.getTotalElements()));
        } catch (Exception ex) {
            logger.error("page Homework ReportList ByGroupIds of {},pageable of {} ", groupIds, page, ex);
            return new LiveHomeworkBriefPage(new PageImpl<>(Collections.emptyList()));
        }
    }

    @Override
    public StudentLiveHomeworkDetail fetchStudentLiveHomeworkDetail(String hid, Long userId) {
        StudentLiveHomeworkDetail studentLiveHomeworkDetail = new StudentLiveHomeworkDetail();
        LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(hid);
        if (liveCastHomework == null) {
            studentLiveHomeworkDetail.setSuccess(false);
            logger.error("fetch StudentLiveHomeworkDetail failed : hid {},userId {}", hid, userId);
            studentLiveHomeworkDetail.setFailedInfo("liveCastHomework is null");
            return studentLiveHomeworkDetail;
        }
        studentLiveHomeworkDetail.setUserId(userId);
        List<Long> useIds = thirdPartyGroupLoaderClient.loadGroupStudentIds(liveCastHomework.getClazzGroupId());
        Map<Long, User> userMap = userLoaderClient.loadUsers(useIds);
        if (!userMap.containsKey(userId)) {
            studentLiveHomeworkDetail.setSuccess(false);
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", userId,
                    "mod1", hid,
                    "op", "studentLiveHomeworkDetail"
            ));
            studentLiveHomeworkDetail.setFailedInfo("userId is error");
            return studentLiveHomeworkDetail;
        }
        LiveCastHomework.Location location = liveCastHomework.toLocation();
        String month = MonthRange.newInstance(location.getCreateTime()).toString();
        LiveCastHomeworkResult.ID id = new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), userId);
        LiveCastHomeworkResult liveCastHomeworkResult = liveCastHomeworkResultDao.load(id.toString());
        if (liveCastHomeworkResult != null) {
            studentLiveHomeworkDetail.setFinished(liveCastHomeworkResult.isFinished());
            studentLiveHomeworkDetail.setUserName(userMap.get(userId).fetchRealname());
        }

        Integer liveHomeworkType;
        boolean subjective = false;
        boolean flag = false;
        for (NewHomeworkPracticeContent newHomeworkPracticeContent : liveCastHomework.getPractices()) {
            if (newHomeworkPracticeContent.getType().isSubjective()) {
                subjective = true;
            } else {
                flag = true;
            }
        }
        if (subjective && flag) {
            liveHomeworkType = 3;
        } else {
            if (subjective) {
                liveHomeworkType = 1;
            } else {
                liveHomeworkType = 2;
            }
        }
        studentLiveHomeworkDetail.setLiveHomeworkType(liveHomeworkType);
        studentLiveHomeworkDetail.setSuccess(true);
        return studentLiveHomeworkDetail;
    }

    @Override
    public Map<String, LiveCastHomeworkDetail> fetchLiveCastHomeworkDetail(Collection<String> homeworkIds) {
        try {
            if (CollectionUtils.isEmpty(homeworkIds)) {
                return Collections.emptyMap();
            }
            Map<String, LiveCastHomework> liveCastHomeworkMap = liveCastHomeworkDao.loads(homeworkIds);
            Set<Long> groupIds = liveCastHomeworkMap.values()
                    .stream()
                    .map(BaseHomework::getClazzGroupId)
                    .collect(Collectors.toSet());
            Map<Long, List<Long>> groupStudents = thirdPartyGroupLoaderClient.loadGroupStudentIds(groupIds);

            Map<String, LiveCastHomeworkDetail> liveCastHomeworkDetailMap = liveCastHomeworkMap.values()
                    .stream()
                    .collect(Collectors.toMap(LiveCastHomework::getId, o -> {
                        LiveCastHomeworkDetail detail = new LiveCastHomeworkDetail();
                        detail.setHomeworkId(o.getId());
                        detail.setGroupId(o.getClazzGroupId());
                        if (groupStudents.containsKey(detail.getGroupId())) {
                            detail.setUserCount(groupStudents.get(detail.getGroupId()).size());
                        }
                        boolean hasSubjective = false;
                        for (NewHomeworkPracticeContent content : o.getPractices()) {
                            if (content.getType().isSubjective()) {
                                hasSubjective = true;
                                break;
                            }
                        }
                        detail.setHasSubjective(hasSubjective);
                        return detail;
                    }));
            Map<String, List<String>> liveCastHomeworkResultIdMap
                    = liveCastHomeworkMap.values().stream()
                    .collect(
                            Collectors.toMap(LiveCastHomework::getId,
                                    o -> {
                                        LiveCastHomework.Location location = o.toLocation();
                                        String month = MonthRange.newInstance(location.getCreateTime()).toString();
                                        List<Long> users = groupStudents.get(o.getClazzGroupId());
                                        if (CollectionUtils.isNotEmpty(users)) {
                                            return users.stream()
                                                    .map(t -> new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), t).toString())
                                                    .collect(Collectors.toList());
                                        } else {
                                            return Collections.emptyList();
                                        }
                                    }));
            List<String> liveCastHomeworkResultIds = liveCastHomeworkResultIdMap.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap = liveCastHomeworkResultDao.loads(liveCastHomeworkResultIds);
            for (LiveCastHomeworkResult n : liveCastHomeworkResultMap.values()) {
                if (liveCastHomeworkDetailMap.containsKey(n.getHomeworkId())) {
                    LiveCastHomeworkDetail detail = liveCastHomeworkDetailMap.get(n.getHomeworkId());
                    if (n.isFinished()) {
                        detail.setFinishedCount(detail.getFinishedCount() + 1);
                    }
                    boolean needCheck = false;
                    if (n.getPractices() != null) {
                        //一个学生是否需要检查
                        //学生有主观作业，
                        //全部的主观作业是没有分数的
                        boolean hasSubjective = detail.isHasSubjective();
                        //是否全部主观作业没有分数
                        boolean flag = true;
                        for (Map.Entry<ObjectiveConfigType, NewHomeworkResultAnswer> entry : n.getPractices().entrySet()) {
                            if (entry.getKey().isSubjective()) {
                                Integer score = entry.getValue().processScore(entry.getKey());
                                if (score != null) {
                                    flag = false;
                                    break;
                                }
                            }
                        }
                        if (hasSubjective && flag) {
                            needCheck = true;
                        } else {
                            detail.setCheckedCount(1 + detail.getCheckedCount());
                        }
                    }
                    if (!detail.isNeedCheck() && needCheck) {
                        detail.setNeedCheck(true);
                    }
                    List<String> newHomeworkProcessIds = n.findAllHomeworkProcessIds(true);
                    if (CollectionUtils.isEmpty(newHomeworkProcessIds)) {
                        continue;
                    }
                    Map<String, LiveCastHomeworkProcessResult> allProcessResultMap = liveCastHomeworkProcessResultDao.loads(newHomeworkProcessIds);
                    if (MapUtils.isEmpty(allProcessResultMap)) {
                        continue;
                    }
                    List<LiveCastHomeworkProcessResult> subjectedResult = allProcessResultMap.values().stream()
                            .filter(p -> p.getObjectiveConfigType().equals(ObjectiveConfigType.PHOTO_OBJECTIVE)
                                    || p.getObjectiveConfigType().equals(ObjectiveConfigType.VOICE_OBJECTIVE))
                            .collect(Collectors.toList());
                    if(CollectionUtils.isEmpty(subjectedResult)){
                        continue;
                    }
                    boolean corrected = subjectedResult.stream().allMatch(r -> SafeConverter.toBoolean(r.getReview()));
                    if (corrected) {
                        detail.setCorrectCount(detail.getCorrectCount() + 1);
                    }
                }
            }
            return liveCastHomeworkDetailMap;
        } catch (Exception e) {
            logger.error("fetch Live Cast HomeworkDetail failed : homeworkIds {}", homeworkIds, e);
            return Collections.emptyMap();
        }
    }


    //1、作业查询条件限制，类型数据初始化
    //2、processNewHomework方法模板处理每个类型的数据
    //3、构件返回数据
    @Override
    public MapMessage loadLiveCastHomeworkReportDetail(String homeworkId) {
        MapMessage mapMessage = new MapMessage();

        try {
//            NewHomework newHomework = newHomeworkLoader.load("201707_597edbffac74599f2f9fb29a_1");
//            NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook("201707_597edbffac74599f2f9fb29a_1");
//            mapMessage.put("newHomework", newHomework);
//            mapMessage.put("newHomeworkBook", newHomeworkBook);
            LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(homeworkId);
            if (liveCastHomework == null) {
                return MapMessage.errorMessage("作业题目有误，请联系助教").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEW_HOMEWORK_TYPE_NOT_EXIST);
            }
            if (CollectionUtils.isEmpty(liveCastHomework.getPractices())) {
                return MapMessage.errorMessage("这份作业里面不存在习题").setErrorCode(ErrorCodeConstants.ERROR_CODE_MISS_HOMEWORK_PRACTICE);
            }
            Map<String, String> objectiveConfigTypes = new LinkedHashMap<>();
            List<String> objectiveConfigTypeRanks = new LinkedList<>();
            Map<String, Object> questionInfoToType = processNewHomework(liveCastHomework);
            if (MapUtils.isEmpty(questionInfoToType)) {
                mapMessage.setSuccess(false);
                mapMessage.setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
                mapMessage.add("info", "该作业类型还没有学生完成哦！");
                return mapMessage;
            }
            List<ObjectiveConfigType> types = liveCastHomework.getPractices()
                    .stream()
                    .map(NewHomeworkPracticeContent::getType)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            types.stream()
                    .filter(type -> questionInfoToType.containsKey(type.name()))
                    .forEach(type -> {
                        objectiveConfigTypes.put(type.name(), type.getValue());
                        objectiveConfigTypeRanks.add(type.name());
                    });
            mapMessage.putAll(
                    MapUtils.m(
                            "questionInfoToType", questionInfoToType,
                            "objectiveConfigTypes", objectiveConfigTypes,
                            "objectiveConfigTypeRanks", objectiveConfigTypeRanks,
                            "homeworkId", homeworkId,
                            "subject", liveCastHomework.getSubject()
                    ));
            mapMessage.setSuccess(true);
        } catch (Exception e) {
            logger.error("get report failed : of hid {} ", homeworkId, e);
            mapMessage.setSuccess(false).setInfo("获取报告，学生完成情况失败").setErrorCode(ErrorCodeConstants.ERROR_CODE_COMMON);
        }
        return mapMessage;
    }

    //1、作业查询条件限制，类型数据初始化
    //2、processNewHomework 方法模板处理每个类型的数据
    //3、构件返回数据
    @Override
    public MapMessage loadLiveCastHomeworkReportDetail(String homeworkId, Long studentId) {
        MapMessage mapMessage = new MapMessage();
        try {
            LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(homeworkId);
            if (liveCastHomework == null) {
                return MapMessage.errorMessage("作业不存在，请联系助教").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEW_HOMEWORK_TYPE_NOT_EXIST);
            }
            if (CollectionUtils.isEmpty(liveCastHomework.getPractices())) {
                return MapMessage.errorMessage("作业题目有误，请联系助教").setErrorCode(ErrorCodeConstants.ERROR_CODE_MISS_HOMEWORK_PRACTICE);
            }
            List<Long> useIds = thirdPartyGroupLoaderClient.loadGroupStudentIds(liveCastHomework.getClazzGroupId());
            Map<Long, User> userMap = userLoaderClient.loadUsers(useIds);
            if (!userMap.containsKey(studentId)) {
                return MapMessage.errorMessage("您未被布置此次作业").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_PERMISSION);
            }
            LiveCastHomework.Location location = liveCastHomework.toLocation();
            String month = MonthRange.newInstance(location.getCreateTime()).toString();
            LiveCastHomeworkResult.ID id = new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), studentId);
            LiveCastHomeworkResult liveCastHomeworkResult = liveCastHomeworkResultDao.load(id.toString());
            if (liveCastHomeworkResult == null
                    || MapUtils.isEmpty(liveCastHomeworkResult.getPractices())) {
                return MapMessage.errorMessage("学生还未开始写作业").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
            }
            User user = userMap.get(studentId);
            long count = liveCastHomeworkResult.getPractices().values().stream().filter(NewHomeworkResultAnswer::isFinished).count();
            if (count == 0) {
                return MapMessage.errorMessage("学生还未完成作业").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
            }
            Map<ObjectiveConfigType, Object> questionInfoMapper = processNewHomework(user, liveCastHomework, liveCastHomeworkResult);
            Map<String, String> objectiveConfigTypes = questionInfoMapper
                    .keySet()
                    .stream()
                    .collect(Collectors.toMap(Enum::name, ObjectiveConfigType::getValue));
            List<ObjectiveConfigType> types = liveCastHomework
                    .findPracticeContents()
                    .keySet()
                    .stream()
                    .sorted(new ObjectiveConfigType.ObjectiveConfigTypeComparator(liveCastHomework.getSubject()))
                    .collect(Collectors.toList());
            List<String> objectiveConfigTypeRanks = types.stream()
                    .filter(questionInfoMapper::containsKey)
                    .map(Enum::name)
                    .collect(Collectors.toList());
            mapMessage.add("questionInfoMapper", questionInfoMapper);
            mapMessage.add("objectiveConfigTypes", objectiveConfigTypes);
            mapMessage.add("objectiveConfigTypeRanks", objectiveConfigTypeRanks);
            mapMessage.add("userId", user.getId());
            mapMessage.add("userName", user.fetchRealname());
            mapMessage.add("finished", liveCastHomeworkResult.isFinished());
            mapMessage.add("homeworkId", homeworkId);
            mapMessage.add("subject", liveCastHomework.getSubject());
            mapMessage.setSuccess(true);
        } catch (Exception e) {
            logger.warn("load LiveCastHomework Report Detail failed of hid {},sid {}", homeworkId, studentId, e);
            mapMessage.setSuccess(false).setInfo("获取报告，学生完成情况失败").setErrorCode(ErrorCodeConstants.ERROR_CODE_COMMON);
        }
        return mapMessage;
    }


    @Override
    public MapMessage reportDetailsBaseApp(String homeworkId, String categoryId, String lessonId, ObjectiveConfigType objectiveConfigType) {
        MapMessage mapMessage = new MapMessage();
        try {
            LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(homeworkId);
            if (Objects.isNull(liveCastHomework)) {
                return MapMessage.errorMessage("homework does not exist");
            }
            NewHomeworkApp target = null;
            List<NewHomeworkApp> apps = liveCastHomework.findNewHomeworkApps(objectiveConfigType);
            List<String> questionIds = new LinkedList<>();
            for (NewHomeworkApp o : apps) {
                if (Objects.equals(categoryId, SafeConverter.toString(o.getCategoryId()))
                        && Objects.equals(lessonId, o.getLessonId())
                        && CollectionUtils.isNotEmpty(o.getQuestions())) {
                    questionIds = o.getQuestions()
                            .stream()
                            .map(NewHomeworkQuestion::getQuestionId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    target = o;
                    break;
                }
            }
            PracticeType practiceType = Objects.nonNull(target) ? practiceServiceClient.getPracticeBuffer().loadPractice(target.getPracticeId()) : null;
            if (Objects.isNull(practiceType)) {
                return MapMessage.errorMessage("practiceType does not exist");
            }
            mapMessage.add("questionInfoMapper", processNewHomeworkForBaseApp(liveCastHomework, categoryId, lessonId, practiceType, questionIds, objectiveConfigType));
            mapMessage.add("homeworkId", homeworkId);
            mapMessage.add("description", practiceType.getDescription());
            mapMessage.add("needRecord", practiceType.getNeedRecord());
            mapMessage.add("categoryName", practiceType.getCategoryName());
            mapMessage.add("categoryId", practiceType.getCategoryId());
            mapMessage.setSuccess(true);
        } catch (Exception e) {
            logger.error("get report Details BaseApp failed : hid {},categoryId {},lessonId {},objectiveConfigType {}", homeworkId, categoryId, lessonId, objectiveConfigType, e);
            return MapMessage.errorMessage(e.getMessage());
        }

        return mapMessage;
    }


    //
    @Override
    public MapMessage reportDetailsBaseApp(String homeworkId, String categoryId, String lessonId, Long studentId, ObjectiveConfigType objectiveConfigType) {
        MapMessage mapMessage = new MapMessage();
        try {
            LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(homeworkId);
            if (liveCastHomework == null) {
                return MapMessage.errorMessage("该作业不存在");
            }
            List<Long> useIds = thirdPartyGroupLoaderClient.loadGroupStudentIds(liveCastHomework.getClazzGroupId());
            Map<Long, User> userMap = userLoaderClient.loadUsers(useIds);
            if (!userMap.containsKey(studentId)) {
                return MapMessage.errorMessage("该学生不在该作业的班级");
            }
            List<NewHomeworkApp> apps = liveCastHomework.findNewHomeworkApps(objectiveConfigType);
            NewHomeworkApp target = CollectionUtils.isNotEmpty(apps) ?
                    apps.stream()
                            .filter(o -> Objects.equals(categoryId, SafeConverter.toString(o.getCategoryId())))
                            .filter(o -> Objects.equals(lessonId, o.getLessonId()))
                            .findFirst()
                            .orElse(null) : null;
            PracticeType practiceType = Objects.nonNull(target) ?
                    practiceServiceClient.getPracticeBuffer().loadPractice(target.getPracticeId()) :
                    null;
            if (Objects.isNull(practiceType)) {
                return MapMessage.errorMessage("practiceType is null");
            }
            LiveCastHomework.Location location = liveCastHomework.toLocation();
            String month = MonthRange.newInstance(location.getCreateTime()).toString();
            LiveCastHomeworkResult.ID id = new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), studentId);
            LiveCastHomeworkResult liveCastHomeworkResult = liveCastHomeworkResultDao.load(id.toString());
            if (Objects.isNull(liveCastHomeworkResult) ||
                    MapUtils.isEmpty(liveCastHomeworkResult.getPractices())) {
                return MapMessage.errorMessage("学生还未开始写作业");
            }
            User user = userLoaderClient.loadUserIncludeDisabled(studentId);
            List<BasicAppQuestionInfo> basicAppQuestionInfos = newInternalProcessHomeworkAnswer(userMap, liveCastHomeworkResult, categoryId, lessonId, practiceType, target, objectiveConfigType);
            mapMessage.add("description", practiceType.getDescription());
            mapMessage.add("questionInfoMapper", basicAppQuestionInfos);
            mapMessage.add("needRecord", practiceType.getNeedRecord());
            mapMessage.add("userId", user.getId());
            mapMessage.add("userName", user.fetchRealname());
            mapMessage.add("homeworkId", homeworkId);
            mapMessage.add("categoryName", practiceType.getCategoryName());
            mapMessage.setSuccess(true);
        } catch (Exception e) {
            logger.error("get report Details BaseApp failed : hid {},categoryId {},lessonId {},objectiveConfigType {},sid {}", homeworkId, categoryId, lessonId, objectiveConfigType, studentId, e);
            return MapMessage.errorMessage(e.getMessage());
        }
        return mapMessage;
    }


    //1、条件限制
    //2、数据初始化
    //3、oralAnswers 循环
    //4、answers 循环处理
    //5、数据组织
    @Override
    public MapMessage personalReadingDetail(String homeworkId, Long studentId, String readingId) {
        MapMessage mapMessage = new MapMessage();
        try {
            LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(homeworkId);
            User u = studentLoaderClient.loadStudent(studentId);
            if (Objects.isNull(liveCastHomework)) {
                return MapMessage.errorMessage("homework does not exist");
            }
            List<Long> useIds = thirdPartyGroupLoaderClient.loadGroupStudentIds(liveCastHomework.getClazzGroupId());
            Map<Long, User> userMap = userLoaderClient.loadUsers(useIds);
            if (!userMap.containsKey(studentId)) {
                return MapMessage.errorMessage("该学生不在该作业的班级");
            }
            LiveCastHomework.Location location = liveCastHomework.toLocation();
            String month = MonthRange.newInstance(location.getCreateTime()).toString();
            LiveCastHomeworkResult.ID id = new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), studentId);
            LiveCastHomeworkResult liveCastHomeworkResult = liveCastHomeworkResultDao.load(id.toString());
            if (liveCastHomeworkResult == null) {
                return MapMessage.errorMessage("数据错误");
            }
            NewHomeworkResultAnswer newHomeworkResultAnswer = liveCastHomeworkResult.getPractices().get(ObjectiveConfigType.READING);
            if (newHomeworkResultAnswer == null) {
                return MapMessage.errorMessage("数据错误");
            }
            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(readingId);
            if (newHomeworkResultAppAnswer == null) {
                return MapMessage.errorMessage("数据错误");
            }
            Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(Collections.singleton(readingId));
            List<String> liveCastHomeworkProcessResultIds = new LinkedList<>();
            List<String> questionIds = new LinkedList<>();
            if (MapUtils.isNotEmpty(newHomeworkResultAppAnswer.getOralAnswers())) {
                liveCastHomeworkProcessResultIds.addAll(newHomeworkResultAppAnswer.getOralAnswers().values());
                questionIds.addAll(newHomeworkResultAppAnswer.getOralAnswers().keySet());
            }
            if (MapUtils.isNotEmpty(newHomeworkResultAppAnswer.getAnswers())) {
                liveCastHomeworkProcessResultIds.addAll(newHomeworkResultAppAnswer.getAnswers().values());
                questionIds.addAll(newHomeworkResultAppAnswer.getAnswers().keySet());
            }
            Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap = liveCastHomeworkProcessResultDao.loads(liveCastHomeworkProcessResultIds);
            Map<String, NewQuestion> questionsMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
            Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
            List<ReadingExercisesInfo.OralQuestion> oralQuestions = new LinkedList<>();
            ReadingExercisesInfo readingExercisesInfo = new ReadingExercisesInfo();
            //是否包含口语题
            if (MapUtils.isNotEmpty(newHomeworkResultAppAnswer.getOralAnswers())) {
                LinkedHashMap<String, String> oralAnswers = newHomeworkResultAppAnswer.getOralAnswers();
                oralAnswers.forEach((key, value) -> {
                    LiveCastHomeworkProcessResult newHomeworkProcessResult = liveCastHomeworkProcessResultMap.get(value);
                    if (newHomeworkProcessResult != null && questionsMap.containsKey(key)) {
                        NewQuestion newQuestion = questionsMap.get(key);
                        List<NewQuestionsSubContents> subContents = newQuestion.getContent().getSubContents();
                        int i = 0;
                        for (NewQuestionsSubContents newQuestionsSubContents : subContents) {
                            if (newQuestionsSubContents.getOralDict() != null
                                    && CollectionUtils.isNotEmpty(newQuestionsSubContents.getOralDict().getOptions())
                                    && newHomeworkProcessResult.getOralDetails().size() >= (i + 1)) {
                                List<LiveCastHomeworkProcessResult.OralDetail> oralDetails = newHomeworkProcessResult.getOralDetails().get(i);
                                List<NewQuestionOralDictOptions> options = newQuestionsSubContents.getOralDict().getOptions();
                                int j = 0;
                                for (NewQuestionOralDictOptions newQuestionOralDictOptions : options) {
                                    if (oralDetails.size() >= (j + 1)) {
                                        LiveCastHomeworkProcessResult.OralDetail oralDetail = oralDetails.get(j);
                                        String voiceUrl = oralDetail.getAudio();
                                        VoiceEngineType voiceEngineType = newHomeworkProcessResult.getVoiceEngineType();
                                        voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                                        ReadingExercisesInfo.OralQuestion oralQuestion = new ReadingExercisesInfo.OralQuestion();
                                        oralQuestion.setText(newQuestionOralDictOptions.getText());
                                        oralQuestion.setAudio(voiceUrl);
                                        oralQuestions.add(oralQuestion);
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
                for (Map.Entry<String, String> entry : answers.entrySet()) {
                    String value = entry.getValue();
                    LiveCastHomeworkProcessResult liveCastHomeworkProcessResult = liveCastHomeworkProcessResultMap.get(value);
                    if (liveCastHomeworkProcessResult == null) continue;
                    NewQuestion newQuestion = questionsMap.get(entry.getKey());
                    if (newQuestion == null) continue;
                    List<NewQuestionsSubContents> subContents = newQuestion.getContent().getSubContents();
                    List<List<String>> standardAnswers = subContents
                            .stream()
                            .map(NewQuestionsSubContents::getAnswerList)
                            .collect(Collectors.toList());
                    if (Objects.equals(liveCastHomeworkProcessResult.getGrasp(), Boolean.TRUE)) {
                        rightNum++;
                    }
                    ReadingExercisesInfo.ExercisesQuestionInfo exercisesQuestionInfo = new ReadingExercisesInfo.ExercisesQuestionInfo();
                    exercisesQuestionInfo.setQuestionId(entry.getKey());
                    exercisesQuestionInfo.setUserAnswers(NewHomeworkUtils.pressAnswer(subContents, liveCastHomeworkProcessResult.getUserAnswers()));
                    exercisesQuestionInfo.setStandardAnswers(NewHomeworkUtils.pressAnswer(subContents, standardAnswers));
                    exercisesQuestionInfo.setDifficultyName(QuestionConstants.newDifficultyMap.get(newQuestion.getDifficultyInt()));
                    exercisesQuestionInfo.setQuestionType(contentTypeMap.get(newQuestion.getContentTypeId()) != null ? contentTypeMap.get(newQuestion.getContentTypeId()).getName() : "无题型");
                    readingExercisesInfo.getExercisesQuestionInfo().add(exercisesQuestionInfo);
                }
                readingExercisesInfo.setRightNum(rightNum);
                readingExercisesInfo.setTotalExercises(totalExercises);
            }
            mapMessage.add("exercisesInfo", readingExercisesInfo);
            mapMessage.add("oralQuestions", oralQuestions);
            mapMessage.add("readingName", pictureBookMap.get(readingId) != null ? pictureBookMap.get(readingId).getName() : "");
            mapMessage.add("userId", studentId);
            mapMessage.add("userName", u != null ? u.fetchRealname() : "");
            mapMessage.add("subject", liveCastHomework.getSubject());
            mapMessage.setSuccess(true);
        } catch (Exception e) {
            logger.error("get personal Reading Detail failed: hid {},sid {},readingId {}", homeworkId, studentId, readingId, e);
            return MapMessage.errorMessage(e.getMessage());
        }
        return mapMessage;
    }

    @Override
    public MapMessage personalReadingDetail(String homeworkId, Long studentId, String readingId, ObjectiveConfigType type) {
        MapMessage mapMessage = new MapMessage();
        LiveCastHomework newHomework = liveCastHomeworkDao.load(homeworkId);
        User u = studentLoaderClient.loadStudent(studentId);
        if (Objects.isNull(newHomework)) {
            return MapMessage.errorMessage("homework does not exist");
        }
        List<Long> useIds = thirdPartyGroupLoaderClient.loadGroupStudentIds(newHomework.getClazzGroupId());
        Map<Long, User> userMap = userLoaderClient.loadUsers(useIds);
        if (!userMap.containsKey(studentId)) {
            return MapMessage.errorMessage("该学生不在该作业的班级");
        }

        LiveCastHomework.Location location = newHomework.toLocation();
        String month = MonthRange.newInstance(location.getCreateTime()).toString();
        LiveCastHomeworkResult.ID id = new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), studentId);
        LiveCastHomeworkResult newHomeworkResult = liveCastHomeworkResultDao.load(id.toString());
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
        Map<String, LiveCastHomeworkProcessResult> newHomeworkProcessResultMap = liveCastHomeworkProcessResultDao.loads(newHomeworkProcessResultIds);
        Map<String, NewQuestion> questionsMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        List<Map<String, Object>> oralQuestions = new LinkedList<>();
        Map<String, Object> exercisesInfo = new LinkedHashMap<>();

        //是否包含口语题
        if (MapUtils.isNotEmpty(newHomeworkResultAppAnswer.getOralAnswers())) {
            LinkedHashMap<String, String> oralAnswers = newHomeworkResultAppAnswer.getOralAnswers();
            oralAnswers.forEach((key, value) -> {
                LiveCastHomeworkProcessResult newHomeworkProcessResult = newHomeworkProcessResultMap.get(value);
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
                LiveCastHomeworkProcessResult newHomeworkProcessResult = newHomeworkProcessResultMap.get(value);
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
        String dubbingId = null;
        AppOralScoreLevel dubbingScoreLevel = null;
        if (newHomeworkResultAppAnswer.getDubbingId() != null) {
            dubbingId = newHomeworkResultAppAnswer.getDubbingId();
            dubbingScoreLevel = newHomeworkResultAppAnswer.getDubbingScoreLevel();
        }

        mapMessage.add("dubbingId", dubbingId);
        mapMessage.add("dubbingScoreLevel", dubbingScoreLevel != null ? dubbingScoreLevel.getDesc() : null);
        mapMessage.add("exercisesInfo", exercisesInfo);
        mapMessage.add("oralQuestions", oralQuestions);
        mapMessage.add("readingName", readingName);
        mapMessage.add("userId", studentId);
        mapMessage.add("userName", u != null ? u.fetchRealname() : "");
        mapMessage.add("homeworkType", newHomework.getNewHomeworkType());
        mapMessage.add("subject", newHomework.getSubject());
        mapMessage.setSuccess(true);
        return mapMessage;
    }


    // 1、题循环组织数据
    // 优化备用升级接口
    private List<BasicAppQuestionInfo> newInternalProcessHomeworkAnswer(Map<Long, User> userMap, LiveCastHomeworkResult liveCastHomeworkResult, String categoryId, String lessonId, PracticeType practiceType, NewHomeworkApp newHomeworkApp, ObjectiveConfigType objectiveConfigType) {
        List<BasicAppQuestionInfo> questionInfo = new LinkedList<>();
        // 取出base_app类型,
        if (Objects.isNull(newHomeworkApp)
                || CollectionUtils.isEmpty(newHomeworkApp.getQuestions())) {
            return Collections.emptyList();
        }
        List<String> qIds = newHomeworkApp
                .getQuestions()
                .stream()
                .map(NewHomeworkQuestion::getQuestionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(qIds)) {
            return Collections.emptyList();
        }
        Map<String, LiveCastHomeworkProcessResult> processResultMap = liveCastHomeworkProcessResultDao.loads(liveCastHomeworkResult.findHomeworkProcessIdsForBaseAppByCategoryIdAndLessonId(categoryId, lessonId, objectiveConfigType));
        Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(qIds);
        List<Long> sentenceIds = newQuestionMap
                .values()
                .stream()
                .map(NewQuestion::getSentenceIds)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Map<Long, Sentence> sentenceMap = englishContentLoaderClient.loadEnglishSentences(sentenceIds);
        Map<String, BasicAppQuestionInfo> basicAppQuestionInfoMap = new LinkedHashMap<>();
        for (String qId : qIds) {
            NewQuestion newQuestion = newQuestionMap.get(qId);
            if (newQuestion == null) continue;
            BasicAppQuestionInfo basicAppQuestionInfo = new BasicAppQuestionInfo();
            basicAppQuestionInfo.setQuestionId(qId);
            List<Long> _sentenceIds = newQuestion.getSentenceIds();
            basicAppQuestionInfo.setNeedRecord(practiceType.fetchNeedRecord());
            List<BasicAppQuestionInfo.BasicAppSentence> sentences = CollectionUtils.isNotEmpty(_sentenceIds) ?
                    _sentenceIds
                            .stream()
                            .map(l -> {
                                BasicAppQuestionInfo.BasicAppSentence basicAppSentence = new BasicAppQuestionInfo.BasicAppSentence();
                                basicAppSentence.setSentenceId(l);
                                basicAppSentence.setSentenceContent(Objects.isNull(sentenceMap.get(l)) ? "" : sentenceMap.get(l).getEnText());
                                return basicAppSentence;
                            })
                            .collect(Collectors.toList()) :
                    Collections.emptyList();
            basicAppQuestionInfo.setSentences(sentences);
            basicAppQuestionInfoMap.put(qId, basicAppQuestionInfo);
        }
        for (Map.Entry<String, LiveCastHomeworkProcessResult> entry : processResultMap.entrySet()) {
            LiveCastHomeworkProcessResult liveCastHomeworkProcessResult = entry.getValue();
            if (liveCastHomeworkProcessResult != null && basicAppQuestionInfoMap.containsKey(liveCastHomeworkProcessResult.getQuestionId())) {
                BasicAppQuestionInfo basicAppQuestionInfo = basicAppQuestionInfoMap.get(liveCastHomeworkProcessResult.getQuestionId());
                basicAppQuestionInfo.setFlag(true);
                BasicAppQuestionInfo.BasicAppRecordInfo basicAppRecordInfo = new BasicAppQuestionInfo.BasicAppRecordInfo();
                Boolean answerInfo = null;
                String answerResultWord = null;
                if (practiceType.fetchNeedRecord()) {
                    int score = 0;
                    AppOralScoreLevel appOralScoreLevel = liveCastHomeworkProcessResult.getAppOralScoreLevel();
                    if (appOralScoreLevel == null) {
                        score = new BigDecimal(SafeConverter.toDouble(liveCastHomeworkProcessResult.getScore())).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                    }
                    if (CollectionUtils.isNotEmpty(liveCastHomeworkProcessResult.getOralDetails())) {
                        for (List<BaseHomeworkProcessResult.OralDetail> oralDetails : liveCastHomeworkProcessResult.getOralDetails()) {
                            if (CollectionUtils.isNotEmpty(oralDetails)) {
                                for (BaseHomeworkProcessResult.OralDetail oralDetail : oralDetails) {
                                    String voiceUrl = oralDetail.getAudio();
                                    VoiceEngineType voiceEngineType = liveCastHomeworkProcessResult.getVoiceEngineType();
                                    voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                                    basicAppRecordInfo.getUserVoiceUrl().add(voiceUrl);
                                }
                            }
                        }
                    }
                    basicAppRecordInfo.setScore(appOralScoreLevel == null ? (score + "分") : appOralScoreLevel.name());
                    basicAppRecordInfo.setUserId(liveCastHomeworkProcessResult.getUserId());
                    basicAppRecordInfo.setUserName(userMap.containsKey(liveCastHomeworkProcessResult.getUserId()) ? userMap.get(liveCastHomeworkProcessResult.getUserId()).fetchRealname() : "");
                    basicAppRecordInfo.setVoiceScoringMode(liveCastHomeworkProcessResult.getVoiceScoringMode());
                } else {
                    boolean grasp = SafeConverter.toBoolean(liveCastHomeworkProcessResult.getGrasp());
                    answerInfo = grasp;
                    if (grasp) {
                        answerResultWord = "我答对了";
                    } else {
                        if (SafeConverter.toDouble(liveCastHomeworkProcessResult.getScore()) > 0) {
                            answerResultWord = "部分正确";
                        } else {
                            answerResultWord = "我答错了";
                        }
                    }
                }
                basicAppQuestionInfo.setAnswerResultWord(answerResultWord);
                basicAppQuestionInfo.setGrasp(answerInfo);
                basicAppQuestionInfo.setNeedRecord(practiceType.getNeedRecord());
                basicAppQuestionInfo.setBasicAppRecordInfo(basicAppRecordInfo);
            }
        }
        for (String qId : qIds) {
            BasicAppQuestionInfo basicAppQuestionInfo = basicAppQuestionInfoMap.get(qId);
            if (basicAppQuestionInfo != null && basicAppQuestionInfo.isFlag()) {
                questionInfo.add(basicAppQuestionInfo);
            }
        }
        return questionInfo;
    }


    private List<BasicAppQuestionInfo> processNewHomeworkForBaseApp(LiveCastHomework liveCastHomework, String categoryId, String lessonId, PracticeType practiceType, List<String> questionIds, ObjectiveConfigType objectiveConfigType) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return Collections.emptyList();
        }
        List<Long> useIds = thirdPartyGroupLoaderClient.loadGroupStudentIds(liveCastHomework.getClazzGroupId());
        Map<Long, User> userMap = userLoaderClient.loadUsers(useIds);
        LiveCastHomework.Location location = liveCastHomework.toLocation();
        String month = MonthRange.newInstance(location.getCreateTime()).toString();
        List<String> liveCastHomeworkResultIds = userMap.values()
                .stream()
                .filter(Objects::nonNull)
                .map(o -> new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), o.getId()).toString())
                .collect(Collectors.toList());
        Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap = liveCastHomeworkResultDao.loads(liveCastHomeworkResultIds);
        // 2. categroyId-lessionId 类型的newHomeworkProcessResultIds
        List<String> newHomeworkProcessResultIds = liveCastHomeworkResultMap
                .values()
                .stream()
                .filter(o -> MapUtils.isNotEmpty(o.getPractices()))
                .map(o -> o.findHomeworkProcessIdsForBaseAppByCategoryIdAndLessonId(categoryId, lessonId, objectiveConfigType))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(newHomeworkProcessResultIds))
            return Collections.emptyList();
        Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap = liveCastHomeworkProcessResultDao.loads(newHomeworkProcessResultIds);
        List<LiveCastHomeworkProcessResult> liveCastHomeworkProcessResults = new ArrayList<>(liveCastHomeworkProcessResultMap.values());
        Map<String, List<LiveCastHomeworkProcessResult>> dataInfo = new LinkedHashMap<>();
        //Map qIds to NewHomeworkProcessResult
        liveCastHomeworkProcessResults = liveCastHomeworkProcessResults
                .stream()
                .filter(o -> userMap
                        .containsKey(o.getUserId()))
                .collect(Collectors.toList());
        for (LiveCastHomeworkProcessResult n : liveCastHomeworkProcessResults) {
            dataInfo.computeIfAbsent(n.getQuestionId(), k -> new LinkedList<>()).add(n);
        }
        return internalProcessHomeworkAnswerDetailForBaseApp(questionIds, dataInfo, userMap, practiceType);
    }


    private List<BasicAppQuestionInfo> internalProcessHomeworkAnswerDetailForBaseApp(List<String> questionIds, Map<String, List<LiveCastHomeworkProcessResult>> qIdMapNewHomeworkProcessResult, Map<Long, User> userMap, PracticeType practiceType) {
        List<BasicAppQuestionInfo> questionInfo = new LinkedList<>();
        Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        List<Long> sentenceIds = newQuestionMap
                .values()
                .stream()
                .filter(o -> CollectionUtils.isNotEmpty(o.getSentenceIds()))
                .map(NewQuestion::getSentenceIds)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Map<Long, Sentence> sentenceMap = englishContentLoaderClient.loadEnglishSentences(sentenceIds);
        for (String qId : newQuestionMap.keySet()) {
            BasicAppQuestionInfo basicAppQuestionInfo = new BasicAppQuestionInfo();
            questionInfo.add(basicAppQuestionInfo);
            List<String> answerErrorInfo = basicAppQuestionInfo.getAnswerErrorInfo();
            List<String> answerRightInfo = basicAppQuestionInfo.getAnswerRightInfo();
            List<BasicAppQuestionInfo.BasicAppRecordInfo> recordInfo = basicAppQuestionInfo.getRecordInfo();
            int answerNum = 0;
            if (MapUtils.isNotEmpty(qIdMapNewHomeworkProcessResult)) {
                //是否是口语题
                if (practiceType.fetchNeedRecord()) {
                    if (CollectionUtils.isNotEmpty(qIdMapNewHomeworkProcessResult.get(qId))) {
                        List<LiveCastHomeworkProcessResult> ls = qIdMapNewHomeworkProcessResult.get(qId);
                        for (LiveCastHomeworkProcessResult n : ls) {
                            int score = 0;
                            AppOralScoreLevel appOralScoreLevel = n.getAppOralScoreLevel();
                            if (Objects.nonNull(n.getScore())) {
                                score = new BigDecimal(n.getScore())
                                        .setScale(0, BigDecimal.ROUND_HALF_UP)
                                        .intValue();
                            }
                            BasicAppQuestionInfo.BasicAppRecordInfo basicAppRecordInfo = new BasicAppQuestionInfo.BasicAppRecordInfo();
                            if (CollectionUtils.isNotEmpty(n.getOralDetails())) {
                                for (List<BaseHomeworkProcessResult.OralDetail> oralDetails : n.getOralDetails()) {
                                    if (CollectionUtils.isNotEmpty(oralDetails)) {
                                        for (BaseHomeworkProcessResult.OralDetail oralDetail : oralDetails) {
                                            String voiceUrl = oralDetail.getAudio();
                                            VoiceEngineType voiceEngineType = n.getVoiceEngineType();
                                            voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                                            basicAppRecordInfo.getUserVoiceUrl().add(voiceUrl);
                                        }
                                    }
                                }
                            }
                            basicAppRecordInfo.setScore(appOralScoreLevel == null ? (score + "分") : appOralScoreLevel.name());
                            basicAppRecordInfo.setRealScore(score);
                            basicAppRecordInfo.setUserId(n.getUserId());
                            basicAppRecordInfo.setUserName(userMap.containsKey(n.getUserId()) ? userMap.get(n.getUserId()).fetchRealname() : "");
                            basicAppRecordInfo.setVoiceScoringMode(n.getVoiceScoringMode());
                            recordInfo.add(basicAppRecordInfo);
                        }
                    }
                } else {
                    if (CollectionUtils.isNotEmpty(qIdMapNewHomeworkProcessResult.get(qId))) {
                        List<LiveCastHomeworkProcessResult> ls = qIdMapNewHomeworkProcessResult.get(qId);
                        for (LiveCastHomeworkProcessResult n : ls) {
                            answerNum++;
                            if (SafeConverter.toBoolean(n.getGrasp())) {
                                answerRightInfo.add(userMap.get(n.getUserId()).fetchRealname());
                            } else {
                                answerErrorInfo.add(userMap.get(n.getUserId()).fetchRealname());
                            }
                        }
                    }
                }
            }
            NewQuestion newQuestion = newQuestionMap.get(qId);
            List<Long> _sentenceIds = newQuestion.getSentenceIds();
            List<BasicAppQuestionInfo.BasicAppSentence> sentences = CollectionUtils.isNotEmpty(_sentenceIds) ?
                    _sentenceIds
                            .stream()
                            .map(l -> {
                                BasicAppQuestionInfo.BasicAppSentence basicAppSentence = new BasicAppQuestionInfo.BasicAppSentence();
                                basicAppSentence.setSentenceId(l);
                                basicAppSentence.setSentenceContent(Objects.isNull(sentenceMap.get(l)) ? "" : sentenceMap.get(l).getEnText());
                                return basicAppSentence;
                            })
                            .collect(Collectors.toList()) :
                    Collections.EMPTY_LIST;
            int errorProportion = 0;
            int rightProportion = 0;
            if (answerNum > 0) {
                errorProportion = new BigDecimal(answerErrorInfo.size())
                        .divide(new BigDecimal(answerNum), 2, BigDecimal.ROUND_UP)
                        .multiply(new BigDecimal(100))
                        .intValue();
                rightProportion = 100 - errorProportion;
            }
            recordInfo.sort((o1, o2) -> Integer.compare(o2.getRealScore(), o1.getRealScore()));
            basicAppQuestionInfo.setQuestionId(qId);
            basicAppQuestionInfo.setSentences(sentences);
            basicAppQuestionInfo.setAnswerErrorInfo(answerErrorInfo);
            basicAppQuestionInfo.setAnswerRightInfo(answerRightInfo);
            basicAppQuestionInfo.setNeedRecord(practiceType.fetchNeedRecord());
            basicAppQuestionInfo.setRightProportion(rightProportion);
            basicAppQuestionInfo.setErrorProportion(errorProportion);
        }
        return questionInfo;
    }

    //1、初始化数据
    //2、根据模板调用构建数据
    private Map<ObjectiveConfigType, Object> processNewHomework(User user, LiveCastHomework liveCastHomework, LiveCastHomeworkResult liveCastHomeworkResult) {
        // 处理学生的做题结果，最里面的map为NewHomeworkProcessResult的简版
        Map<ObjectiveConfigType, NewHomeworkPracticeContent> map = liveCastHomework.findPracticeContents();
        List<String> allQuestionIds = liveCastHomework.findAllQuestionIds();
        Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(allQuestionIds);
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        List<String> newHomeworkProcessIds = liveCastHomeworkResult.findAllHomeworkProcessIds(true);
        Map<String, LiveCastHomeworkProcessResult> allProcessResultMap = liveCastHomeworkProcessResultDao.loads(newHomeworkProcessIds);
        LiveCastReportRateContext reportRateContext = new LiveCastReportRateContext(user,
                liveCastHomeworkResult,
                liveCastHomework,
                allQuestionMap,
                allProcessResultMap,
                contentTypeMap);
        for (Map.Entry<ObjectiveConfigType, NewHomeworkPracticeContent> entry : map.entrySet()) {
            ObjectiveConfigType key = entry.getKey();
            reportRateContext.setType(key);
            if (!liveCastHomeworkResult.isFinishedOfObjectiveConfigType(key))
                continue;
            ProcessLiveCastHomeworkAnswerDetailTemplate template = processLiveCastHomeworkAnswerDetailFactory.getTemplate(key);
            if (template != null) {
                template.processNewHomeworkAnswerDetailPersonal(reportRateContext);
            }
        }
        return reportRateContext.getResultMap();
    }

    //1、初始化数据：userMap、liveCastHomeworkResultMap、allNewQuestionMap、liveCastHomeworkProcessResultMap、contentTypeMap
    //2、根据模板调用构建数据
    private Map<String, Object> processNewHomework(LiveCastHomework liveCastHomework) {
        List<Long> useIds = thirdPartyGroupLoaderClient.loadGroupStudentIds(liveCastHomework.getClazzGroupId());
        Map<Long, User> userMap = userLoaderClient.loadUsers(useIds);
        LiveCastHomework.Location location = liveCastHomework.toLocation();
        String month = MonthRange.newInstance(location.getCreateTime()).toString();
        List<String> liveCastHomeworkResultIds = userMap.keySet()
                .stream()
                .filter(Objects::nonNull)
                .map(o -> new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), o).toString())
                .collect(Collectors.toList());
        Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap = liveCastHomeworkResultDao.loads(liveCastHomeworkResultIds);
        if (liveCastHomeworkResultMap.isEmpty()) return Collections.emptyMap();
        // 取出此次作业中，每种作业形式的题目id
        List<ObjectiveConfigType> types = liveCastHomework
                .getPractices()
                .stream()
                .filter(Objects::nonNull)
                .map(NewHomeworkPracticeContent::getType)
                .collect(Collectors.toList());
        List<String> questionIds = liveCastHomework.findAllQuestionIds();
        Map<String, NewQuestion> allNewQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        // 2. 遍历每种作业形式下的题目
        List<String> liveCastHomeworkProcessResultIds = liveCastHomeworkResultMap
                .values()
                .stream()
                .map(o -> o.findAllHomeworkProcessIds(true))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap = liveCastHomeworkProcessResultDao.loads(liveCastHomeworkProcessResultIds);
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        // 遍历每种作业形式下的报告
        LiveCastReportRateContext reportRateContext = new LiveCastReportRateContext(
                allNewQuestionMap,
                liveCastHomeworkProcessResultMap,
                liveCastHomeworkResultMap,
                userMap,
                liveCastHomework,
                contentTypeMap);
        for (ObjectiveConfigType type : types) {
            reportRateContext.setType(type);
            ProcessLiveCastHomeworkAnswerDetailTemplate template = processLiveCastHomeworkAnswerDetailFactory.getTemplate(type);
            if (template != null) {
                template.processNewHomeworkAnswerDetail(reportRateContext);
            }
        }
        return reportRateContext.getResult();
    }

    @Override
    public MapMessage loadHomeworkQuestionsAnswer(String objectiveConfigTypeStr, String homeworkId, Long studentId, Integer categoryId, String lessonId, String videoId) {
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeStr);
        return MapMessage.successMessage().add("result", liveCastGenerateDataService.loadHomeworkQuestionsAnswer(objectiveConfigType, homeworkId, studentId, categoryId, lessonId, videoId));
    }
}
