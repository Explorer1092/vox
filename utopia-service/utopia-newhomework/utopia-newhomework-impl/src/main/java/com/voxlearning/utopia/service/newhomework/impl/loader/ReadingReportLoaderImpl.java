package com.voxlearning.utopia.service.newhomework.impl.loader;


import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.athena.api.PictureBookReportService;
import com.voxlearning.athena.bean.PictureBookReportDetail;
import com.voxlearning.athena.bean.PictureBookReportHomeworkDetail;
import com.voxlearning.athena.bean.PictureBookReportStudentDetail;
import com.voxlearning.athena.bean.PictureBookReportWeekCount;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.core.helper.VoiceEngineTypeUtils;
import com.voxlearning.utopia.service.newhomework.api.ReadingReportLoader;
import com.voxlearning.utopia.service.newhomework.api.constant.*;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.ReadingDubbingRecommend;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.reading.AbilityAnalysis;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.reading.PictureInfo;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.reading.PictureSemesterReport;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend.ReadingDubbingRecommendDao;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.consumer.PictureBookLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = ReadingReportLoader.class)
@ExposeService(interfaceClass = ReadingReportLoader.class)
public class ReadingReportLoaderImpl extends SpringContainerSupport implements ReadingReportLoader {

    @Inject private RaikouSDK raikouSDK;

    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private PictureBookLoaderClient pictureBookLoaderClient;
    @Inject private QuestionLoaderClient questionLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;
    @Inject private ReadingDubbingRecommendDao readingDubbingRecommendDao;
    @Inject private DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;

    //大数据接口
    @ImportService(interfaceClass = PictureBookReportService.class)
    private PictureBookReportService pictureBookReportService;

    private static Map<Integer, Integer> clazzLevelWordNumMap = new LinkedHashMap<>();

    //大纲推荐周阅读量
    static {
        clazzLevelWordNumMap.put(1, 27);
        clazzLevelWordNumMap.put(2, 33);
        clazzLevelWordNumMap.put(3, 100);
        clazzLevelWordNumMap.put(4, 233);
        clazzLevelWordNumMap.put(5, 367);
        clazzLevelWordNumMap.put(6, 567);
    }

    @Override
    public MapMessage fetchPictureSemesterReport(Long gid) {
        Group group = raikouSDK.getClazzClient().getGroupLoaderClient()
                ._loadGroup(gid).firstOrNull();
        if (group == null) {
            return MapMessage.errorMessage("班组不存在");
        }
        Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient()
                ._loadClazz(group.getClazzId()).firstOrNull();
        if (clazz == null) {
            return MapMessage.errorMessage("班级不存在");
        }

        try {
            PictureBookReportDetail pictureBookReportDetail = pictureBookReportService.loadClazzReadCount(gid);
            if (pictureBookReportDetail == null) {
                return MapMessage.successMessage().add("pictureSemesterReport", null);
            } else {
                PictureSemesterReport pictureSemesterReport = new PictureSemesterReport();
                ClazzLevel clazzLevel = clazz.getClazzLevel();
                //大纲推荐周阅读量
                Integer standardNum = clazzLevelWordNumMap.getOrDefault(clazzLevel.getLevel(), 27);
                pictureSemesterReport.setStandardNum(standardNum);
                pictureSemesterReport.setReadingLevelList(ReadingLevelType.detailInfo);
                pictureSemesterReport.setClazzLevel(SafeConverter.toInt(pictureBookReportDetail.getClazzLevel()));

                //自然周信息
                if (CollectionUtils.isNotEmpty(pictureBookReportDetail.getWeekCountList())) {
                    for (PictureBookReportWeekCount weekCount : pictureBookReportDetail.getWeekCountList()) {
                        if (StringUtils.isBlank(weekCount.getBeginAt()))
                            continue;
                        Date beginAt = DateUtils.stringToDate(weekCount.getBeginAt(), "yyyy-MM-dd");
                        if (beginAt == null)
                            continue;
                        if (StringUtils.isBlank(weekCount.getEndAt()))
                            continue;
                        Date endAt = DateUtils.stringToDate(weekCount.getEndAt(), "yyyy-MM-dd");
                        if (endAt == null)
                            continue;
                        PictureSemesterReport.WeekPictureReport weekPictureReport = new PictureSemesterReport.WeekPictureReport();
                        weekPictureReport.setBeginAt(beginAt);
                        weekPictureReport.setEndAt(endAt);
                        weekPictureReport.setTime(DateUtils.dateToString(beginAt, "MM/dd") + "-" + DateUtils.dateToString(endAt, "MM/dd"));
                        weekPictureReport.setReadCnt(SafeConverter.toInt(weekCount.getReadCount()));
                        pictureSemesterReport.getWeekPictureReports().add(weekPictureReport);
                    }
                    pictureSemesterReport.getWeekPictureReports()
                            .sort((o1, o2) -> Long.compare(o1.getBeginAt().getTime(), o2.getEndAt().getTime()));
                }
                //布置情况
                if (CollectionUtils.isNotEmpty(pictureBookReportDetail.getHomeworkList())) {
                    //按照阅读量排序:一份作业没人做的时候，新增量竟然是0，需求规则
                    List<PictureBookReportHomeworkDetail> pictureBookReportHomeworkDetails = pictureBookReportDetail.getHomeworkList()
                            .stream()
                            .filter(p -> p.getBeginAt() != null)
                            .sorted((o1, o2) -> {
                                int compare = Integer.compare(SafeConverter.toInt(o2.getCumulativeNum()), SafeConverter.toInt(o1.getCumulativeNum()));
                                if (compare == 0) {
                                    Date beginAt1 = DateUtils.stringToDate(o1.getBeginAt(), "yyyy-MM-dd");
                                    Date beginAt2 = DateUtils.stringToDate(o2.getBeginAt(), "yyyy-MM-dd");
                                    if (beginAt1 != null && beginAt2 != null) {
                                        compare = Long.compare(beginAt2.getTime(), beginAt1.getTime());
                                    }
                                }
                                return compare;
                            }).collect(Collectors.toList());
                    //按月显示
                    Map<String, List<PictureSemesterReport.PeerHomeworkReport>> peerHomeworkReportListMap = new LinkedHashMap<>();
                    for (PictureBookReportHomeworkDetail homeworkDetail : pictureBookReportHomeworkDetails) {
                        Date beginAt = DateUtils.stringToDate(homeworkDetail.getBeginAt(), "yyyy-MM-dd");
                        if (beginAt == null)
                            continue;
                        PictureSemesterReport.PeerHomeworkReport peerHomeworkReport = new PictureSemesterReport.PeerHomeworkReport();
                        peerHomeworkReport.setHid(homeworkDetail.getHomeworkId());
                        peerHomeworkReport.setBeginAt(beginAt);
                        peerHomeworkReport.setBeginAtStr(DateUtils.dateToString(beginAt, "MM-dd"));
                        peerHomeworkReport.setCumulativeCnt(SafeConverter.toInt(homeworkDetail.getCumulativeNum()));
                        peerHomeworkReport.setAdditionCnt(SafeConverter.toInt(homeworkDetail.getAdditionCnt()));
                        peerHomeworkReport.setAvgDuration(new BigDecimal(SafeConverter.toInt(homeworkDetail.getAvgDuration())).divide(new BigDecimal(60 * 1000), BigDecimal.ROUND_UP).intValue());
                        peerHomeworkReport.setAvgScore(SafeConverter.toInt(homeworkDetail.getAvgScore()));
                        peerHomeworkReportListMap.computeIfAbsent(DateUtils.dateToString(beginAt, "MM"), o -> new LinkedList<>())
                                .add(peerHomeworkReport);
                    }
                    //需求根据每个月显示
                    for (List<PictureSemesterReport.PeerHomeworkReport> peerHomeworkReportList : peerHomeworkReportListMap.values()) {
                        pictureSemesterReport.getPeerHomeworkReports().add(peerHomeworkReportList);
                    }
                }
                return MapMessage.successMessage().add("pictureSemesterReport", pictureSemesterReport);
            }

        } catch (Exception e) {
            logger.error("fetch Picture Semester Report failed : gid of {}", gid, e);
            return MapMessage.successMessage("获取数据失败");
        }
    }

    @Override
    public MapMessage fetchPictureSemesterReportFromBigData(Long gid) {
        PictureBookReportDetail pictureBookReportDetail = pictureBookReportService.loadClazzReadCount(gid);
        return MapMessage.successMessage().add("pictureBookReportDetail", pictureBookReportDetail);
    }

    @Override
    public MapMessage fetchAbilityAnalysis(Long gid) {
        try {
            PictureBookReportDetail pictureBookReportDetail = pictureBookReportService.loadStudentDetail(gid);
            if (pictureBookReportDetail == null) {
                return MapMessage.successMessage().add("abilityAnalysis", null).add("subject", Subject.ENGLISH)
                        .add("subjectName", Subject.ENGLISH.getValue());
            } else {
                AbilityAnalysis abilityAnalysis = new AbilityAnalysis();
                abilityAnalysis.setReadingLevelList(ReadingLevelType.detailInfo);
                abilityAnalysis.setClazzLevel(SafeConverter.toInt(pictureBookReportDetail.getClazzLevel()));
                AbilityAnalysis.ClazzPart clazzPart = abilityAnalysis.getClazzPart();
                int level = (abilityAnalysis.getClazzLevel() + 1) / 2;
                //解码能力
                if (level != 0 && DecodingAbilityLevel.decodingAbilityLevelMap.containsKey(level)) {
                    DecodingAbilityLevel decodingAbilityLevel = DecodingAbilityLevel.decodingAbilityLevelMap.get(level);
                    AbilityAnalysis.DecodingAbilityLevelModule decodingAbilityLevelModule = clazzPart.getDecodingAbilityLevel();
                    decodingAbilityLevelModule.setLevel(level);
                    decodingAbilityLevelModule.setDesc(decodingAbilityLevel.getDesc());
                    decodingAbilityLevelModule.setDetail(decodingAbilityLevel.getDetail());
                }
                //语言知识
                if (level != 0 && PhoneticKnowledgeLevel.phoneticKnowledgeLevelMap.containsKey(level)) {
                    PhoneticKnowledgeLevel phoneticKnowledgeLevel = PhoneticKnowledgeLevel.phoneticKnowledgeLevelMap.get(level);
                    AbilityAnalysis.PhoneticKnowledgeLevelModule phoneticKnowledgeLevelModule = clazzPart.getPhoneticKnowledgeLevel();
                    phoneticKnowledgeLevelModule.setLevel(level);
                    phoneticKnowledgeLevelModule.setDesc(phoneticKnowledgeLevel.getDesc());
                    phoneticKnowledgeLevelModule.setDetail(phoneticKnowledgeLevel.getDetail());
                }
                //阅读理解
                if (level != 0 && ReadingComprehensionLevel.readingComprehensionLevelMap.containsKey(level)) {
                    ReadingComprehensionLevel readingComprehensionLevel = ReadingComprehensionLevel.readingComprehensionLevelMap.get(level);
                    AbilityAnalysis.ReadingComprehensionLevelModule readingComprehensionLevelModule = clazzPart.getReadingComprehensionLevel();
                    readingComprehensionLevelModule.setLevel(level);
                    readingComprehensionLevelModule.setDesc(readingComprehensionLevel.getDesc());
                    readingComprehensionLevelModule.setDetail(readingComprehensionLevel.getDetail());
                }
                //文化意识
                if (level != 0 && CulturalConsciousnessLevel.culturalConsciousnessLevelMap.containsKey(level)) {
                    CulturalConsciousnessLevel culturalConsciousnessLevel = CulturalConsciousnessLevel.culturalConsciousnessLevelMap.get(level);
                    AbilityAnalysis.CulturalConsciousnessLevelModule culturalConsciousnessLevelModule = clazzPart.getCulturalConsciousnessLevel();
                    culturalConsciousnessLevelModule.setLevel(level);
                    culturalConsciousnessLevelModule.setDesc(culturalConsciousnessLevel.getDesc());
                    culturalConsciousnessLevelModule.setDetail(culturalConsciousnessLevel.getDetail());
                }
                //阅读习惯
                if (level != 0 && ReadingHabitsLevel.readingHabitsLevelMap.containsKey(level)) {
                    ReadingHabitsLevel readingHabitsLevel = ReadingHabitsLevel.readingHabitsLevelMap.get(level);
                    AbilityAnalysis.ReadingHabitsLevelModule readingHabitsLevelModule = clazzPart.getReadingHabitsLevel();
                    readingHabitsLevelModule.setLevel(level);
                    readingHabitsLevelModule.setDesc(readingHabitsLevel.getDesc());
                    readingHabitsLevelModule.setDetail(readingHabitsLevel.getDetail());
                }

                List<User> users = studentLoaderClient.loadGroupStudents(gid);

                Map<Long, PictureBookReportStudentDetail> studentDetailMap = new LinkedHashMap<>();
                if (CollectionUtils.isNotEmpty(pictureBookReportDetail.getStudentDetailList())) {
                    for (PictureBookReportStudentDetail studentDetail : pictureBookReportDetail.getStudentDetailList()) {
                        studentDetailMap.put(studentDetail.getStudentId(), studentDetail);
                    }
                }
                //过滤学生、学生补充
                for (User user : users) {
                    AbilityAnalysis.StudentPartRecord studentPartRecord = new AbilityAnalysis.StudentPartRecord();
                    studentPartRecord.setUserId(user.getId());
                    studentPartRecord.setUserName(user.fetchRealnameIfBlankId());
                    if (studentDetailMap.containsKey(user.getId())) {
                        PictureBookReportStudentDetail detail = studentDetailMap.get(user.getId());
                        studentPartRecord.setCumulativeVocabularyCnt(SafeConverter.toInt(detail.getCumulativeVocabularyCnt()));
                        studentPartRecord.setReadingFrequency(SafeConverter.toInt(detail.getReadingFrequency()));
                        studentPartRecord.setCumulativeDuration(new BigDecimal(SafeConverter.toInt(detail.getCumulativeDuration())).divide(new BigDecimal(1000), BigDecimal.ROUND_UP).intValue());
                        studentPartRecord.setCumulativeBookCnt(SafeConverter.toInt(detail.getCumulativeBookCnt()));
                        studentPartRecord.setCumulativeDurationStr(NewHomeworkUtils.handlerTime(studentPartRecord.getCumulativeDuration()));
                        studentPartRecord.setAvgScore(SafeConverter.toInt(detail.getAvgScore()));
                    }
                    abilityAnalysis.getStudentPartRecordList().add(studentPartRecord);
                }

                abilityAnalysis.getStudentPartRecordList().sort((o1, o2) -> {
                    //排序
                    //绘本阅读量==累计词汇量==绘本平均得分==阅读频率
                    int compare = Integer.compare(o2.getCumulativeBookCnt(), o1.getCumulativeBookCnt());
                    if (compare == 0) {
                        compare = Integer.compare(o2.getCumulativeVocabularyCnt(), o1.getCumulativeVocabularyCnt());
                        if (compare == 0) {
                            compare = Integer.compare(o2.getAvgScore(), o1.getAvgScore());
                            if (compare == 0) {
                                compare = Integer.compare(o2.getReadingFrequency(), o1.getReadingFrequency());
                            }
                        }
                    }
                    return compare;
                });
                return MapMessage.successMessage().add("abilityAnalysis", abilityAnalysis).add("subject", Subject.ENGLISH)
                        .add("subjectName", Subject.ENGLISH.getValue());
            }
        } catch (Exception e) {
            logger.error("fetchAbilityAnalysis failed : gid {}", gid, e);
            return MapMessage.errorMessage("获取接口失败");
        }
    }

    @Override
    public MapMessage fetchAbilityAnalysisFromBigData(Long gid) {
        PictureBookReportDetail pictureBookReportDetail = pictureBookReportService.loadStudentDetail(gid);
        return MapMessage.successMessage().add("pictureBookReportDetail", pictureBookReportDetail);
    }


    @Override
    public MapMessage fetchPictureInfo(Teacher teacher, String hid) {
        if (StringUtils.isBlank(hid)) {
            return MapMessage.errorMessage("作业ID参数缺失");
        }
        NewHomework newHomework = newHomeworkLoader.load(hid);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.LEVEL_READINGS);
        if (target == null) {
            return MapMessage.errorMessage("作业不包含绘练习本");
        }
        List<String> qids = target.processNewHomeworkQuestion(true)
                .stream()
                .map(NewHomeworkQuestion::getQuestionId)
                .collect(Collectors.toList());
        Map<String, NewQuestion> allNewQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(qids);
        List<String> pictureBookIds = target
                .getApps()
                .stream()
                .map(NewHomeworkApp::getPictureBookId)
                .collect(Collectors.toList());
        Map<String, PictureBookPlus> pictureBookPlusMap = pictureBookPlusServiceClient.loadByIds(pictureBookIds);
        List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries();
        Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList
                .stream()
                .collect(Collectors
                        .toMap(PictureBookSeries::getId, Function.identity()));
        List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics();
        Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList
                .stream()
                .collect(Collectors.
                        toMap(PictureBookTopic::getId, Function.identity()));
        Map<String, PictureInfo> pictureInfoMap = new LinkedHashMap<>();
        List<String> recommendIds = new LinkedList<>();
        for (NewHomeworkApp app : target.getApps()) {
            if (!pictureBookPlusMap.containsKey(app.getPictureBookId()))
                continue;
            //推荐
            if (app.containsDubbing()) {
                ReadingDubbingRecommend.ID id = new ReadingDubbingRecommend.ID(hid, ObjectiveConfigType.LEVEL_READINGS, app.getPictureBookId());
                recommendIds.add(id.toString());
            }
            PictureBookPlus pictureBookPlus = pictureBookPlusMap.get(app.getPictureBookId());
            Map<String, Object> map = NewHomeworkContentDecorator.decoratePictureBookPlus(pictureBookPlus, pictureBookSeriesMap, pictureBookTopicMap, null, null, null, null);
            PictureInfo pictureInfo = new PictureInfo();
            pictureInfoMap.put(app.getPictureBookId(), pictureInfo);
            pictureInfo.getPicturePart().setPictureMap(map);
            pictureInfo.setContainsDubbing(app.containsDubbing());
            pictureInfo.setPictureId(app.getPictureBookId());
            pictureInfo.setPictureName(pictureBookPlus.getEname());
            //口语题 ： 单题，单录音
            if (CollectionUtils.isNotEmpty(app.getOralQuestions())) {
                for (NewHomeworkQuestion newHomeworkQuestion : app.getOralQuestions()) {
                    if (!allNewQuestionMap.containsKey(newHomeworkQuestion.getQuestionId()))
                        continue;
                    NewQuestion newQuestion = allNewQuestionMap.get(newHomeworkQuestion.getQuestionId());
                    if (newQuestion.getContent() == null)
                        continue;
                    if (CollectionUtils.isEmpty(newQuestion.getContent().getSubContents()))
                        continue;
                    NewQuestionsSubContents newQuestionsSubContents = newQuestion.getContent().getSubContents().get(0);
                    if (newQuestionsSubContents.getOralDict() == null)
                        continue;
                    if (CollectionUtils.isEmpty(newQuestionsSubContents.getOralDict().getOptions()))
                        continue;
                    NewQuestionOralDictOptions newQuestionOralDictOptions = newQuestionsSubContents.getOralDict().getOptions().get(0);
                    if (newQuestionOralDictOptions.getText() == null)
                        continue;
                    PictureInfo.SentenceDetail sentenceDetail = new PictureInfo.SentenceDetail();
                    sentenceDetail.setQid(newHomeworkQuestion.getQuestionId());
                    sentenceDetail.setText(newQuestionOralDictOptions.getText());
                    pictureInfo.getQuestionDetailPart().getSentenceDetails().add(sentenceDetail);
                }
            }
            //简答题
            if (CollectionUtils.isNotEmpty(app.getQuestions())) {
                for (NewHomeworkQuestion newHomeworkQuestion : app.getQuestions()) {
                    if (!allNewQuestionMap.containsKey(newHomeworkQuestion.getQuestionId()))
                        continue;
                    NewQuestion newQuestion = allNewQuestionMap.get(newHomeworkQuestion.getQuestionId());
                    PictureInfo.QuestionDetail questionDetail = new PictureInfo.QuestionDetail();
                    questionDetail.setQid(newQuestion.getId());
                    List<Integer> subContentTypeIds = newQuestion.getContent()
                            .getSubContents()
                            .stream()
                            .map(NewQuestionsSubContents::getSubContentTypeId)
                            .collect(Collectors.toList());
                    //showRate 字段因为需求变动，导致名字没取好，避免前端麻烦就不换名字了
                    questionDetail.setShowRate(subContentTypeIds.contains(QuestionConstants.XuanZe_DanXuan) || subContentTypeIds.contains(QuestionConstants.PanDuanTi));
                    pictureInfo.getQuestionDetailPart().getQuestionDetails().add(questionDetail);
                }
            }
        }
        Map<String, ReadingDubbingRecommend> recommendMap = readingDubbingRecommendDao.loads(recommendIds)
                .values()
                .stream()
                .collect(Collectors.toMap(ReadingDubbingRecommend::getPictureId, Function.identity()));
        Map<Long, User> userMap = studentLoaderClient
                .loadGroupStudents(newHomework.getClazzGroupId())
                .stream()
                .collect(Collectors
                        .toMap(LongIdEntity::getId, Function.identity()));
        Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), false)
                .values()
                .stream()
                .filter(BaseHomeworkResult::isFinished)
                .collect(Collectors.toMap(BaseHomeworkResult::getUserId, Function.identity()));

        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        List<String> subHomeworkResultAnswerIds = new LinkedList<>();
        Map<ObjectiveConfigType, List<NewHomework.NewHomeworkQuestionObj>> objectiveConfigTypeListMap = newHomework.processSubHomeworkResultAnswerIds(Collections.singleton(ObjectiveConfigType.LEVEL_READINGS));
        List<NewHomework.NewHomeworkQuestionObj> newHomeworkQuestionObjs = objectiveConfigTypeListMap.getOrDefault(ObjectiveConfigType.LEVEL_READINGS, Collections.emptyList());
        Map<String, NewHomework.NewHomeworkQuestionObj> objMap = newHomeworkQuestionObjs.stream()
                .collect(Collectors.toMap(NewHomework.NewHomeworkQuestionObj::getQuestionId, Function.identity()));
        for (NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj : newHomeworkQuestionObjs) {
            for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
                subHomeworkResultAnswerIds.add(newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId()));
            }
        }
        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);
        List<String> newHomeworkProcessResultIds = subHomeworkResultAnswerMap.values()
                .stream()
                .map(SubHomeworkResultAnswer::getProcessId)
                .collect(Collectors.toList());
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(hid, newHomeworkProcessResultIds);
        for (User user : userMap.values()) {
            if (newHomeworkResultMap.containsKey(user.getId())) {
                NewHomeworkResult newHomeworkResult = newHomeworkResultMap.get(user.getId());
                NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.LEVEL_READINGS);
                LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = newHomeworkResultAnswer.getAppAnswers();
                for (PictureInfo pictureInfo : pictureInfoMap.values()) {
                    if (!appAnswers.containsKey(pictureInfo.getPictureId()))
                        continue;
                    NewHomeworkResultAppAnswer appAnswer = appAnswers.get(pictureInfo.getPictureId());
                    //未推荐的时候，全部学生进入推荐信息
                    if ((!recommendMap.containsKey(pictureInfo.getPictureId())) && appAnswer.getDubbingId() != null && appAnswer.getDubbingScoreLevel() != null) {
                        ReadingDubbingRecommend.ReadingDubbing recommendPart = new ReadingDubbingRecommend.ReadingDubbing();
                        recommendPart.setUserId(user.getId());
                        recommendPart.setDubbingId(appAnswer.getDubbingId());
                        recommendPart.setUserName(user.fetchRealnameIfBlankId());
                        recommendPart.setDubbingScoreLevel(appAnswer.getDubbingScoreLevel());
                        recommendPart.setScore((int) appAnswer.getDubbingScoreLevel().getScore());
                        if (MapUtils.isNotEmpty(appAnswer.getDurations()) && appAnswer.getDurations().containsKey("DUBBING")) {
                            recommendPart.setDuration(SafeConverter.toLong(appAnswer.getDurations().get("DUBBING")));
                        }
                        pictureInfo.getRecommendParts().add(recommendPart);
                    }
                    PictureInfo.StudentRecord studentRecord = new PictureInfo.StudentRecord();
                    studentRecord.setUserId(user.getId());
                    studentRecord.setUserName(user.fetchRealnameIfBlankId());
                    studentRecord.setFinished(true);
                    studentRecord.setScore((int) SafeConverter.toDouble(appAnswer.getScore()));
                    pictureInfo.getPicturePart().setFinishedNum(1 + pictureInfo.getPicturePart().getFinishedNum());
                    pictureInfo.getPicturePart().setTotalScore(studentRecord.getScore() + pictureInfo.getPicturePart().getTotalScore());
                    long l = new BigDecimal(SafeConverter.toLong(appAnswer.processDuration())).divide(new BigDecimal(1000), BigDecimal.ROUND_UP).longValue();
                    studentRecord.setDuration(l);
                    studentRecord.setDurationStr(NewHomeworkUtils.handlerTime((int) l));
                    AppOralScoreLevel appOralScoreLevel = appAnswer.getDubbingScoreLevel();
                    if (appOralScoreLevel != null && pictureInfo.isContainsDubbing()) {
                        studentRecord.setDubbingId(appAnswer.getDubbingId());
                        studentRecord.setDubbingScore(appOralScoreLevel.getScore());
                        studentRecord.setDubbingLevel(appOralScoreLevel.getDesc());
                    }
                    //口语题
                    if (CollectionUtils.isNotEmpty(pictureInfo.getQuestionDetailPart().getSentenceDetails())) {
                        for (PictureInfo.SentenceDetail sentenceDetail : pictureInfo.getQuestionDetailPart().getSentenceDetails()) {
                            if (!objMap.containsKey(sentenceDetail.getQid()))
                                continue;
                            NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj = objMap.get(sentenceDetail.getQid());
                            String s = newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId());
                            if (!subHomeworkResultAnswerMap.containsKey(s))
                                continue;
                            SubHomeworkResultAnswer subHomeworkResultAnswer = subHomeworkResultAnswerMap.get(s);
                            if (!newHomeworkProcessResultMap.containsKey(subHomeworkResultAnswer.getProcessId()))
                                continue;
                            NewHomeworkProcessResult p = newHomeworkProcessResultMap.get(subHomeworkResultAnswer.getProcessId());
                            if (CollectionUtils.isEmpty(p.getOralDetails()))
                                continue;
                            List<BaseHomeworkProcessResult.OralDetail> oralDetails = p.getOralDetails().get(0);
                            if (CollectionUtils.isEmpty(oralDetails))
                                continue;
                            BaseHomeworkProcessResult.OralDetail oralDetail = oralDetails.get(0);
                            String voiceUrl = oralDetail.getAudio();
                            if (StringUtils.isBlank(voiceUrl))
                                continue;
                            VoiceEngineType voiceEngineType = p.getVoiceEngineType();
                            voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);

                            studentRecord.getVoices().add(voiceUrl);
                            sentenceDetail.setTotalNum(1 + sentenceDetail.getTotalNum());
                            AppOralScoreLevel appOralScoreLevel1 = p.getAppOralScoreLevel();
                            double score = appOralScoreLevel1 != null ? appOralScoreLevel1.getScore() : 0;
                            sentenceDetail.getVoiceInfo().add(
                                    MapUtils.m(
                                            "userId", user.getId(),
                                            "userName", user.fetchRealnameIfBlankId(),
                                            "score", score,
                                            "voices", Collections.singletonList(voiceUrl)
                                    ));
                            sentenceDetail.setTotalScore(score + sentenceDetail.getTotalScore());
                        }
                    }
                    //简答题
                    if (CollectionUtils.isNotEmpty(pictureInfo.getQuestionDetailPart().getQuestionDetails())) {
                        for (PictureInfo.QuestionDetail questionDetail : pictureInfo.getQuestionDetailPart().getQuestionDetails()) {
                            if (!allNewQuestionMap.containsKey(questionDetail.getQid()))
                                continue;
                            NewQuestion newQuestion = allNewQuestionMap.get(questionDetail.getQid());
                            if (!objMap.containsKey(questionDetail.getQid()))
                                continue;
                            NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj = objMap.get(questionDetail.getQid());
                            String s = newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId());
                            if (!subHomeworkResultAnswerMap.containsKey(s))
                                continue;
                            SubHomeworkResultAnswer subHomeworkResultAnswer = subHomeworkResultAnswerMap.get(s);
                            if (!newHomeworkProcessResultMap.containsKey(subHomeworkResultAnswer.getProcessId()))
                                continue;
                            NewHomeworkProcessResult p = newHomeworkProcessResultMap.get(subHomeworkResultAnswer.getProcessId());
                            if (newQuestion.getContent() == null)
                                continue;
                            if (CollectionUtils.isEmpty(newQuestion.getContent().getSubContents()))
                                continue;
                            if (newQuestion.getContent().getSubContents().size() != p.getUserAnswers().size())
                                continue;
                            questionDetail.setTotalNum(1 + questionDetail.getTotalNum());
                            String answer;
                            //是否是选择题或者判断题
                            if (questionDetail.isShowRate()) {
                                if (SafeConverter.toBoolean(p.getGrasp())) {
                                    answer = "正确";
                                } else {
                                    answer = NewHomeworkUtils.pressAnswer(newQuestion.getContent()
                                            .getSubContents(), p.getUserAnswers());
                                }
                            } else {
                                if (SafeConverter.toBoolean(p.getGrasp())) {
                                    answer = "正确";
                                } else {
                                    answer = "错误";
                                }
                            }

                            PictureInfo.QuestionAnswerInfo questionAnswerInfo;
                            if (questionDetail.getQuestionAnswerInfoMap().containsKey(answer)) {
                                questionAnswerInfo = questionDetail.getQuestionAnswerInfoMap().get(answer);
                            } else {
                                questionAnswerInfo = new PictureInfo.QuestionAnswerInfo();
                                questionAnswerInfo.setAnswer(answer);
                                questionDetail.getQuestionAnswerInfoMap().put(answer, questionAnswerInfo);
                            }
                            questionAnswerInfo.setFinishNum(1 + questionAnswerInfo.getFinishNum());
                            questionAnswerInfo.getUserInfo().add(MapUtils.m(
                                    "userId", user.getId(),
                                    "userName", user.fetchRealnameIfBlankId()
                            ));
                        }
                    }
                    pictureInfo.getStudentRecords().add(studentRecord);
                }
            } else {
                for (PictureInfo pictureInfo : pictureInfoMap.values()) {
                    PictureInfo.StudentRecord studentRecord = new PictureInfo.StudentRecord();
                    studentRecord.setUserId(user.getId());
                    studentRecord.setUserName(user.fetchRealnameIfBlankId());
                    if (!pictureInfo.isContainsDubbing()) {
                        studentRecord.setDubbingLevel("未布置");
                    }
                    pictureInfo.getStudentRecords().add(studentRecord);
                }
            }
        }
        for (PictureInfo pictureInfo : pictureInfoMap.values()) {
            pictureInfo.getPicturePart().setTotalNum(userMap.size());
            //推荐
            if (recommendMap.containsKey(pictureInfo.getPictureId())) {
                //已经推荐
                ReadingDubbingRecommend readingDubbingRecommend = recommendMap.get(pictureInfo.getPictureId());
                pictureInfo.setRecomended(true);
                pictureInfo.setRecommendParts(readingDubbingRecommend.getReadingDubbings());
            } else {
                //未推荐
                if (CollectionUtils.isNotEmpty(pictureInfo.getRecommendParts())) {
                    pictureInfo.getRecommendParts().sort((o1, o2) -> {
                        int compare = Integer.compare(o2.getScore(), o1.getScore());
                        if (compare == 0) {
                            compare = Long.compare(o1.getDuration(), o2.getDuration());
                        }
                        return compare;
                    });
                }
            }
            //计算平均分
            if (pictureInfo.getPicturePart().getFinishedNum() != 0 && pictureInfo.getPicturePart().getTotalScore() != 0) {
                BigDecimal avgScore = new BigDecimal(pictureInfo.getPicturePart().getTotalScore()).divide(new BigDecimal(pictureInfo.getPicturePart().getFinishedNum()), 0, BigDecimal.ROUND_HALF_UP);
                pictureInfo.getPicturePart().setAvgScore(avgScore.intValue());
            }
            //计算口语题的分数
            if (CollectionUtils.isNotEmpty(pictureInfo.getQuestionDetailPart().getSentenceDetails())) {
                for (PictureInfo.SentenceDetail sentenceDetail : pictureInfo.getQuestionDetailPart().getSentenceDetails()) {
                    if (sentenceDetail.getTotalNum() != 0 && sentenceDetail.getTotalScore() != 0) {
                        BigDecimal avgScore = new BigDecimal(sentenceDetail.getTotalScore()).divide(new BigDecimal(sentenceDetail.getTotalNum()), 0, BigDecimal.ROUND_HALF_UP);
                        sentenceDetail.setAvgScore(avgScore.intValue());
                    }
                }
            }
            //计算简答题的答案
            if (CollectionUtils.isNotEmpty(pictureInfo.getQuestionDetailPart().getQuestionDetails())) {
                for (PictureInfo.QuestionDetail questionDetail : pictureInfo.getQuestionDetailPart().getQuestionDetails()) {
                    for (PictureInfo.QuestionAnswerInfo answerInfo : questionDetail.getQuestionAnswerInfoMap().values()) {
                        if (answerInfo.getFinishNum() > 0) {
                            int rate = new BigDecimal(100 * answerInfo.getFinishNum()).divide(new BigDecimal(questionDetail.getTotalNum()), BigDecimal.ROUND_UP).intValue();
                            answerInfo.setRate(rate);
                        }
                    }
                    if (questionDetail.getQuestionAnswerInfoMap().containsKey("正确")) {
                        questionDetail.getQuestionAnswerInfos().add(0, questionDetail.getQuestionAnswerInfoMap().get("正确"));
                        questionDetail.getQuestionAnswerInfoMap().remove("正确");
                    }
                    for (PictureInfo.QuestionAnswerInfo answerInfo : questionDetail.getQuestionAnswerInfoMap().values()) {
                        questionDetail.getQuestionAnswerInfos().add(answerInfo);
                    }
                    questionDetail.setQuestionAnswerInfoMap(null);
                }
            }
            pictureInfo.getStudentRecords().sort((o1, o2) -> {
                int compare = Integer.compare(o2.getScore(), o1.getScore());
                if (compare == 0) {
                    compare = Double.compare(o2.getDubbingScore(), o1.getDubbingScore());
                    if (compare == 0) {
                        compare = Long.compare(SafeConverter.toLong(o1.getDuration()), SafeConverter.toLong(o2.getDuration()));
                    }
                }
                return compare;
            });
        }
        return MapMessage.successMessage().add("pictureInfoMap", pictureInfoMap)
                .add("subject", Subject.ENGLISH)
                .add("subjectName", Subject.ENGLISH.getValue());
    }


    @Override
    public MapMessage fetchRecommend(String hid, ObjectiveConfigType type, String pictureId) {
        Map<String, PictureBookPlus> pictureBookPlusMap = pictureBookPlusServiceClient.loadByIds(Collections.singleton(pictureId));
        if (!pictureBookPlusMap.containsKey(pictureId)) {
            return MapMessage.errorMessage("绘本不存在");
        }
        PictureBookPlus pictureBookPlus = pictureBookPlusMap.get(pictureId);
        if (StringUtils.isBlank(hid)) {
            return MapMessage.errorMessage("作业ID为空");
        }
        if (type == null) {
            return MapMessage.errorMessage("作业类型不存在");
        }
        if (StringUtils.isBlank(pictureId)) {
            return MapMessage.errorMessage("绘本ID错误");
        }
        NewHomework newHomework = newHomeworkLoader.load(hid);
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (target == null) {
            return MapMessage.errorMessage("作业不包含该类型");
        }
        if (CollectionUtils.isEmpty(target.getApps())) {
            return MapMessage.errorMessage("作业不包含绘本");
        }
        ReadingDubbingRecommend.ID id = new ReadingDubbingRecommend.ID(hid, type, pictureId);
        ReadingDubbingRecommend dubbingRecommend = readingDubbingRecommendDao.load(id.toString());
        if (dubbingRecommend == null) {
            return MapMessage.errorMessage("推荐不存在");
        }
        List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries();
        Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList
                .stream()
                .collect(Collectors
                        .toMap(PictureBookSeries::getId, Function.identity()));
        List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics();
        Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList
                .stream()
                .collect(Collectors.
                        toMap(PictureBookTopic::getId, Function.identity()));
        Map<String, Object> pictureInfo = NewHomeworkContentDecorator.decoratePictureBookPlus(pictureBookPlus, pictureBookSeriesMap, pictureBookTopicMap, null, null, null, null);
        Teacher teacher = teacherLoaderClient.loadTeacher(dubbingRecommend.getTeacherId());

        return MapMessage.successMessage().add("dubbingRecommend", dubbingRecommend)
                .add("subject", Subject.ENGLISH)
                .add("subjectName", Subject.ENGLISH.getValue())
                .add("pictureInfo", pictureInfo)
                .add("teacherName", teacher != null ? teacher.fetchRealnameIfBlankId() : "")
                .add("homeworkTime", DateUtils.dateToString(newHomework.getCreateAt(), "yyyy-MM-dd"));
    }

    @Override
    public MapMessage fetchUserInfo(Long gid) {
        Group group = raikouSDK.getClazzClient().getGroupLoaderClient()
                ._loadGroup(gid).firstOrNull();
        if (group == null) {
            return MapMessage.errorMessage("班组不存在");
        }
        Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient()
                ._loadClazz(group.getClazzId()).firstOrNull();
        if (clazz == null) {
            return MapMessage.errorMessage("班级不存在");
        }
        List<User> users = studentLoaderClient.loadGroupStudents(gid);

        List<Map<String, Object>> studentInfo = new LinkedList<>();
        for (User u : users) {
            studentInfo.add(MapUtils.m(
                    "sid", u.getId(),
                    "groupid", gid,
                    "classlv", clazz.fetchClazzLevel().getLevel()
            ));
        }
        return MapMessage.successMessage().add("studentInfo", studentInfo);
    }

    @Override
    public MapMessage fetchPictureWordCnt(List<String> hids) {
        Map<String, NewHomework> newHomeworkMap = newHomeworkLoader.loads(hids);
        if (newHomeworkMap.isEmpty()) {
            return MapMessage.successMessage();
        }
        //给测试方便的接口：就循环查询了
        //千万别仿造这儿写接口
        Map<String, Map<String, Object>> pictureInfo = new LinkedHashMap<>();
        for (NewHomework newHomework : newHomeworkMap.values()) {
            NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.LEVEL_READINGS);
            if (target == null) {
                continue;
            }
            List<String> picIds = target.getApps().stream().map(NewHomeworkApp::getPictureBookId).collect(Collectors.toList());
            Map<String, PictureBookPlus> pictureBookPlusMap = pictureBookPlusServiceClient.loadByIds(picIds);
            int wordsLength = pictureBookPlusMap.values().stream().mapToInt(BasePictureBook::getWordsLength).sum();
            Map<Long, User> userMap = studentLoaderClient
                    .loadGroupStudents(newHomework.getClazzGroupId())
                    .stream()
                    .collect(Collectors
                            .toMap(LongIdEntity::getId, Function.identity()));
            Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), false)
                    .values()
                    .stream()
                    .filter(BaseHomeworkResult::isFinished)
                    .collect(Collectors.toMap(BaseHomeworkResult::getUserId, Function.identity()));

            int totalDuration = 0;
            int totalScore = 0;
            int num = 0;
            for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
                NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.LEVEL_READINGS);
                if (newHomeworkResultAnswer == null)
                    continue;
                totalDuration += newHomeworkResultAnswer.processDuration();
                totalScore += newHomeworkResultAnswer.processScore(ObjectiveConfigType.LEVEL_READINGS);
                num++;
            }
            int avgDuration = 0;
            int avgScore = 0;
            if (num != 0) {
                avgDuration = new BigDecimal(totalDuration).divide(new BigDecimal(60 * num), BigDecimal.ROUND_UP, 0).intValue();
                avgScore = new BigDecimal(totalScore).divide(new BigDecimal(num)).intValue();
            }


            pictureInfo.put(newHomework.getId(), MapUtils.m(
                    "wordsLength", wordsLength,
                    "avgDuration", avgDuration,
                    "avgScore", avgScore
            ));
        }


        return MapMessage.successMessage().add("pictureInfo", pictureInfo);
    }
}
