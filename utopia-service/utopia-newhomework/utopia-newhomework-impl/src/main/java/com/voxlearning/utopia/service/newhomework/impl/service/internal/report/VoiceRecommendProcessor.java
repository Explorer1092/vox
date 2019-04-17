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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.report;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.ScoreLevel;
import com.voxlearning.utopia.core.helper.VoiceEngineTypeUtils;
import com.voxlearning.utopia.service.content.api.entity.ChineseSentence;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.*;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/8/8
 */
@Named
public class VoiceRecommendProcessor extends NewHomeworkSpringBean {

    @Inject private RaikouSDK raikouSDK;

    public void recommendExcellentVoices(Map<Long, User> userMap, NewHomework newHomework, Collection<NewHomeworkResult> newHomeworkResults, MapMessage mapMessage, String cdnUrl) {
        VoiceRecommend voiceRecommend = voiceRecommendDao.load(newHomework.getId());
        boolean hasRecommend = false;
        if (newHomework.getSubject().equals(Subject.ENGLISH)) {
            if (voiceRecommend == null || CollectionUtils.isEmpty(voiceRecommend.getRecommendVoiceList())) {
                Comparator<BasicAppVoiceResult> comparator = (o1, o2) -> Integer.compare(o2.getScore(), o1.getScore());
                comparator = comparator.thenComparing((o1, o2) -> Integer.compare(o2.getMacScore(), o1.getMacScore()));
                comparator = comparator.thenComparing(BasicAppVoiceResult::getStudentName);
                List<BasicAppVoiceResult> voiceResults = loadAllVoiceResults(userMap, newHomework, newHomeworkResults)
                        .stream()
                        .sorted(comparator)
                        .limit(3)
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(voiceResults)) {
                    mapMessage.add("voiceList", voiceResults);
                }
            } else {
                hasRecommend = true;
                List<VoiceRecommend.RecommendVoice> recommendVoices = voiceRecommend.getRecommendVoiceList();
                List<Map<String, Object>> recommendedVoices = recommendVoices.stream()
                        .filter(Objects::nonNull)
                        .map(recommendVoice -> MapUtils.m(
                                "studentName", recommendVoice.getStudentName(),
                                "categoryName", recommendVoice.getCategoryName(),
                                "studentImgUrl", NewHomeworkUtils.getUserAvatarImgUrl(cdnUrl, userMap.get(recommendVoice.getStudentId()) != null ? userMap.get(recommendVoice.getStudentId()).fetchImageUrl() : ""),
                                "voiceList", recommendVoice.getVoiceList()
                        ))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(recommendedVoices)) {
                    mapMessage.add("voiceList", recommendedVoices);
                }
            }
        } else if (newHomework.getSubject().equals(Subject.CHINESE)) {
            if (voiceRecommend == null || CollectionUtils.isEmpty(voiceRecommend.getReadReciteVoices())) {
                Comparator<ReadReciteVoiceResult> comparator = (o1, o2) -> Boolean.compare(o2.getKeyPointParagraph(), o1.getKeyPointParagraph());
                comparator = comparator.thenComparing((o1, o2) -> Integer.compare(o2.getScore(), o1.getScore()));
                List<ReadReciteVoiceResult> voiceResults = loadReadReciteVoiceResults(userMap, newHomework, newHomeworkResults);
                voiceResults = voiceResults.stream()
                        .filter(e -> e.getKeyPointParagraph() != null)
                        .filter(e -> e.getScore() != null)
                        .filter(e -> e.getScore() >= 6)
                        .sorted(comparator)
                        .limit(6)
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(voiceResults)) {
                    mapMessage.add("voiceList", voiceResults);
                }
            } else {
                hasRecommend = true;
                List<VoiceRecommend.ReadReciteVoice> readReciteVoices = voiceRecommend.getReadReciteVoices();
                List<Map<String, Object>> recommendVoices = readReciteVoices.stream()
                        .filter(Objects::nonNull)
                        .map(readReciteVoice -> MapUtils.m(
                                "studentId", readReciteVoice.getStudentId(),
                                "studentName", readReciteVoice.getStudentName(),
                                "studentImgUrl", NewHomeworkUtils.getUserAvatarImgUrl(cdnUrl, userMap.get(readReciteVoice.getStudentId()) != null ? userMap.get(readReciteVoice.getStudentId()).fetchImageUrl() : ""),
                                "lessonName", readReciteVoice.getLessonName(),
                                "paragraph", readReciteVoice.getParagraph(),
                                "type", readReciteVoice.getType(),
                                "voice", readReciteVoice.getVoice()
                        )).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(recommendVoices)) {
                    mapMessage.add("voiceList", recommendVoices);
                }
            }
        }
        mapMessage.add("hasRecommend", hasRecommend);
        mapMessage.add("voiceExpired", DateUtils.dayDiff(new Date(), newHomework.getCreateAt()) > 30);
    }

    MapMessage loadAllVoices(Map<Long, User> userMap, NewHomework newHomework, Collection<NewHomeworkResult> newHomeworkResults) {
        List<BasicAppVoiceResult> voiceResults = loadAllVoiceResults(userMap, newHomework, newHomeworkResults);
        Map<String, List<BasicAppVoiceResult>> categoryVoiceMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(voiceResults)) {
            Comparator<BasicAppVoiceResult> comparator = (o1, o2) -> Double.compare(o2.getScore(), o1.getScore());
            comparator = comparator.thenComparing((o1, o2) -> Integer.compare(o2.getMacScore(), o1.getMacScore()));
            comparator = comparator.thenComparing(BasicAppVoiceResult::getStudentName);
            voiceResults.sort(comparator);
            for (BasicAppVoiceResult voiceResult : voiceResults) {
                String key = StringUtils.join(Arrays.asList(voiceResult.getLessonId(), voiceResult.getCategoryName()), "|");
                List<BasicAppVoiceResult> voiceResultList = categoryVoiceMap.containsKey(key) ? categoryVoiceMap.get(key) : new ArrayList<>();
                voiceResultList.add(voiceResult);
                categoryVoiceMap.put(key, voiceResultList);
            }
        }
        String clazzName = "";
        Long groupId = newHomework.getClazzGroupId();
        GroupMapper groupMapper = groupLoaderClient.loadGroup(groupId, false);
        if (groupMapper != null) {
            Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(groupMapper.getClazzId());
            if (clazz != null) {
                clazzName = clazz.formalizeClazzName();
            }
        }
        MapMessage message = MapMessage.successMessage();
        message.add("clazzName", clazzName);
        message.add("categoryVoiceMap", categoryVoiceMap);
        message.add("voiceExpired", DateUtils.dayDiff(new Date(), newHomework.getCreateAt()) > 30);
        return message;
    }

    /**
     * 单次作业所有学生基础练习语音列表
     */
    private List<BasicAppVoiceResult> loadAllVoiceResults(Map<Long, User> userMap, NewHomework newHomework, Collection<NewHomeworkResult> newHomeworkResults) {
        if (MapUtils.isEmpty(userMap) || CollectionUtils.isEmpty(newHomeworkResults)) {
            return Collections.emptyList();
        }
        List<NewHomeworkApp> newHomeworkAppList = new ArrayList<>();
        for (ObjectiveConfigType objectiveConfigType : NewHomeworkConstants.VoiceRecommendSupportedTypes) {
            newHomeworkAppList.addAll(newHomework.findNewHomeworkApps(objectiveConfigType));
        }
        if (CollectionUtils.isEmpty(newHomeworkAppList)) {
            return Collections.emptyList();
        }
        List<Long> homeworkPracticeTypeList = newHomeworkAppList.stream()
                .filter(newHomeworkApp -> newHomeworkApp.getPracticeId() != null)
                .map(NewHomeworkApp::getPracticeId)
                .collect(Collectors.toList());
        List<PracticeType> subjectPracticeTypeList = practiceLoaderClient.loadSubjectPractices(Subject.ENGLISH);
        Set<Integer> needRecordCategoryIdSet = subjectPracticeTypeList.stream()
                .filter(practiceType -> homeworkPracticeTypeList.contains(practiceType.getId()))
                .filter(PracticeType::fetchNeedRecord)
                .map(PracticeType::getCategoryId)
                .collect(Collectors.toSet());
        // 是否有需要录音的应用
        boolean hasNeedRecord = needRecordCategoryIdSet.size() > 0;
        if (hasNeedRecord) {
            // 获取所有的answerId
            Set<String> subHomeworkResultAnswerIds = new LinkedHashSet<>();
            Map<ObjectiveConfigType, List<NewHomework.NewHomeworkQuestionObj>> newHomeworkQuestionObjMap = new HashMap<>();
            for (NewHomeworkPracticeContent practiceContent : newHomework.getPractices()) {
                ObjectiveConfigType objectiveConfigType = practiceContent.getType();
                if (NewHomeworkConstants.VoiceRecommendSupportedTypes.contains(objectiveConfigType)) {
                    for (NewHomeworkApp app : practiceContent.getApps()) {
                        // 只取需要录音的应用
                        if (needRecordCategoryIdSet.contains(app.getCategoryId())) {
                            for (NewHomeworkQuestion question : app.getQuestions()) {
                                NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomework
                                        .NewHomeworkQuestionObj(newHomework.getId(), practiceContent.getType(), Arrays.asList(SafeConverter.toString(app.getCategoryId()), app.getLessonId()), question.getQuestionId());
                                newHomeworkQuestionObjMap.computeIfAbsent(objectiveConfigType, k -> new ArrayList<>()).add(newHomeworkQuestionObj);
                            }
                        }
                    }
                }
            }

            String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
            for (NewHomeworkResult newHomeworkResult : newHomeworkResults) {
                LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = newHomeworkResult.getPractices();
                if (MapUtils.isNotEmpty(practices)) {
                    for (Map.Entry<ObjectiveConfigType, NewHomeworkResultAnswer> entry : practices.entrySet()) {
                        if (newHomeworkQuestionObjMap.containsKey(entry.getKey()) && entry.getValue().isFinished()) {
                            for (NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj : newHomeworkQuestionObjMap.get(entry.getKey())) {
                                String answerId = newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId());
                                subHomeworkResultAnswerIds.add(answerId);
                            }
                        }
                    }
                }
            }
            Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);
            Set<String> processIds = subHomeworkResultAnswerMap.values().stream().map(SubHomeworkResultAnswer::getProcessId).collect(Collectors.toSet());
            Map<String, NewHomeworkProcessResult> allProcessResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);

            Map<String, NewHomeworkProcessResult> answerIdProcessResultMap = new LinkedHashMap<>();
            for (String answerId : subHomeworkResultAnswerIds) {
                if (subHomeworkResultAnswerMap.containsKey(answerId)) {
                    String processId = subHomeworkResultAnswerMap.get(answerId).getProcessId();
                    if (allProcessResultMap.containsKey(processId)) {
                        answerIdProcessResultMap.put(answerId, allProcessResultMap.get(processId));
                    }
                }
            }

            StudentDetail student = studentLoaderClient.loadStudentDetail(userMap.values().iterator().next().getId());
            boolean needScoreLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(student, "ShowScoreLevel", "WhiteList");
            return newHomeworkResults
                    .stream()
                    .map(newHomeworkResult -> buildBasicAppVoiceResultList(needScoreLevel, newHomeworkResult, userMap, newHomework, needRecordCategoryIdSet, answerIdProcessResultMap))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 单个学生基础练习语音列表
     */
    private List<BasicAppVoiceResult> buildBasicAppVoiceResultList(boolean needScoreLevel, NewHomeworkResult newHomeworkResult, Map<Long, User> userMap, NewHomework newHomework,
                                                                   Set<Integer> needRecordCategoryIdSet, Map<String, NewHomeworkProcessResult> answerIdProcessResultMap) {
        List<BasicAppVoiceResult> voiceResultList = new ArrayList<>();
        if (newHomeworkResult != null && MapUtils.isNotEmpty(newHomeworkResult.getPractices()) && userMap.get(newHomeworkResult.getUserId()) != null) {
            String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
            LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = newHomeworkResult.getPractices();
            for (ObjectiveConfigType objectiveConfigType : NewHomeworkConstants.VoiceRecommendSupportedTypes) {
                if (practices.get(objectiveConfigType) != null && practices.get(objectiveConfigType).isFinished()) {
                    // 作业内容
                    List<NewHomeworkApp> newHomeworkApps = newHomework.findNewHomeworkApps(objectiveConfigType);
                    // 学生答案
                    Map<String, NewHomeworkResultAppAnswer> appAnswers = practices.get(objectiveConfigType).getAppAnswers();
                    if (CollectionUtils.isNotEmpty(newHomeworkApps) && MapUtils.isNotEmpty(appAnswers)) {
                        for (NewHomeworkApp newHomeworkApp : newHomeworkApps) {
                            // app类 <{categoryId-lessonId}, NewHomeworkResultAppAnswer>
                            String appAnswerKey = newHomeworkApp.getCategoryId() + "-" + newHomeworkApp.getLessonId();
                            NewHomeworkResultAppAnswer appAnswer = appAnswers.get(appAnswerKey);
                            // 只取需要录音的应用
                            if (appAnswer != null && appAnswer.isFinished() && needRecordCategoryIdSet.contains(appAnswer.getCategoryId())) {
                                boolean hasLowScoreVoice = false;
                                List<String> voiceList = new ArrayList<>();
                                List<Integer> macScoreList = new ArrayList<>();
                                int totalMacScore = 0;
                                for (NewHomeworkQuestion question : newHomeworkApp.getQuestions()) {
                                    NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomework
                                            .NewHomeworkQuestionObj(newHomework.getId(), objectiveConfigType, Arrays.asList(SafeConverter.toString(newHomeworkApp.getCategoryId()), newHomeworkApp.getLessonId()), question.getQuestionId());
                                    String answerId = newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId());
                                    NewHomeworkProcessResult processResult = answerIdProcessResultMap.get(answerId);
                                    if (processResult != null && processResult.hasRecord()
                                            && CollectionUtils.isNotEmpty(processResult.getOralDetails())
                                            && CollectionUtils.isNotEmpty(processResult.getOralDetails().get(0))) {
                                        if (SafeConverter.toDouble(processResult.getScore()) < 60) {
                                            hasLowScoreVoice = true;
                                        }
                                        NewHomeworkProcessResult.OralDetail oralDetail = processResult.getOralDetails().get(0).get(0);
                                        if (oralDetail != null) {
                                            String voiceUrl = VoiceEngineTypeUtils.getAudioUrl(oralDetail.getAudio(), processResult.getVoiceEngineType());
                                            if (StringUtils.isNoneBlank(voiceUrl)) {
                                                voiceList.add(voiceUrl);
                                                Integer macScore = oralDetail.getMacScore();
                                                if (macScore != null) {
                                                    macScoreList.add(macScore);
                                                    totalMacScore += macScore;
                                                }
                                            }
                                        }
                                    }
                                }
                                PracticeType practiceType = practiceLoaderClient.loadPractice(appAnswer.getPracticeId());
                                if (CollectionUtils.isNotEmpty(voiceList) && appAnswer.getScore() != null && practiceType != null) {
                                    BasicAppVoiceResult basicAppVoiceResult = new BasicAppVoiceResult();
                                    basicAppVoiceResult.setStudentId(newHomeworkResult.getUserId());
                                    basicAppVoiceResult.setStudentName(userMap.get(newHomeworkResult.getUserId()).fetchRealname());
                                    basicAppVoiceResult.setLessonId(appAnswer.getLessonId());
                                    basicAppVoiceResult.setCategoryId(practiceType.getCategoryId());
                                    basicAppVoiceResult.setCategoryName(practiceType.getCategoryName());
                                    basicAppVoiceResult.setVoiceList(voiceList);
                                    basicAppVoiceResult.setScore(new BigDecimal(appAnswer.getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
                                    int macScore = 0;
                                    if (macScoreList.size() > 0) {
                                        macScore = new BigDecimal(totalMacScore).divide(new BigDecimal(macScoreList.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                                    }
                                    basicAppVoiceResult.setMacScore(macScore);
                                    basicAppVoiceResult.setScoreStr(needScoreLevel ? ScoreLevel.processLevel(basicAppVoiceResult.getScore()).getLevel() : SafeConverter.toInt(basicAppVoiceResult.getScore()) + "分");
                                    basicAppVoiceResult.setHasLowScoreVoice(hasLowScoreVoice);
                                    voiceResultList.add(basicAppVoiceResult);
                                }
                            }
                        }
                    }
                }
            }
        }
        return voiceResultList;
    }

    private List<ReadReciteVoiceResult> loadReadReciteVoiceResults(Map<Long, User> userMap, NewHomework newHomework, Collection<NewHomeworkResult> newHomeworkResults) {
        if (MapUtils.isEmpty(userMap) || CollectionUtils.isEmpty(newHomeworkResults)) {
            return Collections.emptyList();
        }
        List<NewHomeworkApp> newHomeworkAppList = newHomework.findNewHomeworkApps(ObjectiveConfigType.READ_RECITE_WITH_SCORE);
        if (CollectionUtils.isEmpty(newHomeworkAppList)) {
            return Collections.emptyList();
        }

        List<String> questionIds = new ArrayList<>();
        List<NewHomework.NewHomeworkQuestionObj> newHomeworkQuestionObjs = new ArrayList<>();
        //初始化lesson数据
        List<String> lessonIds = new ArrayList<>();
        for (NewHomeworkApp newHomeworkApp : newHomeworkAppList) {
            lessonIds.add(newHomeworkApp.getLessonId());
            for (NewHomeworkQuestion question : newHomeworkApp.getQuestions()) {
                questionIds.add(question.getQuestionId());
                NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomework.NewHomeworkQuestionObj(
                        newHomework.getId(),
                        ObjectiveConfigType.READ_RECITE_WITH_SCORE,
                        Collections.singletonList(SafeConverter.toString(newHomeworkApp.getQuestionBoxId())),
                        question.getQuestionId());
                newHomeworkQuestionObjs.add(newHomeworkQuestionObj);
            }
        }
        Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);

        // 获取所有的answerId
        Set<String> subHomeworkResultAnswerIds = new LinkedHashSet<>();
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        for (NewHomeworkResult newHomeworkResult : newHomeworkResults) {
            if (newHomeworkResult.getPractices() == null) continue;
            NewHomeworkResultAnswer resultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.READ_RECITE_WITH_SCORE);
            if (resultAnswer != null && resultAnswer.isFinished()) {
                for (NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj : newHomeworkQuestionObjs) {
                    String answerId = newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId());
                    subHomeworkResultAnswerIds.add(answerId);
                }
            }
        }
        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);
        Set<String> processIds = subHomeworkResultAnswerMap.values().stream().map(SubHomeworkResultAnswer::getProcessId).collect(Collectors.toSet());
        Map<String, NewHomeworkProcessResult> allProcessResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);

        Map<String, NewHomeworkProcessResult> answerIdProcessResultMap = new LinkedHashMap<>();
        for (String answerId : subHomeworkResultAnswerIds) {
            if (subHomeworkResultAnswerMap.containsKey(answerId)) {
                String processId = subHomeworkResultAnswerMap.get(answerId).getProcessId();
                if (allProcessResultMap.containsKey(processId)) {
                    answerIdProcessResultMap.put(answerId, allProcessResultMap.get(processId));
                }
            }
        }


        //初始化每道题对应的段落和是否是重点段落属性
        Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);

        List<Long> chineseSentenceIds = newQuestionMap.values()
                .stream()
                .filter(question -> CollectionUtils.isNotEmpty(question.getSentenceIds()))
                .map(o -> o.getSentenceIds().get(0))
                .collect(Collectors.toList());
        Map<Long, ChineseSentence> chineseSentenceMap = chineseContentLoaderClient.loadChineseSentenceByIds(chineseSentenceIds)
                .stream().collect(Collectors.toMap(ChineseSentence::getId, Function.identity()));

        //句子重点段落
        Map<String, Boolean> qIdReciteParagraph = new HashMap<>();
        //句子所在章节的段落号
        Map<String, Integer> qIdParagraph = new HashMap<>();

        for (NewQuestion newQuestion : newQuestionMap.values()) {
            String qid = newQuestion.getId();
            if (CollectionUtils.isEmpty(newQuestion.getSentenceIds()))
                continue;
            Long cid = newQuestion.getSentenceIds().get(0);
            if (!chineseSentenceMap.containsKey(cid))
                continue;
            ChineseSentence chineseSentence = chineseSentenceMap.get(cid);
            qIdParagraph.put(qid, chineseSentence.getParagraph());
            qIdReciteParagraph.put(qid, chineseSentence.getReciteParagraph());
        }

        return newHomeworkResults
                .stream()
                .map(newHomeworkResult -> buildReadReciteVoiceResultList(newHomeworkResult,
                        userMap,
                        newHomework,
                        answerIdProcessResultMap,
                        lessonMap,
                        qIdReciteParagraph,
                        qIdParagraph))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }


    /**
     * 单个学生课文读背语音列表
     */
    private List<ReadReciteVoiceResult> buildReadReciteVoiceResultList(NewHomeworkResult newHomeworkResult,
                                                                       Map<Long, User> userMap,
                                                                       NewHomework newHomework,
                                                                       Map<String, NewHomeworkProcessResult> answerIdProcessResultMap,
                                                                       Map<String, NewBookCatalog> lessonMap,
                                                                       Map<String, Boolean> qIdReciteParagraph,
                                                                       Map<String, Integer> qIdParagraph) {
        List<ReadReciteVoiceResult> readReciteVoiceResults = new ArrayList<>();
        if (newHomeworkResult != null && MapUtils.isNotEmpty(newHomeworkResult.getPractices()) && userMap.get(newHomeworkResult.getUserId()) != null) {
            String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
            LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = newHomeworkResult.getPractices();


            for (ObjectiveConfigType objectiveConfigType : NewHomeworkConstants.VoiceRecommendSupportedTypes) {
                if (practices.get(objectiveConfigType) != null && practices.get(objectiveConfigType).isFinished()) {
                    // 作业内容
                    List<NewHomeworkApp> newHomeworkApps = newHomework.findNewHomeworkApps(objectiveConfigType);
                    // 学生答案
                    Map<String, NewHomeworkResultAppAnswer> appAnswers = practices.get(objectiveConfigType).getAppAnswers();
                    if (CollectionUtils.isNotEmpty(newHomeworkApps) && MapUtils.isNotEmpty(appAnswers)) {
                        for (NewHomeworkApp newHomeworkApp : newHomeworkApps) {
                            // app类 <{questionBoxId}, NewHomeworkResultAppAnswer>
                            String appAnswerKey = newHomeworkApp.getQuestionBoxId();
                            NewHomeworkResultAppAnswer appAnswer = appAnswers.get(appAnswerKey);
                            if (appAnswer != null && appAnswer.isFinished()) {
                                for (NewHomeworkQuestion question : newHomeworkApp.getQuestions()) {
                                    ReadReciteVoiceResult readReciteVoiceResult = new ReadReciteVoiceResult();
                                    NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomework.NewHomeworkQuestionObj(
                                            newHomework.getId(),
                                            objectiveConfigType,
                                            Collections.singletonList(newHomeworkApp.getQuestionBoxId()),
                                            question.getQuestionId());
                                    String answerId = newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId());
                                    NewHomeworkProcessResult processResult = answerIdProcessResultMap.get(answerId);
                                    if (processResult != null
                                            && CollectionUtils.isNotEmpty(processResult.getOralDetails())
                                            && CollectionUtils.isNotEmpty(processResult.getOralDetails().get(0))) {
                                        NewHomeworkProcessResult.OralDetail oralDetail = processResult.getOralDetails().get(0).get(0);
                                        if (oralDetail != null) {
                                            String voiceUrl = VoiceEngineTypeUtils.getAudioUrl(oralDetail.getAudio(), processResult.getVoiceEngineType());
                                            readReciteVoiceResult.setVoice(voiceUrl);
                                            // 7分制得分
                                            Integer standardScore = oralDetail.getStandardScore();
                                            readReciteVoiceResult.setScore(standardScore);
                                        }
                                        readReciteVoiceResult.setMacScore(processResult.getActualScore());
                                        readReciteVoiceResult.setKeyPointParagraph(SafeConverter.toBoolean(qIdReciteParagraph.get(processResult.getQuestionId())));
                                        readReciteVoiceResult.setParagraph("第" + SafeConverter.toInt(qIdParagraph.get(processResult.getQuestionId())) + "段");
                                    }
                                    readReciteVoiceResult.setStudentId(newHomeworkResult.getUserId());
                                    readReciteVoiceResult.setStudentName(userMap.get(newHomeworkResult.getUserId()).fetchRealname());
                                    readReciteVoiceResult.setLessonId(appAnswer.getLessonId());
                                    String lessonName = "";
                                    if (MapUtils.isNotEmpty(lessonMap)) {
                                        if (lessonMap.get(appAnswer.getLessonId()) != null) {
                                            lessonName = lessonMap.get(appAnswer.getLessonId()).getName();
                                        }
                                    }
                                    readReciteVoiceResult.setLessonName(lessonName);
                                    readReciteVoiceResult.setType(appAnswer.getQuestionBoxType());
                                    readReciteVoiceResults.add(readReciteVoiceResult);
                                }
                            }
                        }
                    }
                }
            }
        }
        return readReciteVoiceResults;
    }
}
