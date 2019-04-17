package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.helper.VoiceEngineTypeUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypeParameter;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypePartContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportPersonalRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.WordRecognitionAndReadingAppInfo;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.WordRecognitionAndReadingAppPart;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.reading.WordRecognitionAndReadingBasicData;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.reading.WordRecognitionAndReadingDetail;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/7/23
 * \* Time: 下午6:20
 * \* Description:生字认读报告详情
 * \
 */
@Named
public class ProcessNewHomeworkAnswerDetailWordRecognitionAndReadingTemplate extends ProcessNewHomeworkAnswerDetailCommonTemplate{
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.WORD_RECOGNITION_AND_READING;
    }

    /**
     * 处理学生成绩表格数据tabinfo
     * @param newHomework
     * @param newHomeworkResultAnswer
     * @param type
     * @return
     */
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
            if (value >= NewHomeworkConstants.WORD_RECOGNITION_AND_READING_STANDARD) {
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
                if (value >= NewHomeworkConstants.WORD_RECOGNITION_AND_READING_STANDARD) {
                    standardNum++;
                }
            }
            return standardNum + "/" + newHomeworkResultAnswer.getAppAnswers().size() + "达标";
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
            MapMessage mapMessage = MapMessage.errorMessage("不存在对应生字认读部分" + parameter.getQuestionBoxId());
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
        WordRecognitionAndReadingAppPart wordRecognitionAndReadingAppPart=new WordRecognitionAndReadingAppPart();
        wordRecognitionAndReadingAppPart.setUserCount(newHomeworkResultMap.size());
        if (newBookCatalogMap.containsKey(targetApp.getLessonId())) {
            wordRecognitionAndReadingAppPart.setLessonName(newBookCatalogMap.get(targetApp.getLessonId()).getName());
        }
        Map<Long, WordRecognitionAndReadingAppPart.WordRecognitionAndReadingAppPartUser> readReciteUserMap = Maps.newLinkedHashMap();
        //************* begin 学生完成部分信息 ********//
        for (NewHomeworkResult r : newHomeworkResultMap.values()) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = r.getPractices().get(type);
            if (newHomeworkResultAnswer.getAppAnswers() == null) continue;
            if (newHomeworkResultAnswer.getAppAnswers().containsKey(parameter.getQuestionBoxId())) {
                NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(parameter.getQuestionBoxId());
                WordRecognitionAndReadingAppPart.WordRecognitionAndReadingAppPartUser user = new WordRecognitionAndReadingAppPart.WordRecognitionAndReadingAppPartUser();
                readReciteUserMap.put(r.getUserId(), user);
                double value = new BigDecimal(SafeConverter.toInt(newHomeworkResultAppAnswer.getStandardNum()) * 100).divide(new BigDecimal(targetApp.getQuestions().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                if (value >= NewHomeworkConstants.WORD_RECOGNITION_AND_READING_STANDARD) {
                    user.setStandard(true);
                    wordRecognitionAndReadingAppPart.setStandardUserCount(1 + wordRecognitionAndReadingAppPart.getStandardUserCount());
                }
                int time = new BigDecimal(newHomeworkResultAppAnswer.processDuration())
                        .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                        .intValue();
                String duration = NewHomeworkUtils.handlerEnTime(time);
                user.setDuration(duration);
                user.setUserId(r.getUserId());
                user.setUserName(userMap.containsKey(r.getUserId()) ? userMap.get(r.getUserId()).fetchRealname() : "");
                wordRecognitionAndReadingAppPart.getUsers().add(user);
            }
        }
        for (NewHomeworkProcessResult p : newHomeworkProcessResultMap.values()) {
            if (!readReciteUserMap.containsKey(p.getUserId()))
                continue;
            WordRecognitionAndReadingAppPart.WordRecognitionAndReadingAppPartUser user = readReciteUserMap.get(p.getUserId());
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
        mapMessage.add("readReciteWithScoreAppPart", wordRecognitionAndReadingAppPart);
        context.setMapMessage(mapMessage);

    }

    /**
     * 处理题目详情
     * @param reportRateContext
     */
    @Override
    public void processNewHomeworkAnswerDetailPersonal(ReportPersonalRateContext reportRateContext) {
        List<WordRecognitionAndReadingBasicData> answerDetailData = Lists.newArrayList();
        ObjectiveConfigType type = reportRateContext.getType();
        Map<String, WordRecognitionAndReadingBasicData> basicDataHashMap = new HashMap<>();
        NewHomeworkPracticeContent target = reportRateContext.getNewHomework().findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        Map<String, NewQuestion> questionMap=reportRateContext.getAllNewQuestionMap();
        handleWordRecognitionAndReading(questionMap,target,answerDetailData, basicDataHashMap);
        NewHomeworkResultAnswer newHomeworkResultAnswer = reportRateContext.getNewHomeworkResult().getPractices().get(type);
        for (String questionBoxId : newHomeworkResultAnswer.getAppAnswers().keySet()) {
            if (!basicDataHashMap.containsKey(questionBoxId))
                continue;
            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(questionBoxId);
            WordRecognitionAndReadingBasicData basicData = basicDataHashMap.get(questionBoxId);
            LinkedHashMap<String, String> answers = newHomeworkResultAppAnswer.getAnswers();
            if (MapUtils.isEmpty(answers))
                continue;
            //是否合格
            double value = new BigDecimal(SafeConverter.toInt(newHomeworkResultAppAnswer.getStandardNum()) * 100).divide(new BigDecimal(newHomeworkResultAppAnswer.getAnswers().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            if (value >= NewHomeworkConstants.WORD_RECOGNITION_AND_READING_STANDARD) {
                basicData.setStandard(true);//是否合格
            }
            for(WordRecognitionAndReadingDetail detail : basicData.getDetailList()){
                if (!answers.containsKey(detail.getQuestionId()))
                    continue;
                String pid = answers.get(detail.getQuestionId());
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
                detail.setVoices(voices);
                detail.setStandard(SafeConverter.toBoolean(newHomeworkProcessResult.grasp));
                basicData.getVoices().addAll(voices);
            }
            basicData.setStandardStr(SafeConverter.toInt(newHomeworkResultAppAnswer.getStandardNum()) +"/"+newHomeworkResultAppAnswer.getAnswers().size()+"字达标");
            long appDuration = SafeConverter.toLong(newHomeworkResultAppAnswer.processDuration());
            int totalDuration = new BigDecimal(appDuration).divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP).intValue();
            basicData.setTotalDuration(NewHomeworkUtils.handlerEnTime(totalDuration));//总时长
        }
        reportRateContext.getResultMap().put(type, answerDetailData);

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
        List<WordRecognitionAndReadingBasicData> answerDetailData = Lists.newArrayList();//返回结果
        Map<String, WordRecognitionAndReadingBasicData> basicDataHashMap = new HashMap<>();//所有结果
        Map<String, NewQuestion> questionMap=reportRateContext.getAllNewQuestionMap();
        handleWordRecognitionAndReading(questionMap,target,answerDetailData, basicDataHashMap);
        Map<String, NewHomeworkApp> appMap = target.getApps().stream().collect(Collectors.toMap(NewHomeworkApp::getQuestionBoxId, Function.identity()));
        for (NewHomeworkResult newHomeworkResult : newHomeworkResultMapToObjectiveConfigType.values()) {
            if (!userMap.containsKey(newHomeworkResult.getUserId()))
                continue;
            User user = userMap.get(newHomeworkResult.getUserId());
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
            for (String questionBoxId : newHomeworkResultAnswer.getAppAnswers().keySet()) {
                if (!basicDataHashMap.containsKey(questionBoxId))
                    continue;
                WordRecognitionAndReadingBasicData basicData=basicDataHashMap.get(questionBoxId);
                WordRecognitionAndReadingBasicData.User su=new WordRecognitionAndReadingBasicData.User();
                NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(questionBoxId);
                int time = new BigDecimal(newHomeworkResultAppAnswer.processDuration())
                        .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                        .intValue();
                String duration = NewHomeworkUtils.handlerEnTime(time);
                su.setDuration(duration);
                su.setUserId(user.getId());
                su.setUserName(user.fetchRealnameIfBlankId());
                basicData.getUsers().add(su);
                basicData.setWordNum(appMap.get(questionBoxId).getQuestions().size());
                for (String pid : newHomeworkResultAppAnswer.getAnswers().values()) {
                    if (!reportRateContext.getNewHomeworkProcessResultMap().containsKey(pid))
                        continue;
                    NewHomeworkProcessResult p = reportRateContext.getNewHomeworkProcessResultMap().get(pid);
                    if (p == null)
                        continue;
                    List<String> voices = CollectionUtils.isNotEmpty(p.getOralDetails()) ?
                            p.getOralDetails()
                                    .stream()
                                    .flatMap(Collection::stream)
                                    .map(o -> VoiceEngineTypeUtils.getAudioUrl(o.getAudio(), p.getVoiceEngineType()))
                                    .collect(Collectors.toList()) :
                            Collections.emptyList();
                    su.getVoices().addAll(voices);
                }
                if (appMap.containsKey(questionBoxId)) {
                    NewHomeworkApp app = appMap.get(questionBoxId);
                    if (CollectionUtils.isEmpty(app.getQuestions()))
                        continue;
                    double value = new BigDecimal(SafeConverter.toInt(newHomeworkResultAppAnswer.getStandardNum()) * 100).divide(new BigDecimal(app.getQuestions().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    if (value >= NewHomeworkConstants.WORD_RECOGNITION_AND_READING_STANDARD) {
                        //判断是否达标
                        su.setStandard(true);
                    }
                }
            }
        }
        reportRateContext.getResult().put(type.name(), answerDetailData);
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
        List<WordRecognitionAndReadingAppInfo> wordAppInfoList = Lists.newLinkedList();
        Map<String, WordRecognitionAndReadingAppInfo> wordAppInfoMap = new LinkedHashMap<>();
        for (NewHomeworkApp app : target.getApps()) {
            if (!newBookCatalogMap.containsKey(app.getLessonId()))
                continue;
            NewBookCatalog newBookCatalog = newBookCatalogMap.get(app.getLessonId());
            WordRecognitionAndReadingAppInfo recognitionAndReadingAppInfo=new WordRecognitionAndReadingAppInfo();
            recognitionAndReadingAppInfo.setLessonId(app.getLessonId());
            recognitionAndReadingAppInfo.setLessonName(newBookCatalog.getName());
            recognitionAndReadingAppInfo.setQuestionBoxId(app.getQuestionBoxId());
            wordAppInfoList.add(recognitionAndReadingAppInfo);
            wordAppInfoMap.put(app.getQuestionBoxId(), recognitionAndReadingAppInfo);
        }
        for (NewHomeworkResult newHomeworkResult : newHomeworkResults) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
            if (newHomeworkResultAnswer != null) {
                for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : newHomeworkResultAnswer.getAppAnswers().entrySet()) {
                    if (!appMap.containsKey(entry.getKey()))
                        continue;
                    NewHomeworkApp app = appMap.get(entry.getKey());
                    if (CollectionUtils.isEmpty(app.getQuestions()))
                        continue;
                    if (!wordAppInfoMap.containsKey(entry.getKey()))
                        continue;
                    WordRecognitionAndReadingAppInfo item = wordAppInfoMap.get(entry.getKey());
                    double value = new BigDecimal(SafeConverter.toInt(entry.getValue().getStandardNum()) * 100).divide(new BigDecimal(app.getQuestions().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    if (value >= NewHomeworkConstants.WORD_RECOGNITION_AND_READING_STANDARD) {
                        //判断是否达标
                        item.setStandardNum(1 + item.getStandardNum());//达标人数
                    }
                }
            }
        }
        int finishCount = newHomeworkResults.size();
        typeResult.put("finishCount", finishCount);//完成人数
        typeResult.put("appNum", appMap.size());
        typeResult.put("wordAppInfos",wordAppInfoList);
        result.put(type, typeResult);
    }

    private void handleWordRecognitionAndReading(Map<String, NewQuestion> questionMap,NewHomeworkPracticeContent target,  List<WordRecognitionAndReadingBasicData> wordRecognitionAndReadingBasicDataList , Map<String, WordRecognitionAndReadingBasicData> recognitionAndReadingBasicDataMap) {
        List<String> questionIds=Lists.newArrayList();
        List<String> lessonIds = target.getApps()
                .stream()
                .filter(Objects::nonNull)
                .filter(o -> Objects.nonNull(o.getLessonId()))
                .map(NewHomeworkApp::getLessonId)
                .collect(Collectors.toList());
        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        for (NewHomeworkApp newHomeworkApp : target.getApps()) {
            WordRecognitionAndReadingBasicData basicData = new WordRecognitionAndReadingBasicData();
            basicData.setQuestionBoxId(newHomeworkApp.getQuestionBoxId());
            if (newBookCatalogMap.containsKey(newHomeworkApp.getLessonId())) {
                NewBookCatalog newBookCatalog = newBookCatalogMap.get(newHomeworkApp.getLessonId());
                String lessonName = newBookCatalog.getAlias();
                if (StringUtils.isBlank(lessonName)) {
                    lessonName = newBookCatalog.getName();
                }
                basicData.setLessonName(lessonName); //lesson Name
            }
            List<NewHomeworkQuestion> questions = newHomeworkApp.getQuestions();
            //具体问题答案结果
            int order=0;
            for (NewHomeworkQuestion newHomeworkQuestion : questions) {
                order++;
                WordRecognitionAndReadingDetail detail=new WordRecognitionAndReadingDetail();
                detail.setQuestionId(newHomeworkQuestion.getQuestionId());
                detail.setOrder(order);//取question中index作为顺序
                NewQuestion question=questionMap.get(detail.getQuestionId());
                if (question != null
                        && question.getContent().getSubContents() != null
                        && question.getContent().getSubContents().size() > 0
                        && question.getContent().getSubContents().get(0).getExtras() != null) {
                    String pinyinMark=question.getContent().getSubContents().get(0).getExtras().get("wordContentPinyinMark");
                    String chineseWordContent=question.getContent().getSubContents().get(0).getExtras().get("chineseWordContent");
                    detail.setPinYinMark(pinyinMark);
                    detail.setChineseWordContent(chineseWordContent);
                }
                basicData.getDetailList().add(detail);
                questionIds.add(newHomeworkQuestion.getQuestionId());
            }
            wordRecognitionAndReadingBasicDataList.add(basicData);
            recognitionAndReadingBasicDataMap.put(newHomeworkApp.getQuestionBoxId(), basicData);
        }

    }


}
