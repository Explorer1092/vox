package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.remote.DisableCacheMethod;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.core.helper.VoiceEngineTypeUtils;
import com.voxlearning.utopia.service.content.api.EnglishContentLoader;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.content.client.PracticeServiceClient;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkLivecastLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.UsTalkHomeworkData;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkProcessResultDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.UsTalkHomeworkDataPersistence;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkContentServiceImpl;
import com.voxlearning.utopia.service.question.api.QuestionLoader;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.Getter;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2016/9/12
 */
@Named
@Service(interfaceClass = NewHomeworkLivecastLoader.class)
@ExposeService(interfaceClass = NewHomeworkLivecastLoader.class)
public class NewHomeworkLivecastLoaderImpl implements NewHomeworkLivecastLoader {

//    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
//    @Inject private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
//    @Inject private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;
    @Inject private NewHomeworkContentServiceImpl newHomeworkContentService;

    @Inject private LiveCastHomeworkDao liveCastHomeworkDao;
    @Inject private LiveCastHomeworkResultDao liveCastHomeworkResultDao;
    @Inject private LiveCastHomeworkProcessResultDao liveCastHomeworkProcessResultDao;

    @Getter
    @ImportService(interfaceClass = QuestionLoader.class)
    private QuestionLoader questionLoader;

    @Getter
    @ImportService(interfaceClass = EnglishContentLoader.class)
    @DisableCacheMethod
    private EnglishContentLoader englishContentLoader;

    @Inject private PracticeServiceClient practiceServiceClient;

    @Override
    public MapMessage getHomeworkContent(Long teacherId, String unitId, String bookId) {
        TeacherDetail teacher = new TeacherDetail();
        teacher.setId(teacherId);
        teacher.setSubject(Subject.ENGLISH);
        return newHomeworkContentService.getHomeworkContent(teacher, null, null, unitId, bookId, ObjectiveConfigType.BASIC_APP, null);
    }

//    @Deprecated
//    @Override
//    public Map<String, NewHomework> loadNewHomeworksIncludeDisabled(Collection<String> ids) {
//        if (CollectionUtils.isEmpty(ids)) {
//            return Collections.emptyMap();
//        }
//
//        return newHomeworkLoader.loads(ids)
//                .values()
//                .stream()
//                .filter(o -> Objects.equals(o.getType().getTypeId(), NewHomeworkType.ThirdPartyType))
//                .collect(Collectors.toMap(NewHomework::getId, Function.identity()));
//    }
//
//    @Deprecated
//    @Override
//    public NewHomeworkResult loadNewHomeworkResult(NewHomework.Location location, Long userId) {
//        if (location == null || userId == null || !Objects.equals(location.getType().getTypeId(), NewHomeworkType.ThirdPartyType)) {
//            return null;
//        }
//        return newHomeworkResultLoader.loadNewHomeworkResult(location, userId, true);
//    }
//
//    @Deprecated
//    @Override
//    public Map<Long, NewHomeworkResult> loadNewHomeworkResult(NewHomework.Location location, Collection<Long> userIds, boolean needAnswer) {
//        if (location == null || CollectionUtils.isEmpty(userIds) || !Objects.equals(location.getType().getTypeId(), NewHomeworkType.ThirdPartyType)) {
//            return Collections.emptyMap();
//        }
//        return newHomeworkResultLoader.loadNewHomeworkResult(location, userIds, needAnswer);
//    }
//
//    @Deprecated
//    @Override
//    public List<NewHomeworkResult> loadNewHomeworkResult(Collection<NewHomework.Location> locations, Long userId) {
//        if (CollectionUtils.isEmpty(locations) || userId == null) {
//            return Collections.emptyList();
//        }
//
//        locations = locations.stream().filter(o -> Objects.equals(o.getType().getTypeId(), NewHomeworkType.ThirdPartyType)).collect(Collectors.toList());
//        return newHomeworkResultLoader.loadNewHomeworkResult(locations, userId);
//    }
//
//    @Override
//    @Deprecated
//    public Map<String, NewHomeworkProcessResult> loadNewHomeworkProcessResult(Collection<String> ids) {
//        if (CollectionUtils.isEmpty(ids)) {
//            return Collections.emptyMap();
//        }
//        return newHomeworkProcessResultLoader.loads(ids);
//    }
//
//    @Deprecated
//    @Override
//    public Map<String, NewHomeworkProcessResult> loadNewHomeworkProcessResult(String homeworkId, Collection<String> ids) {
//        if (CollectionUtils.isEmpty(ids)) {
//            return Collections.emptyMap();
//        }
//        return newHomeworkProcessResultLoader.loads(homeworkId, ids);
//    }

    @Override
    public Map<Long, String> loadCategoryName(Collection<Long> practiceIds) {
        return practiceIds.stream().collect(Collectors.toMap(e -> e, e -> practiceServiceClient.getPracticeBuffer().loadPractice(e).getCategoryName()));
    }

//    @Deprecated
//    @Override
//    public List<Map<String, Object>> loadNewHomeworkQuestionResult(Long studentId, String homeworkId, String categoryId, String lessonId) {
//        List<Map<String, Object>> questionInfo = new LinkedList<>();
//        Map<String, NewHomework> newHomeworkMap = loadNewHomeworksIncludeDisabled(Collections.singleton(homeworkId));
//        NewHomework newHomework = newHomeworkMap.get(homeworkId);
//        NewHomeworkResult newHomeworkResult = loadNewHomeworkResult(newHomework.toLocation(), studentId);
//        List<String> processIds = newHomeworkResult.findHomeworkProcessIdsForBaseAppByCategoryIdAndLessonId(categoryId, lessonId, ObjectiveConfigType.BASIC_APP);
//        Map<String, NewHomeworkProcessResult> processResultMap = loadNewHomeworkProcessResult(homeworkId, processIds);         //作业结果详情
//        Map<String, NewHomeworkProcessResult> dataInfo = processResultMap.values().stream().collect(Collectors.toMap(NewHomeworkProcessResult::getQuestionId, o -> o));
//        List<String> qIds = new ArrayList<>(dataInfo.keySet());
//        Map<String, NewQuestion> newQuestionMap = questionLoader.loadQuestionsIncludeDisabled(dataInfo.keySet());
//        List<Long> sentenceIds = newQuestionMap.values().stream().map(NewQuestion::getSentenceIds).flatMap(Collection::stream).collect(Collectors.toList());
//        Map<Long, Sentence> sentenceMap = englishContentLoader.loadEnglishSentences(sentenceIds);
//        List<NewHomeworkApp> apps = newHomework.findNewHomeworkApps(ObjectiveConfigType.BASIC_APP);
//        NewHomeworkApp target = null;
//        for (NewHomeworkApp o : apps) {
//            if (o.getCategoryId().toString().equals(categoryId) && o.getLessonId().equals(lessonId)) {
//                target = o;
//            }
//        }
//        PracticeType practiceType = Objects.nonNull(target) ? practiceServiceClient.getPracticeBuffer().loadPractice(target.getPracticeId()) : null;
//        for (String qId : qIds) {
//            Boolean answerInfo = null;
//            Map<String, Object> k = null;
//            String answerResultWord = null;
//            if (MapUtils.isNotEmpty(dataInfo)) {
//                //是否是口语题
//                if (practiceType != null && Objects.equals(practiceType.getNeedRecord(), Boolean.TRUE)) {
//                    NewHomeworkProcessResult n = dataInfo.get(qId);
//                    if (Objects.nonNull(n)) {
//                        int score = 0;
//                        AppOralScoreLevel appOralScoreLevel = n.getAppOralScoreLevel();
//                        if (appOralScoreLevel == null && Objects.nonNull(n.getScore())) {
//                            score = new BigDecimal(n.getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
//                        }
//                        String voiceUrl = CollectionUtils.isEmpty(n.getOralDetails()) || CollectionUtils.isEmpty(n.getOralDetails().get(0)) ? null : n.getOralDetails().get(0).get(0).getAudio();
//                        VoiceEngineType voiceEngineType = n.getVoiceEngineType();
//                        voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
//                        k = MiscUtils.m(
//                                "score", appOralScoreLevel == null ? (score + "分") : appOralScoreLevel.name(),
//                                "userId", n.getUserId(),
//                                "userVoiceUrl", voiceUrl,
//                                "voiceScoringMode", n.getVoiceScoringMode()
//                        );
//                    }
//                } else {
//                    NewHomeworkProcessResult ls = dataInfo.get(qId);
//                    if (Objects.nonNull(ls)) {
//                        boolean grasp = ls.getGrasp();
//                        answerInfo = grasp;
//                        if (grasp) {
//                            answerResultWord = "我答对了";
//                        } else {
//                            if (ls.getScore() > 0) {
//                                answerResultWord = "部分正确";
//                            } else {
//                                answerResultWord = "我答错了";
//                            }
//                        }
//                    }
//                }
//            }
//            NewQuestion newQuestion = newQuestionMap.get(qId);
//            List<Long> _sentenceIds = newQuestion.getSentenceIds();
//            List<Map<String, Object>> sentences = _sentenceIds.stream().map(l -> MiscUtils.m(
//                    "sentenceId", l,
//                    "sentenceContent", Objects.nonNull(sentenceMap.get(l)) ? sentenceMap.get(l).getEnText() : ""
//            )).collect(Collectors.toCollection(LinkedList::new));
//            questionInfo.add(MiscUtils.m(
//                    "questionId", qId,
//                    "answerResultWord", answerResultWord,
//                    "sentences", sentences,
//                    "answerInfo", answerInfo,
//                    "needRecord", practiceType != null && Objects.equals(practiceType.getNeedRecord(), Boolean.TRUE),
//                    "recordInfo", MapUtils.isEmpty(k) ? null : k
//            ));
//        }
//        return questionInfo;
//    }


    @Override
    public Map<String, LiveCastHomework> loadLiveCastHomeworkIncludeDisabled(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return liveCastHomeworkDao.loads(ids);
    }

    @Override
    public LiveCastHomeworkResult loadLiveCastHomeworkResult(LiveCastHomework.Location location, Long userId) {
        if (location == null || userId == null) {
            return null;
        }
        String month = MonthRange.newInstance(location.getCreateTime()).toString();
        LiveCastHomeworkResult.ID id = new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), userId);
        return liveCastHomeworkResultDao.load(id.toString());
    }

    @Override
    public Map<Long, LiveCastHomeworkResult> loadLiveCastHomeworkResult(LiveCastHomework.Location location, Collection<Long> userIds) {
        if (location == null || CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }

        String month = MonthRange.newInstance(location.getCreateTime()).toString();
        Set<String> ids = userIds.stream()
                .map(userId -> new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), userId).toString())
                .collect(Collectors.toSet());

        return liveCastHomeworkResultDao.loads(ids).values().stream()
                .collect(Collectors.toMap(LiveCastHomeworkResult::getUserId, Function.identity()));
    }

    @Override
    public List<LiveCastHomeworkResult> loadLiveCastHomeworkResult(Collection<LiveCastHomework.Location> locations, Long userId) {
        if (CollectionUtils.isEmpty(locations) || userId == null) {
            return Collections.emptyList();
        }
        Set<String> ids = new HashSet<>();
        locations.forEach(location -> {
            String month = MonthRange.newInstance(location.getCreateTime()).toString();
            ids.add(new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), userId).toString());
        });
        return new ArrayList<>(liveCastHomeworkResultDao.loads(ids).values());
    }

    @Override
    public Map<String, LiveCastHomeworkProcessResult> loadLiveCastHomeworkProcessResult(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return liveCastHomeworkProcessResultDao.loads(ids);
    }

    @Override
    public List<Map<String, Object>> loadUstalkHomeworkQuestionResult(Long studentId, String homeworkId, String categoryId, String lessonId) {
        List<Map<String, Object>> questionInfo = new LinkedList<>();
        Map<String, LiveCastHomework> homeworkMap = loadLiveCastHomeworkIncludeDisabled(Collections.singleton(homeworkId));
        LiveCastHomework homework = homeworkMap.get(homeworkId);
        LiveCastHomeworkResult homeworkResult = loadLiveCastHomeworkResult(homework.toLocation(), studentId);
        List<String> processIds = homeworkResult.findHomeworkProcessIdsForBaseAppByCategoryIdAndLessonId(categoryId, lessonId, ObjectiveConfigType.BASIC_APP);
        Map<String, LiveCastHomeworkProcessResult> processResultMap = loadLiveCastHomeworkProcessResult(processIds);
        Map<String, LiveCastHomeworkProcessResult> dataInfo = processResultMap.values().stream().collect(Collectors.toMap(LiveCastHomeworkProcessResult::getQuestionId, o -> o));
        List<String> qIds = new ArrayList<>(dataInfo.keySet());
        Map<String, NewQuestion> newQuestionMap = questionLoader.loadQuestionsIncludeDisabled(dataInfo.keySet());
        List<Long> sentenceIds = newQuestionMap.values().stream().map(NewQuestion::getSentenceIds).flatMap(Collection::stream).collect(Collectors.toList());
        Map<Long, Sentence> sentenceMap = englishContentLoader.loadEnglishSentences(sentenceIds);
        List<NewHomeworkApp> apps = homework.findNewHomeworkApps(ObjectiveConfigType.BASIC_APP);
        NewHomeworkApp target = null;
        for (NewHomeworkApp o : apps) {
            if (o.getCategoryId().toString().equals(categoryId) && o.getLessonId().equals(lessonId)) {
                target = o;
            }
        }
        PracticeType practiceType = Objects.nonNull(target) ? practiceServiceClient.getPracticeBuffer().loadPractice(target.getPracticeId()) : null;
        for (String qId : qIds) {
            Boolean answerInfo = null;
            Map<String, Object> k = null;
            String answerResultWord = null;
            if (MapUtils.isNotEmpty(dataInfo)) {
                //是否是口语题
                if (practiceType != null && Objects.equals(practiceType.getNeedRecord(), Boolean.TRUE)) {
                    LiveCastHomeworkProcessResult n = dataInfo.get(qId);
                    if (Objects.nonNull(n)) {
                        int score = 0;
                        AppOralScoreLevel appOralScoreLevel = n.getAppOralScoreLevel();
                        if (appOralScoreLevel == null && Objects.nonNull(n.getScore())) {
                            score = new BigDecimal(n.getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                        }
                        String voiceUrl = CollectionUtils.isEmpty(n.getOralDetails()) || CollectionUtils.isEmpty(n.getOralDetails().get(0)) ? null : n.getOralDetails().get(0).get(0).getAudio();
                        VoiceEngineType voiceEngineType = n.getVoiceEngineType();
                        voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                        k = MiscUtils.m(
                                "score", appOralScoreLevel == null ? (score + "分") : appOralScoreLevel.name(),
                                "userId", n.getUserId(),
                                "userVoiceUrl", voiceUrl,
                                "voiceScoringMode", n.getVoiceScoringMode()
                        );
                    }
                } else {
                    LiveCastHomeworkProcessResult ls = dataInfo.get(qId);
                    if (Objects.nonNull(ls)) {
                        boolean grasp = ls.getGrasp();
                        answerInfo = grasp;
                        if (grasp) {
                            answerResultWord = "我答对了";
                        } else {
                            if (ls.getScore() > 0) {
                                answerResultWord = "部分正确";
                            } else {
                                answerResultWord = "我答错了";
                            }
                        }
                    }
                }
            }
            NewQuestion newQuestion = newQuestionMap.get(qId);
            List<Long> _sentenceIds = newQuestion.getSentenceIds();
            List<Map<String, Object>> sentences = _sentenceIds.stream().map(l -> MiscUtils.m(
                    "sentenceId", l,
                    "sentenceContent", Objects.nonNull(sentenceMap.get(l)) ? sentenceMap.get(l).getEnText() : ""
            )).collect(Collectors.toCollection(LinkedList::new));
            questionInfo.add(MiscUtils.m(
                    "questionId", qId,
                    "answerResultWord", answerResultWord,
                    "sentences", sentences,
                    "answerInfo", answerInfo,
                    "needRecord", practiceType != null && Objects.equals(practiceType.getNeedRecord(), Boolean.TRUE),
                    "recordInfo", MapUtils.isEmpty(k) ? null : k
            ));
        }
        return questionInfo;
    }

    
    @Inject private UsTalkHomeworkDataPersistence usTalkHomeworkDataPersistence;

    @Override
    public List<UsTalkHomeworkData> findAllUsTalkHomeworkData() {
        return usTalkHomeworkDataPersistence.findAll();
    }
}
