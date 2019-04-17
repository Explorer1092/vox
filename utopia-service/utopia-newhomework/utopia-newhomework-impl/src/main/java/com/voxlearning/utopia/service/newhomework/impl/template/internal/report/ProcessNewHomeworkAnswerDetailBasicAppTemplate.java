package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;


import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.core.helper.VoiceEngineTypeUtils;
import com.voxlearning.utopia.service.content.api.constant.PracticeCategory;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalogAncestor;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.newhomework.api.constant.NatureSpellingType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypeParameter;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypePartContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportPersonalRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.BasicAppQuestion;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.BasicAppStudentDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.rate.BasicAppUnitPart;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.rate.ContentValueForQuestion;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.rate.PersonalDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.rate.StudentPersonalInfo;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class ProcessNewHomeworkAnswerDetailBasicAppTemplate extends ProcessNewHomeworkAnswerDetailCommonTemplate {

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.BASIC_APP;
    }

    @Override
    public void processNewHomeworkAnswerDetail(ReportRateContext reportRateContext) {
        List values;
        if (reportRateContext.isPcWay()) {
            //Pc接口
            values = internalProcessHomeworkAnswerDetailForBasicAppIsPcWay(reportRateContext.getType(), reportRateContext.getAllNewQuestionMap(), reportRateContext.getNewHomeworkProcessResultMap(), reportRateContext.getNewHomeworkResultMap(), reportRateContext.getNewHomework(), reportRateContext.getUserMap());
        } else {
            //非pc接口
            values = newInternalProcessHomeworkAnswerDetailForBasicAppIsNotPcWay(reportRateContext.getType(), reportRateContext.getNewHomeworkResultMap(), reportRateContext.getNewHomework());
        }
        if (CollectionUtils.isNotEmpty(values)) {
            reportRateContext.getResult().put(getObjectiveConfigType().name(), values);
        }
    }

    @Override
    public void processNewHomeworkAnswerDetailPersonal(ReportPersonalRateContext reportRateContext) {
        ObjectiveConfigType type = reportRateContext.getType();
        NewHomework newHomework = reportRateContext.getNewHomework();
        Map<ObjectiveConfigType, Object> resultMap = reportRateContext.getResultMap();
        NewHomeworkResult newHomeworkResult = reportRateContext.getNewHomeworkResult();
        User user = reportRateContext.getUser();
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = reportRateContext.getNewHomeworkProcessResultMap();
        NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (newHomeworkPracticeContent == null)
            return;
        List<NewHomeworkApp> apps = newHomeworkPracticeContent.getApps();
        if (apps == null)
            return;
        Set<String> lessonIds = new LinkedHashSet<>();
        for (NewHomeworkApp app : apps) {
            lessonIds.add(app.getLessonId());
        }
        Map<String, NewBookCatalog> lessonNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        Map<String, String> lessonIdToUnitIdMap = new LinkedHashMap<>();
        Set<String> unitIds = new LinkedHashSet<>();
        for (NewBookCatalog newBookCatalog : lessonNewBookCatalogMap.values()) {
            List<NewBookCatalogAncestor> ancestors = newBookCatalog.getAncestors();
            if (ancestors == null)
                continue;
            NewBookCatalogAncestor target = null;
            for (NewBookCatalogAncestor newBookCatalogAncestor : ancestors) {
                if (Objects.equals(newBookCatalogAncestor.getNodeType(), "UNIT")) {
                    target = newBookCatalogAncestor;
                    break;
                }
            }
            if (target != null) {
                lessonIdToUnitIdMap.put(newBookCatalog.getId(), target.getId());
                unitIds.add(target.getId());
            }
        }
        Map<String, NewBookCatalog> unitNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(unitIds);
        List<BasicAppUnitPart> unitParts = new LinkedList<>();
        Map<String, BasicAppUnitPart> unitPartMap = new LinkedHashMap<>();
        NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult
                .getPractices()
                .get(type);
        for (NewHomeworkApp app : apps) {
            PracticeType practiceType = practiceLoaderClient.loadPractice(app.getPracticeId());
            if (practiceType == null)
                continue;
            if (!lessonNewBookCatalogMap.containsKey(app.getLessonId()))
                continue;
            if (!lessonIdToUnitIdMap.containsKey(app.getLessonId()))
                continue;
            String unitId = lessonIdToUnitIdMap.get(app.getLessonId());
            if (!unitNewBookCatalogMap.containsKey(unitId))
                continue;
            NewHomeworkResultAppAnswer appAnswer = newHomeworkResultAnswer
                    .getAppAnswers()
                    .get(app.getCategoryId() + "-" + app.getLessonId());
            if (appAnswer == null)
                continue;
            //单元
            BasicAppUnitPart unitPart;
            if (unitPartMap.containsKey(unitId)) {
                unitPart = unitPartMap.get(unitId);
            } else {
                NewBookCatalog newBookCatalog = unitNewBookCatalogMap.get(unitId);
                unitPart = new BasicAppUnitPart();
                unitPart.setUnitId(unitId);
                unitParts.add(unitPart);
                unitPart.setUnitName(newBookCatalog.getAlias());
                unitPartMap.put(unitId, unitPart);
            }
            //lesson
            BasicAppUnitPart.BasicAppLessonPart basicAppLessonPart;
            if (unitPart.getLessonPartMap().containsKey(app.getLessonId())) {
                basicAppLessonPart = unitPart.getLessonPartMap().get(app.getLessonId());
            } else {
                NewBookCatalog newBookCatalog = lessonNewBookCatalogMap.get(app.getLessonId());
                basicAppLessonPart = new BasicAppUnitPart.BasicAppLessonPart();
                basicAppLessonPart.setLessonName(newBookCatalog.getAlias());
                basicAppLessonPart.setLessonId(app.getLessonId());
                unitPart.getLessonPartMap().put(app.getLessonId(), basicAppLessonPart);
                unitPart.getLessons().add(basicAppLessonPart);
            }
            //category
            List<String> voiceUrls = new LinkedList<>();
            String voiceScoringMode = "ListenOnly";
            if (practiceType.getNeedRecord()) {
                if (MapUtils.isNotEmpty(appAnswer.getAnswers())) {
                    LinkedHashMap<String, String> answers = appAnswer.getAnswers();
                    for (String newHomeworkProcessId : answers.values()) {
                        NewHomeworkProcessResult n = newHomeworkProcessResultMap.get(newHomeworkProcessId);
                        if (n != null) {
                            String voiceUrl = CollectionUtils.isEmpty(n.getOralDetails()) ||
                                    CollectionUtils.isEmpty(n.getOralDetails().get(0)) ?
                                    null :
                                    n.getOralDetails().get(0).get(0).getAudio();
                            if (StringUtils.isNotBlank(voiceUrl)) {
                                VoiceEngineType voiceEngineType = n.getVoiceEngineType();
                                voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                                voiceUrls.add(voiceUrl);
                            }
                            if (StringUtils.equalsIgnoreCase(n.getVoiceScoringMode(), "Normal")) {
                                voiceScoringMode = "Normal";
                            }
                        }
                    }
                }


            }
            BasicAppUnitPart.BasicAppCategoryPart categoryPart = new BasicAppUnitPart.BasicAppCategoryPart();
            int averageScore = Objects.isNull(appAnswer.getScore()) ?
                    null : new BigDecimal(appAnswer.getScore())
                    .setScale(0, BigDecimal.ROUND_HALF_UP)
                    .intValue();
            categoryPart.setCategoryName(practiceType.getCategoryName());
            categoryPart.setVoiceUrls(voiceUrls);
            categoryPart.setVoiceScoringMode(voiceScoringMode);
            categoryPart.setAverageScore(averageScore);
            categoryPart.setUserId(newHomeworkResult.getUserId());
            categoryPart.setCategoryId(app.getCategoryId());
            categoryPart.setUserName(user.fetchRealnameIfBlankId());
            categoryPart.setPracticeCategory(PracticeCategory.icon(practiceType.getCategoryName()));
            basicAppLessonPart.getCategories().add(categoryPart);
        }
        resultMap.put(type, unitParts);
    }

    @Override
    public void fetchNewHomeworkCommonObjectiveConfigTypePart(ObjectiveConfigTypePartContext context) {

        //*********** begin 数据准备 ************//
        NewHomeworkPracticeContent target = context.getTarget();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = context.getNewHomeworkResultMap();
        NewHomework newHomework = context.getNewHomework();
        Map<Long, User> userMap = context.getUserMap();
        Teacher teacher = context.getTeacher();
        ObjectiveConfigType type = context.getType();
        ObjectiveConfigTypeParameter parameter = context.getParameter();
        Integer categoryId = parameter.getCategoryId();
        String lessonId = parameter.getLessonId();
        NewHomeworkApp targetApp = null;
        List<String> questionIds = new LinkedList<>();
        for (NewHomeworkApp o : target.getApps()) {
            if (Objects.equals(o.getCategoryId(), categoryId)
                    && Objects.equals(o.getLessonId(), lessonId)
                    && CollectionUtils.isNotEmpty(o.getQuestions())) {
                questionIds = o.getQuestions()
                        .stream()
                        .map(NewHomeworkQuestion::getQuestionId)
                        .collect(Collectors.toList());
                targetApp = o;
                break;
            }
        }
        PracticeType practiceType = Objects.nonNull(targetApp) ? practiceServiceClient.getPracticeBuffer().loadPractice(targetApp.getPracticeId()) : null;
        if (Objects.isNull(practiceType)) {
            logger.error("fetch NewHomework CommonObjectiveConfigType Part failed : hid {},type {},parameter {},tid {}", newHomework.getId(), type, JsonUtils.toJson(parameter), teacher.getId());
            MapMessage mapMessage = MapMessage.errorMessage("practiceType does not exist");
            context.setMapMessage(mapMessage);
            return;
        }

        Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        Set<Long> sentenceIds = newQuestionMap
                .values()
                .stream()
                .filter(o -> CollectionUtils.isNotEmpty(o.getSentenceIds()))
                .map(NewQuestion::getSentenceIds)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        Map<Long, Sentence> sentenceMap = englishContentLoaderClient.loadEnglishSentences(sentenceIds);
        boolean needRecord = SafeConverter.toBoolean(practiceType.getNeedRecord());

        List<BasicAppStudentDetail> basicAppStudentDetails = new LinkedList<>();
        Map<Long, BasicAppStudentDetail> basicAppStudentDetailMap = new LinkedHashMap<>();
        if (needRecord) {
            for (NewHomeworkResult n : newHomeworkResultMap.values()) {
                if (n.getPractices() == null)
                    continue;
                if (n.getPractices().containsKey(type)) {
                    NewHomeworkResultAnswer newHomeworkResultAnswer = n.getPractices().get(type);
                    if (newHomeworkResultAnswer.getAppAnswers() == null)
                        continue;
                    if (newHomeworkResultAnswer.getAppAnswers().containsKey(categoryId + "-" + lessonId)) {
                        BasicAppStudentDetail basicAppStudentDetail = new BasicAppStudentDetail();
                        basicAppStudentDetail.setUserId(n.getUserId());
                        double score = SafeConverter.toDouble(newHomeworkResultAnswer.getAppAnswers().get(categoryId + "-" + lessonId).getScore());
                        basicAppStudentDetail.setScore(new BigDecimal(score).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
                        basicAppStudentDetail.setUserName(userMap.containsKey(n.getUserId()) ? userMap.get(n.getUserId()).fetchRealnameIfBlankId() : "");
                        basicAppStudentDetailMap.put(n.getUserId(), basicAppStudentDetail);
                        basicAppStudentDetails.add(basicAppStudentDetail);
                    }
                }
            }
        }


        // 构件answerId
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        List<String> subHomeworkResultAnswerIds = new LinkedList<>();
        for (NewHomeworkQuestion newHomeworkQuestion : targetApp.fetchQuestions()) {
            NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomework.NewHomeworkQuestionObj(newHomework.getId(), type, Arrays.asList(SafeConverter.toString(targetApp.getCategoryId()), targetApp.getLessonId()), newHomeworkQuestion.getQuestionId());
            for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
                subHomeworkResultAnswerIds.add(newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId()));
            }
        }
        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);
        List<String> newHomeworkProcessResultIds = subHomeworkResultAnswerMap.values().stream().map(SubHomeworkResultAnswer::getProcessId).collect(Collectors.toList());
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), newHomeworkProcessResultIds);
        //*********** end 数据准备 ************//

        //********** begin BasicAppQuestion对应返回的数据结构，先初始化***********//
        Map<String, BasicAppQuestion> basicAppQuestionMap = newQuestionMap.values()
                .stream()
                .collect(Collectors.toMap(NewQuestion::getId, newQuestion -> {
                    List<Long> _sentenceIds = newQuestion.getSentenceIds();
                    List<Map<String, Object>> sentences = CollectionUtils.isNotEmpty(_sentenceIds) ?
                            _sentenceIds
                                    .stream()
                                    .map(l ->
                                            MapUtils.m(
                                                    "sentenceId", l,
                                                    "sentenceContent", Objects.isNull(sentenceMap.get(l)) ? "" : sentenceMap.get(l).getEnText()))
                                    .collect(Collectors.toList()) :
                            Collections.singletonList(MapUtils.m(
                                    "sentenceId", "1",
                                    "sentenceContent", "单词正在赶来中"));
                    BasicAppQuestion b = new BasicAppQuestion(newQuestion.getId());
                    b.setSentences(sentences);
                    return b;
                }));
        //********** end BasicAppQuestion对应返回的数据结构，先初始化***********//


        //*********** begin NewHomeworkProcessResult 的questionId 对应找到 basicAppQuestionMap 中的 BasicAppQuestion ，然后进行数据汇聚 ************//

        //********* needRecord 是否是语音题 **********//
        for (NewHomeworkProcessResult p : newHomeworkProcessResultMap.values()) {
            if (!basicAppQuestionMap.containsKey(p.getQuestionId())) continue;
            BasicAppQuestion basicAppQuestion = basicAppQuestionMap.get(p.getQuestionId());
            basicAppQuestion.setNum(basicAppQuestion.getNum() + 1);
            //是否是语音回答的题
            if (needRecord) {
                basicAppQuestion.setTotalScore(SafeConverter.toDouble(p.getScore()) + basicAppQuestion.getTotalScore());
                BasicAppStudentDetail basicAppStudentDetail = basicAppStudentDetailMap.get(p.getUserId());
                if (userMap.containsKey(p.getUserId()) && basicAppStudentDetail != null) {
                    BasicAppQuestion.BasicAppQuestionUser basicAppQuestionUser = new BasicAppQuestion.BasicAppQuestionUser();
                    User user = userMap.get(p.getUserId());
                    double score = SafeConverter.toDouble(p.getScore());
                    basicAppQuestion.getUsers().add(basicAppQuestionUser);
                    basicAppQuestionUser.setUid(user.getId());
                    basicAppQuestionUser.setUserName(user.fetchRealname());
                    basicAppQuestionUser.setScore(score);
                    basicAppQuestionUser.setVoiceScoringMode(p.getVoiceScoringMode());
                    basicAppQuestionUser.setAppOralScoreLevel(p.getAppOralScoreLevel());
                    BasicAppStudentDetail.QuestionDetail detail = new BasicAppStudentDetail.QuestionDetail();
                    detail.setSentences(basicAppQuestion.getSentences());
                    detail.setVoiceScoringMode(p.getVoiceScoringMode());
                    detail.setAppOralScoreLevel(NewHomeworkUtils.handleAppOralScoreLevel(score));
                    basicAppStudentDetail.getQuestionDetailMap().put(p.getQuestionId(), detail);
                    if (CollectionUtils.isNotEmpty(p.getOralDetails())) {
                        for (List<BaseHomeworkProcessResult.OralDetail> oralDetails : p.getOralDetails()) {
                            if (CollectionUtils.isNotEmpty(oralDetails)) {
                                for (BaseHomeworkProcessResult.OralDetail oralDetail : oralDetails) {
                                    String voiceUrl = oralDetail.getAudio();
                                    VoiceEngineType voiceEngineType = p.getVoiceEngineType();
                                    voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                                    if (StringUtils.isNotEmpty(voiceUrl)) {
                                        basicAppQuestionUser.getVoiceUrls().add(voiceUrl);
                                        detail.getVoiceUrls().add(voiceUrl);
                                    }
                                }
                            }
                        }
                    }


                }
            } else {
                BasicAppQuestion.BasicAppQuestionUser u = new BasicAppQuestion.BasicAppQuestionUser();
                u.setUid(p.getUserId());
                u.setUserName(userMap.containsKey(p.getUserId()) ? userMap.get(p.getUserId()).fetchRealname() : "");
                if (SafeConverter.toBoolean(p.getGrasp())) {
                    basicAppQuestion.getRightStudentInformation().add(u);
                } else {
                    basicAppQuestion.getErrorStudentInformation().add(u);
                }
            }
        }
        //*********** end NewHomeworkProcessResult 的questionId 对应找到 basicAppQuestionMap 中的 BasicAppQuestion ，然后进行数据汇聚 ************//


        MapMessage mapMessage = MapMessage.successMessage();
        //学生详情
        //音频题目的排序
        if (CollectionUtils.isNotEmpty(basicAppStudentDetails)) {
            for (BasicAppStudentDetail basicAppStudentDetail : basicAppStudentDetails) {
                if (MapUtils.isNotEmpty(basicAppStudentDetail.getQuestionDetailMap())) {
                    String voiceScoringMode = "ListenOnly";
                    for (String qid : questionIds) {
                        if (basicAppStudentDetail.getQuestionDetailMap().containsKey(qid)) {
                            BasicAppStudentDetail.QuestionDetail detail = basicAppStudentDetail.getQuestionDetailMap().get(qid);
                            if (detail.getVoiceUrls() != null) {
                                basicAppStudentDetail.getVoiceUrls().addAll(detail.getVoiceUrls());
                                basicAppStudentDetail.getQuestionDetailList().add(detail);
                            }
                            if (StringUtils.equalsIgnoreCase(detail.getVoiceScoringMode(), "Normal")) {
                                voiceScoringMode = "Normal";
                            }
                        }
                    }
                    basicAppStudentDetail.setQuestionDetailMap(null);
                    basicAppStudentDetail.setVoiceScoringMode(voiceScoringMode);
                }
            }
        }

        mapMessage.add("basicAppStudentDetails", basicAppStudentDetails);
        //是否是绕口令，绕可令需要特殊处理
        boolean isTongueTwister = NatureSpellingType.TONGUE_TWISTER.getCategoryId() == categoryId;


        //数据后处理
        //*********** begin needRecord 是否是口语题 不同的处理**********//
        if (needRecord) {
            if (!isTongueTwister) {
                basicAppQuestionMap.values()
                        .stream()
                        .filter(o -> o.getNum() > 0)
                        .filter(o -> o.getTotalScore() > 0)
                        .forEach(o -> o.setAppOralScoreLevel(NewHomeworkUtils.handleAppOralScoreLevel(new BigDecimal(o.getTotalScore()).divide(new BigDecimal(o.getNum()), 2, BigDecimal.ROUND_HALF_UP).doubleValue())));

            }
            List<BasicAppQuestion> basicAppQuestion = questionIds.stream().filter(basicAppQuestionMap::containsKey).map(basicAppQuestionMap::get).collect(Collectors.toList());
            mapMessage.add("basicAppQuestion", basicAppQuestion);
            context.setMapMessage(mapMessage);
        } else {
            basicAppQuestionMap.values()
                    .stream()
                    .filter(o -> o.getNum() > 0)
                    .filter(o -> o.getErrorStudentInformation().size() > 0)
                    .forEach(
                            o -> {
                                int errorRate = new BigDecimal(100 * o.getErrorStudentInformation().size()).divide(new BigDecimal(o.getNum()), BigDecimal.ROUND_HALF_UP, 0).intValue();
                                o.setErrorRate(errorRate);
                            }
                    );
            List<BasicAppQuestion> basicAppQuestion = questionIds.stream()
                    .filter(basicAppQuestionMap::containsKey)
                    .map(basicAppQuestionMap::get)
                    .collect(Collectors.toList());
            mapMessage.add("basicAppQuestion", basicAppQuestion);
            context.setMapMessage(mapMessage);
        }
        //*********** end needRecord 是否是口语题 不同的处理**********//

        Map<String, Object> m = MapUtils.m(
                "categoryId", categoryId,
                "lessonId", lessonId,
                "categoryName", practiceType.getCategoryName(),
                "practiceCategory", PracticeCategory.icon(practiceType.getCategoryName()),
                "isAlien", NatureSpellingType.FUNNY_SPELLING.getCategoryId() == categoryId || NatureSpellingType.PRONUNCIATION_CLASSIFICATION.getCategoryId() == categoryId,
                "isTongueTwister", isTongueTwister,
                "isNeedRecord", needRecord
        );
        //分数段信息
        List<Map<String, Object>> scoreDesc = new LinkedList<>();
        for (AppOralScoreLevel a : AppOralScoreLevel.values()) {
            scoreDesc.add(MapUtils.m(
                    a.name(), a.getScore()
            ));
        }
        mapMessage.putAll(m);
        mapMessage.put("scoreDesc", scoreDesc);

    }

    @Override
    public void fetchNewHomeworkSingleQuestionPart(ObjectiveConfigTypePartContext context) {
        context.setMapMessage(MapMessage.errorMessage());
    }


    public Map<String, Map<String, Object>> lessonDataForBasicApp(NewHomeworkResult newHomeworkResult, List<NewHomeworkApp> apps, Boolean flag, ObjectiveConfigType objectiveConfig) {
        Map<String, Map<String, Object>> lessonData = new HashMap<>();
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(newHomeworkResult.getUserId());
        String name = Objects.isNull(studentDetail) ?
                "" :
                studentDetail.fetchRealname();
        List<String> newHomeworkProcessIds = newHomeworkResult.findAllHomeworkProcessIds(true);
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(newHomeworkResult.getHomeworkId(), newHomeworkProcessIds);
        for (NewHomeworkApp app : apps) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("categoryId", app.getCategoryId());
            PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(app.getPracticeId());
            data.put("categoryName", practiceType.getCategoryName());
            data.put("userId", newHomeworkResult.getUserId());
            data.put("userName", name);
            data.put("practiceCategory", PracticeCategory.icon(practiceType.getCategoryName()));
            List<String> voiceUrls = new LinkedList<>();
            data.put("voiceUrls", voiceUrls);
            String voiceScoringMode = "";
            if (practiceType.getNeedRecord()) {
                if (!flag) {
                    NewHomeworkResultAppAnswer appAnswer = newHomeworkResult
                            .getPractices()
                            .get(objectiveConfig)
                            .getAppAnswers()
                            .get(app.getCategoryId() + "-" + app.getLessonId());
                    if (MapUtils.isNotEmpty(appAnswer.getAnswers())) {
                        LinkedHashMap<String, String> answers = appAnswer.getAnswers();
                        for (String newHomeworkProcessId : answers.values()) {
                            NewHomeworkProcessResult n = newHomeworkProcessResultMap.get(newHomeworkProcessId);
                            if (n != null) {

                                if (CollectionUtils.isNotEmpty(n.getOralDetails())) {

                                    for (List<BaseHomeworkProcessResult.OralDetail> oralDetails : n.getOralDetails()) {
                                        if (CollectionUtils.isNotEmpty(oralDetails)) {
                                            for (BaseHomeworkProcessResult.OralDetail oralDetail : oralDetails) {
                                                String voiceUrl = oralDetail.getAudio();
                                                if (StringUtils.isNotEmpty(voiceUrl)) {
                                                    VoiceEngineType voiceEngineType = n.getVoiceEngineType();
                                                    voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                                                    voiceUrls.add(voiceUrl);
                                                }
                                            }
                                        }
                                    }
                                }
                                if (StringUtils.isBlank(voiceScoringMode)) {
                                    voiceScoringMode = n.getVoiceScoringMode();
                                    data.put("voiceScoringMode", voiceScoringMode);
                                }
                            }
                        }
                    }
                }
            }
            data.put("voiceScoringMode", voiceScoringMode);
            if (!flag) {
                NewHomeworkResultAppAnswer appAnswer = newHomeworkResult
                        .getPractices()
                        .get(objectiveConfig)
                        .getAppAnswers()
                        .get(app.getCategoryId() + "-" + app.getLessonId());
                data.put("finished", Objects.nonNull(appAnswer) && appAnswer.isFinished());
                data.put("averageScore", Objects.isNull(appAnswer) ||
                        Objects.isNull(appAnswer.getScore()) ?
                        null :
                        new BigDecimal(appAnswer.getScore())
                                .setScale(0, BigDecimal.ROUND_HALF_UP)
                                .intValue());
            } else {
                data.put("finished", false);
                data.put("averageScore", null);
            }
            if (lessonData.containsKey(app.getLessonId())) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> l = (List<Map<String, Object>>) lessonData.get(app.getLessonId()).get("categories");
                l.add(data);
            } else {
                List<Map<String, Object>> l = new LinkedList<>();
                l.add(data);
                lessonData.put(app.getLessonId(),
                        MapUtils.m(
                                "lessonId", app.getLessonId(),
                                "categories", l
                        ));
            }
        }
        return lessonData;
    }


    //基础练习跟读题处理
    @SuppressWarnings("unchecked")
    private void doHandleForReport(Map<String, NewQuestion> newQuestionMap, Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap, Map<String, Map<String, Object>> clMap, NewHomeworkResult n, NewHomeworkResultAppAnswer ns, String k, Map<Long, User> userMap, Map<Long, Sentence> sentenceMap) {
        List<PersonalDetail> list;
        List<String> voiceUrls = new LinkedList<>();
        int score = new BigDecimal(SafeConverter.toDouble(ns.getScore())).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        String voiceScoringMode = "ListenOnly";
        //学生掌控情况页面
        //整个应用，各个学生掌握情况
        PersonalDetail personalDetail = new PersonalDetail();
        personalDetail.setUserId(n.getUserId());
        personalDetail.setUserName(userMap.get(n.getUserId()).fetchRealnameIfBlankId());
        personalDetail.setScore(score);
        personalDetail.setVoiceUrls(voiceUrls);
        //学生得分情况
        if (!clMap.get(k).containsKey("personalStatistics")) {
            list = new LinkedList<>();
            list.add(personalDetail);
            clMap.get(k).put("personalStatistics", list);
        } else {
            list = (List<PersonalDetail>) clMap.get(k).get("personalStatistics");
            list.add(personalDetail);
        }
        if (MapUtils.isNotEmpty(ns.getAnswers())) {
            LinkedHashMap<String, String> answers = ns.getAnswers();
            Map<String, ContentValueForQuestion> contentStatistics;
            if (clMap.get(k).containsKey("contentStatistics")) {
                contentStatistics = (Map<String, ContentValueForQuestion>) clMap.get(k).get("contentStatistics");
            } else {
                contentStatistics = new LinkedHashMap<>();
                clMap.get(k).put("contentStatistics", contentStatistics);
            }
            //contentStatistics key 是 questionId value是 map
            for (String qId : answers.keySet()) {
                NewHomeworkProcessResult np = newHomeworkProcessResultMap.get(answers.get(qId));
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
                ContentValueForQuestion contentValueForQuestion;//内容的得分情况
                //每个题。各个学生的完成情况
                List<StudentPersonalInfo> studentContentInfo;
                StudentPersonalInfo studentPersonalInfo = new StudentPersonalInfo();
                studentPersonalInfo.setUserName(userMap.containsKey(n.getUserId()) ? userMap.get(n.getUserId()).fetchRealname() : "");
                studentPersonalInfo.setUserId(n.getUserId());
                studentPersonalInfo.setVoiceUrls(_voiceUrls);
                studentPersonalInfo.setVoiceScoringMode(np.getVoiceScoringMode());
                studentPersonalInfo.setScore(SafeConverter.toDouble(np.getScore()));
                studentPersonalInfo.setAppOralScoreLevel(np.getAppOralScoreLevel());
                if (contentStatistics.containsKey(qId)) {
                    contentValueForQuestion = contentStatistics.get(qId);
                    studentContentInfo = contentValueForQuestion.getStudentContentInfo();
                    studentContentInfo.add(studentPersonalInfo);
                    contentValueForQuestion.setTotalScore(contentValueForQuestion.getTotalScore() + SafeConverter.toDouble(np.getScore()));
                    contentValueForQuestion.setSize(1 + contentValueForQuestion.getSize());
                    if (StringUtils.equalsIgnoreCase(np.getVoiceScoringMode(), "Normal")) {
                        voiceScoringMode = "Normal";
                    }
                } else {
                    contentValueForQuestion = new ContentValueForQuestion();
                    contentStatistics.put(qId, contentValueForQuestion);
                    contentValueForQuestion.setQId(qId);
                    NewQuestion newQuestion = newQuestionMap.get(qId);
                    List<Long> _sentenceIds = newQuestion.getSentenceIds();
                    List<Map<String, Object>> sentences = CollectionUtils.isNotEmpty(_sentenceIds) ?
                            _sentenceIds.stream()
                                    .map(l ->
                                            MapUtils.m(
                                                    "sentenceId", l,
                                                    "sentenceContent", Objects.nonNull(sentenceMap.get(l)) ? sentenceMap.get(l).getEnText() : ""))
                                    .collect(Collectors.toCollection(LinkedList::new))
                            : Collections.emptyList();
                    contentValueForQuestion.setSentences(sentences);
                    studentContentInfo = new LinkedList<>();
                    contentValueForQuestion.setStudentContentInfo(studentContentInfo);
                    studentContentInfo.add(studentPersonalInfo);
                    if (StringUtils.equalsIgnoreCase(np.getVoiceScoringMode(), "Normal")) {
                        voiceScoringMode = "Normal";
                    }
                    contentValueForQuestion.setTotalScore(SafeConverter.toDouble(np.getScore()));
                    contentValueForQuestion.setSize(1);
                }
            }
            personalDetail.setVoiceScoringMode(voiceScoringMode);
        }
    }


    //基础练习非跟读题
    @SuppressWarnings("unchecked")
    private void doHandleForNotReport(Map<String, NewQuestion> newQuestionMap, Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap, Map<String, Map<String, Object>> clMap, NewHomeworkResult n, NewHomeworkResultAppAnswer ns, String k, Map<Long, User> userMap, Map<Long, Sentence> sentenceMap) {
        //非跟读题
        if (MapUtils.isNotEmpty(ns.getAnswers())) {
            LinkedHashMap<String, String> answers = ns.getAnswers();
            Map<String, ContentValueForQuestion> contentStatistics;
            if (clMap.get(k).containsKey("contentStatistics")) {
                contentStatistics = (Map<String, ContentValueForQuestion>) clMap.get(k).get("contentStatistics");
            } else {
                contentStatistics = new LinkedHashMap<>();
                clMap.get(k).put("contentStatistics", contentStatistics);
            }
            for (String qId : answers.keySet()) {
                NewHomeworkProcessResult np = newHomeworkProcessResultMap.get(answers.get(qId));
                if (np != null) {
                    Map<String, Object> t = MapUtils.m(
                            "userName", userMap.get(n.getUserId()).fetchRealname(),
                            "userId", n.getUserId()
                    );
                    //对于每一题答题信息
                    ContentValueForQuestion contentValueForQuestion;
                    List<Map<String, Object>> errorStudentInformation;
                    List<Map<String, Object>> rightStudentInformation;
                    if (contentStatistics.containsKey(qId)) {
                        contentValueForQuestion = contentStatistics.get(qId);
                        errorStudentInformation = contentValueForQuestion.getErrorStudentInformation();
                        rightStudentInformation = contentValueForQuestion.getRightStudentInformation();
                        if (SafeConverter.toBoolean(np.getGrasp())) {
                            rightStudentInformation.add(t);
                        } else {
                            errorStudentInformation.add(t);
                        }
                    } else {
                        //对于每一题只会进来一次
                        contentValueForQuestion = new ContentValueForQuestion();
                        contentStatistics.put(qId, contentValueForQuestion);
                        NewQuestion newQuestion = newQuestionMap.get(qId);
                        List<Long> _sentenceIds = newQuestion.getSentenceIds();
                        List<Map<String, Object>> sentences = CollectionUtils.isNotEmpty(_sentenceIds) ?
                                _sentenceIds
                                        .stream()
                                        .map(l ->
                                                MapUtils.m(
                                                        "sentenceId", l,
                                                        "sentenceContent", Objects.nonNull(sentenceMap.get(l)) ? sentenceMap.get(l).getEnText() : ""))
                                        .collect(Collectors.toCollection(LinkedList::new))
                                : Collections.EMPTY_LIST;
                        contentValueForQuestion.setSentences(sentences);
                        contentValueForQuestion.setQId(qId);
                        errorStudentInformation = new LinkedList<>();
                        rightStudentInformation = new LinkedList<>();
                        if (SafeConverter.toBoolean(np.getGrasp())) {
                            rightStudentInformation.add(t);
                        } else {
                            errorStudentInformation.add(t);
                        }
                        contentValueForQuestion.setRightStudentInformation(rightStudentInformation);
                        contentValueForQuestion.setErrorStudentInformation(errorStudentInformation);
                    }
                }
            }
        }
    }

    // 整份作业的基础练习报告:PC接口
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> internalProcessHomeworkAnswerDetailForBasicAppIsPcWay(ObjectiveConfigType type, Map<String, NewQuestion> allNewQuestionMap, Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap, Map<Long, NewHomeworkResult> newHomeworkResultMap, NewHomework newHomework, Map<Long, User> userMap) {
        List<Map<String, Object>> values = new ArrayList<>();
        Map<String, NewHomeworkResult> newHomeworkResultMapToObjectiveConfigType = handlerNewHomeworkResultMap(newHomeworkResultMap, type);
        if (MapUtils.isEmpty(newHomeworkResultMapToObjectiveConfigType)) {
            return values;
        }
        List<String> qIds = newHomework
                .findNewHomeworkQuestions(type)
                .stream()
                .map(NewHomeworkQuestion::getQuestionId)
                .collect(Collectors.toList());
        Map<String, NewQuestion> newQuestionMap = qIds
                .stream()
                .collect(Collectors
                        .toMap(Function.identity(), allNewQuestionMap::get));
        Map<String, Map<String, Object>> clMap = new LinkedHashMap<>();
        NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
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


        Set<String> lessonIds = apps.stream().map(NewHomeworkApp::getLessonId).collect(Collectors.toSet());
        Map<String, NewBookCatalog> lessonMs = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        Map<String, String> lineData = NewHomeworkUtils.handleLessonIdToUnitId(lessonMs);
        Map<String, NewBookCatalog> unitMs = newContentLoaderClient.loadBookCatalogByCatalogIds(lineData.values());
        for (NewHomeworkApp app : apps) {
            Integer categoryId = app.getCategoryId();
            String lessonId = app.getLessonId();
            Long practiceId = app.getPracticeId();
            lessonIds.add(lessonId);
            String k = Objects.isNull(categoryId) ? "" : categoryId + "-" + (Objects.isNull(lessonId) ? "" : lessonId);
            PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(practiceId);
            Map<String, Object> m = MapUtils.m(
                    "categoryId", categoryId,
                    "lessonId", lessonId,
                    "categoryName", practiceType.getCategoryName(),
                    "lessonName", lessonMs.containsKey(lessonId) ? lessonMs.get(lessonId).getAlias() : "",
                    "unitName", unitMs.containsKey(lineData.get(lessonId)) ? unitMs.get(lineData.get(lessonId)).getAlias() : "单元",
                    "unitId", lineData.get(lessonId),
                    "practiceCategory", PracticeCategory.icon(practiceType.getCategoryName()),
                    "isAlien", NatureSpellingType.FUNNY_SPELLING.getCategoryId() == categoryId || NatureSpellingType.PRONUNCIATION_CLASSIFICATION.getCategoryId() == categoryId,
                    "isNeedRecord", practiceType.fetchNeedRecord()
            );
            clMap.put(k, m);
        }


        for (NewHomeworkResult n : newHomeworkResultMapToObjectiveConfigType.values()) {
            if (!n.isFinishedOfObjectiveConfigType(type)) {
                continue;
            }
            if (MapUtils.isNotEmpty(n.getPractices()) &&
                    n.getPractices().get(type) != null &&
                    MapUtils.isNotEmpty(n.getPractices().get(type).getAppAnswers())) {
                LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = n.getPractices().get(type).getAppAnswers();
                for (NewHomeworkResultAppAnswer ns : appAnswers.values()) {
                    //学生各类基础练习情况挂上统计上
                    String k = ns.getCategoryId() + "-" + ns.getLessonId();
                    if (SafeConverter.toBoolean(clMap.get(k).get("isNeedRecord"))) {
                        doHandleForReport(newQuestionMap, newHomeworkProcessResultMap, clMap, n, ns, k, userMap, sentenceMap);
                    } else {
                        doHandleForNotReport(newQuestionMap, newHomeworkProcessResultMap, clMap, n, ns, k, userMap, sentenceMap);
                    }
                }
            }
        }

        for (Map<String, Object> m : clMap.values()) {
            if (SafeConverter.toBoolean(m.get("isNeedRecord"))) {
                Map<String, ContentValueForQuestion> contentStatistics = (Map<String, ContentValueForQuestion>) m.get("contentStatistics");
                contentStatistics = contentStatistics == null ? new LinkedHashMap<>() : contentStatistics;
                List<PersonalDetail> personalStatistics = (List<PersonalDetail>) m.get("personalStatistics");
                personalStatistics.sort((o1, o2) -> Double.compare(o2.getScore(), o1.getScore()));
                //key qId value map
                List<ContentValueForQuestion> contentStatisticsList = new LinkedList<>();
                for (ContentValueForQuestion ml : contentStatistics.values()) {
                    List<StudentPersonalInfo> studentContentInfo = ml.getStudentContentInfo();
                    if (CollectionUtils.isNotEmpty(studentContentInfo)) {
                        studentContentInfo.sort((o1, o2) -> Double.compare(o2.getScore(), o1.getScore()));
                    }
                    int averageScore = ml.getSize() > 0 ? new BigDecimal(ml.getTotalScore())
                            .divide(new BigDecimal(ml.getSize()), 1, BigDecimal.ROUND_HALF_UP)
                            .intValue() : 0;
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
                    ml.setAppOralScoreLevel(appOralScoreLevel);
                    contentStatisticsList.add(ml);
                }
                m.remove("contentStatistics");
                m.put("contentStatistics", contentStatisticsList);
            } else {
                Map<String, ContentValueForQuestion> contentStatistics = (Map<String, ContentValueForQuestion>) m.get("contentStatistics");
                for (ContentValueForQuestion so : contentStatistics.values()) {
                    int rate;
                    int rightNum = so.getRightStudentInformation().size();
                    int errorNum = so.getErrorStudentInformation().size();
                    if (rightNum + errorNum == 0) {
                        rate = 0;
                    } else {
                        rate = new BigDecimal(errorNum)
                                .divide(new BigDecimal(rightNum + errorNum), 3, BigDecimal.ROUND_HALF_UP)
                                .multiply(new BigDecimal(100))
                                .intValue();
                    }
                    so.setRate(rate);
                }
                m.remove("contentStatistics");
                m.put("contentStatistics", contentStatistics.values());
            }
            values.add(m);
        }
        //自然拼读特殊处理
        if (getObjectiveConfigType() == ObjectiveConfigType.NATURAL_SPELLING) {
            for (Map<String, Object> map : values) {
                if (map.containsKey("categoryId")) {
                    int categoryId = SafeConverter.toInt(map.get("categoryId"));
                    //字母学习、单词拼读 不需要学生掌握情况页面
                    if (NatureSpellingType.ALPHABETIC_PRACTICE.getCategoryId() == categoryId || NatureSpellingType.REPEAT_WORD.getCategoryId() == categoryId) {
                        if (map.containsKey("personalStatistics")) {
                            map.put("personalStatistics", Collections.emptyList());
                        }

                    }
                    //绕口令，需要内容掌握情况
                    if (NatureSpellingType.TONGUE_TWISTER.getCategoryId() == categoryId) {
                        if (map.containsKey("contentStatistics")) {
                            map.put("contentStatistics", Collections.emptyList());
                        }
                    }
                }
            }
        }
        return values;
    }

    private List<BasicAppUnitPart> newInternalProcessHomeworkAnswerDetailForBasicAppIsNotPcWay(ObjectiveConfigType type, Map<Long, NewHomeworkResult> newHomeworkResultMap, NewHomework newHomework) {
        Map<String, NewHomeworkResult> newHomeworkResultMapToObjectiveConfigType = handlerNewHomeworkResultMap(newHomeworkResultMap, type);
        if (MapUtils.isEmpty(newHomeworkResultMapToObjectiveConfigType)) {
            return Collections.emptyList();
        }
        NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (newHomeworkPracticeContent == null)
            return Collections.emptyList();
        List<NewHomeworkApp> apps = newHomeworkPracticeContent.getApps();
        if (apps == null)
            return Collections.emptyList();
        Set<String> lessonIds = new LinkedHashSet<>();
        for (NewHomeworkApp app : apps) {
            lessonIds.add(app.getLessonId());
        }
        Map<String, NewBookCatalog> lessonNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        Map<String, String> lessonIdToUnitIdMap = new LinkedHashMap<>();
        Set<String> unitIds = new LinkedHashSet<>();
        for (NewBookCatalog newBookCatalog : lessonNewBookCatalogMap.values()) {
            List<NewBookCatalogAncestor> ancestors = newBookCatalog.getAncestors();
            if (ancestors == null)
                continue;
            NewBookCatalogAncestor target = null;
            for (NewBookCatalogAncestor newBookCatalogAncestor : ancestors) {
                if (Objects.equals(newBookCatalogAncestor.getNodeType(), "UNIT")) {
                    target = newBookCatalogAncestor;
                    break;
                }
            }
            if (target != null) {
                lessonIdToUnitIdMap.put(newBookCatalog.getId(), target.getId());
                unitIds.add(target.getId());
            }
        }
        Map<String, NewBookCatalog> unitNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(unitIds);
        List<BasicAppUnitPart> unitParts = new LinkedList<>();
        Map<String, BasicAppUnitPart> unitPartMap = new LinkedHashMap<>();
        for (NewHomeworkApp app : apps) {
            PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(app.getPracticeId());
            if (practiceType == null)
                continue;
            if (!lessonNewBookCatalogMap.containsKey(app.getLessonId()))
                continue;
            if (!lessonIdToUnitIdMap.containsKey(app.getLessonId()))
                continue;
            String unitId = lessonIdToUnitIdMap.get(app.getLessonId());
            if (!unitNewBookCatalogMap.containsKey(unitId))
                continue;
            BasicAppUnitPart unitPart;
            if (unitPartMap.containsKey(unitId)) {
                unitPart = unitPartMap.get(unitId);
            } else {
                NewBookCatalog newBookCatalog = unitNewBookCatalogMap.get(unitId);
                unitPart = new BasicAppUnitPart();
                unitPart.setUnitId(unitId);
                unitParts.add(unitPart);
                unitPart.setUnitName(newBookCatalog.getAlias());
                unitPartMap.put(unitId, unitPart);
            }
            BasicAppUnitPart.BasicAppLessonPart basicAppLessonPart;
            if (unitPart.getLessonPartMap().containsKey(app.getLessonId())) {
                basicAppLessonPart = unitPart.getLessonPartMap().get(app.getLessonId());
            } else {
                NewBookCatalog newBookCatalog = lessonNewBookCatalogMap.get(app.getLessonId());
                basicAppLessonPart = new BasicAppUnitPart.BasicAppLessonPart();
                basicAppLessonPart.setLessonName(newBookCatalog.getAlias());
                basicAppLessonPart.setLessonId(app.getLessonId());
                unitPart.getLessonPartMap().put(app.getLessonId(), basicAppLessonPart);
                unitPart.getLessons().add(basicAppLessonPart);
            }
            BasicAppUnitPart.BasicAppCategoryPart categoryPart = new BasicAppUnitPart.BasicAppCategoryPart();
            categoryPart.setCategoryId(app.getCategoryId());
            categoryPart.setCategoryName(practiceType.getCategoryName());
            categoryPart.setPracticeCategory(PracticeCategory.icon(practiceType.getCategoryName()));
            categoryPart.setHomeworkId(newHomework.getId());
            basicAppLessonPart.getCategories().add(categoryPart);
            basicAppLessonPart.getCategoryPartMap().put(app.getCategoryId(), categoryPart);
        }
        for (NewHomeworkResult n : newHomeworkResultMapToObjectiveConfigType.values()) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = n.getPractices().get(type);
            if (newHomeworkResultAnswer == null)
                continue;
            if (newHomeworkResultAnswer.getAppAnswers() == null)
                continue;
            for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : newHomeworkResultAnswer.getAppAnswers().entrySet()) {
                String key = entry.getKey();
                String[] keys = StringUtils.split(key, "-");
                if (keys == null || keys.length != 2)
                    continue;
                Integer categoryId = SafeConverter.toInt(keys[0]);
                String lessonId = keys[1];
                if (!lessonIdToUnitIdMap.containsKey(lessonId))
                    continue;
                String unitId = lessonIdToUnitIdMap.get(lessonId);
                if (!unitPartMap.containsKey(unitId))
                    continue;
                BasicAppUnitPart unitPart = unitPartMap.get(unitId);
                if (!unitPart.getLessonPartMap().containsKey(lessonId))
                    continue;
                BasicAppUnitPart.BasicAppLessonPart basicAppLessonPart = unitPart.getLessonPartMap().get(lessonId);
                if (!basicAppLessonPart.getCategoryPartMap().containsKey(categoryId))
                    continue;
                BasicAppUnitPart.BasicAppCategoryPart categoryPart = basicAppLessonPart.getCategoryPartMap().get(categoryId);
                categoryPart.setNum(1 + categoryPart.getNum());
                categoryPart.setTotalScore(SafeConverter.toDouble(entry.getValue().getScore()) + categoryPart.getTotalScore());
            }
        }
        for (BasicAppUnitPart unitPart : unitParts) {
            unitPart.setLessonPartMap(null);
            for (BasicAppUnitPart.BasicAppLessonPart lessonPart : unitPart.getLessons()) {
                lessonPart.setCategoryPartMap(null);
                for (BasicAppUnitPart.BasicAppCategoryPart categoryPart : lessonPart.getCategories()) {
                    if (categoryPart.getNum() > 0) {
                        int averageScore = new BigDecimal(categoryPart.getTotalScore())
                                .divide(new BigDecimal(categoryPart.getNum()), 0, BigDecimal.ROUND_HALF_UP)
                                .intValue();
                        categoryPart.setAverageScore(averageScore);
                    }
                }
            }
        }
        return unitParts;
    }

    @Override
    public void processQuestionPartTypeInfo(Map<Long, NewHomeworkResult> newHomeworkResultMap, NewHomework newHomework, ObjectiveConfigType type, Map<ObjectiveConfigType, Object> result, String cdnBaseUrl) {
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap.values().stream().filter(o -> o.isFinishedOfObjectiveConfigType(type)).collect(Collectors.toList());
        Map<String, Object> typeResult = new LinkedHashMap<>();
        typeResult.put("type",type);
        typeResult.put("typeName",type.getValue());
        if (CollectionUtils.isEmpty(newHomeworkResults)){
            typeResult.put("finishCount", 0);
            result.put(type, typeResult);
            return;
        }

        NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        List<NewHomeworkApp> apps = newHomeworkPracticeContent.getApps();
        Set<String> lessonIds = new LinkedHashSet<>();
        for (NewHomeworkApp app : apps) {
            lessonIds.add(app.getLessonId());
        }
        Map<String, NewBookCatalog> lessonNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        //lessonId To UnitId
        Map<String, String> lessonIdToUnitIdMap = new LinkedHashMap<>();
        Set<String> unitIds = new LinkedHashSet<>();
        for (NewBookCatalog newBookCatalog : lessonNewBookCatalogMap.values()) {
            List<NewBookCatalogAncestor> ancestors = newBookCatalog.getAncestors();
            if (ancestors == null)
                continue;
            NewBookCatalogAncestor target = null;
            for (NewBookCatalogAncestor newBookCatalogAncestor : ancestors) {
                if (Objects.equals(newBookCatalogAncestor.getNodeType(), "UNIT")) {
                    target = newBookCatalogAncestor;
                    break;
                }
            }
            if (target != null) {
                lessonIdToUnitIdMap.put(newBookCatalog.getId(), target.getId());
                unitIds.add(target.getId());
            }
        }
        Map<String, NewBookCatalog> unitNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(unitIds);
        List<BasicAppUnitPart> unitParts = new LinkedList<>();
        Map<String, BasicAppUnitPart> unitPartMap = new LinkedHashMap<>();
        for (NewHomeworkApp app : apps) {
            PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(app.getPracticeId());
            if (practiceType == null)
                continue;
            if (!lessonNewBookCatalogMap.containsKey(app.getLessonId()))
                continue;
            if (!lessonIdToUnitIdMap.containsKey(app.getLessonId()))
                continue;
            String unitId = lessonIdToUnitIdMap.get(app.getLessonId());
            if (!unitNewBookCatalogMap.containsKey(unitId))
                continue;
            BasicAppUnitPart unitPart;
            if (unitPartMap.containsKey(unitId)) {
                unitPart = unitPartMap.get(unitId);
            } else {
                NewBookCatalog newBookCatalog = unitNewBookCatalogMap.get(unitId);
                unitPart = new BasicAppUnitPart();
                unitPart.setUnitId(unitId);
                unitParts.add(unitPart);
                unitPart.setUnitName(newBookCatalog.getAlias());
                unitPartMap.put(unitId, unitPart);
            }
            BasicAppUnitPart.BasicAppLessonPart basicAppLessonPart;
            if (unitPart.getLessonPartMap().containsKey(app.getLessonId())) {
                basicAppLessonPart = unitPart.getLessonPartMap().get(app.getLessonId());
            } else {
                NewBookCatalog newBookCatalog = lessonNewBookCatalogMap.get(app.getLessonId());
                basicAppLessonPart = new BasicAppUnitPart.BasicAppLessonPart();
                basicAppLessonPart.setLessonName(newBookCatalog.getAlias());
                basicAppLessonPart.setLessonId(app.getLessonId());
                unitPart.getLessonPartMap().put(app.getLessonId(), basicAppLessonPart);
                unitPart.getLessons().add(basicAppLessonPart);
            }
            BasicAppUnitPart.BasicAppCategoryPart categoryPart = new BasicAppUnitPart.BasicAppCategoryPart();
            categoryPart.setCategoryId(app.getCategoryId());
            categoryPart.setCategoryName(practiceType.getCategoryName());
            categoryPart.setPracticeCategory(PracticeCategory.icon(practiceType.getCategoryName()));
            categoryPart.setHomeworkId(newHomework.getId());
            basicAppLessonPart.getCategories().add(categoryPart);
            basicAppLessonPart.getCategoryPartMap().put(app.getCategoryId(), categoryPart);
        }
        int finishCount = 0;
        int totalDuration = 0;
        int totalScore = 0;
        for (NewHomeworkResult n : newHomeworkResults) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = n.getPractices().get(type);
            if (newHomeworkResultAnswer == null)
                continue;
            if (newHomeworkResultAnswer.getAppAnswers() == null)
                continue;
            finishCount++;
            Integer duration = newHomeworkResultAnswer.processDuration();
            totalDuration += SafeConverter.toInt(new BigDecimal(SafeConverter.toInt(duration)).divide(new BigDecimal(60), 0, BigDecimal.ROUND_UP).intValue());
            totalScore += SafeConverter.toInt(SafeConverter.toInt(newHomeworkResultAnswer.processScore(type)));
            for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : newHomeworkResultAnswer.getAppAnswers().entrySet()) {
                String key = entry.getKey();
                String[] keys = StringUtils.split(key, "-");
                if (keys == null || keys.length != 2)
                    continue;
                Integer categoryId = SafeConverter.toInt(keys[0]);
                String lessonId = keys[1];
                if (!lessonIdToUnitIdMap.containsKey(lessonId))
                    continue;
                String unitId = lessonIdToUnitIdMap.get(lessonId);
                if (!unitPartMap.containsKey(unitId))
                    continue;
                BasicAppUnitPart unitPart = unitPartMap.get(unitId);
                if (!unitPart.getLessonPartMap().containsKey(lessonId))
                    continue;
                BasicAppUnitPart.BasicAppLessonPart basicAppLessonPart = unitPart.getLessonPartMap().get(lessonId);
                if (!basicAppLessonPart.getCategoryPartMap().containsKey(categoryId))
                    continue;
                BasicAppUnitPart.BasicAppCategoryPart categoryPart = basicAppLessonPart.getCategoryPartMap().get(categoryId);
                categoryPart.setNum(1 + categoryPart.getNum());
                categoryPart.setTotalScore(SafeConverter.toDouble(entry.getValue().getScore()) + categoryPart.getTotalScore());
            }
        }
        for (BasicAppUnitPart unitPart : unitParts) {
            //删除临时数据：不需要返回前端
            unitPart.setLessonPartMap(null);
            for (BasicAppUnitPart.BasicAppLessonPart lessonPart : unitPart.getLessons()) {
                //删除临时数据，不需要返回前端
                lessonPart.setCategoryPartMap(null);
                for (BasicAppUnitPart.BasicAppCategoryPart categoryPart : lessonPart.getCategories()) {
                    if (categoryPart.getNum() > 0) {
                        int averageScore = new BigDecimal(categoryPart.getTotalScore())
                                .divide(new BigDecimal(categoryPart.getNum()), 0, BigDecimal.ROUND_HALF_UP)
                                .intValue();
                        categoryPart.setAverageScore(averageScore);
                    }
                }
            }
        }
        //平均分数和平均时长
        int avgScore = 0;
        int avgDuration = 0;
        if (finishCount != 0) {
            avgScore = new BigDecimal(totalScore).divide(new BigDecimal(finishCount), 0, BigDecimal.ROUND_HALF_UP).intValue();
            avgDuration = new BigDecimal(totalDuration).divide(new BigDecimal(finishCount), 0, BigDecimal.ROUND_UP).intValue();
        }
        typeResult.put("finishCount", finishCount);
        typeResult.put("avgScore", avgScore);
        typeResult.put("avgDuration", avgDuration);
        typeResult.put("baseAppInformation", unitParts);
        result.put(type, typeResult);
    }

}
