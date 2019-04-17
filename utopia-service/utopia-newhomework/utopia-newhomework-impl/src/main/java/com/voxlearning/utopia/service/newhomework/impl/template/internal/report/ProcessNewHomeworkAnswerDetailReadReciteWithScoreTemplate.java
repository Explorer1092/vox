package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.core.helper.VoiceEngineTypeUtils;
import com.voxlearning.utopia.service.content.api.entity.ChineseSentence;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypeParameter;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypePartContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportPersonalRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.ReadReciteAppInfo;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.ReadReciteWithScoreAppPart;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.readrecitewithscore.*;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class ProcessNewHomeworkAnswerDetailReadReciteWithScoreTemplate extends ProcessNewHomeworkAnswerDetailCommonTemplate {

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.READ_RECITE_WITH_SCORE;
    }

    @Override
    public String processStudentPartTypeScore(NewHomework newHomework, NewHomeworkResultAnswer newHomeworkResultAnswer, ObjectiveConfigType type) {
        if (MapUtils.isEmpty(newHomeworkResultAnswer.getAppAnswers())) {
            return "";
        }
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (target == null) {
            return "";
        }
        Map<String, NewHomeworkApp> appMap = target.getApps().stream().collect(Collectors.toMap(NewHomeworkApp::getQuestionBoxId, Function.identity()));
        int appSize = newHomeworkResultAnswer.getAppAnswers().size();
        if (appSize == 1) {
            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().values().iterator().next();
            if (!appMap.containsKey(newHomeworkResultAppAnswer.getQuestionBoxId())) {
                return "";
            }
            NewHomeworkApp app = appMap.get(newHomeworkResultAppAnswer.getQuestionBoxId());
            if (CollectionUtils.isEmpty(app.getQuestions())) {
                return "";
            }
            double value = new BigDecimal(SafeConverter.toInt(newHomeworkResultAppAnswer.getStandardNum()) * 100).divide(new BigDecimal(app.getQuestions().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            if (value >= NewHomeworkConstants.READ_RECITE_STANDARD) {
                return "达标";
            } else {
                return "不达标";
            }
        } else {
            int standardNum = 0;
            for (NewHomeworkResultAppAnswer newHomeworkResultAppAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                if (!appMap.containsKey(newHomeworkResultAppAnswer.getQuestionBoxId())) {
                    continue;
                }
                NewHomeworkApp app = appMap.get(newHomeworkResultAppAnswer.getQuestionBoxId());
                if (CollectionUtils.isEmpty(app.getQuestions())) {
                    continue;
                }
                double value = new BigDecimal(SafeConverter.toInt(newHomeworkResultAppAnswer.getStandardNum()) * 100).divide(new BigDecimal(app.getQuestions().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                if (value >= NewHomeworkConstants.READ_RECITE_STANDARD) {
                    standardNum++;
                }
            }
            return standardNum + "/" + newHomeworkResultAnswer.getAppAnswers().size() + "达标";
        }

    }

    //按题查看部分各个类型模板接口==>pc
    public void processQuestionPartTypeInfo(Map<Long, NewHomeworkResult> newHomeworkResultMap, NewHomework newHomework, ObjectiveConfigType type, Map<ObjectiveConfigType, Object> result, String cdnBaseUrl) {
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap.values()
                .stream()
                .filter(o -> o.isFinishedOfObjectiveConfigType(type))
                .collect(Collectors.toList());
        Map<String, Object> typeResult = new LinkedHashMap<>();
        typeResult.put("type", type);
        typeResult.put("typeName", type.getValue());
        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            typeResult.put("finishCount", 0);
            result.put(type, typeResult);
            return;
        }
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        List<String> lessonIds = new LinkedList<>();
        Map<String, NewHomeworkApp> appMap = new LinkedHashMap<>();
        for (NewHomeworkApp app : target.getApps()) {
            lessonIds.add(app.getLessonId());
            appMap.put(app.getQuestionBoxId(), app);
        }
        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        Map<String, ReadReciteAppInfo> readReciteAppInfoMap = new LinkedHashMap<>();
        //用于朗读在前，背诵在后
        List<ReadReciteAppInfo> readAppInfoList = new LinkedList<>();
        List<ReadReciteAppInfo> reciteAppInfoList = new LinkedList<>();
        for (NewHomeworkApp app : target.getApps()) {
            if (!newBookCatalogMap.containsKey(app.getLessonId()))
                continue;
            NewBookCatalog newBookCatalog = newBookCatalogMap.get(app.getLessonId());
            ReadReciteAppInfo readReciteAppInfo = new ReadReciteAppInfo();
            readReciteAppInfo.setLessonId(app.getLessonId());
            readReciteAppInfo.setLessonName(newBookCatalog.getName());
            readReciteAppInfo.setQuestionBoxId(app.getQuestionBoxId());
            readReciteAppInfo.setQuestionBoxType(app.getQuestionBoxType());
            readReciteAppInfo.setQuestionBoxTypeName(app.getQuestionBoxType().getName());
            if (Objects.equals(app.getQuestionBoxType(), QuestionBoxType.READ)) {
                readAppInfoList.add(readReciteAppInfo);
            } else {
                reciteAppInfoList.add(readReciteAppInfo);
            }

            readReciteAppInfoMap.put(app.getQuestionBoxId(), readReciteAppInfo);
        }
        int finishCount = newHomeworkResults.size();
        for (NewHomeworkResult newHomeworkResult : newHomeworkResults) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
            if (newHomeworkResultAnswer != null) {
                for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : newHomeworkResultAnswer.getAppAnswers().entrySet()) {
                    if (!appMap.containsKey(entry.getKey()))
                        continue;
                    NewHomeworkApp app = appMap.get(entry.getKey());
                    if (CollectionUtils.isEmpty(app.getQuestions()))
                        continue;
                    if (!readReciteAppInfoMap.containsKey(entry.getKey()))
                        continue;
                    ReadReciteAppInfo readReciteAppInfo = readReciteAppInfoMap.get(entry.getKey());
                    double value = new BigDecimal(SafeConverter.toInt(entry.getValue().getStandardNum()) * 100).divide(new BigDecimal(app.getQuestions().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    if (value >= NewHomeworkConstants.READ_RECITE_STANDARD) {
                        //判断是否达标
                        readReciteAppInfo.setStandardNum(1 + readReciteAppInfo.getStandardNum());
                    }
                }
            }
        }
        typeResult.put("finishCount", finishCount);
        typeResult.put("appNum", appMap.size());
        readAppInfoList.addAll(reciteAppInfoList);
        typeResult.put("readReciteAppInfos", readAppInfoList);
        result.put(type, typeResult);
    }

    @Override
    public void processNewHomeworkAnswerDetail(ReportRateContext reportRateContext) {
        ObjectiveConfigType type = reportRateContext.getType();
        Map<String, NewHomeworkResult> newHomeworkResultMapToObjectiveConfigType = handlerNewHomeworkResultMap(reportRateContext.getNewHomeworkResultMap(), type);
        Map<Long, User> userMap = reportRateContext.getUserMap();
        if (newHomeworkResultMapToObjectiveConfigType.isEmpty()) {
            return;
        }
        NewHomeworkPracticeContent target = reportRateContext.getNewHomework().findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        ReadReciteWithScoreClazzData readReciteWithScoreClazzData = new ReadReciteWithScoreClazzData();
        Map<String, ReadReciteWithScoreClazzBasicData> readReciteWithScoreClazzBasicDataMap = new LinkedHashMap<>();
        handleChineseReadRecite(reportRateContext.getAllNewQuestionMap(), readReciteWithScoreClazzData, readReciteWithScoreClazzBasicDataMap, target);
        Map<String, NewHomeworkApp> appMap = target.getApps().stream().collect(Collectors.toMap(NewHomeworkApp::getQuestionBoxId, Function.identity()));
        for (NewHomeworkResult newHomeworkResult : newHomeworkResultMapToObjectiveConfigType.values()) {
            if (!userMap.containsKey(newHomeworkResult.getUserId()))
                continue;
            User user = userMap.get(newHomeworkResult.getUserId());
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
            for (String questionBoxId : newHomeworkResultAnswer.getAppAnswers().keySet()) {
                if (!readReciteWithScoreClazzBasicDataMap.containsKey(questionBoxId))
                    continue;
                ReadReciteWithScoreClazzBasicData basicData = readReciteWithScoreClazzBasicDataMap.get(questionBoxId);
                ReadReciteWithScoreClazzBasicData.User su = new ReadReciteWithScoreClazzBasicData.User();
                basicData.getUsers().add(su);
                NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(questionBoxId);
                int time = new BigDecimal(newHomeworkResultAppAnswer.processDuration())
                        .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                        .intValue();
                String duration = NewHomeworkUtils.handlerEnTime(time);
                su.setUserId(user.getId());
                su.setUserName(user.fetchRealnameIfBlankId());
                su.setDurationStr(duration);
                for (String pid : newHomeworkResultAppAnswer.getAnswers().values()) {
                    if (!reportRateContext.getNewHomeworkProcessResultMap().containsKey(pid))
                        continue;
                    NewHomeworkProcessResult p = reportRateContext.getNewHomeworkProcessResultMap().get(pid);
                    if (p == null)
                        continue;
                    if (CollectionUtils.isNotEmpty(p.getOralDetails())) {
                        su.getVoices().addAll(p.getOralDetails()
                                .stream()
                                .flatMap(Collection::stream)
                                .map(o -> VoiceEngineTypeUtils.getAudioUrl(o.getAudio(), p.getVoiceEngineType()))
                                .collect(Collectors.toList()));
                    }
                }
                if (appMap.containsKey(questionBoxId)) {
                    NewHomeworkApp app = appMap.get(questionBoxId);
                    if (CollectionUtils.isEmpty(app.getQuestions()))
                        continue;
                    double value = new BigDecimal(SafeConverter.toInt(newHomeworkResultAppAnswer.getStandardNum()) * 100).divide(new BigDecimal(app.getQuestions().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    if (value >= NewHomeworkConstants.READ_RECITE_STANDARD) {
                        //判断是否达标
                        su.setStandard(true);
                    }
                }
            }
        }
        reportRateContext.getResult().put(type.name(), readReciteWithScoreClazzData);
    }

    private void handleChineseReadRecite(Map<String, NewQuestion> allNewQuestionMap, ReadReciteWithScoreClazzData readReciteWithScoreClazzData, Map<String, ReadReciteWithScoreClazzBasicData> readReciteWithScoreClazzBasicDataMap, NewHomeworkPracticeContent target) {
        if (CollectionUtils.isEmpty(target.getApps()))
            return;
        List<NewHomeworkQuestion> questionList = target.processNewHomeworkQuestion(false);

        //key ==》自然段段落编号
        Map<String, Integer> qidToParagraph = new LinkedHashMap<>();
        List<Long> chineseSentenceIds = questionList
                .stream()
                .filter(o -> allNewQuestionMap.containsKey(o.getQuestionId()))
                .map(o -> allNewQuestionMap.get(o.getQuestionId()))
                .filter(Objects::nonNull)
                .filter(question -> CollectionUtils.isNotEmpty(question.getSentenceIds()))
                .map(o -> o.getSentenceIds().get(0))
                .collect(Collectors.toList());

        List<ChineseSentence> chineseSentences = chineseContentLoaderClient.loadChineseSentenceByIds(chineseSentenceIds);
        if (CollectionUtils.isNotEmpty(chineseSentences)) {
            Map<Long, ChineseSentence> mapChineseSentences = chineseSentences.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(AbstractDatabaseEntity::getId, Function.identity()));
            for (NewHomeworkQuestion question : questionList) {
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
            }
        }
        List<String> lessonIds = target.getApps()
                .stream()
                .filter(Objects::nonNull)
                .filter(o -> Objects.nonNull(o.getLessonId()))
                .map(NewHomeworkApp::getLessonId)
                .collect(Collectors.toList());
        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        for (NewHomeworkApp app : target.getApps()) {
            ReadReciteWithScoreClazzBasicData basicData = new ReadReciteWithScoreClazzBasicData();
            basicData.setLessonId(app.getLessonId());
            basicData.setQuestionBoxId(app.getQuestionBoxId());
            if (newBookCatalogMap.containsKey(app.getLessonId())) {
                NewBookCatalog newBookCatalog = newBookCatalogMap.get(app.getLessonId());
                basicData.setLessonName(newBookCatalog.getName());
            }
            if (CollectionUtils.isNotEmpty(app.getQuestions())) {
                for (NewHomeworkQuestion question : app.getQuestions()) {
                    if (qidToParagraph.containsKey(question.getQuestionId())) {
                        basicData.getParagraphs().add(qidToParagraph.get(question.getQuestionId()));
                    }
                }
            }
            readReciteWithScoreClazzBasicDataMap.put(app.getQuestionBoxId(), basicData);
            if (app.getQuestionBoxType() == QuestionBoxType.READ) {
                readReciteWithScoreClazzData.getReadData().add(basicData);
            } else {
                readReciteWithScoreClazzData.getReciteData().add(basicData);
            }
        }
    }

    @Override
    public void fetchNewHomeworkCommonObjectiveConfigTypePart(ObjectiveConfigTypePartContext context) {
        //****** begin 初始数据准备 *********//
        NewHomeworkPracticeContent target = context.getTarget();
        ObjectiveConfigType type = context.getType();
        NewHomework newHomework = context.getNewHomework();
        Map<Long, User> userMap = context.getUserMap();
        ObjectiveConfigTypeParameter parameter = context.getParameter();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = context.getNewHomeworkResultMap();
        NewHomeworkApp targetApp = null;
        for (NewHomeworkApp app : target.getApps()) {
            if (Objects.equals(app.getQuestionBoxId(), parameter.getQuestionBoxId())) {
                targetApp = app;
                break;
            }
        }
        if (targetApp == null) {
            MapMessage mapMessage = MapMessage.errorMessage("不存在对应朗读背诵部分" + parameter.getQuestionBoxId());
            context.setMapMessage(mapMessage);
            return;
        }
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        List<String> subHomeworkResultAnswerIds = new LinkedList<>();
        for (NewHomeworkQuestion newHomeworkQuestion : targetApp.getQuestions()) {
            NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomework.NewHomeworkQuestionObj(newHomework.getId(), type, Collections.singletonList(targetApp.getQuestionBoxId()), newHomeworkQuestion.getQuestionId());
            for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
                subHomeworkResultAnswerIds.add(newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId()));
            }
        }
        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);
        List<String> newHomeworkProcessResultIds = subHomeworkResultAnswerMap.values()
                .stream()
                .map(SubHomeworkResultAnswer::getProcessId)
                .collect(Collectors.toList());
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(context.getNewHomework().getId(), newHomeworkProcessResultIds);

        //****** end 初始数据准备 *********//


        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(Collections.singleton(targetApp.getLessonId()));
        ReadReciteWithScoreAppPart readReciteWithScoreAppPart = new ReadReciteWithScoreAppPart();
        readReciteWithScoreAppPart.setUserCount(newHomeworkResultMap.size());
        if (newBookCatalogMap.containsKey(targetApp.getLessonId())) {
            readReciteWithScoreAppPart.setLessonName(newBookCatalogMap.get(targetApp.getLessonId()).getName());
        }
        readReciteWithScoreAppPart.setQuestionBoxType(targetApp.getQuestionBoxType());
        readReciteWithScoreAppPart.setQuestionBoxTypeName(targetApp.getQuestionBoxType().getName());
        Map<Long, ReadReciteWithScoreAppPart.ReadReciteWithScoreAppPartUser> readReciteUserMap = new LinkedHashMap<>();

        //************* begin 学生完成部分信息 ********//
        for (NewHomeworkResult r : newHomeworkResultMap.values()) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = r.getPractices().get(type);
            if (newHomeworkResultAnswer.getAppAnswers() == null) continue;
            if (newHomeworkResultAnswer.getAppAnswers().containsKey(parameter.getQuestionBoxId())) {
                NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(parameter.getQuestionBoxId());
                ReadReciteWithScoreAppPart.ReadReciteWithScoreAppPartUser user = new ReadReciteWithScoreAppPart.ReadReciteWithScoreAppPartUser();
                readReciteUserMap.put(r.getUserId(), user);
                double value = new BigDecimal(SafeConverter.toInt(newHomeworkResultAppAnswer.getStandardNum()) * 100).divide(new BigDecimal(targetApp.getQuestions().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                if (value >= NewHomeworkConstants.READ_RECITE_STANDARD) {
                    user.setStandard(true);
                    readReciteWithScoreAppPart.setStandardUserCount(1 + readReciteWithScoreAppPart.getStandardUserCount());
                }
                int time = new BigDecimal(newHomeworkResultAppAnswer.processDuration())
                        .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                        .intValue();
                String duration = NewHomeworkUtils.handlerEnTime(time);
                user.setDuration(duration);
                user.setUserId(r.getUserId());
                user.setUserName(userMap.containsKey(r.getUserId()) ? userMap.get(r.getUserId()).fetchRealname() : "");
                readReciteWithScoreAppPart.getUsers().add(user);
            }
        }
        for (NewHomeworkProcessResult p : newHomeworkProcessResultMap.values()) {
            if (!readReciteUserMap.containsKey(p.getUserId()))
                continue;
            ReadReciteWithScoreAppPart.ReadReciteWithScoreAppPartUser user = readReciteUserMap.get(p.getUserId());
            if (CollectionUtils.isEmpty(p.getOralDetails()))
                continue;
            user.getVoices().addAll(p
                    .getOralDetails()
                    .stream()
                    .flatMap(Collection::stream)
                    .map(o -> VoiceEngineTypeUtils.getAudioUrl(o.getAudio(), p.getVoiceEngineType()))
                    .collect(Collectors.toList()));
        }
        //************* end 学生完成部分信息 ********//
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("readReciteWithScoreAppPart", readReciteWithScoreAppPart);
        context.setMapMessage(mapMessage);
    }

    @Override
    public void processNewHomeworkAnswerDetailPersonal(ReportPersonalRateContext reportRateContext) {
        ReadReciteWithScoreData readReciteData = new ReadReciteWithScoreData();
        ObjectiveConfigType type = reportRateContext.getType();
        Map<String, ReadReciteWithScoreBasicData> readReciteBasicDataMap = new HashMap<>();
        NewHomeworkPracticeContent target = reportRateContext.getNewHomework().findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        handleChineseReadRecite(target, reportRateContext.getAllNewQuestionMap(), readReciteData, readReciteBasicDataMap);
        NewHomeworkResultAnswer newHomeworkResultAnswer = reportRateContext.getNewHomeworkResult().getPractices().get(type);

        for (String questionBoxId : newHomeworkResultAnswer.getAppAnswers().keySet()) {
            if (!readReciteBasicDataMap.containsKey(questionBoxId))
                continue;
            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(questionBoxId);
            ReadReciteWithScoreBasicData readReciteWithScoreBasicData = readReciteBasicDataMap.get(questionBoxId);
            LinkedHashMap<String, String> answers = newHomeworkResultAppAnswer.getAnswers();
            if (MapUtils.isEmpty(answers))
                continue;
            double value = new BigDecimal(SafeConverter.toInt(newHomeworkResultAppAnswer.getStandardNum()) * 100).divide(new BigDecimal(newHomeworkResultAppAnswer.getAnswers().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            if (value >= NewHomeworkConstants.READ_RECITE_STANDARD) {
                readReciteWithScoreBasicData.setStandard(true);
            }
            for (ParagraphDetailed paragraphDetailed : readReciteWithScoreBasicData.getParagraphDetails()) {
                if (!answers.containsKey(paragraphDetailed.getQuestionId()))
                    continue;
                String pid = answers.get(paragraphDetailed.getQuestionId());
                if (!reportRateContext.getNewHomeworkProcessResultMap().containsKey(pid))
                    continue;
                NewHomeworkProcessResult newHomeworkProcessResult = reportRateContext.getNewHomeworkProcessResultMap().get(pid);
                List<String> voices = CollectionUtils.isNotEmpty(newHomeworkProcessResult.getOralDetails()) ?
                        newHomeworkProcessResult
                                .getOralDetails()
                                .stream()
                                .flatMap(Collection::stream)
                                .map(o -> VoiceEngineTypeUtils.getAudioUrl(o.getAudio(), newHomeworkProcessResult.getVoiceEngineType()))
                                .collect(Collectors.toList()) :
                        Collections.emptyList();
                int duration = new BigDecimal(SafeConverter.toLong(newHomeworkProcessResult.getDuration())).divide(new BigDecimal(1000), 0, BigDecimal.ROUND_HALF_UP).intValue();
                String durationStr = NewHomeworkUtils.handlerEnTime(duration);
                paragraphDetailed.setVoices(voices);
                paragraphDetailed.setStandard(SafeConverter.toBoolean(newHomeworkProcessResult.grasp));
                paragraphDetailed.setDuration(durationStr);
                if (CollectionUtils.isNotEmpty(newHomeworkProcessResult.getOralDetails())
                        && CollectionUtils.isNotEmpty(newHomeworkProcessResult.getOralDetails().get(0))
                        && newHomeworkProcessResult.getOralDetails().get(0).get(0) != null) {
                    paragraphDetailed.setSentences(newHomeworkProcessResult.getOralDetails().get(0).get(0).getSentences());
                } else {
                    paragraphDetailed.setSentences(Collections.emptyList());
                }
                paragraphDetailed.setVoiceEngineType(newHomeworkProcessResult.getVoiceEngineType());
                readReciteWithScoreBasicData.getVoices().addAll(voices);
            }
        }
        reportRateContext.getResultMap().put(type, readReciteData);
    }

    public void handleChineseReadRecite(NewHomeworkPracticeContent target, Map<String, NewQuestion> allNewQuestionMap, ReadReciteWithScoreData readReciteData, Map<String, ReadReciteWithScoreBasicData> readReciteBasicDataMap) {
        List<NewHomeworkQuestion> questionList = target.processNewHomeworkQuestion(false);

        //key ==》自然段段落编号
        Map<String, Integer> qidToParagraph = new LinkedHashMap<>();
        Map<String, Boolean> qidToDiffMap = new LinkedHashMap<>();
        List<Long> chineseSentenceIds = questionList
                .stream()
                .filter(o -> allNewQuestionMap.containsKey(o.getQuestionId()))
                .map(o -> allNewQuestionMap.get(o.getQuestionId()))
                .filter(Objects::nonNull)
                .filter(question -> CollectionUtils.isNotEmpty(question.getSentenceIds()))
                .map(o -> o.getSentenceIds().get(0))
                .collect(Collectors.toList());

        List<ChineseSentence> chineseSentences = chineseContentLoaderClient.loadChineseSentenceByIds(chineseSentenceIds);
        if (CollectionUtils.isNotEmpty(chineseSentences)) {
            Map<Long, ChineseSentence> mapChineseSentences = chineseSentences.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(AbstractDatabaseEntity::getId, Function.identity()));
            for (NewHomeworkQuestion question : questionList) {
                if (!allNewQuestionMap.containsKey(question.getQuestionId()))
                    continue;
                NewQuestion newQuestion = allNewQuestionMap.get(question.getQuestionId());
                if (CollectionUtils.isEmpty(newQuestion.getSentenceIds()))
                    continue;
                Long sentenceId = newQuestion.getSentenceIds().get(0);
                if (!mapChineseSentences.containsKey(sentenceId))
                    continue;
                ChineseSentence chineseSentence = mapChineseSentences.get(sentenceId);
                qidToDiffMap.put(question.getQuestionId(), chineseSentence.getReciteParagraph());
                qidToParagraph.put(question.getQuestionId(), chineseSentence.getParagraph());
            }
        }
        List<String> lessonIds = target.getApps()
                .stream()
                .filter(Objects::nonNull)
                .filter(o -> Objects.nonNull(o.getLessonId()))
                .map(NewHomeworkApp::getLessonId)
                .collect(Collectors.toList());
        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        for (NewHomeworkApp newHomeworkApp : target.getApps()) {
            ReadReciteWithScoreBasicData readReciteBasicData = new ReadReciteWithScoreBasicData();
            readReciteBasicData.setQuestionBoxId(newHomeworkApp.getQuestionBoxId());
            if (newBookCatalogMap.containsKey(newHomeworkApp.getLessonId())) {
                //兼容数据Alias和name
                NewBookCatalog newBookCatalog = newBookCatalogMap.get(newHomeworkApp.getLessonId());
                String lessonName = newBookCatalog.getAlias();
                if (StringUtils.isBlank(lessonName)) {
                    lessonName = newBookCatalog.getName();
                }
                readReciteBasicData.setLessonName(lessonName);
            }
            List<NewHomeworkQuestion> questions = newHomeworkApp.getQuestions();

            //包里面的各个自然段
            for (NewHomeworkQuestion newHomeworkQuestion : questions) {
                //一个自然段
                ParagraphDetailed paragraphDetailed = new ParagraphDetailed();
                //各个字段段
                readReciteBasicData.getParagraphDetails().add(paragraphDetailed);
                paragraphDetailed.setQuestionId(newHomeworkQuestion.getQuestionId());
                paragraphDetailed.setParagraphOrder(SafeConverter.toInt(qidToParagraph.get(newHomeworkQuestion.getQuestionId())));
                paragraphDetailed.setParagraphDifficultyType(SafeConverter.toBoolean(qidToDiffMap.get(newHomeworkQuestion.getQuestionId())));
            }
            if (newHomeworkApp.getQuestionBoxType() == QuestionBoxType.READ) {
                readReciteData.getReadData().add(readReciteBasicData);
            } else {
                readReciteData.getReciteData().add(readReciteBasicData);
            }
            readReciteBasicDataMap.put(newHomeworkApp.getQuestionBoxId(), readReciteBasicData);
        }
    }

}
