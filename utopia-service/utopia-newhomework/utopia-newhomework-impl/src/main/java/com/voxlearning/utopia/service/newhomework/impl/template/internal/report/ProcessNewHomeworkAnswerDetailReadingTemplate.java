package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypeParameter;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypePartContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportPersonalRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.ReadingAppPart;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.PictureBook;
import com.voxlearning.utopia.service.question.api.entity.PictureBookSeries;
import com.voxlearning.utopia.service.question.api.entity.PictureBookTopic;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class ProcessNewHomeworkAnswerDetailReadingTemplate extends ProcessNewHomeworkAnswerDetailCommonTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.READING;
    }

    @Override
    public void processNewHomeworkAnswerDetail(ReportRateContext reportRateContext) {
        ObjectiveConfigType type = reportRateContext.getType();
        Map<String, Object> readingReport = new LinkedHashMap<>();
        List<Map<String, Object>> pictureBooksInfo = new LinkedList<>();
        Map<String, List<Map<String, Object>>> studentsInfo = new LinkedHashMap<>();
        Map<String, NewHomeworkResult> newHomeworkResultMapToObjectiveConfigType = handlerNewHomeworkResultMap(reportRateContext.getNewHomeworkResultMap(), type);
        if (MapUtils.isEmpty(newHomeworkResultMapToObjectiveConfigType)) {
            return;
        }
        NewHomeworkPracticeContent newHomeworkPracticeContent = reportRateContext.getNewHomework().findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (Objects.nonNull(newHomeworkPracticeContent) &&
                CollectionUtils.isNotEmpty(newHomeworkPracticeContent.getApps())) {
            List<String> pictureBookIds = newHomeworkPracticeContent
                    .getApps()
                    .stream()
                    .map(NewHomeworkApp::getPictureBookId)
                    .collect(Collectors.toList());
            Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(pictureBookIds);
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
            for (NewHomeworkApp app : newHomeworkPracticeContent.getApps()) {
                Map<String, Object> pictureBookInfo = new LinkedHashMap<>();
                pictureBooksInfo.add(pictureBookInfo);
                if (app.getPictureBookId() != null) {
                    PictureBook pictureBook = pictureBookMap.get(app.getPictureBookId());
                    if (pictureBook == null) {
                        continue;
                    }
                    pictureBookInfo.putAll(NewHomeworkContentDecorator.decoratePictureBook(pictureBook, pictureBookSeriesMap, pictureBookTopicMap, null, null));
                    List<Map<String, Object>> studentInfo = new LinkedList<>();
                    double sumScore = 0;
                    int finishedCount = 0;
                    Long sumDuration = 0L;
                    for (String newHomeworkResultKey : newHomeworkResultMapToObjectiveConfigType.keySet()) {
                        NewHomeworkResult newHomeworkResult = newHomeworkResultMapToObjectiveConfigType.get(newHomeworkResultKey);
                        if (MapUtils.isNotEmpty(newHomeworkResult.getPractices()) &&
                                newHomeworkResult.getPractices().containsKey(type) &&
                                MapUtils.isNotEmpty(newHomeworkResult.getPractices().get(type).getAppAnswers()) &&
                                newHomeworkResult.getPractices().get(type).getAppAnswers().keySet().contains(pictureBook.getId())) {
                            Map<String, Object> studentAchievement = new LinkedHashMap<>();
                            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResult
                                    .getPractices()
                                    .get(type)
                                    .getAppAnswers()
                                    .get(pictureBook.getId());
                            User user = reportRateContext.getUserMap().get(newHomeworkResult.getUserId());
                            studentAchievement.put("userId", newHomeworkResult.getUserId());
                            studentAchievement.put("userName", user != null ? user.fetchRealname() : "");
                            if (newHomeworkResultAppAnswer.getScore() != null) {
                                int score = new BigDecimal(newHomeworkResultAppAnswer.getScore())
                                        .setScale(0, BigDecimal.ROUND_HALF_UP)
                                        .intValue();
                                sumScore += score;
                                studentAchievement.put("score", score);
                            } else {
                                studentAchievement.put("score", null);
                            }
                            if (newHomeworkResultAppAnswer.getDuration() != null) {
                                int duration = new BigDecimal(newHomeworkResultAppAnswer.processDuration())
                                        .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                                        .intValue();
                                studentAchievement.put("duration", duration);
                                sumDuration += duration;
                            } else {
                                studentAchievement.put("duration", null);
                            }
                            finishedCount++;
                            studentAchievement.put("finishAt", newHomeworkResultAppAnswer.getFinishAt());
                            studentInfo.add(studentAchievement);
                        }
                    }
                    BigDecimal _avgScore = new BigDecimal(finishedCount > 0 ? sumScore / finishedCount : 0);
                    int avgScore = _avgScore.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                    BigDecimal _avgDuration = new BigDecimal(sumDuration > 0 ? sumDuration / finishedCount : 0);
                    Long avgDuration = _avgDuration.setScale(0, BigDecimal.ROUND_UP).longValue();
                    pictureBookInfo.put("hasRepeat",CollectionUtils.isNotEmpty(app.getOralQuestions()));
                    pictureBookInfo.put("hasExercises",CollectionUtils.isNotEmpty(app.getQuestions()));
                    pictureBookInfo.put("avgDuration", avgDuration);
                    pictureBookInfo.put("avgScore", avgScore);
                    pictureBookInfo.put("finishedCount", finishedCount);
                    pictureBookInfo.put("totalUserNum", MapUtils.isNotEmpty(reportRateContext.getUserMap()) ? reportRateContext.getUserMap().keySet().size() : 0);
                    pictureBookInfo.put("studentInfo", studentInfo);
                    studentsInfo.put(app.getPictureBookId(), studentInfo);
                }
            }
        }
        readingReport.put("pictureBookInfo", pictureBooksInfo);
        readingReport.put("studentsInfo", studentsInfo);

        if (MapUtils.isNotEmpty(readingReport)) {
            reportRateContext.getResult().put(type.name(), readingReport);
        }
    }

    @Override
    public void processNewHomeworkAnswerDetailPersonal(ReportPersonalRateContext reportRateContext) {
        ObjectiveConfigType type = reportRateContext.getType();
        int totalPictureBook = 0;
        int finishedCount = 0;
        double sumScore = 0;
        Long sumDuration = 0L;
        List<Map<String, Object>> studentAchievement = new LinkedList<>();
        NewHomeworkPracticeContent newHomeworkPracticeContent = reportRateContext.getNewHomework().findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (Objects.nonNull(newHomeworkPracticeContent)
                && CollectionUtils.isNotEmpty(newHomeworkPracticeContent.getApps())
                && MapUtils.isNotEmpty(reportRateContext.getNewHomeworkResult().getPractices())
                && reportRateContext.getNewHomeworkResult().getPractices().containsKey(type)
                && MapUtils.isNotEmpty(reportRateContext.getNewHomeworkResult().getPractices().get(type).getAppAnswers())) {
            List<String> pictureBookIds = newHomeworkPracticeContent
                    .getApps()
                    .stream()
                    .map(NewHomeworkApp::getPictureBookId)
                    .collect(Collectors.toList());
            totalPictureBook = pictureBookIds.size();
            Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(pictureBookIds);
            NewHomeworkResultAnswer newHomeworkResultAnswer = reportRateContext.getNewHomeworkResult().getPractices().get(type);
            LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = newHomeworkResultAnswer.getAppAnswers();
            finishedCount = appAnswers.size();
            List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries();
            Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList
                    .stream()
                    .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));
            List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics();
            Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList.stream()
                    .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));
            for (NewHomeworkApp app : newHomeworkPracticeContent.getApps()) {
                if (app.getPictureBookId() != null
                        && appAnswers.containsKey(app.getPictureBookId())) {
                    NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = appAnswers.get(app.getPictureBookId());
                    PictureBook pictureBook = pictureBookMap.get(app.getPictureBookId());
                    Map<String, Object> pictureBookInfo = NewHomeworkContentDecorator.decoratePictureBook(pictureBook, pictureBookSeriesMap, pictureBookTopicMap, null, null);
                    pictureBookInfo.put("hasRepeat",CollectionUtils.isNotEmpty(app.getOralQuestions()));
                    pictureBookInfo.put("hasExercises",CollectionUtils.isNotEmpty(app.getQuestions()));
                    if (newHomeworkResultAppAnswer.getScore() != null) {
                        int score = new BigDecimal(newHomeworkResultAppAnswer.getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                        pictureBookInfo.put("score", score);
                        sumScore += score;
                    } else {
                        pictureBookInfo.put("score", null);
                    }
                    if (newHomeworkResultAppAnswer.getDuration() != null) {
                        int duration = new BigDecimal(newHomeworkResultAppAnswer.processDuration())
                                .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                                .intValue();
                        pictureBookInfo.put("duration", duration);
                        sumDuration += duration;
                    } else {
                        pictureBookInfo.put("duration", null);
                    }
                    studentAchievement.add(pictureBookInfo);
                }
            }
        }

        double avgScore = finishedCount > 0 ?
                new BigDecimal(sumScore)
                        .divide(new BigDecimal(finishedCount), 0, BigDecimal.ROUND_HALF_UP)
                        .intValue() :
                0;
        int avgDuration = finishedCount > 0 ?
                new BigDecimal(sumDuration)
                        .divide(new BigDecimal(finishedCount), 0, BigDecimal.ROUND_HALF_UP)
                        .intValue() :
                0;

        reportRateContext.getResultMap().put(type,
                MapUtils.m(
                        "avgScore", avgScore,
                        "avgDuration", avgDuration,
                        "totalPictureBook", totalPictureBook,
                        "finishedCount", finishedCount,
                        "finished", finishedCount == totalPictureBook,
                        "studentAchievement", studentAchievement
                ));


    }

    @Override
    public void fetchNewHomeworkCommonObjectiveConfigTypePart(ObjectiveConfigTypePartContext context) {
        //************* begin 初始化数据准备 ********//
        Map<Long, User> userMap = context.getUserMap();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = context.getNewHomeworkResultMap();
        ObjectiveConfigTypeParameter parameter = context.getParameter();
        Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(Collections.singleton(parameter.getPictureBookId()));
        if (!pictureBookMap.containsKey(parameter.getPictureBookId())) {
            MapMessage mapMessage = MapMessage.errorMessage("绘本不存在");
            context.setMapMessage(mapMessage);
            return;
        }
        PictureBook pictureBook = pictureBookMap.get(parameter.getPictureBookId());
        ObjectiveConfigType type = context.getType();
        //************* end 初始化数据准备 ********//



        ReadingAppPart readingAppPart = new ReadingAppPart();
        readingAppPart.setPictureBookId(parameter.getPictureBookId());
        readingAppPart.setPictureBookName(pictureBook.getName());

        //********** begin 数据处理：ReadingAppPart 是数据返回的结构，ReadingAppPart.ReadingAppUser 是个人成绩的数据结构 ***********//
        for (NewHomeworkResult r : newHomeworkResultMap.values()) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = r.getPractices().get(type);
            if (newHomeworkResultAnswer.getAppAnswers().containsKey(parameter.getPictureBookId())) {
                NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(parameter.getPictureBookId());
                ReadingAppPart.ReadingAppUser user = new ReadingAppPart.ReadingAppUser();
                user.setUid(r.getUserId());
                user.setUserName(userMap.containsKey(r.getUserId()) ? userMap.get(r.getUserId()).fetchRealname() : "");
                int score = new BigDecimal(SafeConverter.toDouble(newHomeworkResultAppAnswer.getScore())).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                user.setScore(score);
                readingAppPart.getUsers().add(user);
            }
        }
        //********** end 数据处理：ReadingAppPart 是数据返回的结构，ReadingAppPart.ReadingAppUser 是个人成绩的数据结构 ***********//

        MapMessage mapMessage = MapMessage.successMessage();
        context.setMapMessage(mapMessage);
        mapMessage.set("readingAppPart", readingAppPart);
    }

    @Override
    public void fetchNewHomeworkSingleQuestionPart(ObjectiveConfigTypePartContext context) {
        context.setMapMessage(MapMessage.errorMessage());
    }


    @Override
    public void processQuestionPartTypeInfo(Map<Long, NewHomeworkResult> newHomeworkResultMap, NewHomework newHomework, ObjectiveConfigType type, Map<ObjectiveConfigType, Object> result, String cdnBaseUrl) {
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap
                .values()
                .stream().filter(o -> o.isFinishedOfObjectiveConfigType(type))
                .collect(Collectors.toList());
        Map<String, Object> typeResult = new LinkedHashMap<>();
        typeResult.put("type", type);
        typeResult.put("typeName", type.getValue());
        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            typeResult.put("finishCount", 0);
            result.put(type, typeResult);
            return;
        }

        int finishCount = 0;
        int totalDuration = 0;
        int totalScore = 0;
        for (NewHomeworkResult newHomeworkResult : newHomeworkResults) {
            finishCount++;
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
            Integer duration = newHomeworkResultAnswer.processDuration();
            totalDuration += SafeConverter.toInt(new BigDecimal(SafeConverter.toInt(duration)).divide(new BigDecimal(60), 0, BigDecimal.ROUND_UP).intValue());
            totalScore += SafeConverter.toInt(SafeConverter.toInt(newHomeworkResultAnswer.processScore(type)));
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
        result.put(type, typeResult);
    }
}
