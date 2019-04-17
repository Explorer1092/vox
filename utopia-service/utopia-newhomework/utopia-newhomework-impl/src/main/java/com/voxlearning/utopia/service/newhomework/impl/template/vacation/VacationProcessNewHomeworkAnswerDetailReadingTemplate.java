package com.voxlearning.utopia.service.newhomework.impl.template.vacation;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.api.constant.ScoreLevel;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.vacationhomework.ReportRateContext;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.PictureBook;
import com.voxlearning.utopia.service.question.api.entity.PictureBookSeries;
import com.voxlearning.utopia.service.question.api.entity.PictureBookTopic;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class VacationProcessNewHomeworkAnswerDetailReadingTemplate extends VacationProcessNewHomeworkAnswerDetailTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.READING;
    }

    @Override
    public void processNewHomeworkAnswerDetailPersonal(ReportRateContext reportRateContext) {
        VacationHomework newHomework = reportRateContext.getNewHomework();
        NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.READING);
        if (newHomeworkPracticeContent == null)
            return;
        if (CollectionUtils.isEmpty(newHomeworkPracticeContent.getApps()))
            return;
        VacationHomeworkResult newHomeworkResult = reportRateContext.getNewHomeworkResult();
        if (MapUtils.isEmpty(newHomeworkResult.getPractices()))
            return;
        ObjectiveConfigType type = reportRateContext.getType();
        if (!newHomeworkResult.getPractices().containsKey(type))
            return;
        NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
        int totalPictureBook;
        int finishedCount;
        double sumScore = 0;
        Long sumDuration = 0L;
        //学生完成情况
        List<Map<String, Object>> studentAchievement = new LinkedList<>();

        List<String> pictureBookIds = newHomeworkPracticeContent
                .getApps()
                .stream()
                .map(NewHomeworkApp::getPictureBookId)
                .collect(Collectors.toList());
        totalPictureBook = pictureBookIds.size();
        Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(pictureBookIds);

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
            String pictureBookId = app.getPictureBookId();
            if (pictureBookId == null)
                continue;
            if (!pictureBookMap.containsKey(pictureBookId))
                continue;
            PictureBook pictureBook = pictureBookMap.get(app.getPictureBookId());
            if (!appAnswers.containsKey(pictureBookId))
                continue;
            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = appAnswers.get(pictureBookId);
            Map<String, Object> pictureBookInfo = NewHomeworkContentDecorator.decoratePictureBook(pictureBook, pictureBookSeriesMap, pictureBookTopicMap, null, null);
            if (newHomeworkResultAppAnswer.getScore() != null) {
                int score = new BigDecimal(newHomeworkResultAppAnswer.getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                pictureBookInfo.put("score", score);
                pictureBookInfo.put("scoreLevel", ScoreLevel.processLevel(score).getLevel());
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
        int avgScore = finishedCount > 0 ?
                new BigDecimal(sumScore)
                        .divide(new BigDecimal(finishedCount), 0, BigDecimal.ROUND_HALF_UP)
                        .intValue() :
                0;
        int avgDuration = finishedCount > 0 ?
                new BigDecimal(sumDuration)
                        .divide(new BigDecimal(finishedCount), 0, BigDecimal.ROUND_HALF_UP)
                        .intValue() :
                0;
        reportRateContext.getResultMap().put(ObjectiveConfigType.READING,
                MapUtils.m(
                        "avgScore", avgScore,
                        "avgScoreLevel", ScoreLevel.processLevel(avgScore).getLevel(),
                        "avgDuration", avgDuration,
                        "totalPictureBook", totalPictureBook,
                        "finishedCount", finishedCount,
                        "finished", finishedCount == totalPictureBook,
                        "studentAchievement", studentAchievement
                ));
    }
}
