package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;


import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.content.api.entity.ChineseSentence;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
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
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.NewReadReciteAppPart;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.readrecite.ReadReciteBasicData;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.readrecite.ReadReciteData;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkQuestionFileHelper;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class ProcessNewHomeworkAnswerDetailNewReadReciteTemplate extends ProcessNewHomeworkAnswerDetailReadReciteTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.NEW_READ_RECITE;
    }

    @Override
    public void processNewHomeworkAnswerDetail(ReportRateContext reportRateContext) {
        Map<String, NewHomeworkResult> newHomeworkResultMapToObjectiveConfigType = handlerNewHomeworkResultMap(reportRateContext.getNewHomeworkResultMap(), ObjectiveConfigType.NEW_READ_RECITE);

        if (newHomeworkResultMapToObjectiveConfigType.isEmpty()) {
            return;
        }

        ReadReciteData readReciteData = new ReadReciteData();
        Map<String, ReadReciteBasicData> readReciteBasicDataMap = new HashMap<>();
        NewHomeworkPracticeContent target = reportRateContext.getNewHomework().findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.NEW_READ_RECITE);
        handleChineseReadRecite(target, reportRateContext.getAllNewQuestionMap(), readReciteData, readReciteBasicDataMap);

        for (NewHomeworkResult newHomeworkResult : newHomeworkResultMapToObjectiveConfigType.values()) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.NEW_READ_RECITE);
            for (String questionBoxId : newHomeworkResultAnswer.getAppAnswers().keySet()) {
                NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(questionBoxId);

                //避免作业发生改动
                if (!readReciteBasicDataMap.containsKey(questionBoxId))
                    continue;

                ReadReciteBasicData readReciteBasicData = readReciteBasicDataMap.get(questionBoxId);

                ReadReciteBasicData.UserVoice userVoice = new ReadReciteBasicData.UserVoice();

                readReciteBasicData.getUsers().add(userVoice);

                userVoice.setUserId(newHomeworkResult.getUserId());

                int time = new BigDecimal(newHomeworkResultAppAnswer.processDuration())
                        .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                        .intValue();

                String duration = NewHomeworkUtils.handlerEnTime(time);

                userVoice.setDuration(duration);

                if (reportRateContext.getUserMap().containsKey(newHomeworkResult.getUserId())) {
                    User user = reportRateContext.getUserMap().get(newHomeworkResult.getUserId());
                    userVoice.setUserName(user.fetchRealnameIfBlankId());
                }


                for (ReadReciteBasicData.ParagraphDetailed paragraphDetailed : readReciteBasicData.getParagraphDetaileds()) {

                    if (newHomeworkResultAppAnswer.getAnswers().containsKey(paragraphDetailed.getQuestionId())
                            && reportRateContext.getNewHomeworkProcessResultMap().containsKey(newHomeworkResultAppAnswer.getAnswers().get(paragraphDetailed.getQuestionId()))) {
                        NewHomeworkProcessResult newHomeworkProcessResult = reportRateContext.getNewHomeworkProcessResultMap().get(newHomeworkResultAppAnswer.getAnswers().get(paragraphDetailed.getQuestionId()));
                        if (newHomeworkProcessResult != null && CollectionUtils.isNotEmpty(newHomeworkProcessResult
                                .getFiles())) {
                            userVoice.getShowPics().addAll(newHomeworkProcessResult
                                    .getFiles()
                                    .stream()
                                    .flatMap(Collection::stream)
                                    .map(NewHomeworkQuestionFileHelper::getFileUrl)
                                    .collect(Collectors.toList()));
                        }
                    }
                }

                userVoice.setReview(SafeConverter.toBoolean(newHomeworkResultAppAnswer.getReview()));

                userVoice.setCorrection(SafeConverter.toString(newHomeworkResultAppAnswer.getCorrection(), ""));

                if (userVoice.isReview()) {
                    userVoice.setCorrect_des(newHomeworkResultAppAnswer.getCorrection() != null ? SafeConverter.toString(newHomeworkResultAppAnswer.getCorrection().getDescription(), "") : "阅");
                } else {
                    userVoice.setCorrect_des("未批改");
                }

            }
        }


        handleReadReciteBasicData(readReciteData.getReadData(), reportRateContext.getUserMap());

        handleReadReciteBasicData(readReciteData.getReciteData(), reportRateContext.getUserMap());


        reportRateContext.getResult().put(ObjectiveConfigType.NEW_READ_RECITE.name(), readReciteData);

    }

    @Override
    public void processNewHomeworkAnswerDetailPersonal(ReportPersonalRateContext reportRateContext) {
        if (reportRateContext.getNewHomeworkResult().getPractices().containsKey(ObjectiveConfigType.NEW_READ_RECITE) && reportRateContext.getNewHomeworkResult().getPractices().get(ObjectiveConfigType.NEW_READ_RECITE).isFinished()) {
            ReadReciteData readReciteData = new ReadReciteData();
            Map<String, ReadReciteBasicData> readReciteBasicDataMap = new HashMap<>();
            NewHomeworkPracticeContent target = reportRateContext.getNewHomework().findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.NEW_READ_RECITE);

            handleChineseReadRecite(target, reportRateContext.getAllNewQuestionMap(), readReciteData, readReciteBasicDataMap);

            NewHomeworkResultAnswer newHomeworkResultAnswer = reportRateContext.getNewHomeworkResult().getPractices().get(ObjectiveConfigType.NEW_READ_RECITE);

            for (String questionBoxId : newHomeworkResultAnswer.getAppAnswers().keySet()) {
                NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(questionBoxId);

                if (!readReciteBasicDataMap.containsKey(questionBoxId))
                    continue;
                ReadReciteBasicData readReciteBasicData = readReciteBasicDataMap.get(questionBoxId);


                ReadReciteBasicData.UserVoice userVoice = new ReadReciteBasicData.UserVoice();


                readReciteBasicData.getUsers().add(userVoice);


                LinkedHashMap<String, String> answers = newHomeworkResultAppAnswer.getAnswers();

                List<String> voiceUrls = new LinkedList<>();

                readReciteBasicData.setPersonalVoiceToApp(voiceUrls);

                for (ReadReciteBasicData.ParagraphDetailed paragraphDetailed : readReciteBasicData.getParagraphDetaileds()) {
                    if (answers.containsKey(paragraphDetailed.getQuestionId()) && reportRateContext.getNewHomeworkProcessResultMap().containsKey(answers.get(paragraphDetailed.getQuestionId()))) {
                        NewHomeworkProcessResult newHomeworkProcessResult = reportRateContext.getNewHomeworkProcessResultMap().get(answers.get(paragraphDetailed.getQuestionId()));
                        if (newHomeworkProcessResult != null) {
                            paragraphDetailed.setPersonalVoiceToParagraph(CollectionUtils.isNotEmpty(newHomeworkProcessResult.getFiles()) ?
                                    newHomeworkProcessResult
                                            .getFiles()
                                            .stream()
                                            .flatMap(Collection::stream)
                                            .map(NewHomeworkQuestionFileHelper::getFileUrl)
                                            .collect(Collectors.toList()) :
                                    Collections.emptyList());
                            voiceUrls.addAll(paragraphDetailed.getPersonalVoiceToParagraph());
                        }
                    }
                }


                userVoice.setUserId(reportRateContext.getNewHomeworkResult().getUserId());


                int time = new BigDecimal(newHomeworkResultAppAnswer.processDuration())
                        .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                        .intValue();

                String duration = NewHomeworkUtils.handlerEnTime(time);

                userVoice.setDuration(duration);

                userVoice.setUserName(reportRateContext.getUser().fetchRealname());

                userVoice.setShowPics(voiceUrls);


                userVoice.setReview(SafeConverter.toBoolean(newHomeworkResultAppAnswer.getReview()));

                userVoice.setCorrection(SafeConverter.toString(newHomeworkResultAppAnswer.getCorrection(), ""));

                if (userVoice.isReview()) {
                    userVoice.setCorrect_des(newHomeworkResultAppAnswer.getCorrection() != null ? SafeConverter.toString(newHomeworkResultAppAnswer.getCorrection().getDescription(), "") : "阅");
                } else {
                    userVoice.setCorrect_des("未批改");
                }


                if (newHomeworkResultAppAnswer.getCorrection() != null) {
                    readReciteBasicData.setCorrectionInfo(newHomeworkResultAppAnswer.getCorrection().getDescription());
                    readReciteBasicData.setCorrected(true);
                } else if (newHomeworkResultAppAnswer.getReview() != null) {
                    readReciteBasicData.setCorrected(true);
                    readReciteBasicData.setCorrectionInfo("阅");
                } else {
                    readReciteBasicData.setCorrectionInfo("未批改");
                }

            }

            reportRateContext.getResultMap().put(reportRateContext.getType(), readReciteData);
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
        List<String> qids = targetApp.getQuestions().stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList());

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

        Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(qids);
        List<Long> chineseSentenceIds = questionMap.values()
                .stream()
                .filter(Objects::nonNull)
                .filter(question -> CollectionUtils.isNotEmpty(question.getSentenceIds()))
                .map(o -> o.getSentenceIds().get(0))
                .collect(Collectors.toList());

        List<ChineseSentence> chineseSentences = chineseContentLoaderClient.loadChineseSentenceByIds(chineseSentenceIds);
        //****** end 初始数据准备 *********//


        //******** begin 形成自然段的信息Map<Qid,自然段> *************//
        Map<String, Integer> qidToParagraph = new LinkedHashMap<>();
        Map<String, Boolean> qidToDiffMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(chineseSentences)) {
            Map<Long, ChineseSentence> mapChineseSentences = chineseSentences.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(AbstractDatabaseEntity::getId, Function.identity()));
            for (NewQuestion newQuestion : questionMap.values()) {
                List<Long> sentenceIds = newQuestion.getSentenceIds();
                if (CollectionUtils.isNotEmpty(sentenceIds)) {
                    ChineseSentence chineseSentence = mapChineseSentences.get(sentenceIds.get(0));
                    if (chineseSentence == null)
                        continue;
                    qidToParagraph.put(newQuestion.getId(), chineseSentence.getParagraph());
                    qidToDiffMap.put(newQuestion.getId(), SafeConverter.toBoolean(chineseSentence.getReciteParagraph()));
                }
            }
        }
        //******** end 形成自然段的信息Map<Qid,自然段> *************//

        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(Collections.singleton(targetApp.getLessonId()));
        NewReadReciteAppPart newReadReciteAppPart = new NewReadReciteAppPart();
        if (newBookCatalogMap.containsKey(targetApp.getLessonId())) {
            newReadReciteAppPart.setParagraphName(newBookCatalogMap.get(targetApp.getLessonId()).getName());
        }
        newReadReciteAppPart.setQuestionBoxType(targetApp.getQuestionBoxType());

        //************* begin 每个小题数据信息 ： NewReadReciteAppPart.ParagraphDetailed 表示每个题的信息结构 ********//
        for (String qid : qids) {
            if (!questionMap.containsKey(qid)) continue;
            NewReadReciteAppPart.ParagraphDetailed paragraphDetailed = new NewReadReciteAppPart.ParagraphDetailed();
            newReadReciteAppPart.getParagraphDetaileds().add(paragraphDetailed);
            paragraphDetailed.setQuestionId(qid);
            if (qidToParagraph.containsKey(qid)) {
                paragraphDetailed.setParagraphOrder(SafeConverter.toString(qidToParagraph.get(qid),""));
            }
            paragraphDetailed.setParagraphDifficultyType(SafeConverter.toBoolean(qidToDiffMap.get(qid)));
        }
        //************* end 每个小题数据信息 ********//


        Map<Long, NewReadReciteAppPart.NewReadReciteUser> readReciteUserMap = new LinkedHashMap<>();

        //************* begin 学生完成部分信息 ********//
        for (NewHomeworkResult r : newHomeworkResultMap.values()) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = r.getPractices().get(type);
            if (newHomeworkResultAnswer.getAppAnswers() == null) continue;
            if (newHomeworkResultAnswer.getAppAnswers().containsKey(parameter.getQuestionBoxId())) {
                NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(parameter.getQuestionBoxId());
                NewReadReciteAppPart.NewReadReciteUser user = new NewReadReciteAppPart.NewReadReciteUser();
                int time = new BigDecimal(newHomeworkResultAppAnswer.processDuration())
                        .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                        .intValue();
                String duration = NewHomeworkUtils.handlerEnTime(time);
                user.setUserId(r.getUserId());
                readReciteUserMap.put(r.getUserId(), user);
                user.setDuration(duration);
                user.setUserName(userMap.containsKey(r.getUserId()) ? userMap.get(r.getUserId()).fetchRealname() : "");
                user.setReview(SafeConverter.toBoolean(newHomeworkResultAppAnswer.getReview()));
                user.setCorrection(SafeConverter.toString(newHomeworkResultAppAnswer.getCorrection(), ""));
                if (user.isReview()) {
                    user.setCorrect_des(newHomeworkResultAppAnswer.getCorrection() != null ? SafeConverter.toString(newHomeworkResultAppAnswer.getCorrection().getDescription(), "") : "阅");
                } else {
                    user.setCorrect_des("未批改");
                }
                newReadReciteAppPart.getUsers().add(user);
            }
        }
        Map<String, List<NewHomeworkProcessResult>> qidToProcess = newHomeworkProcessResultMap.values()
                .stream()
                .collect(Collectors.groupingBy(BaseHomeworkProcessResult::getQuestionId));

        for (String qid : qids) {
            if (!qidToProcess.containsKey(qid))
                continue;
            List<NewHomeworkProcessResult> newHomeworkProcessResults = qidToProcess.get(qid);
            if (CollectionUtils.isNotEmpty(newHomeworkProcessResults)) {
                for (NewHomeworkProcessResult newHomeworkProcessResult : newHomeworkProcessResults) {
                    if (readReciteUserMap.containsKey(newHomeworkProcessResult.getUserId()) && CollectionUtils.isNotEmpty(newHomeworkProcessResult
                            .getFiles())) {
                        NewReadReciteAppPart.NewReadReciteUser user = readReciteUserMap.get(newHomeworkProcessResult.getUserId());
                        user.getShowPics().addAll(newHomeworkProcessResult
                                .getFiles()
                                .stream()
                                .flatMap(Collection::stream)
                                .map(NewHomeworkQuestionFileHelper::getFileUrl)
                                .collect(Collectors.toList()));
                    }
                }
            }

        }


        //************* end 学生完成部分信息 ********//
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("newReadReciteAppPart", newReadReciteAppPart);
        context.setMapMessage(mapMessage);
    }

    @Override
    public void fetchNewHomeworkSingleQuestionPart(ObjectiveConfigTypePartContext context) {
        context.setMapMessage(MapMessage.errorMessage());
    }

    @Override
    public String processStudentPartTypeScore(NewHomework newHomework, NewHomeworkResultAnswer newHomeworkResultAnswer, ObjectiveConfigType type) {
        List<String> readCorrections = new ArrayList<>();
        List<String> reciteCorrections = new ArrayList<>();
        boolean isAllUncorrected = true;
        for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
            String correction;
            if (appAnswer.getCorrection() != null) {
                correction = appAnswer.getCorrection().getDescription();
                isAllUncorrected = false;
            } else if (appAnswer.getReview() != null) {
                correction = "阅";
                isAllUncorrected = false;
            } else {
                correction = "未批改";
            }
            if (appAnswer.getQuestionBoxType() == QuestionBoxType.READ) {
                readCorrections.add(correction);
            } else {
                reciteCorrections.add(correction);
            }
        }
        readCorrections.addAll(reciteCorrections);
        String result;
        if (isAllUncorrected) {
            result = "未批改";
        } else {
            result = StringUtils.join(readCorrections, ",");
        }
        return result;
    }


    private void handleReadReciteBasicData(List<ReadReciteBasicData> readReciteBasicDatas, Map<Long, User> userMap) {

        readReciteBasicDatas.forEach(o -> {
            o.setTotalNum(userMap.size());
            o.setFinishedNum(o.getUsers().size());
            int correctionData;

            boolean hasChecked = false;//是否有批改的

            boolean allChecked = true;//是否全部批改

            for (ReadReciteBasicData.UserVoice userVoice : o.getUsers()) {
                if (userVoice.getCorrect_des().equals("未批改")) {
                    allChecked = false;
                } else {
                    hasChecked = true;
                }
            }
            if (allChecked) {
                correctionData = 1;
            } else {
                if (hasChecked) {
                    correctionData = 2;
                } else {
                    correctionData = 3;
                }
            }


            //
            o.setMasterIndex(o.getParagraphDetaileds().stream()
                    .filter(ReadReciteBasicData.ParagraphDetailed::isParagraphDifficultyType)
                    .map(ReadReciteBasicData.ParagraphDetailed::getParagraphOrder)
                    .collect(Collectors.toList()));

            o.setCorrectionData(correctionData);
        });


    }


    public void handleChineseReadRecite(NewHomeworkPracticeContent target, Map<String, NewQuestion> allNewQuestionMap, ReadReciteData readReciteData, Map<String, ReadReciteBasicData> readReciteBasicDataMap) {


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

            for (NewHomeworkQuestion newHomeworkQuestion : questionList) {
                if (!allNewQuestionMap.containsKey(newHomeworkQuestion.getQuestionId()))
                    continue;
                NewQuestion newQuestion = allNewQuestionMap.get(newHomeworkQuestion.getQuestionId());
                List<Long> sentenceIds = newQuestion.getSentenceIds();
                if (CollectionUtils.isNotEmpty(sentenceIds)) {
                    ChineseSentence chineseSentence = mapChineseSentences.get(sentenceIds.get(0));
                    if (chineseSentence == null)
                        continue;

                    qidToParagraph.put(newQuestion.getId(), chineseSentence.getParagraph());
                    qidToDiffMap.put(newQuestion.getId(), chineseSentence.getReciteParagraph());
                }
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
            ReadReciteBasicData readReciteBasicData = new ReadReciteBasicData();
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

            //包里面包含的自然段的锻炼编号
            List<String> paragraphDescriptions = new LinkedList<>();
            List<NewHomeworkQuestion> questions = newHomeworkApp.getQuestions();

            //包里面的各个自然段
            for (NewHomeworkQuestion newHomeworkQuestion : questions) {

                //一个自然段
                ReadReciteBasicData.ParagraphDetailed paragraphDetailed = new ReadReciteBasicData.ParagraphDetailed();

                //各个字段段
                readReciteBasicData.getParagraphDetaileds().add(paragraphDetailed);

                paragraphDetailed.setQuestionId(newHomeworkQuestion.getQuestionId());

                if (qidToParagraph.containsKey(newHomeworkQuestion.getQuestionId())) {
                    paragraphDetailed.setParagraphOrder(SafeConverter.toString(qidToParagraph.get(newHomeworkQuestion.getQuestionId()), ""));
                    paragraphDescriptions.add(paragraphDetailed.getParagraphOrder());
                }
                paragraphDetailed.setParagraphDifficultyType(SafeConverter.toBoolean(qidToDiffMap.get(newHomeworkQuestion.getQuestionId())));
            }
            readReciteBasicData.setParagraphDescription(StringUtils.join(paragraphDescriptions.toArray(), ","));
            if (newHomeworkApp.getQuestionBoxType() == QuestionBoxType.READ) {
                readReciteData.getReadData().add(readReciteBasicData);
            } else {
                readReciteData.getReciteData().add(readReciteBasicData);
            }
            readReciteBasicDataMap.put(newHomeworkApp.getQuestionBoxId(), readReciteBasicData);
        }
    }

}
