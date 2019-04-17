package com.voxlearning.utopia.service.newhomework.impl.template.internal.report.livecast;


import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.core.helper.VoiceEngineTypeUtils;
import com.voxlearning.utopia.service.content.api.constant.PracticeCategory;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveCastBasicData;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveCastReportRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveCastUnitBo;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveLessonData;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.template.ProcessLiveCastHomeworkAnswerDetailTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Named
public class ProcessLiveCastHomeworkAnswerDetailBasicAppTemplate extends ProcessLiveCastHomeworkAnswerDetailTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.BASIC_APP;
    }


    //1、初始化数据
    //2、App循环形成基础结构
    //3、liveCastHomeworkResultMap进行循环，将数据统计到基础结构里面去
    //3->1 语音
    //3->2 非语言
    //4、数据总结回归处理
    @Override
    public void processNewHomeworkAnswerDetail(LiveCastReportRateContext liveCastReportRateContext) {
        ObjectiveConfigType type = liveCastReportRateContext.getType();
        Map<String, NewQuestion> allNewQuestionMap = liveCastReportRateContext.getAllNewQuestionMap();
        Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap = liveCastReportRateContext.getLiveCastHomeworkProcessResultMap();
        Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap = liveCastReportRateContext.getLiveCastHomeworkResultMap();
        Map<Long, User> userMap = liveCastReportRateContext.getUserMap();
        LiveCastHomework liveCastHomework = liveCastReportRateContext.getLiveCastHomework();
        Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMapToObjectiveConfigType = handlerNewHomeworkResultMap(liveCastHomeworkResultMap, type);
        if (MapUtils.isEmpty(liveCastHomeworkResultMapToObjectiveConfigType)) {
            return;
        }
        List<String> qIds = liveCastHomework
                .findNewHomeworkQuestions(type)
                .stream()
                .map(NewHomeworkQuestion::getQuestionId)
                .collect(Collectors.toList());
        Map<String, NewQuestion> newQuestionMap = qIds
                .stream()
                .filter(allNewQuestionMap::containsKey)
                .collect(Collectors
                        .toMap(Function.identity(), allNewQuestionMap::get));
        Map<String, LiveCastBasicData> liveCastBasicDataMap = new LinkedHashMap<>();
        NewHomeworkPracticeContent newHomeworkPracticeContent = liveCastHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        List<NewHomeworkApp> apps = newHomeworkPracticeContent.getApps();
        List<Long> sentenceIds = newQuestionMap
                .values()
                .stream()
                .map(NewQuestion::getSentenceIds)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Map<Long, Sentence> sentenceMap = englishContentLoaderClient.loadEnglishSentences(sentenceIds);
        // 获取作业基础类型的各类信息 key categoryId "_" lessonId
        Set<String> lessonIds = apps.stream()
                .filter(Objects::nonNull)
                .filter(o -> StringUtils.isNotBlank(o.getLessonId()))
                .map(NewHomeworkApp::getLessonId)
                .collect(Collectors.toSet());
        Map<String, NewBookCatalog> lessonMs = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);

        Map<String, String> lineData = NewHomeworkUtils.handleLessonIdToUnitId(lessonMs);

        Set<String> unitIds = new HashSet<>(lineData.values());
        Map<String, NewBookCatalog> unitMs = newContentLoaderClient.loadBookCatalogByCatalogIds(unitIds);

        for (NewHomeworkApp app : apps) {
            Integer categoryId = app.getCategoryId();
            String lessonId = app.getLessonId();
            Long practiceId = app.getPracticeId();
            String k = SafeConverter.toString(categoryId, "") + "-" + SafeConverter.toString(lessonId, "");
            PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(practiceId);
            if (practiceType == null) {
                continue;
            }
            LiveCastBasicData liveCastBasicData = new LiveCastBasicData();
            liveCastBasicData.setCategoryId(categoryId);
            liveCastBasicData.setCategoryName(practiceType.getCategoryName());
            liveCastBasicData.setLessonId(lessonId);
            liveCastBasicData.setLessonName(lessonMs.containsKey(lessonId) ? lessonMs.get(lessonId).getAlias() : "");
            liveCastBasicData.setUnitId(lineData.get(lessonId));
            liveCastBasicData.setUnitName(unitMs.containsKey(lineData.get(lessonId)) ? unitMs.get(lineData.get(lessonId)).getAlias() : "单元");
            liveCastBasicData.setPracticeCategory(PracticeCategory.icon(practiceType.getCategoryName()));
            liveCastBasicData.setNeedRecord(practiceType.fetchNeedRecord());
            liveCastBasicDataMap.put(k, liveCastBasicData);
        }
        for (LiveCastHomeworkResult n : liveCastHomeworkResultMapToObjectiveConfigType.values()) {
            if (MapUtils.isNotEmpty(n.getPractices().get(type).processAnswers())) {
                LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = n.getPractices().get(type).getAppAnswers();
                for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : appAnswers.entrySet()) {
                    //学生各类基础练习情况挂上统计上
                    String k = entry.getKey();
                    NewHomeworkResultAppAnswer ns = entry.getValue();
                    if (liveCastBasicDataMap.get(k).isNeedRecord()) {
                        doHandleForReport(newQuestionMap, liveCastHomeworkProcessResultMap, liveCastBasicDataMap, n, ns, k, userMap, sentenceMap);
                    } else {
                        doHandleForNotReport(newQuestionMap, liveCastHomeworkProcessResultMap, liveCastBasicDataMap, ns, k, userMap, sentenceMap);
                    }
                }
            }
        }
        for (Map.Entry<String, LiveCastBasicData> liveCastBasicDataEntry : liveCastBasicDataMap.entrySet()) {
            LiveCastBasicData liveCastBasicData = liveCastBasicDataEntry.getValue();
            Map<String, LiveCastBasicData.ContentStatistics> contentStatisticsMap = liveCastBasicData.getContentStatisticsMap();
            if (liveCastBasicData.isNeedRecord()) {
                List<LiveCastBasicData.PersonalStatistic> personalStatistics = liveCastBasicData.getPersonalStatistics();
                personalStatistics.sort((o1, o2) -> Integer.compare(o2.getScore(), o1.getScore()));
                for (Map.Entry<String, LiveCastBasicData.ContentStatistics> entry : contentStatisticsMap.entrySet()) {
                    LiveCastBasicData.ContentStatistics contentStatistics = entry.getValue();
                    contentStatistics.getStudentContentInfo().sort((o1, o2) -> Double.compare(o2.getScore(), o1.getScore()));
                    int averageScore = contentStatistics.getSize() != 0 ?
                            new BigDecimal(contentStatistics.getTotalScore())
                                    .divide(new BigDecimal(contentStatistics.getSize()), 1, BigDecimal.ROUND_HALF_UP)
                                    .intValue() :
                            0;
                    AppOralScoreLevel appOralScoreLevel;//内容平均成绩
                    if (averageScore > 90) {
                        appOralScoreLevel = AppOralScoreLevel.A;
                    } else if (averageScore > 75) {
                        appOralScoreLevel = AppOralScoreLevel.B;
                    } else if (averageScore > 60) {
                        appOralScoreLevel = AppOralScoreLevel.C;
                    } else {
                        appOralScoreLevel = AppOralScoreLevel.D;
                    }
                    contentStatistics.setAppOralScoreLevel(appOralScoreLevel);
                }
            } else {
                for (Map.Entry<String, LiveCastBasicData.ContentStatistics> entry : contentStatisticsMap.entrySet()) {
                    LiveCastBasicData.ContentStatistics contentStatistics = entry.getValue();
                    int rate;
                    int rightNum = contentStatistics.getRightStudentInformation().size();
                    int errorNum = contentStatistics.getErrorStudentInformation().size();
                    if (rightNum + errorNum == 0) {
                        rate = 0;
                    } else {
                        rate = new BigDecimal(errorNum)
                                .divide(new BigDecimal(rightNum + errorNum), 3, BigDecimal.ROUND_HALF_UP)
                                .multiply(new BigDecimal(100))
                                .intValue();
                    }
                    contentStatistics.setRate(rate);
                }
            }
            liveCastBasicData.getContentStatisticsList().addAll(liveCastBasicData.getContentStatisticsMap().values());
            liveCastBasicData.setContentStatisticsMap(null);
        }
        liveCastReportRateContext.getResult().put(type.name(), liveCastBasicDataMap.values());
    }

    //另外一种方式先对答题初始化，吧全部答题的信息统计进入，代码组织上或许会更清楚一些
    //1、对题进行循环，在题的维度统计将个人答题信息添加进入
    private void doHandleForNotReport(Map<String, NewQuestion> newQuestionMap,
                                      Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap,
                                      Map<String, LiveCastBasicData> liveCastBasicDataMap,
                                      NewHomeworkResultAppAnswer ns,
                                      String k,
                                      Map<Long, User> userMap,
                                      Map<Long, Sentence> sentenceMap) {
        if (MapUtils.isNotEmpty(ns.getAnswers())) {
            LinkedHashMap<String, String> answers = ns.getAnswers();
            Map<String, LiveCastBasicData.ContentStatistics> contentStatisticsMap = liveCastBasicDataMap.get(k).getContentStatisticsMap();
            for (Map.Entry<String, String> qIdToProcessEntry : answers.entrySet()) {
                LiveCastHomeworkProcessResult np = liveCastHomeworkProcessResultMap.get(qIdToProcessEntry.getValue());
                if (np == null) {
                    continue;
                }
                LiveCastBasicData.ContentStatistics contentStatistics;
                if (contentStatisticsMap.containsKey(qIdToProcessEntry.getKey())) {
                    contentStatistics = contentStatisticsMap.get(qIdToProcessEntry.getKey());
                } else {
                    contentStatistics = new LiveCastBasicData.ContentStatistics();
                    contentStatisticsMap.put(qIdToProcessEntry.getKey(), contentStatistics);
                    NewQuestion newQuestion = newQuestionMap.get(qIdToProcessEntry.getKey());
                    List<Long> _sentenceIds = newQuestion.getSentenceIds();
                    List<LiveCastBasicData.Sentence> sentences = CollectionUtils.isNotEmpty(_sentenceIds) ?
                            _sentenceIds.stream()
                                    .map(l -> {
                                                LiveCastBasicData.Sentence sentence = new LiveCastBasicData.Sentence();
                                                sentence.setSentenceId(l);
                                                sentence.setSentenceContent(Objects.nonNull(sentenceMap.get(l)) ? sentenceMap.get(l).getEnText() : "");
                                                return sentence;
                                            }

                                    )
                                    .collect(Collectors.toList())
                            : Collections.emptyList();
                    contentStatistics.setSentences(sentences);
                    contentStatistics.setQId(qIdToProcessEntry.getKey());
                }
                LiveCastBasicData.StudentContentInfo studentContentInfo = new LiveCastBasicData.StudentContentInfo();
                studentContentInfo.setUserId(np.getUserId());
                studentContentInfo.setUserName(userMap.containsKey(np.getUserId()) ? userMap.get(np.getUserId()).fetchRealname() : "");
                if (SafeConverter.toBoolean(np.getGrasp())) {
                    contentStatistics.getRightStudentInformation().add(studentContentInfo);
                } else {
                    contentStatistics.getErrorStudentInformation().add(studentContentInfo);
                }
            }
        }
    }

    //1、类型下的个人维度的数据统计
    //2、答题题进行循环，在题的维度统计将个人答题信息添加进入
    private void doHandleForReport(Map<String, NewQuestion> newQuestionMap,
                                   Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap,
                                   Map<String, LiveCastBasicData> liveCastBasicDataMap,
                                   LiveCastHomeworkResult n,
                                   NewHomeworkResultAppAnswer ns,
                                   String k,
                                   Map<Long, User> userMap,
                                   Map<Long, Sentence> sentenceMap) {
        List<String> voiceUrls = new LinkedList<>();
        LiveCastBasicData.PersonalStatistic personalStatistic = new LiveCastBasicData.PersonalStatistic();
        personalStatistic.setUserId(n.getUserId());
        personalStatistic.setUserName(userMap.containsKey(n.getUserId()) ? userMap.get(n.getUserId()).fetchRealname() : "");
        int score = new BigDecimal(SafeConverter.toDouble(ns.getScore())).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        personalStatistic.setScore(score);
        personalStatistic.setVoiceUrls(voiceUrls);
        liveCastBasicDataMap.get(k).getPersonalStatistics().add(personalStatistic);
        String voiceScoringMode = "";
        if (MapUtils.isNotEmpty(ns.getAnswers())) {
            LinkedHashMap<String, String> answers = ns.getAnswers();
            for (Map.Entry<String, String> qIdToProcessEntry : answers.entrySet()) {
                LiveCastHomeworkProcessResult np = liveCastHomeworkProcessResultMap.get(qIdToProcessEntry.getValue());
                if (np == null) continue;
                List<String> _voiceUrls = new LinkedList<>();
                if (CollectionUtils.isNotEmpty(np.getOralDetails())) {
                    for (List<BaseHomeworkProcessResult.OralDetail> oralDetails : np.getOralDetails()) {
                        if (CollectionUtils.isNotEmpty(oralDetails)) {
                            for (BaseHomeworkProcessResult.OralDetail oralDetail : oralDetails) {
                                String voiceUrl = oralDetail.getAudio();
                                if (StringUtils.isNotEmpty(voiceUrl)) {
                                    VoiceEngineType voiceEngineType = np.getVoiceEngineType();
                                    voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                                    voiceUrls.add(voiceUrl);
                                    _voiceUrls.add(voiceUrl);
                                }
                            }
                        }
                    }
                }
                LiveCastBasicData.ContentStatistics contentStatistics;
                if (liveCastBasicDataMap.get(k).getContentStatisticsMap().containsKey(qIdToProcessEntry.getKey())) {
                    contentStatistics = liveCastBasicDataMap.get(k).getContentStatisticsMap().get(qIdToProcessEntry.getKey());
                    contentStatistics.setSize(contentStatistics.getSize() + 1);
                    contentStatistics.setTotalScore(contentStatistics.getTotalScore() + np.getScore());
                } else {
                    contentStatistics = new LiveCastBasicData.ContentStatistics();
                    liveCastBasicDataMap.get(k).getContentStatisticsMap().put(qIdToProcessEntry.getKey(), contentStatistics);
                    NewQuestion newQuestion = newQuestionMap.get(qIdToProcessEntry.getKey());
                    List<Long> _sentenceIds = newQuestion.getSentenceIds();
                    List<LiveCastBasicData.Sentence> sentences = CollectionUtils.isNotEmpty(_sentenceIds) ?
                            _sentenceIds.stream()
                                    .map(l -> {
                                                LiveCastBasicData.Sentence sentence = new LiveCastBasicData.Sentence();
                                                sentence.setSentenceId(l);
                                                sentence.setSentenceContent(Objects.nonNull(sentenceMap.get(l)) ? sentenceMap.get(l).getEnText() : "");
                                                return sentence;
                                            }
                                    )
                                    .collect(Collectors.toList())
                            : Collections.emptyList();
                    contentStatistics.setSentences(sentences);
                    contentStatistics.setTotalScore(np.getScore());
                }
                if (StringUtils.isBlank(voiceScoringMode)) {
                    voiceScoringMode = np.getVoiceScoringMode();
                }
                LiveCastBasicData.StudentContentInfo studentContentInfo = new LiveCastBasicData.StudentContentInfo();
                contentStatistics.getStudentContentInfo().add(studentContentInfo);
                studentContentInfo.setUserId(n.getUserId());
                studentContentInfo.setUserName(userMap.containsKey(n.getUserId()) ? userMap.get(n.getUserId()).fetchRealname() : "");
                studentContentInfo.setVoiceUrls(_voiceUrls);
                studentContentInfo.setScore(SafeConverter.toDouble(np.getScore()));
                studentContentInfo.setAppOralScoreLevel(np.getAppOralScoreLevel());
            }
            personalStatistic.setVoiceScoringMode(voiceScoringMode);
        }
    }

    //1、初始化数据，让APP数据循环
    //2、挂钩数据处理
    // 后期代码组织方式，1、构建初始化这个数据，2、将数据放入处理3、后期数据加工处理
    @Override
    public void processNewHomeworkAnswerDetailPersonal(LiveCastReportRateContext liveCastReportRateContext) {
        NewHomeworkPracticeContent newHomeworkPracticeContent = liveCastReportRateContext.getLiveCastHomework().findPracticeContents().get(liveCastReportRateContext.getType());
        if (Objects.isNull(newHomeworkPracticeContent)
                || CollectionUtils.isEmpty(newHomeworkPracticeContent.getApps())) {
            return;
        }
        Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap = liveCastReportRateContext.getLiveCastHomeworkProcessResultMap();
        List<NewHomeworkApp> apps = newHomeworkPracticeContent.getApps();
        Map<String, LiveLessonData> lessonData = newLessonDataForBasicApp(liveCastHomeworkProcessResultMap, liveCastReportRateContext.getLiveCastHomeworkResult(), apps, liveCastReportRateContext.getType());
        Map<String, NewBookCatalog> ms = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonData.keySet());
        Map<String, String> lineData = NewHomeworkUtils.handleLessonIdToUnitId(ms);
        Set<String> lessonIds = apps
                .stream()
                .map(NewHomeworkApp::getLessonId)
                .collect(Collectors.toSet());
        Set<String> unitIds = new HashSet<>(lineData.values());
        Map<String, NewBookCatalog> finalMs = newContentLoaderClient.loadBookCatalogByCatalogIds(unitIds);
        Map<String, LiveCastUnitBo> liveCastUnitBoMap = unitIds
                .stream()
                .collect(Collectors
                        .toMap(Function.identity(), o -> {
                            LiveCastUnitBo liveCastUnitBo = new LiveCastUnitBo();
                            liveCastUnitBo.setUnitId(o);
                            if (finalMs.containsKey(o)) {
                                liveCastUnitBo.setUnitName(finalMs.get(o).getAlias());
                            }
                            return liveCastUnitBo;
                        }));
        List<LiveCastUnitBo.LiveCastLessonBo> liveCastLessonBos = new LinkedList<>();
        for (String lessonId : lessonIds) {
            LiveCastUnitBo.LiveCastLessonBo liveCastLessonBo = new LiveCastUnitBo.LiveCastLessonBo();
            liveCastLessonBo.setLessonId(lessonId);
            if (ms.containsKey(lessonId)) {
                liveCastLessonBo.setLessonName(ms.get(lessonId).getAlias());
            }
            if (lessonData.containsKey(lessonId)) {
                liveCastLessonBo.setCategories(lessonData.get(lessonId).getCategories());
            }
            liveCastLessonBo.setUnitId(lineData.get(lessonId));
            liveCastLessonBos.add(liveCastLessonBo);
        }
        for (LiveCastUnitBo.LiveCastLessonBo liveCastLessonBo : liveCastLessonBos) {
            if (liveCastUnitBoMap.containsKey(liveCastLessonBo.getUnitId())) {
                LiveCastUnitBo liveCastUnitBo = liveCastUnitBoMap.get(liveCastLessonBo.getUnitId());
                liveCastUnitBo.setFlag(true);
                liveCastUnitBo.getLessons().add(liveCastLessonBo);
            }
        }
        if (MapUtils.isNotEmpty(liveCastUnitBoMap)) {
            liveCastReportRateContext.getResultMap().put(liveCastReportRateContext.getType(), liveCastUnitBoMap.values()
                    .stream()
                    .filter(LiveCastUnitBo::isFlag)
                    .collect(Collectors.toList()));
        }
    }

    private Map<String, LiveLessonData> newLessonDataForBasicApp(Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap, LiveCastHomeworkResult liveCastHomeworkResult, List<NewHomeworkApp> apps, ObjectiveConfigType objectiveConfig) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(liveCastHomeworkResult.getUserId());
        String name = Objects.isNull(studentDetail) ?
                "" :
                studentDetail.fetchRealname();
        Map<String, LiveLessonData.LiveCategoryData> liveCategoryDataMap = new LinkedHashMap<>();
        for (NewHomeworkApp app : apps) {
            LiveLessonData.LiveCategoryData liveCategoryData = new LiveLessonData.LiveCategoryData();
            PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(app.getPracticeId());
            if (practiceType == null) continue;
            liveCategoryData.setLessonId(app.getLessonId());
            liveCategoryData.setCategoryId(app.getCategoryId());
            liveCategoryData.setCategoryName(practiceType.getCategoryName());
            liveCategoryData.setUserId(liveCastHomeworkResult.getUserId());
            liveCategoryData.setUserName(name);
            liveCategoryData.setPracticeCategory(PracticeCategory.icon(practiceType.getCategoryName()));
            liveCategoryDataMap.put(app.getCategoryId() + "-" + app.getLessonId(), liveCategoryData);
        }
        if (liveCastHomeworkResult.getPractices() != null && liveCastHomeworkResult.getPractices().get(objectiveConfig) != null) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = liveCastHomeworkResult.getPractices().get(objectiveConfig);
            if (newHomeworkResultAnswer.getAppAnswers() != null) {
                for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : newHomeworkResultAnswer.getAppAnswers().entrySet()) {
                    if (liveCategoryDataMap.containsKey(entry.getKey())) {
                        LiveLessonData.LiveCategoryData liveCategoryData = liveCategoryDataMap.get(entry.getKey());
                        NewHomeworkResultAppAnswer appAnswer = entry.getValue();
                        PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(appAnswer.getPracticeId());
                        if (practiceType == null) continue;
                        if (practiceType.fetchNeedRecord()) {
                            if (MapUtils.isNotEmpty(appAnswer.getAnswers())) {
                                LinkedHashMap<String, String> answers = appAnswer.getAnswers();
                                for (String newHomeworkProcessId : answers.values()) {
                                    LiveCastHomeworkProcessResult n = liveCastHomeworkProcessResultMap.get(newHomeworkProcessId);
                                    if (n != null) {
                                        if (CollectionUtils.isNotEmpty(n.getOralDetails())) {
                                            for (List<BaseHomeworkProcessResult.OralDetail> oralDetails : n.getOralDetails()) {
                                                if (CollectionUtils.isNotEmpty(oralDetails)) {
                                                    for (BaseHomeworkProcessResult.OralDetail oralDetail : oralDetails) {
                                                        String voiceUrl = oralDetail.getAudio();
                                                        if (StringUtils.isNotBlank(voiceUrl)) {
                                                            VoiceEngineType voiceEngineType = n.getVoiceEngineType();
                                                            voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                                                            liveCategoryData.getVoiceUrls().add(voiceUrl);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (StringUtils.isBlank(liveCategoryData.getVoiceScoringMode())) {
                                            liveCategoryData.setVoiceScoringMode(n.getVoiceScoringMode());
                                        }
                                    }
                                }

                            }
                        }
                        liveCategoryData.setAverageScore(Objects.isNull(appAnswer.getScore()) ?
                                0 :
                                new BigDecimal(appAnswer.getScore())
                                        .setScale(0, BigDecimal.ROUND_HALF_UP)
                                        .intValue());

                        liveCategoryData.setFlag(true);
                    }
                }
            }
        }
        List<LiveLessonData.LiveCategoryData> liveCategoryDatas = liveCategoryDataMap.values()
                .stream()
                .filter(LiveLessonData.LiveCategoryData::isFlag)
                .collect(Collectors.toList());

        Map<String, LiveLessonData> liveLessonDataMap = new LinkedHashMap<>();
        for (NewHomeworkApp app : apps) {
            if (!liveLessonDataMap.containsKey(app.getLessonId())) {
                LiveLessonData liveLessonData = new LiveLessonData();
                liveLessonData.setLessonId(app.getLessonId());
                liveLessonDataMap.put(app.getLessonId(), liveLessonData);
            }
        }

        for (LiveLessonData.LiveCategoryData liveCategoryData : liveCategoryDatas) {
            if (liveLessonDataMap.containsKey(liveCategoryData.getLessonId())) {
                LiveLessonData liveLessonData = liveLessonDataMap.get(liveCategoryData.getLessonId());
                liveLessonData.setFlag(true);
                liveLessonData.getCategories().add(liveCategoryData);
            }
        }
        return liveLessonDataMap.values().stream()
                .filter(LiveLessonData::isFlag)
                .collect(Collectors.toMap(LiveLessonData::getLessonId, Function.identity()));
    }
}
