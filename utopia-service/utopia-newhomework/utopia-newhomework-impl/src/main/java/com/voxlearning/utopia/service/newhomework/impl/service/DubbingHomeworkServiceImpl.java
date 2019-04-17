package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.service.content.api.entity.WordStock;
import com.voxlearning.utopia.service.content.consumer.WordStockLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSyntheticHistory;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.service.DubbingHomeworkService;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkProcessResultDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLivecastLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;
import com.voxlearning.utopia.service.question.api.entity.DubbingCategory;
import com.voxlearning.utopia.service.question.api.entity.DubbingTheme;
import com.voxlearning.utopia.service.question.consumer.DubbingLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * @author guoqiang.li
 * @since 2017/10/31
 */
@Named
@Service(interfaceClass = DubbingHomeworkService.class)
@ExposeService(interfaceClass = DubbingHomeworkService.class)
public class DubbingHomeworkServiceImpl implements DubbingHomeworkService {
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject private DubbingLoaderClient dubbingLoaderClient;
    @Inject private VacationHomeworkDao vacationHomeworkDao;
    @Inject private VacationHomeworkResultDao vacationHomeworkResultDao;
    @Inject private NewHomeworkLivecastLoaderImpl newHomeworkLivecastLoader;
    @Inject protected WordStockLoaderClient wordStockLoaderClient;
    @Inject protected NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;
    @Inject protected VacationHomeworkProcessResultDao vacationHomeworkProcessResultDao;

    @Override
    public List<DubbingSummaryResult> getDubbingSummerInfo(String homeworkId, Long studentId, String objectiveConfigTypeKey) {
        if (StringUtils.isBlank(homeworkId)) {
            return Collections.emptyList();
        }
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeKey);
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return Collections.emptyList();
        }

        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
        Map<String, NewHomeworkResultAppAnswer> appAnswerMap = new HashMap<>();
        if (newHomeworkResult != null
                && MapUtils.isNotEmpty(newHomeworkResult.getPractices())
                && newHomeworkResult.getPractices().get(objectiveConfigType) != null
                && MapUtils.isNotEmpty(newHomeworkResult.getPractices().get(objectiveConfigType).getAppAnswers())) {
            appAnswerMap = newHomeworkResult.getPractices().get(objectiveConfigType).getAppAnswers();
        }

        List<DubbingSummaryResult> resultList = new ArrayList<>();
        List<String> dubbingIds = newHomework.findNewHomeworkApps(objectiveConfigType).stream().map(NewHomeworkApp::getDubbingId).collect(Collectors.toList());
        Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(dubbingIds);
        Set<String> albumIds = dubbingMap.values().stream().map(Dubbing::getCategoryId).collect(Collectors.toSet());
        Map<String, DubbingCategory> albumMap = dubbingLoaderClient.loadDubbingCategoriesByIds(albumIds);

        Set<Long> wordStockIds = dubbingMap.values()
                .stream()
                .filter(dubbing -> CollectionUtils.isNotEmpty(dubbing.getKeyWords()))
                .map(Dubbing::getKeyWords)
                .flatMap(Collection::stream)
                .filter(dubbingKeyWord -> dubbingKeyWord.getWordStockId() != null)
                .map(Dubbing.DubbingKeyWord::getWordStockId)
                .collect(Collectors.toSet());
        Map<Long, WordStock> wordStockMap = wordStockLoaderClient.loadWordStocks(new ArrayList<>(wordStockIds));

        // 生成合成配音ids
        List<String> ids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dubbingIds)) {
            for (String id : dubbingIds) {
                ids.add(new DubbingSyntheticHistory.ID(homeworkId, studentId, id).toString());
            }
        }
        Map<String, DubbingSyntheticHistory> dubbingSyntheticHistoryMap = newHomeworkLoader.loadDubbingSyntheticHistories(ids);
        Map<String, String> dubbingThemeMap = dubbingLoaderClient.loadAllDubbingThemes()
                .stream()
                .collect(toMap(DubbingTheme::getId, DubbingTheme::getName));

        // 该作业形式的总体得分(8分制)
        Map<String, SubHomeworkProcessResult> homeworkProcessResultMap = new HashMap<>();
        if (MapUtils.isNotEmpty(appAnswerMap)) {
            // key:questionId   value:dubbingId
            Map<String, String> dubbingQuestionMap = new HashMap<>();
            for (String dubbingId : dubbingIds) {
                NewHomeworkResultAppAnswer appAnswer = appAnswerMap.get(dubbingId);
                if (appAnswer != null) {
                    appAnswer.getAnswers().keySet().forEach(q -> dubbingQuestionMap.put(q, dubbingId));
                }
            }
            String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
            List<String> subHomeworkResultAnswerIds = new LinkedList<>();
            for (Map.Entry<String, String> entry : dubbingQuestionMap.entrySet()) {
                NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomework.NewHomeworkQuestionObj(newHomework.getId(), objectiveConfigType, Collections.singletonList(entry.getValue()), entry.getKey());
                subHomeworkResultAnswerIds.add(newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, studentId));
            }
            Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);
            List<String> newHomeworkProcessResultIds = subHomeworkResultAnswerMap.values()
                    .stream()
                    .map(SubHomeworkResultAnswer::getProcessId)
                    .collect(Collectors.toList());
            Map<String, SubHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(newHomeworkProcessResultIds);
            homeworkProcessResultMap = newHomeworkProcessResultMap.values()
                    .stream()
                    .collect(Collectors.toMap(SubHomeworkProcessResult::getQuestionId, Function.identity()));
        }

        for (String dubbingId : dubbingIds) {
            Dubbing dubbing = dubbingMap.get(dubbingId);
            if (dubbing == null) {
                continue;
            }
            DubbingCategory album = albumMap.get(dubbing.getCategoryId());
            List<Map<String, Object>> keyWords = Collections.emptyList();
            List<Map<String, Object>> keyGrammars = Collections.emptyList();
            if (CollectionUtils.isNotEmpty(dubbing.getKeyWords())) {
                keyWords = dubbing.getKeyWords()
                        .stream()
                        .map(dubbingKeyWord -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("chineseWord", dubbingKeyWord.getChineseWord());
                            map.put("englishWord", dubbingKeyWord.getEnglishWord());
                            String audioUrl = null;
                            if (MapUtils.isNotEmpty(wordStockMap)) {
                                WordStock wordStock = wordStockMap.get(dubbingKeyWord.getWordStockId());
                                if (wordStock != null) {
                                    if (SafeConverter.toBoolean(dubbingKeyWord.getAudioIsUs(), true)) {
                                        audioUrl = wordStock.getAudioUS();
                                    } else {
                                        audioUrl = wordStock.getAudioUK();
                                    }
                                }
                            }
                            if (audioUrl == null) {
                                return null;
                            }
                            map.put("audioUrl", audioUrl);
                            return map;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
            if (CollectionUtils.isNotEmpty(dubbing.getKeyGrammars())) {
                keyGrammars = dubbing.getKeyGrammars()
                        .stream()
                        .map(dubbingGrammar -> MapUtils.m("grammarName", dubbingGrammar.getGrammarName(), "exampleSentence", dubbingGrammar.getExampleSentence()))
                        .collect(Collectors.toList());
            }

            NewHomeworkResultAppAnswer appAnswer = appAnswerMap.get(dubbingId);
            String id = new DubbingSyntheticHistory.ID(homeworkId, studentId, dubbingId).toString();
            DubbingSyntheticHistory dubbingSyntheticHistory = dubbingSyntheticHistoryMap.get(id);

            // 合成配音是否成功
            boolean synthetic = dubbingSyntheticHistory == null || SafeConverter.toBoolean(dubbingSyntheticHistory.isSyntheticSuccess(newHomework.getCreateAt()));
            // 是否跳过上传
            boolean skipUploadVideo = appAnswer != null && appAnswer.isFinished() && SafeConverter.toBoolean(appAnswer.getSkipUploadVideo());

            DubbingSummaryResult result = new DubbingSummaryResult();
            result.setDubbingId(dubbingId);
            result.setDubbingName(dubbing.getVideoName());
            result.setVideoUrl(dubbing.getVideoUrl());
            result.setCoverUrl(dubbing.getCoverUrl());
            result.setSynthetic(synthetic);
            result.setSkipUploadVideo(skipUploadVideo);
            result.setKeyWords(keyWords);
            result.setKeyGrammars(keyGrammars);
            result.setTopics(CollectionUtils.isNotEmpty(dubbing.getThemeIds()) ? dubbing.getThemeIds().stream().map(dubbingThemeMap::get).filter(StringUtils::isNotEmpty).collect(Collectors.toList()) : Collections.emptyList());
            result.setLevel(SafeConverter.toInt(dubbing.getDifficult()));
            result.setClazzLevel(ClazzLevel.getDescription(SafeConverter.toInt(dubbing.getDifficult())));
            result.setAlbumName(album != null ? album.getName() : "");
            result.setVideoSummary(dubbing.getVideoSummary());
            result.setProcessResultUrl(UrlUtils.buildUrlQuery("/exam/flash/newhomework/batch/processresult" + Constants.AntiHijackExt, MapUtils.m("sid", studentId)));
            result.setQuestionUrl(UrlUtils.buildUrlQuery("/student/exam/newhomework/questions" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "videoId", dubbingId, "sid", studentId)));
            result.setCompletedUrl(UrlUtils.buildUrlQuery("/student/exam/newhomework/questions/answer" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "videoId", dubbingId, "sid", studentId)));
            result.setFinished(appAnswer != null && appAnswer.isFinished());

            Double scoreResult = null;
            if (appAnswer != null && MapUtils.isNotEmpty(appAnswer.getAnswers())) {
                // 计算8分制分数
                List<SubHomeworkProcessResult> subHomeworkProcessResultList = new ArrayList<>();
                Set<String> questionIds = appAnswer.getAnswers().keySet();
                for (String questionId : questionIds) {
                    SubHomeworkProcessResult subHomeworkProcessResult = homeworkProcessResultMap.get(questionId);
                    if (subHomeworkProcessResult != null) {
                        subHomeworkProcessResultList.add(subHomeworkProcessResult);
                    }
                }

                double totalScore = subHomeworkProcessResultList.stream().mapToDouble(processResult -> SafeConverter.toDouble(processResult.getActualScore())).sum();
                scoreResult = SafeConverter.toDouble(Math.floor(totalScore / subHomeworkProcessResultList.size()));
            }
            result.setScore(scoreResult);

            //判断该配音是否是欢快歌曲类型   欢快歌曲类型ID:DC_10300000140166
            DubbingCategory dubbingCategory = dubbingLoaderClient.loadDubbingCategoriesByIds(Collections.singleton(dubbing.getCategoryId())).get(dubbing.getCategoryId());
            result.setIsSong(dubbingCategory != null && Objects.equals("DC_10300000140166", dubbingCategory.getParentId()));
            resultList.add(result);
        }
        return resultList;
    }

    @Override
    public List<DubbingSummaryResult> getLiveCastDubbingSummerInfo(String homeworkId, Long studentId) {
        if (StringUtils.isBlank(homeworkId)) {
            return Collections.emptyList();
        }
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.DUBBING;
        LiveCastHomework liveCastHomework = newHomeworkLivecastLoader.loadLiveCastHomeworkIncludeDisabled(homeworkId);
        if (liveCastHomework == null) {
            return Collections.emptyList();
        }

        LiveCastHomeworkResult liveCastHomeworkResult = newHomeworkLivecastLoader.loadLiveCastHomeworkResult(liveCastHomework.toLocation(), studentId);
        Map<String, NewHomeworkResultAppAnswer> appAnswerMap = new HashMap<>();
        if (liveCastHomeworkResult != null
                && MapUtils.isNotEmpty(liveCastHomeworkResult.getPractices())
                && liveCastHomeworkResult.getPractices().get(objectiveConfigType) != null
                && MapUtils.isNotEmpty(liveCastHomeworkResult.getPractices().get(objectiveConfigType).getAppAnswers())) {
            appAnswerMap = liveCastHomeworkResult.getPractices().get(objectiveConfigType).getAppAnswers();
        }

        List<DubbingSummaryResult> resultList = new ArrayList<>();
        List<String> dubbingIds = liveCastHomework.findNewHomeworkApps(objectiveConfigType).stream().map(NewHomeworkApp::getDubbingId).collect(Collectors.toList());
        Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(dubbingIds);
        Set<String> albumIds = dubbingMap.values().stream().map(Dubbing::getCategoryId).collect(Collectors.toSet());
        Map<String, DubbingCategory> albumMap = dubbingLoaderClient.loadDubbingCategoriesByIds(albumIds);

        // 生成合成配音ids
        List<String> ids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dubbingIds)) {
            for (String id : dubbingIds) {
                ids.add(new DubbingSyntheticHistory.ID(homeworkId, studentId, id).toString());
            }
        }
        Map<String, DubbingSyntheticHistory> dubbingSyntheticHistoryMap = newHomeworkLoader.loadDubbingSyntheticHistories(ids);

        for (String dubbingId : dubbingIds) {
            Dubbing dubbing = dubbingMap.get(dubbingId);
            if (dubbing == null) {
                continue;
            }
            DubbingCategory album = albumMap.get(dubbing.getCategoryId());
            List<Map<String, Object>> keyWords = Collections.emptyList();
            List<Map<String, Object>> keyGrammars = Collections.emptyList();
            if (CollectionUtils.isNotEmpty(dubbing.getKeyWords())) {
                keyWords = dubbing.getKeyWords()
                        .stream()
                        .map(dubbingKeyWord -> MapUtils.m("chineseWord", dubbingKeyWord.getChineseWord(), "englishWord", dubbingKeyWord.getEnglishWord()))
                        .collect(Collectors.toList());
            }
            if (CollectionUtils.isNotEmpty(dubbing.getKeyGrammars())) {
                keyGrammars = dubbing.getKeyGrammars()
                        .stream()
                        .map(dubbingGrammar -> MapUtils.m("grammarName", dubbingGrammar.getGrammarName(), "exampleSentence", dubbingGrammar.getExampleSentence()))
                        .collect(Collectors.toList());
            }

            String id = new DubbingSyntheticHistory.ID(homeworkId, studentId, dubbingId).toString();
            DubbingSyntheticHistory dubbingSyntheticHistory = dubbingSyntheticHistoryMap.get(id);
            // 合成配音是否成功
            Boolean synthetic = dubbingSyntheticHistory == null || SafeConverter.toBoolean(dubbingSyntheticHistory.isSyntheticSuccess(liveCastHomework.getCreateAt()));

            DubbingSummaryResult result = new DubbingSummaryResult();
            result.setDubbingId(dubbingId);
            result.setDubbingName(dubbing.getVideoName());
            result.setVideoUrl(dubbing.getVideoUrl());
            result.setCoverUrl(dubbing.getCoverUrl());
            result.setSynthetic(synthetic);
            result.setKeyWords(keyWords);
            result.setKeyGrammars(keyGrammars);
            result.setTopics(dubbing.getTopics().stream().map(Dubbing.DubbingTopic::getName).collect(Collectors.toList()));
            result.setLevel(SafeConverter.toInt(dubbing.getDifficult()));
            result.setClazzLevel(ClazzLevel.getDescription(SafeConverter.toInt(dubbing.getDifficult())));
            result.setAlbumName(album != null ? album.getName() : "");
            result.setVideoSummary(dubbing.getVideoSummary());
            result.setProcessResultUrl("/exam/flash/newhomework/batch/processresult" + Constants.AntiHijackExt);
            result.setQuestionUrl(UrlUtils.buildUrlQuery("/student/exam/newhomework/questions" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "videoId", dubbingId)));
            result.setCompletedUrl(UrlUtils.buildUrlQuery("/student/exam/newhomework/questions/answer" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "videoId", dubbingId)));
            NewHomeworkResultAppAnswer appAnswer = appAnswerMap.get(dubbingId);
            result.setFinished(appAnswer != null && appAnswer.isFinished());
            resultList.add(result);
        }
        return resultList;
    }

    @Override
    public List<DubbingSummaryResult> getVacationDubbingSummerInfo(String homeworkId, Long studentId, String objectiveConfigTypeKey) {
        if (StringUtils.isBlank(homeworkId)) {
            return Collections.emptyList();
        }
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeKey);
        VacationHomework vacationHomework = vacationHomeworkDao.load(homeworkId);
        if (vacationHomework == null) {
            return Collections.emptyList();
        }

        VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.load(homeworkId);
        Map<String, NewHomeworkResultAppAnswer> appAnswerMap = new HashMap<>();
        if (vacationHomeworkResult != null
                && MapUtils.isNotEmpty(vacationHomeworkResult.getPractices())
                && vacationHomeworkResult.getPractices().get(objectiveConfigType) != null
                && MapUtils.isNotEmpty(vacationHomeworkResult.getPractices().get(objectiveConfigType).getAppAnswers())) {
            appAnswerMap = vacationHomeworkResult.getPractices().get(objectiveConfigType).getAppAnswers();
        }

        List<DubbingSummaryResult> resultList = new ArrayList<>();
        List<String> dubbingIds = vacationHomework.findNewHomeworkApps(objectiveConfigType)
                .stream()
                .map(NewHomeworkApp::getDubbingId)
                .collect(Collectors.toList());

        Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(dubbingIds);
        Set<String> albumIds = dubbingMap.values().stream().map(Dubbing::getCategoryId).collect(Collectors.toSet());
        Map<String, DubbingCategory> albumMap = dubbingLoaderClient.loadDubbingCategoriesByIds(albumIds);

        Set<Long> wordStockIds = dubbingMap.values()
                .stream()
                .filter(dubbing -> CollectionUtils.isNotEmpty(dubbing.getKeyWords()))
                .map(Dubbing::getKeyWords)
                .flatMap(Collection::stream)
                .filter(dubbingKeyWord -> dubbingKeyWord.getWordStockId() != null)
                .map(Dubbing.DubbingKeyWord::getWordStockId)
                .collect(Collectors.toSet());
        Map<Long, WordStock> wordStockMap = wordStockLoaderClient.loadWordStocks(new ArrayList<>(wordStockIds));

        // 生成合成配音ids
        List<String> ids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dubbingIds)) {
            for (String id : dubbingIds) {
                ids.add(new DubbingSyntheticHistory.ID(homeworkId, studentId, id).toString());
            }
        }
        Map<String, DubbingSyntheticHistory> dubbingSyntheticHistoryMap = newHomeworkLoader.loadDubbingSyntheticHistories(ids);
        Map<String, String> dubbingThemeMap = dubbingLoaderClient.loadAllDubbingThemes()
                .stream()
                .collect(toMap(DubbingTheme::getId, DubbingTheme::getName));

        // 该作业形式的总体得分(8分制)
        Map<String, VacationHomeworkProcessResult> homeworkProcessResultMap = new HashMap<>();
        if (MapUtils.isNotEmpty(appAnswerMap)) {
            List<String> processrResultIds = appAnswerMap.values().stream()
                    .map(NewHomeworkResultAppAnswer::getAnswers)
                    .filter(MapUtils::isNotEmpty)
                    .map(LinkedHashMap::values)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            Map<String, VacationHomeworkProcessResult> vacationHomeworkResultMap = vacationHomeworkProcessResultDao.loads(processrResultIds);
            homeworkProcessResultMap = vacationHomeworkResultMap.values()
                    .stream()
                    .collect(Collectors.toMap(VacationHomeworkProcessResult::getQuestionId, Function.identity()));
        }

        for (String dubbingId : dubbingIds) {
            Dubbing dubbing = dubbingMap.get(dubbingId);
            if (dubbing == null) {
                continue;
            }
            DubbingCategory album = albumMap.get(dubbing.getCategoryId());
            List<Map<String, Object>> keyWords = Collections.emptyList();
            List<Map<String, Object>> keyGrammars = Collections.emptyList();
            if (CollectionUtils.isNotEmpty(dubbing.getKeyWords())) {
                keyWords = dubbing.getKeyWords()
                        .stream()
                        .map(dubbingKeyWord -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("chineseWord", dubbingKeyWord.getChineseWord());
                            map.put("englishWord", dubbingKeyWord.getEnglishWord());
                            String audioUrl = null;
                            if (MapUtils.isNotEmpty(wordStockMap)) {
                                WordStock wordStock = wordStockMap.get(dubbingKeyWord.getWordStockId());
                                if (wordStock != null) {
                                    if (SafeConverter.toBoolean(dubbingKeyWord.getAudioIsUs(), true)) {
                                        audioUrl = wordStock.getAudioUS();
                                    } else {
                                        audioUrl = wordStock.getAudioUK();
                                    }
                                }
                            }
                            if (audioUrl == null) {
                                return null;
                            }
                            map.put("audioUrl", audioUrl);
                            return map;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
            if (CollectionUtils.isNotEmpty(dubbing.getKeyGrammars())) {
                keyGrammars = dubbing.getKeyGrammars()
                        .stream()
                        .map(dubbingGrammar -> MapUtils.m("grammarName", dubbingGrammar.getGrammarName(), "exampleSentence", dubbingGrammar.getExampleSentence()))
                        .collect(Collectors.toList());
            }

            NewHomeworkResultAppAnswer appAnswer = appAnswerMap.get(dubbingId);
            String id = new DubbingSyntheticHistory.ID(homeworkId, studentId, dubbingId).toString();
            DubbingSyntheticHistory dubbingSyntheticHistory = dubbingSyntheticHistoryMap.get(id);

            // 合成配音是否成功
            Boolean synthetic = dubbingSyntheticHistory == null || SafeConverter.toBoolean(dubbingSyntheticHistory.isSyntheticSuccess(vacationHomework.getCreateAt()));
            // 是否跳过上传
            boolean skipUploadVideo = appAnswer != null && appAnswer.isFinished() && SafeConverter.toBoolean(appAnswer.getSkipUploadVideo());

            DubbingSummaryResult result = new DubbingSummaryResult();
            result.setDubbingId(dubbingId);
            result.setDubbingName(dubbing.getVideoName());
            result.setVideoUrl(dubbing.getVideoUrl());
            result.setCoverUrl(dubbing.getCoverUrl());
            result.setSynthetic(synthetic);
            result.setSkipUploadVideo(skipUploadVideo);
            result.setKeyWords(keyWords);
            result.setKeyGrammars(keyGrammars);
            result.setTopics(CollectionUtils.isNotEmpty(dubbing.getThemeIds()) ? dubbing.getThemeIds().stream().map(dubbingThemeMap::get).filter(StringUtils::isNotEmpty).collect(Collectors.toList()) : Collections.emptyList());
            result.setLevel(SafeConverter.toInt(dubbing.getDifficult()));
            result.setClazzLevel(ClazzLevel.getDescription(SafeConverter.toInt(dubbing.getDifficult())));
            result.setAlbumName(album != null ? album.getName() : "");
            result.setVideoSummary(dubbing.getVideoSummary());
            result.setProcessResultUrl("/exam/flash/newhomework/batch/processresult" + Constants.AntiHijackExt);
            result.setQuestionUrl(UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "videoId", dubbingId)));
            result.setCompletedUrl(UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions/answer" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "videoId", dubbingId)));
            result.setFinished(appAnswer != null && appAnswer.isFinished());

            Double scoreResult = null;
            if (appAnswer != null && MapUtils.isNotEmpty(appAnswer.getAnswers())) {
                // 计算8分制分数
                List<VacationHomeworkProcessResult> vacationHomeworkProcessResultList = new ArrayList<>();
                Set<String> questionIds = appAnswer.getAnswers().keySet();
                for (String questionId : questionIds) {
                    VacationHomeworkProcessResult vacationHomeworkProcessResult = homeworkProcessResultMap.get(questionId);
                    if (vacationHomeworkProcessResult != null) {
                        vacationHomeworkProcessResultList.add(vacationHomeworkProcessResult);
                    }
                }

                double totalScore = vacationHomeworkProcessResultList.stream().mapToDouble(processResult -> SafeConverter.toDouble(processResult.getActualScore())).sum();
                scoreResult = SafeConverter.toDouble(Math.floor(totalScore / vacationHomeworkProcessResultList.size()));
            }
            result.setScore(scoreResult);

            //判断该配音是否是欢快歌曲类型   欢快歌曲类型ID:DC_10300000140166
            DubbingCategory dubbingCategory = dubbingLoaderClient.loadDubbingCategoriesByIds(Collections.singleton(dubbing.getCategoryId())).get(dubbing.getCategoryId());
            result.setIsSong(dubbingCategory != null && Objects.equals("DC_10300000140166", dubbingCategory.getParentId()));
            resultList.add(result);
        }
        return resultList;
    }
}
